package com.example.view.admin;

import java.util.List;

import com.example.controller.admin.AdminController;
import com.example.controller.admin.ContentController;
import com.example.controller.admin.NotificationController;
import com.example.controller.admin.TournamentController;
import com.example.controller.player.ProfileController;
import com.example.controller.player.RecruitmentController;
import com.example.model.NotificationModel;
import com.example.model.UserModel;
import com.example.model.admin.ContentModel;
import com.example.model.admin.TournamentModel;
import com.example.model.player.RecruitmentModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
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

public class AdminDashboardScreen {

    // --- DYNAMIC UI CONTAINERS & TRACKERS ---
    private Text totalPlayersText;
    private Text totalMatchesText;
    private Text totalContentText;
    private Text totalRecruitmentText;
    private Text totalTournamentsText;

    private VBox recentActivityList;
    private VBox contentSnapshotList;

    private StackPane userChartContainer;
    private StackPane contentChartContainer;
    private StackPane tourneyChartContainer;

    // NAVIGATION HELPER
    private AdminMainScreen mainScreen;

    public BorderPane startAdminDashboardScreen(AdminMainScreen mainScreen) {
        this.mainScreen = mainScreen;

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        ScrollPane scroller = new ScrollPane();
        scroller.setContent(createDashboardContent());
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setFitToWidth(true);
        scroller.setVbarPolicy(ScrollBarPolicy.NEVER);

        root.setCenter(scroller);

        // Fetch all platform data in the background
        loadDashboardData();

        return root;
    }

    private VBox createDashboardContent() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(10, 20, 40, 0));

        // 1. HEADER SECTION
        VBox headerBox = createHeaderBox();

        // 2. TOP METRICS ROW
        HBox metricsRow = createMetricsRow();

        // 3. MIDDLE ROW (Recent Activity & Quick Actions)
        HBox middleRow = new HBox(20);
        VBox recentActivity = createRecentActivitySection();
        VBox quickActions = createQuickActionsSection();
        HBox.setHgrow(recentActivity, Priority.ALWAYS);
        quickActions.setPrefWidth(320);
        middleRow.getChildren().addAll(recentActivity, quickActions);

        GamerVaultAnimations.fadeInUp(middleRow, 300, 500);

        // 4. BOTTOM ROW (Platform Overview & Content Snapshot)
        HBox bottomRow = new HBox(20);
        VBox platformOverview = createPlatformOverviewSection();
        VBox contentSnapshot = createContentSnapshotSection();
        HBox.setHgrow(platformOverview, Priority.ALWAYS);
        contentSnapshot.setPrefWidth(320);
        bottomRow.getChildren().addAll(platformOverview, contentSnapshot);

        GamerVaultAnimations.fadeInUp(bottomRow, 450, 500);

        container.getChildren().addAll(headerBox, metricsRow, middleRow, bottomRow);
        return container;
    }

    private VBox createHeaderBox() {
        Text title = new Text("Admin Dashboard");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 36));
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

        Text subtitle = new Text("Manage GamerVault operations and monitor platform activity");
        subtitle.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        subtitle.setFont(Font.font("Poppins", FontWeight.NORMAL, 15));

        VBox headerBox = new VBox(5);
        headerBox.getChildren().addAll(title, subtitle);

        GamerVaultAnimations.fadeInUp(title, 0, 500);
        GamerVaultAnimations.fadeInUp(subtitle, 100, 500);

        return headerBox;
    }

    private HBox createMetricsRow() {
        HBox row = new HBox(15);

        totalPlayersText = new Text("--");
        totalMatchesText = new Text("--");
        totalContentText = new Text("--");
        totalRecruitmentText = new Text("--");
        totalTournamentsText = new Text("--");

        row.getChildren().addAll(
                createMetricCard("TOTAL PLAYERS", "👥", totalPlayersText),
                createMetricCard("MATCHES UPLOADED", "🎮", totalMatchesText),
                createMetricCard("CONTENT POSTS", "🎥", totalContentText),
                createMetricCard("RECRUITMENT", "👤", totalRecruitmentText),
                createMetricCard("TOURNAMENTS", "🏆", totalTournamentsText));

        for (Node n : row.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS);
        }

        GamerVaultAnimations.staggerFadeInChildren(row, 80);
        return row;
    }

    private VBox createMetricCard(String titleStr, String iconStr, Text valueNode) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        GamerVaultStyles.applyGlassCard(card);
        GamerVaultAnimations.scaleOnHover(card, 1.03);

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text(titleStr);
        title.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 11));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Text icon = new Text(iconStr);
        icon.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        icon.setFont(Font.font(14));

        topRow.getChildren().addAll(title, spacer, icon);

        VBox valueBox = new VBox(4);
        valueNode.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        valueNode.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        valueBox.getChildren().add(valueNode);

        card.getChildren().addAll(topRow, valueBox);
        return card;
    }

    private VBox createRecentActivitySection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        GamerVaultStyles.applyGlassCard(section);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Text title = new Text("Recent Activity");
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Text viewAll = new Text("View All");
        viewAll.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
        viewAll.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        viewAll.setStyle("-fx-cursor: hand;");

        header.getChildren().addAll(title, spacer, viewAll);

        recentActivityList = new VBox(12);
        Text loadingTxt = new Text("Loading activity logs...");
        loadingTxt.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        recentActivityList.getChildren().add(loadingTxt);

        section.getChildren().addAll(header, recentActivityList);
        return section;
    }

    private HBox createActivityItem(String iconStr, String titleStr, String subStr, String timeStr) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 15, 10, 15));

        item.setStyle(
                "-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 6; -fx-border-color: transparent transparent transparent "
                        + GamerVaultStyles.ACCENT_PURPLE + "; -fx-border-width: 0 0 0 3; -fx-border-radius: 6;");

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(35, 35);
        iconBox.setStyle("-fx-background-color: rgba(139,92,246,0.15); -fx-background-radius: 6;");
        Text icon = new Text(iconStr);
        icon.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
        iconBox.getChildren().add(icon);

        VBox textBox = new VBox(3);
        Text title = new Text(titleStr);
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        Text sub = new Text(subStr);
        sub.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        sub.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        textBox.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Text time = new Text(timeStr);
        time.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        time.setFont(Font.font("Arial", FontWeight.BOLD, 11));

        item.getChildren().addAll(iconBox, textBox, spacer, time);
        return item;
    }

    private VBox createQuickActionsSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        GamerVaultStyles.applyGlassCard(section);

        Text title = new Text("Quick Actions");
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Button addContentButton = createQuickActionBtn("🎥", "Add Content");
        Button addTournamentButton = createQuickActionBtn("🏆", "Add Tournament");
        Button addAdminButton = createQuickActionBtn("🛡", "Manage Admins");
        Button viewUsersButton = createQuickActionBtn("👥", "View Users");

        addContentButton.setOnAction(e -> {
            AdminDashboardSidebar.pageView = "contentHub";
            if (mainScreen != null)
                mainScreen.updateCenter();
        });

        addTournamentButton.setOnAction(e -> {
            AdminDashboardSidebar.pageView = "tournamentManagement";
            if (mainScreen != null)
                mainScreen.updateCenter();
        });

        viewUsersButton.setOnAction(e -> {
            AdminDashboardSidebar.pageView = "userManagement";
            if (mainScreen != null)
                mainScreen.updateCenter();
        });

        addAdminButton.setOnAction(e -> {
            openAddAdminDialog();
        });

        VBox grid = new VBox(10);
        HBox row1 = new HBox(10);
        row1.getChildren().addAll(addContentButton, addTournamentButton);
        HBox row2 = new HBox(10);
        row2.getChildren().addAll(addAdminButton, viewUsersButton);

        for (Node n : row1.getChildren())
            HBox.setHgrow(n, Priority.ALWAYS);
        for (Node n : row2.getChildren())
            HBox.setHgrow(n, Priority.ALWAYS);

        grid.getChildren().addAll(row1, row2);
        section.getChildren().addAll(title, grid);
        return section;
    }

    private void openAddAdminDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Manage Administrator Roles");
        dialog.getDialogPane().setStyle(
                "-fx-background-color: #0B0F19; -fx-border-color: rgba(139,92,246,0.3); -fx-border-width: 1; -fx-border-radius: 8;");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setPrefWidth(400);
        root.setPrefHeight(500);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search user by IGN or Email...");
        HBox searchBox = GamerVaultStyles.createStyledInput(searchField, GamerVaultStyles.ACCENT_PURPLE);

        VBox usersList = new VBox(10);
        ScrollPane scroller = new ScrollPane(usersList);
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setFitToWidth(true);
        VBox.setVgrow(scroller, Priority.ALWAYS);

        root.getChildren().addAll(searchBox, scroller);
        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Style close button
        Node closeBtn = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        if (closeBtn instanceof Button) {
            GamerVaultStyles.applyGhostButton((Button) closeBtn);
            closeBtn.setStyle(closeBtn.getStyle() + "-fx-text-fill: white;");
        }

        // Fetch Users in background
        new Thread(() -> {
            List<UserModel> users = AdminController.fetchAllUsers();
            Platform.runLater(() -> {
                populateAdminDialogList(usersList, users, "");
                searchField.textProperty()
                        .addListener((obs, oldVal, newVal) -> populateAdminDialogList(usersList, users, newVal));
            });
        }).start();

        dialog.showAndWait();
    }

    private void populateAdminDialogList(VBox container, List<UserModel> users, String query) {
        container.getChildren().clear();
        String lowerQuery = query.toLowerCase().trim();

        for (UserModel u : users) {
            String name = u.getIgn() != null && !u.getIgn().isEmpty() ? u.getIgn() : u.getPlayerName();
            String email = u.getEmail() != null ? u.getEmail() : "";

            if (name.toLowerCase().contains(lowerQuery) || email.toLowerCase().contains(lowerQuery)) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(10));
                row.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 6;");

                VBox textCol = new VBox(2);
                Text nText = new Text(name);
                nText.setFill(Color.WHITE);
                nText.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                Text eText = new Text(email);
                eText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                eText.setFont(Font.font("Arial", 10));
                textCol.getChildren().addAll(nText, eText);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                // Toggle Button for Admin Role
                ToggleButton adminToggle = new ToggleButton();
                boolean isAdmin = "ADMIN".equalsIgnoreCase(u.getRole());
                adminToggle.setSelected(isAdmin);
                adminToggle.setText(isAdmin ? "Admin" : "Player");
                adminToggle.setStyle(isAdmin
                        ? "-fx-background-color: " + GamerVaultStyles.ACCENT_PURPLE
                                + "; -fx-text-fill: white; -fx-cursor: hand;"
                        : "-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: #9CA3AF; -fx-cursor: hand;");

                adminToggle.setOnAction(e -> {
                    boolean isNowAdmin = adminToggle.isSelected();
                    u.setRole(isNowAdmin ? "ADMIN" : "PLAYER");
                    adminToggle.setText(isNowAdmin ? "Admin" : "Player");
                    adminToggle.setStyle(isNowAdmin
                            ? "-fx-background-color: " + GamerVaultStyles.ACCENT_PURPLE
                                    + "; -fx-text-fill: white; -fx-cursor: hand;"
                            : "-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: #9CA3AF; -fx-cursor: hand;");

                    new Thread(
                            () -> ProfileController.updateUserProfile(u.getUserId(), u))
                            .start();
                });

                row.getChildren().addAll(textCol, spacer, adminToggle);
                container.getChildren().add(row);
            }
        }
    }

    private Button createQuickActionBtn(String iconStr, String labelStr) {
        Button btn = new Button();
        VBox content = new VBox(8);
        content.setAlignment(Pos.CENTER);

        Text icon = new Text(iconStr);
        icon.setFont(Font.font(24));
        icon.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));

        Text label = new Text(labelStr);
        label.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        label.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        content.getChildren().addAll(icon, label);

        btn.setGraphic(content);
        btn.setAlignment(Pos.CENTER);
        btn.setPrefHeight(90);
        btn.setMaxWidth(Double.MAX_VALUE);

        GamerVaultStyles.applyInteractiveGlassCard(btn, GamerVaultStyles.ACCENT_PURPLE);

        return btn;
    }

    private VBox createPlatformOverviewSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        GamerVaultStyles.applyGlassCard(section);

        Text title = new Text("Ecosystem Analytics");
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        VBox chartsColumn = new VBox(30);

        userChartContainer = new StackPane();
        contentChartContainer = new StackPane();
        tourneyChartContainer = new StackPane();

        VBox userCol = createChartWrapper("User Growth", userChartContainer);
        VBox contentCol = createChartWrapper("Content Distribution", contentChartContainer);
        VBox tourneyCol = createChartWrapper("Tournament Status", tourneyChartContainer);

        chartsColumn.getChildren().addAll(userCol, contentCol, tourneyCol);
        section.getChildren().addAll(title, chartsColumn);

        return section;
    }

    private VBox createChartWrapper(String titleStr, StackPane container) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        Text t = new Text(titleStr);
        t.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        t.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        container.setPrefHeight(200); // Standardize height for all 3 charts
        box.getChildren().addAll(t, container);
        return box;
    }

    private VBox createContentSnapshotSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        GamerVaultStyles.applyGlassCard(section);

        Text title = new Text("Content Snapshot");
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        contentSnapshotList = new VBox(12);
        Text loadingTxt = new Text("Loading latest content...");
        loadingTxt.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        contentSnapshotList.getChildren().add(loadingTxt);

        section.getChildren().addAll(title, contentSnapshotList);
        return section;
    }

    private Button createSnapshotItem(ContentModel model) {
        Button item = new Button();
        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);

        StackPane thumbnail = new StackPane();
        thumbnail.setPrefSize(90, 55);
        thumbnail.setStyle(
                "-fx-background-color: #0A0F18; -fx-background-radius: 6; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 6;");

        if (model.getThumbnailUrl() != null && !model.getThumbnailUrl().isEmpty()) {
            ImageView imgView = new ImageView(model.getThumbnailUrl());
            imgView.setFitWidth(90);
            imgView.setFitHeight(55);
            imgView.setPreserveRatio(false);

            Rectangle clip = new Rectangle(90, 55);
            clip.setArcWidth(12);
            clip.setArcHeight(12);
            thumbnail.setClip(clip);
            thumbnail.getChildren().add(imgView);
        } else {
            Text playIcon = new Text("▶");
            playIcon.setFill(Color.WHITE);
            playIcon.setFont(Font.font(18));
            thumbnail.getChildren().add(playIcon);
        }

        VBox textBox = new VBox(4);
        HBox platBox = new HBox(4);
        platBox.setAlignment(Pos.CENTER_LEFT);

        String platformStr = model.getPlatform() != null ? model.getPlatform() : "Media";
        Text platform = new Text(platformStr);
        platform.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        platform.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        platBox.getChildren().add(platform);

        Text title = new Text(model.getTitle());
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        title.setWrappingWidth(160);

        textBox.getChildren().addAll(platBox, title);
        content.getChildren().addAll(thumbnail, textBox);

        item.setGraphic(content);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10));

        String defaultStyle = "-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 8; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: rgba(139,92,246,0.1); -fx-background-radius: 8; -fx-cursor: hand;";

        item.setStyle(defaultStyle);
        item.setOnMouseEntered(e -> item.setStyle(hoverStyle));
        item.setOnMouseExited(e -> item.setStyle(defaultStyle));

        item.setOnAction(e -> {
            try {
                if (model.getUrl() != null) {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(model.getUrl()));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return item;
    }

    // --- DYNAMIC DATA PIPELINE ---

    private void loadDashboardData() {
        new Thread(() -> {
            try {
                // Fetch live ecosystem data from all controllers
                List<UserModel> users = AdminController.fetchAllUsers();
                List<TournamentModel> tournaments = TournamentController.loadActiveTournaments();
                List<ContentModel> content = ContentController.loadContent();
                List<RecruitmentModel> recruitments = RecruitmentController.fetchActiveMarketPostings();

                // Aggregate matches
                int totalSystemMatches = 0;
                for (UserModel u : users) {
                    totalSystemMatches += u.getTotalMatches();
                }
                final int finalMatches = totalSystemMatches;

                Platform.runLater(() -> {
                    // 1. UPDATE METRICS
                    totalPlayersText.setText(String.format("%,d", users.size()));
                    totalMatchesText.setText(String.format("%,d", finalMatches));
                    totalTournamentsText.setText(String.format("%,d", tournaments.size()));
                    totalContentText.setText(String.format("%,d", content.size()));
                    totalRecruitmentText.setText(String.format("%,d", recruitments.size()));

                    // 2. RENDER CHART
                    populateDashboards(users, content, tournaments);

                    // 3. RENDER RECENT ACTIVITY
                    populateRecentActivity(users, recruitments, content);

                    // 4. RENDER CONTENT SNAPSHOT
                    populateContentSnapshot(content);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void populateDashboards(List<UserModel> users, List<ContentModel> content, List<TournamentModel> tourneys) {

        // 1. AREA CHART (User Growth)
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setStyle("-fx-tick-label-fill: transparent; -fx-border-color: transparent;");
        yAxis.setStyle("-fx-tick-label-fill: #9CA3AF; -fx-border-color: transparent;");
        xAxis.setTickMarkVisible(false);
        yAxis.setTickMarkVisible(false);

        AreaChart<String, Number> areaChart = new AreaChart<>(xAxis, yAxis);
        areaChart.setLegendVisible(false);
        areaChart.setCreateSymbols(true); // Required so we can attach tooltips to the dots
        areaChart.setHorizontalGridLinesVisible(true);
        areaChart.setVerticalGridLinesVisible(false);
        areaChart.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent;");
        areaChart.lookup(".chart-horizontal-grid-lines").setStyle("-fx-stroke: rgba(255,255,255,0.05);");

        XYChart.Series<String, Number> userSeries = new XYChart.Series<>();
        int base = Math.max(users.size(), 10);
        userSeries.getData().addAll(
                new XYChart.Data<>("W1", base * 0.2), new XYChart.Data<>("W2", base * 0.45),
                new XYChart.Data<>("W3", base * 0.85), new XYChart.Data<>("Now", base));
        areaChart.getData().add(userSeries);
        userChartContainer.getChildren().setAll(areaChart);

        // 2. PIE CHART (Content Platforms)
        int yt = 0, ig = 0;
        for (ContentModel c : content) {
            if ("YouTube".equalsIgnoreCase(c.getPlatform()))
                yt++;
            else
                ig++;
        }
        PieChart pieChart = new PieChart();
        PieChart.Data ytData = new PieChart.Data("YouTube", yt);
        PieChart.Data igData = new PieChart.Data("Insta", ig);
        pieChart.getData().addAll(ytData, igData);
        pieChart.setLegendVisible(false);
        contentChartContainer.getChildren().setAll(pieChart);

        // 3. BAR CHART (Tournament Status)
        int live = 0, upcoming = 0;
        for (TournamentModel t : tourneys) {
            if ("LIVE".equalsIgnoreCase(t.getStatus()))
                live++;
            else
                upcoming++;
        }
        CategoryAxis bX = new CategoryAxis();
        NumberAxis bY = new NumberAxis();
        bX.setStyle("-fx-tick-label-fill: #9CA3AF; -fx-border-color: transparent;");
        bY.setStyle("-fx-tick-label-fill: transparent; -fx-border-color: transparent;");
        bX.setTickMarkVisible(false);
        bY.setTickMarkVisible(false);

        BarChart<String, Number> barChart = new BarChart<>(bX, bY);
        barChart.setLegendVisible(false);
        barChart.setHorizontalGridLinesVisible(false);
        barChart.setVerticalGridLinesVisible(false);
        barChart.lookup(".chart-plot-background").setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> tSeries = new XYChart.Series<>();
        XYChart.Data<String, Number> dLive = new XYChart.Data<>("Live", live);
        XYChart.Data<String, Number> dUp = new XYChart.Data<>("Upcoming", upcoming);
        tSeries.getData().addAll(dLive, dUp);
        barChart.getData().add(tSeries);
        tourneyChartContainer.getChildren().setAll(barChart);

        final int finalYt = yt;
        final int finalIg = ig;
        final int finalLive = live;
        final int finalUpcoming = upcoming;

        // --- APPLY STYLES AND INTERACTIVE HOVER TOOLTIPS ---
        Platform.runLater(() -> {
            // Style Area Chart
            Node fill = userSeries.getNode().lookup(".chart-series-area-fill");
            if (fill != null)
                fill.setStyle("-fx-fill: rgba(139, 92, 246, 0.2);");
            Node line = userSeries.getNode().lookup(".chart-series-area-line");
            if (line != null)
                line.setStyle("-fx-stroke: #8B5CF6; -fx-stroke-width: 3px;");

            // Tooltips for Area Chart Dots
            for (XYChart.Data<String, Number> data : userSeries.getData()) {
                if (data.getNode() != null) {
                    Tooltip t = new Tooltip(
                            data.getXValue() + "\nTotal Users: " + Math.round(data.getYValue().doubleValue()));
                    Tooltip.install(data.getNode(), t);
                    data.getNode().setStyle(
                            "-fx-background-color: #8B5CF6, white; -fx-background-insets: 0, 2; -fx-background-radius: 5px; -fx-padding: 5px;");
                }
            }

            // Style and Tooltips for Pie Chart
            if (ytData.getNode() != null) {
                ytData.getNode().setStyle(
                        "-fx-pie-color: #EF4444; -fx-border-color: #0B0F19; -fx-border-width: 2; -fx-cursor: hand;");
                Tooltip.install(ytData.getNode(),
                        new Tooltip("YouTube\n" + finalYt + " Posts"));
            }
            if (igData.getNode() != null) {
                igData.getNode().setStyle(
                        "-fx-pie-color: #EC4899; -fx-border-color: #0B0F19; -fx-border-width: 2; -fx-cursor: hand;");
                Tooltip.install(igData.getNode(),
                        new Tooltip("Instagram\n" + finalIg + " Posts"));
            }

            // Style and Tooltips for Bar Chart
            if (dLive.getNode() != null) {
                dLive.getNode().setStyle("-fx-bar-fill: #EF4444; -fx-cursor: hand;");
                Tooltip.install(dLive.getNode(),
                        new Tooltip("Live Tournaments: " + finalLive));
            }
            if (dUp.getNode() != null) {
                dUp.getNode().setStyle("-fx-bar-fill: #F59E0B; -fx-cursor: hand;");
                Tooltip.install(dUp.getNode(),
                        new Tooltip("Upcoming Tournaments: " + finalUpcoming));
            }
        });
    }

    private void populateRecentActivity(List<UserModel> users, List<RecruitmentModel> recruitments,
            List<ContentModel> contents) {
        recentActivityList.getChildren().clear();

        // Fetch real global activity log from the Notification System
        // List<NotificationModel> globalLogs = NotificationController
        // .getGlobalAdminActivity();

        // if (globalLogs == null || globalLogs.isEmpty()) {
        // Text emptyTxt = new Text("No system activity logged yet.");
        // emptyTxt.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        // recentActivityList.getChildren().add(emptyTxt);
        // } else {
        // for (NotificationModel log : globalLogs) {
        // // Dynamically assign icons based on the event type
        // String icon = "📣";
        // if (log.getType() != null) {
        // switch (log.getType()) {
        // case "MATCH":
        // icon = "🎮";
        // break;
        // case "TOURNAMENT":
        // icon = "🏆";
        // break;
        // case "CONTENT":
        // icon = "🎥";
        // break;
        // case "SYSTEM":
        // icon = "⚙";
        // break;
        // }
        // }

        // // Add the real log to the UI
        // recentActivityList.getChildren().add(createActivityItem(
        // icon,
        // log.getTitle() != null ? log.getTitle() : "System Alert",
        // log.getMessage() != null ? log.getMessage() : "",
        // "Recent"));
        // }
        // }
    }

    private void populateContentSnapshot(List<ContentModel> content) {
        contentSnapshotList.getChildren().clear();

        int maxItems = Math.min(2, content.size());
        for (int i = 0; i < maxItems; i++) {
            contentSnapshotList.getChildren().add(createSnapshotItem(content.get(i)));
        }

        if (contentSnapshotList.getChildren().isEmpty()) {
            Text emptyTxt = new Text("No content uploaded yet.");
            emptyTxt.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
            contentSnapshotList.getChildren().add(emptyTxt);
        }
    }
}