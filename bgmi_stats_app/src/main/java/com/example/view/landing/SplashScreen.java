package com.example.view.landing;

import com.example.view.auth.LoginScreen;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashScreen extends Application {
    // Variables
    Scene splashScreenScene;
    Stage splashScreenStage;

    // Variables for navigating screens
    Scene loginScreenScene;

    // Main method for beginning
    public void start(Stage primaryStage) {

        // Root atmospheric StackPane
        StackPane rootStack = new StackPane();
        rootStack.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        // Center content layer
        VBox centerBox = getCenterBox();

        // Bottom loader layer
        VBox bottomLoaderBox = getBottomLoaderBox();

        // Main layout container
        VBox mainLayout = new VBox();
        mainLayout.setAlignment(Pos.CENTER);
        VBox.setVgrow(centerBox, Priority.ALWAYS);
        mainLayout.getChildren().addAll(centerBox, bottomLoaderBox);

        rootStack.getChildren().add(mainLayout);

        // Setting stage and screen for splash screen
        splashScreenStage = primaryStage;
        splashScreenStage.setTitle("BGMI GamerVault");

        Scene newScene = new Scene(rootStack, splashScreenStage.getWidth(), splashScreenStage.getHeight());
        splashScreenScene = newScene;
        splashScreenStage.setScene(splashScreenScene);

        splashScreenStage.setScene(splashScreenScene);
        splashScreenStage.setMaximized(true);
        splashScreenStage.show();

        PauseTransition delayTimer = delaySplashScreenTransition();
        delayTimer.play();

    }

    // Center Box for LOGO, TITLE AND SUBTITLE
    VBox getCenterBox() {

        VBox centerBox = new VBox();
        centerBox.setAlignment(Pos.CENTER);

        Image logoImage = new Image(getClass().getResource("/assets/logoImage/LOGO.jpg").toExternalForm());

        ImageView logoImageView = new ImageView(logoImage);
        logoImageView.setFitWidth(200);
        logoImageView.setFitHeight(200);

        // Fade in the logo
        logoImageView.setOpacity(0);
        FadeTransition logoFade = new FadeTransition(Duration.millis(1000), logoImageView);
        logoFade.setFromValue(0);
        logoFade.setToValue(1);
        logoFade.setDelay(Duration.millis(200));
        logoFade.play();

        // Pulse breathing on logo
        GamerVaultAnimations.pulseGlow(logoImageView, 3.0);

        // Add a shimmer glow behind the logo
        DropShadow logoGlow = new DropShadow(40, Color.web(GamerVaultStyles.ACCENT_PURPLE, 0.5));
        logoImageView.setEffect(logoGlow);

        // Title Text below LOGO with rich accent color
        Text titleText = new Text("BGMI GamerVault");
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleText.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
        // Shimmer effect on title
        GamerVaultAnimations.shimmerEffect(titleText, GamerVaultStyles.ACCENT_PURPLE, GamerVaultStyles.ACCENT_CYAN,
                4.0);

        // Fade in up the title
        GamerVaultAnimations.fadeInUp(titleText, 600, 800);

        // Subtitle Text
        Text subtitleText = new Text("Initializing Ultimate Gaming Experience...");
        subtitleText.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        subtitleText.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));

        // Fade in up the subtitle (staggered)
        GamerVaultAnimations.fadeInUp(subtitleText, 900, 800);

        centerBox.getChildren().addAll(logoImageView, SizedBox.height(20), titleText, SizedBox.height(10),
                subtitleText);

        return centerBox;
    }

    // Bottom Box for loading indicator
    VBox getBottomLoaderBox() {

        VBox bottomLoaderBox = new VBox();
        bottomLoaderBox.setAlignment(Pos.CENTER);
        bottomLoaderBox.setSpacing(10);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxHeight(50);
        progressIndicator.setMaxWidth(50);
        // Style the spinner with purple accent
        progressIndicator.setStyle("-fx-accent: " + GamerVaultStyles.ACCENT_PURPLE + ";");

        Text loadingText = new Text("Gathering data...");
        loadingText.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        loadingText.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));

        // Fade in the loader section
        GamerVaultAnimations.fadeInUp(progressIndicator, 1200, 600);
        GamerVaultAnimations.fadeInUp(loadingText, 1400, 600);

        bottomLoaderBox.getChildren().addAll(progressIndicator, SizedBox.height(10), loadingText, SizedBox.height(20));

        return bottomLoaderBox;
    }

    // Method to have a timer for splash Screen before moving forward to the next
    // Screen.
    PauseTransition delaySplashScreenTransition() {
        PauseTransition delay = new PauseTransition(Duration.seconds(3));

        delay.setOnFinished(event -> {
            System.out.println("Timer of splash screen ended.");

            // Logic for navigating to next screen
            navigateToLoginScreen();

        });
        return delay;
    }

    // Next screen navigation logic method
    // Navigation to LoginScreen after the splash Screen for user to Login or
    // Register
    void navigateToLoginScreen() {
        System.out.println("DEBUG: Navigating to Login Screen"); // DEBUG LINE

        // Setting up the login screen with its scene and stage to navigate.
        LoginScreen loginScreen = new LoginScreen();
        loginScreen.setLoginScreenStage(splashScreenStage);
        loginScreenScene = new Scene(loginScreen.startLoginScreen(), splashScreenStage.getScene().getWidth(),
                splashScreenStage.getScene().getHeight());
        loginScreen.setLoginScreenScene(loginScreenScene);
        // splashScreenStage.setScene(loginScreenScene);

        GamerVaultAnimations.slideScreenSwap(splashScreenStage, loginScreenScene);
    }
}