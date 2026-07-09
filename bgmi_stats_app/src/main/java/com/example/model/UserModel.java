package com.example.model;

public class UserModel {

    // AUTHENTICATION
    private String userId;
    private String email;
    private String role;

    // PROFILE NEEDS and PLAYER INFORMATION
    private String playerName;
    private String ign;
    private String profileImageURL;
    private String bio;

    private String primaryRole;

    // STATISTICS
    private int totalMatches;
    private int totalKills;
    private int totalDamage;

    private double averageKills;
    private double averageDamage;
    private double winRate;
    private double averagePlacement;
    private double averageSurvivalTime;

    // ACHIEVEMENTS (For Analytics Screen)
    private int highestKillsMatch;
    private int highestDamageMatch;
    private int bestPlacement;

    // ACCOUNT STATUS
    private boolean verified;

    private String createdAt;
    private String updatedAt;

    private double coinBalance = 500.0; // Give new users 500 coins to test with

    // GETTER AND SETTERS FOR THE USER MODEL

    // GETTERS AND SETTERS

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getIgn() {
        return ign;
    }

    public void setIgn(String ign) {
        this.ign = ign;
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }

    public void setProfileImageURL(String profileImageURL) {
        this.profileImageURL = profileImageURL;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPrimaryRole() {
        return primaryRole;
    }

    public void setPrimaryRole(String primaryRole) {
        this.primaryRole = primaryRole;
    }

    public int getTotalMatches() {
        return totalMatches;
    }

    public void setTotalMatches(int totalMatches) {
        this.totalMatches = totalMatches;
    }

    public int getTotalKills() {
        return totalKills;
    }

    public void setTotalKills(int totalKills) {
        this.totalKills = totalKills;
    }

    public int getTotalDamage() {
        return totalDamage;
    }

    public void setTotalDamage(int totalDamage) {
        this.totalDamage = totalDamage;
    }

    public double getAverageKills() {
        return averageKills;
    }

    public void setAverageKills(double averageKills) {
        this.averageKills = averageKills;
    }

    public double getAverageDamage() {
        return averageDamage;
    }

    public void setAverageDamage(double averageDamage) {
        this.averageDamage = averageDamage;
    }

    public double getWinRate() {
        return winRate;
    }

    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }

    public double getAveragePlacement() {
        return averagePlacement;
    }

    public void setAveragePlacement(double averagePlacement) {
        this.averagePlacement = averagePlacement;
    }

    public double getAverageSurvivalTime() {
        return averageSurvivalTime;
    }

    public void setAverageSurvivalTime(double averageSurvivalTime) {
        this.averageSurvivalTime = averageSurvivalTime;
    }

    public int getHighestKillsMatch() {
        return highestKillsMatch;
    }

    public void setHighestKillsMatch(int highestKillsMatch) {
        this.highestKillsMatch = highestKillsMatch;
    }

    public int getHighestDamageMatch() {
        return highestDamageMatch;
    }

    public void setHighestDamageMatch(int highestDamageMatch) {
        this.highestDamageMatch = highestDamageMatch;
    }

    public int getBestPlacement() {
        return bestPlacement;
    }

    public void setBestPlacement(int bestPlacement) {
        this.bestPlacement = bestPlacement;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public double getCoinBalance() {
        return coinBalance;
    }

    public void setCoinBalance(double coinBalance) {
        this.coinBalance = coinBalance;
    }
}
