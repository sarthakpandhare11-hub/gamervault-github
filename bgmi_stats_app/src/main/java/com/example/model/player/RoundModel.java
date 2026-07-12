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

    private boolean acceptedByA = false;
    private boolean acceptedByB = false;

    // --- NEW: Team A Extracted Stats ---
    private int teamAScore;
    private int teamAKills;
    private double teamAFdRatio;
    private String teamAMvpIgn;
    private int teamAAssists;

    // --- NEW: Team B Extracted Stats ---
    private int teamBScore;
    private int teamBKills;
    private double teamBFdRatio;
    private String teamBMvpIgn;
    private int teamBAssists;

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

    public boolean isAcceptedByA() {
        return acceptedByA;
    }

    public void setAcceptedByA(boolean acceptedByA) {
        this.acceptedByA = acceptedByA;
    }

    public boolean isAcceptedByB() {
        return acceptedByB;
    }

    public void setAcceptedByB(boolean acceptedByB) {
        this.acceptedByB = acceptedByB;
    }

    // --- GETTERS & SETTERS FOR STATS ---

    public int getTeamAScore() {
        return teamAScore;
    }

    public void setTeamAScore(int teamAScore) {
        this.teamAScore = teamAScore;
    }

    public int getTeamAKills() {
        return teamAKills;
    }

    public void setTeamAKills(int teamAKills) {
        this.teamAKills = teamAKills;
    }

    public double getTeamAFdRatio() {
        return teamAFdRatio;
    }

    public void setTeamAFdRatio(double teamAFdRatio) {
        this.teamAFdRatio = teamAFdRatio;
    }

    public String getTeamAMvpIgn() {
        return teamAMvpIgn;
    }

    public void setTeamAMvpIgn(String teamAMvpIgn) {
        this.teamAMvpIgn = teamAMvpIgn;
    }

    public int getTeamAAssists() {
        return teamAAssists;
    }

    public void setTeamAAssists(int teamAAssists) {
        this.teamAAssists = teamAAssists;
    }

    public int getTeamBScore() {
        return teamBScore;
    }

    public void setTeamBScore(int teamBScore) {
        this.teamBScore = teamBScore;
    }

    public int getTeamBKills() {
        return teamBKills;
    }

    public void setTeamBKills(int teamBKills) {
        this.teamBKills = teamBKills;
    }

    public double getTeamBFdRatio() {
        return teamBFdRatio;
    }

    public void setTeamBFdRatio(double teamBFdRatio) {
        this.teamBFdRatio = teamBFdRatio;
    }

    public String getTeamBMvpIgn() {
        return teamBMvpIgn;
    }

    public void setTeamBMvpIgn(String teamBMvpIgn) {
        this.teamBMvpIgn = teamBMvpIgn;
    }

    public int getTeamBAssists() {
        return teamBAssists;
    }

    public void setTeamBAssists(int teamBAssists) {
        this.teamBAssists = teamBAssists;
    }
}