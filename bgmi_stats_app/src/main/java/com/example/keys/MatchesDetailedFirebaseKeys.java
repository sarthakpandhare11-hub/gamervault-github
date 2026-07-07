package com.example.keys;

public interface MatchesDetailedFirebaseKeys {

    // --- COLLECTION NAME ---
    public static final String MATCH_DETAILS_COLLECTION = "MatchDetails";

    // --- SHARED IDENTIFIERS (Crucial for linking) ---
    public static final String FIELD_MATCH_ID = "matchId";
    public static final String FIELD_USER_ID = "userId";

    // --- DETAILED STATS FIELDS (MatchExtractionResultModel) ---
    public static final String FIELD_RESCUES = "rescues";
    public static final String FIELD_RECALLS = "recalls";

    public static final String FIELD_SURVIVAL_SCORE = "survivalScore";
    public static final String FIELD_TRAVEL_DISTANCE = "travelDistance";

    public static final String FIELD_SUPPORT_SCORE = "supportScore";
    public static final String FIELD_HEALTH_RESTORED = "healthRestored";

    public static final String FIELD_SUPPLIES_SCORE = "suppliesScore";
    public static final String FIELD_TOTAL_SUPPLIES = "totalSupplies";
    public static final String FIELD_ADVANCED_SUPPLIES = "advancedSupplies";

    // --- WEAPON & THROW FIELDS ---
    public static final String FIELD_WEAPON_TYPE = "weaponType";
    public static final String FIELD_WEAPON_NAME = "weaponName";
    public static final String FIELD_WEAPON_DAMAGE = "weaponDamage";
    public static final String FIELD_WEAPON_ELIMINATIONS = "weaponEliminations";
    public static final String FIELD_WEAPON_KNOCKDOWNS = "weaponKnockdowns";
    public static final String FIELD_WEAPON_USES = "weaponUses";

    public static final String FIELD_CLOSE_RANGE_THROWS = "closeRangeThrows";
    public static final String FIELD_LONG_RANGE_THROWS = "longRangeThrows";

    // --- AI METADATA (Detail Level) ---
    public static final String FIELD_EXTRACTION_SUCCESSFUL = "extractionSuccessful";
    public static final String FIELD_WARNINGS = "warnings";
}