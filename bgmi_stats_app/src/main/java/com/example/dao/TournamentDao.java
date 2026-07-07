package com.example.dao;

import java.util.ArrayList;
import java.util.List;

import com.example.initialization.FirebaseManager;
import com.example.keys.TournamentFirebaseKeys;
import com.example.model.admin.TournamentModel;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;

public class TournamentDao {

    public static boolean saveTournament(TournamentModel tournament) {
        try {
            Firestore db = FirebaseManager.getDb();
            db.collection(TournamentFirebaseKeys.TOURNAMENTS_COLLECTION)
                    .document(tournament.getTournamentId())
                    .set(tournament)
                    .get();
            return true;
        } catch (Exception e) {
            System.err.println("Error creating tournament: " + e.getMessage());
            return false;
        }
    }

    public static List<TournamentModel> fetchAllTournaments() {
        List<TournamentModel> list = new ArrayList<>();
        try {
            Firestore db = FirebaseManager.getDb();
            var snapshots = db.collection(TournamentFirebaseKeys.TOURNAMENTS_COLLECTION).get().get();
            for (QueryDocumentSnapshot doc : snapshots.getDocuments()) {
                TournamentModel t = doc.toObject(TournamentModel.class);
                if (t != null)
                    list.add(t);
            }
        } catch (Exception e) {
            System.err.println("Error gathering tournaments: " + e.getMessage());
        }
        return list;
    }

    public static boolean deleteTournament(String tournamentId) {
        try {
            FirebaseManager.getDb()
                    .collection(TournamentFirebaseKeys.TOURNAMENTS_COLLECTION)
                    .document(tournamentId).delete().get();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}