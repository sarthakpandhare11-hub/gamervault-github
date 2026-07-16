package com.example.dao;

import com.example.initialization.FirebaseManager;
import com.example.keys.UserFirebaseKeys;
import com.example.model.player.ConnectionModel;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ConnectionDao {

    private static final String CONNECTIONS_COLLECTION = "Connections";

    public static boolean sendConnectionRequest(ConnectionModel conn) {
        try {
            Firestore db = FirebaseManager.getDb();
            db.collection(CONNECTIONS_COLLECTION).document(conn.getConnectionId()).set(conn).get();
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send connection request: " + e.getMessage());
            return false;
        }
    }

    /**
     * The piece that was missing entirely: accepting a request must both flip
     * the connection's status AND add each user's ID to the OTHER user's
     * connectionIds array - this second part is what ProfileController's
     * privacy gate actually reads. Done as a single transaction so a failure
     * partway through can never leave the connection "ACCEPTED" on one side
     * only, or leave the status flipped without the arrays actually updated.
     */
    public static boolean acceptConnectionRequest(String connectionId, String requesterId, String targetId) {
        Firestore db = FirebaseManager.getDb();
        try {
            db.runTransaction(txn -> {
                DocumentReference connRef = db.collection(CONNECTIONS_COLLECTION).document(connectionId);
                DocumentReference requesterRef = db.collection(UserFirebaseKeys.USERS_COLLECTION).document(requesterId);
                DocumentReference targetRef = db.collection(UserFirebaseKeys.USERS_COLLECTION).document(targetId);

                txn.update(connRef, "status", "ACCEPTED");
                txn.update(requesterRef, UserFirebaseKeys.FIELD_CONNECTION_IDS, FieldValue.arrayUnion(targetId));
                txn.update(targetRef, UserFirebaseKeys.FIELD_CONNECTION_IDS, FieldValue.arrayUnion(requesterId));
                return null;
            }).get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Failed to accept connection request: " + e.getMessage());
            return false;
        }
    }

    public static boolean declineConnectionRequest(String connectionId) {
        try {
            FirebaseManager.getDb().collection(CONNECTIONS_COLLECTION).document(connectionId)
                    .update("status", "DECLINED").get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Failed to decline connection request: " + e.getMessage());
            return false;
        }
    }

    /**
     * Incoming requests waiting on this user's decision - the query that backs
     * the Connection Requests inbox. Without this, a sent request had no way
     * to ever surface to the person it was sent to.
     */
    public static List<ConnectionModel> getPendingRequestsFor(String userId) {
        List<ConnectionModel> requests = new ArrayList<>();
        try {
            QuerySnapshot snapshot = FirebaseManager.getDb().collection(CONNECTIONS_COLLECTION)
                    .whereEqualTo("targetId", userId)
                    .whereEqualTo("status", "PENDING")
                    .get().get();
            for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
                requests.add(doc.toObject(ConnectionModel.class));
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Failed to fetch pending connection requests: " + e.getMessage());
        }
        return requests;
    }

    /**
     * Lets the UI show "Request Sent" / "Already Connected" instead of a
     * button that looks freshly clickable every time the profile reloads.
     */
    public static String getConnectionStatus(String userIdA, String userIdB) {
        try {
            String[] ids = { userIdA, userIdB };
            java.util.Arrays.sort(ids);
            String connectionId = "CONN_" + ids[0] + "_" + ids[1];
            com.google.cloud.firestore.DocumentSnapshot doc = FirebaseManager.getDb()
                    .collection(CONNECTIONS_COLLECTION).document(connectionId).get().get();
            if (!doc.exists())
                return "NONE";
            ConnectionModel conn = doc.toObject(ConnectionModel.class);
            return conn != null && conn.getStatus() != null ? conn.getStatus() : "NONE";
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Failed to check connection status: " + e.getMessage());
            return "NONE";
        }
    }
}