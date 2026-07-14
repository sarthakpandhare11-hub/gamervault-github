package com.example.view.player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.example.controller.AuthController;
import com.example.controller.admin.TemplateController;
import com.example.controller.gemini.GeminiImageClient;
import com.example.controller.gemini.PortfolioGeminiController;
import com.example.controller.player.MatchController;
import com.example.model.UserModel;
import com.example.model.admin.TemplateModel;
import com.example.model.player.MatchModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PortfolioGeneratorScreen {

    // --- SCREEN NAVIGATION BOUNDARIES ---
    Scene portfolioScreenScene;
    Stage portfolioScreenStage;

    // --- REVENUE RUNTIME STATES ---
    private TemplateModel selectedTemplate = null; // Changed from String to Model
    private MatchModel chosenMatchModel = null;
    private Image currentAiRenderedImage = null;

    // --- CORE INTERFACE NODES ---
    private HBox visualSwitchesGrid; // Changed to FlowPane to wrap dynamically added templates
    private VBox matchHistorySelectorBox;
    private StackPane selectedMatchDisplayArea;
    private ImageView mainPreviewDisplayArea;
    private Button processGenerationBtn;
    private Button saveToDiskBtn;
    private VBox toastContainer;

    private byte[] currentAiRenderedImageBytes = null;

    public void setPortfolioScreenScene(Scene portfolioScreenScene) {
        this.portfolioScreenScene = portfolioScreenScene;
    }

    public void setPortfolioScreenStage(Stage portfolioScreenStage) {
        this.portfolioScreenStage = portfolioScreenStage;
    }

    public BorderPane startPortfolioGeneratorScreen() {
        BorderPane corePane = new BorderPane();
        corePane.setStyle("-fx-background-color: transparent;");

        ScrollPane contentScroller = new ScrollPane();
        contentScroller.setContent(assembleMainScrollerCanvas());
        GamerVaultStyles.applyStyledScrollPane(contentScroller);
        contentScroller.setFitToWidth(true);
        contentScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        // Initialize the floating toast container
        toastContainer = new VBox(15);
        toastContainer.setAlignment(Pos.BOTTOM_RIGHT);
        toastContainer.setPadding(new Insets(30));
        toastContainer.setPickOnBounds(false); // Let clicks pass through empty space to the UI below

        // Wrap the scroller and the toast container in a StackPane
        StackPane contentOverlay = new StackPane();
        contentOverlay.getChildren().addAll(contentScroller, toastContainer);

        corePane.setCenter(contentOverlay);
        return corePane;
    }

    private VBox assembleMainScrollerCanvas() {
        VBox primaryContainer = new VBox(25);
        primaryContainer.setPadding(new Insets(10, 20, 40, 20));

        VBox layoutHeader = assembleSectionHeader();

        HBox coreSplitPanel = new HBox(40);
        VBox configControlsPanel = assembleConfigurationControlsPanel();
        VBox realTimePreviewPanel = assembleRealTimePreviewPanel();

        // Widen the left panel to perfectly fit 2 massive cards side-by-side
        configControlsPanel.setPrefWidth(540);
        configControlsPanel.setMinWidth(540);
        HBox.setHgrow(realTimePreviewPanel, Priority.NEVER);
        VBox.setVgrow(realTimePreviewPanel, Priority.NEVER);

        coreSplitPanel.getChildren().addAll(configControlsPanel, realTimePreviewPanel);
        primaryContainer.getChildren().addAll(layoutHeader, coreSplitPanel);
        return primaryContainer;
    }

    private VBox assembleSectionHeader() {
        Text mainTitle = new Text("Forge Your Legacy");
        mainTitle.setFont(Font.font("Arial", FontWeight.BOLD, 42));
        mainTitle.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

        Text secondarySubtitle = new Text(
                "Choose a template that fits your playstyle. Link your battle telemetry to generate a customized, AI-driven esports portfolio.");
        secondarySubtitle.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        secondarySubtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

        VBox headerGroup = new VBox(5);
        headerGroup.getChildren().addAll(mainTitle, secondarySubtitle);
        GamerVaultAnimations.fadeInUp(mainTitle, 0, 400);
        GamerVaultAnimations.fadeInUp(secondarySubtitle, 100, 400);
        return headerGroup;
    }

    private VBox assembleConfigurationControlsPanel() {
        VBox configurationsCol = new VBox(25);

        // CARD PANEL 1: VISUAL SELECTION SWATCH MATRIX
        VBox layoutSelectorWrapper = new VBox(15);
        layoutSelectorWrapper.setPadding(new Insets(25));
        GamerVaultStyles.applyGlassCard(layoutSelectorWrapper);

        Text stepOneLabel = new Text("1. Choose Target Performance Template");
        stepOneLabel.setFill(Color.WHITE);
        stepOneLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Use HBox for a single horizontal row
        visualSwitchesGrid = new HBox(15);
        visualSwitchesGrid.setAlignment(Pos.CENTER_LEFT);

        rebuildVisualSwatchesPanel();

        // Wrap the HBox in a horizontal ScrollPane
        ScrollPane templateScroller = new ScrollPane(visualSwitchesGrid);
        GamerVaultStyles.applyStyledScrollPane(templateScroller);
        templateScroller.setFitToHeight(true);
        templateScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Disable vertical scrolling
        templateScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Enable horizontal scrolling
        templateScroller.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        templateScroller.setMinHeight(360); // Ensures the scrollbar doesn't clip the hover animations

        layoutSelectorWrapper.getChildren().addAll(stepOneLabel, templateScroller);

        // CARD PANEL 2: MATCH DEPENDENCY CONTEXT PICKER
        matchHistorySelectorBox = new VBox(15);
        matchHistorySelectorBox.setPadding(new Insets(25));
        GamerVaultStyles.applyGlassCard(matchHistorySelectorBox);

        Text stepTwoLabel = new Text("2. Link Telemetry History");
        stepTwoLabel.setFill(Color.WHITE);
        stepTwoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        selectedMatchDisplayArea = new StackPane();
        selectedMatchDisplayArea.setAlignment(Pos.CENTER_LEFT);

        Button summonMatchHistoryModalBtn = new Button("📂 Scan Match Vault History");
        summonMatchHistoryModalBtn.setMaxWidth(Double.MAX_VALUE);
        GamerVaultStyles.applyGhostButton(summonMatchHistoryModalBtn);
        summonMatchHistoryModalBtn.setOnAction(e -> triggerMatchVaultSelectionDialog());

        matchHistorySelectorBox.getChildren().addAll(stepTwoLabel, selectedMatchDisplayArea,
                summonMatchHistoryModalBtn);
        evaluateDynamicWorkflowVisibility();

        configurationsCol.getChildren().addAll(layoutSelectorWrapper, matchHistorySelectorBox);
        GamerVaultAnimations.fadeInUp(configurationsCol, 200, 500);
        return configurationsCol;
    }

    private void evaluateDynamicWorkflowVisibility() {
        if (selectedTemplate != null && (selectedTemplate.getTemplateId().equals("OVERALL") ||
                (selectedTemplate.getTemplateName().toLowerCase().contains("overall")))) {

            matchHistorySelectorBox.setOpacity(0.25);
            matchHistorySelectorBox.setDisable(true);
            chosenMatchModel = null;

            selectedMatchDisplayArea.getChildren().clear();
            Text emptyText = new Text("Bypassed: Overall profiles parse absolute totals directly.");
            emptyText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
            selectedMatchDisplayArea.getChildren().add(emptyText);
        } else {
            matchHistorySelectorBox.setOpacity(1.0);
            matchHistorySelectorBox.setDisable(false);
            if (chosenMatchModel == null) {
                selectedMatchDisplayArea.getChildren().clear();
                Text pendingText = new Text("⚠️ Linkage pending: Assign 1 match dataset.");
                pendingText.setFill(Color.web("#F59E0B"));
                selectedMatchDisplayArea.getChildren().add(pendingText);
            } else {
                updateSelectedMatchUI(chosenMatchModel);
            }
        }
    }

    private void updateSelectedMatchUI(MatchModel match) {
        selectedMatchDisplayArea.getChildren().clear();

        StackPane matchCard = new StackPane();
        matchCard.setPrefHeight(80);
        matchCard.setStyle("-fx-background-radius: 8; -fx-border-color: " + GamerVaultStyles.ACCENT_PURPLE
                + "; -fx-border-radius: 8; -fx-border-width: 2;");

        Rectangle clip = new Rectangle(400, 80); // Width of the left column
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        matchCard.setClip(clip);

        ImageView mapImg = new ImageView();
        mapImg.setFitWidth(410);
        mapImg.setFitHeight(80);
        mapImg.setPreserveRatio(false);
        try {
            String sanitizedMap = match.getMap() != null ? match.getMap().toLowerCase().trim() : "erangel";
            String path = "/assets/bgmi_images/" + sanitizedMap + "_bg.jpg";
            if (getClass().getResource(path) != null) {
                mapImg.setImage(new Image(getClass().getResource(path).toExternalForm(), true));
            } else {
                mapImg.setStyle("-fx-background-color: #121626;");
            }
        } catch (Exception e) {
        }

        // A slight glassmorphic dark tint *only* behind the text so the right side of
        // the map is fully visible
        HBox content = new HBox(15);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(10, 15, 10, 15));
        content.setStyle("-fx-background-color: linear-gradient(to right, rgba(11,15,25,0.95), rgba(11,15,25,0.1));");

        VBox textBlock = new VBox(2);
        Text mapTitle = new Text(match.getMap() != null ? match.getMap().toUpperCase() : "UNKNOWN MAP");
        mapTitle.setFill(Color.WHITE);
        mapTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Text stats = new Text("Placement: #" + match.getTeamPlacement() + "  |  Kills: " + match.getKills()
                + "  |  Dmg: " + match.getDamage());
        stats.setFill(Color.web(GamerVaultStyles.ACCENT_CYAN));
        stats.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        textBlock.getChildren().addAll(mapTitle, stats);
        content.getChildren().add(textBlock);

        matchCard.getChildren().addAll(mapImg, content);
        selectedMatchDisplayArea.getChildren().add(matchCard);
    }

    private void rebuildVisualSwatchesPanel() {
        visualSwitchesGrid.getChildren().clear();

        // 1. CREATE HARDCODED MODELS
        TemplateModel overall = new TemplateModel();
        overall.setTemplateId("OVERALL");
        overall.setTemplateName("Overall Stats");
        overall.setLocalAssetPath("/assets/templates/overall_template.png");
        overall.setHardcoded(true);
        if (selectedTemplate == null)
            selectedTemplate = overall; // Default

        TemplateModel matchTemp = new TemplateModel();
        matchTemp.setTemplateId("MATCH");
        matchTemp.setTemplateName("Last Match");
        matchTemp.setLocalAssetPath("/assets/templates/match_template.png");
        matchTemp.setHardcoded(true);

        TemplateModel mvpTemp = new TemplateModel();
        mvpTemp.setTemplateId("MVP");
        mvpTemp.setTemplateName("M.V.P Profile");
        mvpTemp.setLocalAssetPath("/assets/templates/mvp_template.png");
        mvpTemp.setHardcoded(true);

        visualSwitchesGrid.getChildren().addAll(
                generateVisualTemplateItemNode(overall),
                generateVisualTemplateItemNode(matchTemp),
                generateVisualTemplateItemNode(mvpTemp));

        // 2. FETCH DYNAMIC ADMIN TEMPLATES IN BACKGROUND
        new Thread(() -> {
            List<TemplateModel> dynamicTemplates = TemplateController.getActiveTemplates();
            Platform.runLater(() -> {
                if (dynamicTemplates != null) {
                    for (TemplateModel dynT : dynamicTemplates) {
                        visualSwitchesGrid.getChildren().add(generateVisualTemplateItemNode(dynT));
                    }
                }
            });
        }).start();
    }

    private StackPane generateVisualTemplateItemNode(TemplateModel model) {
        double width = 240;
        double height = 340;

        StackPane cardRoot = new StackPane();
        cardRoot.setPrefSize(width, height);
        cardRoot.setCursor(javafx.scene.Cursor.HAND);

        Rectangle clipMask = new Rectangle(width, height);
        clipMask.setArcWidth(16);
        clipMask.setArcHeight(16);
        cardRoot.setClip(clipMask);

        // Standard ImageView - Fixes Local Image loading instantly
        ImageView assetPreview = new ImageView();
        assetPreview.setFitWidth(width);
        assetPreview.setFitHeight(height);
        assetPreview.setPreserveRatio(false);

        try {
            if (model.isHardcoded()) {
                if (getClass().getResource(model.getLocalAssetPath()) != null) {
                    assetPreview.setImage(new Image(getClass().getResourceAsStream(model.getLocalAssetPath())));
                }
            } else {
                String url = model.getImageUrl();
                if (url != null && !url.isEmpty()) {
                    if (!url.startsWith("http") && !url.startsWith("file:")) {
                        url = new File(url).toURI().toString();
                    }
                    assetPreview.setImage(new Image(url, true));
                }
            }
        } catch (Exception err) {
            System.err.println("Graphic asset skipped: " + err.getMessage());
        }

        Rectangle bottomGradient = new Rectangle(width, height);
        bottomGradient.setFill(new LinearGradient(
                0, 0, 0, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new Stop(0.6, Color.TRANSPARENT),
                new Stop(1, Color.web("#0A0F18", 0.75))));

        Text defaultTitle = new Text(model.getTemplateName().toUpperCase());
        defaultTitle.setFill(Color.WHITE);
        defaultTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        StackPane.setAlignment(defaultTitle, Pos.BOTTOM_CENTER);
        StackPane.setMargin(defaultTitle, new Insets(20));

        VBox glassOverlay = new VBox(10);
        glassOverlay.setAlignment(Pos.TOP_LEFT);
        glassOverlay.setPadding(new Insets(20));
        glassOverlay.setStyle(
                "-fx-background-color: linear-gradient(to top, rgba(10, 15, 24, 0.95), rgba(139, 92, 246, 0.15));" +
                        "-fx-border-color: rgba(139, 92, 246, 0.4);" +
                        "-fx-border-width: 1 0 0 0;");

        Text revealTitle = new Text(model.getTemplateName());
        revealTitle.setFill(Color.web(GamerVaultStyles.ACCENT_CYAN));
        revealTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        String h1 = "✨ Enhances visual profile";
        String h2 = "📊 Auto-adapts to your data";
        if (model.getTemplateId().equals("OVERALL") || model.getTemplateName().toLowerCase().contains("overall")) {
            h1 = "📈 Shows lifetime meta-metrics";
            h2 = "🏆 Highlights peak Win Rate";
        }
        if (model.getTemplateId().equals("MVP")) {
            h1 = "⚔️ Focuses on Peak Damage";
            h2 = "🎯 Showcases weapon accuracy";
        }

        Text f1 = new Text("• " + h1);
        f1.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        f1.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        f1.setWrappingWidth(width - 40);

        Text f2 = new Text("• " + h2);
        f2.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        f2.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        f2.setWrappingWidth(width - 40);

        glassOverlay.getChildren().addAll(revealTitle, f1, f2);

        glassOverlay.setTranslateY(height);
        StackPane.setAlignment(glassOverlay, Pos.BOTTOM_CENTER);

        Rectangle selectionBorder = new Rectangle(width - 4, height - 4);
        selectionBorder.setFill(Color.TRANSPARENT);
        selectionBorder.setStrokeWidth(3);
        selectionBorder.setArcWidth(16);
        selectionBorder.setArcHeight(16);
        selectionBorder.getProperties().put("isSelectionBorder", true);

        boolean matchingActiveState = (selectedTemplate != null
                && selectedTemplate.getTemplateId().equals(model.getTemplateId()));
        if (matchingActiveState) {
            selectionBorder.setStroke(Color.web(GamerVaultStyles.ACCENT_PURPLE));
            selectionBorder.setEffect(new DropShadow(20, Color.web(GamerVaultStyles.ACCENT_PURPLE, 0.6)));
        } else {
            selectionBorder.setStroke(Color.TRANSPARENT);
        }

        cardRoot.getChildren().addAll(assetPreview, bottomGradient, defaultTitle, glassOverlay, selectionBorder);

        attachPremiumHoverReveal(cardRoot, assetPreview, glassOverlay, defaultTitle, height);

        cardRoot.setOnMouseClicked(e -> {
            this.selectedTemplate = model;
            for (Node n : visualSwitchesGrid.getChildren()) {
                if (n instanceof StackPane) {
                    for (Node child : ((StackPane) n).getChildren()) {
                        if (child.getProperties().containsKey("isSelectionBorder")) {
                            ((Rectangle) child).setStroke(Color.TRANSPARENT);
                            child.setEffect(null);
                        }
                    }
                }
            }
            selectionBorder.setStroke(Color.web(GamerVaultStyles.ACCENT_PURPLE));
            selectionBorder.setEffect(new DropShadow(25, Color.web(GamerVaultStyles.ACCENT_PURPLE, 0.7)));
            evaluateDynamicWorkflowVisibility();
        });

        return cardRoot;
    }

    private void attachPremiumHoverReveal(StackPane cardRoot, ImageView image, VBox glassOverlay, Text defaultTitle,
            double cardHeight) {
        cardRoot.setOnMouseEntered(e -> {
            // Zooms the ImageView perfectly
            ScaleTransition scaleImg = new ScaleTransition(Duration.millis(300), image);
            scaleImg.setToX(1.08);
            scaleImg.setToY(1.08);
            scaleImg.play();

            TranslateTransition slideUp = new TranslateTransition(Duration.millis(300), glassOverlay);
            slideUp.setToY(cardHeight - 130);
            slideUp.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
            slideUp.play();

            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(Duration.millis(200),
                    defaultTitle);
            fadeOut.setToValue(0);
            fadeOut.play();
        });

        cardRoot.setOnMouseExited(e -> {
            ScaleTransition scaleImg = new ScaleTransition(Duration.millis(300), image);
            scaleImg.setToX(1.0);
            scaleImg.setToY(1.0);
            scaleImg.play();

            TranslateTransition slideDown = new TranslateTransition(Duration.millis(300), glassOverlay);
            slideDown.setToY(cardHeight);
            slideDown.setInterpolator(javafx.animation.Interpolator.EASE_IN);
            slideDown.play();

            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(Duration.millis(200),
                    defaultTitle);
            fadeIn.setToValue(1);
            fadeIn.play();
        });
    }

    private void triggerMatchVaultSelectionDialog() {
        Dialog<MatchModel> selectionDialog = new Dialog<>();
        javafx.scene.control.DialogPane dialogPane = selectionDialog.getDialogPane();

        // Remove standard OS window borders
        dialogPane.getStylesheets().add("data:text/css," +
                ".dialog-pane { -fx-background-color: transparent; -fx-padding: 0; }" +
                ".dialog-pane > *.button-bar > *.container { -fx-padding: 0; }");

        VBox dialogWrapper = new VBox(20);
        dialogWrapper.setPadding(new Insets(25));
        dialogWrapper.setPrefWidth(550);
        dialogWrapper.setPrefHeight(600);
        dialogWrapper.setStyle(
                "-fx-background-color: #0B0F19; -fx-border-color: rgba(139,92,246,0.5); -fx-border-width: 2; -fx-border-radius: 16; -fx-background-radius: 16;");

        Text interfaceLabel = new Text("Select Telemetry Profile");
        interfaceLabel.setFill(Color.WHITE);
        interfaceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        Text interfaceSub = new Text("Choose a match to link with your portfolio generation.");
        interfaceSub.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        interfaceSub.setFont(Font.font("Arial", FontWeight.NORMAL, 13));

        VBox interactiveListContainer = new VBox(15);

        Text loadingAnchor = new Text("Querying Vault Records...");
        loadingAnchor.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        interactiveListContainer.getChildren().add(loadingAnchor);

        ScrollPane listScroller = new ScrollPane(interactiveListContainer);
        GamerVaultStyles.applyStyledScrollPane(listScroller);
        listScroller.setFitToWidth(true);
        VBox.setVgrow(listScroller, Priority.ALWAYS);

        // Cancel Button (Custom)
        Button cancelBtn = new Button("Cancel Selection");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        GamerVaultStyles.applyGhostButton(cancelBtn);
        cancelBtn.setOnAction(e -> selectionDialog.setResult(null));

        dialogWrapper.getChildren().addAll(interfaceLabel, interfaceSub, listScroller, cancelBtn);

        dialogPane.getButtonTypes().add(ButtonType.CANCEL);
        Node hiddenNativeBtn = dialogPane.lookupButton(ButtonType.CANCEL);
        if (hiddenNativeBtn != null) {
            hiddenNativeBtn.setManaged(false);
            hiddenNativeBtn.setVisible(false);
        }

        dialogPane.setContent(dialogWrapper);

        // Fetch Matches in Background
        new Thread(() -> {
            String primaryPlayerUid = AuthController.currentUser != null ? AuthController.currentUser.getUserId()
                    : "TEST_USER_123";
            List<MatchModel> userHistoryRecords = MatchController.getUserMatchHistory(primaryPlayerUid);

            Platform.runLater(() -> {
                interactiveListContainer.getChildren().clear();
                if (userHistoryRecords == null || userHistoryRecords.isEmpty()) {
                    Text blankStateText = new Text("No telemetry matches discovered inside Vault.");
                    blankStateText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                    interactiveListContainer.getChildren().add(blankStateText);
                    return;
                }

                for (MatchModel recordItem : userHistoryRecords) {
                    // CREATE PREMIUM LIST ITEM
                    StackPane itemRoot = new StackPane();
                    itemRoot.setPrefHeight(90);
                    itemRoot.setStyle("-fx-background-radius: 12; -fx-cursor: hand;");

                    Rectangle clip = new Rectangle(480, 90);
                    clip.setArcWidth(12);
                    clip.setArcHeight(12);
                    itemRoot.setClip(clip);

                    // Background Map Image
                    ImageView mapImg = new ImageView();
                    mapImg.setFitWidth(480);
                    mapImg.setFitHeight(90);
                    mapImg.setPreserveRatio(false);
                    try {
                        String sanitizedMap = recordItem.getMap() != null ? recordItem.getMap().toLowerCase().trim()
                                : "erangel";
                        String path = "/assets/bgmi_images/" + sanitizedMap + "_bg.jpg";
                        if (getClass().getResource(path) != null) {
                            mapImg.setImage(new Image(getClass().getResource(path).toExternalForm(), true));
                        } else {
                            mapImg.setStyle("-fx-background-color: #121626;");
                        }
                    } catch (Exception e) {
                    }

                    // Gradient Overlay to make text readable
                    Rectangle overlay = new Rectangle(480, 90);
                    overlay.setFill(new LinearGradient(
                            0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                            new Stop(0, Color.web("#0A0F18", 0.95)),
                            new Stop(0.6, Color.web("#0A0F18", 0.8)),
                            new Stop(1, Color.web("#0A0F18", 0.4))));

                    HBox content = new HBox(15);
                    content.setAlignment(Pos.CENTER_LEFT);
                    content.setPadding(new Insets(15));

                    // Glass background just for text readability, leaving map visible
                    VBox textBlock = new VBox(5);
                    textBlock.setPadding(new Insets(5, 10, 5, 10));
                    textBlock.setStyle("-fx-background-color: rgba(11, 15, 25, 0.7); -fx-background-radius: 8;");

                    Text mapTitle = new Text(
                            recordItem.getMap() != null ? recordItem.getMap().toUpperCase() : "UNKNOWN");
                    mapTitle.setFill(Color.WHITE);
                    mapTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

                    HBox statsRow = new HBox(10);
                    Text placeTxt = new Text("#" + recordItem.getTeamPlacement());
                    placeTxt.setFill(Color.web(recordItem.getTeamPlacement() == 1 ? "#F59E0B" : "#E2E8F0"));
                    placeTxt.setFont(Font.font("Arial", FontWeight.BOLD, 14));

                    Text killDmgTxt = new Text(
                            "•  Kills: " + recordItem.getKills() + "  •  Dmg: " + recordItem.getDamage());
                    killDmgTxt.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
                    killDmgTxt.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

                    statsRow.getChildren().addAll(placeTxt, killDmgTxt);
                    textBlock.getChildren().addAll(mapTitle, statsRow);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button selectBtn = new Button("LINK");
                    selectBtn.setPrefHeight(35);
                    GamerVaultStyles.applyGradientButton(selectBtn, GamerVaultStyles.ACCENT_PURPLE,
                            GamerVaultStyles.ACCENT_PURPLE_DARK, "white");
                    selectBtn.setOnAction(evt -> {
                        chosenMatchModel = recordItem;
                        updateSelectedMatchUI(recordItem); // CALL THE NEW UI GENERATOR
                        selectionDialog.setResult(recordItem);
                        selectionDialog.close();
                    });

                    content.getChildren().addAll(textBlock, spacer, selectBtn);
                    itemRoot.getChildren().addAll(mapImg, content);

                    GamerVaultAnimations.scaleOnHover(itemRoot, 1.02);
                    interactiveListContainer.getChildren().add(itemRoot);
                }
            });
        }).start();

        selectionDialog.showAndWait();
    }

    /*
     * ─── PREVIEW & EXECUTION AREA (RIGHT SIDE VIEWER) ──────────────────────
     */
    private VBox assembleRealTimePreviewPanel() {
        VBox processingCol = new VBox(20);
        processingCol.setAlignment(Pos.TOP_CENTER);
        processingCol.setPrefWidth(400);

        StackPane visualFrameBackground = new StackPane();
        // --- RESIZING FIX: Strict constraints to lock the box size forever ---
        visualFrameBackground.setPrefSize(380, 520);
        visualFrameBackground.setMinSize(380, 520);
        visualFrameBackground.setMaxSize(380, 520);
        GamerVaultStyles.applyGlassCard(visualFrameBackground);

        // Apply a strict clip so background images don't bleed out of the rounded
        // corners
        Rectangle clipMask = new Rectangle(380, 520);
        clipMask.setArcWidth(16);
        clipMask.setArcHeight(16);
        visualFrameBackground.setClip(clipMask);

        // 1. The Sliding Faded Background
        Pane bgCarousel = createFadedBackgroundCarousel(380, 520);

        // 2. The Main AI Rendered Image
        mainPreviewDisplayArea = new ImageView();
        mainPreviewDisplayArea.setFitWidth(350);
        mainPreviewDisplayArea.setFitHeight(490);
        mainPreviewDisplayArea.setPreserveRatio(true);

        // 3. The Idle Instructions Overlay
        VBox idleStateContainer = new VBox(15);
        idleStateContainer.setAlignment(Pos.CENTER);

        Text iconText = new Text("🎨");
        iconText.setFont(Font.font(42));
        iconText.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

        Text centerStatePlaceholder = new Text("Select a template from the left to forge your legacy.");
        centerStatePlaceholder.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        centerStatePlaceholder.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        centerStatePlaceholder.setWrappingWidth(260);
        centerStatePlaceholder.setTextAlignment(TextAlignment.CENTER);

        idleStateContainer.getChildren().addAll(iconText, centerStatePlaceholder);

        // Stack them: Carousel in back, Idle Text in middle, Generated Image on top
        visualFrameBackground.getChildren().addAll(bgCarousel, idleStateContainer, mainPreviewDisplayArea);

        HBox controlButtonsRow = new HBox(15);
        controlButtonsRow.setAlignment(Pos.CENTER);

        processGenerationBtn = new Button("✨ Dispatch to Gemini Engine");
        processGenerationBtn.setPrefHeight(50);
        HBox.setHgrow(processGenerationBtn, Priority.ALWAYS);
        processGenerationBtn.setMaxWidth(Double.MAX_VALUE);
        GamerVaultStyles.applyGradientButton(processGenerationBtn, GamerVaultStyles.ACCENT_PURPLE,
                GamerVaultStyles.ACCENT_PURPLE_DARK, "white");

        // Pass the UI elements to the handler so we can hide them when generating
        processGenerationBtn.setOnAction(
                e -> dispatchGeminiApiPipelineSequence(idleStateContainer, centerStatePlaceholder, bgCarousel));

        saveToDiskBtn = new Button("📥 Save File To Disk");
        saveToDiskBtn.setPrefHeight(50);
        HBox.setHgrow(saveToDiskBtn, Priority.ALWAYS);
        saveToDiskBtn.setMaxWidth(Double.MAX_VALUE);
        GamerVaultStyles.applyGhostButton(saveToDiskBtn);
        saveToDiskBtn.setDisable(true);
        saveToDiskBtn.setOnAction(e -> executeLocalDiskDownloadSequence());

        controlButtonsRow.getChildren().addAll(processGenerationBtn, saveToDiskBtn);
        processingCol.getChildren().addAll(visualFrameBackground, controlButtonsRow);

        GamerVaultAnimations.fadeInUp(processingCol, 300, 500);
        return processingCol;
    }

    /*
     * ─── GEMINI MULTIMODAL API ORCHESTRATION PIPELINE ───────────────────────
     */
    private void dispatchGeminiApiPipelineSequence(VBox idleStateContainer, Text statePlaceholderText,
            Pane bgCarousel) {
        if (!selectedTemplate.getTemplateId().equals("OVERALL") && chosenMatchModel == null
                && !selectedTemplate.getTemplateName().toLowerCase().contains("overall")) {
            Alert validationWarning = new Alert(Alert.AlertType.WARNING,
                    "Workflow Error: This configuration profile requires a linked telemetry match snapshot.",
                    ButtonType.OK);
            validationWarning.show();
            return;
        }

        processGenerationBtn.setText("Compiling Multimodal Payload...");
        processGenerationBtn.setDisable(true);
        saveToDiskBtn.setDisable(true);

        // Reset the viewer
        mainPreviewDisplayArea.setImage(null);
        idleStateContainer.setVisible(true);
        bgCarousel.setVisible(true);

        statePlaceholderText.setText("Encoding templates & packing variables into payload container...");

        new Thread(() -> {
            try {
                UserModel profileUser = AuthController.currentUser;
                String structuredSystemInstructionsPrompt;
                String encodedBase64ImageContext;

                if (selectedTemplate.isHardcoded()) {
                    structuredSystemInstructionsPrompt = PortfolioGeminiController
                            .compileStrongPrompt(selectedTemplate.getTemplateId(), profileUser, chosenMatchModel);
                    encodedBase64ImageContext = PortfolioGeminiController
                            .encodeTemplateToBase64(selectedTemplate.getLocalAssetPath());
                } else {
                    structuredSystemInstructionsPrompt = PortfolioGeminiController
                            .compileDynamicAdminPrompt(selectedTemplate, profileUser, chosenMatchModel);
                    encodedBase64ImageContext = PortfolioGeminiController
                            .downloadAndConvertUrlToBase64(selectedTemplate.getImageUrl());
                }

                byte[] rawImageBytes = GeminiImageClient.generateHolographicCardBytes(
                        structuredSystemInstructionsPrompt,
                        encodedBase64ImageContext);

                Image remoteAiGeneratedAssetImage = new Image(new java.io.ByteArrayInputStream(rawImageBytes));

                Platform.runLater(() -> {
                    currentAiRenderedImageBytes = rawImageBytes;
                    currentAiRenderedImage = remoteAiGeneratedAssetImage;

                    // Display image and hide background/idle elements
                    mainPreviewDisplayArea.setImage(currentAiRenderedImage);
                    idleStateContainer.setVisible(false);
                    bgCarousel.setVisible(false); // Hide so it doesn't bleed through transparent PNGs!

                    processGenerationBtn.setText("✨ Dispatch to Gemini Engine");
                    processGenerationBtn.setDisable(false);
                    saveToDiskBtn.setDisable(false);
                    saveToDiskBtn
                            .setStyle(saveToDiskBtn.getStyle() + "-fx-border-color: " + GamerVaultStyles.ACCENT_GREEN
                                    + "; -fx-text-fill: " + GamerVaultStyles.ACCENT_GREEN + ";");
                });

            } catch (Exception err) {
                Platform.runLater(() -> {
                    processGenerationBtn.setText("✨ Dispatch to Gemini Engine");
                    processGenerationBtn.setDisable(false);
                    statePlaceholderText.setText("Network Exception: Execution stream dropped.");
                    statePlaceholderText.setFill(Color.web("#EF4444"));
                });
            }
        }).start();
    }

    private void executeLocalDiskDownloadSequence() {
        if (currentAiRenderedImageBytes == null)
            return;

        FileChooser saveDialog = new FileChooser();
        saveDialog.setTitle("Export Compiled Player Asset");
        saveDialog.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Visual Asset Format", "*.png"));

        String gamerNameIndex = AuthController.currentUser != null ? AuthController.currentUser.getIgn() : "Operator";
        saveDialog.setInitialFileName(
                gamerNameIndex + "_" + selectedTemplate.getTemplateName().replaceAll("\\s", "") + "_AI_Render.png");

        File fileOutputTarget = saveDialog.showSaveDialog(portfolioScreenStage);

        if (fileOutputTarget != null) {
            new Thread(() -> {
                try {
                    // PURE JAVA NIO FILE WRITE
                    java.nio.file.Files.write(fileOutputTarget.toPath(), currentAiRenderedImageBytes);

                    Platform.runLater(() -> {
                        showNotification("Export Confirmed", "Asset successfully saved to your disk.", false);
                    });
                } catch (Exception downloadErr) {
                    Platform.runLater(() -> {
                        Alert faultAlert = new Alert(Alert.AlertType.ERROR,
                                "Disk I/O failure: Could not save the image.", ButtonType.OK);
                        faultAlert.showAndWait();
                    });
                }
            }).start();
        }
    }

    private Pane createFadedBackgroundCarousel(double width, double height) {
        Pane root = new Pane();
        root.setPrefSize(width, height);

        // These are the background placeholders for the idle animation
        String[] paths = {
                "/assets/templates/overall_template.png",
                "/assets/templates/match_template.png",
                "/assets/templates/mvp_template.png"
        };

        List<ImageView> views = new ArrayList<>();
        for (String path : paths) {
            try {
                if (getClass().getResource(path) != null) {
                    Image img = new Image(getClass().getResourceAsStream(path));
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(width);
                    iv.setFitHeight(height);
                    iv.setPreserveRatio(false); // Stretch to fill background safely
                    views.add(iv);
                }
            } catch (Exception e) {
                System.err.println("Carousel background asset skipped.");
            }
        }

        if (views.isEmpty())
            return root;

        HBox filmstrip = new HBox();
        filmstrip.getChildren().addAll(views);
        filmstrip.setOpacity(0.12); // VERY FADED - just a subtle background texture

        root.getChildren().add(filmstrip);

        // Infinite sliding animation
        if (views.size() > 1) {
            final int[] currentIndex = { 0 };
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3.5), event -> {
                TranslateTransition slide = new TranslateTransition(Duration.millis(800), filmstrip);
                slide.setByX(-width);
                slide.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

                slide.setOnFinished(e -> {
                    currentIndex[0] = (currentIndex[0] + 1) % views.size();
                    Node firstImage = filmstrip.getChildren().remove(0);
                    filmstrip.getChildren().add(firstImage);
                    filmstrip.setTranslateX(0); // Snap back seamlessly
                });
                slide.play();
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        }

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