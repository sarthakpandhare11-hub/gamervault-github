package com.example.model.player;

public class ApplicationModel {
    private String applicationId;
    private String recruitmentId;
    private String applicantId;
    private String applicantName; // Useful for UI display without extra queries
    private String recruiterId;
    private String status;

    private double snapshotFdRatio;
    private double snapshotAvgDamage;
    private String snapshotSkillTier;
    private String snapshotRoleArchetype;

    private long createdAt;

    public ApplicationModel() {
    }

    // GETTERS AND SETTERS
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getRecruitmentId() {
        return recruitmentId;
    }

    public void setRecruitmentId(String recruitmentId) {
        this.recruitmentId = recruitmentId;
    }

    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getRecruiterId() {
        return recruiterId;
    }

    public void setRecruiterId(String recruiterId) {
        this.recruiterId = recruiterId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getSnapshotFdRatio() {
        return snapshotFdRatio;
    }

    public void setSnapshotFdRatio(double snapshotFdRatio) {
        this.snapshotFdRatio = snapshotFdRatio;
    }

    public double getSnapshotAvgDamage() {
        return snapshotAvgDamage;
    }

    public void setSnapshotAvgDamage(double snapshotAvgDamage) {
        this.snapshotAvgDamage = snapshotAvgDamage;
    }

    public String getSnapshotSkillTier() {
        return snapshotSkillTier;
    }

    public void setSnapshotSkillTier(String snapshotSkillTier) {
        this.snapshotSkillTier = snapshotSkillTier;
    }

    public String getSnapshotRoleArchetype() {
        return snapshotRoleArchetype;
    }

    public void setSnapshotRoleArchetype(String snapshotRoleArchetype) {
        this.snapshotRoleArchetype = snapshotRoleArchetype;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}