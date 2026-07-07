package com.example.keys;

public interface UserFirebaseKeys {

    // COLLECTION
    String USERS_COLLECTION = "users";

    // DOCUMENT : this is userId (auth)
    // We have saved the userId as document name in the database.

    // FIELDS
    String FIELD_USER_ID = "userId";
    String FIELD_EMAIL = "email";
    String FIELD_PLAYER_NAME = "playerName";
    String FIELD_IGN = "ign";
    String FIELD_PROFILE_IMAGE_URL = "profileImageURL";
    String FIELD_ROLE = "role";
}
