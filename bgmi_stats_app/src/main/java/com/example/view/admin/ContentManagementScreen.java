package com.example.view.admin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.controller.admin.ContentController;
import com.example.dao.StorageDao;
import com.example.model.admin.ContentModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;

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
import javafx.scene.control.TextArea;
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
import javafx.stage.FileChooser;

public class ContentManagementScreen {

    private FlowPane contentGrid;
    private HBox metricsRow;
    private List<ContentModel> allFetchedContent = new ArrayList<>();

    public BorderPane startContentManagementScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        ScrollPane scroller = new ScrollPane();
        scroller.setContent(createContent());
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setFitToWidth(true);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setCenter(scroller);
        return root;
    }

    private VBox createContent() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(10, 20, 40, 0));

        HBox headerBox = createHeaderBox();
        metricsRow = new HBox(20);
        HBox filtersRow = createFiltersRow();

        contentGrid = new FlowPane();
        contentGrid.setHgap(20);
        contentGrid.setVgap(20);

        container.getChildren().addAll(headerBox, metricsRow, filtersRow, contentGrid);

        refreshContent();

        return container;
    }

    private HBox createHeaderBox() {
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox textCol = new VBox(5);
        Text title = new Text("Content Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

        Text subtitle = new Text(
                "Manage YouTube and Instagram content shared with players. Review performance and curate the ultimate feed.");
        subtitle.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        textCol.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addContentBtn = new Button("➕ Add Content");
        addContentBtn.setPrefHeight(45);
        addContentBtn.setPrefWidth(160);
        GamerVaultStyles.applyGradientButton(addContentBtn, GamerVaultStyles.ACCENT_PURPLE,
                GamerVaultStyles.ACCENT_PURPLE_DARK, "white");

        addContentBtn.setOnAction(e -> openAddContentDialog());

        headerRow.getChildren().addAll(textCol, spacer, addContentBtn);

        GamerVaultAnimations.fadeInUp(textCol, 0, 500);
        GamerVaultAnimations.fadeInUp(addContentBtn, 100, 500);

        return headerRow;
    }

    private HBox createFiltersRow() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 20, 10, 20));
        row.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 8;");

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search content titles...");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndRenderGrid(newVal));

        HBox searchBox = GamerVaultStyles.createStyledInput(searchField, GamerVaultStyles.ACCENT_PURPLE);
        searchBox.setPrefWidth(300);
        searchBox.setPrefHeight(40);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(searchBox, spacer);
        return row;
    }

    private void refreshContent() {
        new Thread(() -> {
            try {
                List<ContentModel> dataSet = ContentController.loadContent();
                Platform.runLater(() -> {
                    this.allFetchedContent = dataSet;
                    calculateMetrics();
                    filterAndRenderGrid("");
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void calculateMetrics() {
        int total = allFetchedContent.size();
        int yt = 0;
        int ig = 0;

        for (ContentModel c : allFetchedContent) {
            if ("YouTube".equals(c.getPlatform()))
                yt++;
            else if ("Instagram".equals(c.getPlatform()))
                ig++;
        }

        metricsRow.getChildren().clear();
        metricsRow.getChildren().addAll(
                createMetricCard("ALL", "⊞", String.valueOf(total), "Total Content Posts", GamerVaultStyles.TEXT_MUTED),
                createMetricCard("YOUTUBE", "▶", String.valueOf(yt), "YouTube Posts", "#EF4444"),
                createMetricCard("INSTAGRAM", "📷", String.valueOf(ig), "Instagram Posts", "#EC4899"));

        for (Node n : metricsRow.getChildren())
            HBox.setHgrow(n, Priority.ALWAYS);
        GamerVaultAnimations.staggerFadeInChildren(metricsRow, 80);
    }

    private VBox createMetricCard(String typeStr, String iconStr, String valueStr, String labelStr,
            String accentColor) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        GamerVaultStyles.applyGlassCard(card);
        GamerVaultAnimations.scaleOnHover(card, 1.03);

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        Text icon = new Text(iconStr);
        icon.setFill(Color.web(accentColor));
        icon.setFont(Font.font(18));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Text typeText = new Text(typeStr);
        typeText.setFill(Color.web(accentColor));
        typeText.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        topRow.getChildren().addAll(icon, spacer, typeText);

        VBox valueBox = new VBox(4);
        Text mainValue = new Text(valueStr);
        mainValue.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        mainValue.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        Text bottomLabel = new Text(labelStr);
        bottomLabel.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        bottomLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        valueBox.getChildren().addAll(mainValue, bottomLabel);

        card.getChildren().addAll(topRow, valueBox);
        return card;
    }

    private void filterAndRenderGrid(String query) {
        String lowerQuery = query.toLowerCase().trim();
        contentGrid.getChildren().clear();
        int index = 0;

        for (ContentModel c : allFetchedContent) {
            if (c.getTitle() != null && c.getTitle().toLowerCase().contains(lowerQuery)) {
                VBox card = createContentCard(c);
                contentGrid.getChildren().add(card);
                GamerVaultAnimations.fadeInUp(card, index * 60, 450);
                index++;
            }
        }
    }

    private VBox createContentCard(ContentModel model) {
        VBox card = new VBox(15);
        card.setPrefSize(260, 340);
        card.setPadding(new Insets(15));
        GamerVaultStyles.applyGlassCard(card);
        GamerVaultAnimations.scaleOnHover(card, 1.03);

        String accentColor = "YouTube".equals(model.getPlatform()) ? "#EF4444" : "#EC4899";

        // --- 1. THUMBNAIL ---
        StackPane thumbnail = new StackPane();
        thumbnail.setPrefHeight(130);
        thumbnail.setStyle(
                "-fx-background-color: #0F172A; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.05); -fx-border-radius: 8;");

        Rectangle clip = new Rectangle(230, 130);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        thumbnail.setClip(clip);

        if (model.getThumbnailUrl() != null && !model.getThumbnailUrl().isEmpty()) {
            ImageView imgView = new ImageView(model.getThumbnailUrl());
            imgView.setFitWidth(230);
            imgView.setFitHeight(130);
            imgView.setPreserveRatio(false);
            thumbnail.getChildren().add(imgView);
        } else {
            Text playIcon = new Text("▶");
            playIcon.setFill(Color.WHITE);
            playIcon.setFont(Font.font(24));
            thumbnail.getChildren().add(playIcon);
        }
        Label badge = new Label(("YouTube".equals(model.getPlatform()) ? "▶ " : "📷 ") + model.getPlatform());
        badge.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-text-fill: " + accentColor
                + "; -fx-font-family: Arial; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 4 10 4 10; -fx-background-radius: 6;");

        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(8));
        thumbnail.getChildren().add(badge);

        VBox textInfo = new VBox(8);
        String dateStr = new SimpleDateFormat("MMM dd, yyyy").format(new Date(model.getCreatedAt()));
        Text date = new Text(dateStr);
        date.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        date.setFont(Font.font("Arial", FontWeight.BOLD, 10));

        Text title = new Text(model.getTitle());
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setWrappingWidth(230);

        Text desc = new Text(model.getDescription());
        desc.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        desc.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        desc.setWrappingWidth(230);

        // Truncate description if too long
        if (desc.getText() != null && desc.getText().length() > 60) {
            desc.setText(desc.getText().substring(0, 60) + "...");
        }

        textInfo.getChildren().addAll(date, title, desc);
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // --- 3. ACTIONS ---
        HBox bottomActions = new HBox(8);
        bottomActions.setAlignment(Pos.CENTER_LEFT);

        Button viewAction = new Button("👁 View Post");
        GamerVaultStyles.applyGhostButton(viewAction);
        viewAction.setStyle(viewAction.getStyle() + "-fx-text-fill: " + GamerVaultStyles.TEXT_PRIMARY + ";");
        viewAction.setOnMouseEntered(
                e -> viewAction.setStyle(viewAction.getStyle().replace(GamerVaultStyles.TEXT_PRIMARY, accentColor)));
        viewAction.setOnMouseExited(
                e -> viewAction.setStyle(viewAction.getStyle().replace(accentColor, GamerVaultStyles.TEXT_PRIMARY)));

        // OS Level Link Routing
        viewAction.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(model.getUrl()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        Button deleteBtn = new Button("🗑");
        deleteBtn.setPrefSize(30, 30);
        GamerVaultStyles.applyGhostButton(deleteBtn);
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
                "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #EF4444; -fx-background-radius: 8; -fx-cursor: hand;"));
        deleteBtn.setOnMouseExited(e -> GamerVaultStyles.applyGhostButton(deleteBtn));

        deleteBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Content");
            alert.setHeaderText("Delete '" + model.getTitle() + "'?");
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK) {
                    if (ContentController.deleteContent(model.getContentId()))
                        refreshContent();
                }
            });
        });

        bottomActions.getChildren().addAll(viewAction, hSpacer, deleteBtn);
        card.getChildren().addAll(thumbnail, textInfo, spacer, bottomActions);
        return card;
    }

    private void openAddContentDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Content");
        dialog.getDialogPane().setStyle(
                "-fx-background-color: #0B0F19; -fx-border-color: rgba(139,92,246,0.3); -fx-border-width: 1; -fx-border-radius: 8;");

        VBox rootBox = new VBox(15);
        rootBox.setPadding(new Insets(25));
        rootBox.setPrefWidth(500);

        TextField tUrl = new TextField();
        tUrl.setPromptText("Paste YouTube or Instagram Link...");
        TextField tTitle = new TextField();
        tTitle.setPromptText("Post Title");
        TextArea tDesc = new TextArea();
        tDesc.setPromptText("Post Description (Optional)");
        tDesc.setPrefRowCount(3);
        tDesc.setStyle(
                "-fx-control-inner-background: #1A1D29; -fx-text-fill: white; -fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.06); -fx-border-width: 1; -fx-border-radius: 8;");

        Text helperText = new Text(
                "ℹ️ Note: YouTube thumbnails will be fetched automatically. For Instagram, please upload a custom thumbnail below.");
        helperText.setFill(Color.web(GamerVaultStyles.ACCENT_CYAN));
        helperText.setFont(Font.font("Arial", 11));
        helperText.setWrappingWidth(450);

        File[] selectedImage = new File[1];
        Button chooseImageBtn = new Button("📁 Optional: Custom Thumbnail");
        GamerVaultStyles.applyGhostButton(chooseImageBtn);
        Text selectedFileLabel = new Text("No file selected.");
        selectedFileLabel.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

        chooseImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File file = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                selectedImage[0] = file;
                selectedFileLabel.setText(file.getName());
                selectedFileLabel.setFill(Color.web(GamerVaultStyles.ACCENT_GREEN));
            }
        });

        VBox imageUploadBox = new VBox(5);
        imageUploadBox.getChildren().addAll(chooseImageBtn, selectedFileLabel);

        rootBox.getChildren().addAll(
                createFormRow("Link URL", tUrl),
                helperText,
                createFormRow("Title", tTitle),
                createFormRow("Description", tDesc),
                createFormRow("Custom Thumbnail", imageUploadBox));

        dialog.getDialogPane().setContent(rootBox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Node okBtn = dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (okBtn instanceof Button)
            GamerVaultStyles.applyGradientButton((Button) okBtn, GamerVaultStyles.ACCENT_PURPLE,
                    GamerVaultStyles.ACCENT_PURPLE_DARK, "white");

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK && !tUrl.getText().trim().isEmpty() && !tTitle.getText().trim().isEmpty()) {

                contentGrid.getChildren().clear();
                Text loadingTxt = new Text("Publishing Content...");
                loadingTxt.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
                contentGrid.getChildren().add(loadingTxt);

                new Thread(() -> {
                    String thumbnailUrl = "";
                    if (selectedImage[0] != null) {
                        try {
                            thumbnailUrl = new StorageDao().uploadTournamentImage(selectedImage[0]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    boolean status = ContentController.publishContent(
                            tTitle.getText().trim(), tDesc.getText().trim(), tUrl.getText().trim(), thumbnailUrl);

                    if (status)
                        Platform.runLater(this::refreshContent);
                }).start();
            }
        });
    }

    private VBox createFormRow(String label, Node inputField) {
        VBox box = new VBox(5);
        Text t = new Text(label);
        t.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        t.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        if (inputField instanceof TextField) {
            HBox input = GamerVaultStyles.createStyledInput((TextField) inputField, GamerVaultStyles.ACCENT_PURPLE);
            input.setPrefHeight(40);
            box.getChildren().addAll(t, input);
        } else {
            box.getChildren().addAll(t, inputField);
        }
        return box;
    }
}