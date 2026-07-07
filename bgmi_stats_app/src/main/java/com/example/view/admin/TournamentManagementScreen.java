package com.example.view.admin;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.example.controller.admin.TournamentController;
import com.example.dao.StorageDao;
import com.example.model.admin.TournamentModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
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
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class TournamentManagementScreen {

    // VARIABLES
    private FlowPane tournamentGrid;
    private HBox metricsContainer;

    // Local caching list for client-side fluid searching
    private List<TournamentModel> allFetchedTournaments = new ArrayList<>();

    public BorderPane startTournamentManagementScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        ScrollPane scroller = new ScrollPane();
        scroller.setContent(createContent());
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setFitToWidth(true);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setCenter(scroller);
        return root;
    }

    private VBox createContent() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(10, 20, 40, 0));

        // 1. HEADER SECTION
        HBox headerBox = createHeaderBox();

        // 2. METRICS ROW
        metricsContainer = new HBox(20);

        // 3. SEARCH & FILTERS ROW
        HBox filtersRow = createFiltersRow();

        // 4. TOURNAMENT CARDS GRID
        tournamentGrid = new FlowPane();
        tournamentGrid.setHgap(20);
        tournamentGrid.setVgap(20);

        container.getChildren().addAll(headerBox, metricsContainer, filtersRow, tournamentGrid);

        // Fetch data from Firebase
        refreshTournaments();

        return container;
    }

    /*
     * HEADER UI
     */
    private HBox createHeaderBox() {
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox textCol = new VBox(5);
        Text title = new Text("Tournament Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

        Text subtitle = new Text(
                "Schedule competitive brackets, update arena variables, and allocate custom prize tiers.");
        subtitle.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        textCol.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportBtn = new Button("🏆 Host Tournament");
        exportBtn.setPrefHeight(45);
        exportBtn.setPrefWidth(180);
        GamerVaultStyles.applyGradientButton(exportBtn, GamerVaultStyles.ACCENT_PURPLE,
                GamerVaultStyles.ACCENT_PURPLE_DARK, "white");

        // Wire up the button click to show the form dialog
        exportBtn.setOnAction(e -> openHostDialog());

        headerRow.getChildren().addAll(textCol, spacer, exportBtn);

        GamerVaultAnimations.fadeInUp(textCol, 0, 500);
        GamerVaultAnimations.fadeInUp(exportBtn, 100, 500);

        return headerRow;
    }

    private VBox createMetricCard(String typeStr, String iconStr, String valueStr, String labelStr,
            String accentColor) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        GamerVaultStyles.applyGlassCard(card);
        GamerVaultAnimations.scaleOnHover(card, 1.03);

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Text icon = new Text(iconStr);
        icon.setFill(Color.web(accentColor));
        icon.setFont(Font.font(16));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Text typeText = new Text(typeStr);
        typeText.setFill(Color.web(accentColor));
        typeText.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        topRow.getChildren().addAll(icon, spacer, typeText);

        VBox valueBox = new VBox(4);
        Text mainValue = new Text(valueStr);
        mainValue.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        mainValue.setFont(Font.font("Arial", FontWeight.BOLD, 36));

        Text bottomLabel = new Text(labelStr);
        bottomLabel.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        bottomLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 13));

        valueBox.getChildren().addAll(mainValue, bottomLabel);

        card.getChildren().addAll(topRow, valueBox);
        return card;
    }

    /*
     * SEARCH AND FILTERS SECTION
     */
    private HBox createFiltersRow() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 20, 10, 20));
        row.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 8;");

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search tournaments by title...");

        // Listen for typing and filter instantly
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAndRenderGrid(newValue);
        });

        HBox searchBox = GamerVaultStyles.createStyledInput(searchField, GamerVaultStyles.ACCENT_PURPLE);
        searchBox.setPrefWidth(300);
        searchBox.setPrefHeight(40);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button roleFilter = new Button("Status: All  ▼");
        roleFilter.setPrefHeight(40);
        GamerVaultStyles.applyGhostButton(roleFilter);

        Button statusFilter = new Button("Game: BGMI  ▼");
        statusFilter.setPrefHeight(40);
        GamerVaultStyles.applyGhostButton(statusFilter);

        row.getChildren().addAll(searchBox, spacer, roleFilter, statusFilter);
        GamerVaultAnimations.fadeInUp(row, 200, 500);

        return row;
    }

    // --- MISSING METHOD: FIXED COMPILATION ERROR ---
    private HBox createMetadataRow(String key, String val, String hexColor) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Text k = new Text(key);
        k.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        k.setFont(Font.font("Arial", FontWeight.NORMAL, 13));

        Region s = new Region();
        HBox.setHgrow(s, Priority.ALWAYS);

        Text v = new Text(val != null ? val : "N/A");
        v.setFill(Color.web(hexColor));
        v.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        row.getChildren().addAll(k, s, v);
        return row;
    }

    private VBox createTournamentCard(TournamentModel model) {
        VBox card = new VBox(15);
        card.setPrefSize(320, 390);
        card.setPadding(new Insets(15));
        GamerVaultStyles.applyGlassCard(card);
        GamerVaultAnimations.scaleOnHover(card, 1.02);

        // --- 1. IMAGE CAROUSEL ---
        StackPane carouselPane = new StackPane();
        carouselPane.setPrefHeight(150);
        carouselPane.setStyle("-fx-background-color: #0F172A; -fx-background-radius: 8; -fx-border-radius: 8;");

        Rectangle clip = new Rectangle(290, 150);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        carouselPane.setClip(clip);

        List<String> imageUrls = model.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            ImageView imgView = new ImageView();
            imgView.setFitWidth(290);
            imgView.setFitHeight(150);
            imgView.setPreserveRatio(false);

            // Load first image
            try {
                imgView.setImage(new Image(imageUrls.get(0), true));
            } catch (Exception ignored) {
            }

            carouselPane.getChildren().add(imgView);

            if (imageUrls.size() > 1) {
                int[] currentIdx = { 0 };
                Button prevBtn = new Button("<");
                Button nextBtn = new Button(">");
                String navStyle = "-fx-background-color: rgba(0,0,0,0.6); -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 10 5 10; -fx-background-radius: 15;";
                prevBtn.setStyle(navStyle);
                nextBtn.setStyle(navStyle);

                prevBtn.setOnAction(e -> {
                    currentIdx[0] = (currentIdx[0] - 1 < 0) ? imageUrls.size() - 1 : currentIdx[0] - 1;
                    imgView.setImage(new Image(imageUrls.get(currentIdx[0]), true));
                });

                nextBtn.setOnAction(e -> {
                    currentIdx[0] = (currentIdx[0] + 1) % imageUrls.size();
                    imgView.setImage(new Image(imageUrls.get(currentIdx[0]), true));
                });

                StackPane.setAlignment(prevBtn, Pos.CENTER_LEFT);
                StackPane.setAlignment(nextBtn, Pos.CENTER_RIGHT);
                StackPane.setMargin(prevBtn, new Insets(0, 0, 0, 5));
                StackPane.setMargin(nextBtn, new Insets(0, 5, 0, 0));
                carouselPane.getChildren().addAll(prevBtn, nextBtn);
            }
        } else {
            // Fallback icon
            Text trophyIcon = new Text("🏆");
            trophyIcon.setFont(Font.font(40));
            trophyIcon.setFill(Color.web("rgba(255,255,255,0.15)"));
            carouselPane.getChildren().add(trophyIcon);
        }

        // --- 2. FIXED STATUS BADGE ---
        String status = model.getStatus() != null ? model.getStatus().toUpperCase() : "UPCOMING";
        String statusColorHex = GamerVaultStyles.ACCENT_ORANGE;
        if ("LIVE".equals(status))
            statusColorHex = "#EF4444";
        if ("COMPLETED".equals(status))
            statusColorHex = GamerVaultStyles.ACCENT_GREEN;

        // CHANGED: Using a Label instead of a StackPane prevents it from stretching
        // over the image!
        Label badge = new Label(status);
        badge.setStyle("-fx-background-color: " + statusColorHex
                + "CC; -fx-text-fill: white; -fx-font-family: Arial; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 4 10 4 10; -fx-background-radius: 6;");

        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(8));
        carouselPane.getChildren().add(badge);

        // --- 3. TEXT INFO ---
        VBox textInfo = new VBox(8);
        Text date = new Text("📅 " + (model.getStartDate() != null ? model.getStartDate() : "TBD"));
        date.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
        date.setFont(Font.font("Arial", FontWeight.BOLD, 11));

        Text title = new Text(model.getTitle());
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setWrappingWidth(260);

        Text orgText = new Text(
                "By: " + (model.getOrganizerName() != null ? model.getOrganizerName() : "Community Organizer"));
        orgText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        orgText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

        textInfo.getChildren().addAll(date, title, orgText);

        VBox stats = new VBox(5);
        stats.getChildren().addAll(
                createMetadataRow("Prize:", model.getPrizePool(), GamerVaultStyles.ACCENT_CYAN),
                createMetadataRow("Slots:", model.getSlotsRegistered() + " / " + model.getSlotsMax(), "#F59E0B"));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox bottomActions = new HBox(8);
        bottomActions.setAlignment(Pos.CENTER_LEFT);

        Button viewBtn = new Button("View / Manage");
        viewBtn.setPrefHeight(30);
        GamerVaultStyles.applyGhostButton(viewBtn);
        viewBtn.setOnAction(e -> openDetailsModal(model));

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        Button deleteBtn = new Button("🗑");
        deleteBtn.setPrefSize(30, 30);
        GamerVaultStyles.applyGhostButton(deleteBtn);
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
                "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #EF4444; -fx-background-radius: 8; -fx-cursor: hand;"));
        deleteBtn.setOnMouseExited(e -> GamerVaultStyles.applyGhostButton(deleteBtn));

        deleteBtn.setOnAction(e -> {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Delete Tournament");
            alert.setHeaderText("Delete " + model.getTitle() + "?");
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK) {
                    if (TournamentController.deleteTournament(model.getTournamentId())) {
                        refreshTournaments(); // Instantly removes it from the screen
                    }
                }
            });
        });

        bottomActions.getChildren().addAll(viewBtn, hSpacer, deleteBtn);

        card.getChildren().addAll(carouselPane, textInfo, stats, spacer, bottomActions);
        return card;
    }

    private void refreshTournaments() {
        new Thread(() -> {
            try {
                List<TournamentModel> dataSet = TournamentController.loadActiveTournaments();

                Platform.runLater(() -> {
                    this.allFetchedTournaments = dataSet;

                    calculateAndPopulateMetrics();
                    filterAndRenderGrid("");
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void calculateAndPopulateMetrics() {
        int total = allFetchedTournaments.size();
        int active = 0;
        int upcoming = 0;
        int completed = 0;

        for (TournamentModel t : allFetchedTournaments) {
            String status = t.getStatus() != null ? t.getStatus().toUpperCase() : "UPCOMING";
            if ("LIVE".equals(status))
                active++;
            else if ("COMPLETED".equals(status))
                completed++;
            else
                upcoming++;
        }

        metricsContainer.getChildren().clear();
        metricsContainer.getChildren().addAll(
                createMetricCard("TOTAL EVENTS", "⊞", String.valueOf(total), "All time entries",
                        GamerVaultStyles.TEXT_MUTED),
                createMetricCard("ACTIVE NOW", "🔴", String.valueOf(active), "Currently live matches", "#EF4444"),
                createMetricCard("UPCOMING", "📅", String.valueOf(upcoming), "Registrations open",
                        GamerVaultStyles.ACCENT_ORANGE),
                createMetricCard("COMPLETED", "🏆", String.valueOf(completed), "Concluded Tournaments",
                        GamerVaultStyles.ACCENT_GREEN));

        for (Node n : metricsContainer.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS);
        }
        GamerVaultAnimations.staggerFadeInChildren(metricsContainer, 80);
    }

    private void filterAndRenderGrid(String query) {
        String lowerQuery = query.toLowerCase().trim();
        tournamentGrid.getChildren().clear();
        int index = 0;

        for (TournamentModel t : allFetchedTournaments) {
            String matchTitle = t.getTitle() != null ? t.getTitle().toLowerCase() : "";
            if (matchTitle.contains(lowerQuery)) {
                VBox card = createTournamentCard(t);
                tournamentGrid.getChildren().add(card);
                GamerVaultAnimations.fadeInUp(card, index * 60, 450);
                index++;
            }
        }

        if (tournamentGrid.getChildren().isEmpty()) {
            Text emptyText = new Text("No tournaments found matching criteria.");
            emptyText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
            tournamentGrid.getChildren().add(emptyText);
        }
    }

    private void openDetailsModal(TournamentModel model) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tournament Overview");

        VBox modalRoot = new VBox(15);
        modalRoot.setPadding(new Insets(25));
        modalRoot.setPrefWidth(450);
        modalRoot.setStyle(
                "-fx-background-color: #0B0F19; -fx-border-color: rgba(139,92,246,0.3); -fx-border-width: 1; -fx-border-radius: 8;");

        Text title = new Text(model.getTitle());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setFill(Color.WHITE);
        title.setWrappingWidth(400);

        VBox dataGrid = new VBox(10);
        dataGrid.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-padding: 15; -fx-background-radius: 8;");

        dataGrid.getChildren().addAll(
                createMetadataRow("Organizer", model.getOrganizerName(), "#FFFFFF"),
                createMetadataRow("Game", model.getGame(), GamerVaultStyles.ACCENT_PURPLE_LIGHT),
                createMetadataRow("Prize Pool", model.getPrizePool(), GamerVaultStyles.ACCENT_CYAN),
                createMetadataRow("Registration Begins", model.getRegBeginDate(), "#E2E8F0"),
                createMetadataRow("Registration Ends", model.getRegEndDate(), "#E2E8F0"),
                createMetadataRow("Tournament Starts", model.getStartDate(), "#F59E0B"),
                createMetadataRow("Slots Filled", model.getSlotsRegistered() + " / " + model.getSlotsMax(), "#FFFFFF"));

        modalRoot.getChildren().addAll(title, dataGrid);

        if (model.getInstagramLink() != null && !model.getInstagramLink().isEmpty()) {
            Button instaBtn = new Button("View Organizer Instagram");
            instaBtn.setMaxWidth(Double.MAX_VALUE);
            GamerVaultStyles.applyGhostButton(instaBtn);
            instaBtn.setStyle(instaBtn.getStyle() + "-fx-border-color: #E1306C; -fx-text-fill: #E1306C;");

            instaBtn.setOnAction(e -> {
                try {
                    java.awt.Desktop.getDesktop().browse(new URI(model.getInstagramLink()));
                } catch (Exception ex) {
                    System.err.println("Could not launch browser: " + ex.getMessage());
                }
            });
            modalRoot.getChildren().add(instaBtn);
        }

        dialog.getDialogPane().setContent(modalRoot);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle("-fx-background-color: #0B0F19;");

        Node closeBtn = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        if (closeBtn instanceof Button) {
            GamerVaultStyles.applyGhostButton((Button) closeBtn);
            closeBtn.setStyle(closeBtn.getStyle() + "-fx-text-fill: white;");
        }

        dialog.showAndWait();
    }

    private void openHostDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Launch Tournament Sequence");
        dialog.getDialogPane().setStyle(
                "-fx-background-color: #0B0F19; -fx-border-color: rgba(139,92,246,0.3); -fx-border-width: 1; -fx-border-radius: 8;");

        VBox rootBox = new VBox(20);
        rootBox.setPadding(new Insets(25));
        rootBox.setPrefWidth(750);

        TextField tTitle = new TextField();
        tTitle.setPromptText("e.g., BGIS Qualifier Hub Stage 1");
        TextField tOrganizer = new TextField();
        tOrganizer.setPromptText("e.g., Nodwin Gaming");
        TextField tPrize = new TextField();
        tPrize.setPromptText("e.g., ₹5,000 INR");
        TextField tSlots = new TextField();
        tSlots.setPromptText("e.g., 64");
        TextField tInsta = new TextField();
        tInsta.setPromptText("https://instagram.com/organizer");

        DatePicker dRegBegin = new DatePicker();
        dRegBegin.setMaxWidth(Double.MAX_VALUE);
        DatePicker dRegEnd = new DatePicker();
        dRegEnd.setMaxWidth(Double.MAX_VALUE);
        DatePicker dStart = new DatePicker();
        dStart.setMaxWidth(Double.MAX_VALUE);

        // --- NATIVE FILE PICKER LOGIC ---
        List<File> selectedImageFiles = new ArrayList<>();
        Button chooseImageBtn = new Button("📁 Browse Images");
        GamerVaultStyles.applyGhostButton(chooseImageBtn);
        Text selectedFilesLabel = new Text("No files selected.");
        selectedFilesLabel.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

        chooseImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Tournament Promo Images");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp"));

            Window window = dialog.getDialogPane().getScene().getWindow();
            List<File> files = fileChooser.showOpenMultipleDialog(window);

            if (files != null && !files.isEmpty()) {
                selectedImageFiles.clear();
                selectedImageFiles.addAll(files);
                selectedFilesLabel.setText(files.size() + " image(s) ready for upload.");
                selectedFilesLabel.setFill(Color.web(GamerVaultStyles.ACCENT_GREEN));
            }
        });

        VBox imageUploadBox = new VBox(8);
        imageUploadBox.getChildren().addAll(chooseImageBtn, selectedFilesLabel);

        HBox row1 = new HBox(20);
        VBox titleBox = createFormRow("Tournament Label Title", tTitle);
        VBox orgBox = createFormRow("Organizer Identity Name", tOrganizer);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        HBox.setHgrow(orgBox, Priority.ALWAYS);
        row1.getChildren().addAll(titleBox, orgBox);

        HBox row2 = new HBox(20);
        VBox prizeBox = createFormRow("Prize Allocation Matrix", tPrize);
        VBox slotsBox = createFormRow("Maximum Bracket Slots", tSlots);
        HBox.setHgrow(prizeBox, Priority.ALWAYS);
        HBox.setHgrow(slotsBox, Priority.ALWAYS);
        row2.getChildren().addAll(prizeBox, slotsBox);

        HBox row3 = new HBox(20);
        VBox beginBox = createFormRow("Registration Lifecycle Begin Date", dRegBegin);
        VBox endBox = createFormRow("Registration Lifecycle Closure Date", dRegEnd);
        HBox.setHgrow(beginBox, Priority.ALWAYS);
        HBox.setHgrow(endBox, Priority.ALWAYS);
        row3.getChildren().addAll(beginBox, endBox);

        HBox row4 = new HBox(20);
        VBox startBox = createFormRow("Arena Target Launch Date", dStart);
        VBox instaBox = createFormRow("Organizer Instagram Social Portal Link", tInsta);
        HBox.setHgrow(startBox, Priority.ALWAYS);
        HBox.setHgrow(instaBox, Priority.ALWAYS);
        row4.getChildren().addAll(startBox, instaBox);

        VBox imageBox = createFormRow("Promo Display Assets", imageUploadBox);

        rootBox.getChildren().addAll(row1, row2, row3, row4, imageBox);
        dialog.getDialogPane().setContent(rootBox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Node okBtn = dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (okBtn instanceof Button) {
            GamerVaultStyles.applyGradientButton((Button) okBtn, GamerVaultStyles.ACCENT_PURPLE,
                    GamerVaultStyles.ACCENT_PURPLE_DARK, "white");
        }

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                int parsedSlots = 32;
                try {
                    parsedSlots = Integer.parseInt(tSlots.getText().trim());
                } catch (Exception ignored) {
                }
                final int finalSlots = parsedSlots;

                String startDateStr = dStart.getValue() != null ? dStart.getValue().toString() : "";
                String regBeginStr = dRegBegin.getValue() != null ? dRegBegin.getValue().toString() : "";
                String regEndStr = dRegEnd.getValue() != null ? dRegEnd.getValue().toString() : "";

                tournamentGrid.getChildren().clear();
                Text loadingTxt = new Text("Uploading assets and compiling tournament data...");
                loadingTxt.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
                tournamentGrid.getChildren().add(loadingTxt);

                new Thread(() -> {
                    List<String> uploadedUrls = new ArrayList<>();
                    StorageDao storageDao = new StorageDao();

                    if (!selectedImageFiles.isEmpty()) {
                        for (File file : selectedImageFiles) {
                            try {
                                String url = storageDao.uploadTournamentImage(file);
                                uploadedUrls.add(url);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    boolean status = TournamentController.createNewTournament(
                            tTitle.getText().trim(), tPrize.getText().trim(), finalSlots, startDateStr,
                            regBeginStr, regEndStr, uploadedUrls, tOrganizer.getText().trim(), tInsta.getText().trim());

                    if (status) {
                        Platform.runLater(this::refreshTournaments);
                    }
                }).start();
            }
        });
    }

    private VBox createFormRow(String label, Node inputField) {
        VBox box = new VBox(5);
        Text t = new Text(label);
        t.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        t.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        if (inputField instanceof TextField) {
            HBox input = GamerVaultStyles.createStyledInput((TextField) inputField, GamerVaultStyles.ACCENT_PURPLE);
            input.setPrefHeight(40);
            box.getChildren().addAll(t, input);
        } else {
            box.getChildren().addAll(t, inputField);
        }
        return box;
    }
}