package com.example.controller.player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.dao.MatchesDao;
import com.example.dao.StorageDao;
import com.example.model.player.MatchExtractionResultModel;
import com.example.model.player.MatchModel;

public class MatchController {

    /*
     * POSTING DATA TO FIREBASE
     * 
     */
    public static String handleMatchConfirmation(
            MatchExtractionResultModel aiData,
            String matchName,
            String matchType,
            List<File> localEvidenceFiles,
            String currentUserId) {

        if (aiData == null || !aiData.isExtractionSuccessful())
            return "ERROR: Cannot save unverified data.";
        if (matchName == null || matchName.trim().isEmpty())
            return "ERROR: Provide a valid Match Name.";
        if (currentUserId == null)
            currentUserId = "GUEST_USER";

        String uniqueMatchId = UUID.randomUUID().toString();

        // Uploading Images to Firebase Storage and storing their URL in list of
        // cloudUrls
        List<String> cloudUrls = new ArrayList<>();
        if (localEvidenceFiles != null && !localEvidenceFiles.isEmpty()) {
            StorageDao storageDao = new StorageDao();
            for (File file : localEvidenceFiles) {
                try {
                    String publicUrl = storageDao.uploadMatchImage(file, currentUserId, uniqueMatchId);
                    if (publicUrl != null)
                        cloudUrls.add(publicUrl);
                } catch (Exception e) {
                    System.err.println("Failed to upload evidence file: " + file.getName());
                    e.printStackTrace();
                }
            }
        }

        MatchModel finalMatch = new MatchModel();
        finalMatch.setMatchId(uniqueMatchId);
        finalMatch.setUserId(currentUserId);
        finalMatch.setMatchName(matchName);
        finalMatch.setMatchType(matchType);
        finalMatch.setImageUrls(cloudUrls);
        finalMatch.setCreatedAt(System.currentTimeMillis());

        // Basic stats needed for lists
        finalMatch.setMap(aiData.getMap());
        finalMatch.setGameMode(aiData.getGameMode());
        finalMatch.setTeamPlacement(aiData.getTeamPlacement());
        finalMatch.setKills(aiData.getKills());
        finalMatch.setDamage(aiData.getDamage());
        finalMatch.setRating(aiData.getRating());

        finalMatch.setPerspective(aiData.getPerspective());
        finalMatch.setTotalPlayers(aiData.getTotalPlayers());
        finalMatch.setMatchResult(aiData.getMatchResult());
        finalMatch.setMatchDate(aiData.getMatchDate());
        finalMatch.setAssists(aiData.getAssists());
        finalMatch.setSurvivalTime(aiData.getSurvivalTime());
        finalMatch.setRole(aiData.getRole());
        finalMatch.setMainWeapon(aiData.getMainWeapon());
        finalMatch.setMvp(aiData.isMvp());
        finalMatch.setTeamGrade(aiData.getTeamGrade());

        finalMatch.setVerifiedByGemini(true);
        finalMatch.setConfidenceScore(aiData.getConfidenceScore());
        finalMatch.setProcessingStatus("VERIFIED");

        aiData.setMatchId(uniqueMatchId);
        aiData.setUserId(currentUserId);

        // Save BOTH to Firestore using the WriteBatch in Dao
        boolean isStored = MatchesDao.saveMatchRecord(finalMatch, aiData);

        if (isStored) {
            return "SUCCESS: Match safely vaulted!";
        } else {
            return "ERROR: Database write failed.";
        }
    }

    public static List<MatchModel> getUserMatchHistory(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        List<MatchModel> fetchedUserMatches = MatchesDao.fetchUserMatches(userId);
        return fetchedUserMatches;
    }

    public static boolean deleteMatch(
            String userId,
            String matchId) {
        return MatchesDao.deleteMatch(
                userId,
                matchId);
    }

    public static MatchExtractionResultModel getDetailedMatch(String matchId) {
        try {
            return MatchesDao.getDetailedMatchStats(matchId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}