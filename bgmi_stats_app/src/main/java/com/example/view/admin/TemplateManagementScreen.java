package com.example.view.admin;

import com.example.controller.admin.NotificationController;
import com.example.controller.admin.TemplateController;
import com.example.dao.StorageDao;
import com.example.model.admin.TemplateModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TemplateManagementScreen {

    private VBox activeTemplatesList;
    private File selectedImageFile = null;
    private Text selectedFileNameText;

    public BorderPane startTemplateManagementScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        ScrollPane scroller = new ScrollPane(createContent());
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setFitToWidth(true);
        root.setCenter(scroller);

        return root;
    }

    private VBox createContent() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(20, 30, 40, 30));

        Text title = new Text("Holographic Templates");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.WHITE);
        Text subtitle = new Text("Design and deploy new AI generative card layouts for players.");
        subtitle.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        VBox header = new VBox(5, title, subtitle);

        HBox splitLayout = new HBox(30);
        splitLayout.getChildren().addAll(createTemplateForm(), createActiveTemplatesView());
        HBox.setHgrow(splitLayout.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(splitLayout.getChildren().get(1), Priority.ALWAYS);

        container.getChildren().addAll(header, splitLayout);
        GamerVaultAnimations.fadeInUp(container, 0, 500);

        return container;
    }

    private VBox createTemplateForm() {
        VBox formCard = new VBox(15);
        formCard.setPadding(new Insets(25));
        GamerVaultStyles.applyGlassCard(formCard);
        formCard.setPrefWidth(500);

        Text sectionTitle = new Text("Deploy New Template");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        sectionTitle.setFill(Color.web(GamerVaultStyles.ACCENT_CYAN));

        TextField nameField = new TextField();
        nameField.setPromptText("Template Name (e.g., Cyberpunk MVP)");
        HBox nameBox = GamerVaultStyles.createStyledInput(nameField, GamerVaultStyles.ACCENT_CYAN);

        // --- FILE CHOOSER LOGIC ---
        HBox fileUploadBox = new HBox(15);
        fileUploadBox.setAlignment(Pos.CENTER_LEFT);

        Button selectImgBtn = new Button("📁 Select Base Layout");
        GamerVaultStyles.applyGhostButton(selectImgBtn);
        selectImgBtn.setStyle(selectImgBtn.getStyle() + "-fx-border-color: " + GamerVaultStyles.ACCENT_CYAN + ";");

        selectedFileNameText = new Text("No image selected");
        selectedFileNameText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

        selectImgBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            selectedImageFile = chooser.showOpenDialog(null);
            if (selectedImageFile != null) {
                selectedFileNameText.setText(selectedImageFile.getName());
                selectedFileNameText.setFill(Color.web(GamerVaultStyles.ACCENT_CYAN));
            }
        });
        fileUploadBox.getChildren().addAll(selectImgBtn, selectedFileNameText);

        Text cheatSheet = new Text(
                "Available Tags: {playerName}, {ign}, {role}, {matches}, {kills}, {kdRatio}, {placement}, {survivalTime}");
        cheatSheet.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        cheatSheet.setFont(Font.font("Arial", 11));

        TextArea promptArea = new TextArea();
        promptArea.setPromptText("Write the AI Prompt rules here... Use tags like {ign} to inject live data.");
        promptArea.setPrefHeight(200);
        promptArea.setWrapText(true);
        promptArea.setStyle("-fx-control-inner-background: " + GamerVaultStyles.INPUT_BG
                + "; -fx-text-fill: white; -fx-prompt-text-fill: gray;");

        Button saveBtn = new Button("Deploy to Platform");
        saveBtn.setPrefWidth(Double.MAX_VALUE);
        saveBtn.setPrefHeight(45);
        GamerVaultStyles.applyGradientButton(saveBtn, GamerVaultStyles.ACCENT_CYAN, "#0891B2", "white");

        saveBtn.setOnAction(e -> {
            if (nameField.getText().isEmpty() || selectedImageFile == null || promptArea.getText().isEmpty()) {
                saveBtn.setText("Missing Name, Image, or Prompt!");
                return;
            }
            saveBtn.setText("Uploading & Deploying...");
            saveBtn.setDisable(true);

            new Thread(() -> {
                if (nameField.getText().isEmpty() || selectedImageFile == null || promptArea.getText().isEmpty()) {
                    saveBtn.setText("Missing Name, Image, or Prompt!");
                    return;
                }
                saveBtn.setText("Uploading & Deploying...");
                saveBtn.setDisable(true);

                new Thread(() -> {
                    String templateId = UUID.randomUUID().toString();

                    // --- THE FIX: ACTUALLY UPLOAD THE FILE TO FIREBASE STORAGE ---
                    // We pass the physical file and a unique name
                    String uploadedUrl = StorageDao.uploadTemplateImage(selectedImageFile,
                            templateId + ".png");

                    if (uploadedUrl != null && !uploadedUrl.isEmpty()) {
                        TemplateModel newTemplate = new TemplateModel();
                        newTemplate.setTemplateId(templateId);
                        newTemplate.setTemplateName(nameField.getText().trim());
                        newTemplate.setImageUrl(uploadedUrl); // The live Firebase URL
                        newTemplate.setAiPrompt(promptArea.getText().trim());
                        newTemplate.setCreatedAt(System.currentTimeMillis());

                        boolean success = TemplateController.createNewTemplate(newTemplate);

                        Platform.runLater(() -> {
                            saveBtn.setDisable(false);
                            if (success) {
                                saveBtn.setText("Deployed ✓");
                                nameField.clear();
                                promptArea.clear();
                                selectedImageFile = null;
                                selectedFileNameText.setText("No image selected");
                                selectedFileNameText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

                                // NotificationController.sendNotification("New Generator Template!",
                                // "The '" + newTemplate.getTemplateName() + "' card layout is now available.",
                                // "CONTENT", "GLOBAL");
                                loadActiveTemplates();
                            } else {
                                saveBtn.setText("Database Failed");
                            }
                        });
                    } else {
                        Platform.runLater(() -> {
                            saveBtn.setDisable(false);
                            saveBtn.setText("Image Upload Failed");
                        });
                    }
                }).start();
            }).start();
        });

        formCard.getChildren().addAll(sectionTitle, nameBox, fileUploadBox, cheatSheet, promptArea, SizedBox.height(10),
                saveBtn);
        return formCard;
    }

    private VBox createActiveTemplatesView() {
        VBox viewCard = new VBox(15);
        viewCard.setPadding(new Insets(25));
        GamerVaultStyles.applyGlassCard(viewCard);
        viewCard.setPrefWidth(400);

        Text sectionTitle = new Text("Active Templates");
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        sectionTitle.setFill(Color.WHITE);

        activeTemplatesList = new VBox(10);
        ScrollPane listScroller = new ScrollPane(activeTemplatesList);
        GamerVaultStyles.applyStyledScrollPane(listScroller);
        listScroller.setFitToWidth(true);
        listScroller.setPrefHeight(450);

        viewCard.getChildren().addAll(sectionTitle, listScroller);
        loadActiveTemplates();
        return viewCard;
    }

    private void loadActiveTemplates() {
        activeTemplatesList.getChildren().clear();
        Text loading = new Text("Loading templates...");
        loading.setFill(Color.GRAY);
        activeTemplatesList.getChildren().add(loading);

        new Thread(() -> {
            List<TemplateModel> templates = TemplateController.getActiveTemplates();
            Platform.runLater(() -> {
                activeTemplatesList.getChildren().clear();
                if (templates.isEmpty()) {
                    Text empty = new Text("No custom templates deployed.");
                    empty.setFill(Color.GRAY);
                    activeTemplatesList.getChildren().add(empty);
                } else {
                    for (TemplateModel t : templates) {
                        HBox tCard = new HBox(15);
                        tCard.setAlignment(Pos.CENTER_LEFT);
                        tCard.setPadding(new Insets(15));
                        tCard.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8;");

                        // --- THE FIX: Add the Image Thumbnail to the Admin View ---
                        ImageView previewImg = new ImageView();
                        previewImg.setFitWidth(65);
                        previewImg.setFitHeight(65);
                        previewImg.setPreserveRatio(true);

                        // Apply styling to the image thumbnail
                        StackPane imgWrapper = new StackPane(previewImg);
                        imgWrapper.setStyle(
                                "-fx-border-color: rgba(255,255,255,0.15); -fx-border-radius: 6; -fx-background-radius: 6; -fx-background-color: #0A0F18;");
                        imgWrapper.setPadding(new Insets(2));

                        try {
                            String url = t.getImageUrl();
                            if (url != null && !url.isEmpty()) {
                                // Failsafe URL formatter
                                if (!url.startsWith("http") && !url.startsWith("file:")) {
                                    url = new java.io.File(url).toURI().toString();
                                }
                                previewImg.setImage(new Image(url, true));
                            }
                        } catch (Exception e) {
                            System.err.println("Thumbnail skipped.");
                        }

                        // Text Details
                        VBox textCol = new VBox(5);
                        Text tName = new Text("✨ " + t.getTemplateName());
                        tName.setFill(Color.web(GamerVaultStyles.ACCENT_CYAN));
                        tName.setFont(Font.font("Arial", FontWeight.BOLD, 15));

                        Text typeText = new Text("Dynamic Model");
                        typeText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                        typeText.setFont(Font.font("Arial", 11));
                        textCol.getChildren().addAll(tName, typeText);

                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);

                        // Delete Button
                        Button deleteBtn = new Button("🗑");
                        deleteBtn.setStyle(
                                "-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-cursor: hand; -fx-font-size: 16px;");
                        deleteBtn.setOnAction(e -> confirmAndDeleteTemplate(t));

                        tCard.getChildren().addAll(imgWrapper, textCol, spacer, deleteBtn);
                        activeTemplatesList.getChildren().add(tCard);
                    }
                }
            });
        }).start();
    }

    private void confirmAndDeleteTemplate(TemplateModel template) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Template");
        alert.setHeaderText("Delete '" + template.getTemplateName() + "'?");
        alert.setContentText("This will remove it from the generator for all players.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                boolean success = TemplateController.deleteTemplate(template.getTemplateId());
                if (success) {
                    Platform.runLater(this::loadActiveTemplates);
                }
            }).start();
        }
    }
}