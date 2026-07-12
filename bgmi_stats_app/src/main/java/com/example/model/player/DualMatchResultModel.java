// package com.example.model.player;

// public class DualMatchResultModel {
// public String winnerSide; // "A", "B", "UNCLEAR"
// public boolean isSameMatch;
// public double confidence;

// // Team A Extracted Stats
// public int teamAScore;
// public String teamAMvpIgn;
// public int teamAKills;
// public double teamAFdRatio;
// public int teamAAssists;

// // Team B Extracted Stats
// public int teamBScore;
// public String teamBMvpIgn;
// public int teamBKills;
// public double teamBFdRatio;
// public int teamBAssists;
// }

package com.example.model.player;

public class DualMatchResultModel {
    public ImageExtraction imageOne; // Team A's Upload
    public ImageExtraction imageTwo; // Team B's Upload

    public static class ImageExtraction {
        public String bannerText; // "VICTORY", "DEFEAT", "NOT_A_RESULT_SCREEN"
        public int leftSideScore;
        public int rightSideScore;

        public String leftSideMvpIgn;
        public int leftSideFinishes;
        public double leftSideFdRatio;
        public int leftSideAssists;

        public String rightSideMvpIgn;
        public int rightSideFinishes;
        public double rightSideFdRatio;
        public int rightSideAssists;

        public String onScreenTimestamp;
        public String matchSessionTag;
        public String imageQuality; // "CLEAR", "BLURRY", "PARTIAL"
    }
}