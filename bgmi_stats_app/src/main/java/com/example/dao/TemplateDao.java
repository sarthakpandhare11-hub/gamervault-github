package com.example.dao;

import com.example.initialization.FirebaseManager;
import com.example.keys.TemplateFirebaseKeys;
import com.example.model.admin.TemplateModel;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class TemplateDao {

    public static boolean saveTemplate(TemplateModel template) {
        try {
            Firestore db = FirebaseManager.getDb();
            db.collection(TemplateFirebaseKeys.COLLECTION).document(template.getTemplateId()).set(template).get();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<TemplateModel> fetchAllTemplates() {
        List<TemplateModel> templates = new ArrayList<>();
        try {
            Firestore db = FirebaseManager.getDb();
            List<QueryDocumentSnapshot> docs = db.collection(TemplateFirebaseKeys.COLLECTION)
                    .orderBy(TemplateFirebaseKeys.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                    .get().get().getDocuments();

            for (QueryDocumentSnapshot doc : docs) {
                templates.add(doc.toObject(TemplateModel.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return templates;
    }

    public static boolean deleteTemplate(String templateId) {
        try {
            Firestore db = FirebaseManager.getDb();
            db.collection(TemplateFirebaseKeys.COLLECTION).document(templateId).delete().get();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}