package com.example.controller.player;

import com.example.controller.gemini.GeminiVisionController;
import com.example.dao.BattleDao;
import com.example.dao.StorageDao;
import com.example.keys.BattleFirebaseKeys;
import com.example.model.player.BattleModel;
import com.example.model.player.RoundModel;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.ListenerRegistration;
import com.google.firebase.cloud.FirestoreClient;

import java.io.File;
import java.util.function.Consumer;

public class BattleRoomController {

    private ListenerRegistration activeListener;

    /**
     * Attaches a real-time listener to a specific battle lobby.
     * Every time the database changes (someone joins, status changes), the UI
     * updates instantly.
     */
    public void listenToBattle(String battleId, Consumer<BattleModel> onUpdate) {
        DocumentReference docRef = FirestoreClient.getFirestore()
                .collection(BattleFirebaseKeys.COLLECTION_BATTLES).document(battleId);

        activeListener = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirestoreException e) {
                if (e != null) {
                    System.err.println("Listen failed: " + e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    BattleModel updatedBattle = snapshot.toObject(BattleModel.class);
                    if (updatedBattle != null) {
                        onUpdate.accept(updatedBattle); // Send new data back to the UI thread
                    }
                }
            }
        });
    }

    /**
     * Call this when the user leaves the screen to save memory and network calls.
     */
    public void stopListening() {
        if (activeListener != null) {
            activeListener.remove();
        }
    }

    /**
     * Submits a screenshot to Gemini, compares the claimed outcome, and pushes to
     * Firestore.
     */
    public void submitRoundEvidence(BattleModel battle, int roundNumber, String teamSide, String claimedOutcome,
            File screenshot, Consumer<Boolean> onComplete) {
        new Thread(() -> {
            try {
                // 1. Upload to Storage
                String uploadedUrl = StorageDao.uploadBattleEvidence(screenshot, battle.getBattleId(),
                        roundNumber, teamSide);

                // 2. Lightweight OCR Extraction
                GeminiVisionController.BattleResultExtraction ocrResult = GeminiVisionController
                        .extractBattleWinner(screenshot, "BGMI");

                RoundModel round = battle.getRounds().get(roundNumber - 1);
                if (teamSide.equals("A")) {
                    round.setTeamAClaimedOutcome(claimedOutcome);
                    round.setTeamAOcrResult(ocrResult.winnerSide);
                    round.setTeamAScreenshotUrl(uploadedUrl);
                } else {
                    round.setTeamBClaimedOutcome(claimedOutcome);
                    round.setTeamBOcrResult(ocrResult.winnerSide);
                    round.setTeamBScreenshotUrl(uploadedUrl);
                }

                // 3. Evaluate and Save
                evaluateRound(battle, roundNumber - 1);
                onComplete.accept(true);

            } catch (Exception e) {
                e.printStackTrace();
                onComplete.accept(false);
            }
        }).start();
    }

    public static void evaluateRound(BattleModel battle, int roundIndex) {
        RoundModel round = battle.getRounds().get(roundIndex);

        long currentTime = System.currentTimeMillis();
        // Use lockedAt if available, fallback to createdAt
        long startTime = battle.getLockedAt() > 0 ? battle.getLockedAt() : battle.getCreatedAt();
        long deadline = startTime + (45 * 60 * 1000);

        boolean hasClaimA = round.getTeamAClaimedOutcome() != null;
        boolean hasClaimB = round.getTeamBClaimedOutcome() != null;
        boolean timeExpired = currentTime > deadline;

        // SCENARIO 1: Both users submitted. Execute immediate AI comparison.
        if (hasClaimA && hasClaimB) {
            if (round.getTeamAOcrResult() != null && round.getTeamAOcrResult().equals(round.getTeamBOcrResult())
                    && !round.getTeamAOcrResult().equals("UNCLEAR")) {
                round.setWinningTeam(round.getTeamAOcrResult());
                round.setRoundStatus("VERIFIED");
            } else {
                round.setRoundStatus(BattleFirebaseKeys.STATUS_DISPUTED); // Conflicting evidence
            }
        }
        // SCENARIO 2: Timer expired, only ONE side submitted. Default win to the
        // submitter.
        else if (timeExpired && (hasClaimA ^ hasClaimB)) {
            round.setWinningTeam(hasClaimA ? round.getTeamAOcrResult() : round.getTeamBOcrResult());
            round.setRoundStatus("VERIFIED");
        }
        // SCENARIO 3: Timer expired, NO ONE submitted. Cancel and 80% refund.
        else if (timeExpired && !hasClaimA && !hasClaimB) {
            round.setRoundStatus("CANCELLED");
            battle.setStatus("CANCELLED");
            // NOTE: Add 80% coin refund ledger transaction here later
        }
        // SCENARIO 4: Waiting for the other player, timer still ticking.
        else {
            BattleDao.saveBattle(battle);
            return;
        }

        checkOverallBattleCompletion(battle);
    }

    private static void checkOverallBattleCompletion(BattleModel battle) {
        long aWins = battle.getRounds().stream().filter(r -> "A".equals(r.getWinningTeam())).count();
        long bWins = battle.getRounds().stream().filter(r -> "B".equals(r.getWinningTeam())).count();
        int winsNeeded = battle.getFormat().equals("BO3") ? 2 : 1;

        if (aWins >= winsNeeded) {
            // Send to Payout Engine (Winner: Team A, Refund: false)
            BattleDao.resolveBattleFinancials(battle.getBattleId(), BattleFirebaseKeys.STATUS_COMPLETED, "A", false);
        } else if (bWins >= winsNeeded) {
            // Send to Payout Engine (Winner: Team B, Refund: false)
            BattleDao.resolveBattleFinancials(battle.getBattleId(), BattleFirebaseKeys.STATUS_COMPLETED, "B", false);
        } else if (battle.getRounds().stream()
                .anyMatch(r -> BattleFirebaseKeys.STATUS_DISPUTED.equals(r.getRoundStatus()))) {
            battle.setStatus(BattleFirebaseKeys.STATUS_DISPUTED);
            BattleDao.saveBattle(battle); // Just freeze it for admin review, no money moves yet.
        } else if (battle.getStatus().equals("CANCELLED")) {
            // Send to Refund Engine (Winner: null, Refund: true)
            BattleDao.resolveBattleFinancials(battle.getBattleId(), "CANCELLED", null, true);
        } else {
            BattleDao.saveBattle(battle); // Standard mid-match save
        }
    }
}