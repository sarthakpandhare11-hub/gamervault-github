package com.example.view.auth;

import com.example.controller.AuthController;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class RegistrationScreen {

        Scene registerationScreenScene;
        Stage registerationScreenStage;

        // Setters for the scene and stage of the registeration screen
        public void setRegisterationScreenScene(Scene registerationScreenScene) {
                this.registerationScreenScene = registerationScreenScene;
        }

        public void setRegisterationScreenStage(Stage registerationScreenStage) {
                this.registerationScreenStage = registerationScreenStage;
        }

        // Start method for the registeration screen
        public StackPane startRegisterationScreen(Runnable backNavigation) {

                // Root atmospheric StackPane
                StackPane root = new StackPane();
                root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

                // IMAGES HORIZONTAL BOX COMPLETELY, TOTAL OF 4 IMAGES
                HBox imagesBox = getImagesBox();
                // Make images slightly translucent so atmospheric bg shows through
                imagesBox.setOpacity(0.35);

                VBox mainContentBox = getRegistrationBoxContent(backNavigation);
                StackPane.setAlignment(mainContentBox, Pos.CENTER);
                root.getChildren().addAll(imagesBox, mainContentBox);

                // Animate form entrance
                GamerVaultAnimations.fadeInUp(mainContentBox, 200, 700);

                return root;
        }

        // BACKGROUND UI - 4 IMAGES TOGETHER IN HORIZONTAL WAY
        /* All images are arranged in horizontal way as background for UI. */
        HBox getImagesBox() {

                HBox imagesBox = new HBox();

                ImageView imageView1 = new ImageView(
                                new Image(getClass().getResource("/assets/registerPageImage/image1.jpg")
                                                .toExternalForm()));
                imageView1.setFitWidth(395);
                imageView1.setFitHeight(700);
                imageView1.setPreserveRatio(true);

                ImageView imageView2 = new ImageView(
                                new Image(getClass().getResource("/assets/registerPageImage/image2.jpg")
                                                .toExternalForm()));
                imageView2.setFitWidth(395);
                imageView2.setFitHeight(700);
                imageView2.setPreserveRatio(true);

                ImageView imageView3 = new ImageView(
                                new Image(getClass().getResource("/assets/registerPageImage/image3.jpg")
                                                .toExternalForm()));
                imageView3.setFitWidth(395);
                imageView3.setFitHeight(700);
                imageView3.setPreserveRatio(true);

                ImageView imageView4 = new ImageView(
                                new Image(getClass().getResource("/assets/registerPageImage/image4.jpg")
                                                .toExternalForm()));
                imageView4.setFitWidth(395);
                imageView4.setFitHeight(700);
                imageView4.setPreserveRatio(true);

                imagesBox.getChildren().addAll(imageView1, imageView2, imageView3, imageView4);

                return imagesBox;
        }

        /*
         * Box Content
         * Registeration Box
         */
        VBox getRegistrationBoxContent(Runnable backToPreviousScreen) {

                VBox paddedBox = new VBox();

                VBox mainContentBox = new VBox();
                mainContentBox.setMaxWidth(500);
                mainContentBox.setMaxHeight(20);

                mainContentBox.setAlignment(Pos.TOP_CENTER);
                mainContentBox.setSpacing(25);
                mainContentBox.setPadding(new Insets(20));

                // Apply glassmorphism card styling
                GamerVaultStyles.applyGlassCard(mainContentBox);

                GamerVaultAnimations.applyHoverTilt(mainContentBox);

                // Main Large Title of GAMERVAULT with shimmer
                Text mainTitle = new Text("GAMERVAULT");
                mainTitle.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
                mainTitle.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 48));
                GamerVaultAnimations.shimmerEffect(mainTitle, GamerVaultStyles.ACCENT_PURPLE,
                                GamerVaultStyles.ACCENT_PURPLE_LIGHT, 4.0);

                Text createLabel = new Text("Create Account");
                createLabel.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
                createLabel.setFont(Font.font("Arial", FontWeight.MEDIUM, 24));

                Text subLabel = new Text("Join the elite competitive ecosystem.");
                subLabel.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
                subLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

                TextField userNameTextField = new TextField();
                TextField emailTextField = new TextField();
                PasswordField passwordTextField = new PasswordField();
                PasswordField confirmPasswordTextField = new PasswordField();

                // TEXTFIELD USERNAME FIELDS - with styled input
                VBox userNameBox = createTextFieldBox("Username", "ProPlayer99", userNameTextField, "👤");

                // TEXTFIELD EMAIL FIELDS - with styled input
                VBox emailAddressBox = createTextFieldBox("Email Address", "player@gmail.com", emailTextField, "📧");

                // TEXTFIELD PASSWORD FIELD - with styled input
                VBox passwordBox = createPassFieldBox("Password", "••••••••", passwordTextField);

                // TEXTFIELD CONFIRM - PASSWORD FIELD - with styled input
                VBox confirmPasswordBox = createPassFieldBox("Confirm Password", "••••••••", confirmPasswordTextField);

                // CREATE ACCOUNT BUTTON - gradient style
                /* BUTTON USED WHEN PLAYER WILL BE CREATING NEW ACCOUNT FOR THE FIRST TIME */
                Button createAccButton = new Button("CREATE ACCOUNT");
                createAccButton.setPrefWidth(400);
                createAccButton.setPrefHeight(60);
                GamerVaultStyles.applyGradientButton(createAccButton, GamerVaultStyles.ACCENT_PURPLE,
                                GamerVaultStyles.ACCENT_PURPLE_DARK, "white");

                Label errorMessageLabel = new Label();
                errorMessageLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
                errorMessageLabel.setTextFill(Color.RED);
                errorMessageLabel.setVisible(false);

                // CREATE ACCOUNT BUTTON ACTION
                createAccButton.setOnAction(event -> {
                        String userName = userNameTextField.getText().trim();
                        String email = emailTextField.getText().trim();
                        String password = passwordTextField.getText();
                        String confirmPassword = confirmPasswordTextField.getText();

                        createAccButton.setDisable(true);
                        createAccButton.setText("CREATING ACCOUNT...");

                        // CALL TO NEW REGISTER METHOD
                        new Thread(() -> {
                                String errorMessage = AuthController.handleNewRegister(
                                                email,
                                                password,
                                                confirmPassword,
                                                userName);

                                Platform.runLater(() -> {
                                        if (errorMessage.contains("successfully")) {
                                                errorMessageLabel.setText(errorMessage);
                                                errorMessageLabel.setTextFill(Color.GREEN);
                                                errorMessageLabel.setVisible(true);
                                        } else {
                                                GamerVaultAnimations.shakeOnError(mainContentBox);

                                                errorMessageLabel.setText(errorMessage);
                                                errorMessageLabel.setTextFill(Color.RED);
                                                errorMessageLabel.setVisible(true);
                                        }
                                        clearFields(userNameTextField, emailTextField, passwordTextField,
                                                        confirmPasswordTextField);

                                        createAccButton.setDisable(false);
                                        createAccButton.setText("CREATE ACCOUNT");
                                });
                        }).start();
                });

                Text alreadyAccText = new Text("Already have an account?");
                alreadyAccText.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));

                // REGISTER BUTTON - NEW PLAYERS REGISTER HERE
                Hyperlink loginButton = new Hyperlink("Login Here");
                loginButton.setFocusTraversable(false); // FIX: Stops the CSS focus warning
                loginButton.setStyle("-fx-text-fill: " + GamerVaultStyles.ACCENT_PURPLE
                                + "; -fx-font-weight: bold; -fx-underline: false; -fx-font-size: 14px; -fx-padding: 0;");
                loginButton.setOnMouseEntered(e -> loginButton.setStyle(
                                "-fx-text-fill: " + GamerVaultStyles.ACCENT_CYAN
                                                + "; -fx-font-weight: bold; -fx-underline: true; -fx-font-size: 14px; -fx-padding: 0;"));
                loginButton.setOnMouseExited(e -> loginButton.setStyle(
                                "-fx-text-fill: " + GamerVaultStyles.ACCENT_PURPLE
                                                + "; -fx-font-weight: bold; -fx-underline: false; -fx-font-size: 14px; -fx-padding: 0;"));

                // REGISTER BUTTON ACTION
                loginButton.setOnAction(event -> {
                        System.out.println("DEBUG: Navigating back to login Screen from Registeration Screen.");
                        // NAVIGATE TO LOGIN SCREEN BACK
                        backToPreviousScreen.run();

                });

                HBox registerBox = new HBox();
                registerBox.setAlignment(Pos.CENTER);
                registerBox.getChildren().addAll(alreadyAccText, loginButton);

                // OVERALL ARRANGEMENT FOR THE REGISTERATION FIELDS
                mainContentBox.getChildren().addAll(
                                SizedBox.height(25),

                                mainTitle,

                                createLabel,
                                subLabel,

                                userNameBox,

                                emailAddressBox,

                                passwordBox,

                                confirmPasswordBox,

                                createAccButton,

                                SizedBox.height(5),

                                registerBox,

                                errorMessageLabel);

                // Stagger-animate form fields
                GamerVaultAnimations.staggerFadeInChildren(mainContentBox, 50);

                paddedBox.getChildren().addAll(SizedBox.height(25), mainContentBox, SizedBox.height(20));

                paddedBox.setAlignment(Pos.CENTER);
                paddedBox.setFillWidth(false);

                return paddedBox;
        }

        // Create a UI for 1 complete TextField for this screen UI.
        /* Total 2 TextFields */
        VBox createTextFieldBox(String mainText, String hintText, TextField textField, String iconSymbol) {

                // VBox textFieldBox = new VBox();
                // textFieldBox.setAlignment(Pos.TOP_LEFT);

                // Text text = new Text(mainText);
                // text.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));

                // textField.setPromptText(hintText);

                // HBox styledContainer = GamerVaultStyles.createStyledInput(textField,
                // GamerVaultStyles.ACCENT_PURPLE);
                // styledContainer.setPrefHeight(55);

                // textFieldBox.getChildren().setAll(text, SizedBox.height(10),
                // styledContainer);

                // return textFieldBox;
                VBox textFieldBox = new VBox(8);
                textFieldBox.setAlignment(Pos.TOP_LEFT);

                Text text = new Text(mainText);
                text.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
                text.setFont(Font.font("Arial", FontWeight.BOLD, 12)); // Crisper label

                textField.setPromptText(hintText);
                textField.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
                HBox.setHgrow(textField, Priority.ALWAYS);

                Text icon = new Text(iconSymbol);
                icon.setFill(Color.web("#6B7280"));

                HBox styledContainer = new HBox(10, icon, textField);
                styledContainer.setAlignment(Pos.CENTER_LEFT);
                styledContainer.setPadding(new Insets(0, 15, 0, 15));
                styledContainer.setPrefHeight(50);
                styledContainer.setStyle(
                                "-fx-background-color: #111827; -fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (isNowFocused) {
                                styledContainer.setStyle(
                                                "-fx-background-color: #1F2937; -fx-border-color: #8B5CF6; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
                                icon.setFill(Color.web("#8B5CF6")); // Purple glow for registration
                        } else {
                                styledContainer.setStyle(
                                                "-fx-background-color: #111827; -fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
                                icon.setFill(Color.web("#6B7280"));
                        }
                });

                textFieldBox.getChildren().setAll(text, styledContainer);
                return textFieldBox;
        }

        /*
         * PassWord Field
         * This will be to hide the text that ill br typed by player/user.
         * Total 2 PasswordField
         */
        VBox createPassFieldBox(String mainText, String hintText, PasswordField passwordField) {

                VBox textFieldBox = new VBox();
                textFieldBox.setAlignment(Pos.TOP_LEFT);

                Text text = new Text(mainText);
                text.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));

                passwordField.setPromptText(hintText);
                HBox styledContainer = GamerVaultStyles.createStyledInput(passwordField,
                                GamerVaultStyles.ACCENT_PURPLE);
                styledContainer.setPrefHeight(55);

                textFieldBox.getChildren().setAll(text, SizedBox.height(10), styledContainer);
                return textFieldBox;
        }

        /*
         * Clears all the textfield completely.
         */
        private void clearFields(
                        TextField userNameField,
                        TextField emailField,
                        PasswordField passwordField,
                        PasswordField confirmpasswordField) {
                userNameField.clear();
                emailField.clear();
                passwordField.clear();
                confirmpasswordField.clear();
        }
}
