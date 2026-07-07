package com.example.view.player;

import com.example.controller.AuthController;
import com.example.controller.player.ProfileController;
import com.example.model.UserModel;
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

        // Context
        private Text emailText;
        private Text createdAtText;
        private Text vaultIdText;

        // Avatar (future)
        // private ImageView profileImageView;
        private Text avatarPlaceholder;

        // METHODS

        // Setters of the scene and stage of the Profile Screen
        public void setProfileScreenScene(Scene profileScreenScene) {
                this.profileScreenScene = profileScreenScene;
        }

        public void setProfileScreenStage(Stage profileScreenStage) {
                this.profileScreenStage = profileScreenStage;
        }

        /*
         * This is the start of the ProfileScreen.
         * This method begins the UI of the screen.
         * 
         * This method is the beginning of the UI of the screen.
         * 
         */
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
                container.setPadding(new Insets(10, 20, 40, 20)); // Top, Right, Bottom, Left

                // 1. HERO BANNER
                StackPane heroBanner = createHeroBanner();
                GamerVaultAnimations.fadeInUp(heroBanner, 0, 500);

                // 2. WIDE STATS BAR
                HBox statsBar = createStatsBar();
                GamerVaultAnimations.fadeInUp(statsBar, 150, 500);

                // 3. MAIN CONTENT SPLIT (Settings Form vs Account Context)
                HBox splitContent = new HBox(20);

                // Left Side: Profile Settings Form
                VBox settingsForm = createProfileSettingsForm();
                HBox.setHgrow(settingsForm, Priority.ALWAYS); // Let it take most of the space

                // Right Side: Context & Security Cards
                VBox rightSidebar = new VBox(20);
                rightSidebar.setPrefWidth(320); // Fixed width for sidebar cards
                VBox accContext = createAccountContextCard();
                VBox secActions = createSecurityActionsCard();
                rightSidebar.getChildren().addAll(accContext, secActions);

                splitContent.getChildren().addAll(settingsForm, rightSidebar);
                GamerVaultAnimations.fadeInUp(splitContent, 300, 500);

                container.getChildren().addAll(heroBanner, statsBar, splitContent);

                return container;
        }

        /*
         * HERO BANNER (Avatar, Name, Tags)
         * This method is the creation of the complete banner of the player.
         * It contains the avatar, name, tags and a purple circle to the right side of
         * the hero banner.
         * 
         * It creates the 1st section of top row UI in Profile section.
         */
        private StackPane createHeroBanner() {
                StackPane bannerContainer = new StackPane();
                bannerContainer.setPrefHeight(200);

                // Rich Gradient/Map background
                bannerContainer.setStyle(
                                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1a162e, #0c0817, #020617); -fx-background-radius: 16 16 0 0; -fx-border-color: rgba(255,255,255,0.08); -fx-border-width: 1 1 0 1; -fx-border-radius: 16 16 0 0;");

                // Add an animated background blob to the banner
                // A purple circle to the right side of the hero banner
                Circle bannerBlob = GamerVaultAnimations.buildBlob(80, GamerVaultStyles.ACCENT_PURPLE, 0.3, 50);
                StackPane.setAlignment(bannerBlob, Pos.TOP_RIGHT);
                bannerBlob.setTranslateX(-50);
                bannerBlob.setTranslateY(20);
                GamerVaultAnimations.pulseGlow(bannerBlob, 4.0);
                bannerContainer.getChildren().add(bannerBlob);

                HBox contentBox = new HBox(25);
                contentBox.setAlignment(Pos.CENTER_LEFT);
                contentBox.setPadding(new Insets(30, 30, 20, 30));

                // AVATAR
                StackPane avatarBox = new StackPane();
                avatarBox.setPrefSize(120, 120);
                avatarBox.setStyle(
                                "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 16; -fx-border-color: "
                                                + GamerVaultStyles.ACCENT_PURPLE
                                                + "; -fx-border-width: 2; -fx-border-radius: 16;");
                DropShadow avatarGlow = new DropShadow(15,
                                Color.web(GamerVaultStyles.ACCENT_PURPLE, 0.4));
                avatarBox.setEffect(avatarGlow);

                avatarPlaceholder = new Text("👤");
                avatarPlaceholder.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
                avatarPlaceholder.setFont(Font.font(60));
                avatarBox.getChildren().add(avatarPlaceholder);

                // USER INFO
                VBox infoBox = new VBox(8);
                infoBox.setAlignment(Pos.CENTER_LEFT);
                VBox.setMargin(infoBox, new Insets(15, 0, 0, 0));

                // Name Row
                HBox nameRow = new HBox(15);
                nameRow.setAlignment(Pos.BOTTOM_LEFT);

                playerNameText = new Text();
                playerNameText.setFill(Color.WHITE);
                playerNameText.setFont(Font.font("Arial", FontWeight.BOLD, 42));

                ignText = new Text();
                ignText.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
                ignText.setFont(Font.font("Arial", FontWeight.BOLD, 22));
                HBox.setMargin(ignText, new Insets(0, 0, 5, 0)); // Align visually with base of large text

                nameRow.getChildren().addAll(playerNameText, ignText);

                // Tags Row
                HBox tagsRow = new HBox(10);
                tagsRow.setAlignment(Pos.CENTER_LEFT);

                // Replace the tagsRow content with this:
                roleTagText = new Text("🎯 Entry Fragger");
                regionTagText = new Text("🌍 India Region");
                teamTagText = new Text("🤝 Team: S8UL");

                // Style them here...
                tagsRow.getChildren().addAll(
                                createTag(roleTagText, "rgba(34,211,238,0.15)", GamerVaultStyles.ACCENT_CYAN),
                                createTag(regionTagText, "rgba(255,255,255,0.1)", GamerVaultStyles.TEXT_PRIMARY),
                                createTag(teamTagText, "rgba(255,255,255,0.1)", GamerVaultStyles.TEXT_PRIMARY));

                infoBox.getChildren().addAll(nameRow, tagsRow);
                contentBox.getChildren().addAll(avatarBox, infoBox);
                bannerContainer.getChildren().add(contentBox);

                return bannerContainer;
        }

        /*
         * This method is used by the createHeroSection.
         * This method creates the small rectangle UI boxes for the user info.
         * 
         * text - The text to be displayed in the tag.
         * bgColor - The background color of the tag.
         * textColorHex - The text color of the tag.
         */
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

        /*
         * This method help in arranging the UI that is horizontal row of the stat in
         * Profile screen
         */
        private HBox createStatsBar() {
                HBox statsBar = new HBox(40);
                statsBar.setPadding(new Insets(20, 30, 20, 30));
                statsBar.setStyle(
                                "-fx-background-color: " + GamerVaultStyles.CARD_BG + "; " +
                                                "-fx-background-radius: 0 0 16 16; " + // Bottom corners rounded
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

        /*
         * This method is the arrangement of the widgets of the form, that is player
         * information.
         * All the fields relate to the information of the player.
         * 
         * Arranged all the textfields and drop down fields in this method accordingly.
         */
        private VBox createProfileSettingsForm() {
                VBox formCard = new VBox(20);
                formCard.setPadding(new Insets(25));
                GamerVaultStyles.applyGlassCard(formCard);

                playerNameField = new TextField();
                ignField = new TextField();
                primaryRoleCombo = new ComboBox<>();
                bioArea = new TextArea();

                // Header
                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);
                Text icon = new Text("⚙");
                icon.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
                icon.setFont(Font.font(20));
                Text title = new Text("Profile Settings");
                title.setFill(Color.WHITE);
                title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
                header.getChildren().addAll(icon, title);

                // Row 1: Display Name & Username
                HBox row1 = new HBox(20);
                VBox displayNameBox = createInputField("Display Name", playerNameField);
                VBox usernameBox = createInputField("Username (IGN)", ignField);
                HBox.setHgrow(displayNameBox, Priority.ALWAYS);
                HBox.setHgrow(usernameBox, Priority.ALWAYS);
                row1.getChildren().addAll(displayNameBox, usernameBox);

                // Row 2: Region & Primary Role
                HBox row2 = new HBox(20);

                // VBox roleBox = createDropdownField("Primary Role", "Entry Fragger ▼");
                // HBox.setHgrow(roleBox, Priority.ALWAYS);
                // row2.getChildren().addAll(roleBox);
                primaryRoleCombo.getItems().addAll("Entry Fragger", "IGL", "Support", "Sniper");
                primaryRoleCombo.setPrefHeight(45);
                primaryRoleCombo.setMaxWidth(Double.MAX_VALUE);
                primaryRoleCombo.setStyle(
                                "-fx-background-color: " + GamerVaultStyles.INPUT_BG + "; -fx-text-fill: white;");

                // 2. Add to layout
                VBox roleBox = new VBox(8);
                Text roleLabel = new Text("Primary Role");
                roleLabel.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
                roleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                roleBox.getChildren().addAll(roleLabel, primaryRoleCombo);
                HBox.setHgrow(roleBox, Priority.ALWAYS);
                row2.getChildren().addAll(roleBox);

                // Row 3: Bio
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

                // Focus glow for textarea
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

                // Buttons
                HBox buttonsRow = new HBox(15);
                buttonsRow.setAlignment(Pos.CENTER_RIGHT);

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

                buttonsRow.getChildren().addAll(discardBtn, saveBtn);

                formCard.getChildren().addAll(header, SizedBox.height(10), row1, row2, bioBox, SizedBox.height(10),
                                buttonsRow);
                return formCard;
        }

        /*
         * This method helps in creation of the TextField with al the proper and similar
         * styles to those TextFields.
         * 
         * labelStr - The label string of the field.
         * value - The value string of the field.
         */
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

        /*
         * This method is helper method for creating the dropdown field.
         * 
         * labelStr - The label string of the field.
         * value - The value string of the field.
         */
        // private VBox createDropdownField(String labelStr, String value) {
        // VBox box = new VBox(8);
        // Text label = new Text(labelStr);
        // label.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        // label.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        // Button field = new Button(value);
        // field.setPrefHeight(45);
        // field.setMaxWidth(Double.MAX_VALUE);
        // field.setAlignment(Pos.CENTER_LEFT);
        // GamerVaultStyles.applyGhostButton(field);
        // // Override background to match input fields
        // field.setStyle(field.getStyle() + "-fx-background-color: " +
        // GamerVaultStyles.INPUT_BG + ";");

        // box.getChildren().addAll(label, field);
        // return box;
        // }

        /*
         * RIGHT PANEL: ACCOUNT CONTEXT CARD
         * This method UI is non editable, it is for only displaying.
         * The UI shows the email Id registered and what day account is created along
         * with a unique ID for the GamerVault app, to maintain a unique user.
         */
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

        /*
         * This method is used for arranging the texts properly in the Right panel of
         * the Profile Screen.
         */
        private VBox createContextRow(String titleStr, Text valNode) {
                VBox box = new VBox(5);
                Text title = new Text(titleStr);
                title.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                title.setFont(Font.font("Arial", FontWeight.BOLD, 11));

                valNode.setFill(Color.WHITE); // Style the passed node
                valNode.setFont(Font.font("Arial", FontWeight.BOLD, 13));
                box.getChildren().addAll(title, valNode);

                return box;
        }

        /*
         * RIGHT PANEL: SECURITY ACTIONS CARD
         * 
         * This method have another button of logout from device in Profile screen.
         */
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

        private void loadUserProfile() {
                if (AuthController.currentUser != null && AuthController.currentUser.getUserId() != null) {
                        new Thread(() -> {
                                UserModel fetchedUser = ProfileController
                                                .getUserProfile(AuthController.currentUser.getUserId());

                                Platform.runLater(() -> {
                                        currentUser = fetchedUser;
                                        if (currentUser != null) {
                                                populateProfile();
                                        }
                                });
                        }).start();
                } else {
                        System.out.println("No active user session found.");
                }
        }

        private void populateProfile() {

                if (currentUser == null) {
                        return;
                }

                // Hero Banner
                playerNameText.setText(currentUser.getPlayerName());
                ignText.setText(currentUser.getIgn());

                roleTagText.setText("🎯 " + currentUser.getPrimaryRole());

                regionTagText.setText("🌍 India"); // Currently hardcoded

                teamTagText.setText("🤝 Team : Free Agent");

                // Form
                playerNameField.setText(currentUser.getPlayerName());
                ignField.setText(currentUser.getIgn());
                bioArea.setText(currentUser.getBio());

                totalMatchesText.setText(String.valueOf(currentUser.getTotalMatches()));
                averageKillsText.setText(String.format("%.1f",
                                (double) currentUser.getTotalKills() / currentUser.getTotalMatches()));
                averageDamageText.setText(String.valueOf(currentUser.getTotalDamage()));
                winRateText.setText(String.format("%.1f%%", currentUser.getWinRate()));

                String idStr = currentUser.getUserId();
                String safeId = idStr != null && idStr.length() >= 8 ? idStr.substring(0, 8) : idStr;
                vaultIdText.setText("GV-" + (safeId != null ? safeId.toUpperCase() : "UNKNOWN"));

                if (currentUser.getPrimaryRole() != null) {
                        primaryRoleCombo.setValue(currentUser.getPrimaryRole());
                }

                // Account
                emailText.setText(currentUser.getEmail());
                createdAtText.setText(currentUser.getCreatedAt());

        }
}