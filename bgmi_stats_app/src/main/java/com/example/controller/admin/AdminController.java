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
}