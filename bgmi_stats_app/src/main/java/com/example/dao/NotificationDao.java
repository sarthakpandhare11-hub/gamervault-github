package com.example.dao;

import java.util.function.Consumer;

import com.example.initialization.FirebaseManager;
import com.example.model.NotificationModel;
import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.ListenerRegistration;

public class NotificationDao {

    public static ListenerRegistration listenForNewNotifications(String userId,
            Consumer<NotificationModel> onNewNotification) {
        Firestore db = FirebaseManager.getDb();
        return db.collection("Users").document(userId).collection("Notifications")
                .whereEqualTo("isRead", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null)
                        return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            NotificationModel notif = dc.getDocument().toObject(NotificationModel.class);
                            onNewNotification.accept(notif);
                        }
                    }
                });
    }

    public static void markAsRead(String userId, String notificationId) {
        Firestore db = FirebaseManager.getDb();
        db.collection("Users").document(userId).collection("Notifications")
                .document(notificationId).update("isRead", true);
    }
}
