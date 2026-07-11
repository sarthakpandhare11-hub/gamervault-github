package com.example.view.admin;

import com.example.view.auth.LoginScreen;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class AdminDashboardSidebar {

    public static String pageView = "dashboard";

    private static Scene loginScreenScene;

    /*
     * This is the method for the sidebasr which is used by Player.
     * It is the left side menu for player to choose the screen they want.
     *
     * It has two main sections
     * 1) Quick navigation menus
     * 2) Logout button
     * 
     * It has navigation buttons, player willhave single sidebar throughout their
     * screen.
     * 
     * There is a variable pageView which keeps track of the current page.
     * If pageView is dashboard, then it will display the dashboard screen.
     * If pageView is upload, then it will display the upload screen.
     * and so onnn.
     * 
     * PlayerMainScreen is the parameter for the method because we need to update
     * the center of the screen when a button is clicked.
     * We change the pageView, after pageView changes then PlayerMainScreen will
     * call its updateCenter method which then have switch-case to check which
     * screen to show.
     * 
     * 
     */
    public static VBox createSidebar(AdminMainScreen mainScreen) {

        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(260);
        // Glassmorphism sidebar background
        sidebar.setStyle("-fx-background-color: " + GamerVaultStyles.SIDEBAR_BG
                + "; -fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 0 1 0 0;");

        Label logo = new Label("🎮 GamerVault");
        logo.setStyle(
                "-fx-font-size:28px;-fx-font-weight:bold;-fx-text-fill:" + GamerVaultStyles.ACCENT_PURPLE_LIGHT + ";");
        // Add shimmer glow to logo
        DropShadow logoGlow = new DropShadow(15, Color.web(GamerVaultStyles.ACCENT_PURPLE, 0.4));
        logo.setEffect(logoGlow);

        sidebar.getChildren().addAll(logo, new Region() {
            {
                setPrefHeight(15);
            }
        });

        Button dashboardButton = new Button("🏠  Dashboard");
        applyStyling(dashboardButton, pageView.equals("dashboard"));
        dashboardButton.setOnAction(event -> {
            pageView = "dashboard";
            mainScreen.updateCenter();
            System.out.println("DEBUG: SIDEBAR DASHBOARD CLICKED");
        });

        Button contentHubButton = new Button("📤  Content Hub Management");
        applyStyling(contentHubButton, pageView.equals("contentHub"));
        contentHubButton.setOnAction(event -> {
            pageView = "contentHub";
            mainScreen.updateCenter();
            System.out.println("DEBUG: SIDEBAR CONTENT HUB CLICKED");
        });

        Button tournamentManagementButton = new Button("📜  Tournament Management");
        applyStyling(tournamentManagementButton, pageView.equals("tournamentManagement"));
        tournamentManagementButton.setOnAction(event -> {
            pageView = "tournamentManagement";
            mainScreen.updateCenter();
            System.out.println("DEBUG: SIDEBAR TOURNAMENT MANAGEMENT CLICKED");
        });

        Button disputeResolutionBtn = new Button("⚖️ Dispute Resolution");
        applyStyling(disputeResolutionBtn, pageView.equals("disputeResolution"));
        disputeResolutionBtn.setOnAction(event -> {
            pageView = "disputeResolution";
            mainScreen.updateCenter();
        });

        Button userManagementButton = new Button("🤝  User Management");
        applyStyling(userManagementButton, pageView.equals("userManagement"));
        userManagementButton.setOnAction(event -> {
            pageView = "userManagement";
            mainScreen.updateCenter();
            System.out.println("DEBUG: SIDEBAR USER MANAGEMENT CLICKED");
        });

        Button templatesBtn = new Button("✨ Template Management");
        applyStyling(templatesBtn, pageView.equals("templates"));
        templatesBtn.setOnAction(e -> {
            pageView = "templates";
            mainScreen.updateCenter();
            System.out.println("DEBUG: SIDEBAR TEMPLATES CLICKED");
        });

        Button adminProfileButton = new Button("👤  Admin Profile");
        applyStyling(adminProfileButton, pageView.equals("adminProfile"));
        adminProfileButton.setOnAction(event -> {
            pageView = "adminProfile";
            mainScreen.updateCenter();
            System.out.println("DEBUG: SIDEBAR ADMIN PROFILE CLICKED");
        });

        sidebar.getChildren().addAll(
                dashboardButton,
                contentHubButton,
                tournamentManagementButton,
                disputeResolutionBtn,
                userManagementButton,
                templatesBtn,
                adminProfileButton);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logout = new Button("🚪 Logout");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setAlignment(Pos.CENTER_LEFT);
        // Red accent ghost style for logout
        String logoutBaseStyle = "-fx-background-color: transparent; -fx-text-fill: #FCA5A5; "
                + "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; "
                + "-fx-border-color: rgba(239,68,68,0.2); -fx-border-radius: 10; -fx-border-width: 1; -fx-cursor: hand;";
        String logoutHoverStyle = "-fx-background-color: rgba(239,68,68,0.1); -fx-text-fill: #FCA5A5; "
                + "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; "
                + "-fx-border-color: rgba(239,68,68,0.4); -fx-border-radius: 10; -fx-border-width: 1; -fx-cursor: hand;";
        logout.setStyle(logoutBaseStyle);
        logout.setOnMouseEntered(e -> logout.setStyle(logoutHoverStyle));
        logout.setOnMouseExited(e -> logout.setStyle(logoutBaseStyle));
        logout.setPrefHeight(42);
        GamerVaultAnimations.scaleOnPress(logout);

        logout.setOnAction(EVENT -> {
            navigateToLoginScreen(mainScreen);
        });

        sidebar.getChildren().addAll(spacer, logout);

        return sidebar;
    }

    /*
     * Apply styling to buttons, while considering which button is selected to
     * highlight it with other color to understand which is selected.
     * Now includes: left accent bar for selected, hover scale, hover bg transition.
     */
    private static void applyStyling(Button btn, boolean isSelected) {
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPrefHeight(40);

        if (isSelected) {
            // Selected: left accent bar + tinted bg
            btn.setStyle("-fx-background-color: rgba(139,92,246,0.15); "
                    + "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; "
                    + "-fx-background-radius: 10; "
                    + "-fx-border-color: transparent transparent transparent " + GamerVaultStyles.ACCENT_PURPLE + "; "
                    + "-fx-border-width: 0 0 0 4; -fx-border-radius: 10; -fx-cursor: hand;");
            // Subtle glow on selected
            DropShadow selectedGlow = new DropShadow(10, Color.web(GamerVaultStyles.ACCENT_PURPLE, 0.3));
            btn.setEffect(selectedGlow);
        } else {
            String baseStyle = "-fx-background-color: transparent; "
                    + "-fx-text-fill: " + GamerVaultStyles.TEXT_SECONDARY + "; -fx-font-size: 14px; "
                    + "-fx-background-radius: 10; -fx-cursor: hand;";
            String hoverStyle = "-fx-background-color: rgba(139,92,246,0.08); "
                    + "-fx-text-fill: white; -fx-font-size: 14px; "
                    + "-fx-background-radius: 10; -fx-cursor: hand;";

            btn.setStyle(baseStyle);
            btn.setEffect(null);

            btn.setOnMouseEntered(e -> {
                btn.setStyle(hoverStyle);
                ScaleTransition st = new ScaleTransition(
                        Duration.millis(150), btn);
                st.setToX(1.02);
                st.setToY(1.02);
                st.play();
            });
            btn.setOnMouseExited(e -> {
                btn.setStyle(baseStyle);
                ScaleTransition st = new ScaleTransition(
                        Duration.millis(150), btn);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });
        }
    }

    static void navigateToLoginScreen(AdminMainScreen mainScreen) {
        LoginScreen loginScreen = new LoginScreen();
        loginScreen.setLoginScreenStage(mainScreen.adminMainScreenStage);
        loginScreenScene = new Scene(loginScreen.startLoginScreen(),
                mainScreen.adminMainScreenStage.getWidth(),
                mainScreen.adminMainScreenStage.getHeight());
        loginScreen.setLoginScreenScene(loginScreenScene);
        ;
        mainScreen.adminMainScreenStage.setScene(loginScreenScene);
    }
}
