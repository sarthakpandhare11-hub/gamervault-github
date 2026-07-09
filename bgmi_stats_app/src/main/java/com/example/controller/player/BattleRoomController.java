package com.example.controller.player;

import com.example.controller.gemini.GeminiVisionController;
import com.example.keys.BattleFirebaseKeys;
import com.example.model.player.BattleModel;
import com.example.model.player.MatchExtractionResultModel;
import com.example.model.player.RoundModel;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.ListenerRegistration;
import com.google.firebase.cloud.FirestoreClient;

import java.io.File;
import java.util.Collections;
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
                // 1. Send to Gemini OCR (Reusing your existing vision pipeline)
                MatchExtractionResultModel ocrResult = GeminiVisionController
                        .sendGeminiRequestData(Collections.singletonList(screenshot));

                // 2. Validate Result
                boolean success = false;
                if (ocrResult != null && ocrResult.isExtractionSuccessful()) {
                    // Update the RoundModel locally
                    RoundModel round = battle.getRounds().get(roundNumber - 1);
                    if (teamSide.equals("A")) {
                        round.setTeamAClaimedOutcome(claimedOutcome);
                        round.setTeamAOcrResult(ocrResult.getMatchResult());
                        round.setTeamAScreenshotUrl("UPLOADED_URL"); // In reality, upload to Firebase Storage first
                    } else {
                        round.setTeamBClaimedOutcome(claimedOutcome);
                        round.setTeamBOcrResult(ocrResult.getMatchResult());
                        round.setTeamBScreenshotUrl("UPLOADED_URL");
                    }

                    // 3. Push update to Firestore (BattleDao)
                    // BattleDao.saveBattle(battle); // Uncomment when BattleDao is ready
                    success = true;
                }

                // 4. Return callback
                onComplete.accept(success);

            } catch (Exception e) {
                e.printStackTrace();
                onComplete.accept(false);
            }
        }).start();
    }
}