package com.example.controller.player;

import java.util.ArrayList;
import java.util.List;

import com.example.initialization.FirebaseManager;
import com.example.keys.UserFirebaseKeys;
import com.example.model.UserModel;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

public class LeaderboardController {

    public static List<UserModel> getLeaderboardUsers() {
        List<UserModel> usersList = new ArrayList<>();
        try {
            Firestore db = FirebaseManager.getDb();

            // Fetching all user documents from the users collection, add user to list to
            // show all users in leaderboard.
            QuerySnapshot querySnapshot = db.collection(UserFirebaseKeys.USERS_COLLECTION).get().get();

            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                UserModel user = document.toObject(UserModel.class);
                if (user != null) {
                    usersList.add(user);
                }
            }
            return usersList;
        } catch (Exception e) {
            System.out.println("Exception in UserController.getLeaderboardUsers : " + e.getMessage());
            return usersList;
        }
    }

    public static void syncLeaderboardStats(String userId, int totalMatches, int totalKills, double totalDamage,
            double winRate, double avgPlacement) {
        new Thread(() -> {
            try {
                Firestore db = FirebaseManager.getDb();

                db.collection(UserFirebaseKeys.USERS_COLLECTION).document(userId)
                        .update(
                                "totalMatches", totalMatches,
                                "totalKills", totalKills,
                                "totalDamage", totalDamage,
                                "winRate", winRate,
                                "averagePlacement", avgPlacement)
                        .get();

            } catch (Exception e) {
                System.err.println("Failed to sync stats to Leaderboard: " + e.getMessage());
            }
        }).start();
    }
}
