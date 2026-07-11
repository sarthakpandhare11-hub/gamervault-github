package com.example.controller;

import com.example.initialization.FirebaseManager;
import com.example.keys.UserFirebaseKeys;
import com.example.model.UserModel;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

public class UserController {

    // create user in database
    public static boolean createProfile(UserModel user) {
        try {
            Firestore db = FirebaseManager.getDb();

            db.collection(UserFirebaseKeys.USERS_COLLECTION)
                    .document(user.getUserId())
                    .set(user)
                    .get();

            return true;
        } catch (Exception e) {
            System.out.println("Exception in UserController.createProfile : " + e.getMessage());
            return false;
        }
    }

    public static UserModel getUserProfile(String userId) {
        try {
            Firestore db = FirebaseManager.getDb();

            // document snapshot from Firestore
            DocumentSnapshot document = db.collection(UserFirebaseKeys.USERS_COLLECTION)
                    .document(userId).get().get();

            if (document.exists()) {
                return document.toObject(UserModel.class);
            } else {
                System.out.println("No such document found in Firestore!");
                return null;
            }
        } catch (Exception e) {
            System.out.println("Exception in UserController.getUserProfile : " + e.getMessage());
            return null;
        }
    }

}