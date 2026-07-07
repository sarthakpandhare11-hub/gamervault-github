package com.example.view.player;

import java.util.List;

import com.example.controller.AuthController;
import com.example.controller.player.LeaderboardController;
import com.example.model.UserModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LeaderboardScreen {

    // VARIABLES
    Scene leaderboardScreenScene;
    Stage leaderboardScreenStage;

    private FlowPane statsGrid;
    private VBox tableContainer;

    // METHODS
    public void setLeaderboardScreenScene(Scene leaderboardScreenScene) {
        this.leaderboardScreenScene = leaderboardScreenScene;
    }

    public void setLeaderboardScreenStage(Stage leaderboardScreenStage) {
        this.leaderboardScreenStage = leaderboardScreenStage;
    }

    /*
     * This is the start of the LeaderboardScreen.
     * This method begins the UI of the screen.
     */
    public BorderPane startLeaderboardScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        ScrollPane scroller = new ScrollPane();
        scroller.setFitToWidth(true);
        scroller.setContent(createLeaderboardContent());
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setCenter(scroller);

        return root;
    }

    /*
     * This method creates the content of the leaderboard screen.
     * It is called by the startLeaderboardScreen() method.
     * This screen has all the sub-calls for all the UI methods.
     */
    private VBox createLeaderboardContent() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(10, 20, 40, 20)); // Top, Right, Bottom, Left

        // 1. PAGE TITLE & SUBTITLE
        VBox headerBox = createHeaderBox();

        // 2. STATS CARDS ROW
        statsGrid = new FlowPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);
        statsGrid.getChildren().addAll(
                createStatCard("Total Players", "👥", "Loading..."),
                createStatCard("Your Current Rank", "🏆", "--"),
                createStatCard("Best Category", "🎯", "--"),
                createStatCard("Top Percentage", "⏱", "--"));

        // Stagger animate stat cards
        GamerVaultAnimations.staggerFadeInChildren(statsGrid, 100);

        // 3. FILTERS / TABS ROW
        HBox filtersRow = createFiltersRow();

        // 4. CUSTOM TABLE SECTION
        VBox tableSection = createTableSection();

        // Fade in table
        GamerVaultAnimations.fadeInUp(tableSection, 400, 500);

        loadLeaderboardData("KILLS");

        container.getChildren().addAll(headerBox, statsGrid, filtersRow, tableSection);

        return container;
    }

    /*
     * HEADER UI - HELPER METHOD
     * This method creates the header UI of the leaderboard screen.
     * 
     * Parameters-
     * titleStr: "Leaderboard"
     * subtitleStr:
     * "Compete with players based on verified BGMI performance. Rankings update in\nreal-time."
     */
    private VBox createHeaderBox() {
        Text title = new Text("Leaderboard");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 42));
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

        Text subtitle = new Text(
                "Compete with players based on verified BGMI performance. Rankings update in\nreal-time.");
        subtitle.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        subtitle.setFont(Font.font("Poppins", FontWeight.NORMAL, 16));
        subtitle.setLineSpacing(4);

        VBox headerBox = new VBox(5);
        headerBox.getChildren().addAll(title, subtitle);

        GamerVaultAnimations.fadeInUp(title, 0, 500);
        GamerVaultAnimations.fadeInUp(subtitle, 150, 500);

        return headerBox;
    }

    /*
     * MAIN STATS BOX UI - HELPER METHOD
     * This method helps in creating the stat boxes of UI in leaderboard screen.
     * It creates the boxes for Total Players, Your Current Rank, Best Category,
     * Top Percentage
     * 
     * Parameters-
     * titleStr: TOTAL PLAYERS
     * iconStr: Hardcoded icon for each box
     * valueStr: Values ofthe stat, from database
     */
    private VBox createStatCard(
            String titleStr,
            String iconStr,
            String valueStr) {
        VBox card = new VBox(12);
        card.setPrefSize(230, 130);
        card.setPadding(new Insets(15, 20, 15, 20));

        // Glassmorphism + hover scale
        GamerVaultStyles.applyGlassCard(card);
        GamerVaultAnimations.scaleOnHover(card, 1.04);

        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Text icon = new Text(iconStr);
        icon.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        icon.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        Text title = new Text(titleStr);
        title.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        topRow.getChildren().addAll(icon, title);

        Text value = new Text(valueStr);
        value.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        value.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        VBox contentBox = new VBox(2);
        contentBox.getChildren().add(value);

        // Decorative bottom line
        Region line = new Region();
        line.setPrefHeight(2);
        line.setPrefWidth(100);
        line.setStyle("-fx-background-color: rgba(139,92,246,0.3);");
        VBox.setMargin(line, new Insets(10, 0, 0, 0));
        contentBox.getChildren().add(line);

        card.getChildren().addAll(topRow, contentBox);
        return card;
    }

    /*
     * This is the method which creates the filter and searchBox row.
     * 
     * This method help in arranging the all the button which help in filtering
     * different leaderboard.
     * This method arranges them in horizontal proper manner.
     */
    private HBox createFiltersRow() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        // Buttons
        HBox tabs = new HBox(10);

        Button killRankButton = new Button("⚔ Kills Ranking");
        Button damageRankButton = new Button("🎯 Damage Ranking");
        Button winRateRankButton = new Button("🏆 Win Rate Ranking");
        Button placementRankButton = new Button("📊 Placement Ranking");

        Button[] allTabs = { killRankButton, damageRankButton, winRateRankButton, placementRankButton };

        // Button on Click Styling and Logic.
        killRankButton.setOnAction(e -> {
            selectTab(killRankButton, allTabs);
            loadLeaderboardData("KILLS");
        });
        damageRankButton.setOnAction(e -> {
            selectTab(damageRankButton, allTabs);
            loadLeaderboardData("DAMAGE");
        });
        winRateRankButton.setOnAction(e -> {
            selectTab(winRateRankButton, allTabs);
            loadLeaderboardData("WIN_RATE");
        });
        placementRankButton.setOnAction(e -> {
            selectTab(placementRankButton, allTabs);
            loadLeaderboardData("PLACEMENT");
        });

        selectTab(killRankButton, allTabs);

        tabs.getChildren().addAll(killRankButton, damageRankButton, winRateRankButton, placementRankButton);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Search Field with focus glow
        TextField searchField = new TextField();
        searchField.setPromptText("🔍  Find Player...");
        HBox searchBox = GamerVaultStyles.createStyledInput(searchField, GamerVaultStyles.ACCENT_PURPLE);
        searchBox.setPrefWidth(250);
        searchBox.setPrefHeight(40);

        row.getChildren().addAll(tabs, spacer, searchBox);
        return row;
    }

    /*
     * This is the method which is a helper method which will provide
     * style to the buttons.
     * Only button styles are given through this method.
     * 
     * Parameter:
     * isActive: This parameter tells which button is pressed currently and needs to
     * be given different style in UI.
     */
    private Button getTabButtonStyle(Button btn, boolean isActive) {
        btn.setPrefHeight(38);
        if (isActive) {
            // Gradient active tab
            btn.setStyle(
                    "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, " + GamerVaultStyles.ACCENT_PURPLE
                            + ", " + GamerVaultStyles.ACCENT_PURPLE_DARK + "); "
                            + "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; "
                            + "-fx-background-radius: 8; -fx-cursor: hand;");
        } else {
            // Ghost inactive tab
            btn.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.04); "
                            + "-fx-text-fill: " + GamerVaultStyles.TEXT_SECONDARY
                            + "; -fx-font-weight: bold; -fx-font-size: 13px; "
                            + "-fx-background-radius: 8; -fx-cursor: hand; "
                            + "-fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 8;");
        }
        return btn;
    }

    /*
     * This method tells which button was clicked to make sure that button is
     * highlighted.
     * 
     * From the row of buttons of Kills, Damage, Win, Placement
     * This button helps in identifying which button is clicked.
     */
    private void selectTab(Button clickedBtn, Button[] allTabs) {
        for (Button btn : allTabs) {
            getTabButtonStyle(btn, btn == clickedBtn);
        }
    }

    /*
     * This is the table section of the leaderboard
     * This method arranges the UI of the entire leaderboard.
     * This is where the main table with the player stats will be displayed.
     */
    private VBox createTableSection() {
        VBox table = new VBox();
        // Glassmorphism table container
        table.setStyle(
                "-fx-background-color: " + GamerVaultStyles.CARD_BG + "; "
                        + "-fx-background-radius: 16; -fx-border-color: " + GamerVaultStyles.CARD_BORDER + "; "
                        + "-fx-border-radius: 16;");

        // 1. Table Header
        HBox header = createTableRow("RANK", "PLAYER", "MATCHES", "KILLS", "DAMAGE", "WIN RATE", "AVG PLACE", false,
                true);
        header.setStyle(
                "-fx-border-color: transparent transparent rgba(255,255,255,0.06) transparent; -fx-border-width: 0 0 1 0; -fx-padding: 15 20 15 20;");
        table.getChildren().add(header);

        tableContainer = new VBox();

        tableContainer.getChildren()
                .add(createTableRow("--", "Loading leaderboard...", "--", "--", "--", "--", "--", false, false));

        table.getChildren().add(tableContainer);

        return table;
    }

    /*
     * This is the method used for the creation of the table row for a player row
     * PARAMETERS THAT THESE METHOD NEED ARE FOR -
     * rank: string for the rank of the player
     * player: string for the name of the player
     * matches: string for the number of matches played by the player
     * kills: string for the number of kills made by the player
     * damage: string for the amount of damage dealt by the player
     * winRate: string for the win rate of the player
     * isHighlighted: boolean for the highlighted row
     * isHeader: boolean for the header row
     */
    private HBox createTableRow(
            String rank,
            String player,
            String matches,
            String kills,
            String damage,
            String winRate,
            String placement,
            boolean isHighlighted,
            boolean isHeader) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        if (isHighlighted) {
            // Player own row highlighted with purple accent
            row.setStyle(
                    "-fx-background-color: rgba(139,92,246,0.12); "
                            + "-fx-border-color: transparent transparent transparent " + GamerVaultStyles.ACCENT_PURPLE
                            + "; "
                            + "-fx-border-width: 0 0 0 4; -fx-padding: 15 20 15 16;");
        } else if (!isHeader) {
            // Standard row with hover effect
            String baseRowStyle = "-fx-padding: 15 20 15 20; -fx-background-color: transparent;";
            String hoverRowStyle = "-fx-padding: 15 20 15 20; -fx-background-color: rgba(139,92,246,0.05);";
            row.setStyle(baseRowStyle);
            row.setOnMouseEntered(e -> row.setStyle(hoverRowStyle));
            row.setOnMouseExited(e -> row.setStyle(baseRowStyle));
        }

        // Font settings based on if it's a header or standard row
        Font font = isHeader ? Font.font("Arial", FontWeight.BOLD, 10) : Font.font("Arial", FontWeight.BOLD, 14);
        Color textColor = isHeader ? Color.web(GamerVaultStyles.TEXT_MUTED) : Color.web(GamerVaultStyles.TEXT_PRIMARY);
        Color faintColor = isHeader ? Color.web(GamerVaultStyles.TEXT_MUTED)
                : Color.web(GamerVaultStyles.TEXT_SECONDARY);

        // 1. RANK COLUMN
        HBox rankBox = new HBox(5);
        rankBox.setAlignment(Pos.CENTER_LEFT);
        rankBox.setPrefWidth(70);

        if (rank.equals("1") || rank.equals("2") || rank.equals("3")) {
            Text medal = new Text("🏆");
            if (rank.equals("1"))
                medal.setFill(Color.web("#F59E0B")); // Gold
            if (rank.equals("2"))
                medal.setFill(Color.web("#9CA3AF")); // Silver
            if (rank.equals("3"))
                medal.setFill(Color.web("#B45309")); // Bronze
            rankBox.getChildren().add(medal);
        }

        Text rankText = new Text(rank);
        rankText.setFont(font);

        if (!isHeader) {
            if (rank.equals("1"))
                rankText.setFill(Color.web("#F59E0B"));
            else if (rank.equals("2"))
                rankText.setFill(Color.web("#9CA3AF"));
            else if (rank.equals("3"))
                rankText.setFill(Color.web("#B45309"));
            else
                rankText.setFill(faintColor);
        } else {
            rankText.setFill(textColor);
        }
        rankBox.getChildren().add(rankText);

        // 2. PLAYER NAME COLUMN
        HBox playerBox = new HBox(12);
        playerBox.setAlignment(Pos.CENTER_LEFT);

        if (!isHeader) {
            StackPane avatarBox = new StackPane();
            avatarBox.setPrefSize(30, 30);
            avatarBox.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 6; "
                    + "-fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 6;");

            Text imgIcon = new Text("👤");
            imgIcon.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE));
            avatarBox.getChildren().add(imgIcon);
            playerBox.getChildren().add(avatarBox);

            Text nameText = new Text(player);
            nameText.setFont(font);
            nameText.setFill(isHighlighted ? Color.WHITE : textColor);
            playerBox.getChildren().add(nameText);

        } else {
            Text nameHeader = new Text(player);
            nameHeader.setFont(font);
            nameHeader.setFill(textColor);
            playerBox.getChildren().add(nameHeader);
        }

        HBox.setHgrow(playerBox, Priority.ALWAYS);

        // 3-6. DATA COLUMNS
        HBox matchesBox = createCell(matches, font, faintColor, 100, Pos.CENTER);
        HBox killsBox = createCell(kills, font, textColor, 100, Pos.CENTER);
        HBox damageBox = createCell(damage, font, faintColor, 100, Pos.CENTER);

        HBox placeBox = createCell(placement, font, isHeader ? textColor : Color.web("#3B82F6"), 90, Pos.CENTER);

        HBox winRateBox = new HBox(3);
        winRateBox.setAlignment(Pos.CENTER_RIGHT);
        winRateBox.setPrefWidth(100);
        Text winText = new Text(winRate);
        winText.setFont(font);
        winText.setFill(faintColor);
        winRateBox.getChildren().add(winText);

        if (!isHeader) {
            StackPane dot = new StackPane();
            dot.setPrefSize(4, 4);
            dot.setStyle("-fx-background-color: " + GamerVaultStyles.ACCENT_GREEN + "; -fx-background-radius: 4;");
            winRateBox.getChildren().add(dot);
        }

        row.getChildren().addAll(rankBox, playerBox, matchesBox, killsBox, damageBox, placeBox, winRateBox);
        return row;
    }

    /*
     * This method is used for the creation of the table cells.
     */
    private HBox createCell(String text, Font font, Color color, double width, Pos alignment) {
        HBox box = new HBox();
        box.setAlignment(alignment);
        box.setPrefWidth(width);
        Text t = new Text(text);
        t.setFont(font);
        t.setFill(color);
        box.getChildren().add(t);
        return box;
    }

    private void loadLeaderboardData(String sortBy) {
        // Safe check for current user
        String currentUserId = "TEST_USER_123";
        if (AuthController.currentUser != null && AuthController.currentUser.getUserId() != null) {
            currentUserId = AuthController.currentUser.getUserId();
        }
        final String finalUserId = currentUserId;

        new Thread(() -> {
            try {

                List<UserModel> players = LeaderboardController.getLeaderboardUsers();

                Platform.runLater(() -> {
                    tableContainer.getChildren().clear();

                    if (players == null || players.isEmpty()) {
                        tableContainer.getChildren()
                                .add(createTableRow("-", "No players found.", "-", "-", "-", "-", "-", false, false));
                        return;
                    }

                    players.sort((p1, p2) -> {
                        switch (sortBy) {
                            case "DAMAGE":
                                return Double.compare(p2.getTotalDamage(), p1.getTotalDamage());
                            case "WIN_RATE":
                                return Double.compare(p2.getWinRate(), p1.getWinRate());
                            case "PLACEMENT":
                                // Lower is better! If 0, push them to the bottom.
                                double place1 = p1.getAveragePlacement() > 0 ? p1.getAveragePlacement() : 999;
                                double place2 = p2.getAveragePlacement() > 0 ? p2.getAveragePlacement() : 999;
                                return Double.compare(place1, place2);
                            case "KILLS":
                            default:
                                return Integer.compare(p2.getTotalKills(), p1.getTotalKills());
                        }
                    });

                    int totalPlayers = players.size();
                    int userRankIndex = -1;

                    for (int i = 0; i < players.size(); i++) {
                        if (players.get(i).getUserId().equals(finalUserId)) {
                            userRankIndex = i;
                            break;
                        }
                    }

                    String rankDisplay = userRankIndex != -1 ? "#" + (userRankIndex + 1) : "Unranked";
                    String topPercent = userRankIndex != -1
                            ? "Top " + Math.max(1, (int) (((double) (userRankIndex + 1) / totalPlayers) * 100)) + "%"
                            : "--";
                    String categoryStr = sortBy.substring(0, 1).toUpperCase()
                            + sortBy.substring(1).toLowerCase().replace("_", " ");

                    statsGrid.getChildren().clear();
                    statsGrid.getChildren().addAll(
                            createStatCard("Total Players", "👥", String.format("%,d", totalPlayers)),
                            createStatCard("Your " + categoryStr + " Rank", "🏆", rankDisplay),
                            createStatCard("Sorted By", "🎯", categoryStr),
                            createStatCard("Top Percentage", "⏱", topPercent));

                    for (int i = 0; i < players.size(); i++) {
                        UserModel player = players.get(i);
                        boolean isCurrentUser = player.getUserId() != null && player.getUserId().equals(finalUserId);

                        String rank = String.valueOf(i + 1);

                        // ALIGNED: Use your exact model getter getIgn() instead of getUsername()
                        String name = player.getIgn() != null && !player.getIgn().isEmpty() ? player.getIgn()
                                : "Unknown Player";
                        if (isCurrentUser)
                            name += " (You)";

                        // ALIGNED: Extracting matching exact primitives from your UserModel
                        String matches = String.valueOf(player.getTotalMatches());
                        String kills = String.format("%,d", player.getTotalKills());

                        double dmg = player.getTotalDamage();
                        String damage = dmg >= 1_000_000 ? String.format("%.1fM", dmg / 1_000_000.0)
                                : String.format("%,.0f", dmg);

                        // ALIGNED: Uses your exact getter getWinRate() from UserModel directly
                        String winRate = String.format("%.1f%%", player.getWinRate());
                        String avgPlace = player.getAveragePlacement() > 0
                                ? String.format("#%.1f", player.getAveragePlacement())
                                : "--";

                        // Passes variables right into your layout generator
                        HBox row = createTableRow(rank, name, matches, kills, damage, winRate, avgPlace, isCurrentUser,
                                false);
                        tableContainer.getChildren().add(row);

                        // Stagger animate rows
                        GamerVaultAnimations.fadeInUp(row, i * 30, 400);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    tableContainer.getChildren().clear();
                    tableContainer.getChildren()
                            .add(createTableRow("!", "Error loading data.", "-", "-", "-", "-", "-", false, false));
                });
            }
        }).start();
    }

}