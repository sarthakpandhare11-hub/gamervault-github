package com.example.dao;

import java.util.ArrayList;
import java.util.List;

import com.example.initialization.FirebaseManager;
import com.example.keys.RecruitmentFirebaseKeys;
import com.example.model.player.RecruitmentModel;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

public class RecruitmentDao {

    public static boolean saveRecruitmentRecord(RecruitmentModel post) {
        try {
            Firestore db = FirebaseManager.getDb();
            db.collection(RecruitmentFirebaseKeys.RECRUITMENTS_COLLECTION)
                    .document(post.getRecruitmentId())
                    .set(post)
                    .get();
            return true;
        } catch (Exception e) {
            System.err.println("Firestore Recruitment Save Failure: " + e.getMessage());
            return false;
        }
    }

    public static List<RecruitmentModel> fetchRecruitmentsChronologically() {
        List<RecruitmentModel> recordList = new ArrayList<>();
        try {
            Firestore db = FirebaseManager.getDb();
            QuerySnapshot snapshot = db.collection(RecruitmentFirebaseKeys.RECRUITMENTS_COLLECTION)
                    .orderBy(RecruitmentFirebaseKeys.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                    .get()
                    .get();

            for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
                RecruitmentModel item = document.toObject(RecruitmentModel.class);
                if (item != null) {
                    recordList.add(item);
                }
            }
        } catch (Exception e) {
            System.err.println("Firestore Recruitment Fetch Failure: " + e.getMessage());
        }
        return recordList;
    }
}