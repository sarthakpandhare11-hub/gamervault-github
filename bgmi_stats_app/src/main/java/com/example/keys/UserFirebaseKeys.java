package com.example.keys;

public interface UserFirebaseKeys {

    // COLLECTION
    public static final String USERS_COLLECTION = "users";

    // DOCUMENT : this is userId (auth)
    // We have saved the userId as document name in the database.

    // FIELDS
    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_PLAYER_NAME = "playerName";
    public static final String FIELD_IGN = "ign";
    public static final String FIELD_PROFILE_IMAGE_URL = "profileImageURL";
    public static final String FIELD_ROLE = "role";
    public static final String FIELD_COIN_BALANCE = "coinBalance";

    public static final String FIELD_PRIVACY_STATUS = "privacyStatus";
    public static final String FIELD_AVAILABILITY = "availability";
    public static final String FIELD_COMPETITIVE_TIER = "competitiveTier";
    public static final String FIELD_T1_SCRIM_EXPERIENCE = "t1ScrimExperience";
    public static final String FIELD_PREFERRED_WEAPON = "preferredWeapon";
    public static final String FIELD_CONNECTION_IDS = "connectionIds";
}
