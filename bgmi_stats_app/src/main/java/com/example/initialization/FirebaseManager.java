package com.example.initialization;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

public class FirebaseManager {
    private static Firestore dbInstance;

    // Returns the single centralized instance of Firestore
    public static Firestore getDb() {
        if (dbInstance == null) {
            dbInstance = FirestoreClient.getFirestore();
        }
        return dbInstance;
    }
}