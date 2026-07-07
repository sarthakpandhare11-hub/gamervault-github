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
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PlayerDashboardScreen {

    // VARIABLES

    // Variables for player's dashboard screen
    Scene playerDashboardScreenScene;
    Stage playerDashboardScreenStage;

    private PlayerMainScreen mainScreen;

    // UI VARIABLES
    private VBox heroContainer;
    private HBox row1;
    private HBox row2;

    // METHODS

    // Setters for the scene and stage for the player's dashboard screen
    public void setPlayerDashboardScreenScene(Scene playerDashboardScreenScene) {
        this.playerDashboardScreenScene = playerDashboardScreenScene;
    }

    public void setPlayerDashboardScreenStage(Stage playerDashboardScreenStage) {
        this.playerDashboardScreenStage = playerDashboardScreenStage;
    }

    // Start method for the Player Dashboard screen
    public BorderPane startPlayerDashboardScreen(PlayerMainScreen mainScreen) {
        this.mainScreen = mainScreen;

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + GamerVaultStyles.BASE_BG + ";");

        root.setCenter(createDashboardContent());

        loadDashboardData();

        return root;
    }

    /*
     * This method handles the UI management of the 1st button used in sidebar -
     * Dashboard
     * Complete UI of the quick navigation and information is what this card
     * contains.
     * 
     * This Dashboard contains -
     * 1) Top bar with search, notification, settings, and profile icon
     * 2) Hero section
     * 3) Last match card
     * 4) Performance card
     * 5) Leaderboard card
     * 6) Trending content card
     * 7) Tournament card
     * 8) Recruitment card
     */
    private ScrollPane createDashboardContent() {

        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: " + GamerVaultStyles.BASE_BG + ";");

        heroContainer = new VBox();
        row1 = new HBox(20);
        row2 = new HBox(20);
        container.getChildren().addAll(heroContainer, row1, row2, SizedBox.height(30));

        ScrollPane scroller = new ScrollPane();
        scroller.setContent(container);
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setFitToHeight(true);
        scroller.setVbarPolicy(ScrollBarPolicy.NEVER);

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
                // Fetching all the data neededhere
                List<MatchModel> matches = MatchController.getUserMatchHistory(finalUserId);
                List<UserModel> leaderboard = LeaderboardController.getLeaderboardUsers();
                List<ContentModel> content = ContentController.loadContent();
                List<TournamentModel> tourneys = TournamentController.loadActiveTournaments();
                List<RecruitmentModel> recruits = RecruitmentController.fetchActiveMarketPostings();

                Platform.runLater(() -> {
                    // HERO SECTION
                    heroContainer.getChildren().clear();
                    heroContainer.getChildren()
                            .add(PlayerDashboardHeroSection.createHeroSection(AuthController.currentUser, mainScreen));
                    MatchModel lastMatch = (matches != null && !matches.isEmpty()) ? matches.get(0) : null;
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
                    String lmKills = lastMatch != null ? String.valueOf(lastMatch.getKills()) : "0";
                    String lmDmg = lastMatch != null ? String.valueOf(lastMatch.getDamage()) : "0";
                    String lmTime = lastMatch != null ? lastMatch.getSurvivalTime() : "00:00";
                    String lmPlace = lastMatch != null ? "#" + lastMatch.getTeamPlacement() : "--";

                    VBox lastMatchCard = FeatureCardsMethods.createDashboardStatCard(
                            "Last Match", "➜", lmMap, "Latest Deployment",
                            Arrays.asList("🔫", lmKills, "Kills"),
                            Arrays.asList("💥", lmDmg, "Damage"),
                            Arrays.asList("⏱", lmTime, "Survived"),
                            Arrays.asList("🏆", lmPlace, "Placement"));

                    VBox perfCard = FeatureCardsMethods.createDashboardStatCard(
                            "Performance", "📈", String.format("%.1f%%", winRate), "Current Win Rate",
                            Arrays.asList("🎯", String.format("%.1f", avgKills), "Avg Kills"),
                            Arrays.asList("💥", String.format("%.0f", avgDamage), "Avg Dmg"),
                            Arrays.asList("🏆", String.format("%.1f%%", winRate), "Win Rate"),
                            Arrays.asList("🎮", String.valueOf(totalMatches), "Matches"));

                    int rankIndex = -1;
                    if (leaderboard != null) {
                        leaderboard.sort((p1, p2) -> Integer.compare(p2.getTotalKills(), p1.getTotalKills()));
                        for (int i = 0; i < leaderboard.size(); i++) {
                            if (leaderboard.get(i).getUserId().equals(finalUserId)) {
                                rankIndex = i;
                                break;
                            }
                        }
                    }
                    String myRank = rankIndex != -1 ? "#" + (rankIndex + 1) : "Unranked";
                    String topPercent = rankIndex != -1
                            ? "Top " + Math.max(1, (int) (((double) (rankIndex + 1) / leaderboard.size()) * 100)) + "%"
                            : "--";

                    VBox leaderCard = FeatureCardsMethods.createDashboardStatCard(
                            "Leaderboard", "🏆", myRank, "Current Position",
                            Arrays.asList("⭐", topPercent, "Players"),
                            Arrays.asList("📈", "KILLS", "Best Rank"),
                            Arrays.asList("🎯", myRank, "Kill Rank"),
                            Arrays.asList("💥", "--", "Damage Rank"));

                    row1.getChildren().addAll(lastMatchCard, perfCard, leaderCard);
                    GamerVaultAnimations.staggerFadeInChildren(row1, 120);

                    List<String> emptyItem = Arrays.asList("⏳", "Check back later", "--", "More updates soon.");

                    List<String> c1 = content != null && content.size() > 0
                            ? Arrays.asList("YouTube".equals(content.get(0).getPlatform()) ? "▶" : "📷",
                                    content.get(0).getTitle(), "Recent", "From Admins")
                            : emptyItem;
                    List<String> c2 = content != null && content.size() > 1
                            ? Arrays.asList("YouTube".equals(content.get(1).getPlatform()) ? "▶" : "📷",
                                    content.get(1).getTitle(), "Recent", "From Admins")
                            : emptyItem;

                    VBox contentCard = FeatureCardsMethods.createDashboardContentCard("Trending Content", "🎥", c1, c2,
                            "#7c3ced", () -> {
                                PlayerDashboardSidebar.pageView = "discover_content";
                                if (mainScreen != null)
                                    mainScreen.updateCenter();
                            });

                    List<String> t1 = tourneys != null && tourneys.size() > 0
                            ? Arrays.asList("🎮", tourneys.get(0).getTitle(), tourneys.get(0).getStatus(),
                                    tourneys.get(0).getPrizePool())
                            : emptyItem;
                    List<String> t2 = tourneys != null && tourneys.size() > 1
                            ? Arrays.asList("🎮", tourneys.get(1).getTitle(), tourneys.get(1).getStatus(),
                                    tourneys.get(1).getPrizePool())
                            : emptyItem;

                    VBox tourneyCard = FeatureCardsMethods.createDashboardContentCard("Active Tourneys", "🏆", t1, t2,
                            "#f59e0b", () -> {
                                PlayerDashboardSidebar.pageView = "discover_content";
                                if (mainScreen != null)
                                    mainScreen.updateCenter();
                            });

                    List<String> r1 = recruits != null && recruits.size() > 0
                            ? Arrays.asList("⚔", recruits.get(0).getTeamName(), recruits.get(0).getRole(),
                                    recruits.get(0).getRegion())
                            : emptyItem;
                    List<String> r2 = recruits != null && recruits.size() > 1
                            ? Arrays.asList("🔥", recruits.get(1).getTeamName(), recruits.get(1).getRole(),
                                    recruits.get(1).getRegion())
                            : emptyItem;

                    VBox recruitCard = FeatureCardsMethods.createDashboardContentCard("Recruitment Hub", "🤝", r1, r2,
                            "#10b981", () -> {
                                PlayerDashboardSidebar.pageView = "recruitmentHub";
                                if (mainScreen != null)
                                    mainScreen.updateCenter();
                            });

                    row2.getChildren().addAll(contentCard, tourneyCard, recruitCard);
                    GamerVaultAnimations.staggerFadeInChildren(row2, 120);

                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}