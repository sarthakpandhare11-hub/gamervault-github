package com.example.dao;

import com.example.initialization.FirebaseManager;
import com.example.keys.GamerMomentFirebaseKeys;
import com.example.model.player.GamerMomentModel;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GamerMomentDao {

    public static boolean savePost(GamerMomentModel post) {
        try {
            Firestore db = FirebaseManager.getDb();
            db.collection(GamerMomentFirebaseKeys.COLLECTION_GAMER_MOMENTS)
                    .document(post.getPostId())
                    .set(post).get(); // blocking call safely wrapped in thread by Controller
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<GamerMomentModel> fetchGlobalTimeline() {
        List<GamerMomentModel> feed = new ArrayList<>();
        try {
            Firestore db = FirebaseManager.getDb();
            // Fetch top 50 most recent posts
            var snapshot = db.collection(GamerMomentFirebaseKeys.COLLECTION_GAMER_MOMENTS)
                    .orderBy(GamerMomentFirebaseKeys.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                    .limit(50)
                    .get().get();

            for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
                feed.add(doc.toObject(GamerMomentModel.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return feed;
    }

    public static List<GamerMomentModel> fetchPostsByUser(String userId) {
        List<GamerMomentModel> userPosts = new ArrayList<>();
        try {
            Firestore db = FirebaseManager.getDb();
            var snapshot = db.collection(GamerMomentFirebaseKeys.COLLECTION_GAMER_MOMENTS)
                    .whereEqualTo(GamerMomentFirebaseKeys.FIELD_AUTHOR_ID, userId)
                    .orderBy(GamerMomentFirebaseKeys.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                    .get().get();

            for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
                userPosts.add(doc.toObject(GamerMomentModel.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userPosts;
    }

    public static void incrementLike(String postId) {
        try {
            Firestore db = FirebaseManager.getDb();
            db.collection(GamerMomentFirebaseKeys.COLLECTION_GAMER_MOMENTS)
                    .document(postId)
                    .update(GamerMomentFirebaseKeys.FIELD_LIKES_COUNT, FieldValue.increment(1));
        } catch (Exception e) {
            System.err.println("Failed to like post: " + e.getMessage());
        }
    }
}