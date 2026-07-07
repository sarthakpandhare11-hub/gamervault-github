package com.example.dao;

import java.util.ArrayList;
import java.util.List;

import com.example.initialization.FirebaseManager;
import com.example.keys.ContentFirebaseKeys;
import com.example.model.admin.ContentModel;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;

public class ContentDao {
    public static boolean saveContent(ContentModel content) {
        try {
            FirebaseManager.getDb().collection(ContentFirebaseKeys.CONTENT_COLLECTION)
                    .document(content.getContentId()).set(content).get();
            System.out.println("Content saved successfully");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Content not saved");
            return false;
        }
    }

    public static List<ContentModel> fetchAllContent() {
        List<ContentModel> list = new ArrayList<>();
        try {
            var snapshots = FirebaseManager.getDb().collection(ContentFirebaseKeys.CONTENT_COLLECTION)
                    .orderBy(ContentFirebaseKeys.FIELD_CREATED_AT, Query.Direction.DESCENDING).get().get();
            for (QueryDocumentSnapshot doc : snapshots.getDocuments()) {
                ContentModel c = doc.toObject(ContentModel.class);
                if (c != null)
                    list.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean deleteContent(String contentId) {
        try {
            FirebaseManager.getDb().collection(ContentFirebaseKeys.CONTENT_COLLECTION).document(contentId).delete()
                    .get();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}