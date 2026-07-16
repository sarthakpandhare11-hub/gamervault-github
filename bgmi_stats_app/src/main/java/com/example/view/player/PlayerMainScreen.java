package com.example.view.player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.controller.AuthController;
import com.example.controller.admin.NotificationController;
import com.example.controller.player.ProfileController;
import com.example.model.NotificationModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.google.cloud.firestore.ListenerRegistration;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
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
import javafx.util.Duration;

public class PlayerMainScreen {

    public static PlayerMainScreen instance;

    private java.util.Stack<String> navigationHistory = new java.util.Stack<>();
    private String currentPage = "dashboard";

    // LAYOUT VARIABLES
    private StackPane rootStack;
    private BorderPane mainLayout;
    private VBox toastContainer; // Floats on top for popups

    Scene playerMainScreenScene;
    Stage playerMainScreenStage;
    public String activeBattleId;

    // NOTIFICATION STATE VARIABLES
    private ListenerRegistration notificationListener;
    private List<NotificationModel> liveNotifications = new ArrayList<>();
    private Set<String> seenNotificationIds = new HashSet<>();
    private long sessionStartTime = System.currentTimeMillis();

    private Circle badge;
    private VBox popupNotificationsList;
    private Popup notificationPopup;

    public void setPlayerMainScreenScene(Scene playerMainScreenScene) {
        this.playerMainScreenScene = playerMainScreenScene;
    }

    public void setPlayerMainScreenStage(Stage playerMainScreenStage) {
        this.playerMainScreenStage = playerMainScreenStage;
    }

    // Start method for the player's main screen
    public StackPane startPlayerMainScreen(Runnable backNavigation) {
        instance = this;

        // 1. The True Root (Allows floating layers on top)
        rootStack = new StackPane();

        // 2. The Main Application Layout
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: " + GamerVaultStyles.BASE_BG + ";");
        mainLayout.setLeft(PlayerDashboardSidebar.createSidebar(this));

        // 3. The Floating Toast Container (Bottom Right)
        toastContainer = new VBox(12);
        toastContainer.setAlignment(Pos.BOTTOM_RIGHT);
        toastContainer.setPickOnBounds(false); // Lets clicks pass through empty space to the app below
        StackPane.setAlignment(toastContainer, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(toastContainer, new Insets(0, 30, 30, 0)); // 30px offset from corner

        rootStack.getChildren().addAll(mainLayout, toastContainer);

        updateCenter();
        initializeLiveNotifications(); // Boot up the listener

        return rootStack;
    }

    public void updateCenter() {
        mainLayout.setLeft(PlayerDashboardSidebar.createSidebar(this));

        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: " + GamerVaultStyles.BASE_BG + ";");

        container.getChildren().add(createTopBar());
        container.getChildren().add(getScreen());

        GamerVaultAnimations.fadeInUp(container, 0, 350);
        mainLayout.setCenter(container);
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
            case "battleArena":
                return new BattleArenaScreen().startBattleArenaScreen(this);
            case "activeBattleRoom":
                if (activeBattleId == null || activeBattleId.trim().isEmpty()) {
                    PlayerDashboardSidebar.pageView = "battleArena";
                    return new BattleArenaScreen().startBattleArenaScreen(this);
                }
                return new ActiveBattleRoomScreen().startBattleRoomScreen(this, activeBattleId);
            case "versusHistory":
                return new VersusHistoryScreen().startVersusHistoryScreen();
            case "recruitmentHub":
                return new RecruitmentHubScreen().startRecruitmentHubScreen();
            case "scoutsInbox":
                return new RecruitmentInboxScreen().startRecruitmentInboxScreen();
            case "profile":
                return new ProfileScreen().startProfileScreen();
            case "discover_content":
                return new PlayerDiscoveryHubScreen().startDiscoveryScreen("CONTENT");
            case "directMessage":
                return new DirectMessageScreen().startChatScreen();
            case "socialFeed":
                return new SocialFeedScreen().startFeedScreen();
            case "connectionRequests":
                return new ConnectionRequestsScreen().startConnectionRequestsScreen();
            default:
                return new BorderPane();
        }
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(5, 0, 5, 0));

        TextField search = new TextField();
        search.setPromptText("🔍  Search...");
        HBox searchContainer = GamerVaultStyles.createStyledInput(search, GamerVaultStyles.ACCENT_PURPLE);
        searchContainer.setPrefWidth(300);
        searchContainer.setPrefHeight(40);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane bellContainer = createNotificationBell();

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
            // FIX: Set font natively so the CSS hover state doesn't erase it!
            btn.setFont(Font.font(18));
            btn.setPrefSize(50, 50);
            GamerVaultStyles.applyGhostButton(btn);
        }

        topBar.getChildren().addAll(searchContainer, spacer, bellContainer, settings, profile);
        return topBar;
    }

    // ─── LIVE NOTIFICATION ENGINE ──────────────────────────────────────────

    private void initializeLiveNotifications() {
        String currentUserId = (AuthController.currentUser != null) ? AuthController.currentUser.getUserId()
                : "TEST_USER_123";

        notificationListener = NotificationController.listenToUserNotifications(currentUserId, (notifs) -> {
            Platform.runLater(() -> {
                liveNotifications = notifs;
                int unreadCount = 0;

                for (NotificationModel n : notifs) {
                    if (!n.isRead())
                        unreadCount++;

                    // If it's a NEW notification that arrived AFTER login, show a Toast
                    if (!seenNotificationIds.contains(n.getNotificationId())) {
                        seenNotificationIds.add(n.getNotificationId());
                        if (n.getTimestamp() > sessionStartTime) {
                            showToast(n);
                        }
                    }
                }

                // Update the Red Dot
                if (badge != null)
                    badge.setVisible(unreadCount > 0);

                // Live update the popup if the user is currently looking at it
                if (notificationPopup != null && notificationPopup.isShowing()) {
                    renderPopupList();
                }
            });
        });
    }

    private StackPane createNotificationBell() {
        StackPane bellWrapper = new StackPane();
        bellWrapper.setPrefSize(40, 40);
        bellWrapper.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-cursor: hand;");
        bellWrapper.setOnMouseEntered(e -> bellWrapper
                .setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-cursor: hand;"));
        bellWrapper.setOnMouseExited(e -> bellWrapper
                .setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-cursor: hand;"));

        Text icon = new Text("🔔");
        icon.setFont(Font.font(16));
        icon.setFill(Color.WHITE);

        badge = new Circle(4, Color.web("#EF4444"));
        badge.setStroke(Color.web(GamerVaultStyles.BASE_BG));
        badge.setStrokeWidth(1.5);
        badge.setVisible(false); // Hidden initially until listener finds unread data
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(6, 6, 0, 0));

        bellWrapper.getChildren().addAll(icon, badge);

        // Build Popup structure
        notificationPopup = new Popup();
        notificationPopup.setAutoHide(true);

        VBox popupContent = new VBox(10);
        popupContent.setPadding(new Insets(15));
        popupContent.setPrefSize(350, 400);
        popupContent.setStyle(
                "-fx-background-color: #0F172A; -fx-border-color: rgba(139,92,246,0.5); -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        Text title = new Text("Recent Notifications");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        popupNotificationsList = new VBox(10);
        ScrollPane scroller = new ScrollPane(popupNotificationsList);
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setFitToWidth(true);

        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox.setVgrow(scroller, Priority.ALWAYS);

        popupContent.getChildren().addAll(title, scroller);
        notificationPopup.getContent().add(popupContent);

        // Open Action
        bellWrapper.setOnMouseClicked(e -> {
            if (!notificationPopup.isShowing()) {
                badge.setVisible(false);
                notificationPopup.show(bellWrapper.getScene().getWindow(), e.getScreenX() - 300, e.getScreenY() + 20);
                renderPopupList();
            } else {
                notificationPopup.hide();
            }
        });

        return bellWrapper;
    }

    private void renderPopupList() {
        popupNotificationsList.getChildren().clear();
        List<String> unreadIdsToMark = new ArrayList<>();

        if (liveNotifications.isEmpty()) {
            Text empty = new Text("No new notifications.");
            empty.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
            popupNotificationsList.getChildren().add(empty);
        } else {
            for (NotificationModel n : liveNotifications) {
                popupNotificationsList.getChildren().add(createNotificationItem(n));
                if (!n.isRead()) {
                    unreadIdsToMark.add(n.getNotificationId());
                    n.setRead(true); // Instant local visual update
                }
            }
        }

        // Fire and forget batch update to Firestore
        if (!unreadIdsToMark.isEmpty()) {
            new Thread(() -> NotificationController.markAllAsRead(unreadIdsToMark)).start();
        }
    }

    private void showToast(NotificationModel n) {
        HBox toast = createNotificationItem(n);

        // Upgrade the styling for floating toasts to look highly premium
        toast.setStyle("-fx-background-color: rgba(25, 33, 50, 0.95); " +
                "-fx-border-color: " + GamerVaultStyles.ACCENT_PURPLE + "; " +
                "-fx-border-width: 1.5; " +
                "-fx-background-radius: 10; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 20, 0, 0, 10);");

        toastContainer.getChildren().add(toast);
        GamerVaultAnimations.slideInNotification(toast);

        // Auto Dismiss after 4.5 seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(4.5));
        delay.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(400), toast);
            fade.setToValue(0);
            fade.setOnFinished(ev -> toastContainer.getChildren().remove(toast));
            fade.play();
        });
        delay.play();
    }

    private HBox createNotificationItem(NotificationModel n) {
        HBox item = new HBox(12);
        item.setPadding(new Insets(10));
        item.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 8;");
        GamerVaultAnimations.scaleOnHover(item, 1.02);

        String emoji = "📣";
        if (n.getType() != null) {
            switch (n.getType()) {
                case "MATCH":
                    emoji = "🎮";
                    break;
                case "TOURNAMENT":
                    emoji = "🏆";
                    break;
                case "CONTENT":
                    emoji = "🎥";
                    break;
                case "BATTLE":
                    emoji = "⚔";
                    break;
                case "APPLICATION":
                    emoji = "📝";
                    break;
            }
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

    // Add these routing methods
    public void switchPage(String targetPage) {
        if (!targetPage.equals(currentPage)) {
            if (!targetPage.equals("profile")) {
                ProfileController.clearTargetProfile();
            }

            navigationHistory.push(currentPage);
            currentPage = targetPage;
            PlayerDashboardSidebar.pageView = targetPage;
            updateCenter();
        }
    }

    public void navigateBack() {
        if (!navigationHistory.isEmpty()) {
            String previousPage = navigationHistory.pop();

            if (!previousPage.equals("profile")) {
                ProfileController.clearTargetProfile();
            }

            currentPage = previousPage;
            PlayerDashboardSidebar.pageView = previousPage;
            updateCenter();
        } else {
            switchPage("dashboard");
        }
    }
}