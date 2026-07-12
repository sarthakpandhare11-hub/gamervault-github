package com.example.controller.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.example.controller.AuthController;
import com.example.dao.BattleDao;
import com.example.keys.BattleFirebaseKeys;
import com.example.keys.UserFirebaseKeys;
import com.example.model.UserModel;
import com.example.model.player.BattleModel;
import com.example.model.player.RoundModel;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

public class BattleController {

    // 1. HOST A NEW BATTLE
    // Notice the new 'gameTitle' parameter
    public static String hostBattle(String gameTitle, String mode, String format, double entryFee, String chosenTeam,
            long scheduledTime) {
        UserModel currentUser = AuthController.currentUser;
        if (currentUser == null)
            return null;

        // 1. Check Wallet Balance
        if (currentUser.getCoinBalance() < entryFee) {
            System.err.println("Insufficient funds to host.");
            return null;
        }

        BattleModel battle = new BattleModel();

        // ADD MULTI-GAME TITLE
        battle.setGameTitle(gameTitle);
        battle.setScheduledTime(scheduledTime);

        battle.setHostType("PLAYER");
        battle.setHostUserId(currentUser.getUserId());
        battle.setMode(mode);
        battle.setFormat(format);
        battle.setStatus(BattleFirebaseKeys.STATUS_OPEN);
        battle.setEntryFeeCoins(entryFee);

        int max = 2;
        if (mode.equals("2V2"))
            max = 4;
        if (mode.equals("4V4"))
            max = 8;
        battle.setMaxParticipants(max);

        // Assign Host to their chosen team
        HashMap<String, String> players = new HashMap<>();
        players.put(currentUser.getUserId(), chosenTeam.toUpperCase());
        battle.setParticipants(players);
        battle.setParticipantIds(new ArrayList<>(Arrays.asList(currentUser.getUserId())));
        battle.setRounds(new ArrayList<>());

        int totalRounds = format.equals("BO3") ? 3 : 1;
        for (int i = 1; i <= totalRounds; i++) {
            RoundModel r = new RoundModel();
            r.setRoundNumber(i);
            r.setRoundStatus("PENDING");
            battle.getRounds().add(r);
        }

        battle.setCreatedAt(System.currentTimeMillis());

        // ATOMIC BATCH WRITE: Create Battle AND Deduct Fee simultaneously
        Firestore db = FirestoreClient.getFirestore();
        com.google.cloud.firestore.WriteBatch batch = db.batch();

        String newBattleId = java.util.UUID.randomUUID().toString();
        battle.setBattleId(newBattleId);

        // Queue Battle Creation
        DocumentReference battleRef = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES).document(newBattleId);
        batch.set(battleRef, battle);

        // Queue Wallet Deduction
        if (entryFee > 0) {
            DocumentReference userRef = db.collection(UserFirebaseKeys.USERS_COLLECTION)
                    .document(currentUser.getUserId());
            batch.update(userRef, UserFirebaseKeys.FIELD_COIN_BALANCE, FieldValue.increment(-entryFee));
        }

        try {
            // Commit both actions safely
            batch.commit().get();
            if (entryFee > 0)
                currentUser.setCoinBalance(currentUser.getCoinBalance() - entryFee);
            return newBattleId;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to host battle atomically.");
            return null;
        }
    }

    // 2. JOIN AN EXISTING BATTLE (Updated to require chosenTeam)
    public static boolean joinBattle(String battleId, String chosenTeam) {
        UserModel currentUser = AuthController.currentUser;
        if (currentUser == null)
            return false;

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference ref = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES).document(battleId);

        try {
            return db.runTransaction(txn -> {
                BattleModel battle = txn.get(ref).get().toObject(BattleModel.class);
                if (battle == null)
                    return false;
                if (battle.getParticipants().containsKey(currentUser.getUserId()))
                    return false; // Already in
                if (battle.getParticipants().size() >= battle.getMaxParticipants())
                    return false; // Full lobby

                // NEW LOGIC: Check if the specific team they requested is full
                long currentTeamSize = battle.getParticipants().values().stream()
                        .filter(team -> team.equals(chosenTeam.toUpperCase())).count();

                if (currentTeamSize >= battle.getMaxParticipants() / 2) {
                    return false; // The chosen team is full! Transaction fails, UI should tell them to pick the
                                  // other team.
                }

                // 1. Verify and deduct user balance ATOMICALLY
                DocumentReference userRef = db.collection(UserFirebaseKeys.USERS_COLLECTION)
                        .document(currentUser.getUserId());
                Double currentBalance = txn.get(userRef).get().getDouble(UserFirebaseKeys.FIELD_COIN_BALANCE);
                if (currentBalance == null)
                    currentBalance = 0.0;

                if (currentBalance < battle.getEntryFeeCoins())
                    return false; // Insufficient funds

                txn.update(userRef, UserFirebaseKeys.FIELD_COIN_BALANCE,
                        FieldValue.increment(-battle.getEntryFeeCoins()));

                // 2. Assign Specifically Chosen Team
                battle.getParticipants().put(currentUser.getUserId(), chosenTeam.toUpperCase());
                battle.getParticipantIds().add(currentUser.getUserId());

                // 3. Lock Room if Full
                if (battle.getParticipants().size() == battle.getMaxParticipants()) {
                    battle.setStatus(BattleFirebaseKeys.STATUS_LOCKED);
                    battle.setLockedAt(System.currentTimeMillis());
                }

                txn.set(ref, battle);
                return true;
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. FETCH OPEN LOBBIES
    public static List<BattleModel> getOpenLobbies() {
        return BattleDao.getOpenBattles();
    }
}