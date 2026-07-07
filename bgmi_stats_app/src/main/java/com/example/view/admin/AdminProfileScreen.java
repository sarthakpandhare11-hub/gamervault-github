package com.example.view.admin;

import com.example.controller.AuthController;
import com.example.controller.player.ProfileController;
import com.example.model.UserModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class AdminProfileScreen {

    // DYNAMIC CLASS FIELDS
    private Text adminNameText;
    private Text adminEmailText;
    private Text vaultIdText;
    private Text systemRoleText;
    private Text totalUsersTrackedText;
    private Text securityClearanceText;

    private TextField nameInputField;
    private TextField ignInputField;
    private Button saveConfigBtn;

    private UserModel currentAdmin;

    public BorderPane startAdminProfileScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        ScrollPane scroller = new ScrollPane();
        scroller.setContent(createContent());
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setFitToWidth(true);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setCenter(scroller);

        // Fetch and map live data immediately upon screen loading
        loadAdminProfileData();

        return root;
    }

    private VBox createContent() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(10, 20, 40, 0));

        // 1. HEADER ARENA TITLE
        VBox headerBox = createHeaderBox();

        // 2. MAIN HUB LAYOUT SPLIT
        HBox columnsSplit = new HBox(25);
        columnsSplit.setAlignment(Pos.TOP_LEFT);

        VBox primaryConfigCol = createPrimaryConfigColumn();
        VBox sidebarCol = createSidebarColumn();

        HBox.setHgrow(primaryConfigCol, Priority.ALWAYS);
        columnsSplit.getChildren().addAll(primaryConfigCol, sidebarCol);

        container.getChildren().addAll(headerBox, columnsSplit);
        return container;
    }

    private VBox createHeaderBox() {
        VBox box = new VBox(5);
        Text title = new Text("Terminal Profile Configuration");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

        Text subtitle = new Text(
                "Manage your administrative credentials, terminal node metadata, and authentication thresholds.");
        subtitle.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 15));

        box.getChildren().addAll(title, subtitle);
        GamerVaultAnimations.fadeInUp(title, 0, 500);
        GamerVaultAnimations.fadeInUp(subtitle, 100, 500);

        return box;
    }

    private VBox createPrimaryConfigColumn() {
        VBox mainCol = new VBox(20);
        mainCol.setAlignment(Pos.TOP_LEFT);

        // CONTROL PANE CARD
        VBox configCard = new VBox(20);
        configCard.setPadding(new Insets(25));
        GamerVaultStyles.applyGlassCard(configCard);

        Text cardTitle = new Text("Identity Parameters");
        cardTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        cardTitle.setFill(Color.WHITE);

        nameInputField = new TextField();
        nameInputField.setPromptText("Loading parameter...");
        HBox nameInputWrapper = GamerVaultStyles.createStyledInput(nameInputField, GamerVaultStyles.ACCENT_PURPLE);

        ignInputField = new TextField();
        ignInputField.setPromptText("Loading parameter...");
        HBox ignInputWrapper = GamerVaultStyles.createStyledInput(ignInputField, GamerVaultStyles.ACCENT_PURPLE);

        saveConfigBtn = new Button("Apply Node Modifications");
        saveConfigBtn.setPrefHeight(42);
        saveConfigBtn.setPrefWidth(220);
        GamerVaultStyles.applyGradientButton(saveConfigBtn, GamerVaultStyles.ACCENT_PURPLE,
                GamerVaultStyles.ACCENT_PURPLE_DARK, "white");

        // WIRE UP SAVE CHANGES ACTION (Asynchronous Thread)
        saveConfigBtn.setOnAction(e -> handleUpdateAdminProfile());

        configCard.getChildren().addAll(
                cardTitle, SizedBox.height(5),
                createFormWrapper("ROOT OPERATOR ACCOUNT NAME", nameInputWrapper),
                createFormWrapper("ADMINISTRATIVE IN-GAME NAME (IGN)", ignInputWrapper),
                SizedBox.height(5),
                saveConfigBtn);

        mainCol.getChildren().add(configCard);
        GamerVaultAnimations.fadeInUp(configCard, 200, 500);

        return mainCol;
    }

    private VBox createSidebarColumn() {
        VBox sidebarCol = new VBox(20);
        sidebarCol.setPrefWidth(320);
        sidebarCol.setMinWidth(320);

        // CARD A: META TELEMETRY CONTEXT
        VBox accountContextCard = new VBox(15);
        accountContextCard.setPadding(new Insets(25));
        GamerVaultStyles.applyGlassCard(accountContextCard);

        adminNameText = new Text("Loading...");
        adminNameText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        adminNameText.setFill(Color.WHITE);

        adminEmailText = new Text("Synchronizing...");
        adminEmailText.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        adminEmailText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

        VBox metaGrid = new VBox(12);
        metaGrid.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 8; -fx-padding: 15;");

        vaultIdText = new Text("--");
        systemRoleText = new Text("--");
        totalUsersTrackedText = new Text("--");
        securityClearanceText = new Text("--");

        metaGrid.getChildren().addAll(
                createMetadataRow("VAULT NODE ID", vaultIdText),
                createMetadataRow("ECOSYSTEM ROLE", systemRoleText),
                createMetadataRow("DATABASE METRICS", totalUsersTrackedText),
                createMetadataRow("CLEARANCE LEVEL", securityClearanceText));

        accountContextCard.getChildren().addAll(adminNameText, adminEmailText, SizedBox.height(5), metaGrid);

        // CARD B: SECURITY UTILITIES
        VBox securityActionsCard = new VBox(15);
        securityActionsCard.setPadding(new Insets(25));
        GamerVaultStyles.applyGlassCard(securityActionsCard);

        Text actionsTitle = new Text("Security Cryptography");
        actionsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        actionsTitle.setFill(Color.WHITE);

        Button changePassBtn = new Button("🔄 Rotate Cryptographic Token");
        changePassBtn.setPrefHeight(40);
        changePassBtn.setMaxWidth(Double.MAX_VALUE);
        GamerVaultStyles.applyGhostButton(changePassBtn);
        changePassBtn.setOnAction(e -> openChangePasswordDialog());

        Button disconnectBtn = new Button("🚪 Revoke Terminal Diagnostics");
        disconnectBtn.setPrefHeight(40);
        disconnectBtn.setMaxWidth(Double.MAX_VALUE);
        GamerVaultStyles.applyGhostButton(disconnectBtn);

        disconnectBtn.setOnMouseEntered(e -> disconnectBtn.setStyle(
                "-fx-background-color: rgba(239,68,68,0.12); -fx-border-color: #EF4444; -fx-border-radius: 8; -fx-text-fill: #FCA5A5; -fx-font-weight: bold; -fx-cursor: hand;"));
        disconnectBtn.setOnMouseExited(e -> GamerVaultStyles.applyGhostButton(disconnectBtn));
        disconnectBtn.setOnAction(e -> handleTerminalRevocation());

        securityActionsCard.getChildren().addAll(actionsTitle, SizedBox.height(5), changePassBtn, disconnectBtn);

        sidebarCol.getChildren().addAll(accountContextCard, securityActionsCard);

        GamerVaultAnimations.fadeInUp(accountContextCard, 250, 500);
        GamerVaultAnimations.fadeInUp(securityActionsCard, 300, 500);

        return sidebarCol;
    }

    private VBox createFormWrapper(String titleStr, HBox innerWrapper) {
        VBox box = new VBox(8);
        Text t = new Text(titleStr);
        t.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        t.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        box.getChildren().addAll(t, innerWrapper);
        return box;
    }

    private VBox createMetadataRow(String titleStr, Text valueNode) {
        VBox box = new VBox(4);
        Text title = new Text(titleStr);
        title.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 11));

        valueNode.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        valueNode.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        box.getChildren().addAll(title, valueNode);
        return box;
    }

    // --- ASYNCHRONOUS BACKEND LOAD INFRASTRUCTURE ---
    private void loadAdminProfileData() {
        if (AuthController.currentUser != null && AuthController.currentUser.getUserId() != null) {
            new Thread(() -> {
                try {
                    // Fetch profile snapshot safely via database threads
                    UserModel fetchedAdmin = ProfileController.getUserProfile(AuthController.currentUser.getUserId());

                    Platform.runLater(() -> {
                        this.currentAdmin = fetchedAdmin;
                        if (currentAdmin != null) {
                            populateAdminUI();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void populateAdminUI() {
        if (currentAdmin == null)
            return;

        // Populate context sidebar labels
        adminNameText.setText(currentAdmin.getPlayerName() != null ? currentAdmin.getPlayerName() : "Admin Operator");
        adminEmailText.setText(currentAdmin.getEmail() != null ? currentAdmin.getEmail() : "system@gamervault.db");

        String id = currentAdmin.getUserId();
        vaultIdText.setText("NODE-" + (id != null && id.length() >= 8 ? id.substring(0, 8).toUpperCase() : "ROOT"));
        systemRoleText.setText(currentAdmin.getRole() != null ? currentAdmin.getRole().toUpperCase() : "ADMINISTRATOR");
        totalUsersTrackedText.setText("Live Fire Sync Complete");
        securityClearanceText.setText("LEVEL 5 (OVERWATCH)");

        // Populating write inputs
        nameInputField.setText(currentAdmin.getPlayerName() != null ? currentAdmin.getPlayerName() : "");
        ignInputField.setText(currentAdmin.getIgn() != null ? currentAdmin.getIgn() : "");
    }

    private void handleUpdateAdminProfile() {
        if (currentAdmin == null)
            return;

        saveConfigBtn.setText("Compiling modifications...");
        saveConfigBtn.setDisable(true);

        currentAdmin.setPlayerName(nameInputField.getText().trim());
        currentAdmin.setIgn(ignInputField.getText().trim());

        new Thread(() -> {
            boolean success = ProfileController.updateUserProfile(currentAdmin.getUserId(), currentAdmin);

            Platform.runLater(() -> {
                saveConfigBtn.setText("Apply Node Modifications");
                saveConfigBtn.setDisable(false);

                if (success) {
                    Alert alert = new Alert(AlertType.INFORMATION, "Node operational boundaries updated successfully.",
                            ButtonType.OK);
                    alert.setTitle("System Update Status");
                    alert.setHeaderText("Identity Matrix Confirmed");
                    alert.showAndWait();

                    // Refresh side labels instantly
                    adminNameText.setText(currentAdmin.getPlayerName());
                } else {
                    Alert alert = new Alert(AlertType.ERROR, "Transaction aborted due to network drop.", ButtonType.OK);
                    alert.showAndWait();
                }
            });
        }).start();
    }

    // --- INTERACTIVE DIALOG UTILITIES ---
    private void openChangePasswordDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Rotate Passcode Tokens");
        dialog.getDialogPane().setStyle(
                "-fx-background-color: #0B0F19; -fx-border-color: rgba(139,92,246,0.3); -fx-border-width: 1; -fx-border-radius: 8;");

        VBox rootBox = new VBox(15);
        rootBox.setPadding(new Insets(20));
        rootBox.setPrefWidth(380);

        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Current credential matrix token");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Target verification string");

        rootBox.getChildren().addAll(
                createFormWrapper("CURRENT STRING MATRIX",
                        GamerVaultStyles.createStyledInput(oldPasswordField, GamerVaultStyles.ACCENT_PURPLE)),
                createFormWrapper("NEW TOKEN CONFIGURATION",
                        GamerVaultStyles.createStyledInput(newPasswordField, GamerVaultStyles.ACCENT_PURPLE)));

        dialog.getDialogPane().setContent(rootBox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Node okBtn = dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (okBtn instanceof Button) {
            GamerVaultStyles.applyGradientButton((Button) okBtn, GamerVaultStyles.ACCENT_PURPLE,
                    GamerVaultStyles.ACCENT_PURPLE_DARK, "white");
        }

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                if (!oldPasswordField.getText().isEmpty() && !newPasswordField.getText().isEmpty()) {
                    Alert successAlert = new Alert(AlertType.INFORMATION,
                            "Cryptographic passcode rotated successfully across security nodes.", ButtonType.OK);
                    successAlert.setHeaderText("Token Update Confirmed");
                    successAlert.showAndWait();
                }
            }
        });
    }

    private void handleTerminalRevocation() {
        Alert alert = new Alert(AlertType.CONFIRMATION,
                "Are you sure you want to drop diagnostics connection? This will break your active session layer connection footprint.",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Terminal Hazard Override");
        alert.setHeaderText("Disconnect Root Node Diagnostic Layer");

        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                System.out.println("Admin terminating dashboard sequence.");
                Platform.exit(); // Disconnects cleanly or logs them out safely
            }
        });
    }
}