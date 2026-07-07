package com.example.model.player;

public class RecruitmentModel {
    // Core Identity
    private String recruitmentId;
    private String authorId;
    private String teamName;
    private String teamLogoURL;
    private boolean verifiedTeam;

    // Position Attributes
    private String role; // e.g., IGL, ENTRY FRAGGER, SNIPER
    private String lineUpType; // e.g., MAIN ROSTER, ACADEMY, UNDER-19
    private String region; // e.g., INDIA, GLOBAL
    private String primaryWeapon; // e.g., M416, BERYL M762, AWM
    private String status; // e.g., OPEN, CLOSED

    // Performance Threshold Filters
    private double minFdRatio;
    private int minAvgDamage;
    private String experienceTier; // T1 SCRIMS, LAN EXPERIENCED, BGIS SEMIS+

    // Text Descriptions
    private String description;
    private String detailedRequirements;

    // Metadata Timeline
    private long createdAt;
    private long updatedAt;

    public RecruitmentModel() {

    }

    public String getRecruitmentId() {
        return recruitmentId;
    }

    public void setRecruitmentId(String recruitmentId) {
        this.recruitmentId = recruitmentId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamLogoURL() {
        return teamLogoURL;
    }

    public void setTeamLogoURL(String teamLogoURL) {
        this.teamLogoURL = teamLogoURL;
    }

    public boolean isVerifiedTeam() {
        return verifiedTeam;
    }

    public void setVerifiedTeam(boolean verifiedTeam) {
        this.verifiedTeam = verifiedTeam;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLineUpType() {
        return lineUpType;
    }

    public void setLineUpType(String lineUpType) {
        this.lineUpType = lineUpType;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPrimaryWeapon() {
        return primaryWeapon;
    }

    public void setPrimaryWeapon(String primaryWeapon) {
        this.primaryWeapon = primaryWeapon;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getMinFdRatio() {
        return minFdRatio;
    }

    public void setMinFdRatio(double minFdRatio) {
        this.minFdRatio = minFdRatio;
    }

    public int getMinAvgDamage() {
        return minAvgDamage;
    }

    public void setMinAvgDamage(int minAvgDamage) {
        this.minAvgDamage = minAvgDamage;
    }

    public String getExperienceTier() {
        return experienceTier;
    }

    public void setExperienceTier(String experienceTier) {
        this.experienceTier = experienceTier;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetailedRequirements() {
        return detailedRequirements;
    }

    public void setDetailedRequirements(String detailedRequirements) {
        this.detailedRequirements = detailedRequirements;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

}