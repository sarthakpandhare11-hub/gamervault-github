package com.example.view.player;

import java.util.ArrayList;
import java.util.List;

import com.example.controller.admin.ContentController;
import com.example.controller.admin.TournamentController;
import com.example.model.admin.ContentModel;
import com.example.model.admin.TournamentModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class PlayerDiscoveryHubScreen {

    private String activeTab = "CONTENT"; // Default tab
    private FlowPane gridLayout;

    private List<ContentModel> allContent;
    private List<TournamentModel> allTournaments;

    private Button contentTabBtn;
    private Button tourneyTabBtn;

    private VBox toastContainer;

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

        // Initialize the floating toast container
        toastContainer = new VBox(15);
        toastContainer.setAlignment(Pos.BOTTOM_RIGHT);
        toastContainer.setPadding(new Insets(30));
        toastContainer.setPickOnBounds(false); // Let clicks pass through empty space to the UI below

        // Wrap the scroller and the toast container in a StackPane
        StackPane contentOverlay = new StackPane();
        contentOverlay.getChildren().addAll(scroller, toastContainer);

        root.setCenter(contentOverlay);

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
        card.setPrefSize(400, 320);
        card.setPadding(new Insets(15));

        String baseStyle = "-fx-background-color: #121626; -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 12;";
        card.setStyle(baseStyle);

        // 1. Ambient Glow DropShadow
        DropShadow glow = new DropShadow();
        glow.setColor(Color.TRANSPARENT);
        glow.setRadius(20);
        glow.setSpread(0.1);
        card.setEffect(glow);

        // 2. BIG IMAGE container with inner scaling
        StackPane imageBox = new StackPane();
        imageBox.setPrefSize(370, 208);
        imageBox.setStyle("-fx-background-color: #0F172A; -fx-background-radius: 12;");

        Rectangle clip = new Rectangle(370, 208);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        imageBox.setClip(clip);

        ImageView img = null;
        if (model.getThumbnailUrl() != null && !model.getThumbnailUrl().isEmpty()) {
            img = new ImageView(model.getThumbnailUrl());
            img.setFitWidth(370);
            img.setFitHeight(208);
            img.setPreserveRatio(false);
            imageBox.getChildren().add(img);
        }

        // 3. PLATFORM BADGE
        boolean isYT = "YouTube".equalsIgnoreCase(model.getPlatform());
        String accentColor = isYT ? "#EF4444" : "#EC4899"; // Red for YT, Pink for others

        Label badge = new Label((isYT ? "▶ " : "🎮 ") + model.getPlatform());
        badge.setStyle("-fx-background-color: rgba(0,0,0,0.85); -fx-text-fill: " + accentColor
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
        watchBtn.setStyle(
                watchBtn.getStyle() + "-fx-border-color: " + accentColor + "; -fx-text-fill: " + accentColor + ";");
        watchBtn.setOnAction(e -> {
            try {
                if (model.getUrl() != null)
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(model.getUrl()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        card.getChildren().addAll(imageBox, textInfo, spacer, watchBtn);

        // 4. Attach Premium Animations
        attachCardHoverEffects(card, glow, imageBox, accentColor, baseStyle);

        return card;
    }

    private VBox createMassiveTournamentCard(TournamentModel model) {
        VBox card = new VBox(15);
        card.setPrefSize(400, 360);
        card.setPadding(new Insets(15));

        String baseStyle = "-fx-background-color: #121626; -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 12;";
        card.setStyle(baseStyle);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.TRANSPARENT);
        glow.setRadius(20);
        glow.setSpread(0.1);
        card.setEffect(glow);

        StackPane imageBox = new StackPane();
        imageBox.setPrefSize(370, 208);
        imageBox.setStyle("-fx-background-color: #0F172A; -fx-background-radius: 12;");

        Rectangle clip = new Rectangle(370, 208);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        imageBox.setClip(clip);

        // Inject the sliding carousel (matching the 370x208 dimensions)
        Pane imageCarousel = createSlidingCarousel(model.getImageUrls(), 370, 208);
        imageBox.getChildren().add(imageCarousel);

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
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(externalLink));
                } else {
                    showNotification("Registration Unavailable",
                            "The organizer has not provided a valid external link.", true);
                }
            } catch (Exception ex) {
                System.err.println("Could not open external browser link: " + ex.getMessage());
            }
        });

        buttonsRow.getChildren().addAll(infoBtn, registerBtn);
        card.getChildren().addAll(imageBox, textInfo, spacer, buttonsRow);

        // Attach Premium Animations
        attachCardHoverEffects(card, glow, imageBox, GamerVaultStyles.ACCENT_PURPLE_LIGHT, baseStyle);

        return card;
    }

    private void attachCardHoverEffects(VBox card, DropShadow glow, Node imageNode, String accentColor,
            String baseStyle) {
        String hoverStyle = "-fx-background-color: #161B2E; -fx-background-radius: 12; -fx-border-color: " + accentColor
                + "; -fx-border-radius: 12;";

        card.setOnMouseEntered(e -> {
            // 1. Lift the card
            ScaleTransition cardScale = new ScaleTransition(Duration.millis(250), card);
            cardScale.setToX(1.03);
            cardScale.setToY(1.03);
            cardScale.play();

            // 2. Inner Image/Carousel Zoom (Ken Burns effect)
            if (imageNode != null) {
                ScaleTransition imgScale = new ScaleTransition(Duration.millis(300), imageNode);
                imgScale.setToX(1.08);
                imgScale.setToY(1.08);
                imgScale.play();
            }

            // 3. Ignite Border & Shadow
            card.setStyle(hoverStyle);
            Timeline glowAnim = new Timeline(
                    new KeyFrame(Duration.millis(250),
                            new KeyValue(glow.colorProperty(), Color.web(accentColor, 0.35)),
                            new KeyValue(glow.radiusProperty(), 30)));
            glowAnim.play();
            card.setCursor(javafx.scene.Cursor.HAND);
        });

        card.setOnMouseExited(e -> {
            // Revert Everything
            ScaleTransition cardScale = new ScaleTransition(Duration.millis(250), card);
            cardScale.setToX(1.0);
            cardScale.setToY(1.0);
            cardScale.play();

            if (imageNode != null) {
                ScaleTransition imgScale = new ScaleTransition(Duration.millis(300), imageNode);
                imgScale.setToX(1.0);
                imgScale.setToY(1.0);
                imgScale.play();
            }

            card.setStyle(baseStyle);
            Timeline glowAnim = new Timeline(
                    new KeyFrame(Duration.millis(250),
                            new KeyValue(glow.colorProperty(), Color.TRANSPARENT),
                            new KeyValue(glow.radiusProperty(), 20)));
            glowAnim.play();
        });
    }

    private void openDetailsModal(TournamentModel model) {
        Dialog<Void> dialog = new Dialog<>();
        DialogPane dialogPane = dialog.getDialogPane();

        dialogPane.getStylesheets().add("data:text/css," +
                ".dialog-pane { -fx-background-color: transparent; -fx-padding: 0; }" +
                ".dialog-pane > *.button-bar > *.container { -fx-padding: 0; }");

        VBox root = new VBox();
        root.setPrefSize(750, 600);
        root.setStyle(
                "-fx-background-color: #0A0F18; " +
                        "-fx-border-color: rgba(139, 92, 246, 0.5); " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 16; " +
                        "-fx-background-radius: 16;");

        // 1. HERO SECTION (With New Infinite Carousel)
        StackPane heroSection = new StackPane();
        heroSection.setPrefHeight(300);

        Rectangle clip = new Rectangle(750, 300);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        heroSection.setClip(clip);

        // Inject the Carousel here
        Pane imageCarousel = createSlidingCarousel(model.getImageUrls(), 750, 300);
        Rectangle fadeOverlay = new Rectangle(750, 300);
        fadeOverlay.setFill(new LinearGradient(
                0, 0, 0, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(0.5, Color.web("#0A0F18", 0.3)),
                new Stop(1, Color.web("#0A0F18", 1.0))));

        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.BOTTOM_LEFT);
        titleBox.setPadding(new Insets(30));

        Text tTitle = new Text(model.getTitle());
        tTitle.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        tTitle.setFill(Color.WHITE);
        tTitle.setWrappingWidth(650);

        String orgName = model.getOrganizerName() != null ? model.getOrganizerName() : "Community Event";
        Text tOrg = new Text("Hosted by " + orgName);
        tOrg.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        tOrg.setFill(Color.web(GamerVaultStyles.ACCENT_CYAN));

        titleBox.getChildren().addAll(tTitle, tOrg);
        heroSection.getChildren().addAll(imageCarousel, fadeOverlay, titleBox);

        // 2. QUICK STATS BAR
        HBox statsBar = new HBox(60); // Increased spacing for better symmetry
        statsBar.setAlignment(Pos.CENTER);
        statsBar.setPadding(new Insets(15));
        statsBar.setStyle(
                "-fx-background-color: rgba(255,255,255,0.02); " +
                        "-fx-border-color: rgba(255,255,255,0.05); " +
                        "-fx-border-width: 1 0 1 0;");

        String slotsText = model.getSlotsRegistered() + " / " + model.getSlotsMax();
        statsBar.getChildren().addAll(
                createQuickStat("PRIZE POOL", model.getPrizePool(), "#F59E0B"),
                createQuickStat("TEAM SLOTS", slotsText, "#E2E8F0"),
                createQuickStat("STARTS ON", model.getStartDate(), GamerVaultStyles.ACCENT_GREEN));

        // 3. MAIN CONTENT (Symmetrical Split)
        HBox mainContent = new HBox(20); // Tighter gap between columns
        mainContent.setPadding(new Insets(30));
        mainContent.setAlignment(Pos.TOP_CENTER);

        // Left Column (Narrative - 60%)
        VBox leftCol = new VBox(15);
        leftCol.setPrefWidth(420); // Explicit Width

        Text aboutTitle = new Text("Event Details");
        aboutTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        aboutTitle.setFill(Color.WHITE);

        Text description = new Text(
                "Prepare for the ultimate battleground experience. Registration is currently open to all eligible teams. Ensure your squad meets the requirements and is ready to compete on the scheduled start date. Good luck!");
        description.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        description.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        description.setWrappingWidth(400); // Wrap slightly inside the column width
        description.setLineSpacing(5);

        leftCol.getChildren().addAll(aboutTitle, description);

        // Right Column (Action Area - 40%)
        VBox rightCol = new VBox(20);
        rightCol.setPrefWidth(250); // Explicit Width guarantees perfect balance
        rightCol.setStyle(
                "-fx-background-color: #121626; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: rgba(255,255,255,0.05); " +
                        "-fx-border-radius: 12; " +
                        "-fx-padding: 20;");

        VBox regInfo = new VBox(5);
        regInfo.setAlignment(Pos.CENTER); // Center align the right column data

        Text reqTitle = new Text("Registration Closes:");
        reqTitle.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        reqTitle.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

        Text regDate = new Text(model.getRegEndDate());
        regDate.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        regDate.setFill(Color.web("#EF4444"));
        regInfo.getChildren().addAll(reqTitle, regDate);

        Button registerBtn = new Button("REGISTER TEAM");
        registerBtn.setPrefWidth(Double.MAX_VALUE);
        registerBtn.setPrefHeight(45);
        GamerVaultStyles.applyGradientButton(registerBtn, GamerVaultStyles.ACCENT_PURPLE,
                GamerVaultStyles.ACCENT_PURPLE_DARK, "white");
        registerBtn.setOnAction(e -> {
            try {
                String externalLink = model.getInstagramLink();
                if (externalLink != null && !externalLink.trim().isEmpty()) {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(externalLink));
                }
            } catch (Exception ex) {
                System.err.println("Error: " + ex.getMessage());
            }
        });

        Button closeBtn = new Button("Close Viewer");
        closeBtn.setPrefWidth(Double.MAX_VALUE);
        closeBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-cursor: hand; -fx-font-weight: bold;");
        closeBtn.setOnAction(e -> dialog.setResult(null));

        rightCol.getChildren().addAll(regInfo, registerBtn, closeBtn);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS); // Ensures columns are pushed apart evenly

        mainContent.getChildren().addAll(leftCol, spacer, rightCol);

        // Assemble the UI
        root.getChildren().addAll(heroSection, statsBar, mainContent);

        dialogPane.getButtonTypes().add(ButtonType.CANCEL);
        javafx.scene.Node cancelNode = dialogPane.lookupButton(ButtonType.CANCEL);
        cancelNode.setManaged(false);
        cancelNode.setVisible(false);

        dialogPane.setContent(root);
        dialog.showAndWait();
    }

    // UI Helper for the Stats Bar
    private VBox createQuickStat(String title, String value, String colorHex) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);

        Text t = new Text(title);
        t.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        t.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

        Text v = new Text(value != null ? value : "TBD");
        v.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        v.setFill(Color.web(colorHex));

        box.getChildren().addAll(t, v);
        return box;
    }

    private StackPane createSlidingCarousel(List<String> imageUrls, double width, double height) {
        StackPane root = new StackPane();
        root.setPrefSize(width, height);
        root.setStyle("-fx-background-color: #121626;");

        if (imageUrls == null || imageUrls.isEmpty()) {
            return root; // Return empty dark background if no images
        }

        // 1. Pre-load all images into memory to prevent loading flashes
        List<ImageView> imageViews = new ArrayList<>();
        for (String url : imageUrls) {
            Image img = new Image(url, true); // true = load in background safely
            ImageView view = new ImageView(img);
            view.setFitWidth(width);
            view.setFitHeight(height);
            view.setPreserveRatio(false);
            imageViews.add(view);
        }

        if (imageUrls.size() == 1) {
            root.getChildren().add(imageViews.get(0));
            return root; // No animation needed for a single image
        }

        // 2. The Filmstrip (A horizontal track holding all images)
        HBox filmstrip = new HBox();
        filmstrip.getChildren().addAll(imageViews);

        // A clipping pane so we only see one image at a time
        Pane clipPane = new Pane(filmstrip);
        clipPane.setPrefSize(width, height);
        Rectangle clip = new Rectangle(width, height);
        clipPane.setClip(clip);

        // 3. Pagination Dots (The small indicator circles)
        HBox dotsContainer = new HBox(8);
        dotsContainer.setAlignment(Pos.BOTTOM_CENTER);
        dotsContainer.setPadding(new Insets(15));

        List<Circle> dots = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            Circle dot = new Circle(4);
            // Highlight the first dot as active initially
            dot.setFill(i == 0 ? Color.WHITE : Color.web("#FFFFFF", 0.3));
            dots.add(dot);
            dotsContainer.getChildren().add(dot);
        }

        root.getChildren().addAll(clipPane, dotsContainer);

        // 4. The Infinite Slide Logic
        final int[] currentIndex = { 0 };

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(4), event -> {

            // Slide the entire filmstrip to the left by exactly one image width
            TranslateTransition slide = new TranslateTransition(Duration.millis(600), filmstrip);
            slide.setByX(-width);
            slide.setInterpolator(Interpolator.EASE_BOTH);

            slide.setOnFinished(e -> {
                // Update our active index
                currentIndex[0] = (currentIndex[0] + 1) % imageUrls.size();

                // MAGIC TRICK: Move the image that just slid off-screen to the back of the line
                Node firstImage = filmstrip.getChildren().remove(0);
                filmstrip.getChildren().add(firstImage);

                // Instantly snap the filmstrip back to X=0.
                // Because we moved the images, the user sees absolutely no jump!
                filmstrip.setTranslateX(0);

                // Update the dots to reflect the new active image
                for (int i = 0; i < dots.size(); i++) {
                    dots.get(i).setFill(i == currentIndex[0] ? Color.WHITE : Color.web("#FFFFFF", 0.3));
                }
            });

            slide.play();
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        return root;
    }

    private void showNotification(String title, String message, boolean isError) {
        Platform.runLater(() -> {
            HBox toast = new HBox(15);
            toast.setAlignment(Pos.CENTER_LEFT);
            toast.setPadding(new Insets(15, 20, 15, 20));

            // Build the card
            GamerVaultStyles.applyGlassCard(toast);
            String accentColor = isError ? "#EF4444" : GamerVaultStyles.ACCENT_GREEN;
            toast.setStyle(toast.getStyle() + "-fx-border-color: " + accentColor + "; -fx-border-width: 1 1 1 4;");

            Text titleText = new Text(title);
            titleText.setFill(Color.WHITE);
            titleText.setFont(Font.font("Arial", FontWeight.BOLD, 14));

            Text msgText = new Text(message);
            msgText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
            msgText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            msgText.setWrappingWidth(250);

            VBox textBox = new VBox(5, titleText, msgText);
            toast.getChildren().add(textBox);

            toastContainer.getChildren().add(toast);

            // Trigger the animation
            GamerVaultAnimations.slideInNotification(toast);

            // Auto-dismiss cleanup loop
            PauseTransition delay = new PauseTransition(Duration.seconds(4));
            delay.setOnFinished(e -> {
                FadeTransition ft = new FadeTransition(Duration.millis(300), toast);
                ft.setToValue(0);
                ft.setOnFinished(ev -> toastContainer.getChildren().remove(toast));
                ft.play();
            });
            delay.play();
        });
    }
}