// package com.example.view.player;

// import java.io.File;
// import java.util.List;

// import com.example.controller.AuthController;
// import com.example.controller.gemini.PortfolioGeminiController;
// import com.example.controller.player.MatchController;
// import com.example.model.UserModel;
// import com.example.model.player.MatchModel;
// import com.example.view.util.GamerVaultAnimations;
// import com.example.view.util.GamerVaultStyles;

// import javafx.application.Platform;
// import javafx.geometry.Insets;
// import javafx.geometry.Pos;
// import javafx.scene.Node;
// import javafx.scene.Scene;
// import javafx.scene.control.Alert;
// import javafx.scene.control.Button;
// import javafx.scene.control.ButtonType;
// import javafx.scene.control.Dialog;
// import javafx.scene.control.ScrollPane;
// import javafx.scene.effect.DropShadow;
// import javafx.scene.image.Image;
// import javafx.scene.image.ImageView;
// import javafx.scene.layout.BorderPane;
// import javafx.scene.layout.HBox;
// import javafx.scene.layout.Priority;
// import javafx.scene.layout.Region;
// import javafx.scene.layout.StackPane;
// import javafx.scene.layout.VBox;
// import javafx.scene.paint.Color;
// import javafx.scene.text.Font;
// import javafx.scene.text.FontWeight;
// import javafx.scene.text.Text;
// import javafx.stage.FileChooser;
// import javafx.stage.Stage;

// public class PortfolioGeneratorScreen {

//     // --- SCREEN NAVIGATION BOUNDARIES ---
//     Scene portfolioScreenScene;
//     Stage portfolioScreenStage;

//     // --- REVENUE RUNTIME STATES ---
//     private String selectedTemplateType = "OVERALL"; // Default Type
//     private MatchModel chosenMatchModel = null;
//     private Image currentAiRenderedImage = null;

//     // --- CORE INTERFACE NODES ---
//     private HBox visualSwitchesRow;
//     private VBox matchHistorySelectorBox;
//     private Text feedbackMatchSummaryLabel;
//     private ImageView mainPreviewDisplayArea;
//     private Button processGenerationBtn;
//     private Button saveToDiskBtn;

//     private byte[] currentAiRenderedImageBytes = null;

//     public void setPortfolioScreenScene(Scene portfolioScreenScene) {
//         this.portfolioScreenScene = portfolioScreenScene;
//     }

//     public void setPortfolioScreenStage(Stage portfolioScreenStage) {
//         this.portfolioScreenStage = portfolioScreenStage;
//     }

//     public BorderPane startPortfolioGeneratorScreen() {
//         BorderPane corePane = new BorderPane();
//         corePane.setStyle("-fx-background-color: transparent;");

//         ScrollPane contentScroller = new ScrollPane();
//         contentScroller.setContent(assembleMainScrollerCanvas());
//         GamerVaultStyles.applyStyledScrollPane(contentScroller);
//         contentScroller.setFitToWidth(true);
//         contentScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
//         contentScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

//         corePane.setCenter(contentScroller);
//         return corePane;
//     }

//     private VBox assembleMainScrollerCanvas() {
//         VBox primaryContainer = new VBox(25);
//         primaryContainer.setPadding(new Insets(10, 20, 40, 20));

//         // HEADER BLOCK INJECTION
//         VBox layoutHeader = assembleSectionHeader();

//         // SPLIT LAYOUT PIPELINE
//         HBox coreSplitPanel = new HBox(40);
//         VBox configControlsPanel = assembleConfigurationControlsPanel();
//         VBox realTimePreviewPanel = assembleRealTimePreviewPanel();

//         configControlsPanel.setPrefWidth(460);
//         configControlsPanel.setMinWidth(460);
//         HBox.setHgrow(realTimePreviewPanel, Priority.ALWAYS);

//         coreSplitPanel.getChildren().addAll(configControlsPanel, realTimePreviewPanel);
//         primaryContainer.getChildren().addAll(layoutHeader, coreSplitPanel);
//         return primaryContainer;
//     }

//     private VBox assembleSectionHeader() {
//         Text mainTitle = new Text("Portfolio Engine");
//         mainTitle.setFont(Font.font("Arial", FontWeight.BOLD, 42));
//         mainTitle.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

//         Text secondarySubtitle = new Text(
//                 "Bifurcate your battle telemetry. Click an exact template variant layout profile to synchronize stats with AI generation pipelines.");
//         secondarySubtitle.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
//         secondarySubtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

//         VBox headerGroup = new VBox(5);
//         headerGroup.getChildren().addAll(mainTitle, secondarySubtitle);
//         GamerVaultAnimations.fadeInUp(mainTitle, 0, 400);
//         GamerVaultAnimations.fadeInUp(secondarySubtitle, 100, 400);
//         return headerGroup;
//     }

//     /*
//      * ─── CONFIGURATION LOGIC (LEFT SIDE CONTROLS) ──────────────────────────
//      */
//     private VBox assembleConfigurationControlsPanel() {
//         VBox configurationsCol = new VBox(25);

//         // CARD PANEL 1: VISUAL SELECTION SWATCH MATRIX
//         VBox layoutSelectorWrapper = new VBox(15);
//         layoutSelectorWrapper.setPadding(new Insets(25));
//         GamerVaultStyles.applyGlassCard(layoutSelectorWrapper);

//         Text stepOneLabel = new Text("1. Choose Target Performance Template");
//         stepOneLabel.setFill(Color.WHITE);
//         stepOneLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

//         visualSwitchesRow = new HBox(15);
//         visualSwitchesRow.setAlignment(Pos.CENTER);
//         rebuildVisualSwatchesPanel();

//         layoutSelectorWrapper.getChildren().addAll(stepOneLabel, visualSwitchesRow);

//         // CARD PANEL 2: MATCH DEPENDENCY CONTEXT PICKER
//         matchHistorySelectorBox = new VBox(15);
//         matchHistorySelectorBox.setPadding(new Insets(25));
//         GamerVaultStyles.applyGlassCard(matchHistorySelectorBox);

//         Text stepTwoLabel = new Text("2. Link Telemetry History");
//         stepTwoLabel.setFill(Color.WHITE);
//         stepTwoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

//         feedbackMatchSummaryLabel = new Text("No operational history bound.");
//         feedbackMatchSummaryLabel.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
//         feedbackMatchSummaryLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

//         Button summonMatchHistoryModalBtn = new Button("📂 Scan Match Vault History");
//         summonMatchHistoryModalBtn.setMaxWidth(Double.MAX_VALUE);
//         GamerVaultStyles.applyGhostButton(summonMatchHistoryModalBtn);
//         summonMatchHistoryModalBtn.setOnAction(e -> triggerMatchVaultSelectionDialog());

//         matchHistorySelectorBox.getChildren().addAll(stepTwoLabel, feedbackMatchSummaryLabel,
//                 summonMatchHistoryModalBtn);
//         evaluateDynamicWorkflowVisibility();

//         configurationsCol.getChildren().addAll(layoutSelectorWrapper, matchHistorySelectorBox);
//         GamerVaultAnimations.fadeInUp(configurationsCol, 200, 500);
//         return configurationsCol;
//     }

//     private void rebuildVisualSwatchesPanel() {
//         visualSwitchesRow.getChildren().clear();
//         visualSwitchesRow.getChildren().addAll(
//                 generateVisualTemplateItemNode("OVERALL", "Overall Stats", "/assets/templates/overall_template.png"),
//                 generateVisualTemplateItemNode("MATCH", "Last Match", "/assets/templates/match_template.png"),
//                 generateVisualTemplateItemNode("MVP", "M.V.P Profile", "/assets/templates/mvp_template.png"));
//     }

//     private VBox generateVisualTemplateItemNode(String templateKey, String visibleTitle, String imageAssetPath) {
//         VBox gridCard = new VBox(10);
//         gridCard.setAlignment(Pos.CENTER);
//         gridCard.setPadding(new Insets(12));

//         boolean matchingActiveState = this.selectedTemplateType.equals(templateKey);

//         String standardStyle = "-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 12; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 12;";
//         String activatedStyle = "-fx-background-color: rgba(139,92,246,0.1); -fx-background-radius: 12; -fx-cursor: hand; -fx-border-color: "
//                 + GamerVaultStyles.ACCENT_PURPLE + "; -fx-border-width: 2; -fx-border-radius: 12;";

//         gridCard.setStyle(matchingActiveState ? activatedStyle : standardStyle);
//         if (matchingActiveState) {
//             gridCard.setEffect(new DropShadow(15, Color.web(GamerVaultStyles.ACCENT_PURPLE, 0.4)));
//         }

//         ImageView assetPreview = new ImageView();
//         assetPreview.setFitWidth(105);
//         assetPreview.setFitHeight(145);
//         assetPreview.setPreserveRatio(true);
//         try {
//             if (getClass().getResource(imageAssetPath) != null) {
//                 assetPreview.setImage(new Image(getClass().getResourceAsStream(imageAssetPath)));
//             }
//         } catch (Exception err) {
//             System.err.println("Graphic asset index point skipped: " + imageAssetPath);
//         }

//         Text descriptiveTitle = new Text(visibleTitle);
//         descriptiveTitle.setFill(matchingActiveState ? Color.WHITE : Color.web(GamerVaultStyles.TEXT_SECONDARY));
//         descriptiveTitle.setFont(Font.font("Arial", matchingActiveState ? FontWeight.BOLD : FontWeight.NORMAL, 12));

//         gridCard.getChildren().addAll(assetPreview, descriptiveTitle);

//         gridCard.setOnMouseClicked(e -> {
//             this.selectedTemplateType = templateKey;
//             rebuildVisualSwatchesPanel();
//             evaluateDynamicWorkflowVisibility();
//         });

//         return gridCard;
//     }

//     private void evaluateDynamicWorkflowVisibility() {
//         if (selectedTemplateType.equals("OVERALL")) {
//             matchHistorySelectorBox.setOpacity(0.25);
//             matchHistorySelectorBox.setDisable(true);
//             chosenMatchModel = null;
//             feedbackMatchSummaryLabel.setText("Bypassed: Overall profiles parse absolute totals directly.");
//             feedbackMatchSummaryLabel.setStyle("-fx-text-fill: " + GamerVaultStyles.TEXT_MUTED + ";");
//         } else {
//             matchHistorySelectorBox.setOpacity(1.0);
//             matchHistorySelectorBox.setDisable(false);
//             if (chosenMatchModel == null) {
//                 feedbackMatchSummaryLabel.setText("⚠️ Linkage pending: Assign 1 match dataset.");
//                 feedbackMatchSummaryLabel.setStyle("-fx-text-fill: #F59E0B;");
//             }
//         }
//     }

//     private void triggerMatchVaultSelectionDialog() {
//         Dialog<MatchModel> selectionDialog = new Dialog<>();
//         selectionDialog.setTitle("Synchronize Telemetry Match");

//         VBox dialogWrapper = new VBox(15);
//         dialogWrapper.setPadding(new Insets(20));
//         dialogWrapper.setPrefWidth(460);
//         dialogWrapper.setPrefHeight(480);
//         dialogWrapper.setStyle(
//                 "-fx-background-color: #0B0F19; -fx-border-color: rgba(139,92,246,0.25); -fx-border-width: 1; -fx-border-radius: 8;");

//         Text interfaceLabel = new Text("Link Single Match Asset Coordinates");
//         interfaceLabel.setFill(Color.WHITE);
//         interfaceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

//         VBox interactiveListContainer = new VBox(10);
//         Text loadingAnchor = new Text("Querying Firestore Records...");
//         loadingAnchor.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
//         interactiveListContainer.getChildren().add(loadingAnchor);

//         ScrollPane listScroller = new ScrollPane(interactiveListContainer);
//         GamerVaultStyles.applyStyledScrollPane(listScroller);
//         listScroller.setFitToWidth(true);
//         VBox.setVgrow(listScroller, Priority.ALWAYS);

//         dialogWrapper.getChildren().addAll(interfaceLabel, listScroller);
//         selectionDialog.getDialogPane().setContent(dialogWrapper);
//         selectionDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
//         selectionDialog.getDialogPane().setStyle("-fx-background-color: #0B0F19;");

//         Node nativeCancelBtn = selectionDialog.getDialogPane().lookupButton(ButtonType.CANCEL);
//         if (nativeCancelBtn instanceof Button) {
//             GamerVaultStyles.applyGhostButton((Button) nativeCancelBtn);
//         }

//         new Thread(() -> {
//             String primaryPlayerUid = AuthController.currentUser != null ? AuthController.currentUser.getUserId()
//                     : "TEST_USER_123";
//             List<MatchModel> userHistoryRecords = MatchController.getUserMatchHistory(primaryPlayerUid);

//             Platform.runLater(() -> {
//                 interactiveListContainer.getChildren().clear();
//                 if (userHistoryRecords == null || userHistoryRecords.isEmpty()) {
//                     Text blankStateText = new Text("No telemetry matches discovered inside Vault infrastructure.");
//                     blankStateText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
//                     interactiveListContainer.getChildren().add(blankStateText);
//                     return;
//                 }
//                 for (MatchModel recordItem : userHistoryRecords) {
//                     HBox itemRow = new HBox(15);
//                     itemRow.setAlignment(Pos.CENTER_LEFT);
//                     itemRow.setPadding(new Insets(12));
//                     itemRow.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 6;");

//                     VBox textualBlock = new VBox(3);
//                     Text metaHeader = new Text(recordItem.getMap() + " (" + recordItem.getGameMode() + ")");
//                     metaHeader.setFill(Color.WHITE);
//                     metaHeader.setFont(Font.font("Arial", FontWeight.BOLD, 13));
//                     Text metricsSubHeader = new Text("Placement: #" + recordItem.getTeamPlacement()
//                             + " | Kills Secured: " + recordItem.getKills() + " | Dmg: " + recordItem.getDamage());
//                     metricsSubHeader.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
//                     metricsSubHeader.setFont(Font.font("Arial", 11));
//                     textualBlock.getChildren().addAll(metaHeader, metricsSubHeader);

//                     Region structuralSpacer = new Region();
//                     HBox.setHgrow(structuralSpacer, Priority.ALWAYS);

//                     Button chooseRowBtn = new Button("Link Data");
//                     GamerVaultStyles.applyGhostButton(chooseRowBtn);
//                     chooseRowBtn.setStyle(
//                             chooseRowBtn.getStyle() + "-fx-border-color: " + GamerVaultStyles.ACCENT_PURPLE + ";");

//                     chooseRowBtn.setOnAction(evt -> {
//                         chosenMatchModel = recordItem;
//                         feedbackMatchSummaryLabel.setText(
//                                 "Linked: " + recordItem.getMap() + " [Rank " + recordItem.getTeamPlacement() + "]");
//                         feedbackMatchSummaryLabel.setStyle("-fx-text-fill: " + GamerVaultStyles.ACCENT_GREEN + ";");
//                         selectionDialog.setResult(recordItem);
//                         selectionDialog.close();
//                     });

//                     itemRow.getChildren().addAll(textualBlock, structuralSpacer, chooseRowBtn);
//                     interactiveListContainer.getChildren().add(itemRow);
//                 }
//             });
//         }).start();

//         selectionDialog.showAndWait();
//     }

//     /*
//      * ─── PREVIEW & EXECUTION AREA (RIGHT SIDE VIEWER) ──────────────────────
//      */
//     private VBox assembleRealTimePreviewPanel() {
//         VBox processingCol = new VBox(20);
//         processingCol.setAlignment(Pos.TOP_CENTER);

//         StackPane visualFrameBackground = new StackPane();
//         visualFrameBackground.setPrefHeight(460);
//         visualFrameBackground.setMaxWidth(Double.MAX_VALUE);
//         GamerVaultStyles.applyGlassCard(visualFrameBackground);

//         mainPreviewDisplayArea = new ImageView();
//         mainPreviewDisplayArea.setFitWidth(340);
//         mainPreviewDisplayArea.setFitHeight(410);
//         mainPreviewDisplayArea.setPreserveRatio(true);

//         Text centerStatePlaceholder = new Text(
//                 "Generation pipeline idle. Setup parameters and dispatch execution request.");
//         centerStatePlaceholder.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
//         centerStatePlaceholder.setFont(Font.font("Arial", 13));

//         visualFrameBackground.getChildren().addAll(centerStatePlaceholder, mainPreviewDisplayArea);

//         HBox controlButtonsRow = new HBox(15);
//         controlButtonsRow.setAlignment(Pos.CENTER);

//         processGenerationBtn = new Button("✨ Dispatch to Gemini Engine");
//         processGenerationBtn.setPrefHeight(50);
//         HBox.setHgrow(processGenerationBtn, Priority.ALWAYS);
//         processGenerationBtn.setMaxWidth(Double.MAX_VALUE);
//         GamerVaultStyles.applyGradientButton(processGenerationBtn, GamerVaultStyles.ACCENT_PURPLE,
//                 GamerVaultStyles.ACCENT_PURPLE_DARK, "white");
//         processGenerationBtn.setOnAction(e -> dispatchGeminiApiPipelineSequence(centerStatePlaceholder));

//         saveToDiskBtn = new Button("📥 Save File To Disk");
//         saveToDiskBtn.setPrefHeight(50);
//         HBox.setHgrow(saveToDiskBtn, Priority.ALWAYS);
//         saveToDiskBtn.setMaxWidth(Double.MAX_VALUE);
//         GamerVaultStyles.applyGhostButton(saveToDiskBtn);
//         saveToDiskBtn.setDisable(true); // Frozen until asset is fully generated
//         saveToDiskBtn.setOnAction(e -> executeLocalDiskDownloadSequence());

//         controlButtonsRow.getChildren().addAll(processGenerationBtn, saveToDiskBtn);
//         processingCol.getChildren().addAll(visualFrameBackground, controlButtonsRow);

//         GamerVaultAnimations.fadeInUp(processingCol, 300, 500);
//         return processingCol;
//     }

//     /*
//      * ─── GEMINI MULTIMODAL API ORCHESTRATION PIPELINE ───────────────────────
//      */
//     private void dispatchGeminiApiPipelineSequence(Text statePlaceholderText) {
//         if (!selectedTemplateType.equals("OVERALL") && chosenMatchModel == null) {
//             Alert validationWarning = new Alert(Alert.AlertType.WARNING,
//                     "Workflow Error: This configuration profile requires a linked telemetry match snapshot.",
//                     ButtonType.OK);
//             validationWarning.show();
//             return;
//         }

//         processGenerationBtn.setText("Compiling Multimodal Payload...");
//         processGenerationBtn.setDisable(true);
//         saveToDiskBtn.setDisable(true);
//         mainPreviewDisplayArea.setImage(null);
//         statePlaceholderText.setText("Encoding templates & packing variables into payload container...");
//         statePlaceholderText.setVisible(true);

//         new Thread(() -> {
//             try {
//                 UserModel profileUser = AuthController.currentUser;

//                 // 1. ARCHITECTURE CLEANLINESS: Gather prompt structures straight from
//                 // Controller layer
//                 String structuredSystemInstructionsPrompt = PortfolioGeminiController
//                         .compileStrongPrompt(selectedTemplateType, profileUser, chosenMatchModel);

//                 String innerInternalPath = "/assets/templates/" + selectedTemplateType.toLowerCase() + "_template.png";
//                 String encodedBase64ImageContext = PortfolioGeminiController.encodeTemplateToBase64(innerInternalPath);

//                 // DEBUG VERIFICATION PRINTS (Shows your exact clean payload data before hitting
//                 // the network)
//                 System.out.println("--- DYNAMIC WORKFLOW PAYLOAD LOG ---");
//                 System.out.println("TARGET ENVELOPE METHOD: " + selectedTemplateType);
//                 System.out.println("COMPILED INSTRUCTION SET:\n" + structuredSystemInstructionsPrompt);
//                 System.out.println(
//                         "BASE64 SEGMENT PACKED INDEX LENGTH: " + encodedBase64ImageContext.length() + " bytes");

//                 byte[] rawImageBytes = com.example.controller.gemini.GeminiImageClient.generateHolographicCardBytes(
//                         structuredSystemInstructionsPrompt,
//                         encodedBase64ImageContext);

//                 Image remoteAiGeneratedAssetImage = new Image(new java.io.ByteArrayInputStream(rawImageBytes));

//                 Platform.runLater(() -> {
//                     currentAiRenderedImageBytes = rawImageBytes; // SAVE BYTES FOR DOWNLOAD
//                     currentAiRenderedImage = remoteAiGeneratedAssetImage;
//                     mainPreviewDisplayArea.setImage(currentAiRenderedImage);
//                     statePlaceholderText.setVisible(false);

//                     processGenerationBtn.setText("✨ Dispatch to Gemini Engine");
//                     processGenerationBtn.setDisable(false);
//                     saveToDiskBtn.setDisable(false);
//                     saveToDiskBtn
//                             .setStyle(saveToDiskBtn.getStyle() + "-fx-border-color: " + GamerVaultStyles.ACCENT_GREEN
//                                     + "; -fx-text-fill: " + GamerVaultStyles.ACCENT_GREEN + ";");
//                 });

//             } catch (Exception err) {
//                 err.printStackTrace();
//                 Platform.runLater(() -> {
//                     processGenerationBtn.setText("✨ Dispatch to Gemini Engine");
//                     processGenerationBtn.setDisable(false);
//                     statePlaceholderText.setText("Network Exception: Execution stream dropped.");
//                     statePlaceholderText.setFill(Color.web("#EF4444"));
//                 });
//             }
//         }).start();
//     }

//     private void executeLocalDiskDownloadSequence() {
//         if (currentAiRenderedImageBytes == null)
//             return;

//         FileChooser saveDialog = new FileChooser();
//         saveDialog.setTitle("Export Compiled Player Asset");
//         saveDialog.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Visual Asset Format", "*.png"));

//         String gamerNameIndex = AuthController.currentUser != null ? AuthController.currentUser.getIgn() : "Operator";
//         saveDialog.setInitialFileName(gamerNameIndex + "_" + selectedTemplateType + "_AI_Render.png");

//         File fileOutputTarget = saveDialog.showSaveDialog(portfolioScreenStage);

//         if (fileOutputTarget != null) {
//             new Thread(() -> {
//                 try {
//                     // PURE JAVA NIO FILE WRITE (Bypasses SwingFXUtils entirely!)
//                     java.nio.file.Files.write(fileOutputTarget.toPath(), currentAiRenderedImageBytes);

//                     Platform.runLater(() -> {
//                         Alert completedAlert = new Alert(Alert.AlertType.INFORMATION,
//                                 "The AI-rendered player card has been successfully saved.", ButtonType.OK);
//                         completedAlert.setTitle("Export Confirmed");
//                         completedAlert.setHeaderText("Asset Downloaded Successfully");
//                         completedAlert.getDialogPane().setStyle("-fx-background-color: #0B0F19;");
//                         completedAlert.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: white;");
//                         completedAlert.showAndWait();
//                     });
//                 } catch (Exception downloadErr) {
//                     Platform.runLater(() -> {
//                         Alert faultAlert = new Alert(Alert.AlertType.ERROR,
//                                 "Disk I/O failure: Could not save the image.", ButtonType.OK);
//                         faultAlert.showAndWait();
//                     });
//                 }
//             }).start();
//         }
//     }
// }

package com.example.view.player;

import java.io.File;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class PortfolioGeneratorScreen {

    // --- SCREEN NAVIGATION BOUNDARIES ---
    Scene portfolioScreenScene;
    Stage portfolioScreenStage;

    // --- REVENUE RUNTIME STATES ---
    private TemplateModel selectedTemplate = null; // Changed from String to Model
    private MatchModel chosenMatchModel = null;
    private Image currentAiRenderedImage = null;

    // --- CORE INTERFACE NODES ---
    private FlowPane visualSwitchesGrid; // Changed to FlowPane to wrap dynamically added templates
    private VBox matchHistorySelectorBox;
    private Text feedbackMatchSummaryLabel;
    private ImageView mainPreviewDisplayArea;
    private Button processGenerationBtn;
    private Button saveToDiskBtn;

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

        corePane.setCenter(contentScroller);
        return corePane;
    }

    private VBox assembleMainScrollerCanvas() {
        VBox primaryContainer = new VBox(25);
        primaryContainer.setPadding(new Insets(10, 20, 40, 20));

        // HEADER BLOCK INJECTION
        VBox layoutHeader = assembleSectionHeader();

        // SPLIT LAYOUT PIPELINE
        HBox coreSplitPanel = new HBox(40);
        VBox configControlsPanel = assembleConfigurationControlsPanel();
        VBox realTimePreviewPanel = assembleRealTimePreviewPanel();

        configControlsPanel.setPrefWidth(460);
        configControlsPanel.setMinWidth(460);
        HBox.setHgrow(realTimePreviewPanel, Priority.ALWAYS);

        coreSplitPanel.getChildren().addAll(configControlsPanel, realTimePreviewPanel);
        primaryContainer.getChildren().addAll(layoutHeader, coreSplitPanel);
        return primaryContainer;
    }

    private VBox assembleSectionHeader() {
        Text mainTitle = new Text("Portfolio Engine");
        mainTitle.setFont(Font.font("Arial", FontWeight.BOLD, 42));
        mainTitle.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

        Text secondarySubtitle = new Text(
                "Bifurcate your battle telemetry. Click an exact template variant layout profile to synchronize stats with AI generation pipelines.");
        secondarySubtitle.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        secondarySubtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

        VBox headerGroup = new VBox(5);
        headerGroup.getChildren().addAll(mainTitle, secondarySubtitle);
        GamerVaultAnimations.fadeInUp(mainTitle, 0, 400);
        GamerVaultAnimations.fadeInUp(secondarySubtitle, 100, 400);
        return headerGroup;
    }

    /*
     * ─── CONFIGURATION LOGIC (LEFT SIDE CONTROLS) ──────────────────────────
     */
    private VBox assembleConfigurationControlsPanel() {
        VBox configurationsCol = new VBox(25);

        // CARD PANEL 1: VISUAL SELECTION SWATCH MATRIX
        VBox layoutSelectorWrapper = new VBox(15);
        layoutSelectorWrapper.setPadding(new Insets(25));
        GamerVaultStyles.applyGlassCard(layoutSelectorWrapper);

        Text stepOneLabel = new Text("1. Choose Target Performance Template");
        stepOneLabel.setFill(Color.WHITE);
        stepOneLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Changed to FlowPane to allow multiple templates to wrap lines
        visualSwitchesGrid = new FlowPane();
        visualSwitchesGrid.setHgap(15);
        visualSwitchesGrid.setVgap(15);

        rebuildVisualSwatchesPanel();

        layoutSelectorWrapper.getChildren().addAll(stepOneLabel, visualSwitchesGrid);

        // CARD PANEL 2: MATCH DEPENDENCY CONTEXT PICKER
        matchHistorySelectorBox = new VBox(15);
        matchHistorySelectorBox.setPadding(new Insets(25));
        GamerVaultStyles.applyGlassCard(matchHistorySelectorBox);

        Text stepTwoLabel = new Text("2. Link Telemetry History");
        stepTwoLabel.setFill(Color.WHITE);
        stepTwoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        feedbackMatchSummaryLabel = new Text("Select a template first.");
        feedbackMatchSummaryLabel.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        feedbackMatchSummaryLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        Button summonMatchHistoryModalBtn = new Button("📂 Scan Match Vault History");
        summonMatchHistoryModalBtn.setMaxWidth(Double.MAX_VALUE);
        GamerVaultStyles.applyGhostButton(summonMatchHistoryModalBtn);
        summonMatchHistoryModalBtn.setOnAction(e -> triggerMatchVaultSelectionDialog());

        matchHistorySelectorBox.getChildren().addAll(stepTwoLabel, feedbackMatchSummaryLabel,
                summonMatchHistoryModalBtn);
        evaluateDynamicWorkflowVisibility();

        configurationsCol.getChildren().addAll(layoutSelectorWrapper, matchHistorySelectorBox);
        GamerVaultAnimations.fadeInUp(configurationsCol, 200, 500);
        return configurationsCol;
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

    private VBox generateVisualTemplateItemNode(TemplateModel model) {
        VBox gridCard = new VBox(10);
        gridCard.setAlignment(Pos.CENTER);
        gridCard.setPadding(new Insets(12));

        boolean matchingActiveState = (selectedTemplate != null
                && selectedTemplate.getTemplateId().equals(model.getTemplateId()));

        String standardStyle = "-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 12; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 12;";
        String activatedStyle = "-fx-background-color: rgba(139,92,246,0.1); -fx-background-radius: 12; -fx-cursor: hand; -fx-border-color: "
                + GamerVaultStyles.ACCENT_PURPLE + "; -fx-border-width: 2; -fx-border-radius: 12;";

        gridCard.setStyle(matchingActiveState ? activatedStyle : standardStyle);
        if (matchingActiveState) {
            gridCard.setEffect(new DropShadow(15, Color.web(GamerVaultStyles.ACCENT_PURPLE, 0.4)));
        }

        ImageView assetPreview = new ImageView();
        assetPreview.setFitWidth(105);
        assetPreview.setFitHeight(145);
        assetPreview.setPreserveRatio(true);

        try {
            if (model.isHardcoded()) {
                if (getClass().getResource(model.getLocalAssetPath()) != null) {
                    assetPreview.setImage(new Image(getClass().getResourceAsStream(model.getLocalAssetPath())));
                }
            } else {
                String url = model.getImageUrl();
                if (url != null && !url.isEmpty()) {
                    // --- THE FIX: Format raw local file paths into valid JavaFX URIs ---
                    if (!url.startsWith("http") && !url.startsWith("file:")) {
                        url = new File(url).toURI().toString();
                    }
                    // Background loading for web or local URIs
                    assetPreview.setImage(new Image(url, true));
                }
            }
        } catch (Exception err) {
            System.err.println("Graphic asset index point skipped: " + err.getMessage());
        }

        Text descriptiveTitle = new Text(model.getTemplateName());
        descriptiveTitle.setFill(matchingActiveState ? Color.WHITE : Color.web(GamerVaultStyles.TEXT_SECONDARY));
        descriptiveTitle.setFont(Font.font("Arial", matchingActiveState ? FontWeight.BOLD : FontWeight.NORMAL, 12));

        gridCard.getChildren().addAll(assetPreview, descriptiveTitle);

        gridCard.setOnMouseClicked(e -> {
            this.selectedTemplate = model;
            // Force re-render to update the purple border highlights
            for (Node n : visualSwitchesGrid.getChildren()) {
                n.setStyle(standardStyle);
                n.setEffect(null);
                if (n instanceof VBox) {
                    ((Text) ((VBox) n).getChildren().get(1)).setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
                    ((Text) ((VBox) n).getChildren().get(1)).setFont(Font.font("Arial", FontWeight.NORMAL, 12));
                }
            }
            gridCard.setStyle(activatedStyle);
            gridCard.setEffect(new DropShadow(15, Color.web(GamerVaultStyles.ACCENT_PURPLE, 0.4)));
            descriptiveTitle.setFill(Color.WHITE);
            descriptiveTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));

            evaluateDynamicWorkflowVisibility();
        });

        return gridCard;
    }

    private void evaluateDynamicWorkflowVisibility() {
        // Overall stat templates don't need a specific match attached
        if (selectedTemplate != null && (selectedTemplate.getTemplateId().equals("OVERALL") ||
                (selectedTemplate.getTemplateName().toLowerCase().contains("overall")))) {

            matchHistorySelectorBox.setOpacity(0.25);
            matchHistorySelectorBox.setDisable(true);
            chosenMatchModel = null;
            feedbackMatchSummaryLabel.setText("Bypassed: Overall profiles parse absolute totals directly.");
            feedbackMatchSummaryLabel.setStyle("-fx-text-fill: " + GamerVaultStyles.TEXT_MUTED + ";");
        } else {
            matchHistorySelectorBox.setOpacity(1.0);
            matchHistorySelectorBox.setDisable(false);
            if (chosenMatchModel == null) {
                feedbackMatchSummaryLabel.setText("⚠️ Linkage pending: Assign 1 match dataset.");
                feedbackMatchSummaryLabel.setStyle("-fx-text-fill: #F59E0B;");
            }
        }
    }

    // ... (Keep triggerMatchVaultSelectionDialog exactly as it was) ...
    private void triggerMatchVaultSelectionDialog() {
        Dialog<MatchModel> selectionDialog = new Dialog<>();
        selectionDialog.setTitle("Synchronize Telemetry Match");

        VBox dialogWrapper = new VBox(15);
        dialogWrapper.setPadding(new Insets(20));
        dialogWrapper.setPrefWidth(460);
        dialogWrapper.setPrefHeight(480);
        dialogWrapper.setStyle(
                "-fx-background-color: #0B0F19; -fx-border-color: rgba(139,92,246,0.25); -fx-border-width: 1; -fx-border-radius: 8;");

        Text interfaceLabel = new Text("Link Single Match Asset Coordinates");
        interfaceLabel.setFill(Color.WHITE);
        interfaceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        VBox interactiveListContainer = new VBox(10);
        Text loadingAnchor = new Text("Querying Firestore Records...");
        loadingAnchor.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        interactiveListContainer.getChildren().add(loadingAnchor);

        ScrollPane listScroller = new ScrollPane(interactiveListContainer);
        GamerVaultStyles.applyStyledScrollPane(listScroller);
        listScroller.setFitToWidth(true);
        VBox.setVgrow(listScroller, Priority.ALWAYS);

        dialogWrapper.getChildren().addAll(interfaceLabel, listScroller);
        selectionDialog.getDialogPane().setContent(dialogWrapper);
        selectionDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        selectionDialog.getDialogPane().setStyle("-fx-background-color: #0B0F19;");

        Node nativeCancelBtn = selectionDialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (nativeCancelBtn instanceof Button) {
            GamerVaultStyles.applyGhostButton((Button) nativeCancelBtn);
        }

        new Thread(() -> {
            String primaryPlayerUid = AuthController.currentUser != null ? AuthController.currentUser.getUserId()
                    : "TEST_USER_123";
            List<MatchModel> userHistoryRecords = MatchController.getUserMatchHistory(primaryPlayerUid);

            Platform.runLater(() -> {
                interactiveListContainer.getChildren().clear();
                if (userHistoryRecords == null || userHistoryRecords.isEmpty()) {
                    Text blankStateText = new Text("No telemetry matches discovered inside Vault infrastructure.");
                    blankStateText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                    interactiveListContainer.getChildren().add(blankStateText);
                    return;
                }
                for (MatchModel recordItem : userHistoryRecords) {
                    HBox itemRow = new HBox(15);
                    itemRow.setAlignment(Pos.CENTER_LEFT);
                    itemRow.setPadding(new Insets(12));
                    itemRow.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 6;");

                    VBox textualBlock = new VBox(3);
                    Text metaHeader = new Text(recordItem.getMap() + " (" + recordItem.getGameMode() + ")");
                    metaHeader.setFill(Color.WHITE);
                    metaHeader.setFont(Font.font("Arial", FontWeight.BOLD, 13));
                    Text metricsSubHeader = new Text("Placement: #" + recordItem.getTeamPlacement()
                            + " | Kills Secured: " + recordItem.getKills() + " | Dmg: " + recordItem.getDamage());
                    metricsSubHeader.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
                    metricsSubHeader.setFont(Font.font("Arial", 11));
                    textualBlock.getChildren().addAll(metaHeader, metricsSubHeader);

                    Region structuralSpacer = new Region();
                    HBox.setHgrow(structuralSpacer, Priority.ALWAYS);

                    Button chooseRowBtn = new Button("Link Data");
                    GamerVaultStyles.applyGhostButton(chooseRowBtn);
                    chooseRowBtn.setStyle(
                            chooseRowBtn.getStyle() + "-fx-border-color: " + GamerVaultStyles.ACCENT_PURPLE + ";");

                    chooseRowBtn.setOnAction(evt -> {
                        chosenMatchModel = recordItem;
                        feedbackMatchSummaryLabel.setText(
                                "Linked: " + recordItem.getMap() + " [Rank " + recordItem.getTeamPlacement() + "]");
                        feedbackMatchSummaryLabel.setStyle("-fx-text-fill: " + GamerVaultStyles.ACCENT_GREEN + ";");
                        selectionDialog.setResult(recordItem);
                        selectionDialog.close();
                    });

                    itemRow.getChildren().addAll(textualBlock, structuralSpacer, chooseRowBtn);
                    interactiveListContainer.getChildren().add(itemRow);
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

        StackPane visualFrameBackground = new StackPane();
        visualFrameBackground.setPrefHeight(460);
        visualFrameBackground.setMaxWidth(Double.MAX_VALUE);
        GamerVaultStyles.applyGlassCard(visualFrameBackground);

        mainPreviewDisplayArea = new ImageView();
        mainPreviewDisplayArea.setFitWidth(340);
        mainPreviewDisplayArea.setFitHeight(410);
        mainPreviewDisplayArea.setPreserveRatio(true);

        Text centerStatePlaceholder = new Text(
                "Generation pipeline idle. Setup parameters and dispatch execution request.");
        centerStatePlaceholder.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        centerStatePlaceholder.setFont(Font.font("Arial", 13));

        visualFrameBackground.getChildren().addAll(centerStatePlaceholder, mainPreviewDisplayArea);

        HBox controlButtonsRow = new HBox(15);
        controlButtonsRow.setAlignment(Pos.CENTER);

        processGenerationBtn = new Button("✨ Dispatch to Gemini Engine");
        processGenerationBtn.setPrefHeight(50);
        HBox.setHgrow(processGenerationBtn, Priority.ALWAYS);
        processGenerationBtn.setMaxWidth(Double.MAX_VALUE);
        GamerVaultStyles.applyGradientButton(processGenerationBtn, GamerVaultStyles.ACCENT_PURPLE,
                GamerVaultStyles.ACCENT_PURPLE_DARK, "white");
        processGenerationBtn.setOnAction(e -> dispatchGeminiApiPipelineSequence(centerStatePlaceholder));

        saveToDiskBtn = new Button("📥 Save File To Disk");
        saveToDiskBtn.setPrefHeight(50);
        HBox.setHgrow(saveToDiskBtn, Priority.ALWAYS);
        saveToDiskBtn.setMaxWidth(Double.MAX_VALUE);
        GamerVaultStyles.applyGhostButton(saveToDiskBtn);
        saveToDiskBtn.setDisable(true); // Frozen until asset is fully generated
        saveToDiskBtn.setOnAction(e -> executeLocalDiskDownloadSequence());

        controlButtonsRow.getChildren().addAll(processGenerationBtn, saveToDiskBtn);
        processingCol.getChildren().addAll(visualFrameBackground, controlButtonsRow);

        GamerVaultAnimations.fadeInUp(processingCol, 300, 500);
        return processingCol;
    }

    /*
     * ─── GEMINI MULTIMODAL API ORCHESTRATION PIPELINE ───────────────────────
     */
    private void dispatchGeminiApiPipelineSequence(Text statePlaceholderText) {
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
        mainPreviewDisplayArea.setImage(null);
        statePlaceholderText.setText("Encoding templates & packing variables into payload container...");
        statePlaceholderText.setVisible(true);

        new Thread(() -> {
            try {
                UserModel profileUser = AuthController.currentUser;
                String structuredSystemInstructionsPrompt;
                String encodedBase64ImageContext;

                // --- DYNAMIC AI ROUTING ---
                if (selectedTemplate.isHardcoded()) {
                    // Route to your complex hardcoded logic block
                    structuredSystemInstructionsPrompt = PortfolioGeminiController
                            .compileStrongPrompt(selectedTemplate.getTemplateId(), profileUser, chosenMatchModel);
                    encodedBase64ImageContext = PortfolioGeminiController
                            .encodeTemplateToBase64(selectedTemplate.getLocalAssetPath());
                } else {
                    // Route to your new dynamic Admin-created engine
                    structuredSystemInstructionsPrompt = PortfolioGeminiController
                            .compileDynamicAdminPrompt(selectedTemplate, profileUser, chosenMatchModel);
                    encodedBase64ImageContext = PortfolioGeminiController
                            .downloadAndConvertUrlToBase64(selectedTemplate.getImageUrl());
                }

                // DEBUG VERIFICATION PRINTS
                System.out.println("--- DYNAMIC WORKFLOW PAYLOAD LOG ---");
                System.out.println("TARGET ENVELOPE METHOD: " + selectedTemplate.getTemplateName());
                System.out.println("COMPILED INSTRUCTION SET:\n" + structuredSystemInstructionsPrompt);
                System.out.println(
                        "BASE64 SEGMENT PACKED INDEX LENGTH: " + encodedBase64ImageContext.length() + " bytes");

                byte[] rawImageBytes = GeminiImageClient.generateHolographicCardBytes(
                        structuredSystemInstructionsPrompt,
                        encodedBase64ImageContext);

                Image remoteAiGeneratedAssetImage = new Image(new java.io.ByteArrayInputStream(rawImageBytes));

                Platform.runLater(() -> {
                    currentAiRenderedImageBytes = rawImageBytes; // SAVE BYTES FOR DOWNLOAD
                    currentAiRenderedImage = remoteAiGeneratedAssetImage;
                    mainPreviewDisplayArea.setImage(currentAiRenderedImage);
                    statePlaceholderText.setVisible(false);

                    processGenerationBtn.setText("✨ Dispatch to Gemini Engine");
                    processGenerationBtn.setDisable(false);
                    saveToDiskBtn.setDisable(false);
                    saveToDiskBtn
                            .setStyle(saveToDiskBtn.getStyle() + "-fx-border-color: " + GamerVaultStyles.ACCENT_GREEN
                                    + "; -fx-text-fill: " + GamerVaultStyles.ACCENT_GREEN + ";");
                });

            } catch (Exception err) {
                err.printStackTrace();
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
                        Alert completedAlert = new Alert(Alert.AlertType.INFORMATION,
                                "The AI-rendered player card has been successfully saved.", ButtonType.OK);
                        completedAlert.setTitle("Export Confirmed");
                        completedAlert.setHeaderText("Asset Downloaded Successfully");
                        completedAlert.getDialogPane().setStyle("-fx-background-color: #0B0F19;");
                        completedAlert.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: white;");
                        completedAlert.showAndWait();
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
}