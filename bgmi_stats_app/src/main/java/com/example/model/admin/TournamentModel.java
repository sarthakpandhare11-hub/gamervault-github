package com.example.model.admin;

public class TournamentModel {
    private String tournamentId;
    private String title;
    private String game;
    private String prizePool;
    private String status;
    private int slotsMax;
    private int slotsRegistered;
    private String startDate;

    private String regBeginDate;
    private String regEndDate;
    private java.util.List<String> imageUrls;
    private String organizerName;
    private String instagramLink;

    public TournamentModel() {
    }

    public String getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getPrizePool() {
        return prizePool;
    }

    public void setPrizePool(String prizePool) {
        this.prizePool = prizePool;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getSlotsMax() {
        return slotsMax;
    }

    public void setSlotsMax(int slotsMax) {
        this.slotsMax = slotsMax;
    }

    public int getSlotsRegistered() {
        return slotsRegistered;
    }

    public void setSlotsRegistered(int slotsRegistered) {
        this.slotsRegistered = slotsRegistered;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getRegBeginDate() {
        return regBeginDate;
    }

    public void setRegBeginDate(String regBeginDate) {
        this.regBeginDate = regBeginDate;
    }

    public String getRegEndDate() {
        return regEndDate;
    }

    public void setRegEndDate(String regEndDate) {
        this.regEndDate = regEndDate;
    }

    public java.util.List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(java.util.List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getInstagramLink() {
        return instagramLink;
    }

    public void setInstagramLink(String instagramLink) {
        this.instagramLink = instagramLink;
    }

}