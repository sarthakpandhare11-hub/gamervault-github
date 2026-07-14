package com.example.view.player;

import com.example.controller.player.ApplicationController;
import com.example.controller.player.DirectMessageController;
import com.example.controller.player.ProfileController;
import com.example.model.player.ApplicationModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;

public class RecruitmentInboxScreen {

    private VBox inboxContainer;

    public BorderPane startRecruitmentInboxScreen() {

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        // --- HEADER SECTION ---
        VBox headerBox = new VBox(5);
        headerBox.setPadding(new Insets(10, 20, 20, 20));

        Text title = new Text("Scout's Inbox");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 36));
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

        Text subtitle = new Text("Review pending applications and recruit top talent to your roster.");
        subtitle.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        subtitle.setFont(Font.font("Poppins", FontWeight.NORMAL, 15));

        headerBox.getChildren().addAll(title, subtitle);
        GamerVaultAnimations.fadeInUp(title, 0, 500);
        GamerVaultAnimations.fadeInUp(subtitle, 150, 500);

        // --- INBOX CONTAINER ---
        inboxContainer = new VBox(15);
        inboxContainer.setPadding(new Insets(10, 20, 40, 20));

        ScrollPane scroller = new ScrollPane(inboxContainer);
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setFitToWidth(true);
        scroller.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.setTop(headerBox);
        root.setCenter(scroller);

        loadInbox();

        return root;
    }

    private void loadInbox() {
        inboxContainer.getChildren().clear();

        // Stylized Loading State
        HBox loadingBox = new HBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(50));
        Text loadingText = new Text("📡 Scanning network for applications...");
        loadingText.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
        loadingText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        loadingBox.getChildren().add(loadingText);
        inboxContainer.getChildren().add(loadingBox);

        new Thread(() -> {
            List<ApplicationModel> apps = ApplicationController.fetchMyInbox();
            Platform.runLater(() -> {
                inboxContainer.getChildren().clear();
                if (apps == null || apps.isEmpty()) {
                    // Stylized Empty State
                    VBox emptyBox = new VBox(15);
                    emptyBox.setAlignment(Pos.CENTER);
                    emptyBox.setPadding(new Insets(100, 0, 0, 0));

                    Text icon = new Text("📭");
                    icon.setFont(Font.font(48));
                    Text empty = new Text("INBOX EMPTY");
                    empty.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                    empty.setFont(Font.font("Poppins", FontWeight.BOLD, 24));
                    Text subEmpty = new Text("Your active recruitment posts currently have no pending applications.");
                    subEmpty.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));

                    emptyBox.getChildren().addAll(icon, empty, subEmpty);
                    inboxContainer.getChildren().add(emptyBox);
                } else {
                    int delay = 0;
                    for (ApplicationModel app : apps) {
                        HBox card = createApplicationCard(app);
                        inboxContainer.getChildren().add(card);
                        GamerVaultAnimations.fadeInUp(card, delay * 80, 400);
                        delay++;
                    }
                }
            });
        }).start();
    }

    private HBox createApplicationCard(ApplicationModel app) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.03); " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: rgba(255,255,255,0.08); " +
                        "-fx-border-radius: 12;");
        GamerVaultAnimations.scaleOnHover(card, 1.02);

        HBox identityBox = new HBox(15);
        identityBox.setAlignment(Pos.CENTER_LEFT);
        identityBox.setPrefWidth(250);

        StackPane avatar = new StackPane();
        avatar.setPrefSize(50, 50);
        avatar.setStyle(
                "-fx-background-color: rgba(139,92,246,0.15); -fx-background-radius: 10; -fx-border-color: rgba(139,92,246,0.4); -fx-border-radius: 10;");

        String initial = (app.getApplicantName() != null && !app.getApplicantName().isEmpty())
                ? app.getApplicantName().substring(0, 1).toUpperCase()
                : "?";
        Text avatarInitials = new Text(initial);
        avatarInitials.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
        avatarInitials.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        avatar.getChildren().add(avatarInitials);

        VBox nameRoleBox = new VBox(5);
        nameRoleBox.setAlignment(Pos.CENTER_LEFT);

        Text nameLabel = new Text(app.getApplicantName());
        nameLabel.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        String roleColor = getRoleColorHex(app.getSnapshotRoleArchetype());
        StackPane roleBadge = new StackPane();
        roleBadge.setPadding(new Insets(3, 8, 3, 8));
        roleBadge.setStyle("-fx-background-color: " + roleColor + "20; -fx-background-radius: 4; -fx-border-color: "
                + roleColor + "60; -fx-border-radius: 4;");
        Text roleLabel = new Text(app.getSnapshotRoleArchetype() != null ? app.getSnapshotRoleArchetype() : "UNKNOWN");
        roleLabel.setFill(Color.web(roleColor));
        roleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        roleBadge.getChildren().add(roleLabel);

        nameRoleBox.getChildren().addAll(nameLabel, roleBadge);
        identityBox.getChildren().addAll(avatar, nameRoleBox);

        identityBox.setStyle("-fx-cursor: hand; -fx-padding: 5; -fx-background-radius: 8;");
        identityBox.setOnMouseEntered(e -> identityBox.setStyle(
                "-fx-cursor: hand; -fx-padding: 5; -fx-background-radius: 8; -fx-background-color: rgba(14, 165, 233, 0.1);"));
        identityBox.setOnMouseExited(e -> identityBox.setStyle(
                "-fx-cursor: hand; -fx-padding: 5; -fx-background-radius: 8; -fx-background-color: transparent;"));

        identityBox.setOnMouseClicked(e -> {
            ProfileController.setTargetProfile(app.getApplicantId());
            PlayerDashboardSidebar.pageView = "profile";
            PlayerMainScreen.instance.updateCenter();
        });

        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER);

        String skillTier = app.getSnapshotSkillTier() != null ? app.getSnapshotSkillTier() : "UNRANKED";

        String tierColor = skillTier.equals("UNRANKED") ? "#6B7280" : "#F59E0B";

        statsBox.getChildren().addAll(
                createStatBlock("AVG KILLS", String.format("%.1f", app.getSnapshotFdRatio()),
                        GamerVaultStyles.ACCENT_CYAN),
                createStatBlock("AVG DMG", String.format("%.0f", app.getSnapshotAvgDamage()), "#F43F5E"),
                createStatBlock("TIER", skillTier, tierColor));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionBox = new HBox(12);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnDecline = new Button("Decline");
        btnDecline.setPrefHeight(38);
        btnDecline.setPrefWidth(100);
        btnDecline.setStyle(
                "-fx-background-color: rgba(239, 68, 68, 0.1); -fx-text-fill: #ef4444; -fx-border-color: rgba(239, 68, 68, 0.4); -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
        btnDecline.setOnMouseEntered(e -> btnDecline.setStyle(
                "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;"));
        btnDecline.setOnMouseExited(e -> btnDecline.setStyle(
                "-fx-background-color: rgba(239, 68, 68, 0.1); -fx-text-fill: #ef4444; -fx-border-color: rgba(239, 68, 68, 0.4); -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;"));
        btnDecline.setOnAction(e -> handleAction(app, false, card));

        Button btnAccept = new Button("Accept Connection");
        btnAccept.setPrefHeight(38);
        btnAccept.setPrefWidth(150);
        btnAccept.setStyle(
                "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        btnAccept.setOnMouseEntered(e -> btnAccept.setStyle(
                "-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnAccept.setOnMouseExited(e -> btnAccept.setStyle(
                "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnAccept.setOnAction(e -> handleAction(app, true, card));

        actionBox.getChildren().addAll(btnDecline, btnAccept);
        card.getChildren().addAll(identityBox, statsBox, spacer, actionBox);
        return card;
    }

    private void handleAction(ApplicationModel app, boolean accept, HBox cardUI) {
        if (accept) {
            // Find the accept button in the card to show a loading state
            HBox actionBox = (HBox) cardUI.getChildren().get(cardUI.getChildren().size() - 1);
            Button acceptBtn = (Button) actionBox.getChildren().get(1);

            Platform.runLater(() -> {
                acceptBtn.setText("Connecting...");
                acceptBtn.setStyle(
                        "-fx-background-color: #0EA5E9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
            });

            new Thread(() -> {
                // 1. Update application status in the database
                boolean success = ApplicationController.accept(app);

                if (success) {
                    // 2. Initialize the direct message room in Firebase
                    String roomId = DirectMessageController.initializeConnection(app.getApplicantId());
                    DirectMessageController.activeChatRoomId = roomId;

                    Platform.runLater(() -> {
                        // 3. Remove the card from the UI smoothly
                        inboxContainer.getChildren().remove(cardUI);

                        PlayerDashboardSidebar.pageView = "directMessage";
                        PlayerMainScreen.instance.updateCenter();
                    });
                } else {
                    Platform.runLater(this::loadInbox);
                }
            }).start();

        } else {
            // Decline Flow
            Platform.runLater(() -> {
                new Thread(() -> {
                    try {
                        Thread.sleep(250);
                    } catch (Exception ex) {
                    }
                    Platform.runLater(() -> inboxContainer.getChildren().remove(cardUI));
                }).start();
            });
            new Thread(() -> ApplicationController.decline(app)).start();
        }
    }

    // Helper: HUD Style Data Block
    private VBox createStatBlock(String label, String value, String valueColor) {
        VBox block = new VBox(2);
        block.setAlignment(Pos.CENTER);
        block.setPrefWidth(90);
        block.setPadding(new Insets(8, 12, 8, 12));
        block.setStyle(
                "-fx-background-color: rgba(0,0,0,0.2); -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.05); -fx-border-radius: 8;");

        Text lblText = new Text(label);
        lblText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        lblText.setFont(Font.font("Arial", FontWeight.BOLD, 10));

        Text valText = new Text(value);
        valText.setFill(Color.web(valueColor));
        valText.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        block.getChildren().addAll(lblText, valText);
        return block;
    }

    // Helper: Dynamic colors based on standard gaming archetypes
    private String getRoleColorHex(String role) {
        if (role == null)
            return "#9CA3AF"; // Gray
        String r = role.toUpperCase();
        if (r.contains("FRAGGER") || r.contains("ASSAULTER"))
            return "#EF4444"; // Red
        if (r.contains("SUPPORT"))
            return "#10B981"; // Green
        if (r.contains("IGL"))
            return "#8B5CF6"; // Purple
        if (r.contains("SNIPER"))
            return "#3B82F6"; // Blue
        if (r.contains("SURVIVALIST"))
            return "#F59E0B"; // Yellow
        return "#38BDF8"; // Light Blue default
    }

    // private void handleAction(String appId, boolean accept, HBox cardUI) {
    // // Optimistic UI update: Remove the card immediately to make the app feel
    // // ultra-responsive
    // // GamerVaultAnimations.fadeOutDown(cardUI, 0, 300);
    // Platform.runLater(() -> {
    // // Wait slightly for animation to finish before removing from layout
    // new Thread(() -> {
    // try {
    // Thread.sleep(300);
    // } catch (Exception ex) {
    // }
    // Platform.runLater(() -> inboxContainer.getChildren().remove(cardUI));
    // }).start();
    // });

    // // Fire the actual database update in the background
    // new Thread(() -> {
    // boolean success = accept ? ApplicationController.accept(appId) :
    // ApplicationController.decline(appId);
    // if (!success) {
    // // If it fails, reload the inbox to fix the optimistic UI removal
    // Platform.runLater(this::loadInbox);
    // }
    // }).start();
    // }
}