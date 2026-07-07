package com.example.dao;

import java.util.ArrayList;
import java.util.List;

import com.example.initialization.FirebaseManager;
import com.example.keys.MatchesDetailedFirebaseKeys;
import com.example.keys.MatchesFirebaseKeys;
import com.example.model.player.MatchExtractionResultModel;
import com.example.model.player.MatchModel;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;

public class MatchesDao {

    /**
     * Saves a fully populated MatchModel to the Firestore database.
     * 
     * This is a method used when a user want to save a match detail uploaded by
     * him.
     * It is a batch thatis data adding to 2 collection.
     * Same match data, 1 with simple values and other with many values is added
     * into it.
     */
    public static boolean saveMatchRecord(MatchModel match, MatchExtractionResultModel detailedMatch) {
        try {
            // 1. Get the initialized Firestore database instance
            Firestore db = FirebaseManager.getDb();
            WriteBatch batch = db.batch();

            DocumentReference matchesRef = db.collection(MatchesFirebaseKeys.MATCHES_COLLECTION)
                    .document(match.getMatchId());

            // 3. Reference for the heavy Details using MatchDetailsFirebaseKeys
            DocumentReference detailsRef = db.collection(MatchesDetailedFirebaseKeys.MATCH_DETAILS_COLLECTION)
                    .document(detailedMatch.getMatchId());

            batch.set(matchesRef, match);
            batch.set(detailsRef, detailedMatch);

            batch.commit().get();

            return true;

        } catch (Exception e) {
            System.err.println("Database Write Error in MatchesDao: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static List<MatchModel> fetchUserMatches(String userId) {

        Firestore db = FirebaseManager.getDb();
        try {
            QuerySnapshot querySnapshot = db.collection(MatchesFirebaseKeys.MATCHES_COLLECTION)
                    .whereEqualTo(MatchesFirebaseKeys.FIELD_USER_ID, userId)
                    .get().get();

            List<MatchModel> matches = new ArrayList<>();

            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                matches.add(document.toObject(MatchModel.class));
            }

            matches.sort((m1, m2) -> Long.compare(m2.getCreatedAt(), m1.getCreatedAt()));

            return matches;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static MatchExtractionResultModel getDetailedMatchStats(String matchId) throws Exception {
        Firestore db = FirebaseManager.getDb();

        DocumentSnapshot document = db.collection(MatchesDetailedFirebaseKeys.MATCH_DETAILS_COLLECTION)
                .document(matchId)
                .get()
                .get();

        if (document.exists()) {
            return document.toObject(MatchExtractionResultModel.class);
        }

        return null;
    }

    public static boolean deleteMatch(String userId, String matchId) {
        try {
            Firestore db = FirebaseManager.getDb();
            db.collection(MatchesFirebaseKeys.MATCHES_COLLECTION)
                    .document(matchId)
                    .delete()
                    .get();
            db.collection(MatchesDetailedFirebaseKeys.MATCH_DETAILS_COLLECTION)
                    .document(matchId)
                    .delete()
                    .get();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}