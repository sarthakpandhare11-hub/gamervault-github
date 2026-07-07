package com.example.view.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.controller.AuthController;
import com.example.controller.player.MatchController;
import com.example.model.player.MatchExtractionResultModel;
import com.example.model.player.MatchModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MatchHistoryScreen {

    // VARIABLES

    Scene matchHistoryScreenScene;
    Stage matchHistoryScreenStage;

    // UI WIDGETS
    private FlowPane matchGrid;
    private HBox statsRow;
    private TextField searchField;
    private ComboBox<String> mapFilter;
    private BorderPane parentContainer;

    // Documents from firestore will have all of the matches collected.
    private List<MatchModel> matchHistory;

    // METHODS

    // Setters for the scene and stage of the MatchHistoryScreen
    public void setMatchHistoryScreenScene(Scene matchHistoryScreenScene) {
        this.matchHistoryScreenScene = matchHistoryScreenScene;
    }

    public void setMatchHistoryScreenStage(Stage matchHistoryScreenStage) {
        this.matchHistoryScreenStage = matchHistoryScreenStage;
    }

    public void setParentContainer(BorderPane parentContainer) {
        this.parentContainer = parentContainer;
    }

    // Start of the MatchHistoryScreen. This method begin the UI of this screen.
    public BorderPane startMatchHistoryScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        this.parentContainer = root;

        // Use a ScrollPane in case the screen height is small
        ScrollPane scroller = new ScrollPane();
        scroller.setContent(createMatchHistoryContent());
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setCenter(scroller);

        return root;
    }

    /*
     * This method creates and sets all the UI of the Match History widgets in
     * order. This screen has all the sub-calls for the UI methods to make complete
     * screen.
     */
    private VBox createMatchHistoryContent() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(10, 20, 40, 20)); // Top, Right, Bottom, Left

        VBox headerBox = createHeaderBox();

        statsRow = new HBox(20);
        statsRow.getChildren().add(createStatCard("Loading...", "--"));

        searchField = new TextField();
        searchField.setPromptText("🔍  Search map, placement, or date...");
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            filterMatches();
        });

        HBox searchBox = new HBox();
        searchBox.getChildren().add(GamerVaultStyles.createStyledInput(searchField, GamerVaultStyles.ACCENT_PURPLE));
        searchBox.setPrefWidth(500);
        searchBox.setPrefHeight(40);

        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);

        mapFilter = new ComboBox<>();
        mapFilter.getItems().addAll(
                "All",
                "Erangel",
                "Miramar",
                "Livik",
                "Sanhok",
                "Rondo");

        mapFilter.setValue("All");
        mapFilter.setPrefHeight(40);
        mapFilter.setPrefWidth(140);
        mapFilter.setStyle(
                "-fx-background-color: #1A1F35; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: rgba(255,255,255,0.1); " +
                        "-fx-border-radius: 8;");
        mapFilter.setOnAction(e -> {
            filterMatches();
            GamerVaultAnimations.staggerFadeInChildren(matchGrid, 100);
        });

        searchBox.getChildren().addAll(region, mapFilter, SizedBox.width(30));

        matchGrid = new FlowPane();
        matchGrid.setHgap(50);
        matchGrid.setVgap(30);

        showLoadingState();
        loadAndDisplayMatchHistory();

        // Stagger animate match cards
        GamerVaultAnimations.staggerFadeInChildren(matchGrid, 100);

        container.getChildren().addAll(headerBox, statsRow, searchBox, matchGrid, SizedBox.height(20));

        return container;
    }

    /*
     * This method takes the placement text as a parameter and returns the color of
     * the placement.
     * 
     * UI HELPER - Changes the color as per the placement secured by player.
     */
    String getPlacementColor(int placement) {
        if (placement == 1) {
            return "#F59E0B";
        } else if (placement >= 2 && placement <= 10) {
            return "#E2E8F0";
        } else {
            return "#EF4444";
        }
    }

    // Helper Method: Top Stat Cards
    private VBox createStatCard(String titleStr, String valueStr) {
        VBox card = new VBox(8);
        card.setPrefSize(250, 130);
        card.setPadding(new Insets(15, 20, 15, 20));

        // Glassmorphism + hover scale
        GamerVaultStyles.applyGlassCard(card);
        GamerVaultAnimations.scaleOnHover(card, 1.04);

        Text title = new Text(titleStr);
        title.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Text value = new Text(valueStr);
        value.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        value.setFont(Font.font("Arial", FontWeight.BOLD, 44));

        card.getChildren().addAll(title, value);
        return card;
    }

    /*
     * This method is the main method for creating one card of match stats in short.
     */
    private VBox createMatchCard(MatchModel match) {

        // Fill up the data from Firebase, match contains all of it.
        String mode = match.getGameMode() != null ? match.getGameMode().toUpperCase() : "UNKNOWN";
        String map = match.getMap() != null ? match.getMap() : "Unknown Map";
        int placement = match.getTeamPlacement();
        String placementText = "#" + placement;
        String placementColor = getPlacementColor(placement);

        String date = match.getMatchDate() != null && !match.getMatchDate().equals("N/A")
                ? match.getMatchDate()
                : "Recent";
        String kills = String.valueOf(match.getKills());
        String damage = String.valueOf(match.getDamage());
        String time = match.getSurvivalTime() != null ? match.getSurvivalTime() : "00:00";

        StackPane cardStack = new StackPane();
        cardStack.setPrefSize(320, 240);

        VBox card = new VBox(15);
        card.setPrefSize(320, 240);
        card.setPadding(new Insets(20));

        // Glassmorphism card with left accent border and hover scale
        card.setStyle(
                "-fx-background-color: transparent; "
                        + "-fx-background-radius: 14; "
                        + "-fx-border-color: " + GamerVaultStyles.CARD_BORDER + " " + GamerVaultStyles.CARD_BORDER + " "
                        + GamerVaultStyles.CARD_BORDER + " " + placementColor + "; "
                        + "-fx-border-width: 1 1 1 4; -fx-border-radius: 14;");

        ImageView mapBgImage = new ImageView();
        mapBgImage.setFitWidth(316);
        mapBgImage.setFitHeight(238);
        mapBgImage.setPreserveRatio(false);
        mapBgImage.setOpacity(0.7);

        Rectangle clipMask = new Rectangle(316, 238);
        clipMask.setArcWidth(24);
        clipMask.setArcHeight(24);
        mapBgImage.setClip(clipMask);

        try {
            String sanitizedMapName = map.toLowerCase().trim();
            String path = "/assets/bgmi_images/" + sanitizedMapName + "_bg.jpg";

            // Check file reference path matches availability securely
            if (getClass().getResource(path) != null) {
                mapBgImage.setImage(new Image(getClass().getResource(path).toExternalForm(), true));
            } else {
                // Global fallback matching asset map
                mapBgImage.setImage(new Image(
                        getClass().getResource("/assets/bgmi_images/erangel_bg.jpg").toExternalForm(), true));
            }
            System.out.println(getClass().getResource(path));
            System.out.println(sanitizedMapName);
        } catch (Exception e) {
            System.out.println(e);
        }

        DropShadow cardShadow = new DropShadow(20, Color.rgb(0, 0, 0, 0.5));
        cardShadow.setOffsetY(6);
        card.setEffect(cardShadow);
        GamerVaultAnimations.scaleOnHover(card, 1.03);

        StackPane baseGlassCanvas = new StackPane();
        baseGlassCanvas.setStyle("-fx-background-color: " + GamerVaultStyles.CARD_BG + "; -fx-background-radius: 14;");

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.TOP_LEFT);

        StackPane modeTag = new StackPane();
        modeTag.setPadding(new Insets(3, 8, 3, 8));
        modeTag.setStyle("-fx-background-color: rgba(22,78,99,0.6); -fx-background-radius: 6;");
        Text modeLabel = new Text(mode);
        modeLabel.setFill(Color.web("#67E8F9"));
        modeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        modeTag.getChildren().add(modeLabel);

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        // PLACEMENT UI SHOWCASE
        VBox placementBox = new VBox(-2);
        placementBox.setAlignment(Pos.TOP_RIGHT);
        Text pText = new Text(placementText);
        pText.setFill(Color.web(placementColor));
        pText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        placementBox.getChildren().add(pText);

        topRow.getChildren().addAll(modeTag, spacer1, placementBox);

        Text mapTitle = new Text(map);
        mapTitle.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        mapTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        VBox.setMargin(mapTitle, new Insets(-15, 0, 0, 0));

        HBox dateRow = new HBox(10);
        dateRow.setAlignment(Pos.CENTER_LEFT);
        Text dateText = new Text("📅  " + date);
        dateText.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        dateText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        dateRow.getChildren().add(dateText);

        HBox statsGrid = new HBox();
        statsGrid.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 8; -fx-padding: 10; "
                + "-fx-border-color: rgba(255,255,255,0.04); -fx-border-radius: 8;");

        VBox killCol = createStatColumn("KILLS", kills);
        VBox dmgCol = createStatColumn("DAMAGE", damage);
        VBox timeCol = createStatColumn("TIME", time);

        Region s1 = new Region();
        HBox.setHgrow(s1, Priority.ALWAYS);
        Region s2 = new Region();
        HBox.setHgrow(s2, Priority.ALWAYS);

        statsGrid.getChildren().addAll(killCol, s1, dmgCol, s2, timeCol);

        // Bottom Actions
        HBox actionsRow = new HBox(10);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        Button viewDetails = new Button("View Details");
        viewDetails.setPrefHeight(35);
        viewDetails.setPrefWidth(180);
        GamerVaultStyles.applyGhostButton(viewDetails);
        viewDetails.setOnAction(e -> {
            viewDetails.setText("Loading...");
            viewDetails.setDisable(true);

            new Thread(() -> {
                try {
                    MatchExtractionResultModel detailedMatch = MatchController.getDetailedMatch(match.getMatchId());

                    Platform.runLater(() -> {
                        viewDetails.setText("View Details");
                        viewDetails.setDisable(false);

                        if (detailedMatch == null) {
                            System.err.println("Could not load match details.");
                            return;
                        }

                        MatchesDetailScreen detailsScreen = new MatchesDetailScreen(match, detailedMatch);

                        parentContainer.setCenter(
                                detailsScreen.startMatchDetailsScreen(() -> {
                                    parentContainer.setCenter(createMatchHistoryContent());
                                }));
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        viewDetails.setText("View Details");
                        viewDetails.setDisable(false);
                        System.err.println("Error fetching match details: " + ex.getMessage());
                    });
                }
            }).start();
        });

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Button deleteBtn = new Button("🗑");
        deleteBtn.setPrefSize(35, 35);
        String delBase = "-fx-background-color: rgba(255,255,255,0.04); -fx-text-fill: "
                + GamerVaultStyles.TEXT_SECONDARY + "; "
                + "-fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 8;";
        String delHover = "-fx-background-color: rgba(239,68,68,0.1); -fx-text-fill: #FCA5A5; "
                + "-fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: rgba(239,68,68,0.3); -fx-border-radius: 8;";
        deleteBtn.setStyle(delBase);
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(delHover));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(delBase));
        deleteBtn.setOnAction(e -> {
            confirmAndDeleteMatch(match);
        });

        actionsRow.getChildren().addAll(viewDetails, spacer2, deleteBtn);

        card.getChildren().addAll(topRow, SizedBox.height(3), mapTitle, dateRow, statsGrid, actionsRow);

        cardStack.getChildren().addAll(baseGlassCanvas, mapBgImage, card);
        GamerVaultAnimations.scaleOnHover(cardStack, 1.03);

        VBox externalWrapper = new VBox(cardStack);
        return externalWrapper;
    }

    /*
     * This method creates the stat columns displayed in match cards.
     */
    private VBox createStatColumn(String labelStr, String valueStr) {
        VBox col = new VBox(5);
        col.setAlignment(Pos.CENTER);

        Text label = new Text(labelStr);
        label.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        label.setFont(Font.font("Arial", FontWeight.BOLD, 10));

        Text value = new Text(valueStr);
        value.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        value.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        col.getChildren().addAll(label, value);
        return col;
    }

    /*
     * HEADER UI - HELPER METHOD
     */
    private VBox createHeaderBox() {
        Text title = new Text("Match History");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 42));
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

        Text subtitle = new Text("View and manage all your processed BGMI matches.");
        subtitle.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        subtitle.setFont(Font.font("Poppins", FontWeight.NORMAL, 16));

        VBox headerBox = new VBox(5);
        headerBox.getChildren().addAll(title, subtitle);

        GamerVaultAnimations.fadeInUp(title, 0, 500);
        GamerVaultAnimations.fadeInUp(subtitle, 150, 500);

        return headerBox;
    }

    private void showLoadingState() {

        matchGrid.getChildren().clear();

        for (int i = 0; i < 6; i++) {

            VBox skeleton = createSkeletonCard();

            matchGrid.getChildren().add(skeleton);
        }
    }

    private VBox createSkeletonCard() {

        VBox card = new VBox(15);
        card.setPrefSize(320, 240);
        card.setPadding(new Insets(20));

        GamerVaultStyles.applyGlassCard(card);

        Region line1 = new Region();
        line1.setPrefSize(120, 18);
        line1.setStyle("-fx-background-color:#2D3748;-fx-background-radius:8;");

        Region line2 = new Region();
        line2.setPrefSize(180, 28);
        line2.setStyle("-fx-background-color:#374151;-fx-background-radius:8;");

        Region block = new Region();
        block.setPrefSize(260, 80);
        block.setStyle("-fx-background-color:#2D3748;-fx-background-radius:8;");

        Region button = new Region();
        button.setPrefSize(120, 35);
        button.setStyle("-fx-background-color:#374151;-fx-background-radius:8;");

        card.getChildren().addAll(
                line1,
                line2,
                block,
                button);

        // GamerVaultAnimations.shimmerNode(card);

        return card;
    }

    /*
     * This method is called to create those number of cards that will be there in
     * firestore database document, it will be used to make card for all matches.
     */
    private void displayMatchCards(List<MatchModel> matches) {

        matchGrid.getChildren().clear();

        if (matches == null || matches.isEmpty()) {

            Text emptyText = new Text("No matches found in your vault.");
            emptyText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
            emptyText.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

            matchGrid.getChildren().add(emptyText);
            return;
        }

        for (int i = 0; i < matches.size(); i++) {

            VBox card = createMatchCard(matches.get(i));

            matchGrid.getChildren().add(card);

            GamerVaultAnimations.fadeInUp(card, i * 50, 400);
        }
    }

    /*
     * This method is called to get the list of matches from the firestore database.
     * It calls the method from match controller - dao to get all the matches list
     * to be displayed on the screen.
     * 
     * Calculate data that needs to be shown and pass it to the updateStatsUI
     * method.
     */
    private void loadAndDisplayMatchHistory() {
        String userId = "TEST_USER_123";
        if (AuthController.currentUser != null && AuthController.currentUser.getUserId() != null) {
            userId = AuthController.currentUser.getUserId();
        }
        final String finalUserId = userId;

        new Thread(() -> {
            try {
                matchHistory = MatchController.getUserMatchHistory(finalUserId);

                Platform.runLater(() -> {

                    if (matchHistory == null || matchHistory.isEmpty()) {
                        Text emptyText = new Text("No matches found in your vault.");
                        emptyText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                        emptyText.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
                        matchGrid.getChildren().add(emptyText);
                        updateStatsUI(0, 0, 0, 0);
                        return;
                    }

                    int totalMatches = matchHistory.size();

                    double totalKills = 0;
                    double totalDamage = 0;
                    int wins = 0;

                    for (MatchModel match : matchHistory) {

                        totalKills += match.getKills();

                        totalDamage += match.getDamage();

                        if (match.getTeamPlacement() == 1) {
                            wins++;
                        }
                    }

                    matchGrid.getChildren().clear();
                    displayMatchCards(matchHistory);

                    double avgKills = totalMatches > 0 ? (totalKills / totalMatches) : 0;
                    double avgDamage = totalMatches > 0 ? (totalDamage / totalMatches) : 0;
                    double winRate = totalMatches > 0 ? ((double) wins / totalMatches) * 100 : 0;

                    updateStatsUI(totalMatches, avgKills, avgDamage, winRate);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Text errorText = new Text("Error loading matches.");
                    errorText.setFill(Color.web("#EF4444"));
                    matchGrid.getChildren().add(errorText);
                });
            }
        }).start();
    }

    /*
     * This method is called to update the stats ui.
     * It is called after the matches are loaded from the firestore database.
     * 
     * Dynamic data needs to be shown using this method.
     */
    private void updateStatsUI(int totalMatches, double avgKills, double avgDamage, double winRate) {
        statsRow.getChildren().clear();

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        Region spacer3 = new Region();
        HBox.setHgrow(spacer3, Priority.ALWAYS);

        statsRow.getChildren().addAll(
                createStatCard("Total Matches", String.valueOf(totalMatches)),
                spacer1,
                createStatCard("Avg Kills", String.format("%.1f", avgKills)),
                spacer2,
                createStatCard("Avg Damage", String.format("%.0f", avgDamage)),
                spacer3,
                createStatCard("Win Rate", String.format("%.1f%%", winRate)));
        GamerVaultAnimations.fadeInUp(statsRow, 0, 500);
    }

    private void filterMatches() {
        if (matchHistory == null) {
            return;
        }
        String search = searchField.getText().trim().toLowerCase();
        List<MatchModel> filtered = new ArrayList<>();
        for (MatchModel match : matchHistory) {
            String selectedMap = mapFilter.getValue();
            if (!selectedMap.equals("All")
                    && !match.getMap().equalsIgnoreCase(selectedMap)) {
                continue;
            }
            if (match.getMap().toLowerCase().contains(search)
                    || match.getMatchName().toLowerCase().contains(search)
                    || match.getMatchDate().toLowerCase().contains(search)
                    || String.valueOf(match.getTeamPlacement()).contains(search)
                    || String.valueOf(match.getKills()).contains(search)) {
                filtered.add(match);
            }
        }
        displayMatchCards(filtered);
    }

    private void confirmAndDeleteMatch(MatchModel match) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Match");
        alert.setHeaderText("Delete Match");
        alert.setContentText(
                "Are you sure you want to permanently delete this match?\n\nThis action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = MatchController.deleteMatch(
                    match.getUserId(),
                    match.getMatchId());
            if (success) {
                loadAndDisplayMatchHistory();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Delete Failed");
                error.setHeaderText(null);
                error.setContentText("Unable to delete the selected match.");
                error.show();
            }
        }
    }
}