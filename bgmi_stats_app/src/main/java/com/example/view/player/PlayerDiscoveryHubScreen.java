package com.example.view.player;

import java.util.List;
import java.util.function.BiFunction;

import com.example.controller.admin.ContentController;
import com.example.controller.admin.TournamentController;
import com.example.model.admin.ContentModel;
import com.example.model.admin.TournamentModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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

public class PlayerDiscoveryHubScreen {

    private String activeTab = "CONTENT"; // Default tab
    private FlowPane gridLayout;

    private List<ContentModel> allContent;
    private List<TournamentModel> allTournaments;

    private Button contentTabBtn;
    private Button tourneyTabBtn;

    public BorderPane startDiscoveryScreen(String initialTab) {
        if (initialTab != null)
            this.activeTab = initialTab;

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        ScrollPane scroller = new ScrollPane();
        scroller.setContent(createContent());
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setFitToWidth(true);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setCenter(scroller);

        loadHubData();
        return root;
    }

    private VBox createContent() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(10, 20, 40, 20));

        // HEADER
        VBox headerBox = new VBox(5);
        Text title = new Text("Discovery Hub");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 42));
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

        Text subtitle = new Text(
                "Explore the latest curated content and register for high-stakes competitive brackets.");
        subtitle.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        headerBox.getChildren().addAll(title, subtitle);

        // CONTROLS ROW (Toggle & Search)
        HBox controlsRow = new HBox(20);
        controlsRow.setAlignment(Pos.CENTER_LEFT);

        // Custom Segmented Toggle Switch
        HBox toggleBox = new HBox();
        toggleBox.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-padding: 4;");

        contentTabBtn = new Button("🎥 Trending Content");
        tourneyTabBtn = new Button("🏆 Active Tournaments");

        for (Button b : new Button[] { contentTabBtn, tourneyTabBtn }) {
            b.setPrefHeight(38);
            b.setPrefWidth(180);
            b.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            b.setStyle("-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-cursor: hand;");
        }

        contentTabBtn.setOnAction(e -> switchTab("CONTENT"));
        tourneyTabBtn.setOnAction(e -> switchTab("TOURNAMENTS"));

        toggleBox.getChildren().addAll(contentTabBtn, tourneyTabBtn);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> renderGrid(newVal));
        HBox searchBox = GamerVaultStyles.createStyledInput(searchField, GamerVaultStyles.ACCENT_PURPLE);
        searchBox.setPrefWidth(300);

        controlsRow.getChildren().addAll(toggleBox, spacer, searchBox);

        // DYNAMIC GRID
        gridLayout = new FlowPane();
        gridLayout.setHgap(25);
        gridLayout.setVgap(25);

        container.getChildren().addAll(headerBox, controlsRow, gridLayout);
        updateToggleStyles();
        return container;
    }

    private void switchTab(String tab) {
        this.activeTab = tab;
        updateToggleStyles();
        renderGrid("");
    }

    private void updateToggleStyles() {
        String activeStyle = "-fx-background-color: rgba(139,92,246,0.2); -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;";
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-cursor: hand;";

        contentTabBtn.setStyle("CONTENT".equals(activeTab) ? activeStyle : inactiveStyle);
        tourneyTabBtn.setStyle("TOURNAMENTS".equals(activeTab) ? activeStyle : inactiveStyle);
    }

    private void loadHubData() {
        new Thread(() -> {
            try {
                List<ContentModel> contentData = ContentController.loadContent();
                List<TournamentModel> tourneyData = TournamentController.loadActiveTournaments();

                Platform.runLater(() -> {
                    this.allContent = contentData;
                    this.allTournaments = tourneyData;
                    renderGrid("");
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void renderGrid(String query) {
        gridLayout.getChildren().clear();
        String lowerQuery = query.toLowerCase().trim();
        int index = 0;

        if ("CONTENT".equals(activeTab) && allContent != null) {
            for (ContentModel c : allContent) {
                if (c.getTitle() != null && c.getTitle().toLowerCase().contains(lowerQuery)) {
                    VBox card = createMassiveContentCard(c);
                    gridLayout.getChildren().add(card);
                    GamerVaultAnimations.fadeInUp(card, index * 50, 400);
                    index++;
                }
            }
        } else if ("TOURNAMENTS".equals(activeTab) && allTournaments != null) {
            for (TournamentModel t : allTournaments) {
                if (t.getTitle() != null && t.getTitle().toLowerCase().contains(lowerQuery)) {
                    VBox card = createMassiveTournamentCard(t);
                    gridLayout.getChildren().add(card);
                    GamerVaultAnimations.fadeInUp(card, index * 50, 400);
                    index++;
                }
            }
        }

        if (gridLayout.getChildren().isEmpty()) {
            Text empty = new Text("No results found in " + ("CONTENT".equals(activeTab) ? "Content." : "Tournaments."));
            empty.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
            gridLayout.getChildren().add(empty);
        }
    }

    // --- MASSIVE IMAGE CARDS ---

    private VBox createMassiveContentCard(ContentModel model) {
        VBox card = new VBox(15);
        card.setPrefSize(400, 320); // Big card dimensions
        card.setPadding(new Insets(15));
        GamerVaultStyles.applyGlassCard(card);
        GamerVaultAnimations.scaleOnHover(card, 1.02);

        // BIG IMAGE (16:9 ratio -> 370x208)
        StackPane imageBox = new StackPane();
        imageBox.setPrefSize(370, 208);
        imageBox.setStyle("-fx-background-color: #0F172A; -fx-background-radius: 12;");

        Rectangle clip = new Rectangle(370, 208);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        imageBox.setClip(clip);

        if (model.getThumbnailUrl() != null && !model.getThumbnailUrl().isEmpty()) {
            ImageView img = new ImageView(model.getThumbnailUrl());
            img.setFitWidth(370);
            img.setFitHeight(208);
            img.setPreserveRatio(false);
            imageBox.getChildren().add(img);
        }

        // PLATFORM BADGE
        boolean isYT = "YouTube".equalsIgnoreCase(model.getPlatform());
        Label badge = new Label((isYT ? "▶ " : "📷 ") + model.getPlatform());
        badge.setStyle("-fx-background-color: rgba(0,0,0,0.85); -fx-text-fill: " + (isYT ? "#EF4444" : "#EC4899")
                + "; -fx-font-family: Arial; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 5 12 5 12; -fx-background-radius: 8;");
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(10));
        imageBox.getChildren().add(badge);

        VBox textInfo = new VBox(5);
        Text title = new Text(model.getTitle());
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setWrappingWidth(370);

        Text desc = new Text(model.getDescription() != null ? model.getDescription().replace("\n", " ")
                : "No description available.");
        desc.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        desc.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        desc.setWrappingWidth(370);
        if (desc.getText().length() > 80)
            desc.setText(desc.getText().substring(0, 80) + "...");

        textInfo.getChildren().addAll(title, desc);
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button watchBtn = new Button("👁 Watch Now");
        watchBtn.setMaxWidth(Double.MAX_VALUE);
        GamerVaultStyles.applyGhostButton(watchBtn);
        watchBtn.setStyle(watchBtn.getStyle() + "-fx-border-color: " + (isYT ? "#EF4444" : "#EC4899")
                + "; -fx-text-fill: " + (isYT ? "#EF4444" : "#EC4899") + ";");
        watchBtn.setOnAction(e -> {
            try {
                if (model.getUrl() != null)
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(model.getUrl()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        card.getChildren().addAll(imageBox, textInfo, spacer, watchBtn);
        return card;
    }

    private VBox createMassiveTournamentCard(TournamentModel model) {
        VBox card = new VBox(15);
        card.setPrefSize(400, 360);
        card.setPadding(new Insets(15));
        GamerVaultStyles.applyGlassCard(card);
        GamerVaultAnimations.scaleOnHover(card, 1.02);

        // BIG IMAGE
        StackPane imageBox = new StackPane();
        imageBox.setPrefSize(370, 208);
        imageBox.setStyle("-fx-background-color: #0F172A; -fx-background-radius: 12;");
        Rectangle clip = new Rectangle(370, 208);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        imageBox.setClip(clip);

        List<String> images = model.getImageUrls();
        if (images != null && !images.isEmpty()) {
            ImageView img = new ImageView(images.get(0));
            img.setFitWidth(370);
            img.setFitHeight(208);
            img.setPreserveRatio(false);
            imageBox.getChildren().add(img);
        }

        // STATUS BADGE
        String status = model.getStatus() != null ? model.getStatus().toUpperCase() : "UPCOMING";
        String statusHex = "LIVE".equals(status) ? "#EF4444"
                : ("COMPLETED".equals(status) ? GamerVaultStyles.ACCENT_GREEN : GamerVaultStyles.ACCENT_ORANGE);
        Label badge = new Label(status);
        badge.setStyle("-fx-background-color: " + statusHex
                + "CC; -fx-text-fill: white; -fx-font-family: Arial; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 5 12 5 12; -fx-background-radius: 8;");
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(10));
        imageBox.getChildren().add(badge);

        VBox textInfo = new VBox(5);
        Text title = new Text(model.getTitle());
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setWrappingWidth(370);

        Text orgText = new Text(
                "Organized by: " + (model.getOrganizerName() != null ? model.getOrganizerName() : "Community"));
        orgText.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        orgText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

        HBox statsRow = new HBox(15);
        Text prize = new Text("Prize: " + model.getPrizePool());
        prize.setFill(Color.web(GamerVaultStyles.ACCENT_CYAN));
        prize.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        Text slots = new Text("Slots: " + model.getSlotsRegistered() + "/" + model.getSlotsMax());
        slots.setFill(Color.web("#F59E0B"));
        slots.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        statsRow.getChildren().addAll(prize, slots);

        textInfo.getChildren().addAll(title, orgText, SizedBox.height(5), statsRow);
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox buttonsRow = new HBox(10);
        buttonsRow.setAlignment(Pos.CENTER_LEFT);

        Button infoBtn = new Button("ℹ Details");
        GamerVaultStyles.applyGhostButton(infoBtn);
        infoBtn.setOnAction(e -> openDetailsModal(model));

        Button registerBtn = new Button("Register Team");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(registerBtn, Priority.ALWAYS);
        GamerVaultStyles.applyGradientButton(registerBtn, GamerVaultStyles.ACCENT_PURPLE,
                GamerVaultStyles.ACCENT_PURPLE_DARK, "white");
        registerBtn.setOnAction(e -> {
            try {
                String externalLink = model.getInstagramLink();
                if (externalLink != null && !externalLink.trim().isEmpty()) {
                    // Opens the default web browser to the provided URL
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(externalLink));
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,
                            "The organizer has not provided an external registration link yet.",
                            ButtonType.OK);
                    // Theme the alert
                    alert.getDialogPane().setStyle("-fx-background-color: #0B0F19;");
                    Node contentText = alert.getDialogPane().lookup(".content.label");
                    if (contentText != null)
                        contentText.setStyle("-fx-text-fill: white;");
                    alert.showAndWait();
                }
            } catch (Exception ex) {
                System.err.println("Could not open external browser link: " + ex.getMessage());
            }
        });

        buttonsRow.getChildren().addAll(infoBtn, registerBtn);
        card.getChildren().addAll(imageBox, textInfo, spacer, buttonsRow);
        return card;
    }

    private void openDetailsModal(TournamentModel model) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tournament Details");
        VBox modalRoot = new VBox(15);
        modalRoot.setPadding(new Insets(25));
        modalRoot.setPrefWidth(450);
        modalRoot.setStyle(
                "-fx-background-color: #0B0F19; -fx-border-color: rgba(139,92,246,0.3); -fx-border-width: 1; -fx-border-radius: 8;");

        Text title = new Text(model.getTitle());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setFill(Color.WHITE);
        title.setWrappingWidth(400);

        VBox dataGrid = new VBox(10);
        dataGrid.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-padding: 15; -fx-background-radius: 8;");

        BiFunction<String, String, HBox> createRow = (k, v) -> {
            HBox r = new HBox();
            r.setAlignment(Pos.CENTER_LEFT);
            Text label = new Text(k);
            label.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
            label.setFont(Font.font("Arial", 13));
            Region s = new Region();
            HBox.setHgrow(s, Priority.ALWAYS);
            Text val = new Text(v != null ? v : "N/A");
            val.setFill(Color.WHITE);
            val.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            r.getChildren().addAll(label, s, val);
            return r;
        };

        dataGrid.getChildren().addAll(
                createRow.apply("Organizer", model.getOrganizerName()),
                createRow.apply("Prize Pool", model.getPrizePool()),
                createRow.apply("Reg. Closes", model.getRegEndDate()),
                createRow.apply("Tournament Starts", model.getStartDate()),
                createRow.apply("Slots Filled", model.getSlotsRegistered() + " / " + model.getSlotsMax()));

        modalRoot.getChildren().addAll(title, dataGrid);
        dialog.getDialogPane().setContent(modalRoot);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle("-fx-background-color: #0B0F19;");

        Node closeBtn = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        if (closeBtn instanceof Button) {
            GamerVaultStyles.applyGhostButton((Button) closeBtn);
            closeBtn.setStyle(closeBtn.getStyle() + "-fx-text-fill: white;");
        }
        dialog.showAndWait();
    }
}