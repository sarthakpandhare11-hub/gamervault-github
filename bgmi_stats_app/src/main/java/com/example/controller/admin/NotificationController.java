package com.example.controller.admin;

import com.example.initialization.FirebaseManager;
import com.example.model.NotificationModel;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotificationController {

    private static final String COLLECTION = "notifications";

    private static Firestore db = FirebaseManager.getDb();

    public static void sendNotification(String title, String message, String type, String targetUserId) {
        try {
            CollectionReference ref = db.collection(COLLECTION);

            NotificationModel notif = new NotificationModel();
            notif.setId(UUID.randomUUID().toString());
            notif.setTitle(title);
            notif.setMessage(message);
            notif.setType(type); // "MATCH", "TOURNAMENT", "SYSTEM", "CONTENT"
            notif.setTargetUserId(targetUserId);
            notif.setTimestamp(System.currentTimeMillis());

            ref.document(notif.getId()).set(notif);
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }

    public static List<NotificationModel> getPlayerNotifications(String userId) {
        List<NotificationModel> list = new ArrayList<>();
        try {
            CollectionReference ref = db.collection(COLLECTION);

            ApiFuture<QuerySnapshot> globalQuery = ref.whereEqualTo("targetUserId", "GLOBAL")
                    .orderBy("timestamp", Query.Direction.DESCENDING).limit(15).get();
            ApiFuture<QuerySnapshot> personalQuery = ref.whereEqualTo("targetUserId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING).limit(10).get();

            for (DocumentSnapshot doc : globalQuery.get().getDocuments()) {
                list.add(doc.toObject(NotificationModel.class));
            }
            for (DocumentSnapshot doc : personalQuery.get().getDocuments()) {
                list.add(doc.toObject(NotificationModel.class));
            }

            list.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
            return list.size() > 20 ? list.subList(0, 20) : list;

        } catch (Exception e) {
            return list;
        }
    }

    public static List<NotificationModel> getGlobalAdminActivity() {
        List<NotificationModel> list = new ArrayList<>();
        try {
            CollectionReference ref = db.collection(COLLECTION);
            ApiFuture<QuerySnapshot> query = ref.whereEqualTo("targetUserId", "GLOBAL")
                    .orderBy("timestamp", Query.Direction.DESCENDING).limit(15).get();
            for (DocumentSnapshot doc : query.get().getDocuments()) {
                list.add(doc.toObject(NotificationModel.class));
            }
        } catch (Exception e) {
        }
        return list;
    }
}