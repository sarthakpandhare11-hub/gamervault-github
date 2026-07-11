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

    /**
     * Downloads the opponent's image and feeds both into the Gemini Referee.
     */
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

            // 2. Send BOTH to Gemini
            DualMatchResultModel aiResult = GeminiVisionController
                    .analyzeDualMatchImages(imageA, imageB, battle.getGameTitle());

            // 3. Fraud Detection
            String finalDecision;
            if (!aiResult.isSameMatch) {
                // Someone uploaded a fake/unrelated screenshot! Send straight to Admin Dispute
                // Queue
                finalDecision = "DISPUTED";
            } else {
                finalDecision = aiResult.winnerSide;
            }

            // 4. Transactionally save result and change status
            BattleDao.saveDualVerificationResult(battle.getBattleId(), roundIndex, finalDecision);

        } catch (Exception e) {
            e.printStackTrace();
            BattleDao.saveDualVerificationResult(battle.getBattleId(), roundIndex, "DISPUTED");
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

        com.example.model.player.RoundModel round = battle.getRounds().get(roundIndex);

        long currentTime = System.currentTimeMillis();
        long startTime = battle.getLockedAt() > 0 ? battle.getLockedAt() : battle.getCreatedAt();
        long deadline = startTime + (45 * 60 * 1000);

        boolean hasClaimA = round.getTeamAClaimedOutcome() != null;
        boolean hasClaimB = round.getTeamBClaimedOutcome() != null;
        boolean timeExpired = currentTime > deadline;

        // SCENARIO 3: Time expired, NO ONE submitted. Cancel & 80% Refund
        if (timeExpired && !hasClaimA && !hasClaimB) {
            round.setRoundStatus(BattleFirebaseKeys.STATUS_CANCELLED);
            battle.setStatus(BattleFirebaseKeys.STATUS_CANCELLED);
        }
        // SCENARIO 4: Time expired, ONLY ONE submitted. Trigger AI Forfeit Check
        else if (timeExpired && (hasClaimA ^ hasClaimB) && !battle.getStatus().equals("PENDING_FORFEIT")) {
            round.setRoundStatus("PENDING_FORFEIT");
            battle.setStatus("PENDING_FORFEIT");
        }
    }

    public static void evaluateRound(BattleModel battle, int roundIndex) {
        applyEvaluationLogic(battle, roundIndex);

        // Handle the new Forfeit Validation State
        if (battle.getStatus().equals("PENDING_FORFEIT")) {
            com.example.dao.BattleDao.updateBattleStatus(battle.getBattleId(), "PENDING_FORFEIT");
            boolean hasClaimA = battle.getRounds().get(roundIndex).getTeamAClaimedOutcome() != null;

            // Spin up a thread to validate the single image
            new Thread(() -> validateAndExecuteForfeit(battle, roundIndex, hasClaimA)).start();
            return; // Stop checkOverallBattleCompletion until the thread finishes
        }

        checkOverallBattleCompletion(battle);
    }

    /**
     * Downloads the single uploaded image, asks Gemini if it's real, and pays out
     * or penalizes.
     */
    private static void validateAndExecuteForfeit(BattleModel battle, int roundIndex, boolean isTeamA) {
        try {
            com.example.model.player.RoundModel round = battle.getRounds().get(roundIndex);
            String imageUrl = isTeamA ? round.getTeamAScreenshotUrl() : round.getTeamBScreenshotUrl();

            // 1. Download Image
            byte[] imageBytes = downloadImageToBytes(imageUrl);

            // 2. Validate with Gemini
            String aiResult = com.example.controller.gemini.GeminiVisionController.validateSingleMatchImage(imageBytes,
                    battle.getGameTitle());

            // 3. Resolve
            if ("A".equals(aiResult)) {
                // Legitimate win! Award the forfeit victory.
                String winningTeam = isTeamA ? "A" : "B";
                com.example.dao.BattleDao.resolveBattleFinancials(battle.getBattleId(),
                        BattleFirebaseKeys.STATUS_COMPLETED, winningTeam, false);
            } else {
                // Fraud caught! They uploaded a fake image hoping for a free forfeit win.
                // Cancel the match and refund both parties 80% (or void completely).
                com.example.dao.BattleDao.resolveBattleFinancials(battle.getBattleId(),
                        BattleFirebaseKeys.STATUS_CANCELLED, null, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            com.example.dao.BattleDao.resolveBattleFinancials(battle.getBattleId(), BattleFirebaseKeys.STATUS_CANCELLED,
                    null, true);
        }
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
        } else if (battle.getStatus().equals(BattleFirebaseKeys.STATUS_CANCELLED)) {
            // Send to Refund Engine (Winner: null, Refund: true)
            BattleDao.resolveBattleFinancials(battle.getBattleId(), BattleFirebaseKeys.STATUS_CANCELLED, null, true);
        } else {
            BattleDao.saveBattle(battle); // Standard mid-match save
        }
    }
}