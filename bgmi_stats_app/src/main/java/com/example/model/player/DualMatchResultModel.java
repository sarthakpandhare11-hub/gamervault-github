package com.example.model.player;

public class DualMatchResultModel {
    public String winnerSide; // "A", "B", "UNCLEAR"
    public boolean isSameMatch;
    public double confidence;

    // Team A Extracted Stats
    public int teamAScore;
    public String teamAMvpIgn;
    public int teamAKills;
    public double teamAFdRatio;
    public int teamAAssists;

    // Team B Extracted Stats
    public int teamBScore;
    public String teamBMvpIgn;
    public int teamBKills;
    public double teamBFdRatio;
    public int teamBAssists;
}