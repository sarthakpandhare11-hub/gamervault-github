package com.example.dao;

import com.example.keys.BattleFirebaseKeys;
import com.example.model.player.BattleModel;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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
}