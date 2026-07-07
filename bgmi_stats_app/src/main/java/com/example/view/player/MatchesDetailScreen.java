package com.example.view.player;

import com.example.model.player.MatchExtractionResultModel;
import com.example.model.player.MatchModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class MatchesDetailScreen {

        // VARIABLES

        private final MatchModel summaryMatch;
        private final MatchExtractionResultModel match;

        // UI Containers
        private VBox mainContainer;
        private FlowPane overviewCards;

        // CONSTRUCTOR
        public MatchesDetailScreen(MatchModel summaryMatch, MatchExtractionResultModel match) {
                this.summaryMatch = summaryMatch;
                this.match = match;
        }

        public BorderPane startMatchDetailsScreen(
                        Runnable backNavigation) {

                BorderPane root = new BorderPane();

                root.setStyle("-fx-background-color: transparent;");

                ScrollPane scrollPane = new ScrollPane();

                scrollPane.setFitToWidth(true);

                GamerVaultStyles.applyStyledScrollPane(scrollPane);

                scrollPane.setContent(createScreenContent(backNavigation));

                root.setCenter(scrollPane);

                return root;
        }

        private VBox createScreenContent(
                        Runnable backNavigation) {
                mainContainer = new VBox(25);
                mainContainer.setPadding(new Insets(15, 20, 40, 20));

                Button backButton = new Button("← Back");
                GamerVaultStyles.applyGhostButton(backButton);
                backButton.setOnAction(e -> backNavigation.run());
                VBox header = createHeaderSection();
                overviewCards = new FlowPane();
                overviewCards.setHgap(20);
                overviewCards.setVgap(20);
                overviewCards.getChildren().addAll(

                                createOverviewCard("Kills",
                                                String.valueOf(match.getKills())),
                                createOverviewCard("Damage",
                                                String.valueOf(match.getDamage())),
                                createOverviewCard("Assists",
                                                String.valueOf(match.getAssists())),
                                createOverviewCard("Rating",
                                                String.valueOf(match.getRating())),
                                createOverviewCard("Survival",
                                                match.getSurvivalTime()));
                GamerVaultAnimations.staggerFadeInChildren(
                                overviewCards,
                                100);

                mainContainer.getChildren().addAll(

                                backButton,
                                header,
                                overviewCards,
                                createMatchImagesSection(),
                                createStatisticsSection()

                );
                return mainContainer;
        }

        private VBox createHeaderSection() {
                VBox container = new VBox(12);

                Text title = new Text("MATCH DETAILS");
                title.setFill(Color.web(
                                GamerVaultStyles.TEXT_PRIMARY));
                title.setFont(Font.font(
                                "Poppins",
                                FontWeight.BOLD,
                                40));

                Text subtitle = new Text(
                                "View complete AI extracted match statistics.");
                subtitle.setFill(Color.web(
                                GamerVaultStyles.TEXT_SECONDARY));
                subtitle.setFont(Font.font(
                                "Poppins",
                                FontWeight.NORMAL,
                                16));

                GamerVaultAnimations.fadeInUp(title, 0, 500);
                GamerVaultAnimations.fadeInUp(subtitle, 150, 500);
                HBox hero = createHeroBanner();

                container.getChildren().addAll(
                                title,
                                subtitle,
                                SizedBox.height(10),
                                hero);
                return container;
        }

        private HBox createHeroBanner() {
                HBox hero = new HBox();

                hero.setPadding(new Insets(20));
                hero.setSpacing(20);
                GamerVaultStyles.applyGlassCard(hero);
                VBox left = new VBox(8);

                Text map = new Text(match.getMap());
                map.setFill(Color.web(
                                GamerVaultStyles.TEXT_PRIMARY));
                map.setFont(Font.font(
                                "Arial",
                                FontWeight.BOLD,
                                30));

                Text mode = new Text(match.getGameMode());
                mode.setFill(Color.web(
                                GamerVaultStyles.ACCENT_CYAN));

                Text date = new Text(match.getMatchDate());
                date.setFill(Color.web(
                                GamerVaultStyles.TEXT_SECONDARY));

                left.getChildren().addAll(
                                map,
                                mode,
                                date);
                Region spacer = new Region();
                HBox.setHgrow(spacer,
                                Priority.ALWAYS);

                VBox right = new VBox(8);
                right.setAlignment(Pos.CENTER_RIGHT);
                Text placement = new Text(
                                "#" + match.getTeamPlacement());
                placement.setFont(Font.font(
                                "Arial",
                                FontWeight.BOLD,
                                42));
                placement.setFill(Color.web("#F59E0B"));

                Text result = new Text(
                                match.getMatchResult());
                result.setFill(Color.web(
                                GamerVaultStyles.TEXT_PRIMARY));
                right.getChildren().addAll(
                                placement,
                                result);

                hero.getChildren().addAll(
                                left,
                                spacer,
                                right);
                return hero;
        }

        private VBox createOverviewCard(
                        String title,
                        String value) {

                VBox card = new VBox(10);
                card.setPrefSize(180, 110);
                card.setPadding(new Insets(18));

                GamerVaultStyles.applyGlassCard(card);
                GamerVaultAnimations.scaleOnHover(card, 1.03);

                Text t = new Text(title);

                t.setFill(Color.web(
                                GamerVaultStyles.TEXT_SECONDARY));

                t.setFont(Font.font(
                                "Arial",
                                FontWeight.BOLD,
                                14));

                Text v = new Text(value);
                v.setFill(Color.web(
                                GamerVaultStyles.TEXT_PRIMARY));

                v.setFont(Font.font(
                                "Arial",
                                FontWeight.BOLD,
                                30));
                card.getChildren().addAll(
                                t,
                                v);
                return card;
        }

        private VBox createStatisticsSection() {
                VBox container = new VBox(25);

                container.getChildren().addAll(

                                createStatGroup(
                                                "🟣 Survival",
                                                createStatRow("Survival Score",
                                                                String.valueOf(match.getSurvivalScore())),
                                                createStatRow("Travel Distance",
                                                                match.getTravelDistance() + " km")),

                                createStatGroup(
                                                "🔵 Support",
                                                createStatRow("Support Score",
                                                                String.valueOf(match.getSupportScore())),
                                                createStatRow("Health Restored",
                                                                String.valueOf(match.getHealthRestored())),
                                                createStatRow("Rescues",
                                                                String.valueOf(match.getRescues()))),

                                createStatGroup(
                                                "🟢 Supplies",
                                                createStatRow("Supplies Score",
                                                                String.valueOf(match.getSuppliesScore())),
                                                createStatRow("Total Supplies",
                                                                String.valueOf(match.getTotalSupplies())),
                                                createStatRow("Advanced Supplies",
                                                                String.valueOf(match.getAdvancedSupplies()))),

                                createWeaponSection());
                return container;
        }

        private VBox createStatGroup(
                        String title,
                        HBox... rows) {
                VBox card = new VBox(15);
                card.setPadding(new Insets(20));
                GamerVaultStyles.applyGlassCard(card);
                Text heading = new Text(title);

                heading.setFont(Font.font(
                                "Arial",
                                FontWeight.BOLD,
                                20));
                heading.setFill(Color.web(
                                GamerVaultStyles.TEXT_PRIMARY));

                card.getChildren().add(heading);
                card.getChildren().addAll(rows);
                return card;
        }

        private HBox createStatRow(
                        String label,
                        String value) {
                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_LEFT);
                Text left = new Text(label);
                left.setFill(Color.web(
                                GamerVaultStyles.TEXT_SECONDARY));

                left.setFont(Font.font(
                                "Arial",
                                FontWeight.NORMAL,
                                15));
                Region spacer = new Region();

                HBox.setHgrow(
                                spacer,
                                Priority.ALWAYS);
                Text right = new Text(value);
                right.setFill(Color.web(
                                GamerVaultStyles.TEXT_PRIMARY));

                right.setFont(Font.font(
                                "Arial",
                                FontWeight.BOLD,
                                15));

                row.getChildren().addAll(
                                left,
                                spacer,
                                right);

                return row;
        }

        private VBox createWeaponSection() {

                VBox card = new VBox(15);
                card.setPadding(new Insets(20));
                GamerVaultStyles.applyGlassCard(card);
                Text title = new Text("🟠 Weapon Statistics");

                title.setFont(Font.font(
                                "Arial",
                                FontWeight.BOLD,
                                20));
                title.setFill(Color.web(
                                GamerVaultStyles.TEXT_PRIMARY));

                card.getChildren().addAll(
                                title,

                                createStatRow(
                                                "Weapon",
                                                match.getWeaponName()),

                                createStatRow(
                                                "Weapon Type",
                                                match.getWeaponType()),

                                createStatRow(
                                                "Weapon Damage",
                                                String.valueOf(match.getWeaponDamage())),

                                createStatRow(
                                                "Eliminations",
                                                String.valueOf(match.getWeaponEliminations())),

                                createStatRow(
                                                "Knockdowns",
                                                String.valueOf(match.getWeaponKnockdowns())),

                                createStatRow(
                                                "Uses",
                                                String.valueOf(match.getWeaponUses())));
                return card;
        }

        private VBox createMatchImagesSection() {
                VBox card = new VBox(15);
                card.setPadding(new Insets(20));
                GamerVaultStyles.applyGlassCard(card);

                Text title = new Text("📸 Match Evidence & Screenshots");
                title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
                title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

                // Horizontal Gallery Container
                HBox imageGallery = new HBox(15);
                imageGallery.setAlignment(Pos.CENTER_LEFT);

                // Make it scrollable horizontally
                ScrollPane scroller = new ScrollPane(imageGallery);
                GamerVaultStyles.applyStyledScrollPane(scroller);
                scroller.setFitToHeight(true);
                scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scroller.setMinHeight(220);

                java.util.List<String> imageUrls = (summaryMatch != null) ? summaryMatch.getImageUrls() : null;

                if (imageUrls == null || imageUrls.isEmpty()) {
                        Text noImages = new Text("No screenshots uploaded for this match.");
                        noImages.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                        imageGallery.getChildren().add(noImages);
                } else {
                        for (String url : imageUrls) {

                                // The 'true' parameter enables background loading so the UI doesn't freeze!
                                javafx.scene.image.Image img = new javafx.scene.image.Image(url, true);
                                javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
                                imgView.setFitHeight(190);
                                imgView.setPreserveRatio(true);

                                // Wrap in a StackPane for styling, borders, and hover animations
                                javafx.scene.layout.StackPane imgWrapper = new javafx.scene.layout.StackPane(imgView);
                                imgWrapper.setStyle(
                                                "-fx-background-color: #0A0F18; -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 12;");
                                imgWrapper.setPadding(new Insets(5));

                                // Hover effect
                                GamerVaultAnimations.scaleOnHover(imgWrapper, 1.05);
                                imgWrapper.setStyle(imgWrapper.getStyle() + " -fx-cursor: hand;");

                                // Click to view full screen
                                imgWrapper.setOnMouseClicked(e -> showFullScreenImage(url));

                                imageGallery.getChildren().add(imgWrapper);
                        }
                }

                card.getChildren().addAll(title, scroller);
                return card;
        }

        private void showFullScreenImage(String url) {
                javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
                dialog.getDialogPane().setStyle(
                                "-fx-background-color: rgba(11, 15, 25, 0.95); -fx-border-color: #8B5CF6; -fx-border-width: 2; -fx-border-radius: 8;");

                javafx.scene.image.ImageView fullImg = new javafx.scene.image.ImageView(
                                new javafx.scene.image.Image(url, true));
                fullImg.setPreserveRatio(true);

                // Restrict max size so it fits on most screens
                fullImg.setFitWidth(900);
                fullImg.setFitHeight(700);

                javafx.scene.layout.StackPane pane = new javafx.scene.layout.StackPane(fullImg);
                pane.setPadding(new Insets(20));

                dialog.getDialogPane().setContent(pane);
                dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);

                // Style the close button to match our theme
                javafx.scene.Node closeBtn = dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.CLOSE);
                if (closeBtn instanceof javafx.scene.control.Button) {
                        GamerVaultStyles.applyGhostButton((javafx.scene.control.Button) closeBtn);
                        closeBtn.setStyle(closeBtn.getStyle() + "-fx-text-fill: white; -fx-font-weight: bold;");
                }

                dialog.showAndWait();
        }

}