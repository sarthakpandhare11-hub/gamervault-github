package com.example.controller.player;

import java.util.List;
import java.util.UUID;

import com.example.controller.AuthController;
import com.example.controller.admin.NotificationController;
import com.example.dao.RecruitmentDao;
import com.example.model.player.RecruitmentModel;

public class RecruitmentController {

    public static boolean publishRecruitment(
            String teamName, String role, String lineUpType, String region,
            String primaryWeapon, double minFd, int minDmg, String expTier,
            String desc, String detailedReqs, String logoURL) {

        String currentUserId = "GUEST_PLAYER";
        if (AuthController.currentUser != null && AuthController.currentUser.getUserId() != null) {
            currentUserId = AuthController.currentUser.getUserId();
        }

        String uniqueId = UUID.randomUUID().toString();
        long currentTime = System.currentTimeMillis();

        RecruitmentModel model = new RecruitmentModel();
        model.setRecruitmentId(uniqueId);
        model.setAuthorId(currentUserId);
        model.setTeamName(teamName);
        model.setTeamLogoURL(logoURL != null && !logoURL.isEmpty() ? logoURL : "");

        String upperTeam = teamName.toUpperCase();
        model.setVerifiedTeam(
                upperTeam.contains("S8UL") || upperTeam.contains("GODLIKE") || upperTeam.contains("GLOBAL"));

        model.setRole(role.toUpperCase());
        model.setLineUpType(lineUpType.toUpperCase());
        model.setRegion(region.toUpperCase());
        model.setPrimaryWeapon(primaryWeapon.toUpperCase());
        model.setStatus("OPEN");

        model.setMinFdRatio(minFd);
        model.setMinAvgDamage(minDmg);
        model.setExperienceTier(expTier);

        model.setDescription(desc);
        model.setDetailedRequirements(detailedReqs);

        model.setCreatedAt(currentTime);
        model.setUpdatedAt(currentTime);

        boolean isSaved = RecruitmentDao.saveRecruitmentRecord(model);
        if (isSaved) {
            // Alert players that a team is looking for a specific role
            NotificationController.sendNotification(
                    "Roster Search: " + teamName,
                    teamName + " is officially recruiting a " + role + ". Check the Recruitment Hub!",
                    "SYSTEM",
                    "GLOBAL");
        }
        return isSaved;
    }

    public static List<RecruitmentModel> fetchActiveMarketPostings() {
        return RecruitmentDao.fetchRecruitmentsChronologically();
    }
}