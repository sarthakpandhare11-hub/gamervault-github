package com.example.dao;

import com.example.initialization.FirebaseManager;
import com.example.model.player.ChatMessageModel;
import com.google.cloud.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DirectMessageDao {

    public static void sendMessage(String roomId, ChatMessageModel msg) {
        try {
            Firestore db = FirebaseManager.getDb();
            // Store in the "messages" sub-collection inside the DM room
            db.collection("DirectMessages").document(roomId)
                    .collection("messages").document(msg.getMessageId())
                    .set(msg);

            Map<String, Object> roomData = new java.util.HashMap<>();
            roomData.put("lastActive", msg.getTimestamp());

            // Update lastActive timestamp on the room itself for sorting
            db.collection("DirectMessages").document(roomId)
                    .set(roomData, SetOptions.merge());

        } catch (Exception e) {
            System.err.println("Failed to send DM: " + e.getMessage());
        }
    }

    // Attach a real-time listener to the specific chat room
    public static ListenerRegistration listenForMessages(String roomId,
            Consumer<List<ChatMessageModel>> onMessagesUpdate) {
        try {
            Firestore db = FirebaseManager.getDb();
            return db.collection("DirectMessages").document(roomId).collection("messages")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            System.err.println("Listen failed: " + e.getMessage());
                            return;
                        }
                        List<ChatMessageModel> messages = new ArrayList<>();
                        if (snapshots != null) {
                            for (DocumentSnapshot doc : snapshots) {
                                ChatMessageModel m = doc.toObject(ChatMessageModel.class);
                                if (m != null)
                                    messages.add(m);
                            }
                        }
                        onMessagesUpdate.accept(messages);
                    });
        } catch (Exception e) {
            System.err.println("Failed to attach DM listener: " + e.getMessage());
            return null;
        }
    }
}