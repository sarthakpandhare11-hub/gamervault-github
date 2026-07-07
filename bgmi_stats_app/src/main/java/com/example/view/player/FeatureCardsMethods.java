package com.example.view.player;

import java.util.List;

import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
        static VBox createDashboardStatCard(
                        String titleText,
                        String iconText,
                        String mainStatValueText,
                        String subValueText,
                        List<String> row1StatBox1List,
                        List<String> row1StatBox2List,
                        List<String> row2StatBox1List,
                        List<String> row2StatBox2List) {

                VBox card = new VBox(18);
                card.setPrefSize(340, 220);
                card.setPadding(new Insets(20));

                // Apply glassmorphism with scale-on-hover
                GamerVaultStyles.applyGlassCard(card);
                GamerVaultAnimations.scaleOnHover(card, 1.03);

                // Header
                HBox header = new HBox();

                /*
                 * Used as -
                 * Title Text -> LAST MATCH / PERFORMANCE / LEADERBOARD
                 */
                Text mainTitleText = new Text(titleText);
                mainTitleText.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
                mainTitleText.setFont(Font.font("Poppins", FontWeight.BOLD, 24));

                // SPACER BETWEEN TITLE TEXT AND ICON TEXT
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                // Icon Text -> ➜ / 📈 / 🏆 depending on the type of card
                Text mainIconText = new Text(iconText);
                mainIconText.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
                mainIconText.setFont(Font.font("Poppins", FontWeight.BOLD, 18));

                header.getChildren().addAll(
                                mainTitleText,
                                spacer,
                                mainIconText);

                /*
                 * VALUES BETWEEN
                 * LAST MATCH - mainTitleText
                 * 127 - mainValueText
                 * Last Match Map & Mode - subValueText
                 * 
                 * PERFORMANCE - mainTitleText
                 * +12% - mainValueText
                 * Performance Improved - subValueText
                 * 
                 * LEADERBOARD - mainTitleText
                 * #1 - mainValueText
                 * Current Position - subValueText
                 */
                Text mainValueText = new Text(mainStatValueText);
                mainValueText.setFill(Color.web(GamerVaultStyles.ACCENT_ORANGE));
                mainValueText.setFont(Font.font("Poppins", FontWeight.BOLD, 32));

                Text mainSubText = new Text(subValueText);
                mainSubText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                mainSubText.setFont(Font.font("Poppins", FontWeight.NORMAL, 14));

                // Stats Row 1
                HBox row1 = new HBox(40);

                VBox mainRow1StatBox1 = dashboardCardStatBox(row1StatBox1List.get(0), row1StatBox1List.get(1),
                                row1StatBox1List.get(2));
                Region spacerRegion1 = new Region();
                HBox.setHgrow(spacerRegion1, Priority.ALWAYS);
                VBox mainRow1StatBox2 = dashboardCardStatBox(row1StatBox2List.get(0), row1StatBox2List.get(1),
                                row1StatBox2List.get(2));

                row1.getChildren().addAll(
                                SizedBox.width(7),
                                mainRow1StatBox1,
                                spacerRegion1,
                                mainRow1StatBox2,
                                SizedBox.width(7));

                // Stats Row 2

                HBox row2 = new HBox(40);

                VBox mainRow2StatBox1 = dashboardCardStatBox(row2StatBox1List.get(0), row2StatBox1List.get(1),
                                row2StatBox1List.get(2));
                Region spacerRegion2 = new Region();
                HBox.setHgrow(spacerRegion2, Priority.ALWAYS);
                VBox mainRow2StatBox2 = dashboardCardStatBox(row2StatBox2List.get(0), row2StatBox2List.get(1),
                                row2StatBox2List.get(2));

                row2.getChildren().addAll(
                                SizedBox.width(7),
                                mainRow2StatBox1,
                                spacerRegion2,
                                mainRow2StatBox2,
                                SizedBox.width(7));

                card.getChildren().addAll(
                                header,
                                mainValueText,
                                mainSubText,
                                row1,
                                SizedBox.height(5),
                                row2);

                return card;
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

                box.getChildren().addAll(
                                iconText,
                                valueText,
                                titleText);

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
        static VBox createDashboardContentCard(
                        String titleText,
                        String iconText,
                        List<String> content1List,
                        List<String> content2List,
                        String buttonColor,
                        Runnable onSeeMoreClick) {

                VBox card = new VBox(18);
                card.setPrefSize(370, 260);
                card.setPadding(new Insets(20));

                // Apply interactive glass card with colored hover glow
                GamerVaultStyles.applyInteractiveGlassCard(card, buttonColor);
                // Note: scaleOnHover is not added here because applyInteractiveGlassCard
                // already sets onMouseEntered/Exited

                // HEADER

                HBox header = new HBox();

                Text mainTitleText = new Text(titleText);
                mainTitleText.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
                mainTitleText.setFont(Font.font("Poppins", FontWeight.BOLD, 24));

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Text mainIconText = new Text(iconText);
                mainIconText.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
                mainIconText.setFont(Font.font("Poppins", FontWeight.BOLD, 18));

                header.getChildren().addAll(
                                mainTitleText,
                                spacer,
                                mainIconText);

                VBox recruitmentContainer = new VBox(12);

                recruitmentContainer.getChildren().addAll(

                                createContentItem(
                                                content1List.get(0),
                                                content1List.get(1),
                                                content1List.get(2),
                                                content1List.get(3)),

                                createContentItem(
                                                content2List.get(0),
                                                content2List.get(1),
                                                content2List.get(2),
                                                content2List.get(3)));

                Button seeMoreButton = new Button("See More");
                seeMoreButton.setPrefWidth(120);
                seeMoreButton.setPrefHeight(35);
                GamerVaultStyles.applyGradientButton(seeMoreButton, buttonColor, buttonColor, "white");

                seeMoreButton.setOnAction(e -> {
                        if (onSeeMoreClick != null)
                                onSeeMoreClick.run();
                });

                HBox buttonContainer = new HBox();
                buttonContainer.setAlignment(Pos.CENTER);
                buttonContainer.getChildren().add(seeMoreButton);

                card.getChildren().addAll(
                                header,
                                recruitmentContainer,
                                buttonContainer);

                return card;
        }

        /*
         * This method is called from createDashboardContentCard method to create
         * the content boxes.
         * 
         * - Data send through the list content to arrange is,
         * 1) imageURL/ Icon of the content
         * 2) title of the content
         * 3) time Uploaded of the content
         * 4) description of the content
         * 
         */
        static HBox createContentItem(
                        String image,
                        String title,
                        String uploadTime,
                        String description) {

                HBox root = new HBox(12);

                StackPane imagePlaceholder = new StackPane();
                imagePlaceholder.setPrefSize(70, 50);

                imagePlaceholder.setStyle(
                                "-fx-background-color: rgba(255,255,255,0.04);" +
                                                "-fx-background-radius: 10; "
                                                + "-fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 10;");

                Text imageText = new Text(image);
                imageText.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
                imageText.setFont(Font.font(20));

                imagePlaceholder.getChildren().add(imageText);

                // DETAILS

                VBox details = new VBox(4);

                Text titleText = new Text(title);
                titleText.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
                titleText.setFont(Font.font("Poppins", FontWeight.BOLD, 15));

                Text uploadTimeText = new Text(uploadTime);
                uploadTimeText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                uploadTimeText.setFont(Font.font("Poppins", FontWeight.NORMAL, 12));

                Text descriptionText = new Text(description);
                descriptionText.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
                descriptionText.setFont(Font.font("Poppins", FontWeight.NORMAL, 13));

                details.getChildren().addAll(
                                titleText,
                                uploadTimeText,
                                descriptionText);

                root.getChildren().addAll(
                                imagePlaceholder,
                                details);

                return root;
        }
}
