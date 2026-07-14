package com.example.controller.player;

import com.example.controller.AuthController;
import com.example.dao.ProfileDao;
import com.example.model.UserModel;
import com.example.model.player.ProfileViewContext;

public class ProfileController {

    public static String targetViewUserId = null;

    // The core privacy gate logic
    public static ProfileViewContext getActiveProfileContext() {
        String targetId = getActiveProfileId();
        UserModel fetchedUser = ProfileDao.fetchUserProfile(targetId);

        if (fetchedUser == null)
            return null;

        ProfileViewContext context = new ProfileViewContext();
        context.setUser(fetchedUser);

        UserModel activeUser = AuthController.currentUser;

        // 1. Is it their own profile?
        if (activeUser != null && activeUser.getUserId().equals(targetId)) {
            context.setOwnProfile(true);
            context.setPrivate(false);
            context.setConnected(true);
            return context;
        }

        // 2. Someone else's profile
        context.setOwnProfile(false);
        context.setPrivate("PRIVATE".equalsIgnoreCase(fetchedUser.getPrivacyStatus()));

        // 3. Are they connected? (Allowing DM and stats viewing if private)
        boolean connected = false;
        if (activeUser != null && fetchedUser.getConnectionIds() != null) {
            connected = fetchedUser.getConnectionIds().contains(activeUser.getUserId());
        }
        context.setConnected(connected);

        return context;
    }

    public static String getActiveProfileId() {
        if (targetViewUserId != null) {
            return targetViewUserId;
        }
        return AuthController.currentUser != null ? AuthController.currentUser.getUserId() : "GUEST";
    }

    public static void setTargetProfile(String userId) {
        targetViewUserId = userId;
    }

    public static void clearTargetProfile() {
        targetViewUserId = null;
    }

    public static boolean updateUserProfile(String userId, UserModel updatedUser) {
        return ProfileDao.updateUserProfile(userId, updatedUser);
    }

    public static UserModel getUserProfile(String userId) {
        return ProfileDao.fetchUserProfile(userId);
    }
}