package com.example.controller.player;

import com.example.controller.AuthController;
import com.example.controller.gemini.GeminiVisionController;
import com.example.dao.BattleDao;
import com.example.dao.StorageDao;
import com.example.keys.BattleFirebaseKeys;
import com.example.model.player.BattleModel;
import com.example.model.player.DualMatchResultModel;
import com.example.model.player.RoundModel;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.ListenerRegistration;
import com.google.firebase.cloud.FirestoreClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
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
     * Handles the upload of a single screenshot. If it's the second screenshot, it
     * triggers the Dual AI.
     */
    public void submitRoundEvidence(BattleModel battle, int roundNumber, String teamSide, String claimedOutcome,
            File screenshot, Consumer<Boolean> onComplete) {
        new Thread(() -> {
            try {
                // 1. Upload to Firebase Storage
                String uploadedUrl = StorageDao.uploadBattleEvidence(screenshot, battle.getBattleId(),
                        roundNumber, teamSide);

                // 2. Transactionally save URL and Claim to DB (No AI yet)
                boolean saved = BattleDao.saveEvidenceUrlTransaction(
                        battle.getBattleId(), roundNumber - 1, teamSide, claimedOutcome, uploadedUrl,
                        AuthController.currentUser.getUserId());

                if (saved) {
                    // 3. Fetch fresh battle to check if the opponent has ALSO submitted
                    BattleModel freshBattle = BattleDao.getBattleById(battle.getBattleId());
                    RoundModel round = freshBattle.getRounds().get(roundNumber - 1);

                    boolean hasA = round.getTeamAScreenshotUrl() != null;
                    boolean hasB = round.getTeamBScreenshotUrl() != null;

                    if (hasA && hasB && round.getWinningTeam() == null) {
                        // THE TRIGGER: Both images are present! Run the Dual-Image Referee Engine
                        triggerDualAIVerification(freshBattle, roundNumber - 1, screenshot, teamSide);
                    } else {
                        // Standard passive timeout check
                        evaluateRound(freshBattle, roundNumber - 1);
                    }
                }
                onComplete.accept(saved);

            } catch (Exception e) {
                e.printStackTrace();
                onComplete.accept(false);
            }
        }).start();
    }

    public static void evaluateRound(BattleModel battle, int roundIndex) {
        applyEvaluationLogic(battle, roundIndex);
        checkOverallBattleCompletion(battle);
    }

    private void triggerDualAIVerification(BattleModel battle, int roundIndex, File localScreenshot,
            String myTeamSide) {
        RoundModel round = battle.getRounds().get(roundIndex);
        String opponentUrl = myTeamSide.equals("A") ? round.getTeamBScreenshotUrl() : round.getTeamAScreenshotUrl();

        try {
            // 1. Fetch Opponent's Image into memory
            byte[] opponentImageBytes = downloadImageToBytes(opponentUrl);
            byte[] myImageBytes = Files.readAllBytes(localScreenshot.toPath());

            byte[] imageA = myTeamSide.equals("A") ? myImageBytes : opponentImageBytes;
            byte[] imageB = myTeamSide.equals("B") ? myImageBytes : opponentImageBytes;

            // 2. Send BOTH to Gemini (Removed battle.getGameTitle() here)
            DualMatchResultModel aiResult = GeminiVisionController.analyzeDualMatchImages(imageA, imageB);

            // 3. Transactionally save result and handle fraud/status changes natively
            BattleDao.saveDualVerificationResult(battle.getBattleId(), roundIndex, aiResult);

        } catch (Exception e) {
            e.printStackTrace();
            DualMatchResultModel errorFallback = new DualMatchResultModel();
            // Fallback object to trigger UNCLEAR state in the DAO
            BattleDao.saveDualVerificationResult(battle.getBattleId(), roundIndex, errorFallback);
        }
    }

    /**
     * Helper to grab the opponent's image from Firebase Storage URL.
     */
    public static byte[] downloadImageToBytes(String imageUrl) throws Exception {
        URL url = new URL(imageUrl);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (InputStream inputStream = url.openStream()) {
            byte[] chunk = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(chunk)) > 0) {
                output.write(chunk, 0, bytesRead);
            }
        }
        return output.toByteArray();
    }

    public static void applyEvaluationLogic(BattleModel battle, int roundIndex) {
        if (battle.getRounds() == null || battle.getRounds().isEmpty() || roundIndex >= battle.getRounds().size()) {
            return;
        }

        RoundModel round = battle.getRounds().get(roundIndex);

        long currentTime = System.currentTimeMillis();
        long startTime = battle.getLockedAt() > 0 ? battle.getLockedAt() : battle.getCreatedAt();
        long deadline = startTime + (45 * 60 * 1000);

        boolean hasClaimA = round.getTeamAClaimedOutcome() != null;
        boolean hasClaimB = round.getTeamBClaimedOutcome() != null;
        boolean timeExpired = currentTime > deadline;

        // SCENARIO 3: Time expired, NO ONE submitted. Auto-Cancel & 80% Refund.
        if (timeExpired && !hasClaimA && !hasClaimB) {
            round.setRoundStatus(BattleFirebaseKeys.STATUS_CANCELLED);
            battle.setStatus(BattleFirebaseKeys.STATUS_CANCELLED);
        }
        // SCENARIO 4: Time expired, ONLY ONE submitted. Route straight to Admin!
        else if (timeExpired && (hasClaimA ^ hasClaimB)
                && !battle.getStatus().equals(BattleFirebaseKeys.STATUS_DISPUTED)) {
            round.setRoundStatus(BattleFirebaseKeys.STATUS_DISPUTED);
            battle.setStatus(BattleFirebaseKeys.STATUS_DISPUTED);
            battle.setDisputeReason("TIMEOUT_ONE_SIDED"); // Gap 4 Tagging
        }
    }

    private static void checkOverallBattleCompletion(BattleModel battle) {
        // Only count rounds that have been fully completed (mutually accepted or admin
        // forced)
        long aWins = battle.getRounds().stream().filter(
                r -> "A".equals(r.getWinningTeam()) && BattleFirebaseKeys.STATUS_COMPLETED.equals(r.getRoundStatus()))
                .count();
        long bWins = battle.getRounds().stream().filter(
                r -> "B".equals(r.getWinningTeam()) && BattleFirebaseKeys.STATUS_COMPLETED.equals(r.getRoundStatus()))
                .count();

        int winsNeeded = battle.getFormat().equals("BO3") ? 2 : 1;

        // Check for disputes FIRST (Fixing your flagged BO3 Dispute Override Bug)
        if (battle.getRounds().stream().anyMatch(r -> BattleFirebaseKeys.STATUS_DISPUTED.equals(r.getRoundStatus()))) {
            battle.setStatus(BattleFirebaseKeys.STATUS_DISPUTED);
            BattleDao.saveBattle(battle); // Freeze for admin
            return;
        }

        // Evaluate total wins
        if (aWins >= winsNeeded) {
            BattleDao.resolveBattleFinancials(battle.getBattleId(), BattleFirebaseKeys.STATUS_COMPLETED, "A", 0.0);
        } else if (bWins >= winsNeeded) {
            BattleDao.resolveBattleFinancials(battle.getBattleId(), BattleFirebaseKeys.STATUS_COMPLETED, "B", 0.0);
        } else if (battle.getStatus().equals(BattleFirebaseKeys.STATUS_CANCELLED)) {
            BattleDao.resolveBattleFinancials(battle.getBattleId(), BattleFirebaseKeys.STATUS_CANCELLED, null, 0.8);
        } else {
            BattleDao.saveBattle(battle); // Save mid-match state (e.g., Round 1 finished, waiting for Round 2)
        }
    }

    public static void handlePlayerAccept(String battleId, int roundIndex, String teamSide) {
        new Thread(() -> {
            boolean success = BattleDao.acceptRoundTransaction(battleId, roundIndex, teamSide);
            if (success) {
                // Fetch fresh state to check if the opponent ALSO accepted
                BattleModel freshBattle = BattleDao.getBattleById(battleId);
                RoundModel round = freshBattle.getRounds().get(roundIndex);

                if (round.isAcceptedByA() && round.isAcceptedByB()) {
                    round.setRoundStatus(BattleFirebaseKeys.STATUS_COMPLETED);
                    checkOverallBattleCompletion(freshBattle);
                }
            }
        }).start();
    }
}