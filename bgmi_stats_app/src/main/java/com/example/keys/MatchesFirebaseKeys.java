package com.example.keys;

public interface MatchesFirebaseKeys {

    // --- COLLECTION NAME ---
    public static final String MATCHES_COLLECTION = "Matches";

    // --- SHARED IDENTIFIERS ---
    // ALIGNED: Changed from uppercase/snake_case variants to match standard
    // camelCase variables
    public static final String FIELD_MATCH_ID = "matchId";
    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_CREATED_AT = "createdAt";

    // --- SUMMARY FIELDS (MatchModel) ---
    public static final String FIELD_MATCH_NAME = "matchName";
    public static final String FIELD_MATCH_TYPE = "matchType";
    public static final String FIELD_IMAGE_URLS = "imageUrls";

    public static final String FIELD_MAP = "map";
    public static final String FIELD_GAME_MODE = "gameMode";
    public static final String FIELD_PERSPECTIVE = "perspective";
    public static final String FIELD_TEAM_PLACEMENT = "teamPlacement";
    public static final String FIELD_TOTAL_PLAYERS = "totalPlayers";
    public static final String FIELD_MATCH_RESULT = "matchResult";
    public static final String FIELD_MATCH_DATE = "matchDate";

    public static final String FIELD_KILLS = "kills";
    public static final String FIELD_ASSISTS = "assists";
    public static final String FIELD_DAMAGE = "damage";
    public static final String FIELD_SURVIVAL_TIME = "survivalTime";
    public static final String FIELD_RATING = "rating";
    public static final String FIELD_ROLE = "role";
    public static final String FIELD_MAIN_WEAPON = "mainWeapon";
    public static final String FIELD_MVP = "mvp";
    public static final String FIELD_TEAM_GRADE = "teamGrade";

    // --- AI META DATA ---
    public static final String FIELD_VERIFIED_BY_GEMINI = "verifiedByGemini";
    public static final String FIELD_CONFIDENCE_SCORE = "confidenceScore";
    public static final String FIELD_PROCESSING_STATUS = "processingStatus";
}