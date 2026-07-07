package com.example.view.player;

import com.example.model.UserModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class PlayerDashboardHeroSection {

        /*
         * HERO SECTION CONTENT - TOP MOST SECTION OF PLAYER DASHBOARD
         * It ontains the welcoing of player along with quick navigation button for
         * Upload match, profile and tournaments.
         * CONTAINS -
         * 1) TEXTS
         * 2) BUTTONS
         */
        public static VBox createHeroSection(UserModel user, PlayerMainScreen mainScreen) {

                VBox paddedBox = new VBox(15);
                paddedBox.setPadding(new Insets(25));

                // Apply glassmorphism with a subtle purple gradient overlay
                paddedBox.setStyle(
                                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, rgba(139,92,246,0.08), rgba(17,24,39,0.85)); "
                                                + "-fx-background-radius: 16; -fx-border-radius: 16; "
                                                + "-fx-border-color: rgba(255,255,255,0.08); -fx-border-width: 1;");
                DropShadow heroShadow = new DropShadow(25, Color.rgb(0, 0, 0, 0.5));
                heroShadow.setOffsetY(8);
                paddedBox.setEffect(heroShadow);

                HBox container = new HBox(15);

                Text welcomeText = new Text("Welcome back,");
                welcomeText.setFont(Font.font("Arial", FontWeight.BOLD, 48));
                welcomeText.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

                // Name From Database
                String ign = (user != null && user.getIgn() != null && !user.getIgn().isEmpty()) ? user.getIgn()
                                : "Player";
                Text playerNameText = new Text(ign);
                playerNameText.setFont(Font.font("Arial", FontWeight.BOLD, 48));
                playerNameText.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE));
                GamerVaultAnimations.shimmerEffect(playerNameText, GamerVaultStyles.ACCENT_PURPLE,
                                GamerVaultStyles.ACCENT_PURPLE_LIGHT, 5.0);

                // VERIFIED TEXT
                boolean isVerified = true;
                // user != null && user.isVerified();
                Text statusText = new Text(isVerified ? "✓ Verified Player" : "Pending Verification");
                statusText.setFill(isVerified ? Color.web(GamerVaultStyles.ACCENT_GREEN)
                                : Color.web(GamerVaultStyles.ACCENT_ORANGE));
                statusText.setFont(Font.font("Arial", FontWeight.BOLD, 24));

                VBox textsBox = new VBox(15);
                textsBox.getChildren().addAll(welcomeText, playerNameText, SizedBox.height(5), statusText);

                GamerVaultAnimations.fadeInUp(welcomeText, 100, 500);
                GamerVaultAnimations.fadeInUp(playerNameText, 250, 500);
                GamerVaultAnimations.fadeInUp(statusText, 400, 500);

                // BUTTONS FOR QUICK NAVIGATION FROM HERO SECTION
                HBox buttonsBox = new HBox(15);

                Button uploadBtn = new Button("📤 Upload Match");
                uploadBtn.setPrefSize(220, 55);
                GamerVaultStyles.applyGradientButton(uploadBtn, GamerVaultStyles.ACCENT_PURPLE,
                                GamerVaultStyles.ACCENT_PURPLE_DARK, "white");
                uploadBtn.setOnAction(e -> {
                        PlayerDashboardSidebar.pageView = "upload";
                        if (mainScreen != null)
                                mainScreen.updateCenter();
                });

                Button profileBtn = new Button("👤 View Profile");
                profileBtn.setPrefSize(220, 55);
                GamerVaultStyles.applyGhostButton(profileBtn);
                profileBtn.setOnAction(e -> {
                        PlayerDashboardSidebar.pageView = "profile";
                        if (mainScreen != null)
                                mainScreen.updateCenter();
                });

                Button tournamentsBtn = new Button("🏆 Find Tournaments");
                tournamentsBtn.setPrefSize(220, 55);
                GamerVaultStyles.applyGhostButton(tournamentsBtn);
                tournamentsBtn.setOnAction(e -> {
                        PlayerDashboardSidebar.pageView = "discover_content";
                        if (mainScreen != null)
                                mainScreen.updateCenter();
                });

                buttonsBox.getChildren().addAll(uploadBtn, profileBtn, tournamentsBtn);

                container.getChildren().addAll(textsBox, SizedBox.width(80), buttonsBox);

                paddedBox.getChildren().addAll(container);

                return paddedBox;
        }
}
