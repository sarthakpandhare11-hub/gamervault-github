package com.example.controller.player;

import java.util.List;

import com.example.controller.AuthController;
import com.example.controller.admin.NotificationController;
import com.example.dao.ApplicationDao;
import com.example.keys.ApplicationFirebaseKeys;
import com.example.model.UserModel;
import com.example.model.player.ApplicationModel;
import com.example.model.player.RecruitmentModel;

public class ApplicationController {

    // Inside ApplicationController.java, replace the accept, decline, and apply
    // methods:

    public static boolean applyToPost(RecruitmentModel recruitment) {
        UserModel currentUser = AuthController.currentUser;
        if (currentUser == null)
            return false;

        if (ApplicationDao.hasUserApplied(currentUser.getUserId(), recruitment.getRecruitmentId())) {
            return false;
        }

        ApplicationModel app = new ApplicationModel();
        app.setApplicationId(recruitment.getRecruitmentId() + "_" + currentUser.getUserId());
        app.setRecruitmentId(recruitment.getRecruitmentId());
        app.setApplicantId(currentUser.getUserId());
        app.setApplicantName(currentUser.getPlayerName());
        app.setRecruiterId(recruitment.getAuthorId());
        app.setStatus(ApplicationFirebaseKeys.STATUS_PENDING);

        app.setSnapshotFdRatio(currentUser.getAverageKills());
        app.setSnapshotAvgDamage(currentUser.getAverageDamage());
        app.setSnapshotRoleArchetype(currentUser.getPrimaryRole());
        app.setCreatedAt(System.currentTimeMillis());

        boolean success = ApplicationDao.saveApplication(app);

        if (success) {
            NotificationController.sendNotification(
                    "New Application: " + recruitment.getRole(),
                    currentUser.getPlayerName() + " applied. Check your Scout's Inbox.",
                    "APPLICATION",
                    recruitment.getAuthorId(),
                    "scoutsInbox" // Action route to inbox
            );
        }
        return success;
    }

    public static boolean accept(ApplicationModel app) {
        // Trigger the atomic transaction
        boolean success = ApplicationDao.acceptAndCreateDMTransaction(
                app.getApplicationId(), app.getRecruiterId(), app.getApplicantId());

        if (success) {
            NotificationController.sendNotification(
                    "Application Accepted! 🤝",
                    "Your application was accepted! Check your Direct Messages.",
                    "APPLICATION",
                    app.getApplicantId(),
                    "directMessage" // Action route to chat
            );
        }
        return success;
    }

    public static boolean decline(ApplicationModel app) {
        boolean success = ApplicationDao.updateStatus(app.getApplicationId(), ApplicationFirebaseKeys.STATUS_DECLINED);
        if (success) {
            NotificationController.sendNotification(
                    "Application Update",
                    "Your application was reviewed, but the team went in another direction.",
                    "APPLICATION",
                    app.getApplicantId());
        }
        return success;
    }

    public static boolean hasApplied(String recruitmentId) {
        if (AuthController.currentUser == null)
            return false;
        return com.example.dao.ApplicationDao.hasUserApplied(AuthController.currentUser.getUserId(), recruitmentId);
    }

    public static List<ApplicationModel> fetchMyInbox() {
        if (AuthController.currentUser == null)
            return new java.util.ArrayList<>();
        return com.example.dao.ApplicationDao.getPendingApplications(AuthController.currentUser.getUserId());
    }
}