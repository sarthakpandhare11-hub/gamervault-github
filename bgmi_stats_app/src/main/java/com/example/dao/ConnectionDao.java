package com.example.dao;

import com.example.initialization.FirebaseManager;
import com.example.model.player.ConnectionModel;
import com.google.cloud.firestore.Firestore;

public class ConnectionDao {
    public static boolean sendConnectionRequest(ConnectionModel conn) {
        try {
            Firestore db = FirebaseManager.getDb();
            db.collection("Connections").document(conn.getConnectionId()).set(conn).get();
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send connection request: " + e.getMessage());
            return false;
        }
    }
}