package com.example.controller.admin;

import java.util.ArrayList;
import java.util.List;

import com.example.initialization.FirebaseManager;
import com.example.keys.UserFirebaseKeys;
import com.example.model.UserModel;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

public class AdminController {

    // Fetches every registered user from the database
    public static List<UserModel> fetchAllUsers() {
        List<UserModel> usersList = new ArrayList<>();
        try {
            Firestore db = FirebaseManager.getDb();
            QuerySnapshot snapshot = db.collection(UserFirebaseKeys.USERS_COLLECTION).get().get();

            for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
                UserModel user = doc.toObject(UserModel.class);
                if (user != null) {
                    usersList.add(user);
                }
            }
        } catch (Exception e) {
            System.err.println("Admin Fetch Error: " + e.getMessage());
        }
        return usersList;
    }

    /**
     * Toggles a user's suspension status.
     */
    public static boolean suspendUser(String userId, boolean suspendStatus) {
        try {
            Firestore db = FirebaseManager.getDb();
            db.collection(UserFirebaseKeys.USERS_COLLECTION)
                    .document(userId)
                    .update("suspended", suspendStatus)
                    .get(); // .get() forces the thread to wait for confirmation
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Permanently deletes a user profile from the database.
     * Note: In a production app, you might just permanently suspend them rather
     * than deleting
     * to preserve their Match History references, but this fulfills the delete
     * function.
     */
    public static boolean deleteUser(String userId) {
        try {
            Firestore db = FirebaseManager.getDb();
            db.collection(UserFirebaseKeys.USERS_COLLECTION)
                    .document(userId)
                    .delete()
                    .get();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}