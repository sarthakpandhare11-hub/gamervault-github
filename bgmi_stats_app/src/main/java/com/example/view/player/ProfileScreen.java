package com.example.view.player;

import java.util.concurrent.CompletableFuture;

import com.example.controller.player.ConnectionController;
import com.example.controller.player.DirectMessageController;
import com.example.controller.player.ProfileController;
import com.example.model.UserModel;
import com.example.model.player.ProfileViewContext;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ProfileScreen {

        // VARIABLES
        Scene profileScreenScene;
        Stage profileScreenStage;

        private UserModel currentUser;

        // Hero
        private Text playerNameText;
        private Text ignText;

        private Text roleTagText;
        private Text regionTagText;
        private Text teamTagText;

        // Statistics
        private Text totalMatchesText;
        private Text averageKillsText;
        private Text averageDamageText;
        private Text winRateText;

        // Editable Fields
        private TextField playerNameField;
        private TextField ignField;
        private ComboBox<String> primaryRoleCombo;
        private TextArea bioArea;

        // UI References for Dynamic Access
        private HBox settingsButtonsRow; // <--- ADDED to fix fragile index access

        // Context
        private Text emailText;
        private Text createdAtText;
        private Text vaultIdText;

        private Text avatarPlaceholder;

        private ComboBox<String> privacyCombo;

        // METHODS

        public void setProfileScreenScene(Scene profileScreenScene) {
                this.profileScreenScene = profileScreenScene;
        }

        public void setProfileScreenStage(Stage profileScreenStage) {
                this.profileScreenStage = profileScreenStage;
        }

        public BorderPane startProfileScreen() {
                BorderPane root = new BorderPane();
                root.setStyle("-fx-background-color: transparent;");

                ScrollPane scroller = new ScrollPane();
                scroller.setFitToWidth(true);
                scroller.setContent(createProfileContent());
                scroller.setVbarPolicy(ScrollBarPolicy.NEVER);
                GamerVaultStyles.applyStyledScrollPane(scroller);

                root.setCenter(scroller);
                loadUserProfile();

                return root;
        }

        private VBox createProfileContent() {
                VBox container = new VBox(20);
                container.setPadding(new Insets(10, 20, 40, 20));

                StackPane heroBanner = createHeroBanner();
                GamerVaultAnimations.fadeInUp(heroBanner, 0, 500);

                HBox statsBar = createStatsBar();
                GamerVaultAnimations.fadeInUp(statsBar, 150, 500);

                HBox splitContent = new HBox(20);

                VBox settingsForm = createProfileSettingsForm();
                HBox.setHgrow(settingsForm, Priority.ALWAYS);

                VBox rightSidebar = new VBox(20);
                rightSidebar.setPrefWidth(320);
                VBox accContext = createAccountContextCard();
                VBox secActions = createSecurityActionsCard();
                rightSidebar.getChildren().addAll(accContext, secActions);

                splitContent.getChildren().addAll(settingsForm, rightSidebar);
                GamerVaultAnimations.fadeInUp(splitContent, 300, 500);

                container.getChildren().addAll(heroBanner, statsBar, splitContent);

                return container;
        }

        private StackPane createHeroBanner() {
                StackPane bannerContainer = new StackPane();
                bannerContainer.setPrefHeight(200);

                bannerContainer.setStyle(
                                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1a162e, #0c0817, #020617); -fx-background-radius: 16 16 0 0; -fx-border-color: rgba(255,255,255,0.08); -fx-border-width: 1 1 0 1; -fx-border-radius: 16 16 0 0;");

                Circle bannerBlob = GamerVaultAnimations.buildBlob(80, GamerVaultStyles.ACCENT_PURPLE, 0.3, 50);
                StackPane.setAlignment(bannerBlob, Pos.TOP_RIGHT);
                bannerBlob.setTranslateX(-50);
                bannerBlob.setTranslateY(20);
                GamerVaultAnimations.pulseGlow(bannerBlob, 4.0);
                bannerContainer.getChildren().add(bannerBlob);

                HBox contentBox = new HBox(25);
                contentBox.setAlignment(Pos.CENTER_LEFT);
                contentBox.setPadding(new Insets(30, 30, 20, 30));

                StackPane avatarBox = new StackPane();
                avatarBox.setPrefSize(120, 120);
                avatarBox.setStyle(
                                "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 16; -fx-border-color: "
                                                + GamerVaultStyles.ACCENT_PURPLE
                                                + "; -fx-border-width: 2; -fx-border-radius: 16;");
                DropShadow avatarGlow = new DropShadow(15, Color.web(GamerVaultStyles.ACCENT_PURPLE, 0.4));
                avatarBox.setEffect(avatarGlow);

                avatarPlaceholder = new Text("👤");
                avatarPlaceholder.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
                avatarPlaceholder.setFont(Font.font(60));
                avatarBox.getChildren().add(avatarPlaceholder);

                VBox infoBox = new VBox(8);
                infoBox.setAlignment(Pos.CENTER_LEFT);
                VBox.setMargin(infoBox, new Insets(15, 0, 0, 0));

                HBox nameRow = new HBox(15);
                nameRow.setAlignment(Pos.BOTTOM_LEFT);

                playerNameText = new Text();
                playerNameText.setFill(Color.WHITE);
                playerNameText.setFont(Font.font("Arial", FontWeight.BOLD, 42));

                ignText = new Text();
                ignText.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
                ignText.setFont(Font.font("Arial", FontWeight.BOLD, 22));
                HBox.setMargin(ignText, new Insets(0, 0, 5, 0));

                nameRow.getChildren().addAll(playerNameText, ignText);

                HBox tagsRow = new HBox(10);
                tagsRow.setAlignment(Pos.CENTER_LEFT);

                roleTagText = new Text("🎯 Entry Fragger");
                regionTagText = new Text("🌍 India Region");
                teamTagText = new Text("🤝 Team: S8UL");

                tagsRow.getChildren().addAll(
                                createTag(roleTagText, "rgba(34,211,238,0.15)", GamerVaultStyles.ACCENT_CYAN),
                                createTag(regionTagText, "rgba(255,255,255,0.1)", GamerVaultStyles.TEXT_PRIMARY),
                                createTag(teamTagText, "rgba(255,255,255,0.1)", GamerVaultStyles.TEXT_PRIMARY));

                infoBox.getChildren().addAll(nameRow, tagsRow);
                contentBox.getChildren().addAll(avatarBox, infoBox);
                bannerContainer.getChildren().add(contentBox);

                return bannerContainer;
        }

        private StackPane createTag(Text textNode, String bgColor, String textColorHex) {
                StackPane tag = new StackPane();
                tag.setPadding(new Insets(6, 12, 6, 12));
                tag.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 6; "
                                + "-fx-border-color: rgba(255,255,255,0.05); -fx-border-radius: 6;");

                textNode.setFill(Color.web(textColorHex));
                textNode.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                tag.getChildren().add(textNode);
                return tag;
        }

        private HBox createStatsBar() {
                HBox statsBar = new HBox(40);
                statsBar.setPadding(new Insets(20, 30, 20, 30));
                statsBar.setStyle(
                                "-fx-background-color: " + GamerVaultStyles.CARD_BG + "; " +
                                                "-fx-background-radius: 0 0 16 16; " +
                                                "-fx-border-color: " + GamerVaultStyles.CARD_BORDER + "; " +
                                                "-fx-border-width: 0 1 1 1; " +
                                                "-fx-border-radius: 0 0 16 16;");

                DropShadow barShadow = new DropShadow(25, Color.rgb(0, 0, 0, 0.4));
                barShadow.setOffsetY(8);
                statsBar.setEffect(barShadow);

                totalMatchesText = new Text("--");
                averageKillsText = new Text("--");
                averageDamageText = new Text("--");
                winRateText = new Text("--");

                statsBar.getChildren().addAll(
                                createDynamicStatItem("Total Matches", totalMatchesText),
                                createDynamicStatItem("Average Kills", averageKillsText),
                                createDynamicStatItem("Avg Damage", averageDamageText),
                                createDynamicStatItem("Win Rate", winRateText));

                return statsBar;
        }

        private VBox createDynamicStatItem(String titleStr, Text valueNode) {
                VBox box = new VBox(5);
                Text title = new Text(titleStr);
                title.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
                title.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                HBox valBox = new HBox(5);
                valBox.setAlignment(Pos.BOTTOM_LEFT);

                valueNode.setFill(Color.WHITE);
                valueNode.setFont(Font.font("Arial", FontWeight.BOLD, 32));
                valBox.getChildren().add(valueNode);

                box.getChildren().addAll(title, valBox);
                HBox.setHgrow(box, Priority.ALWAYS);
                return box;
        }

        private VBox createProfileSettingsForm() {
                VBox formCard = new VBox(20);
                formCard.setPadding(new Insets(25));
                GamerVaultStyles.applyGlassCard(formCard);

                playerNameField = new TextField();
                ignField = new TextField();
                primaryRoleCombo = new ComboBox<>();
                bioArea = new TextArea();

                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);
                Text icon = new Text("⚙");
                icon.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
                icon.setFont(Font.font(20));
                Text title = new Text("Profile Settings");
                title.setFill(Color.WHITE);
                title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
                header.getChildren().addAll(icon, title);

                HBox row1 = new HBox(20);
                VBox displayNameBox = createInputField("Display Name", playerNameField);
                VBox usernameBox = createInputField("Username (IGN)", ignField);
                HBox.setHgrow(displayNameBox, Priority.ALWAYS);
                HBox.setHgrow(usernameBox, Priority.ALWAYS);
                row1.getChildren().addAll(displayNameBox, usernameBox);

                HBox row2 = new HBox(20);
                primaryRoleCombo.getItems().addAll("Entry Fragger", "IGL", "Support", "Sniper");
                primaryRoleCombo.setPrefHeight(45);
                primaryRoleCombo.setMaxWidth(Double.MAX_VALUE);
                primaryRoleCombo.setStyle(
                                "-fx-background-color: " + GamerVaultStyles.INPUT_BG + "; -fx-text-fill: white;");

                VBox roleBox = new VBox(8);
                Text roleLabel = new Text("Primary Role");
                roleLabel.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
                roleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                roleBox.getChildren().addAll(roleLabel, primaryRoleCombo);
                HBox.setHgrow(roleBox, Priority.ALWAYS);

                privacyCombo = new ComboBox<>();
                privacyCombo.getItems().addAll("PUBLIC", "PRIVATE");
                privacyCombo.setPrefHeight(45);
                privacyCombo.setMaxWidth(Double.MAX_VALUE);
                privacyCombo.setStyle("-fx-background-color: " + GamerVaultStyles.INPUT_BG + "; -fx-text-fill: white;");

                VBox privacyBox = new VBox(8);
                Text privacyLabel = new Text("Account Privacy");
                privacyLabel.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
                privacyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                privacyBox.getChildren().addAll(privacyLabel, privacyCombo);
                HBox.setHgrow(privacyBox, Priority.ALWAYS);

                row2.getChildren().addAll(roleBox, privacyBox);

                VBox bioBox = new VBox(8);
                Text bioLabel = new Text("Player Bio");
                bioLabel.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
                bioLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                bioArea = new TextArea(
                                "Professional BGMI player for S8UL Esports. 2x Tournament MVP. Grinding daily to dominate the leaderboards.");
                bioArea.setWrapText(true);
                bioArea.setPrefRowCount(3);
                bioArea.setStyle(
                                "-fx-control-inner-background: " + GamerVaultStyles.INPUT_BG + "; "
                                                + "-fx-text-fill: white; -fx-background-color: transparent; "
                                                + "-fx-border-color: rgba(255,255,255,0.06); -fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10;");

                String focusStyle = "-fx-control-inner-background: " + GamerVaultStyles.INPUT_BG + "; "
                                + "-fx-text-fill: white; -fx-background-color: transparent; "
                                + "-fx-border-color: " + GamerVaultStyles.ACCENT_PURPLE
                                + "; -fx-border-width: 1.5; -fx-border-radius: 10; -fx-background-radius: 10; "
                                + "-fx-effect: dropshadow(three-pass-box, " + GamerVaultStyles.ACCENT_PURPLE
                                + "40, 10, 0, 0, 0);";
                String defaultStyle = bioArea.getStyle();
                bioArea.focusedProperty().addListener((obs, oldVal, newVal) -> {
                        bioArea.setStyle(newVal ? focusStyle : defaultStyle);
                });

                bioBox.getChildren().addAll(bioLabel, bioArea);

                // --- NEW: Using Class Level Variable instead of Local ---
                settingsButtonsRow = new HBox(15);
                settingsButtonsRow.setAlignment(Pos.CENTER_RIGHT);

                Button discardBtn = new Button("Discard");
                discardBtn.setPrefHeight(35);
                GamerVaultStyles.applyGhostButton(discardBtn);

                Button saveBtn = new Button("Save Changes");
                saveBtn.setPrefHeight(35);
                saveBtn.setPadding(new Insets(0, 20, 0, 20));

                saveBtn.setOnAction(e -> {
                        if (currentUser != null) {
                                currentUser.setPlayerName(playerNameField.getText());
                                currentUser.setIgn(ignField.getText());
                                currentUser.setBio(bioArea.getText());
                                currentUser.setPrimaryRole(primaryRoleCombo.getValue());

                                currentUser.setPrivacyStatus(privacyCombo.getValue());

                                saveBtn.setText("Saving...");
                                saveBtn.setDisable(true);

                                new Thread(() -> {
                                        boolean success = ProfileController.updateUserProfile(currentUser.getUserId(),
                                                        currentUser);

                                        Platform.runLater(() -> {
                                                saveBtn.setText("Save Changes");
                                                saveBtn.setDisable(false);
                                                if (success) {
                                                        System.out.println("Profile Saved!");
                                                }
                                        });
                                }).start();
                        }
                });

                GamerVaultStyles.applyGradientButton(saveBtn, GamerVaultStyles.ACCENT_PURPLE,
                                GamerVaultStyles.ACCENT_PURPLE_DARK, "white");

                settingsButtonsRow.getChildren().addAll(discardBtn, saveBtn);

                formCard.getChildren().addAll(header, SizedBox.height(10), row1, row2, bioBox, SizedBox.height(10),
                                settingsButtonsRow); // <-- Using the named reference
                return formCard;
        }

        private VBox createInputField(String labelStr, TextField field) {
                VBox box = new VBox(8);
                Text label = new Text(labelStr);
                label.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
                label.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                HBox styledContainer = GamerVaultStyles.createStyledInput(field, GamerVaultStyles.ACCENT_PURPLE);
                styledContainer.setPrefHeight(45);

                box.getChildren().addAll(label, styledContainer);
                return box;
        }

        private VBox createAccountContextCard() {
                VBox card = new VBox(20);
                card.setPadding(new Insets(25));
                GamerVaultStyles.applyGlassCard(card);
                GamerVaultAnimations.scaleOnHover(card, 1.02);

                emailText = new Text("vortexx.pro@s8ul.esports  ✓");
                createdAtText = new Text("Oct 14, 2021 (Season 4)");
                vaultIdText = new Text("GV-8924-XX1");

                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);
                Text icon = new Text("🛡");
                icon.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                icon.setFont(Font.font(20));

                Text title = new Text("Account\nContext");
                title.setFill(Color.WHITE);
                title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                header.getChildren().addAll(icon, title);

                VBox content = new VBox(15);
                content.getChildren().addAll(
                                createContextRow("Registered Email", emailText),
                                createContextRow("Account Created", createdAtText),
                                createContextRow("Vault ID", vaultIdText));
                card.getChildren().addAll(header, SizedBox.height(5), content);
                return card;
        }

        private VBox createContextRow(String titleStr, Text valNode) {
                VBox box = new VBox(5);
                Text title = new Text(titleStr);
                title.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                title.setFont(Font.font("Arial", FontWeight.BOLD, 11));

                valNode.setFill(Color.WHITE);
                valNode.setFont(Font.font("Arial", FontWeight.BOLD, 13));
                box.getChildren().addAll(title, valNode);

                return box;
        }

        private VBox createSecurityActionsCard() {
                VBox card = new VBox(15);
                card.setPadding(new Insets(25));
                GamerVaultStyles.applyGlassCard(card);
                GamerVaultAnimations.scaleOnHover(card, 1.02);

                Text title = new Text("Security Actions");
                title.setFill(Color.WHITE);
                title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

                Button changePassBtn = new Button("🔑 Change Password");
                changePassBtn.setPrefHeight(40);
                changePassBtn.setMaxWidth(Double.MAX_VALUE);
                GamerVaultStyles.applyGhostButton(changePassBtn);

                Button disconnectBtn = new Button("🚪 Disconnect Device");
                disconnectBtn.setPrefHeight(40);
                disconnectBtn.setMaxWidth(Double.MAX_VALUE);

                String delBase = "-fx-background-color: rgba(239,68,68,0.05); -fx-text-fill: #FCA5A5; "
                                + "-fx-background-radius: 10; -fx-cursor: hand; -fx-border-color: rgba(239,68,68,0.2); -fx-border-radius: 10; -fx-font-weight: bold;";
                String delHover = "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: white; "
                                + "-fx-background-radius: 10; -fx-cursor: hand; -fx-border-color: rgba(239,68,68,0.4); -fx-border-radius: 10; -fx-font-weight: bold;";
                disconnectBtn.setStyle(delBase);
                disconnectBtn.setOnMouseEntered(e -> disconnectBtn.setStyle(delHover));
                disconnectBtn.setOnMouseExited(e -> disconnectBtn.setStyle(delBase));
                GamerVaultAnimations.scaleOnPress(disconnectBtn);

                card.getChildren().addAll(title, SizedBox.height(5), changePassBtn, disconnectBtn);
                return card;
        }

        private ProfileViewContext currentContext;

        private void loadUserProfile() {
                new Thread(() -> {
                        ProfileViewContext context = ProfileController.getActiveProfileContext();
                        Platform.runLater(() -> {
                                this.currentContext = context;
                                if (currentContext != null && currentContext.getUser() != null) {
                                        this.currentUser = currentContext.getUser();
                                        populateProfile();
                                }
                        });
                }).start();
        }

        private void populateProfile() {
                if (currentUser == null || currentContext == null)
                        return;

                // Populate basic text (Name, IGN, Tags)
                playerNameText.setText(currentUser.getPlayerName());
                ignText.setText(currentUser.getIgn());
                roleTagText.setText("🎯 "
                                + (currentUser.getPrimaryRole() != null ? currentUser.getPrimaryRole() : "Unassigned"));
                regionTagText.setText("🌍 India");
                teamTagText.setText("🤝 Team: Free Agent");

                emailText.setText(currentContext.isOwnProfile() ? currentUser.getEmail() : "Hidden for privacy");
                createdAtText.setText(
                                currentUser.getCreatedAt() != null ? currentUser.getCreatedAt() : "Recently Joined");
                String safeId = currentUser.getUserId().length() >= 8 ? currentUser.getUserId().substring(0, 8)
                                : currentUser.getUserId();
                vaultIdText.setText("GV-" + safeId.toUpperCase());

                // --- PRIVACY GATE FOR STATS ---
                if (currentContext.isPrivate() && !currentContext.isConnected() && !currentContext.isOwnProfile()) {
                        totalMatchesText.setText("🔒");
                        averageKillsText.setText("Private");
                        averageDamageText.setText("Private");
                        winRateText.setText("🔒");
                } else {
                        totalMatchesText.setText(String.valueOf(currentUser.getTotalMatches()));
                        double kda = currentUser.getTotalMatches() > 0
                                        ? (double) currentUser.getTotalKills() / currentUser.getTotalMatches()
                                        : 0.0;
                        averageKillsText.setText(String.format("%.1f", kda));
                        averageDamageText.setText(String.valueOf(currentUser.getTotalDamage()));
                        winRateText.setText(String.format("%.1f%%", currentUser.getWinRate()));
                }

                // --- PRIVACY GATE FOR BUTTONS & FORM ---
                playerNameField.setText(currentUser.getPlayerName());
                ignField.setText(currentUser.getIgn());
                bioArea.setText(currentUser.getBio());
                primaryRoleCombo.setValue(
                                currentUser.getPrimaryRole() != null ? currentUser.getPrimaryRole() : "Entry Fragger");
                privacyCombo.setValue(
                                currentUser.getPrivacyStatus() != null ? currentUser.getPrivacyStatus() : "PUBLIC");

                if (!currentContext.isOwnProfile()) {
                        playerNameField.setEditable(false);
                        ignField.setEditable(false);
                        bioArea.setEditable(false);
                        primaryRoleCombo.setDisable(true);

                        String readOnlyStyle = "-fx-background-color: rgba(255,255,255,0.02); -fx-text-fill: #9CA3AF; -fx-border-color: rgba(255,255,255,0.05); -fx-border-radius: 8;";
                        playerNameField.setStyle(readOnlyStyle);
                        ignField.setStyle(readOnlyStyle);
                        bioArea.setStyle(readOnlyStyle);

                        Button actionBtn = new Button();
                        actionBtn.setPrefHeight(40);
                        actionBtn.setPadding(new Insets(0, 25, 0, 25));

                        if (currentContext.isPrivate() && !currentContext.isConnected()) {
                                // Not connected to a private profile -> Request Connection
                                actionBtn.setText("Checking status...");
                                actionBtn.setDisable(true);
                                actionBtn.setStyle(
                                                "-fx-background-color: #374151; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");

                                // Check whether a request is already pending before assuming this is a
                                // fresh, unsent state - otherwise revisiting the profile after sending a
                                // request would show "Send Connection Request" again every time.
                                CompletableFuture.runAsync(() -> {
                                        String status = ConnectionController
                                                        .getConnectionStatusWith(currentUser.getUserId());
                                        Platform.runLater(() -> {
                                                if ("PENDING".equals(status)) {
                                                        actionBtn.setText("⏳ Request Pending");
                                                        actionBtn.setDisable(true);
                                                } else {
                                                        actionBtn.setText("🔗 Send Connection Request");
                                                        actionBtn.setDisable(false);
                                                        actionBtn.setOnAction(e -> {
                                                                actionBtn.setText("Sending...");
                                                                actionBtn.setDisable(true);

                                                                CompletableFuture.runAsync(() -> {
                                                                        boolean success = ConnectionController
                                                                                        .sendRequest(currentUser
                                                                                                        .getUserId());
                                                                        Platform.runLater(() -> {
                                                                                actionBtn.setText(
                                                                                                success ? "Request Sent ✓"
                                                                                                                : "Failed - Retry");
                                                                                if (!success)
                                                                                        actionBtn.setDisable(false);
                                                                        });
                                                                });
                                                        });
                                                }
                                        });
                                });
                        } else {
                                // Public OR Connected -> Slide into DMs
                                actionBtn.setText("⚡ Connect & Message");
                                actionBtn.setStyle(
                                                "-fx-background-color: linear-gradient(to right, #0EA5E9, #0284C7); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
                                DropShadow glow = new DropShadow(15, Color.web("#0EA5E9", 0.4));
                                actionBtn.setEffect(glow);

                                actionBtn.setOnAction(e -> {
                                        actionBtn.setText("Connecting...");
                                        CompletableFuture.runAsync(() -> {
                                                String roomId = DirectMessageController
                                                                .initializeConnection(currentUser.getUserId());
                                                Platform.runLater(() -> {
                                                        if (roomId != null) {
                                                                DirectMessageController.activeChatRoomId = roomId;
                                                                PlayerDashboardSidebar.pageView = "directMessage";
                                                                PlayerMainScreen.instance.updateCenter();
                                                        }
                                                });
                                        });
                                });
                        }
                        settingsButtonsRow.getChildren().setAll(actionBtn);
                }
        }
}