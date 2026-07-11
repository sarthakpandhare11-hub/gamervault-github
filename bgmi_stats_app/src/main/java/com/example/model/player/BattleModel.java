package com.example.model.player;

import java.util.List;
import java.util.Map;

public class BattleModel {
    private String gameTitle; // "BGMI", "VALORANT", "FREE_FIRE"

    private String battleId;
    private String hostType; // "PLAYER" or "ADMIN"
    private String hostUserId;
    private String mode; // "1V1", "2V2", "4V4"
    private String format; // "BO1", "BO3"
    private String status; // "OPEN", "LOCKED", "IN_PROGRESS", "COMPLETED", "DISPUTED"

    // Financials
    private double entryFeeCoins;
    private double prizePoolAmount;

    // Room Details (Revealed only when status == LOCKED)
    private String roomId;
    private String roomPassword;

    // Participants - Maps UserId to their Team ("A" or "B")
    private Map<String, String> participants;
    private int maxParticipants;

    // BO1 or BO3 Tracking
    private List<RoundModel> rounds;
    private String overallWinner; // "A" or "B"

    private long createdAt;
    private long lockedAt;

    private java.util.List<String> participantIds;

    private String teamASubmittedBy;
    private String teamBSubmittedBy;

    public BattleModel() {
    }

    // GETTERS AND SETTERS

    public String getGameTitle() {
        return gameTitle;
    }

    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }

    public String getBattleId() {
        return battleId;
    }

    public void setBattleId(String battleId) {
        this.battleId = battleId;
    }

    public String getHostType() {
        return hostType;
    }

    public void setHostType(String hostType) {
        this.hostType = hostType;
    }

    public String getHostUserId() {
        return hostUserId;
    }

    public void setHostUserId(String hostUserId) {
        this.hostUserId = hostUserId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getEntryFeeCoins() {
        return entryFeeCoins;
    }

    public void setEntryFeeCoins(double entryFeeCoins) {
        this.entryFeeCoins = entryFeeCoins;
    }

    public double getPrizePoolAmount() {
        return prizePoolAmount;
    }

    public void setPrizePoolAmount(double prizePoolAmount) {
        this.prizePoolAmount = prizePoolAmount;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomPassword() {
        return roomPassword;
    }

    public void setRoomPassword(String roomPassword) {
        this.roomPassword = roomPassword;
    }

    public Map<String, String> getParticipants() {
        return participants;
    }

    public void setParticipants(Map<String, String> participants) {
        this.participants = participants;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public List<RoundModel> getRounds() {
        return rounds;
    }

    public void setRounds(List<RoundModel> rounds) {
        this.rounds = rounds;
    }

    public String getOverallWinner() {
        return overallWinner;
    }

    public void setOverallWinner(String overallWinner) {
        this.overallWinner = overallWinner;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(long lockedAt) {
        this.lockedAt = lockedAt;
    }

    public List<String> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<String> participantIds) {
        this.participantIds = participantIds;
    }

    public String getTeamASubmittedBy() {
        return teamASubmittedBy;
    }

    public void setTeamASubmittedBy(String teamASubmittedBy) {
        this.teamASubmittedBy = teamASubmittedBy;
    }

    public String getTeamBSubmittedBy() {
        return teamBSubmittedBy;
    }

    public void setTeamBSubmittedBy(String teamBSubmittedBy) {
        this.teamBSubmittedBy = teamBSubmittedBy;
    }
}