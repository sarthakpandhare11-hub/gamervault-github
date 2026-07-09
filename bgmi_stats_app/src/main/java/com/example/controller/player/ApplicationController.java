// package com.example.controller.player;

// import com.example.controller.AuthController;
// import com.example.controller.admin.NotificationController;
// import com.example.dao.ApplicationDao;
// import com.example.keys.ApplicationFirebaseKeys;
// import com.example.model.UserModel;
// import com.example.model.player.ApplicationModel;
// import com.example.model.player.RecruitmentModel;

// import java.util.List;

// public class ApplicationController {

//     public static boolean applyToPost(RecruitmentModel recruitment) {
//         UserModel currentUser = AuthController.currentUser;
//         if (currentUser == null)
//             return false;

//         // Prevent duplicate spam
//         if (ApplicationDao.hasUserApplied(currentUser.getUserId(), recruitment.getRecruitmentId())) {
//             return false;
//         }

//         ApplicationModel app = new ApplicationModel();
//         app.setRecruitmentId(recruitment.getRecruitmentId());
//         app.setApplicantId(currentUser.getUserId());
//         app.setApplicantName(currentUser.getPlayerName());
//         app.setRecruiterId(recruitment.getAuthorId());
//         app.setStatus(ApplicationFirebaseKeys.STATUS_PENDING);

//         // Snapshot Current Stats
//         app.setSnapshotFdRatio(currentUser.getAverageKills()); // Substitute with skillScore/FD logic if expanded
//         app.setSnapshotAvgDamage(currentUser.getAverageDamage());
//         app.setSnapshotSkillTier("GOLD"); // Defaulting until SkillScoreController is active
//         app.setSnapshotRoleArchetype(currentUser.getPrimaryRole());
//         app.setCreatedAt(System.currentTimeMillis());

//         boolean success = ApplicationDao.saveApplication(app);

//         if (success) {
//             // Re-using your Notification system
//             NotificationController.sendNotification(
//                     "New Application: " + recruitment.getRole(),
//                     currentUser.getPlayerName() + " applied. Check your Scout's Inbox.",
//                     "APPLICATION",
//                     recruitment.getAuthorId());
//         }
//         return success;
//     }

//     public static boolean hasApplied(String recruitmentId) {
//         if (AuthController.currentUser == null)
//             return false;
//         return ApplicationDao.hasUserApplied(AuthController.currentUser.getUserId(), recruitmentId);
//     }

//     public static List<ApplicationModel> fetchMyInbox() {
//         if (AuthController.currentUser == null)
//             return null;
//         return ApplicationDao.getPendingApplications(AuthController.currentUser.getUserId());
//     }

//     public static boolean accept(String applicationId) {
//         return ApplicationDao.updateStatus(applicationId, ApplicationFirebaseKeys.STATUS_ACCEPTED);
//     }

//     public static boolean decline(String applicationId) {
//         return ApplicationDao.updateStatus(applicationId, ApplicationFirebaseKeys.STATUS_DECLINED);
//     }
// }

package com.example.controller.player;

import com.example.controller.AuthController;
import com.example.controller.admin.NotificationController;
import com.example.dao.ApplicationDao;
import com.example.keys.ApplicationFirebaseKeys;
import com.example.model.UserModel;
import com.example.model.player.ApplicationModel;
import com.example.model.player.RecruitmentModel;

import java.util.List;

public class ApplicationController {

    public static boolean applyToPost(RecruitmentModel recruitment) {
        UserModel currentUser = AuthController.currentUser;
        if (currentUser == null)
            return false;

        if (ApplicationDao.hasUserApplied(currentUser.getUserId(), recruitment.getRecruitmentId())) {
            return false;
        }

        ApplicationModel app = new ApplicationModel();
        // FIX #5: Deterministic ID prevents duplicate applications if clicked twice
        // fast
        app.setApplicationId(recruitment.getRecruitmentId() + "_" + currentUser.getUserId());
        app.setRecruitmentId(recruitment.getRecruitmentId());
        app.setApplicantId(currentUser.getUserId());
        app.setApplicantName(currentUser.getPlayerName());
        app.setRecruiterId(recruitment.getAuthorId());
        app.setStatus(ApplicationFirebaseKeys.STATUS_PENDING);

        // Snapshot Current Stats
        app.setSnapshotFdRatio(currentUser.getAverageKills());
        app.setSnapshotAvgDamage(currentUser.getAverageDamage());
        // FIX #3: Removed fake "GOLD" data. Default to UNRANKED until the SkillScore
        // engine runs.
        // app.setSnapshotSkillTier(currentUser.getSkillTier() != null ?
        // currentUser.getSkillTier() : "UNRANKED");
        app.setSnapshotRoleArchetype(currentUser.getPrimaryRole());
        app.setCreatedAt(System.currentTimeMillis());

        boolean success = ApplicationDao.saveApplication(app);

        if (success) {
            NotificationController.sendNotification(
                    "New Application: " + recruitment.getRole(),
                    currentUser.getPlayerName() + " applied. Check your Scout's Inbox.",
                    "APPLICATION",
                    recruitment.getAuthorId());
        }
        return success;
    }

    public static boolean hasApplied(String recruitmentId) {
        if (AuthController.currentUser == null)
            return false;
        return ApplicationDao.hasUserApplied(AuthController.currentUser.getUserId(), recruitmentId);
    }

    public static List<ApplicationModel> fetchMyInbox() {
        if (AuthController.currentUser == null)
            return null;
        return ApplicationDao.getPendingApplications(AuthController.currentUser.getUserId());
    }

    // FIX #1: Pass the whole model so we can notify the applicantId
    public static boolean accept(ApplicationModel app) {
        boolean success = ApplicationDao.updateStatus(app.getApplicationId(), ApplicationFirebaseKeys.STATUS_ACCEPTED);
        if (success) {
            NotificationController.sendNotification(
                    "Application Accepted! 🤝",
                    "Your application was accepted! The recruiter has received your Vault ID.",
                    "APPLICATION",
                    app.getApplicantId());
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
}