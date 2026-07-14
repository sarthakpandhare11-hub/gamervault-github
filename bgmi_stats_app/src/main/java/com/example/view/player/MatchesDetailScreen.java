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
                                                String.valueOf(match.getKills()), "🔫", "#EF4444"),
                                createOverviewCard("Damage",
                                                String.valueOf(match.getDamage()), "💥", "#F59E0B"),
                                createOverviewCard("Assists",
                                                String.valueOf(match.getAssists()), "🤝", GamerVaultStyles.ACCENT_CYAN),
                                createOverviewCard("Rating",
                                                String.valueOf(match.getRating()), "⭐",
                                                GamerVaultStyles.ACCENT_PURPLE_LIGHT),
                                createOverviewCard("Survival",
                                                match.getSurvivalTime(), "⏱", GamerVaultStyles.ACCENT_GREEN));
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

                hero.setPadding(new Insets(25));
                hero.setSpacing(20);
                hero.setStyle(
                                "-fx-background-color: " + GamerVaultStyles.CARD_BG + "; -fx-background-radius: 16; "
                                                + "-fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 16;");

                // Map background image - same asset lookup pattern already used in
                // MatchHistoryScreen's match cards, kept visually consistent across screens.
                javafx.scene.image.ImageView mapBg = new javafx.scene.image.ImageView();
                mapBg.setOpacity(0.35);
                mapBg.setPreserveRatio(false);
                try {
                        String sanitizedMapName = match.getMap() != null ? match.getMap().toLowerCase().trim()
                                        : "erangel";
                        String path = "/assets/bgmi_images/" + sanitizedMapName + "_bg.jpg";
                        if (getClass().getResource(path) != null) {
                                mapBg.setImage(new javafx.scene.image.Image(
                                                getClass().getResource(path).toExternalForm(), true));
                        } else {
                                mapBg.setImage(new javafx.scene.image.Image(
                                                getClass().getResource("/assets/bgmi_images/erangel_bg.jpg")
                                                                .toExternalForm(),
                                                true));
                        }
                } catch (Exception e) {
                        System.out.println(e);
                }

                javafx.scene.layout.StackPane heroStack = new javafx.scene.layout.StackPane();
                heroStack.setStyle("-fx-background-radius: 16;");

                VBox left = new VBox(8);

                HBox mapRow = new HBox(10);
                mapRow.setAlignment(Pos.CENTER_LEFT);

                Text map = new Text(match.getMap());
                map.setFill(Color.web(
                                GamerVaultStyles.TEXT_PRIMARY));
                map.setFont(Font.font(
                                "Arial",
                                FontWeight.BOLD,
                                30));
                mapRow.getChildren().add(map);

                // MVP crown - match.isMvp() already exists on the model but was never
                // surfaced anywhere in this screen before.
                if (match.isMvp()) {
                        HBox mvpBadge = new HBox(4);
                        mvpBadge.setAlignment(Pos.CENTER);
                        mvpBadge.setPadding(new Insets(4, 10, 4, 10));
                        mvpBadge.setStyle(
                                        "-fx-background-color: rgba(245,158,11,0.15); -fx-background-radius: 20; -fx-border-color: #F59E0B; -fx-border-radius: 20;");
                        Text crown = new Text("👑 MVP");
                        crown.setFill(Color.web("#F59E0B"));
                        crown.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                        mvpBadge.getChildren().add(crown);
                        mapRow.getChildren().add(mvpBadge);
                }

                HBox contextRow = new HBox(8);
                contextRow.setAlignment(Pos.CENTER_LEFT);
                Text mode = new Text(match.getGameMode());
                mode.setFill(Color.web(
                                GamerVaultStyles.ACCENT_CYAN));
                contextRow.getChildren().add(mode);
                if (match.getPerspective() != null && !match.getPerspective().isEmpty()) {
                        Text dot1 = new Text("·");
                        dot1.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                        Text perspective = new Text(match.getPerspective());
                        perspective.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
                        contextRow.getChildren().addAll(dot1, perspective);
                }
                if (match.getTotalPlayers() > 0) {
                        Text dot2 = new Text("·");
                        dot2.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                        Text players = new Text(match.getTotalPlayers() + " Players");
                        players.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
                        contextRow.getChildren().addAll(dot2, players);
                }

                Text date = new Text(match.getMatchDate());
                date.setFill(Color.web(
                                GamerVaultStyles.TEXT_SECONDARY));

                // AI trust indicator - confidenceScore/extractionSuccessful/warnings all
                // already exist on the model and were never shown before either.
                HBox trustBadge = buildTrustBadge();

                left.getChildren().addAll(
                                mapRow,
                                contextRow,
                                date,
                                trustBadge);
                Region spacer = new Region();
                HBox.setHgrow(spacer,
                                Priority.ALWAYS);

                VBox right = new VBox(4);
                right.setAlignment(Pos.CENTER_RIGHT);
                Text placement = new Text(
                                "#" + match.getTeamPlacement());
                placement.setFont(Font.font(
                                "Arial",
                                FontWeight.BOLD,
                                48));
                placement.setFill(Color.web("#F59E0B"));
                javafx.scene.effect.DropShadow placementGlow = new javafx.scene.effect.DropShadow(15,
                                Color.web("#F59E0B", 0.5));
                placement.setEffect(placementGlow);

                boolean isVictory = match.getMatchResult() != null
                                && (match.getMatchResult().toLowerCase().contains("winner")
                                                || match.getMatchResult().toLowerCase().contains("chicken"));

                Text result = new Text(
                                match.getMatchResult());
                result.setFill(Color.web(
                                isVictory ? "#FBBF24" : GamerVaultStyles.TEXT_SECONDARY));
                result.setFont(Font.font("Arial", isVictory ? FontWeight.BOLD : FontWeight.NORMAL, 16));
                right.getChildren().addAll(
                                placement,
                                result);

                hero.getChildren().addAll(
                                left,
                                spacer,
                                right);

                heroStack.getChildren().addAll(mapBg, hero);
                HBox wrapper = new HBox(heroStack);
                HBox.setHgrow(heroStack, Priority.ALWAYS);
                return wrapper;
        }

        /*
         * Small trust indicator built from fields that already exist on
         * MatchExtractionResultModel (confidenceScore, extractionSuccessful,
         * warnings) but weren't displayed anywhere before. Keeps the AI's own
         * transparency signal visible to the player instead of hidden in the data.
         */
        private HBox buildTrustBadge() {
                HBox badge = new HBox(6);
                badge.setAlignment(Pos.CENTER_LEFT);
                badge.setPadding(new Insets(4, 10, 4, 10));

                boolean hasWarnings = match.getWarnings() != null && !match.getWarnings().isEmpty();
                boolean lowConfidence = match.getConfidenceScore() > 0 && match.getConfidenceScore() < 0.75;
                boolean flagged = hasWarnings || lowConfidence || !match.isExtractionSuccessful();

                String color = flagged ? "#F59E0B" : "#10B981";
                badge.setStyle(
                                "-fx-background-color: " + color + "1A; -fx-background-radius: 8; -fx-border-color: "
                                                + color
                                                + "55; -fx-border-radius: 8;");

                String label;
                if (flagged) {
                        label = "⚠ Review Recommended" + (hasWarnings ? " (" + match.getWarnings().size() + " flag"
                                        + (match.getWarnings().size() > 1 ? "s" : "") + ")" : "");
                } else {
                        label = "✓ AI Verified"
                                        + (match.getConfidenceScore() > 0
                                                        ? " · " + Math.round(match.getConfidenceScore() * 100)
                                                                        + "% confidence"
                                                        : "");
                }
                Text text = new Text(label);
                text.setFill(Color.web(color));
                text.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                badge.getChildren().add(text);

                if (flagged && hasWarnings) {
                        javafx.scene.control.Tooltip tip = new javafx.scene.control.Tooltip(
                                        String.join("\n", match.getWarnings()));
                        javafx.scene.control.Tooltip.install(badge, tip);
                }
                return badge;
        }

        private VBox createOverviewCard(
                        String title,
                        String value,
                        String icon,
                        String accentColor) {

                VBox card = new VBox(10);
                card.setPrefSize(180, 115);
                card.setPadding(new Insets(16));

                card.setStyle(
                                "-fx-background-color: " + GamerVaultStyles.CARD_BG + "; -fx-background-radius: 14; "
                                                + "-fx-border-color: " + GamerVaultStyles.CARD_BORDER + " "
                                                + GamerVaultStyles.CARD_BORDER + " " + GamerVaultStyles.CARD_BORDER
                                                + " " + accentColor
                                                + "; -fx-border-width: 1 1 1 4; -fx-border-radius: 14;");
                GamerVaultAnimations.scaleOnHover(card, 1.04);

                HBox topRow = new HBox(8);
                topRow.setAlignment(Pos.CENTER_LEFT);

                javafx.scene.layout.StackPane iconBadge = new javafx.scene.layout.StackPane();
                iconBadge.setPrefSize(28, 28);
                iconBadge.setStyle("-fx-background-color: " + accentColor + "22; -fx-background-radius: 8;");
                Text iconText = new Text(icon);
                iconText.setFont(Font.font(13));
                iconBadge.getChildren().add(iconText);

                Text t = new Text(title);
                t.setFill(Color.web(
                                GamerVaultStyles.TEXT_SECONDARY));
                t.setFont(Font.font(
                                "Arial",
                                FontWeight.BOLD,
                                13));
                topRow.getChildren().addAll(iconBadge, t);

                Text v = new Text(value);
                v.setFill(Color.web(accentColor));

                v.setFont(Font.font(
                                "Arial",
                                FontWeight.BOLD,
                                28));
                card.getChildren().addAll(
                                topRow,
                                v);
                animateCountUpText(v, value);
                return card;
        }

        /*
         * Same defensive count-up pattern used elsewhere - a plain number or a
         * decimal animates from 0 to the final value; anything not cleanly
         * parseable is left exactly as constructed, so the settled value is
         * always identical to a static label.
         */
        private void animateCountUpText(Text node, String finalText) {
                try {
                        String trimmed = finalText.trim();
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(-?\\d+(?:\\.\\d+)?)(.*)$")
                                        .matcher(trimmed);
                        if (!m.matches())
                                return;

                        double target = Double.parseDouble(m.group(1));
                        String suffix = m.group(2);
                        boolean isWhole = target == Math.floor(target) && !m.group(1).contains(".");

                        node.setText((isWhole ? "0" : "0.0") + suffix);

                        javafx.animation.Timeline countUp = new javafx.animation.Timeline();
                        int steps = 24;
                        for (int i = 1; i <= steps; i++) {
                                double progress = i / (double) steps;
                                double value = target * progress;
                                String frameText = (isWhole ? String.valueOf(Math.round(value))
                                                : String.format("%.1f", value)) + suffix;
                                countUp.getKeyFrames().add(new javafx.animation.KeyFrame(
                                                javafx.util.Duration.millis(600 * progress),
                                                e -> node.setText(frameText)));
                        }
                        countUp.getKeyFrames().add(new javafx.animation.KeyFrame(javafx.util.Duration.millis(620),
                                        e -> node.setText(finalText)));
                        countUp.play();
                } catch (Exception ignored) {
                        // Leave the node showing its original constructor-set text.
                }
        }

        private VBox createStatisticsSection() {
                VBox container = new VBox(25);

                container.getChildren().addAll(

                                createStatGroup(
                                                "🟣 Survival",
                                                createStatGauge("Survival Score", match.getSurvivalScore(), 100,
                                                                "#8B5CF6"),
                                                createStatRow("Travel Distance",
                                                                match.getTravelDistance() + " km")),

                                createStatGroup(
                                                "🔵 Support",
                                                createStatGauge("Support Score", match.getSupportScore(), 100,
                                                                GamerVaultStyles.ACCENT_CYAN),
                                                createStatRow("Health Restored",
                                                                String.valueOf(match.getHealthRestored())),
                                                createStatRow("Rescues",
                                                                String.valueOf(match.getRescues()))),

                                createStatGroup(
                                                "🟢 Supplies",
                                                createStatGauge("Supplies Score", match.getSuppliesScore(), 100,
                                                                GamerVaultStyles.ACCENT_GREEN),
                                                createStatRow("Total Supplies",
                                                                String.valueOf(match.getTotalSupplies())),
                                                createStatRow("Advanced Supplies",
                                                                String.valueOf(match.getAdvancedSupplies()))),

                                createWeaponSection());
                return container;
        }

        /*
         * Visual gauge for a 0-max scored dimension: label + numeric value above
         * a filled progress track, animated to its final width. Used only for
         * the *Score fields, which are genuinely 0-100 scores - the raw counts
         * (Rescues, Total Supplies, etc.) stay as plain rows via createStatRow,
         * since a progress bar wouldn't mean anything for an open-ended count.
         */
        private VBox createStatGauge(String label, double value, double max, String color) {
                VBox box = new VBox(6);

                HBox labelRow = new HBox();
                labelRow.setAlignment(Pos.CENTER_LEFT);
                Text left = new Text(label);
                left.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
                left.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Text right = new Text(String.format("%.1f", value));
                right.setFill(Color.web(color));
                right.setFont(Font.font("Arial", FontWeight.BOLD, 15));
                labelRow.getChildren().addAll(left, spacer, right);

                javafx.scene.layout.StackPane track = new javafx.scene.layout.StackPane();
                track.setPrefHeight(8);
                track.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 6;");

                Region fill = new Region();
                fill.setPrefHeight(8);
                fill.setMaxWidth(0);
                fill.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6;");
                javafx.scene.layout.StackPane.setAlignment(fill, Pos.CENTER_LEFT);
                track.getChildren().add(fill);
                track.setAlignment(Pos.CENTER_LEFT);

                box.getChildren().addAll(labelRow, track);

                double ratio = max > 0 ? Math.max(0, Math.min(1.0, value / max)) : 0;
                // Widen the fill to its target ratio once the track has been laid out and
                // has a real width to measure against.
                track.widthProperty().addListener((obs, oldW, newW) -> {
                        if (newW.doubleValue() > 0 && fill.getMaxWidth() == 0) {
                                javafx.animation.Timeline grow = new javafx.animation.Timeline(
                                                new javafx.animation.KeyFrame(javafx.util.Duration.ZERO,
                                                                new javafx.animation.KeyValue(fill.maxWidthProperty(),
                                                                                0)),
                                                new javafx.animation.KeyFrame(javafx.util.Duration.millis(700),
                                                                new javafx.animation.KeyValue(fill.maxWidthProperty(),
                                                                                newW.doubleValue() * ratio,
                                                                                javafx.animation.Interpolator.EASE_OUT)));
                                grow.play();
                        }
                });

                return box;
        }

        private VBox createStatGroup(
                        String title,
                        javafx.scene.Node... rows) {
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

                VBox card = new VBox(18);
                card.setPadding(new Insets(20));
                GamerVaultStyles.applyGlassCard(card);

                HBox headerRow = new HBox(12);
                headerRow.setAlignment(Pos.CENTER_LEFT);

                javafx.scene.layout.StackPane weaponIcon = new javafx.scene.layout.StackPane();
                weaponIcon.setPrefSize(48, 48);
                weaponIcon.setStyle(
                                "-fx-background-color: rgba(245,158,11,0.15); -fx-background-radius: 12; -fx-border-color: rgba(245,158,11,0.4); -fx-border-radius: 12;");
                Text gunEmoji = new Text("🔫");
                gunEmoji.setFont(Font.font(20));
                weaponIcon.getChildren().add(gunEmoji);

                VBox nameBlock = new VBox(2);
                Text weaponName = new Text(match.getWeaponName() != null ? match.getWeaponName() : "Unknown Weapon");
                weaponName.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
                weaponName.setFont(Font.font("Arial", FontWeight.BOLD, 20));
                Text weaponType = new Text(
                                match.getWeaponType() != null ? match.getWeaponType().toUpperCase() : "PRIMARY");
                weaponType.setFill(Color.web("#F59E0B"));
                weaponType.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                nameBlock.getChildren().addAll(weaponName, weaponType);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                // Hero stat - Eliminations, the single number that matters most about a
                // weapon's performance, styled big rather than buried in a text row.
                VBox eliminationsHero = new VBox(-4);
                eliminationsHero.setAlignment(Pos.CENTER_RIGHT);
                Text elimValue = new Text(String.valueOf(match.getWeaponEliminations()));
                elimValue.setFill(Color.web("#EF4444"));
                elimValue.setFont(Font.font("Arial", FontWeight.BOLD, 36));
                Text elimLabel = new Text("ELIMINATIONS");
                elimLabel.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                elimLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                eliminationsHero.getChildren().addAll(elimValue, elimLabel);
                animateCountUpText(elimValue, String.valueOf(match.getWeaponEliminations()));

                headerRow.getChildren().addAll(weaponIcon, nameBlock, spacer, eliminationsHero);

                // Secondary stats mini-grid, mirroring the Damage / Knockouts / Uses
                // layout the in-game Weapon Data panel itself uses.
                HBox miniGrid = new HBox();
                miniGrid.setStyle(
                                "-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 10; -fx-padding: 14; "
                                                + "-fx-border-color: rgba(255,255,255,0.05); -fx-border-radius: 10;");

                Region g1 = new Region();
                HBox.setHgrow(g1, Priority.ALWAYS);
                Region g2 = new Region();
                HBox.setHgrow(g2, Priority.ALWAYS);
                Region g3 = new Region();
                HBox.setHgrow(g3, Priority.ALWAYS);

                miniGrid.getChildren().addAll(
                                createWeaponMiniStat("DAMAGE", String.format("%.0f", match.getWeaponDamage())),
                                g1,
                                createWeaponMiniStat("KNOCKDOWNS", String.valueOf(match.getWeaponKnockdowns())),
                                g2,
                                createWeaponMiniStat("USES", String.valueOf(match.getWeaponUses())),
                                g3,
                                createWeaponMiniStat("THROWS",
                                                (match.getCloseRangeThrows() + match.getLongRangeThrows()) + ""));

                card.getChildren().addAll(headerRow, miniGrid);
                return card;
        }

        private VBox createWeaponMiniStat(String label, String value) {
                VBox col = new VBox(4);
                col.setAlignment(Pos.CENTER);
                Text l = new Text(label);
                l.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                l.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                Text v = new Text(value);
                v.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
                v.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                col.getChildren().addAll(l, v);
                animateCountUpText(v, value);
                return col;
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