package com.example.view.player;

import java.util.List;

import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public interface FeatureCardsMethods {

        /*
         * This card is used for creating various card on dashboard which are seen that
         * are LastMatchCard, PerformanceCard, LeaderboardCard
         * CONTAINS:
         * 1) Header with title and trend arrow
         * 2) Main trend with trend value and trend text
         * 
         * PARAMETERS :
         * titleText - Title of the card
         * iconText - Icon of the card
         * mainStatValueText - Main stat value of the card
         * subValueText - Sub stat value of the card
         * row1StatBox1List - Row 1 stat box 1 list
         * - First element is the icon
         * - Second element is the value
         * - Third element is the text
         * row1StatBox2List - Row 1 stat box 2 list
         * - First element is the icon
         * - Second element is the value
         * - Third element is the text
         * row2StatBox1List - Row 2 stat box 1 list
         * - First element is the icon
         * - Second element is the value
         * - Third element is the text
         * row2StatBox2List - Row 2 stat box 2 list
         * - First element is the icon
         * - Second element is the value
         * - Third element is the text
         * 
         * Return Type:
         * VBox
         */
        static StackPane createDashboardStatCard(
                        String titleText,
                        String iconText,
                        String mainStatValueText,
                        String subValueText,
                        String bgImagePath,
                        List<String> row1StatBox1List,
                        List<String> row1StatBox2List,
                        List<String> row2StatBox1List,
                        List<String> row2StatBox2List) {

                StackPane rootCard = new StackPane();
                rootCard.setPrefSize(340, 220);

                // --- 1. THE FAINTED BACKGROUND IMAGE ---
                ImageView bgImage = new ImageView();

                bgImage.setManaged(false);
                bgImage.setPreserveRatio(false);
                bgImage.fitWidthProperty().bind(rootCard.widthProperty());
                bgImage.fitHeightProperty().bind(rootCard.heightProperty());
                bgImage.setOpacity(0.40);

                // Crop image to match the 14px border radius of the card
                Rectangle clip = new Rectangle();
                clip.widthProperty().bind(rootCard.widthProperty());
                clip.heightProperty().bind(rootCard.heightProperty());
                clip.setArcWidth(28);
                clip.setArcHeight(28);
                bgImage.setClip(clip);

                try {
                        if (bgImagePath != null && !bgImagePath.isEmpty()
                                        && FeatureCardsMethods.class.getResource(bgImagePath) != null) {
                                bgImage.setImage(new Image(
                                                FeatureCardsMethods.class.getResource(bgImagePath).toExternalForm(),
                                                true));
                        }
                } catch (Exception e) {
                        System.out.println("Could not load card background: " + bgImagePath);
                }

                // --- 2. THE FOREGROUND CONTENT ---
                VBox content = new VBox(18);
                content.setPadding(new Insets(20));

                HBox header = new HBox();
                Text mainTitleText = new Text(titleText);
                mainTitleText.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
                mainTitleText.setFont(Font.font("Poppins", FontWeight.BOLD, 24));

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Text mainIconText = new Text(iconText);
                mainIconText.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
                mainIconText.setFont(Font.font("Poppins", FontWeight.BOLD, 18));

                header.getChildren().addAll(mainTitleText, spacer, mainIconText);

                Text mainValueText = new Text(mainStatValueText);
                mainValueText.setFill(Color.web(GamerVaultStyles.ACCENT_ORANGE));
                mainValueText.setFont(Font.font("Poppins", FontWeight.BOLD, 32));

                Text mainSubText = new Text(subValueText);
                mainSubText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                mainSubText.setFont(Font.font("Poppins", FontWeight.NORMAL, 14));

                HBox row1 = new HBox(40);
                VBox mainRow1StatBox1 = dashboardCardStatBox(row1StatBox1List.get(0), row1StatBox1List.get(1),
                                row1StatBox1List.get(2));
                Region spacerRegion1 = new Region();
                HBox.setHgrow(spacerRegion1, Priority.ALWAYS);
                VBox mainRow1StatBox2 = dashboardCardStatBox(row1StatBox2List.get(0), row1StatBox2List.get(1),
                                row1StatBox2List.get(2));
                row1.getChildren().addAll(SizedBox.width(7), mainRow1StatBox1, spacerRegion1, mainRow1StatBox2,
                                SizedBox.width(7));

                HBox row2 = new HBox(40);
                VBox mainRow2StatBox1 = dashboardCardStatBox(row2StatBox1List.get(0), row2StatBox1List.get(1),
                                row2StatBox1List.get(2));
                Region spacerRegion2 = new Region();
                HBox.setHgrow(spacerRegion2, Priority.ALWAYS);
                VBox mainRow2StatBox2 = dashboardCardStatBox(row2StatBox2List.get(0), row2StatBox2List.get(1),
                                row2StatBox2List.get(2));
                row2.getChildren().addAll(SizedBox.width(7), mainRow2StatBox1, spacerRegion2, mainRow2StatBox2,
                                SizedBox.width(7));

                content.getChildren().addAll(header, mainValueText, mainSubText, row1, SizedBox.height(5), row2);

                // --- 3. ASSEMBLE AND STYLE ---
                GamerVaultStyles.applyGlassCard(rootCard); // Apply styling to the root StackPane
                GamerVaultAnimations.applyHoverTilt(rootCard); // Moving the tilt directly into the component

                rootCard.getChildren().addAll(bgImage, content);

                return rootCard;
        }

        /*
         * This method is used by createDashboardStatCard method to create the stat
         * boxes.
         * CONTAINS:
         * 1) icon of the stat
         * 2) value of the stat
         * 3) title of the stat
         * These 3 things are in total counted as 1 stat shown in vertical.
         * 
         * PARAMETERS :
         * icon - Icon of the stat
         * value - Value of the stat
         * title - Title of the stat
         * Return Type:
         * VBox
         * 
         * Stat Box example -
         * - ⭐
         * - Top 5%
         * - Players
         */
        static VBox dashboardCardStatBox(String icon, String value, String title) {
                VBox box = new VBox(4);
                Text iconText = new Text(icon);
                iconText.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
                iconText.setFont(Font.font("Arial", FontWeight.BOLD, 22));

                Text valueText = new Text(value);
                valueText.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
                valueText.setFont(Font.font("Arial", FontWeight.BOLD, 28));

                Text titleText = new Text(title);
                titleText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                titleText.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

                box.getChildren().addAll(iconText, valueText, titleText);
                return box;
        }

        /*
         * This method is used by createDashboardContentCard method to create the
         * content boxes.
         * 
         * - PARAMETER content1List wants things in sequnce that are,
         * 1) imageURL/ Icon of the content
         * 2) title of the content
         * 3) time Uploaded of the content
         * 4) description of the content
         * 
         * 
         * These 4 things are in total counted as 1 content shown in vertical.
         * 
         * PARAMETERS :
         * icon - Icon of the content
         * title - Title of the content
         * subtitle - Uploaded Time of the content
         * description - Description of the content
         * Return Type:
         * VBox
         * 
         * Content Box example -
         * - ▶
         * - TDM Movement Guide
         * - 2 Hours Ago
         * - Learn advanced movement techniques.
         */
        static StackPane createDashboardContentCard(
                        String titleText, String iconText, List<String> content1List, List<String> content2List,
                        String buttonColor, Runnable seeMore) {

                StackPane rootCard = new StackPane();

                // THE FIX: Hard-lock the Row 2 cards as well
                rootCard.setPrefSize(370, 260);

                VBox content = new VBox(18);
                content.setPadding(new Insets(20));

                HBox header = new HBox();
                Text mainTitleText = new Text(titleText);
                mainTitleText.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
                mainTitleText.setFont(Font.font("Poppins", FontWeight.BOLD, 24));

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Text mainIconText = new Text(iconText);
                mainIconText.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
                mainIconText.setFont(Font.font("Poppins", FontWeight.BOLD, 18));
                header.getChildren().addAll(mainTitleText, spacer, mainIconText);

                VBox itemsContainer = new VBox(10);
                itemsContainer.getChildren().addAll(
                                createInteractiveRowItem(content1List.get(0), content1List.get(1), content1List.get(2),
                                                content1List.get(3)),
                                createInteractiveRowItem(content2List.get(0), content2List.get(1), content2List.get(2),
                                                content2List.get(3)));

                Button seeMoreButton = new Button("See More");
                seeMoreButton.setPrefWidth(120);
                seeMoreButton.setPrefHeight(35);
                GamerVaultStyles.applyGradientButton(seeMoreButton, buttonColor, buttonColor, "white");
                GamerVaultAnimations.applyPremiumHover(seeMoreButton, buttonColor);
                seeMoreButton.setOnAction(e -> {
                        if (seeMore != null) {
                                seeMore.run();
                        }
                });

                HBox buttonContainer = new HBox();
                buttonContainer.setAlignment(Pos.CENTER);
                buttonContainer.setMinHeight(50);

                buttonContainer.getChildren().add(seeMoreButton);

                content.getChildren().addAll(header, itemsContainer, buttonContainer);

                GamerVaultStyles.applyGlassCard(rootCard);
                GamerVaultAnimations.applyHoverTilt(rootCard);

                DropShadow neonGlow = new DropShadow(25, Color.web(buttonColor, 0.25));
                neonGlow.setOffsetY(6);
                rootCard.setEffect(neonGlow);

                rootCard.getChildren().add(content);
                return rootCard;
        }

        static HBox createInteractiveRowItem(String imagePathOrIcon, String title, String uploadTime,
                        String description) {
                HBox row = new HBox(12);
                row.setPadding(new Insets(10));
                row.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 12; -fx-cursor: hand;");

                row.setOnMouseEntered(e -> row.setStyle(
                                "-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 12; -fx-cursor: hand;"));
                row.setOnMouseExited(e -> row.setStyle(
                                "-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 12; -fx-cursor: hand;"));
                GamerVaultAnimations.scaleOnHover(row, 1.02);

                StackPane iconBox = new StackPane();
                iconBox.setPrefSize(70, 50);
                iconBox.setMinSize(70, 50);
                iconBox.setMaxSize(70, 50);
                iconBox.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8;");

                if (imagePathOrIcon.contains("/") || imagePathOrIcon.contains(".")) {
                        ImageView imgView = new ImageView();
                        imgView.setFitWidth(70);
                        imgView.setFitHeight(50);
                        imgView.setPreserveRatio(false);

                        try {
                                if (imagePathOrIcon.startsWith("http")) {
                                        imgView.setImage(new Image(imagePathOrIcon, true));
                                } else if (FeatureCardsMethods.class.getResource(imagePathOrIcon) != null) {
                                        imgView.setImage(new Image(FeatureCardsMethods.class
                                                        .getResource(imagePathOrIcon).toExternalForm(), true));
                                }
                        } catch (Exception e) {
                                System.out.println("Could not load thumbnail: " + imagePathOrIcon);
                        }

                        Rectangle clip = new Rectangle(70, 50);
                        clip.setArcWidth(16);
                        clip.setArcHeight(16);
                        imgView.setClip(clip);

                        iconBox.getChildren().add(imgView);
                } else {
                        Text imageText = new Text(imagePathOrIcon);
                        imageText.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
                        imageText.setFont(Font.font(20));
                        iconBox.getChildren().add(imageText);
                }

                VBox details = new VBox(2);
                details.setAlignment(Pos.CENTER_LEFT);

                Text titleText = new Text(title);
                titleText.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
                titleText.setFont(Font.font("Poppins", FontWeight.BOLD, 14));

                Text subText = new Text(description + " • " + uploadTime);
                subText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                subText.setFont(Font.font("Poppins", FontWeight.NORMAL, 11));

                details.getChildren().addAll(titleText, subText);
                row.getChildren().addAll(iconBox, details);

                return row;
        }
}
