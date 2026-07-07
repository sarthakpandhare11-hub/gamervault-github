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
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
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

    // Advanced View States
    private StackPane resultsContainer;
    private VBox skeletonLoaderView;
    private VBox actualTableView;
    private VBox resultsTableContainer;
    private MFXProgressSpinner progressSpinner;
    private MFXButton processButton;
    private HBox dynamicStepperContainer;
    private MFXButton saveMatchBtn;

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
        root.setCenter(scroller);
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
        MFXButton addFilesBtn = new MFXButton("Browse Images  📁");
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

        addFilesBtn.setOnMouseEntered(e -> addFilesBtn.setStyle(
                "-fx-background-color: rgba(139,92,246,0.15); -fx-border-color: " + GamerVaultStyles.ACCENT_PURPLE
                        + "; -fx-border-radius: 8; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;"));
        addFilesBtn.setOnMouseExited(e -> addFilesBtn
                .setStyle("-fx-background-color: transparent; -fx-border-color: " + GamerVaultStyles.ACCENT_PURPLE
                        + "; -fx-border-radius: 8; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;"));

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

        leftCol.getChildren().addAll(titleRow, guidanceText, guidanceCards, imagesContainer);
        return leftCol;
    }

    /*
     * This method creates a static Mockup image / reference images that user will
     * be uploading.
     * This is a static cards and nothing will change in them in the UI.
     * Creating card for dummy upload screenshot.
     */
    private VBox createGuidanceMockup(String titleStr, String iconStr, String descStr, String image) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(180, 140);
        card.setPadding(new Insets(15));

        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.02); " +
                        "-fx-border-color: rgba(255,255,255,0.15); " +
                        "-fx-border-style: dashed; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12;");

        ImageView imageView = new ImageView(new Image(getClass().getResource("/assets/" + image).toExternalForm()));
        imageView.setFitWidth(350);
        imageView.setFitHeight(350);
        imageView.setPreserveRatio(true);

        Text icon = new Text(iconStr);
        icon.setFont(Font.font(36));
        icon.setFill(Color.web("rgba(255, 255, 255, 0.46)"));

        Text title = new Text(titleStr);
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Text desc = new Text(descStr);
        desc.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        desc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        desc.setWrappingWidth(140);
        desc.setTextAlignment(TextAlignment.CENTER);

        card.getChildren().addAll(imageView, SizedBox.height(3), icon, title, desc);
        return card;
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

        progressSpinner = new MFXProgressSpinner();
        progressSpinner.setPrefSize(40, 40);
        progressSpinner.setStyle("-fx-progress-color: " + GamerVaultStyles.ACCENT_CYAN + ";");
        progressSpinner.setVisible(false);

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
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;");
        DropShadow glow = new DropShadow(20, Color.web(GamerVaultStyles.ACCENT_PURPLE, 0.6));
        processButton.setEffect(glow);
        GamerVaultAnimations.scaleOnPress(processButton);

        // START AND PROCESS GEMINI ACTION BUTTON
        processButton.setOnAction(e -> {
            System.out.print("Process button clicked");
            handleProcessExecution();
        });

        processBox.getChildren().addAll(progressSpinner, processButton);
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
            showNotification("Missing Screenshots", "Please upload screenshots before processing.");
            return;
        }

        progressSpinner.setVisible(true);
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
                progressSpinner.setVisible(false);
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

                    skeletonLoaderView.setVisible(false);
                    actualTableView.setVisible(true);
                    actualTableView.setOpacity(0);
                    FadeTransition ft = new FadeTransition(Duration.millis(600), actualTableView);
                    ft.setToValue(1.0);
                    ft.play();

                    dynamicStepperContainer.getChildren().clear();
                    dynamicStepperContainer.getChildren().add(createStepsBox(3));

                    showNotification("Extraction Complete", "Gemini successfully normalized match data.");
                } else {
                    skeletonLoaderView.setVisible(false);
                    dynamicStepperContainer.getChildren().clear();
                    dynamicStepperContainer.getChildren().add(createStepsBox(1));

                    showNotification("Extraction Failed", "Ensure images are clear BGMI screenshots.");
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
        HBox killsBox = createCell(kills, font, isHeader ? textColor : Color.web("#EF4444"), 70, Pos.CENTER);

        // NEW: Assists Cell (Cyan accent for support stats)
        HBox assistsBox = createCell(assists, font, isHeader ? textColor : Color.web(GamerVaultStyles.ACCENT_CYAN), 70,
                Pos.CENTER);

        HBox dmgBox = createCell(damage, font, textColor, 80, Pos.CENTER);

        // NEW: Survival Time Cell
        HBox timeBox = createCell(survivalTime, font, textColor, 90, Pos.CENTER);

        HBox ratingBox = createCell(rating, font, isHeader ? textColor : Color.web(GamerVaultStyles.ACCENT_GREEN), 80,
                Pos.CENTER_RIGHT);

        row.getChildren().addAll(mapBox, modeBox, SizedBox.width(15), placeBox, SizedBox.width(15), killsBox,
                SizedBox.width(15), assistsBox, SizedBox.width(15), dmgBox, SizedBox.width(15), timeBox,
                ratingBox);
        return row;
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

        for (File file : uploadedImages) {
            VBox fileCard = new VBox(8);
            fileCard.setAlignment(Pos.CENTER);
            fileCard.setPrefSize(240, 260);
            fileCard.setStyle("-fx-background-color: " + GamerVaultStyles.INPUT_BG
                    + "; -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 12;");
            fileCard.setPadding(new Insets(10));

            // Create Thumbnail Container with a clean, hard clipped border layout radius
            StackPane imageWrapper = new StackPane();
            imageWrapper.setPrefSize(220, 185);
            imageWrapper.setMaxSize(220, 185);

            try {
                // Lazy load local file thumbnail URI paths directly to ImageView layer pipeline
                Image img = new Image(file.toURI().toString(), 220, 185, true, true);
                ImageView imageView = new ImageView(img);
                imageView.setFitWidth(220);
                imageView.setFitHeight(185);
                imageView.setPreserveRatio(true);

                // Clip mask to enforce smooth background-radius boundaries on previews
                Rectangle clip = new Rectangle(220, 185);
                clip.setArcWidth(10);
                clip.setArcHeight(10);
                imageView.setClip(clip);

                imageWrapper.getChildren().add(imageView);
            } catch (Exception ex) {
                // Safe fallback indicator block layer if file rendering fails
                Text fallbackIcon = new Text("⚠️");
                fallbackIcon.setFont(Font.font(28));
                imageWrapper.getChildren().add(fallbackIcon);
            }

            Text fileName = new Text(file.getName());
            fileName.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
            fileName.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            fileName.setWrappingWidth(210);
            fileName.setTextAlignment(TextAlignment.CENTER);

            MFXButton removeBtn = new MFXButton("Remove");
            removeBtn.setStyle(
                    "-fx-text-fill: #EF4444; -fx-background-color: rgba(239,68,68,0.1); -fx-border-color: #EF4444; -fx-border-radius: 4; -fx-font-size: 10px; -fx-cursor: hand;");
            removeBtn.setOnAction(e -> {
                uploadedImages.remove(file);
                updateImageContainerUI();
            });

            fileCard.getChildren().addAll(imageWrapper, fileName, removeBtn);
            imagesContainer.getChildren().add(fileCard);

            GamerVaultAnimations.fadeInUp(fileCard, 0, 300);
            GamerVaultAnimations.scaleOnHover(fileCard, 1.05);
        }
    }

    /*
     * ─── HELPER COMPONENTS ───────────────────────────────────────────────────
     */
    private void showNotification(String title, String message) {
        System.out.println("FCM NOTIFICATION -> " + title + ": " + message);
        // TODO; Integrate real Firebase FCM Toast Notification Logic here
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
            showNotification("Missing Metadata", "Please provide a Match Name before saving.");
            return;
        }

        // 1. Show loading state and disable button on the main thread
        showNotification("Uploading...", "Saving evidence and stats to Vault...");
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
                showNotification("Database Status", response);

                if (response.startsWith("SUCCESS")) {
                    // Reset the UI completely for the next upload
                    uploadedImages.clear();
                    updateImageContainerUI();
                    actualTableView.setVisible(false);
                    matchNameField.clear();
                    typeCombo.setValue("Classic / Compi Match");
                    currentAiResult = null;

                    dynamicStepperContainer.getChildren().clear();
                    dynamicStepperContainer.getChildren().add(createStepsBox(1));

                    NotificationController.sendNotification(
                            "Match Telemetry Uploaded",
                            "Your match '" + customName + "' was successfully parsed by Gemini AI.",
                            "MATCH",
                            userId);
                }

                if (saveMatchBtn != null) {
                    saveMatchBtn.setDisable(false);
                }
            });
        }).start();
    }
}