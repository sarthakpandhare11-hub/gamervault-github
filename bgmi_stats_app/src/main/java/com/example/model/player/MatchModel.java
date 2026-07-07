package com.example.model.player;

import java.util.List;

public class MatchModel {
    // Core Identity
    private String matchId;
    private String userId;
    private String matchName;
    private String matchType;
    private long createdAt;

    // Firebase Storage Evidence
    private List<String> imageUrls;

    // Extracted Match Info
    private String map;
    private String gameMode;
    private String perspective;
    private int teamPlacement;
    private int totalPlayers;
    private String matchResult;
    private String matchDate;

    // Extracted Player Stats
    private int kills;
    private int assists;
    private int damage;
    private String survivalTime;
    private double rating;
    private String role;
    private String mainWeapon;
    private boolean mvp;

    // Extended Stats
    private double teamGrade;
    private int rescues;
    private int recalls;
    private double survivalScore;
    private double travelDistance;
    private double supportScore;
    private int healthRestored;
    private double suppliesScore;
    private int totalSupplies;
    private int advancedSupplies;

    // Weapon & Throw Stats
    private String weaponType;
    private String weaponName;
    private double weaponDamage;
    private int weaponEliminations;
    private int weaponKnockdowns;
    private int weaponUses;
    private int closeRangeThrows;
    private int longRangeThrows;

    // AI Tracking
    private boolean verifiedByGemini;
    private double confidenceScore;
    private String processingStatus;

    public MatchModel() {

    }

    // GETTERS AND SETTERS

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMatchName() {
        return matchName;
    }

    public void setMatchName(String matchName) {
        this.matchName = matchName;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public String getPerspective() {
        return perspective;
    }

    public void setPerspective(String perspective) {
        this.perspective = perspective;
    }

    public int getTeamPlacement() {
        return teamPlacement;
    }

    public void setTeamPlacement(int teamPlacement) {
        this.teamPlacement = teamPlacement;
    }

    public int getTotalPlayers() {
        return totalPlayers;
    }

    public void setTotalPlayers(int totalPlayers) {
        this.totalPlayers = totalPlayers;
    }

    public String getMatchResult() {
        return matchResult;
    }

    public void setMatchResult(String matchResult) {
        this.matchResult = matchResult;
    }

    public String getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(String matchDate) {
        this.matchDate = matchDate;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getAssists() {
        return assists;
    }

    public void setAssists(int assists) {
        this.assists = assists;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public String getSurvivalTime() {
        return survivalTime;
    }

    public void setSurvivalTime(String survivalTime) {
        this.survivalTime = survivalTime;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMainWeapon() {
        return mainWeapon;
    }

    public void setMainWeapon(String mainWeapon) {
        this.mainWeapon = mainWeapon;
    }

    public boolean isMvp() {
        return mvp;
    }

    public void setMvp(boolean mvp) {
        this.mvp = mvp;
    }

    public double getTeamGrade() {
        return teamGrade;
    }

    public void setTeamGrade(double teamGrade) {
        this.teamGrade = teamGrade;
    }

    public int getRescues() {
        return rescues;
    }

    public void setRescues(int rescues) {
        this.rescues = rescues;
    }

    public int getRecalls() {
        return recalls;
    }

    public void setRecalls(int recalls) {
        this.recalls = recalls;
    }

    public double getSurvivalScore() {
        return survivalScore;
    }

    public void setSurvivalScore(double survivalScore) {
        this.survivalScore = survivalScore;
    }

    public double getTravelDistance() {
        return travelDistance;
    }

    public void setTravelDistance(double travelDistance) {
        this.travelDistance = travelDistance;
    }

    public double getSupportScore() {
        return supportScore;
    }

    public void setSupportScore(double supportScore) {
        this.supportScore = supportScore;
    }

    public int getHealthRestored() {
        return healthRestored;
    }

    public void setHealthRestored(int healthRestored) {
        this.healthRestored = healthRestored;
    }

    public double getSuppliesScore() {
        return suppliesScore;
    }

    public void setSuppliesScore(double suppliesScore) {
        this.suppliesScore = suppliesScore;
    }

    public int getTotalSupplies() {
        return totalSupplies;
    }

    public void setTotalSupplies(int totalSupplies) {
        this.totalSupplies = totalSupplies;
    }

    public int getAdvancedSupplies() {
        return advancedSupplies;
    }

    public void setAdvancedSupplies(int advancedSupplies) {
        this.advancedSupplies = advancedSupplies;
    }

    public String getWeaponType() {
        return weaponType;
    }

    public void setWeaponType(String weaponType) {
        this.weaponType = weaponType;
    }

    public String getWeaponName() {
        return weaponName;
    }

    public void setWeaponName(String weaponName) {
        this.weaponName = weaponName;
    }

    public double getWeaponDamage() {
        return weaponDamage;
    }

    public void setWeaponDamage(double weaponDamage) {
        this.weaponDamage = weaponDamage;
    }

    public int getWeaponEliminations() {
        return weaponEliminations;
    }

    public void setWeaponEliminations(int weaponEliminations) {
        this.weaponEliminations = weaponEliminations;
    }

    public int getWeaponKnockdowns() {
        return weaponKnockdowns;
    }

    public void setWeaponKnockdowns(int weaponKnockdowns) {
        this.weaponKnockdowns = weaponKnockdowns;
    }

    public int getWeaponUses() {
        return weaponUses;
    }

    public void setWeaponUses(int weaponUses) {
        this.weaponUses = weaponUses;
    }

    public int getCloseRangeThrows() {
        return closeRangeThrows;
    }

    public void setCloseRangeThrows(int closeRangeThrows) {
        this.closeRangeThrows = closeRangeThrows;
    }

    public int getLongRangeThrows() {
        return longRangeThrows;
    }

    public void setLongRangeThrows(int longRangeThrows) {
        this.longRangeThrows = longRangeThrows;
    }

    public boolean isVerifiedByGemini() {
        return verifiedByGemini;
    }

    public void setVerifiedByGemini(boolean verifiedByGemini) {
        this.verifiedByGemini = verifiedByGemini;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }

}