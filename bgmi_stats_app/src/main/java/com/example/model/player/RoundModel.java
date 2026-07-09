package com.example.model.player;

public class RoundModel {
    private int roundNumber; // 1, 2, or 3

    // Team A Submission
    private String teamASubmittedBy;
    private String teamAScreenshotUrl;
    private String teamAClaimedOutcome; // "WIN" or "LOSS"
    private String teamAOcrResult; // Filled by Gemini

    // Team B Submission
    private String teamBSubmittedBy;
    private String teamBScreenshotUrl;
    private String teamBClaimedOutcome; // "WIN" or "LOSS"
    private String teamBOcrResult; // Filled by Gemini

    // Round Status
    private String roundStatus; // "PENDING", "VERIFIED", "DISPUTED"
    private String winningTeam; // "A", "B", or "NONE"

    public RoundModel() {
    }

    // GETTERS AND SETTERS
    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public String getTeamASubmittedBy() {
        return teamASubmittedBy;
    }

    public void setTeamASubmittedBy(String teamASubmittedBy) {
        this.teamASubmittedBy = teamASubmittedBy;
    }

    public String getTeamAScreenshotUrl() {
        return teamAScreenshotUrl;
    }

    public void setTeamAScreenshotUrl(String teamAScreenshotUrl) {
        this.teamAScreenshotUrl = teamAScreenshotUrl;
    }

    public String getTeamAClaimedOutcome() {
        return teamAClaimedOutcome;
    }

    public void setTeamAClaimedOutcome(String teamAClaimedOutcome) {
        this.teamAClaimedOutcome = teamAClaimedOutcome;
    }

    public String getTeamAOcrResult() {
        return teamAOcrResult;
    }

    public void setTeamAOcrResult(String teamAOcrResult) {
        this.teamAOcrResult = teamAOcrResult;
    }

    public String getTeamBSubmittedBy() {
        return teamBSubmittedBy;
    }

    public void setTeamBSubmittedBy(String teamBSubmittedBy) {
        this.teamBSubmittedBy = teamBSubmittedBy;
    }

    public String getTeamBScreenshotUrl() {
        return teamBScreenshotUrl;
    }

    public void setTeamBScreenshotUrl(String teamBScreenshotUrl) {
        this.teamBScreenshotUrl = teamBScreenshotUrl;
    }

    public String getTeamBClaimedOutcome() {
        return teamBClaimedOutcome;
    }

    public void setTeamBClaimedOutcome(String teamBClaimedOutcome) {
        this.teamBClaimedOutcome = teamBClaimedOutcome;
    }

    public String getTeamBOcrResult() {
        return teamBOcrResult;
    }

    public void setTeamBOcrResult(String teamBOcrResult) {
        this.teamBOcrResult = teamBOcrResult;
    }

    public String getRoundStatus() {
        return roundStatus;
    }

    public void setRoundStatus(String roundStatus) {
        this.roundStatus = roundStatus;
    }

    public String getWinningTeam() {
        return winningTeam;
    }

    public void setWinningTeam(String winningTeam) {
        this.winningTeam = winningTeam;
    }
}