package com.example.view.player;

import java.util.Arrays;
import java.util.List;

import com.example.controller.AuthController;
import com.example.controller.admin.ContentController;
import com.example.controller.admin.TournamentController;
import com.example.controller.player.LeaderboardController;
import com.example.controller.player.MatchController;
import com.example.controller.player.RecruitmentController;
import com.example.model.UserModel;
import com.example.model.admin.ContentModel;
import com.example.model.admin.TournamentModel;
import com.example.model.player.MatchModel;
import com.example.model.player.RecruitmentModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class PlayerDashboardScreen {

        Scene playerDashboardScreenScene;
        Stage playerDashboardScreenStage;
        private PlayerMainScreen mainScreen;

        private VBox mainContainer;
        private VBox heroContainer;
        private HBox row1;
        private HBox row2;
        private VBox loadingBox;

        public void setPlayerDashboardScreenScene(Scene playerDashboardScreenScene) {
                this.playerDashboardScreenScene = playerDashboardScreenScene;
        }

        public void setPlayerDashboardScreenStage(Stage playerDashboardScreenStage) {
                this.playerDashboardScreenStage = playerDashboardScreenStage;
        }

        public BorderPane startPlayerDashboardScreen(PlayerMainScreen mainScreen) {
                this.mainScreen = mainScreen;
                BorderPane root = new BorderPane();
                root.setStyle("-fx-background-color: " + GamerVaultStyles.BASE_BG + ";");
                root.setCenter(createDashboardContent());
                loadDashboardData();
                return root;
        }

        private ScrollPane createDashboardContent() {
                mainContainer = new VBox(20);
                mainContainer.setPadding(new Insets(20));
                mainContainer.setStyle("-fx-background-color: " + GamerVaultStyles.BASE_BG + ";");

                Text loadingText = new Text("Fetching live telemetry...");
                loadingText.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
                loadingText.setFont(Font.font("Arial", 18));
                GamerVaultAnimations.pulseGlow(loadingText, 1.0);

                loadingBox = new VBox(loadingText);
                loadingBox.setAlignment(Pos.CENTER);
                loadingBox.setPrefHeight(400);

                heroContainer = new VBox();
                row1 = new HBox(20);
                row2 = new HBox(20);

                mainContainer.getChildren().add(loadingBox);

                ScrollPane scroller = new ScrollPane();
                scroller.setContent(mainContainer);
                GamerVaultStyles.applyStyledScrollPane(scroller);
                scroller.setFitToHeight(true);
                scroller.setVbarPolicy(ScrollBarPolicy.NEVER);
                scroller.setHbarPolicy(ScrollBarPolicy.NEVER);

                return scroller;
        }

        private void loadDashboardData() {
                String userId = "TEST_USER_123";
                if (AuthController.currentUser != null) {
                        userId = AuthController.currentUser.getUserId();
                }
                final String finalUserId = userId;

                new Thread(() -> {
                        try {
                                List<MatchModel> matches = MatchController.getUserMatchHistory(finalUserId);
                                List<UserModel> leaderboard = LeaderboardController.getLeaderboardUsers();
                                List<ContentModel> content = ContentController.loadContent();
                                List<TournamentModel> tourneys = TournamentController.loadActiveTournaments();
                                List<RecruitmentModel> recruits = RecruitmentController.fetchActiveMarketPostings();

                                Platform.runLater(() -> {
                                        mainContainer.getChildren().clear();
                                        mainContainer.getChildren().addAll(heroContainer, row1, row2,
                                                        SizedBox.height(30));

                                        heroContainer.getChildren().clear();
                                        heroContainer.getChildren().add(PlayerDashboardHeroSection
                                                        .createHeroSection(AuthController.currentUser, mainScreen));

                                        MatchModel lastMatch = (matches != null && !matches.isEmpty()) ? matches.get(0)
                                                        : null;
                                        int totalMatches = matches != null ? matches.size() : 0;
                                        double totalKills = 0, totalDamage = 0, wins = 0;

                                        if (matches != null) {
                                                for (MatchModel m : matches) {
                                                        totalKills += m.getKills();
                                                        totalDamage += m.getDamage();
                                                        if (m.getTeamPlacement() == 1)
                                                                wins++;
                                                }
                                        }

                                        double avgKills = totalMatches > 0 ? totalKills / totalMatches : 0;
                                        double avgDamage = totalMatches > 0 ? totalDamage / totalMatches : 0;
                                        double winRate = totalMatches > 0 ? (wins / totalMatches) * 100 : 0;

                                        String lmMap = lastMatch != null ? lastMatch.getMap() : "No Matches";
                                        String mapAssetTarget = lastMatch != null
                                                        ? lastMatch.getMap().toLowerCase().trim()
                                                        : "erangel";
                                        String dynamicMapBg = "/assets/bgmi_images/" + mapAssetTarget + "_bg.jpg";

                                        String lmKills = lastMatch != null ? String.valueOf(lastMatch.getKills()) : "0";
                                        String lmDmg = lastMatch != null ? String.valueOf(lastMatch.getDamage()) : "0";
                                        String lmTime = lastMatch != null ? lastMatch.getSurvivalTime() : "00:00";
                                        String lmPlace = lastMatch != null ? "#" + lastMatch.getTeamPlacement() : "--";

                                        StackPane lastMatchCard = FeatureCardsMethods.createDashboardStatCard(
                                                        "Last Match", "➜", lmMap, "Latest Deployment", dynamicMapBg,
                                                        Arrays.asList("🔫", lmKills, "Kills"),
                                                        Arrays.asList("💥", lmDmg, "Damage"),
                                                        Arrays.asList("⏱", lmTime, "Survived"),
                                                        Arrays.asList("🏆", lmPlace, "Placement"));

                                        StackPane perfCard = FeatureCardsMethods.createDashboardStatCard(
                                                        "Performance", "📈", String.format("%.1f%%", winRate),
                                                        "Current Win Rate", "/assets/logoImage/performance.png",
                                                        Arrays.asList("🎯", String.format("%.1f", avgKills),
                                                                        "Avg Kills"),
                                                        Arrays.asList("💥", String.format("%.0f", avgDamage),
                                                                        "Avg Dmg"),
                                                        Arrays.asList("🏆", String.format("%.1f%%", winRate),
                                                                        "Win Rate"),
                                                        Arrays.asList("🎮", String.valueOf(totalMatches), "Matches"));

                                        int rankIndex = -1;
                                        if (leaderboard != null) {
                                                leaderboard.sort((p1, p2) -> Integer.compare(p2.getTotalKills(),
                                                                p1.getTotalKills()));
                                                for (int i = 0; i < leaderboard.size(); i++) {
                                                        if (leaderboard.get(i).getUserId().equals(finalUserId)) {
                                                                rankIndex = i;
                                                                break;
                                                        }
                                                }
                                        }
                                        String myRank = rankIndex != -1 ? "#" + (rankIndex + 1) : "Unranked";
                                        String topPercent = rankIndex != -1
                                                        ? "Top " + Math.max(1,
                                                                        (int) (((double) (rankIndex + 1)
                                                                                        / leaderboard.size()) * 100))
                                                                        + "%"
                                                        : "--";

                                        StackPane leaderCard = FeatureCardsMethods.createDashboardStatCard(
                                                        "Leaderboard", "🏆", myRank, "Current Position",
                                                        "/assets/logoImage/leaderboard.png",
                                                        Arrays.asList("⭐", topPercent, "Players"),
                                                        Arrays.asList("📈", "KILLS", "Best Rank"),
                                                        Arrays.asList("🎯", myRank, "Kill Rank"),
                                                        Arrays.asList("💥", "--", "Damage Rank"));

                                        // THE FIX: Clear the row to prevent duplicate children exception on multiple
                                        // clicks
                                        row1.getChildren().clear();
                                        row1.getChildren().addAll(lastMatchCard, perfCard, leaderCard);
                                        GamerVaultAnimations.staggerFadeInChildren(row1, 120);

                                        List<String> emptyItem = Arrays.asList("⏳", "Check back later", "--",
                                                        "More updates soon.");

                                        String defaultContentImg = "/assets/images/content_thumb.jpg";
                                        String defaultTourneyImg = "/assets/images/tourney_thumb.jpg";

                                        String c1Img = (content != null && content.size() > 0
                                                        && content.get(0).getThumbnailUrl() != null
                                                        && !content.get(0).getThumbnailUrl().isEmpty())
                                                                        ? content.get(0).getThumbnailUrl()
                                                                        : defaultContentImg;
                                        String c2Img = (content != null && content.size() > 1
                                                        && content.get(1).getThumbnailUrl() != null
                                                        && !content.get(1).getThumbnailUrl().isEmpty())
                                                                        ? content.get(1).getThumbnailUrl()
                                                                        : defaultContentImg;

                                        List<String> c1 = content != null && content.size() > 0 ? Arrays.asList(c1Img,
                                                        content.get(0).getTitle(), "Recent", "From Admins") : emptyItem;
                                        List<String> c2 = content != null && content.size() > 1 ? Arrays.asList(c2Img,
                                                        content.get(1).getTitle(), "Recent", "From Admins") : emptyItem;
                                        StackPane contentCard = FeatureCardsMethods.createDashboardContentCard(
                                                        "Trending Content", "🎥", c1, c2, "#7c3ced", () -> {
                                                                PlayerDashboardSidebar.pageView = "discover_content";
                                                                if (mainScreen != null)
                                                                        mainScreen.updateCenter();
                                                        });

                                        String t1Img = (tourneys != null && tourneys.size() > 0
                                                        && tourneys.get(0).getImageUrls() != null
                                                        && !tourneys.get(0).getImageUrls().isEmpty())
                                                                        ? tourneys.get(0).getImageUrls().get(0)
                                                                        : defaultTourneyImg;
                                        String t2Img = (tourneys != null && tourneys.size() > 1
                                                        && tourneys.get(1).getImageUrls() != null
                                                        && !tourneys.get(1).getImageUrls().isEmpty())
                                                                        ? tourneys.get(1).getImageUrls().get(0)
                                                                        : defaultTourneyImg;

                                        List<String> t1 = tourneys != null && tourneys.size() > 0 ? Arrays.asList(t1Img,
                                                        tourneys.get(0).getTitle(), tourneys.get(0).getStatus(),
                                                        tourneys.get(0).getPrizePool()) : emptyItem;
                                        List<String> t2 = tourneys != null && tourneys.size() > 1 ? Arrays.asList(t2Img,
                                                        tourneys.get(1).getTitle(), tourneys.get(1).getStatus(),
                                                        tourneys.get(1).getPrizePool()) : emptyItem;
                                        StackPane tourneyCard = FeatureCardsMethods.createDashboardContentCard(
                                                        "Active Tourneys", "🏆", t1, t2, "#f59e0b", () -> {
                                                                PlayerDashboardSidebar.pageView = "discover_content";
                                                                if (mainScreen != null)
                                                                        mainScreen.updateCenter();
                                                        });

                                        List<String> r1 = recruits != null && recruits.size() > 0 ? Arrays.asList("⚔",
                                                        recruits.get(0).getTeamName(), recruits.get(0).getRole(),
                                                        recruits.get(0).getRegion()) : emptyItem;
                                        List<String> r2 = recruits != null && recruits.size() > 1 ? Arrays.asList("🔥",
                                                        recruits.get(1).getTeamName(), recruits.get(1).getRole(),
                                                        recruits.get(1).getRegion()) : emptyItem;
                                        StackPane recruitCard = FeatureCardsMethods.createDashboardContentCard(
                                                        "Recruitment Hub", "🤝", r1, r2, "#10b981", () -> {
                                                                PlayerDashboardSidebar.pageView = "recruitmentHub";
                                                                if (mainScreen != null)
                                                                        mainScreen.updateCenter();
                                                        });

                                        // THE FIX: Clear the row to prevent duplicate children exception on multiple
                                        // clicks
                                        row2.getChildren().clear();
                                        row2.getChildren().addAll(contentCard, tourneyCard, recruitCard);
                                        GamerVaultAnimations.staggerFadeInChildren(row2, 120);

                                });
                        } catch (Exception e) {
                                e.printStackTrace();
                        }
                }).start();
        }
}