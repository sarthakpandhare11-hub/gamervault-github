package com.example.dao;

import com.example.keys.ApplicationFirebaseKeys;
import com.example.model.player.ApplicationModel;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ApplicationDao {

    private static Firestore getDb() {
        return FirestoreClient.getFirestore();
    }

    public static boolean saveApplication(ApplicationModel app) {
        if (app.getApplicationId() == null || app.getApplicationId().isEmpty()) {
            app.setApplicationId(UUID.randomUUID().toString());
        }
        try {
            ApiFuture<WriteResult> future = getDb().collection(ApplicationFirebaseKeys.COLLECTION_APPLICATIONS)
                    .document(app.getApplicationId())
                    .set(app);
            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean hasUserApplied(String applicantId, String recruitmentId) {
        try {
            String deterministicId = recruitmentId + "_" + applicantId;
            return getDb().collection(ApplicationFirebaseKeys.COLLECTION_APPLICATIONS)
                    .document(deterministicId).get().get().exists();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<ApplicationModel> getPendingApplications(String recruiterId) {
        List<ApplicationModel> apps = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> future = getDb().collection(ApplicationFirebaseKeys.COLLECTION_APPLICATIONS)
                    .whereEqualTo(ApplicationFirebaseKeys.FIELD_RECRUITER_ID, recruiterId)
                    .whereEqualTo(ApplicationFirebaseKeys.FIELD_STATUS, ApplicationFirebaseKeys.STATUS_PENDING)
                    .orderBy(ApplicationFirebaseKeys.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                    .get();

            for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                apps.add(doc.toObject(ApplicationModel.class));
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return apps;
    }

    public static boolean updateStatus(String applicationId, String newStatus) {
        try {
            ApiFuture<WriteResult> future = getDb().collection(ApplicationFirebaseKeys.COLLECTION_APPLICATIONS)
                    .document(applicationId)
                    .update(ApplicationFirebaseKeys.FIELD_STATUS, newStatus);
            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- NEW: Atomic Transaction for Acceptance & Chat Initialization ---
    public static boolean acceptAndCreateDMTransaction(String applicationId, String recruiterId, String applicantId) {
        try {
            Firestore db = getDb();

            // Deterministic Room ID ensuring they always share the same thread
            String[] ids = { recruiterId, applicantId };
            Arrays.sort(ids);
            String roomId = "DM_" + ids[0] + "_" + ids[1];

            ApiFuture<Void> transaction = db.runTransaction(t -> {
                DocumentReference appRef = db.collection(ApplicationFirebaseKeys.COLLECTION_APPLICATIONS)
                        .document(applicationId);
                DocumentReference dmRef = db.collection("DirectMessages").document(roomId);

                // 1. Update Application Status
                t.update(appRef, ApplicationFirebaseKeys.FIELD_STATUS, ApplicationFirebaseKeys.STATUS_ACCEPTED);

                // 2. Initialize Direct Message Room
                Map<String, Object> roomData = new HashMap<>();
                roomData.put("participants", Arrays.asList(recruiterId, applicantId));
                roomData.put("lastActive", System.currentTimeMillis());
                roomData.put("roomType", "RECRUITMENT_DM");

                // Merge prevents wiping out existing chat history if they previously connected
                t.set(dmRef, roomData, SetOptions.merge());
                return null;
            });

            transaction.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Transaction Failed: " + e.getMessage());
            return false;
        }
    }
}