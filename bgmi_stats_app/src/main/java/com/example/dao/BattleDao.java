package com.example.dao;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.example.initialization.FirebaseManager;
import com.example.keys.BattleFirebaseKeys;
import com.example.keys.UserFirebaseKeys;
import com.example.model.UserModel;
import com.example.model.player.BattleModel;
import com.example.model.player.DualMatchResultModel;
import com.example.model.player.RoundModel;
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
            Firestore db = FirebaseManager.getDb();
            ApiFuture<WriteResult> future = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES)
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
            Firestore db = FirebaseManager.getDb();
            ApiFuture<QuerySnapshot> future = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES)
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
            Firestore db = FirebaseManager.getDb();
            ApiFuture<QuerySnapshot> future = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES)
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
            Firestore db = FirebaseManager.getDb();
            ApiFuture<WriteResult> future = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES)
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
            Firestore db = FirebaseManager.getDb();
            DocumentSnapshot doc = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES)
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
            Firestore db = FirebaseManager.getDb();
            ApiFuture<QuerySnapshot> future = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES)
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
     * ONCE.
     */
    public static boolean resolveBattleFinancials(String battleId, String newStatus, String winningTeam,
            double refundMultiplier) {
        Firestore db = FirebaseManager.getDb();
        DocumentReference battleRef = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES).document(battleId);

        try {
            return db.runTransaction(txn -> {
                BattleModel battle = txn.get(battleRef).get().toObject(BattleModel.class);

                // If already resolved, abort (Prevents double payouts)
                if (battle == null || battle.getStatus().equals(newStatus))
                    return false;

                battle.setStatus(newStatus);
                if (winningTeam != null)
                    battle.setOverallWinner(winningTeam);
                txn.set(battleRef, battle);

                // Process Financials
                if (battle.getEntryFeeCoins() > 0) {
                    if (refundMultiplier > 0.0) {
                        // REFUND SCENARIO: Multiplier handles 80% (0.8) or 100% (1.0)
                        double refundAmount = battle.getEntryFeeCoins() * refundMultiplier;
                        for (String pId : battle.getParticipantIds()) {
                            DocumentReference uRef = db.collection(UserFirebaseKeys.USERS_COLLECTION).document(pId);
                            txn.update(uRef, UserFirebaseKeys.FIELD_COIN_BALANCE, FieldValue.increment(refundAmount));
                        }
                    } else if (winningTeam != null) {
                        // WINNER PAYOUT SCENARIO
                        double totalPool = battle.getEntryFeeCoins() * battle.getMaxParticipants();
                        List<String> winners = new ArrayList<>();
                        for (Map.Entry<String, String> e : battle.getParticipants().entrySet()) {
                            if (e.getValue().equals(winningTeam))
                                winners.add(e.getKey());
                        }

                        if (!winners.isEmpty()) {
                            double splitAmount = totalPool / winners.size();
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
        Firestore db = FirebaseManager.getDb();
        DocumentReference battleRef = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES).document(battleId);

        try {
            return db.runTransaction(txn -> {
                BattleModel battle = txn.get(battleRef).get().toObject(BattleModel.class);
                if (battle == null)
                    return false;

                RoundModel round = battle.getRounds().get(roundIndex);
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
        Firestore db = FirebaseManager.getDb();
        DocumentReference battleRef = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES)
                .document(battleId);

        try {
            return db.runTransaction(txn -> {
                BattleModel battle = txn.get(battleRef).get().toObject(BattleModel.class);
                if (battle == null)
                    return false;

                RoundModel round = battle.getRounds().get(roundIndex);
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

    public static boolean saveDualVerificationResult(String battleId, int roundIndex, DualMatchResultModel aiResult) {
        Firestore db = FirebaseManager.getDb();
        DocumentReference battleRef = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES).document(battleId);

        try {
            return db.runTransaction(txn -> {
                BattleModel battle = txn.get(battleRef).get().toObject(BattleModel.class);
                // Null-safe checks against the new model structure
                if (battle == null || aiResult == null || aiResult.imageOne == null || aiResult.imageTwo == null)
                    return false;

                RoundModel round = battle.getRounds().get(roundIndex);
                DualMatchResultModel.ImageExtraction i1 = aiResult.imageOne; // ALWAYS Team A's Upload
                DualMatchResultModel.ImageExtraction i2 = aiResult.imageTwo; // ALWAYS Team B's Upload

                String disputeReason = null;
                String winningTeam = null;

                // 1. CONFIDENCE FLOOR (Image Quality)
                if (!"CLEAR".equals(i1.imageQuality) || !"CLEAR".equals(i2.imageQuality)) {
                    disputeReason = "LOW_QUALITY_IMAGE";
                }
                // 2. GARBAGE DATA
                else if ("NOT_A_RESULT_SCREEN".equals(i1.bannerText) || "NOT_A_RESULT_SCREEN".equals(i2.bannerText)) {
                    disputeReason = "GARBAGE_DATA_SUBMITTED";
                }
                // 3. PERSPECTIVE CONSISTENCY (Outcomes must be opposite, preventing lazy
                // duplicates)
                else if (i1.bannerText.equals(i2.bannerText)) {
                    disputeReason = "PERSPECTIVE_MISMATCH";
                }
                // 4. SCORE CONSISTENCY (The Spatial Crossover Check)
                else {
                    // Because players always see themselves on the left, Image 1's Left Score MUST
                    // equal Image 2's Right Score.
                    boolean crossedMatch = (i1.leftSideScore == i2.rightSideScore
                            && i1.rightSideScore == i2.leftSideScore);
                    if (!crossedMatch) {
                        disputeReason = "DATA_MISMATCH_SCORES";
                    }
                }

                // 5. REPLAY FRAUD (Timestamp Check)
                if (disputeReason == null && i1.onScreenTimestamp != null && !i1.onScreenTimestamp.isEmpty()) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter
                                .ofPattern("yyyy-MM-dd HH:mm");
                        LocalDateTime screenshotTime = LocalDateTime.parse(i1.onScreenTimestamp,
                                formatter);
                        long screenshotMillis = screenshotTime.atZone(ZoneId.systemDefault()).toInstant()
                                .toEpochMilli();

                        long battleStartTime = battle.getLockedAt() > 0 ? battle.getLockedAt() : battle.getCreatedAt();
                        if (screenshotMillis < (battleStartTime - 300000)) { // 5 minute tolerance
                            disputeReason = "REPLAY_FRAUD_DETECTED";
                        }
                    } catch (Exception ignored) {
                        // Skip if format is weird, let admin review
                    }
                }

                // 6. STOLEN VALOR (Fuzzy Identity Match using spatial properties)
                if (disputeReason == null) {
                    boolean foundA = false;
                    boolean foundB = false;

                    for (Map.Entry<String, String> entry : battle.getParticipants().entrySet()) {
                        String userId = entry.getKey();
                        String team = entry.getValue();

                        UserModel u = txn
                                .get(db.collection(UserFirebaseKeys.USERS_COLLECTION).document(userId)).get()
                                .toObject(UserModel.class);
                        if (u != null && u.getIgn() != null) {
                            String realIgn = u.getIgn().toLowerCase().replaceAll("[^a-z0-9]", "");

                            // Clean the AI extracted text for fuzzy matching
                            String i1Left = i1.leftSideMvpIgn != null
                                    ? i1.leftSideMvpIgn.toLowerCase().replaceAll("[^a-z0-9]", "")
                                    : "";
                            String i1Right = i1.rightSideMvpIgn != null
                                    ? i1.rightSideMvpIgn.toLowerCase().replaceAll("[^a-z0-9]", "")
                                    : "";

                            if (team.equals("A") && (i1Left.contains(realIgn) || i1Right.contains(realIgn)))
                                foundA = true;
                            if (team.equals("B") && (i1Left.contains(realIgn) || i1Right.contains(realIgn)))
                                foundB = true;
                        }
                    }
                    if (!foundA || !foundB)
                        disputeReason = "FRAUD_DETECTED_IGN";
                }

                // --- ROUTING DETERMINATION ---
                if (disputeReason != null) {
                    round.setRoundStatus(BattleFirebaseKeys.STATUS_DISPUTED);
                    battle.setStatus(BattleFirebaseKeys.STATUS_DISPUTED);
                    battle.setDisputeReason(disputeReason);
                } else {
                    // All checks passed!
                    // Because Image 1 is ALWAYS uploaded by Team A, if Image 1's banner is VICTORY,
                    // Team A won.
                    winningTeam = "VICTORY".equals(i1.bannerText) ? "A" : "B";

                    round.setWinningTeam(winningTeam);
                    round.setRoundStatus("RESULT_PENDING");
                    battle.setStatus("RESULT_PENDING");

                    // Save permanent stats mapped perfectly to the spatial properties
                    round.setTeamAScore(i1.leftSideScore);
                    round.setTeamAMvpIgn(i1.leftSideMvpIgn);
                    round.setTeamAKills(i1.leftSideFinishes);
                    round.setTeamAFdRatio(i1.leftSideFdRatio);

                    round.setTeamBScore(i1.rightSideScore);
                    round.setTeamBMvpIgn(i1.rightSideMvpIgn);
                    round.setTeamBKills(i1.rightSideFinishes);
                    round.setTeamBFdRatio(i1.rightSideFdRatio);
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
        Firestore db = FirebaseManager.getDb();
        DocumentReference ref = db
                .collection(BattleFirebaseKeys.COLLECTION_BATTLES).document(battleId);
        try {
            return db.runTransaction(txn -> {
                BattleModel b = txn.get(ref).get()
                        .toObject(BattleModel.class);
                if (b != null) {
                    b.setStatus(BattleFirebaseKeys.STATUS_DISPUTED);
                    b.getRounds().get(roundIndex).setRoundStatus(BattleFirebaseKeys.STATUS_DISPUTED);
                    txn.set(ref, b);
                }
                return true;
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean acceptRoundTransaction(String battleId, int roundIndex, String teamSide) {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference battleRef = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES).document(battleId);

        try {
            return db.runTransaction(txn -> {
                BattleModel battle = txn.get(battleRef).get().toObject(BattleModel.class);
                if (battle == null || battle.getStatus().equals(BattleFirebaseKeys.STATUS_DISPUTED))
                    return false; // Ignore accepts if already disputed

                RoundModel round = battle.getRounds().get(roundIndex);
                if (teamSide.equals("A")) {
                    round.setAcceptedByA(true);
                } else {
                    round.setAcceptedByB(true);
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
     * Allows a player to switch sides safely, preventing team overflow.
     */
    public static boolean switchTeamTransaction(String battleId, String userId, String newTeam) {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference ref = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES).document(battleId);

        try {
            return db.runTransaction(txn -> {
                BattleModel battle = txn.get(ref).get().toObject(BattleModel.class);

                // Only allow switching if the battle is still OPEN
                if (battle == null || !battle.getStatus().equals(BattleFirebaseKeys.STATUS_OPEN)) {
                    return false;
                }

                String currentTeam = battle.getParticipants().get(userId);
                if (currentTeam == null || currentTeam.equals(newTeam))
                    return false; // Not in lobby, or already on that team

                // Check if the requested team is full
                long countInNewTeam = battle.getParticipants().values().stream().filter(t -> t.equals(newTeam)).count();
                if (countInNewTeam >= battle.getMaxParticipants() / 2) {
                    return false; // The new team is full, cannot switch
                }

                // Switch successful
                battle.getParticipants().put(userId, newTeam);
                txn.set(ref, battle);
                return true;
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean leaveBattleTransaction(String battleId, String userId) {
        Firestore db = FirebaseManager.getDb();
        DocumentReference battleRef = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES).document(battleId);

        try {
            return db.runTransaction(txn -> {
                BattleModel battle = txn.get(battleRef).get().toObject(BattleModel.class);

                if (battle == null || (!battle.getStatus().equals(BattleFirebaseKeys.STATUS_OPEN)
                        && !battle.getStatus().equals(BattleFirebaseKeys.STATUS_LOCKED))) {
                    return false;
                }

                if (!battle.getParticipants().containsKey(userId))
                    return false; // User isn't in this battle

                // SCENARIO A: The Host is leaving
                if (battle.getHostUserId().equals(userId)) {
                    // Cancel the battle completely
                    battle.setStatus(BattleFirebaseKeys.STATUS_CANCELLED);

                    // Host gets 0 refund. OTHER players get 100% refund.
                    for (String participantId : battle.getParticipantIds()) {
                        if (!participantId.equals(userId)) { // Skip the host
                            DocumentReference pRef = db.collection(UserFirebaseKeys.USERS_COLLECTION)
                                    .document(participantId);
                            txn.update(pRef, UserFirebaseKeys.FIELD_COIN_BALANCE,
                                    FieldValue.increment(battle.getEntryFeeCoins()));
                        }
                    }
                    txn.set(battleRef, battle);
                    return true;
                }

                // SCENARIO B: A normal player is leaving
                // 1. Remove them from the lobby
                battle.getParticipants().remove(userId);
                battle.getParticipantIds().remove(userId);

                // 2. Refund their specific entry fee
                DocumentReference userRef = db.collection(UserFirebaseKeys.USERS_COLLECTION).document(userId);
                txn.update(userRef, UserFirebaseKeys.FIELD_COIN_BALANCE,
                        FieldValue.increment(battle.getEntryFeeCoins()));

                // 3. If the lobby was LOCKED, downgrade it back to OPEN because a slot is now
                // free
                if (battle.getStatus().equals(BattleFirebaseKeys.STATUS_LOCKED)) {
                    battle.setStatus(BattleFirebaseKeys.STATUS_OPEN);
                    battle.setLockedAt(0); // Reset lock timer
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
     * Used by Admins to forcefully resolve a dispute, process payouts, and
     * permanently save the match stats.
     */
    public static boolean resolveDisputeWithStats(String battleId, int roundIndex, String winningTeam,
            double refundMultiplier, RoundModel finalStats) {
        Firestore db = FirebaseManager.getDb();
        DocumentReference battleRef = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES).document(battleId);

        try {
            return db.runTransaction(txn -> {
                BattleModel battle = txn.get(battleRef).get().toObject(BattleModel.class);
                if (battle == null || !battle.getStatus().equals(BattleFirebaseKeys.STATUS_DISPUTED))
                    return false;

                // 1. Update the Round Stats
                RoundModel round = battle.getRounds().get(roundIndex);
                round.setWinningTeam(winningTeam);
                round.setRoundStatus(refundMultiplier > 0.0 ? BattleFirebaseKeys.STATUS_CANCELLED
                        : BattleFirebaseKeys.STATUS_COMPLETED);

                // Overwrite with Admin-confirmed stats
                round.setTeamAScore(finalStats.getTeamAScore());
                round.setTeamAMvpIgn(finalStats.getTeamAMvpIgn());
                round.setTeamAKills(finalStats.getTeamAKills());
                round.setTeamAFdRatio(finalStats.getTeamAFdRatio());

                round.setTeamBScore(finalStats.getTeamBScore());
                round.setTeamBMvpIgn(finalStats.getTeamBMvpIgn());
                round.setTeamBKills(finalStats.getTeamBKills());
                round.setTeamBFdRatio(finalStats.getTeamBFdRatio());

                // 2. Update Battle Status
                String newStatus = refundMultiplier > 0.0 ? BattleFirebaseKeys.STATUS_CANCELLED
                        : BattleFirebaseKeys.STATUS_COMPLETED;
                battle.setStatus(newStatus);
                if (winningTeam != null)
                    battle.setOverallWinner(winningTeam);

                txn.set(battleRef, battle);

                // 3. Process Financials (Identical to your secure resolveBattleFinancials
                // logic)
                if (battle.getEntryFeeCoins() > 0) {
                    if (refundMultiplier > 0.0) {
                        double refundAmount = battle.getEntryFeeCoins() * refundMultiplier;
                        for (String pId : battle.getParticipantIds()) {
                            DocumentReference uRef = db.collection(UserFirebaseKeys.USERS_COLLECTION).document(pId);
                            txn.update(uRef, UserFirebaseKeys.FIELD_COIN_BALANCE,
                                    FieldValue.increment(refundAmount));
                        }
                    } else if (winningTeam != null) {
                        double totalPool = battle.getEntryFeeCoins() * battle.getMaxParticipants();
                        long winnerCount = battle.getParticipants().values().stream().filter(t -> t.equals(winningTeam))
                                .count();
                        if (winnerCount > 0) {
                            double splitAmount = totalPool / winnerCount;
                            for (Map.Entry<String, String> e : battle.getParticipants().entrySet()) {
                                if (e.getValue().equals(winningTeam)) {
                                    DocumentReference uRef = db.collection(UserFirebaseKeys.USERS_COLLECTION)
                                            .document(e.getKey());
                                    txn.update(uRef, UserFirebaseKeys.FIELD_COIN_BALANCE,
                                            FieldValue.increment(splitAmount));
                                }
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
}