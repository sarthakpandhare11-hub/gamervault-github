package com.example.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.example.keys.BattleFirebaseKeys;
import com.example.keys.UserFirebaseKeys;
import com.example.model.player.BattleModel;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;

public class BattleDao {

    private static Firestore getDb() {
        return FirestoreClient.getFirestore();
    }

    public static boolean saveBattle(BattleModel battle) {
        if (battle.getBattleId() == null || battle.getBattleId().isEmpty()) {
            battle.setBattleId(UUID.randomUUID().toString());
        }
        try {
            ApiFuture<WriteResult> future = getDb().collection(BattleFirebaseKeys.COLLECTION_BATTLES)
                    .document(battle.getBattleId())
                    .set(battle);
            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Fetches all battles waiting for players
    public static List<BattleModel> getOpenBattles() {
        List<BattleModel> openBattles = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> future = getDb().collection(BattleFirebaseKeys.COLLECTION_BATTLES)
                    .whereEqualTo(BattleFirebaseKeys.FIELD_STATUS, BattleFirebaseKeys.STATUS_OPEN)
                    .orderBy(BattleFirebaseKeys.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                    .get();

            for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                openBattles.add(doc.toObject(BattleModel.class));
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return openBattles;
    }

    // Fetches all battles that are not yet finished
    public static List<BattleModel> getAllActiveBattles() {
        List<BattleModel> activeBattles = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> future = getDb().collection(BattleFirebaseKeys.COLLECTION_BATTLES)
                    .whereNotEqualTo(BattleFirebaseKeys.FIELD_STATUS, BattleFirebaseKeys.STATUS_COMPLETED)
                    .orderBy(BattleFirebaseKeys.FIELD_STATUS) // Required by Firestore when using NotEqualTo
                    .orderBy(BattleFirebaseKeys.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                    .get();

            for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                activeBattles.add(doc.toObject(BattleModel.class));
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return activeBattles;
    }

    // Direct update for simple status changes (e.g., OPEN to LOCKED)
    public static boolean updateBattleStatus(String battleId, String newStatus) {
        try {
            ApiFuture<WriteResult> future = getDb().collection(BattleFirebaseKeys.COLLECTION_BATTLES)
                    .document(battleId)
                    .update(BattleFirebaseKeys.FIELD_STATUS, newStatus);
            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static BattleModel getBattleById(String battleId) {
        try {
            DocumentSnapshot doc = getDb().collection(BattleFirebaseKeys.COLLECTION_BATTLES)
                    .document(battleId).get().get();
            return doc.exists() ? doc.toObject(BattleModel.class) : null;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<BattleModel> getBattleHistoryForUser(String userId) {
        List<BattleModel> history = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> future = getDb().collection(BattleFirebaseKeys.COLLECTION_BATTLES)
                    .whereArrayContains("participantIds", userId)
                    .whereIn(BattleFirebaseKeys.FIELD_STATUS, Arrays.asList(
                            BattleFirebaseKeys.STATUS_COMPLETED,
                            "RESOLVED",
                            "CANCELLED"))
                    .orderBy(BattleFirebaseKeys.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                    .get();

            for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                history.add(doc.toObject(BattleModel.class));
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return history;
    }

    /**
     * Safely resolves a battle. Guarantees the payout/refund only happens EXACTLY
     * ONCE,
     * even if 8 players trigger this method simultaneously.
     */
    public static boolean resolveBattleFinancials(String battleId, String newStatus, String winningTeam,
            boolean isRefund) {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference battleRef = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES).document(battleId);

        try {
            return db.runTransaction(txn -> {
                BattleModel battle = txn.get(battleRef).get().toObject(BattleModel.class);

                // If it's already resolved, abort! (Prevents double payouts)
                if (battle == null || battle.getStatus().equals(newStatus))
                    return false;

                // 1. Update Match Status
                battle.setStatus(newStatus);
                if (winningTeam != null)
                    battle.setOverallWinner(winningTeam);
                txn.set(battleRef, battle);

                // 2. Process Financials
                if (battle.getEntryFeeCoins() > 0) {

                    if (isRefund) {
                        // SCENARIO A: 80% Penalty Refund for cancelled matches
                        double refundAmount = battle.getEntryFeeCoins() * 0.80;
                        for (String pId : battle.getParticipantIds()) {
                            DocumentReference uRef = db.collection(UserFirebaseKeys.USERS_COLLECTION).document(pId);
                            txn.update(uRef, UserFirebaseKeys.FIELD_COIN_BALANCE, FieldValue.increment(refundAmount));
                        }
                    } else if (winningTeam != null) {
                        // SCENARIO B: Distribute Prize Pool to Winners
                        double totalPool = battle.getEntryFeeCoins() * battle.getMaxParticipants();

                        // Find how many players are on the winning team
                        List<String> winners = new ArrayList<>();
                        for (Map.Entry<String, String> e : battle.getParticipants().entrySet()) {
                            if (e.getValue().equals(winningTeam))
                                winners.add(e.getKey());
                        }

                        if (!winners.isEmpty()) {
                            double splitAmount = totalPool / winners.size(); // Divide pool evenly
                            for (String wId : winners) {
                                DocumentReference uRef = db.collection(UserFirebaseKeys.USERS_COLLECTION).document(wId);
                                txn.update(uRef, UserFirebaseKeys.FIELD_COIN_BALANCE,
                                        FieldValue.increment(splitAmount));
                            }
                        }
                    }
                }
                return true;
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Atomically saves round evidence to prevent concurrent overwrite races.
     */
    public static boolean submitRoundEvidenceTransaction(String battleId, int roundIndex, String teamSide,
            String claimedOutcome, String actualWinningTeam, String uploadedUrl) {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference battleRef = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES).document(battleId);

        try {
            return db.runTransaction(txn -> {
                BattleModel battle = txn.get(battleRef).get().toObject(BattleModel.class);
                if (battle == null)
                    return false;

                com.example.model.player.RoundModel round = battle.getRounds().get(roundIndex);
                if (teamSide.equals("A")) {
                    round.setTeamAClaimedOutcome(claimedOutcome);
                    round.setTeamAOcrResult(actualWinningTeam);
                    round.setTeamAScreenshotUrl(uploadedUrl);
                } else {
                    round.setTeamBClaimedOutcome(claimedOutcome);
                    round.setTeamBOcrResult(actualWinningTeam);
                    round.setTeamBScreenshotUrl(uploadedUrl);
                }

                txn.set(battleRef, battle);
                return true;
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Fetches battles that require manual admin intervention
    public static List<BattleModel> getDisputedBattles() {
        List<BattleModel> disputes = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> future = getDb().collection(BattleFirebaseKeys.COLLECTION_BATTLES)
                    .whereEqualTo(BattleFirebaseKeys.FIELD_STATUS, BattleFirebaseKeys.STATUS_DISPUTED)
                    .get();

            for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                disputes.add(doc.toObject(BattleModel.class));
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return disputes;
    }

    /**
     * STAGE 1: Saves a player's uploaded evidence URL and Claim to the database.
     * Does NOT run the AI evaluation yet.
     */
    public static boolean saveEvidenceUrlTransaction(String battleId, int roundIndex, String teamSide,
            String claimedOutcome, String uploadedUrl, String submitterId) {
        com.google.cloud.firestore.Firestore db = com.google.firebase.cloud.FirestoreClient.getFirestore();
        com.google.cloud.firestore.DocumentReference battleRef = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES)
                .document(battleId);

        try {
            return db.runTransaction(txn -> {
                BattleModel battle = txn.get(battleRef).get().toObject(BattleModel.class);
                if (battle == null)
                    return false;

                com.example.model.player.RoundModel round = battle.getRounds().get(roundIndex);
                if (teamSide.equals("A")) {
                    round.setTeamAClaimedOutcome(claimedOutcome);
                    round.setTeamAScreenshotUrl(uploadedUrl);
                    round.setTeamASubmittedBy(submitterId);
                } else {
                    round.setTeamBClaimedOutcome(claimedOutcome);
                    round.setTeamBScreenshotUrl(uploadedUrl);
                    round.setTeamBSubmittedBy(submitterId);
                }

                txn.set(battleRef, battle);
                return true;
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * STAGE 2: Saves the final Dual-AI decision and shifts the match to
     * RESULT_PENDING.
     */
    public static boolean saveDualVerificationResult(String battleId, int roundIndex, String aiDeclaredWinner) {
        com.google.cloud.firestore.Firestore db = com.google.firebase.cloud.FirestoreClient.getFirestore();
        com.google.cloud.firestore.DocumentReference battleRef = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES)
                .document(battleId);

        try {
            return db.runTransaction(txn -> {
                BattleModel battle = txn.get(battleRef).get().toObject(BattleModel.class);
                if (battle == null)
                    return false;

                com.example.model.player.RoundModel round = battle.getRounds().get(roundIndex);
                round.setWinningTeam(aiDeclaredWinner);

                // If it's a dispute, flag it. Otherwise, pend user confirmation.
                if (aiDeclaredWinner.equals("DISPUTED") || aiDeclaredWinner.equals("UNCLEAR")) {
                    round.setRoundStatus(BattleFirebaseKeys.STATUS_DISPUTED);
                    battle.setStatus(BattleFirebaseKeys.STATUS_DISPUTED);
                } else {
                    round.setRoundStatus("RESULT_PENDING");
                    battle.setStatus("RESULT_PENDING");
                }

                txn.set(battleRef, battle);
                return true;
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Safely escalates a match to a dispute, updating both the battle and the
     * round.
     */
    public static boolean escalateToDispute(String battleId, int roundIndex) {
        com.google.cloud.firestore.Firestore db = com.google.firebase.cloud.FirestoreClient.getFirestore();
        com.google.cloud.firestore.DocumentReference ref = db
                .collection(com.example.keys.BattleFirebaseKeys.COLLECTION_BATTLES).document(battleId);
        try {
            return db.runTransaction(txn -> {
                com.example.model.player.BattleModel b = txn.get(ref).get()
                        .toObject(com.example.model.player.BattleModel.class);
                if (b != null) {
                    b.setStatus(com.example.keys.BattleFirebaseKeys.STATUS_DISPUTED);
                    b.getRounds().get(roundIndex).setRoundStatus(com.example.keys.BattleFirebaseKeys.STATUS_DISPUTED);
                    txn.set(ref, b);
                }
                return true;
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}