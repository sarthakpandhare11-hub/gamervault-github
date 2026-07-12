package com.example.dao;

import com.example.initialization.FirebaseManager;
import com.example.keys.UserFirebaseKeys;
import com.example.model.UserModel;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

public class ProfileDao {

    public static UserModel fetchUserProfile(String userId) {

        try {
            Firestore db = FirebaseManager.getDb();

            DocumentSnapshot document = db
                    .collection(UserFirebaseKeys.USERS_COLLECTION)
                    .document(userId)
                    .get()
                    .get();
            if (document.exists()) {
                return document.toObject(UserModel.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean updateUserProfile(String userId, UserModel updatedUser) {
        try {
            Firestore db = FirebaseManager.getDb();
            db.collection(UserFirebaseKeys.USERS_COLLECTION).document(userId).set(updatedUser).get();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static UserModel getUserProfileSync(String userId) {
        try {
            Firestore db = FirebaseManager.getDb();
            DocumentSnapshot doc = db.collection(UserFirebaseKeys.USERS_COLLECTION)
                    .document(userId).get().get();
            return doc.exists() ? doc.toObject(UserModel.class) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
