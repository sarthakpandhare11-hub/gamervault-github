package com.example.view.player;

import java.util.List;

import com.example.controller.AuthController;
import com.example.controller.admin.NotificationController;
import com.example.model.NotificationModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class PlayerMainScreen {

    // VARIABLES
    private BorderPane root;

    Scene playerMainScreenScene;
    Stage playerMainScreenStage;

    // METHODS

    // Setters for the scene and stage for the player's main screen
    public void setPlayerMainScreenScene(Scene playerMainScreenScene) {
        this.playerMainScreenScene = playerMainScreenScene;
    }

    public void setPlayerMainScreenStage(Stage playerMainScreenStage) {
        this.playerMainScreenStage = playerMainScreenStage;
    }

    // Start method for the player's main screen
    public BorderPane startPlayerMainScreen(Runnable backNavigation) {

        root = new BorderPane();
        root.setStyle("-fx-background-color: " + GamerVaultStyles.BASE_BG + ";");

        root.setLeft(PlayerDashboardSidebar.createSidebar(this));

        updateCenter();

        return root;
    }

    /*
     * This method is used to update the center of the player screen after a button
     * is clicked on the sidebar.
     */
    public void updateCenter() {

        root.setLeft(PlayerDashboardSidebar.createSidebar(this));

        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: " + GamerVaultStyles.BASE_BG + ";");

        container.getChildren().add(createTopBar());
        container.getChildren().add(getScreen());

        // Fade-in transition when center content swaps
        GamerVaultAnimations.fadeInUp(container, 0, 350);

        // This updates the middle of the screen!
        root.setCenter(container);
    }

    private BorderPane getScreen() {

        switch (PlayerDashboardSidebar.pageView) {
            case "dashboard":
                return new PlayerDashboardScreen().startPlayerDashboardScreen(this);
            case "upload":
                return new UploadMatchScreen().startUploadMatchScreen();
            case "matchHistory":
                return new MatchHistoryScreen().startMatchHistoryScreen();
            case "analytics":
                return new AnalyticsScreen().startAnalyticsScreen();
            case "portfolioGenerator":
                return new PortfolioGeneratorScreen().startPortfolioGeneratorScreen();
            case "leaderboard":
                return new LeaderboardScreen().startLeaderboardScreen();
            case "recruitmentHub":
                return new RecruitmentHubScreen().startRecruitmentHubScreen();
            case "profile":
                return new ProfileScreen().startProfileScreen();
            case "discover_content":
                return new PlayerDiscoveryHubScreen().startDiscoveryScreen("CONTENT");
            default:
                return new BorderPane();
        }
    }

    /*
     * Styled Top Bar with glassmorphism search field and ghost icon buttons.
     */
    private HBox createTopBar() {

        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(5, 0, 5, 0));

        // Styled search field with focus glow
        TextField search = new TextField();
        search.setPromptText("🔍  Search...");
        HBox searchContainer = GamerVaultStyles.createStyledInput(search, GamerVaultStyles.ACCENT_PURPLE);
        searchContainer.setPrefWidth(300);
        searchContainer.setPrefHeight(40);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String currentUserId = (AuthController.currentUser != null) ? AuthController.currentUser.getUserId()
                : "TEST_USER_123";
        StackPane bellContainer = createNotificationBell(currentUserId);

        Button settings = new Button("⚙");
        Button profile = new Button("👤");

        settings.setOnAction(e -> {
            PlayerDashboardSidebar.pageView = "profile";
            updateCenter();
        });

        profile.setOnAction(e -> {
            PlayerDashboardSidebar.pageView = "profile";
            updateCenter();
        });

        for (Button btn : new Button[] { settings, profile }) {
            GamerVaultStyles.applyGhostButton(btn);
            btn.setPrefSize(40, 40);
            btn.setStyle(btn.getStyle() + " -fx-font-size: 16px;");
        }

        topBar.getChildren().addAll(searchContainer, spacer, bellContainer, settings, profile);

        return topBar;
    }

    private StackPane createNotificationBell(String userId) {
        StackPane bellWrapper = new StackPane();
        bellWrapper.setPrefSize(40, 40); // Matches the size of settings/profile buttons
        bellWrapper.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-cursor: hand;");

        // Hover effects matching Ghost Buttons
        bellWrapper.setOnMouseEntered(e -> bellWrapper
                .setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-cursor: hand;"));
        bellWrapper.setOnMouseExited(e -> bellWrapper
                .setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-cursor: hand;"));

        Text icon = new Text("🔔");
        icon.setFont(Font.font(16));
        icon.setFill(Color.WHITE);

        // Unread Red Badge Dot
        Circle badge = new Circle(4, Color.web("#EF4444"));
        badge.setStroke(Color.web(GamerVaultStyles.BASE_BG));
        badge.setStrokeWidth(1.5);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(6, 6, 0, 0));

        bellWrapper.getChildren().addAll(icon, badge);

        // Create Custom Popup
        Popup notificationPopup = new Popup();
        notificationPopup.setAutoHide(true);

        VBox popupContent = new VBox(10);
        popupContent.setPadding(new Insets(15));
        popupContent.setPrefSize(350, 400);

        // Glassmorphism styling for the popup box
        popupContent.setStyle(
                "-fx-background-color: #0F172A; -fx-border-color: rgba(139,92,246,0.5); -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        Text title = new Text("Recent Notifications");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        VBox notificationsList = new VBox(10);
        ScrollPane scroller = new ScrollPane(notificationsList);
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setFitToWidth(true);
        VBox.setVgrow(scroller, Priority.ALWAYS);

        popupContent.getChildren().addAll(title, scroller);
        notificationPopup.getContent().add(popupContent);

        // Click Event to open popup and fetch real-time data
        bellWrapper.setOnMouseClicked(e -> {
            badge.setVisible(false); // Hide the red dot once opened

            notificationsList.getChildren().clear();
            Text loading = new Text("Fetching updates...");
            loading.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
            notificationsList.getChildren().add(loading);

            if (!notificationPopup.isShowing()) {
                // Show floating directly below the bell icon
                notificationPopup.show(bellWrapper.getScene().getWindow(),
                        e.getScreenX() - 300, // Shift left so it doesn't go off screen
                        e.getScreenY() + 20);

                // Fetch live from Firestore
                new Thread(() -> {
                    List<NotificationModel> notifs = NotificationController.getPlayerNotifications(userId);
                    Platform.runLater(() -> {
                        notificationsList.getChildren().clear();
                        if (notifs.isEmpty()) {
                            Text empty = new Text("No new notifications.");
                            empty.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                            notificationsList.getChildren().add(empty);
                        } else {
                            for (NotificationModel n : notifs) {
                                notificationsList.getChildren().add(createNotificationItem(n));
                            }
                        }
                    });
                }).start();
            }
        });

        return bellWrapper;
    }

    private HBox createNotificationItem(NotificationModel n) {
        HBox item = new HBox(12);
        item.setPadding(new Insets(10));
        item.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 8;");
        GamerVaultAnimations.scaleOnHover(item, 1.02);

        String emoji = "📣"; // Default System
        if (n.getType() != null) {
            if (n.getType().equals("MATCH"))
                emoji = "🎮";
            else if (n.getType().equals("TOURNAMENT"))
                emoji = "🏆";
            else if (n.getType().equals("CONTENT"))
                emoji = "🎥";
        }

        Text icon = new Text(emoji);
        icon.setFont(Font.font(20));

        VBox textCol = new VBox(4);
        Text title = new Text(n.getTitle() != null ? n.getTitle() : "Alert");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        Text msg = new Text(n.getMessage() != null ? n.getMessage() : "");
        msg.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        msg.setFont(Font.font("Arial", 11));
        msg.setWrappingWidth(260);

        textCol.getChildren().addAll(title, msg);
        item.getChildren().addAll(icon, textCol);
        return item;
    }
}
