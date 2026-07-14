package com.example.controller.player;

import com.example.controller.AuthController;
import com.example.initialization.FirebaseManager;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DirectMessageController {

    // Tracks the current room for the UI to load
    public static String activeChatRoomId = null;

    public static String initializeConnection(String targetUserId) {
        if (AuthController.currentUser == null)
            return null;

        String currentUserId = AuthController.currentUser.getUserId();

        // Generate a consistent ID based on the two user IDs
        String[] ids = { currentUserId, targetUserId };
        Arrays.sort(ids);
        String roomId = "DM_" + ids[0] + "_" + ids[1];

        try {
            Firestore db = FirebaseManager.getDb();
            Map<String, Object> roomData = new HashMap<>();
            roomData.put("participants", Arrays.asList(currentUserId, targetUserId));
            roomData.put("lastActive", System.currentTimeMillis());
            roomData.put("roomType", "RECRUITMENT_DM");

            // Merge prevents overwriting existing chat history if they connected before
            db.collection("DirectMessages").document(roomId).set(roomData, SetOptions.merge());
            return roomId;
        } catch (Exception e) {
            System.err.println("Failed to initialize DM Room: " + e.getMessage());
            return null;
        }
    }
}