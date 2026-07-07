package com.example.controller.player;

import com.example.dao.ProfileDao;
import com.example.model.UserModel;

public class ProfileController {

    public static UserModel getUserProfile(String userId) {

        return ProfileDao.fetchUserProfile(userId);
    }

    public static boolean updateUserProfile(String userId, UserModel updatedUser) {
        return ProfileDao.updateUserProfile(userId, updatedUser);
    }
}
