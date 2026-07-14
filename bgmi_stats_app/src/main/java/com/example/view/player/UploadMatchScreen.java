package com.example.view.player;

import com.example.model.player.MatchExtractionResultModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;
import com.example.controller.AuthController;
import com.example.controller.admin.NotificationController;
import com.example.controller.gemini.GeminiVisionController;
import com.example.controller.player.MatchController;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UploadMatchScreen {

    // VARIABLES
    Scene uploadMatchScreenScene;
    Stage uploadMatchScreenStage;

    // State & UI Components
    private List<File> uploadedImages = new ArrayList<>();
    private FlowPane imagesContainer;
    private VBox emptyDropState;

    // Advanced View States
    private StackPane resultsContainer;
    private VBox skeletonLoaderView;
    private VBox actualTableView;
    private VBox resultsTableContainer;

    private StackPane aiLoaderNode;

    private MFXButton processButton;
    private HBox dynamicStepperContainer;
    private MFXButton saveMatchBtn;

    private MFXButton addFilesBtn;

    private VBox toastContainer;

    // LOGICAL VARIABLES (VARIABLES FOR UI HANDLING ON DATA)
    private MFXTextField matchNameField;
    private ComboBox<String> typeCombo;
    private MatchExtractionResultModel currentAiResult;

    // METHODS
    public void setUploadMatchScreenScene(Scene uploadMatchScreenScene) {
        this.uploadMatchScreenScene = uploadMatchScreenScene;
    }

    public void setUploadMatchScreenStage(Stage uploadMatchScreenStage) {
        this.uploadMatchScreenStage = uploadMatchScreenStage;
    }

    /*
     * This is the start method for the UPLOAD MATCH SCREEN. This method is called
     * from Sidebar to make this screen avalable for the users to see UI.
     */
    public BorderPane startUploadMatchScreen() {

        ScrollPane scroller = new ScrollPane();
        scroller.setFitToWidth(true);
        scroller.setContent(createUploadMatchContent());
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        toastContainer = new VBox(15);
        toastContainer.setAlignment(Pos.BOTTOM_RIGHT);
        toastContainer.setPadding(new Insets(30));
        toastContainer.setPickOnBounds(false); // Let clicks pass through empty space to the UI below

        StackPane contentOverlay = new StackPane();
        contentOverlay.getChildren().addAll(scroller, toastContainer);

        root.setCenter(contentOverlay);
        return root;
    }

    /*
     * This is the method which calls other UI HELPER Methods to show the UI.
     */
    private VBox createUploadMatchContent() {
        VBox container = new VBox(30);
        container.setPadding(new Insets(10, 20, 50, 0));

        // 1. PAGE TITLE & HEADER
        Text title = new Text("Match Upload");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 42));
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

        GamerVaultAnimations.shimmerEffect(title, GamerVaultStyles.TEXT_PRIMARY, GamerVaultStyles.ACCENT_CYAN, 4.0);
        GamerVaultAnimations.fadeInUp(title, 0, 500);

        HBox subHeaderRow = getHeaderBox();

        // CONFIGURATION ROW
        HBox configRow = createConfigurationRow();
        GamerVaultAnimations.fadeInUp(configRow, 150, 500);

        // 3. MAIN WORKSPACE (Left: Upload/Guidance | Right: AI Context/Process)
        HBox workspaceRow = new HBox(40);

        VBox leftColumn = createLeftUploadColumn();
        VBox rightColumn = createRightAIColumn();

        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        rightColumn.setPrefWidth(380);

        workspaceRow.getChildren().addAll(leftColumn, rightColumn);
        GamerVaultAnimations.fadeInUp(workspaceRow, 300, 600);

        // 4. DYNAMIC RESULTS AREA (Skeleton -> Table)
        resultsContainer = new StackPane();
        resultsContainer.setAlignment(Pos.TOP_CENTER);

        skeletonLoaderView = createSkeletonTableLoader();
        actualTableView = createResultsTableSection();

        skeletonLoaderView.setVisible(false);
        actualTableView.setVisible(false);

        resultsContainer.getChildren().addAll(skeletonLoaderView, actualTableView);
        GamerVaultAnimations.fadeInUp(resultsContainer, 450, 600);

        container.getChildren().addAll(title, subHeaderRow, configRow, workspaceRow, resultsContainer);
        return container;
    }

    /**
     * This method is used to apply premium styles to the MFX text inputs
     * 
     * This makes the complete textFields to style as per our background and UI.
     */
    private void applyPremiumMFXInputStyle(MFXTextField field) {
        field.setTextFill(Color.WHITE); // Force text typed by user to be white
        field.setStyle(
                "-mfx-main: " + GamerVaultStyles.ACCENT_PURPLE + ";" +
                        "-fx-prompt-text-fill: " + GamerVaultStyles.TEXT_MUTED + ";" +
                        "-fx-background-color: transparent;" // Clear outer background
        );
        field.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    // FIX 1 & 2: The actual input field background and typing text color
                    Node innerNativeField = field.lookup(".text-field");
                    if (innerNativeField != null) {
                        innerNativeField.setStyle(
                                "-fx-background-color: #1A1F35;" +
                                        "-fx-text-fill: white;" +
                                        "-fx-text-inner-color: white;" +
                                        "-fx-background-radius: 6;" + "-fx-border-color: rgba(255, 255, 255, 0.24);"
                                        + "-fx-border-radius: 6;");
                    }

                    // FIX 3: The floating border title color
                    Node floatingText = field.lookup(".floating-text");
                    if (floatingText != null) {
                        floatingText.setStyle("-fx-text-fill: white;");
                    }
                });
            }
        });
    }

    /**
     * This method is used to apply premium styles to the MFX dropdowns
     * 
     * This makes the complete dropdown to style as per our background and UI.
     */

    private HBox createConfigurationRow() {
        HBox configRow = new HBox(25);
        configRow.setAlignment(Pos.CENTER_LEFT);
        configRow.setPadding(new Insets(25));
        GamerVaultStyles.applyInteractiveGlassCard(configRow, GamerVaultStyles.ACCENT_PURPLE);

        Text configTitle = new Text("BGMI");
        configTitle.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        configTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        configTitle.setRotate(-90);

        // 1. MFX Match Name Field (Keep this as it works fine)
        matchNameField = new MFXTextField();
        matchNameField.setFloatMode(FloatMode.BORDER);
        matchNameField.setFloatingText("Match Name / Title");
        matchNameField.setPromptText("e.g., Semi-Finals Game 1");
        matchNameField.setPrefWidth(320);
        matchNameField.setPrefHeight(50);
        applyPremiumMFXInputStyle(matchNameField);

        // 2. STANDARD JavaFX ComboBox (Bypasses MaterialFX Popup Bugs entirely)
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("TDM Match", "Classic / Compi Match");
        typeCombo.setValue("Classic / Compi Match"); // Set default so text is immediately visible
        typeCombo.setPrefWidth(250);
        typeCombo.setPrefHeight(50);

        // Apply dark base style to the combo box itself
        typeCombo.setStyle(
                "-fx-background-color: " + GamerVaultStyles.INPUT_BG + ";" +
                        "-fx-background-radius: 6;" +
                        "-fx-border-color: rgba(255, 255, 255, 0.24);" +
                        "-fx-border-radius: 6;");

        // Force the main selected text to be white
        typeCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setTextFill(Color.WHITE);
                    setFont(Font.font("Arial", FontWeight.BOLD, 13));
                    setStyle("-fx-background-color: transparent; -fx-padding: 5 0 0 10;");
                }
            }
        });

        // Force the dropdown menu (popup) to be dark with hover effects
        typeCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #1A1F35;");
                } else {
                    setText(item);
                    setTextFill(Color.WHITE);
                    setFont(Font.font("Arial", 13));
                    setStyle("-fx-background-color: #1A1F35; -fx-padding: 10 15 10 15;");

                    setOnMouseEntered(e -> setStyle(
                            "-fx-background-color: rgba(139,92,246,0.3); -fx-padding: 10 15 10 15; -fx-cursor: hand;"));
                    setOnMouseExited(e -> setStyle("-fx-background-color: #1A1F35; -fx-padding: 10 15 10 15;"));
                }
            }
        });

        // Wrap it with a title to perfectly match the MFX floating text look
        VBox comboWrapper = new VBox(5);
        Text comboLabel = new Text("Match Type");
        comboLabel.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        comboLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        comboWrapper.getChildren().addAll(comboLabel, typeCombo);

        configRow.getChildren().addAll(configTitle, matchNameField, comboWrapper);
        return configRow;
    }

    /*
     * LEFT COLUMN: UPLOAD & GUIDANCE
     * 
     */
    private VBox createLeftUploadColumn() {
        VBox leftCol = new VBox(20);

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Text sectionTitle = new Text("Provide Match Screenshots");
        sectionTitle.setFill(Color.WHITE);
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // MFX Browse Button
        addFilesBtn = new MFXButton("Browse Images  📁");
        addFilesBtn.setPrefHeight(45);
        addFilesBtn.setPrefWidth(180);
        addFilesBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: " + GamerVaultStyles.ACCENT_PURPLE + "; " +
                        "-fx-border-radius: 8; " +
                        "-fx-text-fill: white; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-weight: bold;");

        // BROWSE FILES ON CLICK CALL
        addFilesBtn.setOnAction(e -> {
            handleFilePicker(e);
        });
        GamerVaultAnimations.scaleOnHoverAndPress(addFilesBtn, 1.03);

        // addFilesBtn.setOnMouseEntered(e -> addFilesBtn.setStyle(
        // "-fx-background-color: rgba(139,92,246,0.15); -fx-border-color: " +
        // GamerVaultStyles.ACCENT_PURPLE
        // + "; -fx-border-radius: 8; -fx-text-fill: white; -fx-cursor: hand;
        // -fx-font-weight: bold;"));
        // addFilesBtn.setOnMouseExited(e -> addFilesBtn
        // .setStyle("-fx-background-color: transparent; -fx-border-color: " +
        // GamerVaultStyles.ACCENT_PURPLE
        // + "; -fx-border-radius: 8; -fx-text-fill: white; -fx-cursor: hand;
        // -fx-font-weight: bold;"));

        titleRow.getChildren().addAll(sectionTitle, spacer, addFilesBtn);

        // DUMMY Screnshot for user/player to know what type of images need to be
        // uploaded to the system
        Text guidanceText = new Text("Required Screenshots for AI Extraction:");
        guidanceText.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        guidanceText.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        HBox guidanceCards = new HBox(15);
        guidanceCards.getChildren().addAll(
                createGuidanceMockup("Final Standings", "🏆",
                        "Shows placement & survival time", "bgmi_images/result.jpeg"),
                createGuidanceMockup("Detailed Stats", "📊",
                        "Shows kills, damage, & assists", "bgmi_images/detailed.jpeg"));

        // Dynamic Container for actual uploaded images
        imagesContainer = new FlowPane(15, 15);
        imagesContainer.setPadding(new Insets(10, 0, 10, 0));

        // Empty-state placeholder — shown only while no screenshots have been added yet
        emptyDropState = createEmptyDropState();

        leftCol.getChildren().addAll(titleRow, guidanceText, guidanceCards, emptyDropState, imagesContainer);
        return leftCol;
    }

    /*
     * Purely visual empty-state shown before any screenshots are uploaded, so the
     * upload target area has a clear visual presence instead of blank space.
     * Visibility is toggled inside updateImageContainerUI() based on whether
     * uploadedImages is empty — no file-handling logic lives here.
     */
    private VBox createEmptyDropState() {
        VBox placeholder = new VBox(10);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setPrefHeight(160);
        placeholder.setPadding(new Insets(20));
        placeholder.setStyle(
                "-fx-background-color: rgba(255,255,255,0.015); " +
                        "-fx-border-color: rgba(255,255,255,0.12); " +
                        "-fx-border-style: dashed; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 14; " +
                        "-fx-background-radius: 14;");

        Text icon = new Text("🖼️");
        icon.setFont(Font.font(30));
        GamerVaultAnimations.animateFloating(icon, 2.2, -4, 4);

        Text label = new Text("No screenshots added yet");
        label.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        label.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        Text hint = new Text("Click Browse Images above to get started");
        hint.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        hint.setFont(Font.font("Arial", FontWeight.NORMAL, 11));

        placeholder.getChildren().addAll(icon, label, hint);
        return placeholder;
    }

    /*
     * This method creates a static Mockup image / reference images that user will
     * be uploading.
     * This is a static cards and nothing will change in them in the UI.
     * Creating card for dummy upload screenshot.
     */
    private StackPane createGuidanceMockup(String titleStr, String iconStr, String descStr, String imagePath) {
        StackPane cardRoot = new StackPane();
        // INCREASED SIZE: 260x160
        cardRoot.setPrefSize(360, 220);

        // Glow
        DropShadow ambientGlow = new DropShadow();
        ambientGlow.setBlurType(BlurType.GAUSSIAN);
        ambientGlow.setColor(Color.web(GamerVaultStyles.ACCENT_CYAN, 0.15));
        ambientGlow.setRadius(15);
        ambientGlow.setSpread(0.02);
        cardRoot.setEffect(ambientGlow);

        Rectangle baseShape = new Rectangle(360, 220);
        baseShape.setArcWidth(16);
        baseShape.setArcHeight(16);

        StackPane clippedContent = new StackPane();
        clippedContent.setClip(baseShape);

        // ADDED BORDER: 2px solid cyan border around the guidance mockups
        clippedContent.setStyle("-fx-background-color: #121626; " +
                "-fx-border-color: " + GamerVaultStyles.ACCENT_CYAN + "; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 16;");

        // Using ImagePattern to ensure it perfectly acts as the container base
        Image img = new Image(getClass().getResource("/assets/" + imagePath).toExternalForm());
        Rectangle imageCover = new Rectangle(360, 220);
        imageCover.setFill(new ImagePattern(img));

        StackPane imageWrapper = new StackPane(imageCover);
        StackPane.setAlignment(imageWrapper, Pos.TOP_CENTER);

        // Gradient Fade
        Rectangle fadeOverlay = new Rectangle(360, 220);
        LinearGradient fade = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(0.3, Color.TRANSPARENT),
                new Stop(0.8, Color.web("#121626")),
                new Stop(1, Color.web("#121626")));
        fadeOverlay.setFill(fade);

        // Text Content Layout Tweaked for the wider size
        VBox content = new VBox(6);
        content.setAlignment(Pos.BOTTOM_CENTER);
        content.setPadding(new Insets(0, 15, 12, 15));

        Text icon = new Text(iconStr);
        icon.setFont(Font.font(20));

        Text title = new Text(titleStr);
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Text desc = new Text(descStr);
        desc.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        desc.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        desc.setWrappingWidth(220);
        desc.setTextAlignment(TextAlignment.CENTER);

        content.getChildren().addAll(icon, title, desc);

        clippedContent.getChildren().addAll(imageWrapper, fadeOverlay, content);
        cardRoot.getChildren().add(clippedContent);

        attachPremiumCardHover(cardRoot, ambientGlow, imageWrapper);

        return cardRoot;
    }

    /*
     * RIGHT COLUMN
     * This method creates a static UI of the Right side column which contains
     * Functionality information and button to gemini API call.
     */
    private VBox createRightAIColumn() {
        VBox rightCol = new VBox(20);

        VBox aiInfoCard = createAIInfoCard();
        HBox processBox = new HBox(15);
        processBox.setAlignment(Pos.CENTER);

        aiLoaderNode = new StackPane();
        Circle outerPulse = new Circle(18, Color.web(GamerVaultStyles.ACCENT_PURPLE, 0.3));
        outerPulse.setEffect(new GaussianBlur(8));
        Circle innerCore = new Circle(10, Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
        aiLoaderNode.getChildren().addAll(outerPulse, innerCore);
        aiLoaderNode.setVisible(false);

        // Bind the continuous animations from your GamerVaultAnimations class
        GamerVaultAnimations.pulseGlow(outerPulse, 0.8);
        GamerVaultAnimations.pulseGlow(innerCore, 1.2);

        processButton = new MFXButton("START MATCH ANALYSIS ✨");
        processButton.setPrefHeight(60);
        processButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(processButton, Priority.ALWAYS);
        processButton.setStyle(
                "-fx-background-color: linear-gradient(to right, " + GamerVaultStyles.ACCENT_PURPLE + ", "
                        + GamerVaultStyles.ACCENT_PURPLE_DARK + "); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 10;");

        // --- NEW: Premium Hover / Lift applied ---
        GamerVaultAnimations.applyPremiumHover(processButton, GamerVaultStyles.ACCENT_PURPLE);
        attachRippleEffect(processButton, "#FFFFFF");

        // START AND PROCESS GEMINI ACTION BUTTON
        processButton.setOnAction(e -> {
            System.out.print("Process button clicked");
            handleProcessExecution();
        });

        processBox.getChildren().addAll(aiLoaderNode, processButton);
        rightCol.getChildren().addAll(aiInfoCard, processBox);
        return rightCol;
    }

    /*
     * EXECUTION LOGIC & ADVANCED ANIMATIONS
     * This method handles the execution of the process and advanced animations.
     */
    private void handleProcessExecution() {
        // CHECKS IF USER HAS UPLOADED IMAGES ELSE SHOWS ERROR
        if (uploadedImages.isEmpty()) {
            GamerVaultAnimations.shakeOnError(addFilesBtn);
            showNotification("Missing Screenshots", "Please upload screenshots before processing.", true);
            return;
        }

        aiLoaderNode.setVisible(true);
        processButton.setText("PROCESSING DATA...");
        processButton.setDisable(true);
        actualTableView.setVisible(false);
        skeletonLoaderView.setVisible(true);

        // Change the UI step from 1->2
        dynamicStepperContainer.getChildren().clear();
        dynamicStepperContainer.getChildren().add(createStepsBox(2));

        // REAL GEMINI API CALL IN BACKGROUND THREAD
        new Thread(() -> {
            // Heavy lifting: Call Gemini API
            MatchExtractionResultModel result = GeminiVisionController.sendGeminiRequestData(uploadedImages);

            // 3. Push the results back to the UI thread
            Platform.runLater(() -> {
                currentAiResult = result;

                // Reset loading UI
                aiLoaderNode.setVisible(false);
                processButton.setText("START MATCH ANALYSIS ✨");
                processButton.setDisable(false);

                if (currentAiResult != null && currentAiResult.isExtractionSuccessful()) {
                    if (resultsTableContainer.getChildren().size() > 1) {
                        resultsTableContainer.getChildren().remove(1, resultsTableContainer.getChildren().size());
                    }

                    HBox dataRow = createTableRow(
                            currentAiResult.getMap(),
                            currentAiResult.getGameMode(),
                            "#" + currentAiResult.getTeamPlacement(),
                            String.valueOf(currentAiResult.getKills()),
                            String.valueOf(currentAiResult.getAssists()),
                            String.valueOf(currentAiResult.getDamage()),
                            currentAiResult.getSurvivalTime(),
                            String.valueOf(currentAiResult.getRating()),
                            false);
                    resultsTableContainer.getChildren().add(dataRow);
                    GamerVaultAnimations.fadeInUp(dataRow, 100, 450);

                    skeletonLoaderView.setVisible(false);
                    actualTableView.setVisible(true);
                    actualTableView.setOpacity(0);
                    FadeTransition ft = new FadeTransition(Duration.millis(600), actualTableView);
                    ft.setToValue(1.0);
                    ft.play();

                    dynamicStepperContainer.getChildren().clear();
                    dynamicStepperContainer.getChildren().add(createStepsBox(3));

                    showNotification("Extraction Complete", "Gemini successfully normalized match data.", false);
                } else {
                    skeletonLoaderView.setVisible(false);
                    dynamicStepperContainer.getChildren().clear();
                    dynamicStepperContainer.getChildren().add(createStepsBox(1));

                    showNotification("Extraction Failed", "Ensure images are clear BGMI screenshots.", true);
                }
            });
        }).start();
    }

    /*
     * SKELETON LOADER UI (SHIMMER EFFECT)
     */
    private VBox createSkeletonTableLoader() {
        VBox skeletonBox = new VBox(15);
        skeletonBox.setPadding(new Insets(20));
        GamerVaultStyles.applyGlassCard(skeletonBox);

        HBox header = new HBox(15);
        header.getChildren().addAll(
                createShimmerBlock(120, 20),
                createShimmerBlock(120, 20),
                createShimmerBlock(80, 20),
                createShimmerBlock(80, 20));

        skeletonBox.getChildren().add(header);

        for (int i = 0; i < 4; i++) {
            HBox row = new HBox(15);
            row.getChildren().addAll(
                    createShimmerBlock(120, 35),
                    createShimmerBlock(120, 35),
                    createShimmerBlock(80, 35),
                    createShimmerBlock(80, 35));
            skeletonBox.getChildren().add(row);
        }

        return skeletonBox;
    }

    private Rectangle createShimmerBlock(double width, double height) {
        Rectangle block = new Rectangle(width, height);
        block.setArcWidth(8);
        block.setArcHeight(8);
        block.setFill(Color.web("#1A1F35"));

        FadeTransition shimmer = new FadeTransition(Duration.millis(800), block);
        shimmer.setFromValue(0.3);
        shimmer.setToValue(0.8);
        shimmer.setCycleCount(Animation.INDEFINITE);
        shimmer.setAutoReverse(true);
        shimmer.play();

        return block;
    }

    /*
     * RESULTS TABLE SECTION
     */
    private VBox createResultsTableSection() {
        VBox tableContainer = new VBox(15);
        tableContainer.setPadding(new Insets(20));
        GamerVaultStyles.applyGlassCard(tableContainer);

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Text tableTitle = new Text("Normalized Match Metrics");
        tableTitle.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        saveMatchBtn = new MFXButton("Confirm & Save to Vault");
        saveMatchBtn.setPrefHeight(32);
        saveMatchBtn.setPadding(new Insets(0, 20, 0, 20));
        saveMatchBtn.setStyle("-fx-background-color: " + GamerVaultStyles.ACCENT_GREEN
                + "; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-background-radius: 6; "
                + "-fx-font-size: 12px; -fx-cursor: hand;");

        GamerVaultAnimations.applyPremiumHover(saveMatchBtn, GamerVaultStyles.ACCENT_GREEN);
        attachRippleEffect(saveMatchBtn, "#000000");
        saveMatchBtn.setOnAction(e -> saveMatchToDatabase());

        titleRow.getChildren().addAll(tableTitle, spacer, saveMatchBtn);

        // --- CUSTOM LEADERBOARD-STYLE TABLE UI ---
        resultsTableContainer = new VBox(8); // 8px spacing between rows
        resultsTableContainer.setPadding(new Insets(10));
        resultsTableContainer.setStyle(
                "-fx-background-color: #0A0F18; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: rgba(255,255,255,0.05); " +
                        "-fx-border-radius: 8;");

        // Add Header Row
        HBox headerRow = createTableRow("MAP", "MODE", "PLACEMENT", "KILLS", "ASSISTS", "DAMAGE", "SURVIVED", "RATING",
                true);
        resultsTableContainer.getChildren().add(headerRow);

        tableContainer.getChildren().addAll(titleRow, resultsTableContainer);
        return tableContainer;
    }

    // Helper Method 1: Creates a full row (used for both Headers and Data)
    private HBox createTableRow(
            String map,
            String mode,
            String placement,
            String kills,
            String assists,
            String damage,
            String survivalTime,
            String rating,
            boolean isHeader) {

        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefHeight(45);
        row.setPadding(new Insets(0, 15, 0, 15));

        if (!isHeader) {
            row.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 6;");
            GamerVaultAnimations.scaleOnHover(row, 1.01);
        }

        Font font = Font.font("Arial", isHeader ? FontWeight.BOLD : FontWeight.NORMAL, isHeader ? 15 : 17);
        Color textColor = isHeader ? Color.web(GamerVaultStyles.TEXT_MUTED) : Color.web("#E2E8F0");

        // Adjusted widths to fit the new columns perfectly
        HBox mapBox = createCell(map, font, isHeader ? textColor : Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT), 100,
                Pos.CENTER_LEFT);
        HBox modeBox = createCell(mode, font, textColor, 350, Pos.CENTER_LEFT);

        HBox placeBox = createCell(placement, font, isHeader ? textColor : Color.web("#F59E0B"), 120, Pos.CENTER);
        HBox killsBox = isHeader
                ? createCell(kills, font, textColor, 70, Pos.CENTER)
                : createAnimatedCell(kills, font, Color.web("#EF4444"), 70, Pos.CENTER);

        // NEW: Assists Cell (Cyan accent for support stats)
        HBox assistsBox = isHeader
                ? createCell(assists, font, textColor, 70, Pos.CENTER)
                : createAnimatedCell(assists, font, Color.web(GamerVaultStyles.ACCENT_CYAN), 70, Pos.CENTER);

        HBox dmgBox = isHeader
                ? createCell(damage, font, textColor, 80, Pos.CENTER)
                : createAnimatedCell(damage, font, textColor, 80, Pos.CENTER);

        // NEW: Survival Time Cell
        HBox timeBox = createCell(survivalTime, font, textColor, 90, Pos.CENTER);

        HBox ratingBox = isHeader
                ? createCell(rating, font, textColor, 80, Pos.CENTER_RIGHT)
                : createAnimatedCell(rating, font, Color.web(GamerVaultStyles.ACCENT_GREEN), 80, Pos.CENTER_RIGHT);

        row.getChildren().addAll(mapBox, modeBox, SizedBox.width(15), placeBox, SizedBox.width(15), killsBox,
                SizedBox.width(15), assistsBox, SizedBox.width(15), dmgBox, SizedBox.width(15), timeBox,
                ratingBox);
        return row;
    }

    /*
     * Sibling to createCell(), used only for the numeric data-row stat values
     * (Kills, Assists, Damage, Rating). Counts up from 0 to the target value
     * purely for visual reveal. If the text isn't a clean parseable number
     * (e.g. "N/A"), the catch block leaves it exactly as createCell would have
     * shown it - identical fallback behavior, no visual difference at all.
     * The very last frame always sets the EXACT original string, guaranteeing
     * the settled value is byte-for-byte what createCell would have produced.
     */
    private HBox createAnimatedCell(String text, Font font, Color color, double width, Pos alignment) {
        HBox box = new HBox();
        box.setAlignment(alignment);
        box.setPrefWidth(width);
        box.setMinWidth(width);

        Text textNode = new Text(text != null ? text : "N/A");
        textNode.setFont(font);
        textNode.setFill(color);
        box.getChildren().add(textNode);

        try {
            double target = Double.parseDouble(text.trim());
            boolean isWhole = target == Math.floor(target);
            textNode.setText(isWhole ? "0" : "0.0");

            Timeline countUp = new Timeline();
            int steps = 20;
            for (int i = 1; i <= steps; i++) {
                double progress = i / (double) steps;
                double value = target * progress;
                String frameText = isWhole ? String.valueOf(Math.round(value)) : String.format("%.1f", value);
                countUp.getKeyFrames().add(new KeyFrame(
                        Duration.millis(500 * progress), e -> textNode.setText(frameText)));
            }
            // Final guaranteed frame: the exact original string, no rounding drift
            countUp.getKeyFrames()
                    .add(new KeyFrame(Duration.millis(520), e -> textNode.setText(text)));
            countUp.play();
        } catch (Exception ignored) {
            // Not a plain number - textNode already shows the original text untouched.
        }

        return box;
    }

    // Helper Method 2: Creates individual cells (matching LeaderboardScreen
    // architecture)
    private HBox createCell(String text, Font font, Color color, double width, Pos alignment) {
        HBox box = new HBox();
        box.setAlignment(alignment);
        box.setPrefWidth(width);
        box.setMinWidth(width);

        Text textNode = new Text(text != null ? text : "N/A");
        textNode.setFont(font);
        textNode.setFill(color);

        box.getChildren().add(textNode);
        return box;
    }

    // THE REAL BACKGROUND SAVING LOGIC

    /*
     * FILE HANDLING & DYNAMIC UI
     */
    private void handleFilePicker(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Match Screenshots");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        Node source = (Node) event.getSource();
        Stage currentStage = (Stage) source.getScene().getWindow();

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(currentStage);

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            uploadedImages.addAll(selectedFiles);
            updateImageContainerUI();
        }
    }

    /*
     * This method creates the UI for uploaded images of the matches.
     * User uploaded images with file name from file chooser are seen in UI with
     * this method.
     */
    private void updateImageContainerUI() {
        imagesContainer.getChildren().clear();

        if (emptyDropState != null) {
            emptyDropState.setVisible(uploadedImages.isEmpty());
            emptyDropState.setManaged(uploadedImages.isEmpty());
        }

        for (File file : uploadedImages) {
            StackPane cardRoot = new StackPane();
            // INCREASED SIZE: 320x200
            cardRoot.setPrefSize(360, 220);

            // Ambient Glow
            DropShadow ambientGlow = new DropShadow();
            ambientGlow.setBlurType(BlurType.GAUSSIAN);
            ambientGlow.setColor(Color.web(GamerVaultStyles.ACCENT_CYAN, 0.15));
            ambientGlow.setRadius(20);
            ambientGlow.setSpread(0.05);
            cardRoot.setEffect(ambientGlow);

            // Base Clip Mask for rounded corners
            Rectangle baseShape = new Rectangle(360, 220);
            baseShape.setArcWidth(16);
            baseShape.setArcHeight(16);

            StackPane clippedContent = new StackPane();
            clippedContent.setClip(baseShape);
            clippedContent.setStyle("-fx-background-color: #121626;");

            try {
                // THE FIX: Use ImagePattern on a Rectangle instead of ImageView to prevent
                // bounding/squishing
                Image img = new Image(file.toURI().toString());
                Rectangle imageCover = new Rectangle(360, 220);
                imageCover.setFill(new ImagePattern(img));

                StackPane imageWrapper = new StackPane(imageCover);
                StackPane.setAlignment(imageWrapper, Pos.TOP_CENTER);

                // Gradient Fade
                Rectangle fadeOverlay = new Rectangle(360, 220);
                LinearGradient fade = new LinearGradient(
                        0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.TRANSPARENT),
                        new Stop(0.3, Color.TRANSPARENT),
                        new Stop(0.8, Color.web("#121626")),
                        new Stop(1, Color.web("#121626")));
                fadeOverlay.setFill(fade);

                // Data Pane
                VBox dataPane = new VBox(10);
                dataPane.setAlignment(Pos.BOTTOM_CENTER);
                dataPane.setPadding(new Insets(0, 15, 20, 15));

                Text fileName = new Text(file.getName());
                fileName.setFill(Color.WHITE);
                fileName.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                fileName.setWrappingWidth(290);
                fileName.setTextAlignment(TextAlignment.CENTER);

                MFXButton removeBtn = new MFXButton("Remove Evidence");
                removeBtn.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-border-color: rgba(239, 68, 68, 0.6); -fx-border-radius: 6; " +
                                "-fx-text-fill: #EF4444; -fx-font-size: 11px; -fx-cursor: hand;");
                removeBtn.setOnAction(e -> playRemoveExitThenUpdate(cardRoot, file));

                dataPane.getChildren().addAll(fileName, removeBtn);

                clippedContent.getChildren().addAll(imageWrapper, fadeOverlay, dataPane);
                cardRoot.getChildren().add(clippedContent);

                attachPremiumCardHover(cardRoot, ambientGlow, imageWrapper);

                imagesContainer.getChildren().add(cardRoot);
                GamerVaultAnimations.fadeInUp(cardRoot, 0, 300);

            } catch (Exception ex) {
                System.out.println("Error rendering thumbnail: " + ex.getMessage());
            }
        }
    }

    /*
     * Plays a quick fade + scale-down exit on a single image card, then performs
     * the exact same removal that used to run instantly: uploadedImages.remove()
     * followed by a full updateImageContainerUI() rebuild. No change to what
     * gets removed or how the list is rebuilt — only when it visually happens.
     */
    /*
     * Plays a quick fade + scale-down exit on a single image card, then performs
     * the exact same removal that used to run instantly.
     */
    private void playRemoveExitThenUpdate(StackPane fileCard, File file) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(220),
                fileCard);
        scaleDown.setToX(0.85);
        scaleDown.setToY(0.85);
        scaleDown.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(220), fileCard);
        fadeOut.setToValue(0);

        ParallelTransition exit = new ParallelTransition(scaleDown, fadeOut);
        exit.setOnFinished(ev -> {
            uploadedImages.remove(file);
            updateImageContainerUI();
        });
        exit.play();
    }

    /*
     * Attaches a Material-style expanding ripple centered on the click point.
     * The ripple circle is added to the button's own parent Pane with
     * setManaged(false), so it never participates in that Pane's layout pass -
     * existing siblings, spacing, and alignment are completely unaffected. This
     * is purely a MOUSE_PRESSED visual listener added alongside whatever
     * setOnAction handler the button already has; it never consumes the event
     * or interferes with the button's normal click/action firing.
     */
    private void attachRippleEffect(Region button, String colorHex) {
        button.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            Node parent = button.getParent();
            if (!(parent instanceof Pane))
                return;
            Pane parentPane = (Pane) parent;

            Circle ripple = new Circle(4, Color.web(colorHex, 0.35));
            ripple.setManaged(false);
            ripple.setMouseTransparent(true);

            Point2D clickInParent = button.localToParent(e.getX(), e.getY());
            ripple.setCenterX(clickInParent.getX());
            ripple.setCenterY(clickInParent.getY());

            parentPane.getChildren().add(ripple);

            double targetRadius = Math.max(button.getWidth(), button.getHeight()) * 0.9;
            Timeline grow = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(ripple.radiusProperty(), 4)),
                    new KeyFrame(Duration.millis(450),
                            new KeyValue(ripple.radiusProperty(), targetRadius,
                                    Interpolator.EASE_OUT)));

            FadeTransition fade = new FadeTransition(Duration.millis(450), ripple);
            fade.setFromValue(0.7);
            fade.setToValue(0);

            ParallelTransition rippleAnim = new ParallelTransition(grow, fade);
            rippleAnim.setOnFinished(ev -> parentPane.getChildren().remove(ripple));
            rippleAnim.play();
        });
    }

    /*
     * This method helps out with the creation of HEADER SECTION OF THE PAGE
     */
    private HBox getHeaderBox() {
        HBox subHeaderRow = new HBox();
        subHeaderRow.setAlignment(Pos.CENTER_LEFT);

        Text robotIconText = new Text("🤖 ");
        robotIconText.setFill(Color.web(GamerVaultStyles.ACCENT_CYAN));
        robotIconText.setFont(Font.font(18));

        Text subtitleText = new Text("Securely process your performance data using Gemini Vision AI.");
        subtitleText.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        subtitleText.setFont(Font.font("Poppins", FontWeight.NORMAL, 16));

        HBox subtitleBox = new HBox(8);
        subtitleBox.setAlignment(Pos.CENTER_LEFT);
        subtitleBox.getChildren().addAll(robotIconText, subtitleText);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        dynamicStepperContainer = new HBox();
        dynamicStepperContainer.getChildren().add(createStepsBox(1));
        subHeaderRow.getChildren().addAll(subtitleBox, spacer, dynamicStepperContainer);

        GamerVaultAnimations.fadeInUp(subHeaderRow, 100, 500);
        return subHeaderRow;
    }

    /*
     * This method consist of the static text data and video playing in it.
     */
    private VBox createAIInfoCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        GamerVaultStyles.applyGlassCard(card);

        GamerVaultAnimations.applyHoverTilt(card);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Text stars = new Text("✨");
        stars.setFill(Color.web(GamerVaultStyles.ACCENT_ORANGE));
        GamerVaultAnimations.pulseGlow(stars, 2.5);

        Text aiTitle = new Text("AI Intelligence Node");
        aiTitle.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        aiTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        header.getChildren().addAll(stars, aiTitle);

        Text descText = new Text(
                "Our neural engine directly analyzes screenshots, bypassing manual entry. Ensure images are high resolution.");
        descText.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        descText.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        descText.setWrappingWidth(320);
        descText.setLineSpacing(4);

        // // ─── VIDEO PLAYER INTEGRATION ────────────────────────────────────
        // MediaView aiVideoView = new MediaView();
        // try {
        // // Update this path to point to your actual video file inside
        // // src/main/resources/
        // URL videoUrl =
        // getClass().getResource("/assets/bgmi_images/kill_feed_video.mp4");

        // if (videoUrl != null) {
        // Media media = new Media(videoUrl.toExternalForm());
        // MediaPlayer mediaPlayer = new MediaPlayer(media);

        // // Configure playback behavior
        // mediaPlayer.setAutoPlay(true);
        // mediaPlayer.setMute(true); // Usually best to mute looping UI videos
        // mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop infinitely

        // aiVideoView.setMediaPlayer(mediaPlayer);

        // // Size it to fit the card (matching your text wrapping width)
        // aiVideoView.setFitWidth(320);
        // aiVideoView.setPreserveRatio(true);

        // // Apply rounded corners to the video to match the premium UI
        // Rectangle clip = new Rectangle(320, 180); // Height will adapt, but clip
        // ensures corners are cut
        // clip.setArcWidth(12);
        // clip.setArcHeight(12);
        // aiVideoView.setClip(clip);

        // // Optional: Fade in the video when the card loads
        // GamerVaultAnimations.fadeInUp(aiVideoView, 200, 500);
        // } else {
        // System.out.println("Video asset not found. Please check the file path.");
        // }
        // } catch (Exception e) {
        // System.out.println("Error initializing video: " + e.getMessage());
        // }

        VBox checkListBox = new VBox(10);
        checkListBox.getChildren().addAll(
                new HBox(40, createCheckmarkItem("Kills"), createCheckmarkItem("Survival Time")),
                new HBox(40, createCheckmarkItem("Damage"), createCheckmarkItem("Placement")));

        card.getChildren().addAll(header, descText, SizedBox.height(5), checkListBox);
        return card;
    }

    /*
     * This method creates a checkbox UI which will be static for the UI and will
     * not change according to dynamic things
     */
    private HBox createCheckmarkItem(String labelStr) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPrefWidth(120);

        Text check = new Text("✓");
        check.setFill(Color.web(GamerVaultStyles.ACCENT_GREEN));
        check.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Text label = new Text(labelStr);
        label.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        label.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        item.getChildren().addAll(check, label);
        return item;
    }

    /*
     * This is the stepsBox. It tells about which process is running in UI
     * (processing, etc.).
     */
    private HBox createStepsBox(int currentStep) {
        HBox stepper = new HBox(15);
        stepper.setAlignment(Pos.CENTER);
        stepper.setStyle(
                "-fx-background-color: " + GamerVaultStyles.CARD_BG + "; -fx-background-radius: 12; -fx-border-color: "
                        + GamerVaultStyles.CARD_BORDER + "; -fx-border-radius: 12;");
        stepper.setPadding(new Insets(10, 25, 10, 25));

        Text dash1 = new Text("—");
        dash1.setFill(Color.web(currentStep >= 2 ? GamerVaultStyles.ACCENT_PURPLE_LIGHT : GamerVaultStyles.TEXT_MUTED));
        dash1.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Text dash2 = new Text("—");
        dash2.setFill(Color.web(currentStep >= 3 ? GamerVaultStyles.ACCENT_PURPLE_LIGHT : GamerVaultStyles.TEXT_MUTED));
        dash2.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        stepper.getChildren().addAll(
                createStepIndicator("1", "Upload", currentStep == 1, currentStep > 1),
                dash1,
                createStepIndicator("2", "AI Process", currentStep == 2, currentStep > 2),
                dash2,
                createStepIndicator("3", "Verify", currentStep == 3, false));
        return stepper;
    }

    /*
     * This method is used for creation of each step in createStepBox, It takes
     * the number, text label and active status as input.
     * 
     * This method will indicate and circle it with a glow if it is active and if it
     * is not active, it will just show the number and text.
     */
    private HBox createStepIndicator(String index, String label, boolean isActive, boolean isCompleted) {
        HBox step = new HBox(10);
        step.setAlignment(Pos.CENTER);

        StackPane circle = new StackPane();
        circle.setPrefSize(24, 24);

        String circleBg = GamerVaultStyles.INPUT_BG;
        String borderCol = "rgba(255,255,255,0.1)";
        String txtColor = GamerVaultStyles.TEXT_MUTED;

        if (isActive) {
            circleBg = "rgba(168, 85, 247, 0.2)";
            borderCol = GamerVaultStyles.ACCENT_PURPLE_LIGHT;
            txtColor = "#FFFFFF";
        } else if (isCompleted) {
            circleBg = GamerVaultStyles.ACCENT_PURPLE_LIGHT;
            borderCol = GamerVaultStyles.ACCENT_PURPLE_LIGHT;
            txtColor = "#000000";
        }

        circle.setStyle("-fx-background-color: " + circleBg + "; -fx-background-radius: 50%; "
                + "-fx-border-color: " + borderCol + "; -fx-border-radius: 50%; -fx-border-width: 1.5;");

        Text idxText = new Text(isCompleted ? "✓" : index);
        idxText.setFill(Color.web(txtColor));
        idxText.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        circle.getChildren().add(idxText);

        // Newly-active step gets a quick bounce-in pop so the 1→2→3 progression
        // reads as a live event rather than an instant style swap.
        if (isActive) {
            circle.setScaleX(0.4);
            circle.setScaleY(0.4);
            ScaleTransition pop = new ScaleTransition(Duration.millis(350), circle);
            pop.setToX(1.0);
            pop.setToY(1.0);
            pop.setInterpolator(Interpolator.SPLINE(0.34, 1.0, 0.64, 1.0));
            pop.play();
        }

        Text lblText = new Text(label);
        lblText.setFill(Color.web(isActive || isCompleted ? "#FFFFFF" : GamerVaultStyles.TEXT_MUTED));
        lblText.setFont(Font.font("Arial", isActive ? FontWeight.BOLD : FontWeight.NORMAL, 12));

        step.getChildren().addAll(circle, lblText);
        return step;
    }

    // FUNCTIONALITY METHODS
    private void saveMatchToDatabase() {
        if (currentAiResult == null)
            return;

        String customName = matchNameField.getText();
        String gameType = typeCombo.getValue();

        if (customName == null || customName.isEmpty()) {
            GamerVaultAnimations.shakeOnError(matchNameField);
            showNotification("Missing Metadata", "Please provide a Match Name before saving.", true);
            return;
        }

        // 1. Show loading state and disable button on the main thread
        showNotification("Uploading...", "Saving evidence and stats to Vault...", false);
        if (saveMatchBtn != null) {
            saveMatchBtn.setDisable(true);
        }

        // 2. Start a raw background thread for Firebase Storage & Firestore
        new Thread(() -> {

            // Get user ID
            final String userId = AuthController.currentUser.getUserId();

            // Heavy lifting: Upload images and write to Firestore
            String response = MatchController.handleMatchConfirmation(
                    currentAiResult,
                    customName,
                    gameType,
                    uploadedImages,
                    userId);

            // 3. Push the UI updates back to the main thread
            Platform.runLater(() -> {
                if (response.startsWith("SUCCESS")) {
                    showNotification("Database Status", "Saved successfully to Vault.", false);

                    // Reset the UI completely for the next upload
                    uploadedImages.clear();
                    updateImageContainerUI();
                    actualTableView.setVisible(false);
                    matchNameField.clear();
                    typeCombo.setValue("Classic / Compi Match");
                    currentAiResult = null;

                    dynamicStepperContainer.getChildren().clear();
                    dynamicStepperContainer.getChildren().add(createStepsBox(1));

                    // Trigger the global notification listener for the top bar!
                    NotificationController.sendNotification(
                            "Match Telemetry Uploaded",
                            "Your match '" + customName + "' was successfully parsed by Gemini AI.",
                            "MATCH",
                            userId,
                            "");
                } else {
                    // Show the backend error message in a red toast
                    showNotification("Database Status", response, true);
                }

                if (saveMatchBtn != null) {
                    saveMatchBtn.setDisable(false);
                }
            });
        }).start();
    }

    // --- NEW: Constructed real floating Toast UI ---
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

            // Re-use your built-in notification animation hook
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

    private void attachPremiumCardHover(StackPane cardRoot, DropShadow glow, StackPane imageLayer) {
        cardRoot.setOnMouseEntered(e -> {
            // 1. Lift the card and scale up
            ScaleTransition scale = new ScaleTransition(Duration.millis(250),
                    cardRoot);
            scale.setToX(1.04);
            scale.setToY(1.04);
            scale.setInterpolator(Interpolator.EASE_OUT);

            // 2. Parallax: Move the image slightly independently of the card
            TranslateTransition parallax = new TranslateTransition(
                    Duration.millis(250), imageLayer);
            parallax.setToY(-6); // Image moves up slightly inside the frame
            parallax.setInterpolator(Interpolator.EASE_OUT);

            // 3. Ignite the glow
            Timeline glowAnim = new Timeline(
                    new KeyFrame(Duration.millis(250),
                            new KeyValue(glow.colorProperty(),
                                    Color.web(GamerVaultStyles.ACCENT_CYAN, 0.45)),
                            new KeyValue(glow.radiusProperty(), 35),
                            new KeyValue(glow.spreadProperty(), 0.15)));

            scale.play();
            parallax.play();
            glowAnim.play();
            cardRoot.setCursor(javafx.scene.Cursor.HAND);
        });

        cardRoot.setOnMouseExited(e -> {
            // Reverse everything
            ScaleTransition scale = new ScaleTransition(Duration.millis(250),
                    cardRoot);
            scale.setToX(1.0);
            scale.setToY(1.0);

            TranslateTransition parallax = new TranslateTransition(
                    Duration.millis(250), imageLayer);
            parallax.setToY(0);

            Timeline glowAnim = new Timeline(
                    new KeyFrame(Duration.millis(250),
                            new KeyValue(glow.colorProperty(),
                                    Color.web(GamerVaultStyles.ACCENT_CYAN, 0.15)),
                            new KeyValue(glow.radiusProperty(), 20),
                            new KeyValue(glow.spreadProperty(), 0.05)));

            scale.play();
            parallax.play();
            glowAnim.play();
        });
    }
}