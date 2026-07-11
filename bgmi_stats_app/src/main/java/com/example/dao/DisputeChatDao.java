package com.example.dao;

import com.example.keys.BattleFirebaseKeys;
import com.example.model.player.ChatMessageModel;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DisputeChatDao {

    /**
     * Sends a message to the sub-collection of a specific battle.
     */
    public static void sendMessage(String battleId, ChatMessageModel message) {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(BattleFirebaseKeys.COLLECTION_BATTLES)
                .document(battleId)
                .collection("DisputeChat")
                .document(message.getMessageId())
                .set(message);
    }

    /**
     * Attaches a real-time listener to the chat.
     */
    public static ListenerRegistration listenToChat(String battleId, Consumer<List<ChatMessageModel>> onUpdate) {
        Firestore db = FirestoreClient.getFirestore();
        return db.collection(BattleFirebaseKeys.COLLECTION_BATTLES)
                .document(battleId)
                .collection("DisputeChat")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        System.err.println("Chat listen failed: " + e);
                        return;
                    }
                    List<ChatMessageModel> messages = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            messages.add(doc.toObject(ChatMessageModel.class));
                        }
                    }
                    onUpdate.accept(messages);
                });
    }
}