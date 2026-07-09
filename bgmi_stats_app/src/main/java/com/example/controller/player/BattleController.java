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
    public static String hostBattle(String gameTitle, String mode, String format, double entryFee, String chosenTeam) {
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

    // 2. JOIN AN EXISTING BATTLE
    public static boolean joinBattle(String battleId) { // NOTE: Pass ID, not the Object
        UserModel currentUser = AuthController.currentUser;
        if (currentUser == null)
            return false;

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference ref = db.collection(BattleFirebaseKeys.COLLECTION_BATTLES)
                .document(battleId);

        try {
            return db.runTransaction(txn -> {
                BattleModel battle = txn.get(ref).get().toObject(BattleModel.class);
                if (battle == null)
                    return false;
                if (battle.getParticipants().containsKey(currentUser.getUserId()))
                    return false; // Already in
                if (battle.getParticipants().size() >= battle.getMaxParticipants())
                    return false; // Full

                // 1. Verify and deduct user balance ATOMICALLY
                DocumentReference userRef = db
                        .collection(UserFirebaseKeys.USERS_COLLECTION)
                        .document(currentUser.getUserId());
                Double currentBalance = txn.get(userRef).get()
                        .getDouble(UserFirebaseKeys.FIELD_COIN_BALANCE);
                if (currentBalance == null)
                    currentBalance = 0.0;

                if (currentBalance < battle.getEntryFeeCoins())
                    return false; // Insufficient funds! abort join.

                // Deduct fee
                txn.update(userRef, UserFirebaseKeys.FIELD_COIN_BALANCE,
                        FieldValue.increment(-battle.getEntryFeeCoins()));

                // 2. Assign Team
                int currentSize = battle.getParticipants().size();
                String team = (currentSize < battle.getMaxParticipants() / 2) ? "A" : "B";
                battle.getParticipants().put(currentUser.getUserId(), team);
                battle.getParticipantIds().add(currentUser.getUserId());

                // 3. Lock Room if Full
                if (battle.getParticipants().size() == battle.getMaxParticipants()) {
                    battle.setStatus(BattleFirebaseKeys.STATUS_LOCKED);
                    battle.setLockedAt(System.currentTimeMillis());
                    battle.setRoomId(String.valueOf(1000000 + new Random().nextInt(9000000)));
                    battle.setRoomPassword(String.valueOf(1000 + new Random().nextInt(9000)));
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