package com.example.view.player;

import java.util.List;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.structure.SingleSelectionField;
import com.example.controller.AuthController;
import com.example.controller.player.ApplicationController;
import com.example.controller.player.ProfileController;
import com.example.controller.player.RecruitmentController;
import com.example.model.player.RecruitmentModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
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
import javafx.stage.Stage;

public class RecruitmentHubScreen {

    // VARIABLES
    Scene recruitmentHubScreenScene;
    Stage recruitmentHubScreenStage;

    // UI LOGIC VARIABLES
    private FlowPane cardsGrid;

    // METHODS

    // These are the setters for the scene and stage of the RecruitmentHub screen.
    public void setRecruitmentHubScreenScene(Scene recruitmentHubScreenScene) {
        this.recruitmentHubScreenScene = recruitmentHubScreenScene;
    }

    public void setRecruitmentHubScreenStage(Stage recruitmentHubScreenStage) {
        this.recruitmentHubScreenStage = recruitmentHubScreenStage;
    }

    /*
     * This is the start of the RecruitmentHubScreen.
     * This method begins the UI of the screen.
     */
    public BorderPane startRecruitmentHubScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        ScrollPane scroller = new ScrollPane();
        scroller.setFitToWidth(true);
        scroller.setContent(createRecruitmentContent());
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setCenter(scroller);

        return root;
    }

    /*
     * This method creates the content of the recruitment hub screen.
     */
    private VBox createRecruitmentContent() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(10, 20, 40, 20)); // Top, Right, Bottom, Left

        // 1. PAGE TITLE & SUBTITLE
        VBox headerBox = createHeaderBox();

        // 2. FILTERS / TABS ROW
        HBox filtersRow = createFiltersRow();
        GamerVaultAnimations.fadeInUp(filtersRow, 200, 500);

        // 3. RECRUITMENT CARDS GRID
        cardsGrid = new FlowPane();
        cardsGrid.setHgap(20);
        cardsGrid.setVgap(20);

        Text loadingText = new Text("Fetching open recruitments...");
        loadingText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        cardsGrid.getChildren().add(loadingText);

        // Stagger animate recruitment cards
        GamerVaultAnimations.staggerFadeInChildren(cardsGrid, 120);

        container.getChildren().addAll(headerBox, filtersRow, cardsGrid, SizedBox.height(10));

        loadRecruitmentData("ALL");

        return container;
    }

    /*
     * HEADER UI - HELPER METHOD
     */
    private VBox createHeaderBox() {
        Text title = new Text("Recruitment Hub");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 36));
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

        Text subtitle = new Text("Discover teams and recruitment opportunities in the BGMI competitive scene");
        subtitle.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        subtitle.setFont(Font.font("Poppins", FontWeight.NORMAL, 15));

        VBox headerBox = new VBox(5);
        headerBox.getChildren().addAll(title, subtitle);

        GamerVaultAnimations.fadeInUp(title, 0, 500);
        GamerVaultAnimations.fadeInUp(subtitle, 150, 500);

        return headerBox;
    }

    /*
     * This method creates the complete row of filters buttons.
     */
    private HBox createFiltersRow() {
        HBox tabsBox = new HBox(10);
        tabsBox.setAlignment(Pos.CENTER_LEFT);

        Button allRolesButton = new Button("All Roles");
        Button iglButton = new Button("IGL");
        Button entryFraggerButton = new Button("Entry Fragger");
        Button supportButton = new Button("Support");
        Button assaulterButton = new Button("Assaulter");
        Button sniperButton = new Button("Sniper");

        Button[] allTabs = { allRolesButton, iglButton, entryFraggerButton, supportButton, assaulterButton,
                sniperButton };

        // Functionality to all the buttons - call()
        allRolesButton.setOnAction(e -> {
            selectTab(allRolesButton, allTabs);
            loadRecruitmentData("ALL");
        });
        iglButton.setOnAction(e -> {
            selectTab(iglButton, allTabs);
            loadRecruitmentData("IGL");
        });
        entryFraggerButton.setOnAction(e -> {
            selectTab(entryFraggerButton, allTabs);
            loadRecruitmentData("ENTRY FRAGGER");
        });
        supportButton.setOnAction(e -> {
            selectTab(supportButton, allTabs);
            loadRecruitmentData("SUPPORT");
        });
        assaulterButton.setOnAction(e -> {
            selectTab(assaulterButton, allTabs);
            loadRecruitmentData("ASSAULTER");
        });
        sniperButton.setOnAction(e -> {
            selectTab(sniperButton, allTabs);
            loadRecruitmentData("SNIPER");
        });

        selectTab(allRolesButton, allTabs);

        tabsBox.getChildren().addAll(allTabs);

        // Creating button of recruitemnt card
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button createPostBtn = new Button("+ Create Post");
        createPostBtn.setPrefHeight(38);
        GamerVaultStyles.applyGradientButton(createPostBtn, GamerVaultStyles.ACCENT_PURPLE,
                GamerVaultStyles.ACCENT_PURPLE_DARK, "white");
        createPostBtn.setOnAction(e -> openFormsFXCreationDialog());

        tabsBox.getChildren().addAll(spacer, createPostBtn);

        return tabsBox;
    }

    /*
     * This method tells which button was clicked to make sure that button is
     * highlighted.
     */
    private void selectTab(Button clickedBtn, Button[] allTabs) {
        for (Button btn : allTabs) {
            getTabButtonStyle(btn, btn == clickedBtn);
        }
    }

    /*
     * This styles the filter buttons with active gradient and inactive ghost.
     */
    private void getTabButtonStyle(Button btn, boolean isActive) {
        btn.setPrefHeight(38);
        if (isActive) {
            btn.setStyle(
                    "-fx-background-color: linear-gradient(to right, " + GamerVaultStyles.ACCENT_PURPLE + ", "
                            + GamerVaultStyles.ACCENT_PURPLE_DARK + "); "
                            + "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; "
                            + "-fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 0 16 0 16;");
            DropShadow glow = new DropShadow(15,
                    Color.web(GamerVaultStyles.ACCENT_PURPLE, 0.4));
            btn.setEffect(glow);
        } else {
            btn.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.04); "
                            + "-fx-text-fill: " + GamerVaultStyles.TEXT_SECONDARY
                            + "; -fx-font-weight: bold; -fx-font-size: 13px; "
                            + "-fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 0 16 0 16; "
                            + "-fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 20;");
            btn.setEffect(null);
        }
    }

    private void openFormsFXCreationDialog() {
        // 1. Define standard String properties for text fields
        StringProperty teamNameProp = new SimpleStringProperty("");
        StringProperty lineUpProp = new SimpleStringProperty("");
        StringProperty regionProp = new SimpleStringProperty("");
        StringProperty weaponProp = new SimpleStringProperty("");
        DoubleProperty fdProp = new SimpleDoubleProperty(0.0);
        IntegerProperty dmgProp = new SimpleIntegerProperty(0);
        StringProperty expProp = new SimpleStringProperty("");
        StringProperty descProp = new SimpleStringProperty("");
        StringProperty reqsProp = new SimpleStringProperty("");

        // 2. Define the list of options as a standard Java List
        java.util.List<String> roleOptions = java.util.Arrays.asList("IGL", "ENTRY FRAGGER", "SUPPORT", "ASSAULTER",
                "SNIPER");

        // 3. COMPILER FIX: Create the selection field explicitly as a variable FIRST
        SingleSelectionField<String> roleField = Field
                .ofSingleSelectionType(roleOptions, 1) // 1 = ENTRY FRAGGER default
                .label("Role Needed")
                .required(true);

        // 4. Build FormsFX Layout
        Form createForm = Form.of(
                Group.of(
                        Field.ofStringType(teamNameProp).label("Team / Clan Name")
                                .required(true),
                        roleField, // <-- Passed cleanly here without type errors
                        Field.ofStringType(lineUpProp)
                                .label("Lineup Type (e.g., Main, Academy)").required(true),
                        Field.ofStringType(regionProp)
                                .label("Region Bracket (e.g., IN, ASIA)")),
                Group.of(
                        Field.ofDoubleType(fdProp).label("Minimum F/D Ratio")
                                .required(true),
                        Field.ofIntegerType(dmgProp).label("Minimum Avg Damage")
                                .required(true),
                        Field.ofStringType(expProp).label("Required Experience"),
                        Field.ofStringType(weaponProp)
                                .label("Preferred Primary Weapon")),
                Group.of(
                        Field.ofStringType(descProp).label("Short Description")
                                .multiline(true).required(true),
                        Field.ofStringType(reqsProp).label("Detailed Requirements")
                                .multiline(true)))
                .title("Elite Recruitment Console");

        // 5. Render and Mount into a Dialog window pane
        com.dlsc.formsfx.view.renderer.FormRenderer formRenderer = new com.dlsc.formsfx.view.renderer.FormRenderer(
                createForm);

        // UI FIX: Increase width to give labels room, preventing ellipses
        formRenderer.setPrefWidth(700);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Recruitment Post");

        // --- APPLY CUSTOM GAMERVAULT STYLING TO THE DIALOG ---
        DialogPane dialogPane = dialog.getDialogPane();
        dialog.getDialogPane().setPrefWidth(750);
        dialogPane.setContent(formRenderer);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Style the base dialog pane to be dark
        dialogPane.setStyle(
                "-fx-background-color: #0B0F19; -fx-border-color: rgba(139,92,246,0.3); -fx-border-width: 1; -fx-border-radius: 8;");

        // Attach the custom CSS file that styles FormsFX (See Step 2 below)
        try {
            dialogPane.getStylesheets().add(getClass().getResource("/formsfx-custom.css").toExternalForm());
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/formsfx-custom.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load formsfx-custom.css - " + e.getMessage());
        }

        // 6. Handle Submission
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                createForm.persist();

                if (!teamNameProp.get().trim().isEmpty() && !descProp.get().trim().isEmpty()) {

                    // Native, error-free value extraction
                    String finalRole = roleField.getSelection();

                    boolean success = RecruitmentController.publishRecruitment(
                            teamNameProp.get(),
                            finalRole,
                            lineUpProp.get(),
                            regionProp.get(),
                            weaponProp.get(),
                            fdProp.get(),
                            dmgProp.get(),
                            expProp.get(),
                            descProp.get(),
                            reqsProp.get(),
                            "");

                    if (success) {
                        loadRecruitmentData("ALL");
                    }
                }
            }
        });
    }

    private void loadRecruitmentData(String roleFilter) {
        new Thread(() -> {
            try {
                List<RecruitmentModel> posts = RecruitmentController.fetchActiveMarketPostings();

                Platform.runLater(() -> {
                    cardsGrid.getChildren().clear();
                    int delayIndex = 0;

                    for (RecruitmentModel post : posts) {
                        if (roleFilter.equals("ALL") || post.getRole().equalsIgnoreCase(roleFilter)) {
                            VBox card = createRecruitmentCard(post);
                            cardsGrid.getChildren().add(card);
                            GamerVaultAnimations.fadeInUp(card, delayIndex * 80, 400);
                            delayIndex++;
                        }
                    }

                    if (cardsGrid.getChildren().isEmpty()) {
                        Text emptyText = new Text("No open recruitments found for: " + roleFilter);
                        emptyText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                        emptyText.setFont(Font.font("Arial", 16));
                        cardsGrid.getChildren().add(emptyText);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    cardsGrid.getChildren().clear();
                    Text errTxt = new Text("⚠️ Failed to load recruitment data.");
                    errTxt.setFill(Color.web("#EF4444"));
                    cardsGrid.getChildren().add(errTxt);
                });
            }
        }).start();
    }

    private VBox createRecruitmentCard(RecruitmentModel post) {
        VBox card = new VBox(12);
        card.setPrefSize(350, 310);
        card.setPadding(new Insets(20));

        GamerVaultStyles.applyGlassCard(card);
        GamerVaultAnimations.scaleOnHover(card, 1.03);

        // TOP ROW: Avatar & Team
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        avatar.setPrefSize(40, 40);
        avatar.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8;");

        String initial = post.getTeamName() != null && !post.getTeamName().isEmpty()
                ? post.getTeamName().substring(0, 1)
                : "?";
        Text avatarInitials = new Text(initial);
        avatarInitials.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE));
        avatarInitials.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        avatar.getChildren().add(avatarInitials);

        VBox nameBox = new VBox(2);
        HBox nameBadgeRow = new HBox(5);
        Text name = new Text(post.getTeamName());
        name.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        name.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameBadgeRow.getChildren().add(name);

        if (post.isVerifiedTeam()) {
            Text vBadge = new Text("⭐");
            vBadge.setFont(Font.font(10));
            nameBadgeRow.getChildren().add(vBadge);
        }
        nameBox.getChildren().add(nameBadgeRow);

        // --- NEW: Interactive Identity Container ---
        HBox identityContainer = new HBox(12);
        identityContainer.setAlignment(Pos.CENTER_LEFT);
        identityContainer.getChildren().addAll(avatar, nameBox);

        // Charcoal to Electric Blue transition on hover
        identityContainer.setStyle("-fx-cursor: hand; -fx-padding: 4; -fx-background-radius: 8;");
        identityContainer.setOnMouseEntered(e -> identityContainer.setStyle(
                "-fx-cursor: hand; -fx-padding: 4; -fx-background-radius: 8; -fx-background-color: rgba(14, 165, 233, 0.1);"));
        identityContainer.setOnMouseExited(e -> identityContainer.setStyle(
                "-fx-cursor: hand; -fx-padding: 4; -fx-background-radius: 8; -fx-background-color: transparent;"));

        identityContainer.setOnMouseClicked(e -> {
            ProfileController.setTargetProfile(post.getAuthorId());

            PlayerDashboardSidebar.pageView = "profile";
            PlayerMainScreen.instance.updateCenter();
        });
        // -------------------------------------------

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Time calculations
        long diffMillis = System.currentTimeMillis() - post.getCreatedAt();
        long hours = diffMillis / (60 * 60 * 1000);
        String timeText = hours > 24 ? (hours / 24) + " days ago" : hours + " hrs ago";

        StackPane timeBadge = new StackPane();
        timeBadge.setPadding(new Insets(4, 8, 4, 8));
        timeBadge.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 6;");
        Text time = new Text("⏱ " + timeText);
        time.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        time.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        timeBadge.getChildren().add(time);

        // Add identityContainer instead of avatar and nameBox separately
        topRow.getChildren().addAll(identityContainer, spacer, timeBadge);

        // ROLE BADGE
        HBox roleRow = new HBox();
        StackPane roleBadge = new StackPane();
        roleBadge.setPadding(new Insets(4, 10, 4, 10));
        roleBadge.setStyle(
                "-fx-background-color: rgba(139,92,246,0.15); -fx-background-radius: 6; -fx-border-color: rgba(139,92,246,0.3); -fx-border-radius: 6;");
        Text roleTxt = new Text(post.getRole());
        roleTxt.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
        roleTxt.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        roleBadge.getChildren().add(roleTxt);
        roleRow.getChildren().add(roleBadge);

        // DYNAMIC THRESHOLD BADGES
        HBox statBadgesRow = new HBox(8);
        statBadgesRow.getChildren().addAll(
                createStatBadge("F/D: " + post.getMinFdRatio() + "+", "#F59E0B"),
                createStatBadge("DMG: " + post.getMinAvgDamage() + "+", GamerVaultStyles.ACCENT_CYAN),
                createStatBadge(post.getRegion(), GamerVaultStyles.TEXT_SECONDARY));

        // DESCRIPTION
        Text descText = new Text(post.getDescription());
        descText.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        descText.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        descText.setWrappingWidth(300);
        descText.setLineSpacing(3);

        Region vSpacer = new Region();
        VBox.setVgrow(vSpacer, Priority.ALWAYS);

        // BUTTONS
        HBox buttonsRow = new HBox(10);
        Button viewBtn = new Button("View Details");
        viewBtn.setPrefWidth(140);
        viewBtn.setPrefHeight(36);
        GamerVaultStyles.applyGhostButton(viewBtn);

        viewBtn.setOnAction(e -> {
            Dialog<ButtonType> detailDialog = new Dialog<>();
            detailDialog.setTitle("Recruitment Profile Details");

            VBox modalRoot = new VBox(20);
            modalRoot.setPadding(new Insets(25));
            modalRoot.setPrefWidth(450);
            modalRoot.setStyle(
                    "-fx-background-color: #0B0F19; -fx-border-color: rgba(139,92,246,0.2); -fx-border-width: 1; -fx-border-radius: 12;");

            // Header
            Text titleText = new Text(post.getTeamName().toUpperCase() + " Details");
            titleText.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            titleText.setFill(Color.WHITE);

            // Detailed statistics rows grid
            VBox detailsGrid = new VBox(12);
            detailsGrid.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.02); -fx-padding: 15; -fx-background-radius: 8;");

            detailsGrid.getChildren().addAll(
                    createDetailRow("Target Position:", post.getRole(), "#8B5CF6"),
                    createDetailRow("Roster Status:", post.getLineUpType(), "#67E8F9"),
                    createDetailRow("Regional Bracket:", post.getRegion(), "#FFFFFF"),
                    createDetailRow("Primary Weapon:", post.getPrimaryWeapon(), "#2DD4BF"),
                    createDetailRow("Required F/D Ratio:", post.getMinFdRatio() + "+", "#F59E0B"),
                    createDetailRow("Min Avg Damage:", post.getMinAvgDamage() + "+", "#EF4444"),
                    createDetailRow("Experience Tier:", post.getExperienceTier(), "#E2E8F0"));

            // Extra info blocks
            VBox textBlocks = new VBox(8);
            Text reqsHeading = new Text("Detailed Requirements & Description:");
            reqsHeading.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            reqsHeading.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

            Text detailedDesc = new Text(
                    post.getDetailedRequirements() != null && !post.getDetailedRequirements().isEmpty()
                            ? post.getDetailedRequirements()
                            : post.getDescription());
            detailedDesc.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
            detailedDesc.setFont(Font.font("Arial", 13));
            detailedDesc.setWrappingWidth(400);
            detailedDesc.setLineSpacing(3);
            textBlocks.getChildren().addAll(reqsHeading, detailedDesc);

            modalRoot.getChildren().addAll(titleText, detailsGrid, textBlocks);

            detailDialog.getDialogPane().setContent(modalRoot);
            detailDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            // Apply clean style modifications to window container buttons
            DialogPane pane = detailDialog.getDialogPane();
            pane.setStyle("-fx-background-color: #0B0F19;");
            Button closeButton = (Button) pane.lookupButton(ButtonType.CLOSE);
            if (closeButton != null) {
                GamerVaultStyles.applyGhostButton(closeButton);
                closeButton.setStyle(closeButton.getStyle() + "-fx-text-fill: white;");
            }

            detailDialog.showAndWait();
        });

        Button applyBtn = new Button("Checking...");
        applyBtn.setPrefWidth(140);
        applyBtn.setPrefHeight(36);
        applyBtn.setDisable(true);

        new Thread(() -> {
            boolean alreadyApplied = ApplicationController.hasApplied(post.getRecruitmentId());
            boolean isMyPost = AuthController.currentUser != null &&
                    post.getAuthorId().equals(AuthController.currentUser.getUserId());

            Platform.runLater(() -> {
                if (isMyPost) {
                    applyBtn.setText("Your Post");
                    applyBtn.setStyle(
                            "-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: #6b7280; -fx-background-radius: 6;");
                    applyBtn.setDisable(true);
                } else if (alreadyApplied) {
                    applyBtn.setText("Applied");
                    applyBtn.setStyle(
                            "-fx-background-color: #4b5563; -fx-text-fill: #9ca3af; -fx-background-radius: 6;");
                    applyBtn.setDisable(true);
                } else {
                    applyBtn.setText("Apply Now");
                    applyBtn.setDisable(false);
                    GamerVaultStyles.applyGradientButton(applyBtn, GamerVaultStyles.ACCENT_PURPLE,
                            GamerVaultStyles.ACCENT_PURPLE_DARK, "white");

                    applyBtn.setOnAction(e -> {
                        applyBtn.setText("Applying...");
                        applyBtn.setDisable(true);

                        new Thread(() -> {
                            boolean success = ApplicationController.applyToPost(post);
                            Platform.runLater(() -> {
                                if (success) {
                                    applyBtn.setText("Applied");
                                    applyBtn.setStyle(
                                            "-fx-background-color: #4b5563; -fx-text-fill: #9ca3af; -fx-background-radius: 6;");
                                } else {
                                    applyBtn.setText("Apply Now");
                                    applyBtn.setDisable(false);
                                    GamerVaultStyles.applyGradientButton(applyBtn, GamerVaultStyles.ACCENT_PURPLE,
                                            GamerVaultStyles.ACCENT_PURPLE_DARK, "white");

                                    Alert errorAlert = new Alert(Alert.AlertType.ERROR,
                                            "Failed to send application. Please try again.");
                                    errorAlert.getDialogPane().setStyle(
                                            "-fx-background-color: #0B0F19; -fx-border-color: rgba(239,68,68,0.3); -fx-border-width: 1; -fx-border-radius: 8;");
                                    Node contentLabel = errorAlert.getDialogPane().lookup(".content.label");
                                    if (contentLabel != null)
                                        contentLabel.setStyle("-fx-text-fill: white;");
                                    errorAlert.show();
                                }
                            });
                        }).start();
                    });
                }
            });
        }).start();
        // ==========================================

        buttonsRow.getChildren().addAll(viewBtn, applyBtn);

        card.getChildren().addAll(topRow, roleRow, statBadgesRow, descText, vSpacer, buttonsRow);
        return card;
    }

    // Helper for threshold badges
    private StackPane createStatBadge(String text, String colorHex) {
        StackPane badge = new StackPane();
        badge.setPadding(new Insets(3, 8, 3, 8));
        badge.setStyle(
                "-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 4; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 4;");
        Text t = new Text(text);
        t.setFill(Color.web(colorHex));
        t.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        badge.getChildren().add(t);
        return badge;
    }

    private HBox createDetailRow(String labelStr, String valueStr, String valueColorHex) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Text label = new Text(labelStr);
        label.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        label.setFont(Font.font("Arial", FontWeight.NORMAL, 13));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Text value = new Text(valueStr != null ? valueStr : "N/A");
        value.setFill(Color.web(valueColorHex));
        value.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        row.getChildren().addAll(label, spacer, value);
        return row;
    }
}