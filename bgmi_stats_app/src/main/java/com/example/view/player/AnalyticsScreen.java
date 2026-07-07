package com.example.view.player;

import java.util.List;

import com.example.controller.AuthController;
import com.example.controller.player.LeaderboardController;
import com.example.controller.player.MatchController;
import com.example.model.player.MatchModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AnalyticsScreen {

    // VARIABLES
    Scene analyticsScreenScene;
    Stage analyticsScreenStage;

    private FlowPane statsGrid;

    // METHODS
    public void setAnalyticsScreenScene(Scene analyticsScreenScene) {
        this.analyticsScreenScene = analyticsScreenScene;
    }

    public void setAnalyticsScreenStage(Stage analyticsScreenStage) {
        this.analyticsScreenStage = analyticsScreenStage;
    }

    /*
     * This is the start of the Analytics screen.
     * This method begins the UI of the screen.
     */
    public BorderPane startAnalyticsScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        ScrollPane scroller = new ScrollPane();
        scroller.setFitToWidth(true);
        scroller.setContent(createAnalyticsContent());
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setCenter(scroller);

        return root;
    }

    /*
     * This method creates all the UI of the Analytics Screen widgets.
     */
    private VBox createAnalyticsContent() {
        VBox container = new VBox(30);
        container.setPadding(new Insets(10, 20, 40, 20));

        VBox headerBox = createHeaderBox();

        statsGrid = new FlowPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);

        statsGrid.getChildren().add(createStatCard("LOADING METRICS...", "⏳", "--"));

        loadAndCalculateAnalytics();

        GamerVaultAnimations.staggerFadeInChildren(statsGrid, 100);

        container.getChildren().addAll(headerBox, statsGrid);

        return container;
    }

    /*
     * HEADER UI - HELPER METHOD
     */
    private VBox createHeaderBox() {
        Text title = new Text("Analytics");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 42));
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

        Text subtitle = new Text("Track your BGMI performance and growth.");
        subtitle.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        subtitle.setFont(Font.font("Poppins", FontWeight.NORMAL, 16));

        VBox headerBox = new VBox(5);
        headerBox.getChildren().addAll(title, subtitle);

        GamerVaultAnimations.fadeInUp(title, 0, 500);
        GamerVaultAnimations.fadeInUp(subtitle, 150, 500);

        return headerBox;
    }

    /*
     * STAT CARD - HELPER METHOD
     * Glassmorphism data card with icon glow on hover and scale animation.
     */
    private VBox createStatCard(String titleStr, String iconStr, String valueStr) {
        VBox card = new VBox(15);
        card.setPrefSize(230, 130);
        card.setPadding(new Insets(20));

        // Glassmorphism + hover scale
        GamerVaultStyles.applyGlassCard(card);
        GamerVaultAnimations.scaleOnHover(card, 1.04);

        // Top Row: Title & Icon
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text(titleStr);
        title.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Text icon = new Text(iconStr);
        icon.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        icon.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        topRow.getChildren().addAll(title, spacer, icon);

        // Main Value
        Text value = new Text(valueStr);
        value.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        value.setFont(Font.font("Arial", FontWeight.BOLD, 38));

        // Assemble Card
        card.getChildren().addAll(topRow, value);
        return card;
    }

    private void loadAndCalculateAnalytics() {
        String userId = "TEST_USER_123";
        if (AuthController.currentUser != null && AuthController.currentUser.getUserId() != null) {
            userId = AuthController.currentUser.getUserId();
        }
        final String finalUserId = userId;

        new Thread(() -> {
            try {
                // Pull raw array history lists directly from Firestore through our Controller
                // layer
                List<MatchModel> matches = MatchController.getUserMatchHistory(finalUserId);

                Platform.runLater(() -> {
                    statsGrid.getChildren().clear();

                    if (matches == null || matches.isEmpty()) {
                        statsGrid.getChildren().addAll(
                                createStatCard("TOTAL MATCHES", "🎮", "0"),
                                createStatCard("TOTAL KILLS", "⚔", "0"),
                                createStatCard("TOTAL DAMAGE", "🎯", "0"),
                                createStatCard("WIN RATE", "🏆", "0.0%"),
                                createStatCard("AVG PLACEMENT", "📊", "N/A"),
                                createStatCard("AVG SURVIVAL", "⏱", "00m 00s"));
                        return;
                    }

                    // Allocation structures for calculation metrics
                    int totalMatches = matches.size();
                    int totalKills = 0;
                    double totalDamage = 0;
                    int wins = 0;
                    double combinedPlacement = 0;
                    long totalSurvivalSeconds = 0;

                    for (MatchModel match : matches) {
                        totalKills += match.getKills();
                        totalDamage += match.getDamage();
                        combinedPlacement += match.getTeamPlacement();

                        if (match.getTeamPlacement() == 1) {
                            wins++;
                        }

                        String timeStr = match.getSurvivalTime();
                        if (timeStr != null && timeStr.contains(":")) {
                            try {
                                String[] parts = timeStr.split(":");
                                int minutes = Integer.parseInt(parts[0].trim());
                                int seconds = Integer.parseInt(parts[1].trim());
                                totalSurvivalSeconds += (minutes * 60) + seconds;
                            } catch (Exception ignored) {
                            }
                        }
                    }

                    double winRate = ((double) wins / totalMatches) * 100;
                    double avgPlacement = combinedPlacement / totalMatches;
                    long avgSurvivalSeconds = totalSurvivalSeconds / totalMatches;
                    long avgMinutes = avgSurvivalSeconds / 60;
                    long avgSeconds = avgSurvivalSeconds % 60;

                    LeaderboardController.syncLeaderboardStats(finalUserId, totalMatches, totalKills, totalDamage,
                            winRate, avgPlacement);

                    String damageDisplay = totalDamage >= 1_000_000
                            ? String.format("%.1fM", totalDamage / 1_000_000.0)
                            : String.format("%,.0f", totalDamage);

                    statsGrid.getChildren().addAll(
                            createStatCard("TOTAL MATCHES", "🎮", String.format("%,d", totalMatches)),
                            createStatCard("TOTAL KILLS", "⚔", String.format("%,d", totalKills)),
                            createStatCard("TOTAL DAMAGE", "🎯", damageDisplay),
                            createStatCard("WIN RATE", "🏆", String.format("%.1f%%", winRate)),
                            createStatCard("AVG PLACEMENT", "📊", String.format("#%.1f", avgPlacement)),
                            createStatCard("AVG SURVIVAL", "⏱", String.format("%02dm %02ds", avgMinutes, avgSeconds)));

                    GamerVaultAnimations.staggerFadeInChildren(statsGrid, 80);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("Failed to process dynamic analytics sequence: " + e.getMessage());
                    statsGrid.getChildren().clear();
                    Text errTxt = new Text("⚠️ Failed to load performance metrics.");
                    errTxt.setFill(Color.web("#EF4444"));
                    statsGrid.getChildren().add(errTxt);
                });
            }
        }).start();
    }
}