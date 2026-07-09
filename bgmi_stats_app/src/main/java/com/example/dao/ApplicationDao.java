// package com.example.dao;

// import com.example.keys.ApplicationFirebaseKeys;
// import com.example.model.player.ApplicationModel;
// import com.google.api.core.ApiFuture;
// import com.google.cloud.firestore.Firestore;
// import com.google.cloud.firestore.QueryDocumentSnapshot;
// import com.google.cloud.firestore.QuerySnapshot;
// import com.google.cloud.firestore.WriteResult;
// import com.google.firebase.cloud.FirestoreClient;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.UUID;
// import java.util.concurrent.ExecutionException;

// public class ApplicationDao {

//     private static Firestore getDb() {
//         return FirestoreClient.getFirestore();
//     }

//     public static boolean saveApplication(ApplicationModel app) {
//         if (app.getApplicationId() == null || app.getApplicationId().isEmpty()) {
//             app.setApplicationId(UUID.randomUUID().toString());
//         }
//         try {
//             ApiFuture<WriteResult> future = getDb().collection(ApplicationFirebaseKeys.COLLECTION_APPLICATIONS)
//                     .document(app.getApplicationId())
//                     .set(app);
//             future.get();
//             return true;
//         } catch (InterruptedException | ExecutionException e) {
//             e.printStackTrace();
//             return false;
//         }
//     }

//     public static boolean hasUserApplied(String applicantId, String recruitmentId) {
//         try {
//             ApiFuture<QuerySnapshot> future = getDb().collection(ApplicationFirebaseKeys.COLLECTION_APPLICATIONS)
//                     .whereEqualTo(ApplicationFirebaseKeys.FIELD_APPLICANT_ID, applicantId)
//                     .whereEqualTo(ApplicationFirebaseKeys.FIELD_RECRUITMENT_ID, recruitmentId)
//                     .get();
//             return !future.get().isEmpty();
//         } catch (InterruptedException | ExecutionException e) {
//             e.printStackTrace();
//             return false;
//         }
//     }

//     public static List<ApplicationModel> getPendingApplications(String recruiterId) {
//         List<ApplicationModel> apps = new ArrayList<>();
//         try {
//             ApiFuture<QuerySnapshot> future = getDb().collection(ApplicationFirebaseKeys.COLLECTION_APPLICATIONS)
//                     .whereEqualTo(ApplicationFirebaseKeys.FIELD_RECRUITER_ID, recruiterId)
//                     .whereEqualTo(ApplicationFirebaseKeys.FIELD_STATUS, ApplicationFirebaseKeys.STATUS_PENDING)
//                     .get();

//             for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
//                 apps.add(doc.toObject(ApplicationModel.class));
//             }
//         } catch (InterruptedException | ExecutionException e) {
//             e.printStackTrace();
//         }
//         return apps;
//     }

//     public static boolean updateStatus(String applicationId, String newStatus) {
//         try {
//             ApiFuture<WriteResult> future = getDb().collection(ApplicationFirebaseKeys.COLLECTION_APPLICATIONS)
//                     .document(applicationId)
//                     .update(ApplicationFirebaseKeys.FIELD_STATUS, newStatus);
//             future.get();
//             return true;
//         } catch (InterruptedException | ExecutionException e) {
//             e.printStackTrace();
//             return false;
//         }
//     }
// }

package com.example.dao;

import com.example.keys.ApplicationFirebaseKeys;
import com.example.model.player.ApplicationModel;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ApplicationDao {

    private static Firestore getDb() {
        return FirestoreClient.getFirestore();
    }

    public static boolean saveApplication(ApplicationModel app) {
        // If ID is missing, fallback to UUID (though Controller now sets a
        // deterministic one)
        if (app.getApplicationId() == null || app.getApplicationId().isEmpty()) {
            app.setApplicationId(UUID.randomUUID().toString());
        }
        try {
            // Using .set() with a deterministic ID makes this naturally idempotent
            // (overwrites instead of duplicating)
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
            // FIX #5: O(1) Document lookup instead of a query. Avoids composite index need
            // here.
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
            // FIX #7: Added .orderBy Descending so oldest apps don't get buried
            // FIX #6 NOTE: You MUST create a Composite Index in Firebase Console for:
            // Collection: Applications | Fields: recruiterId (Ascending), status
            // (Ascending), createdAt (Descending)
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
}