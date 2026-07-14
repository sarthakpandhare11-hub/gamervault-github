package com.example.controller.admin;

import com.example.initialization.FirebaseManager;
import com.example.model.NotificationModel;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.ListenerRegistration;
import com.google.cloud.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class NotificationController {

    private static final String COLLECTION = "notifications";
    private static Firestore db = FirebaseManager.getDb();

    // 1. Fixed Send Method: Now properly writes to Firestore
    public static void sendNotification(String title, String message, String type, String targetUserId,
            String actionRoute) {
        try {
            CollectionReference ref = db.collection(COLLECTION);
            NotificationModel notif = new NotificationModel();

            notif.setNotificationId(UUID.randomUUID().toString());
            notif.setTitle(title);
            notif.setMessage(message);
            notif.setType(type);
            notif.setTargetUserId(targetUserId);
            notif.setActionRoute(actionRoute);
            notif.setTimestamp(System.currentTimeMillis());
            notif.setRead(false);

            // Execute the write
            ref.document(notif.getNotificationId()).set(notif);
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }

    public static void sendNotification(String title, String message, String type, String targetUserId) {
        sendNotification(title, message, type, targetUserId, null);
    }

    // 2. The Live Listener: Pushes updates instantly to the UI
    public static ListenerRegistration listenToUserNotifications(String userId,
            Consumer<List<NotificationModel>> onUpdate) {
        return db.collection(COLLECTION)
                // Fetch both personal and system-wide notifications in one sweep
                .whereIn("targetUserId", Arrays.asList(userId, "GLOBAL"))
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        System.err.println("Notification listen failed: " + e);
                        return;
                    }

                    List<NotificationModel> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        list.add(doc.toObject(NotificationModel.class));
                    }

                    // Sort descending by timestamp locally (avoids requiring users to build complex
                    // Firestore Indexes)
                    list.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                    // Send to the UI thread
                    onUpdate.accept(list);
                });
    }

    // 3. Mark as Read (Batch operation for performance)
    public static void markAllAsRead(List<String> unreadIds) {
        if (unreadIds == null || unreadIds.isEmpty())
            return;

        try {
            WriteBatch batch = db.batch();
            for (String id : unreadIds) {
                batch.update(db.collection(COLLECTION).document(id), "isRead", true);
            }
            batch.commit();
        } catch (Exception e) {
            System.err.println("Failed to mark read: " + e.getMessage());
        }
    }
}