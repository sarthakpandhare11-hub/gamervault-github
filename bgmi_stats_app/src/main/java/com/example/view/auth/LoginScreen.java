package com.example.view.auth;

import com.example.controller.AuthController;
import com.example.view.admin.AdminMainScreen;
import com.example.view.player.PlayerMainScreen;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LoginScreen {

    // VARIABLES

    // Variables for the login screen
    Scene loginScreenScene;
    Stage loginScreenStage;

    // Variables for navigating screens

    // Registeration Screen scene
    Scene registerationScreenScene;

    // Player Main Screen Scene
    Scene playerMainScreenScene;

    // Admin Main Screen Scene
    Scene adminMainScreenScene;

    // METHODS

    // Setters for the scene and stage of the login screen
    public void setLoginScreenScene(Scene loginScreenScene) {
        this.loginScreenScene = loginScreenScene;
    }

    public void setLoginScreenStage(Stage loginScreenStage) {
        this.loginScreenStage = loginScreenStage;
    }

    // Start method for the UI of the login screen
    public StackPane startLoginScreen() {

        // Root atmospheric StackPane
        StackPane root = new StackPane();
        try {
            String bgImgUrl = getClass().getResource("/assets/bgmi_images/bgmibgimg.png").toExternalForm();
            root.setStyle(
                    "-fx-background-image: url('" + bgImgUrl + "'); " +
                            "-fx-background-size: cover; " +
                            "-fx-background-position: center center; " +
                            "-fx-background-repeat: no-repeat;");
        } catch (Exception e) {
            System.err.println("Could not load background image: " + e.getMessage());
            // Fallback to black if the image path is wrong
            root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        }

        HBox mainContainer = new HBox();
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setSpacing(60);

        StackPane leftSideSection = getLeftSideContent();
        VBox rightSideSection = getRightSideContent();

        mainContainer.getChildren().addAll(leftSideSection, rightSideSection);

        root.getChildren().add(mainContainer);

        // Stagger animate the main container
        GamerVaultAnimations.fadeInUp(leftSideSection, 200, 600);
        GamerVaultAnimations.fadeInUp(rightSideSection, 400, 700);

        return root;
    }

    /*
     * LEFT SIDE CONTENT SECTION
     * It contains images, and bottom text for better UI
     * CONTAINS -
     * 1) IMAGE
     * 2) BOTTOM TEXTS
     */
    private StackPane getLeftSideContent() {
        StackPane leftPane = new StackPane();
        leftPane.setPrefHeight(700);
        leftPane.setPrefWidth(800);

        // 1) IMAGE
        Image playerBgImage = new Image(getClass().getResource("/assets/logoImage/playerpng.png").toExternalForm());
        ImageView playerBgImageView = new ImageView(playerBgImage);

        playerBgImageView.setFitWidth(800);
        playerBgImageView.setFitHeight(800);
        playerBgImageView.setPreserveRatio(true);

        // SUBLABEL ON IMAGE
        HBox subLabel = new HBox();
        Separator miniSeparator = new Separator();
        miniSeparator.setPrefWidth(100);

        Text subtitle = new Text("SYSTEM ONLINE");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        subtitle.setFill(Color.web("#cbbaf1"));

        subLabel.getChildren()
                .addAll(
                        SizedBox.width(12),
                        miniSeparator,
                        SizedBox.width(5),
                        subtitle);

        // 2ND LABEL ON IMAGE
        Text title = new Text("ELITE TIER GAMERVAULT");
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 44));
        title.setFill(Color.web("#e8e3f3"));
        // Add a subtle glow to the title
        DropShadow titleGlow = new DropShadow(25, Color.web(GamerVaultStyles.ACCENT_PURPLE, 0.4));
        title.setEffect(titleGlow);

        VBox textBox = new VBox();
        textBox.getChildren().addAll(subLabel, SizedBox.height(15), title);
        textBox.setAlignment(Pos.BOTTOM_LEFT);

        StackPane.setMargin(textBox, new Insets(0, 0, 70, 70));

        // Floating decorative badges (InfluMatch style)
        StackPane badge1 = createFloatingBadge("⚔", GamerVaultStyles.ACCENT_PURPLE_LIGHT);
        StackPane.setAlignment(badge1, Pos.TOP_RIGHT);
        badge1.setTranslateX(-30);
        badge1.setTranslateY(60);
        GamerVaultAnimations.animateFloating(badge1, 5.5, -15, 10);

        StackPane badge2 = createFloatingBadge("🎯", "#ffb690");
        StackPane.setAlignment(badge2, Pos.TOP_LEFT);
        badge2.setTranslateX(30);
        badge2.setTranslateY(40);
        GamerVaultAnimations.animateFloating(badge2, 6.0, -10, 15);

        StackPane badge3 = createFloatingBadge("🏆", GamerVaultStyles.ACCENT_CYAN);
        StackPane.setAlignment(badge3, Pos.BOTTOM_RIGHT);
        badge3.setTranslateX(-50);
        badge3.setTranslateY(-100);
        GamerVaultAnimations.animateFloating(badge3, 4.5, -20, 5);

        leftPane.getChildren()
                .addAll(
                        playerBgImageView,
                        badge1, badge2, badge3);

        StackPane.setAlignment(textBox, Pos.BOTTOM_LEFT);
        return leftPane;
    }

    /**
     * Helper: creates a small glassmorphism floating badge with an icon.
     */
    private StackPane createFloatingBadge(String iconText, String fillColor) {
        StackPane badge = new StackPane();
        badge.setPadding(new Insets(12));
        badge.setMaxSize(50, 50);
        badge.setStyle(
                "-fx-background-radius: 14; -fx-border-radius: 14; "
                        + "-fx-background-color: rgba(255, 255, 255, 0.06); "
                        + "-fx-border-color: rgba(255, 255, 255, 0.12); -fx-border-width: 1;");
        DropShadow badgeShadow = new DropShadow(10, Color.rgb(0, 0, 0, 0.4));
        badge.setEffect(badgeShadow);

        Text icon = new Text(iconText);
        icon.setFont(Font.font(18));
        icon.setFill(Color.web(fillColor));
        badge.getChildren().add(icon);
        return badge;
    }

    /*
     * RIGHT SIDE CONTENT SECTION
     * It contains complete UI for login purpose.
     * CONTAINS -
     * 1) SOME TITLE
     * 2) EMAIL AREA
     * 3) PASSWORD AREA
     * 4) LOGIN BUTTON
     * 5) GOOGLE AUTH BUTTON
     * 6) NON LOGIN/REGISTERED BUTTON
     */
    private VBox getRightSideContent() {

        VBox paddedBox = new VBox();

        VBox rightBox = new VBox();

        rightBox.setAlignment(Pos.TOP_CENTER);
        rightBox.setSpacing(18);
        rightBox.setPadding(new Insets(40));

        rightBox.setPrefWidth(500);
        rightBox.setPrefHeight(620);

        // Apply glassmorphism card styling
        GamerVaultStyles.applyGlassCard(rightBox);

        Text logoText = new Text("⚔ GamerVault");
        logoText.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE));
        logoText.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        // Shimmer on logo
        GamerVaultAnimations.shimmerEffect(logoText, GamerVaultStyles.ACCENT_PURPLE,
                GamerVaultStyles.ACCENT_PURPLE_LIGHT, 5.0);

        Text subtitle = new Text("Initialize your dashboard sequence.");
        subtitle.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

        Text emailText = new Text("OPERATOR ID / EMAIL");
        emailText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

        // 2) TextField area for EMAIL - with focus glow
        TextField emailTextField = new TextField();
        emailTextField.setPromptText("Enter your email");
        HBox emailContainer = GamerVaultStyles.createStyledInput(emailTextField, GamerVaultStyles.ACCENT_PURPLE);
        emailContainer.setPrefHeight(55);

        Text passwordText = new Text("ACCESS CODE");
        passwordText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

        // 2) TextField area for PASSWORD - with focus glow and eye toggle
        PasswordField hiddenPassField = new PasswordField();
        hiddenPassField.setPromptText("••••••••");
        TextField revealedPassField = new TextField();
        revealedPassField.setPromptText("••••••••");

        String inputStyle = "-fx-background-color: transparent; -fx-text-fill: " + GamerVaultStyles.TEXT_PRIMARY
                + "; -fx-prompt-text-fill: #464555; -fx-font-size: 14px; -fx-padding: 0;";
        hiddenPassField.setStyle(inputStyle);
        revealedPassField.setStyle(inputStyle);
        revealedPassField.setVisible(false);
        revealedPassField.setManaged(false);
        hiddenPassField.textProperty().bindBidirectional(revealedPassField.textProperty());

        StackPane passInputStack = new StackPane();
        passInputStack.getChildren().addAll(hiddenPassField, revealedPassField);
        HBox.setHgrow(passInputStack, Priority.ALWAYS);

        // Eye toggle button
        Button toggleBtn = new Button("👁");
        toggleBtn.setStyle(
                "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0; -fx-text-fill: #918fa1;");
        toggleBtn.setOnAction(e -> {
            if (hiddenPassField.isVisible()) {
                hiddenPassField.setVisible(false);
                hiddenPassField.setManaged(false);
                revealedPassField.setVisible(true);
                revealedPassField.setManaged(true);
                toggleBtn.setText("🔒");
                revealedPassField.requestFocus();
            } else {
                revealedPassField.setVisible(false);
                revealedPassField.setManaged(false);
                hiddenPassField.setVisible(true);
                hiddenPassField.setManaged(true);
                toggleBtn.setText("👁");
                hiddenPassField.requestFocus();
            }
        });

        HBox passwordContainer = new HBox(12);
        passwordContainer.setAlignment(Pos.CENTER_LEFT);
        passwordContainer.setPrefHeight(55);
        passwordContainer.getChildren().addAll(passInputStack, toggleBtn);

        // Apply focus glow to the password container
        String passDefaultStyle = "-fx-background-color: " + GamerVaultStyles.INPUT_BG + "; "
                + "-fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.06); "
                + "-fx-border-radius: 10; -fx-border-width: 1; -fx-padding: 0 14;";
        String passFocusStyle = "-fx-background-color: " + GamerVaultStyles.INPUT_BG + "; "
                + "-fx-background-radius: 10; -fx-border-color: " + GamerVaultStyles.ACCENT_PURPLE + "; "
                + "-fx-border-radius: 10; -fx-border-width: 1.5; -fx-padding: 0 14; "
                + "-fx-effect: dropshadow(three-pass-box, " + GamerVaultStyles.ACCENT_PURPLE + "33, 10, 0, 0, 0);";
        passwordContainer.setStyle(passDefaultStyle);

        ChangeListener<Boolean> passFocusListener = (obs, oldVal, newVal) -> {
            if (newVal) {
                passwordContainer.setStyle(passFocusStyle);
            } else if (!hiddenPassField.isFocused() && !revealedPassField.isFocused()) {
                passwordContainer.setStyle(passDefaultStyle);
            }
        };
        hiddenPassField.focusedProperty().addListener(passFocusListener);
        revealedPassField.focusedProperty().addListener(passFocusListener);

        // 3) FORGOT BOX - DECORATION
        Hyperlink forgotPassword = new Hyperlink("Forgot Code?");
        forgotPassword.setStyle("-fx-text-fill: " + GamerVaultStyles.ACCENT_PURPLE
                + "; -fx-underline: false; -fx-font-size: 14px; -fx-padding: 0;");
        forgotPassword.setOnMouseEntered(e -> forgotPassword.setStyle(
                "-fx-text-fill: " + GamerVaultStyles.ACCENT_CYAN
                        + "; -fx-underline: false; -fx-font-size: 14px; -fx-padding: 0;"));
        forgotPassword.setOnMouseExited(e -> forgotPassword.setStyle(
                "-fx-text-fill: " + GamerVaultStyles.ACCENT_PURPLE
                        + "; -fx-underline: false; -fx-font-size: 14px; -fx-padding: 0;"));

        HBox forgotBox = new HBox();
        forgotBox.setAlignment(Pos.CENTER_RIGHT);
        forgotBox.getChildren().add(forgotPassword);

        Label errorMessageLabel = new Label();
        errorMessageLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        errorMessageLabel.setTextFill(Color.RED);
        errorMessageLabel.setVisible(false);

        /*
         * 4) LOGIN BUTTON
         * LOGIN USING EMAIL-PASS OF FIREBASE
         */
        Button loginButton = new Button("EXECUTE LOGIN");
        loginButton.setPrefWidth(400);
        loginButton.setPrefHeight(60);
        // Apply gradient button with scale animations
        GamerVaultStyles.applyGradientButton(loginButton, GamerVaultStyles.ACCENT_PURPLE,
                GamerVaultStyles.ACCENT_PURPLE_DARK, "white");

        // LOGIN BUTTON ACTION
        loginButton.setOnAction(event -> {

            String email = emailTextField.getText().trim().toLowerCase();
            String password = hiddenPassField.getText().trim();

            String responseMessage = AuthController.handleLogin(email, password);
            if (responseMessage.contains("successful")) {
                errorMessageLabel.setText(responseMessage);
                errorMessageLabel.setTextFill(Color.GREEN);
                errorMessageLabel.setVisible(true);

                if (AuthController.currentUser != null
                        && "ADMIN".equalsIgnoreCase(AuthController.currentUser.getRole())) {
                    navigateToAdminMainScreen();
                } else {
                    navigateToPlayerMainScreen();
                }
            } else {
                errorMessageLabel.setText(responseMessage);
                errorMessageLabel.setTextFill(Color.RED);
                errorMessageLabel.setVisible(true);
            }
            clearFields(emailTextField, hiddenPassField);
        });

        // Divider
        HBox dividerRow = new HBox(12);
        dividerRow.setAlignment(Pos.CENTER);
        Region line1 = new Region();
        line1.setPrefHeight(1);
        HBox.setHgrow(line1, Priority.ALWAYS);
        line1.setStyle("-fx-background-color: rgba(255,255,255,0.1);");
        Text middleText = new Text("GOOGLE AUTH");
        middleText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        middleText.setFont(Font.font("Arial", FontWeight.MEDIUM, 12));
        Region line2 = new Region();
        line2.setPrefHeight(1);
        HBox.setHgrow(line2, Priority.ALWAYS);
        line2.setStyle("-fx-background-color: rgba(255,255,255,0.1);");
        dividerRow.getChildren().addAll(line1, middleText, line2);

        /*
         * 5) GOOGLE LOGIN BUTTON
         * LOGIN USING DIRECT GOOGLE AUTHENTICATION
         */
        Button googleButton = new Button("Continue with Google");
        googleButton.setPrefWidth(400);
        googleButton.setPrefHeight(50);
        // Apply ghost button styling
        GamerVaultStyles.applyGhostButton(googleButton);

        // GOOGLE AUTH LOGIN BUTTON ACTION
        googleButton.setOnAction(event -> {

        });

        Text registerText = new Text("New recruit? ");
        registerText.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));

        // REGISTER BUTTON - NEW PLAYERS REGISTER HERE
        Hyperlink registerButton = new Hyperlink("Register Now");
        registerButton.setStyle("-fx-text-fill: " + GamerVaultStyles.ACCENT_PURPLE
                + "; -fx-font-weight: bold; -fx-underline: false; -fx-font-size: 14px; -fx-padding: 0;");
        registerButton.setOnMouseEntered(e -> registerButton.setStyle(
                "-fx-text-fill: " + GamerVaultStyles.ACCENT_CYAN
                        + "; -fx-font-weight: bold; -fx-underline: true; -fx-font-size: 14px; -fx-padding: 0;"));
        registerButton.setOnMouseExited(e -> registerButton.setStyle(
                "-fx-text-fill: " + GamerVaultStyles.ACCENT_PURPLE
                        + "; -fx-font-weight: bold; -fx-underline: false; -fx-font-size: 14px; -fx-padding: 0;"));

        // REGISTER BUTTON ACTION
        registerButton.setOnAction(event -> {
            // NAVIGATE TO REGISTER SCREEN
            navigateToRegisterScreen();

        });

        HBox registerBox = new HBox();
        registerBox.setAlignment(Pos.CENTER);
        registerBox.getChildren().addAll(registerText, registerButton);

        // RIGHT SIDE COMPLETE ARRANGEMENT OF UI
        rightBox.getChildren().addAll(
                logoText,
                subtitle,

                SizedBox.height(15),

                emailText,
                emailContainer,

                passwordText,
                passwordContainer,

                forgotBox,

                SizedBox.height(10),

                errorMessageLabel,

                loginButton,

                SizedBox.height(10),

                dividerRow,
                googleButton,

                SizedBox.height(15),

                registerBox);

        // Stagger animate form fields
        GamerVaultAnimations.staggerFadeInChildren(rightBox, 60);

        paddedBox.getChildren().addAll(SizedBox.height(35), rightBox, SizedBox.height(35));
        return paddedBox;
    }

    private void clearFields(TextField emailTextField, PasswordField hiddenPassField) {
        emailTextField.clear();
        hiddenPassField.clear();
    }

    // NAVIGATION METHODS

    /*
     * Navigation : LOGIN -> REGISTERATION SCREEN
     */
    void navigateToRegisterScreen() {
        System.out.println("DEBUG: Navigating to Register Screen"); // DEBUG LINE

        RegistrationScreen registerationScreen = new RegistrationScreen();
        registerationScreen.setRegisterationScreenStage(loginScreenStage);
        registerationScreenScene = new Scene(registerationScreen.startRegisterationScreen(this::handleBackButton),
                loginScreenStage.getWidth(), loginScreenStage.getHeight());
        registerationScreen.setRegisterationScreenScene(registerationScreenScene);
        loginScreenStage.setScene(registerationScreenScene);
    }

    /*
     * Navigation : LOGIN -> PLAYER MAIN SCREEN
     */

    void navigateToPlayerMainScreen() {
        System.out.println("DEBUG: Navigating to Player Main Screen"); // DEBUG LINE

        PlayerMainScreen playerMainScreen = new PlayerMainScreen();
        playerMainScreen.setPlayerMainScreenStage(loginScreenStage);
        playerMainScreenScene = new Scene(
                playerMainScreen.startPlayerMainScreen(this::handleBackButton), loginScreenStage.getWidth(),
                loginScreenStage.getHeight());
        playerMainScreen.setPlayerMainScreenScene(playerMainScreenScene);

        loginScreenStage.setScene(playerMainScreenScene);
    }

    /*
     * Navigation: LOGIN -> ADMIN MAIN SCREEN
     */
    void navigateToAdminMainScreen() {
        System.out.println("DEBUG: Navigating to Admin Main Screen");

        AdminMainScreen adminMainScreen = new AdminMainScreen();
        adminMainScreen.setAdminMainScreenStage(loginScreenStage);
        adminMainScreenScene = new Scene(adminMainScreen.startAdminMainScreen(this::handleBackButton),
                loginScreenStage.getWidth(), loginScreenStage.getHeight());

        adminMainScreen.setAdminMainScreenScene(adminMainScreenScene);

        loginScreenStage.setScene(adminMainScreenScene);

    }

    void handleBackButton() {
        loginScreenStage.setScene(loginScreenScene);
    }
}