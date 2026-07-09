package com.example.view.player;

import com.example.controller.AuthController;
import com.example.controller.player.BattleController;
import com.example.model.player.BattleModel;
import com.example.keys.BattleFirebaseKeys;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;
import java.util.stream.Collectors;

public class BattleArenaScreen {

    private PlayerMainScreen mainScreen;

    // UI Containers
    private FlowPane playerLobbiesGrid;
    private HBox featuredAdminBox;

    // Filters
    private ComboBox<String> modeFilter;
    private ComboBox<String> formatFilter;

    private List<BattleModel> allOpenBattles;

    public BorderPane startBattleArenaScreen(PlayerMainScreen mainScreen) {
        this.mainScreen = mainScreen;

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        ScrollPane scroller = new ScrollPane();
        scroller.setContent(createArenaContent());
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setFitToWidth(true);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setCenter(scroller);

        loadBattles();

        return root;
    }

    private VBox createArenaContent() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(10, 20, 40, 20));

        // 1. Header & Wallet
        HBox headerRow = createHeaderRow();

        // 2. Controls & Filters
        HBox controlsRow = createControlsRow();
        GamerVaultAnimations.fadeInUp(controlsRow, 150, 400);

        // 3. Featured Admin Tournaments (Horizontal Scroll)
        VBox featuredSection = createFeaturedSection();
        GamerVaultAnimations.fadeInUp(featuredSection, 250, 400);

        // 4. Open Player Wager Lobbies (Grid)
        VBox playerSection = createPlayerLobbiesSection();
        GamerVaultAnimations.fadeInUp(playerSection, 350, 400);

        container.getChildren().addAll(headerRow, controlsRow, featuredSection, playerSection);
        return container;
    }

    // --- 1. HEADER ---
    private HBox createHeaderRow() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(5);
        Text title = new Text("Battle Arena");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 36));
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

        Text subtitle = new Text("Compete in high-stakes TDM wagers and official cash tournaments.");
        subtitle.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        titleBox.getChildren().addAll(title, subtitle);
        GamerVaultAnimations.fadeInUp(titleBox, 0, 400);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Wallet Pill
        HBox wallet = new HBox(15);
        wallet.setPadding(new Insets(8, 20, 8, 20));
        wallet.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 20;");
        wallet.setAlignment(Pos.CENTER);

        Text coins = new Text("Coins: 1,250 🪙"); // In future, link to UserModel
        coins.setFill(Color.web("#F59E0B"));
        coins.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Region div = new Region();
        div.setPrefSize(1, 20);
        div.setStyle("-fx-background-color: rgba(255,255,255,0.2);");

        Text cash = new Text("Cash: ₹500 💵");
        cash.setFill(Color.web(GamerVaultStyles.ACCENT_GREEN));
        cash.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        wallet.getChildren().addAll(coins, div, cash);
        GamerVaultAnimations.fadeInUp(wallet, 50, 400);

        header.getChildren().addAll(titleBox, spacer, wallet);
        return header;
    }

    // --- 2. CONTROLS ---
    private HBox createControlsRow() {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);

        // Filters
        modeFilter = createStyledDropdown("Mode: All", "1V1", "2V2", "4V4");
        formatFilter = createStyledDropdown("Format: All", "BO1", "BO3");

        modeFilter.setOnAction(e -> filterLobbies());
        formatFilter.setOnAction(e -> filterLobbies());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Host Button
        Button hostBtn = new Button("+ Host New Battle");
        hostBtn.setPrefHeight(40);
        hostBtn.setPadding(new Insets(0, 20, 0, 20));
        GamerVaultStyles.applyGradientButton(hostBtn, GamerVaultStyles.ACCENT_PURPLE,
                GamerVaultStyles.ACCENT_PURPLE_DARK, "white");
        hostBtn.setOnAction(e -> openHostBattleDialog());

        controls.getChildren().addAll(modeFilter, formatFilter, spacer, hostBtn);
        return controls;
    }

    // --- 3. FEATURED (ADMIN) SECTION ---
    private VBox createFeaturedSection() {
        VBox section = new VBox(15);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Text icon = new Text("⭐");
        icon.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
        Text title = new Text("Featured Cash Clashes");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        header.getChildren().addAll(icon, title);

        featuredAdminBox = new HBox(20);
        ScrollPane featuredScroller = new ScrollPane(featuredAdminBox);
        GamerVaultStyles.applyStyledScrollPane(featuredScroller);
        featuredScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        featuredScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        featuredScroller.setFitToHeight(true);

        section.getChildren().addAll(header, featuredScroller);
        return section;
    }

    // --- 4. PLAYER WAGER LOBBIES ---
    private VBox createPlayerLobbiesSection() {
        VBox section = new VBox(15);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Text title = new Text("Open Wager Lobbies");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Text onlineDot = new Text("● ");
        onlineDot.setFill(Color.web(GamerVaultStyles.ACCENT_GREEN));
        Text onlineTxt = new Text("142 Players Online"); // Mock data
        onlineTxt.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        onlineTxt.setFont(Font.font("Arial", 12));

        header.getChildren().addAll(title, spacer, onlineDot, onlineTxt);

        playerLobbiesGrid = new FlowPane();
        playerLobbiesGrid.setHgap(20);
        playerLobbiesGrid.setVgap(20);

        section.getChildren().addAll(header, playerLobbiesGrid);
        return section;
    }

    // --- DATA LOADING & FILTERING ---
    private void loadBattles() {
        playerLobbiesGrid.getChildren().clear();
        featuredAdminBox.getChildren().clear();

        Text loading = new Text("Scanning Arena Servers...");
        loading.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE));
        playerLobbiesGrid.getChildren().add(loading);

        new Thread(() -> {
            allOpenBattles = BattleController.getOpenLobbies();
            Platform.runLater(this::filterLobbies);
        }).start();
    }

    private void filterLobbies() {
        if (allOpenBattles == null)
            return;
        playerLobbiesGrid.getChildren().clear();
        featuredAdminBox.getChildren().clear();

        String selectedMode = modeFilter.getValue();
        String selectedFormat = formatFilter.getValue();

        int delay = 0;
        for (BattleModel battle : allOpenBattles) {
            // Apply Filters
            if (selectedMode != null && !selectedMode.contains("All") && !battle.getMode().equals(selectedMode))
                continue;
            if (selectedFormat != null && !selectedFormat.contains("All") && !battle.getFormat().equals(selectedFormat))
                continue;

            if ("ADMIN".equals(battle.getHostType())) {
                // Future expansion: Render Wide Admin Card here
            } else {
                StackPane card = createPlayerLobbyCard(battle);
                playerLobbiesGrid.getChildren().add(card);
                GamerVaultAnimations.fadeInUp(card, delay * 50, 400);
                delay++;
            }
        }

        if (playerLobbiesGrid.getChildren().isEmpty()) {
            Text empty = new Text("No active wagers match your filters.");
            empty.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
            playerLobbiesGrid.getChildren().add(empty);
        }
    }

    // --- LOBBY CARD BUILDER ---
    private StackPane createPlayerLobbyCard(BattleModel battle) {
        StackPane cardBase = new StackPane();
        cardBase.setPrefSize(260, 320);

        // 1. Background Image Based on Mode
        ImageView bgImg = new ImageView();
        bgImg.setFitWidth(260);
        bgImg.setFitHeight(320);
        try {
            String path = "/assets/bgmi_images/" + battle.getMode().toLowerCase() + "_bg.jpg";
            if (getClass().getResource(path) != null) {
                bgImg.setImage(new Image(getClass().getResource(path).toExternalForm(), true));
            } else {
                bgImg.setImage(
                        new Image(getClass().getResource("/assets/bgmi_images/erangel_bg.jpg").toExternalForm(), true));
            }
        } catch (Exception e) {
        }

        Rectangle clip = new Rectangle(260, 320);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        bgImg.setClip(clip);

        // Dark Overlay for readability
        Region darkOverlay = new Region();
        darkOverlay.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 0% 100%, rgba(10,14,26,0.85), rgba(10,14,26,0.98)); -fx-background-radius: 16;");

        // 2. Foreground Content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 16; -fx-background-radius: 16;");
        GamerVaultAnimations.scaleOnHover(cardBase, 1.03);

        // Top: Host Info & Badge
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        avatar.setPrefSize(35, 35);
        avatar.setStyle("-fx-background-color: rgba(139,92,246,0.2); -fx-background-radius: 8;");
        Text initial = new Text("👤");
        initial.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
        avatar.getChildren().add(initial);

        VBox hostBox = new VBox();
        Text hostName = new Text("Host"); // To replace with real IGN via query later
        hostName.setFill(Color.WHITE);
        hostName.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        HBox regionBox = new HBox(4);
        regionBox.setAlignment(Pos.CENTER_LEFT);
        Text pin = new Text("📍 IN");
        pin.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        pin.setFont(Font.font("Arial", 10));
        regionBox.getChildren().add(pin);
        hostBox.getChildren().addAll(hostName, regionBox);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane formatBadge = new StackPane();
        formatBadge.setPadding(new Insets(4, 8, 4, 8));
        formatBadge.setStyle("-fx-background-color: rgba(0, 255, 255, 0.15); -fx-background-radius: 4;");
        Text formatTxt = new Text("BEST OF " + battle.getFormat().replace("BO", ""));
        formatTxt.setFill(Color.web(GamerVaultStyles.ACCENT_CYAN));
        formatTxt.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        formatBadge.getChildren().add(formatTxt);

        topRow.getChildren().addAll(avatar, hostBox, spacer, formatBadge);

        // Middle: Match Type
        VBox typeBox = new VBox(2);
        Text tLbl = new Text("MATCH TYPE");
        tLbl.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        tLbl.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        Text tVal = new Text(battle.getMode().replace("V", " V "));
        tVal.setFill(Color.WHITE);
        tVal.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        typeBox.getChildren().addAll(tLbl, tVal);

        // Stats: Entry & Pool
        HBox financialBox = new HBox(40);
        VBox entryBox = new VBox(2);
        Text eLbl = new Text("ENTRY");
        eLbl.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        eLbl.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        Text eVal = new Text(String.format("%.0f 🪙", battle.getEntryFeeCoins()));
        eVal.setFill(Color.web("#F59E0B"));
        eVal.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        entryBox.getChildren().addAll(eLbl, eVal);

        VBox poolBox = new VBox(2);
        Text pLbl = new Text("WIN POOL");
        pLbl.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        pLbl.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        // Total Pool = Entry Fee * Max Participants
        double totalPool = battle.getEntryFeeCoins() * battle.getMaxParticipants();
        Text pVal = new Text(String.format("%.0f 🪙", totalPool));
        pVal.setFill(Color.web("#F59E0B"));
        pVal.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        poolBox.getChildren().addAll(pLbl, pVal);
        financialBox.getChildren().addAll(entryBox, poolBox);

        // Progress Bar
        VBox progBox = new VBox(5);
        HBox pTop = new HBox();
        Text pLabel = new Text("PLAYERS JOINED");
        pLabel.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        pLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        Region pSp = new Region();
        HBox.setHgrow(pSp, Priority.ALWAYS);
        int currentJoined = battle.getParticipants() != null ? battle.getParticipants().size() : 1;
        Text pCount = new Text(currentJoined + "/" + battle.getMaxParticipants());
        pCount.setFill(Color.WHITE);
        pCount.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        pTop.getChildren().addAll(pLabel, pSp, pCount);

        StackPane track = new StackPane();
        track.setAlignment(Pos.CENTER_LEFT);
        Rectangle bgTrack = new Rectangle(220, 4, Color.web("rgba(255,255,255,0.1)"));
        bgTrack.setArcWidth(4);
        bgTrack.setArcHeight(4);

        double fillWidth = 220 * ((double) currentJoined / battle.getMaxParticipants());
        Rectangle fgTrack = new Rectangle(fillWidth, 4, Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
        fgTrack.setArcWidth(4);
        fgTrack.setArcHeight(4);
        track.getChildren().addAll(bgTrack, fgTrack);

        progBox.getChildren().addAll(pTop, track);

        Region vSpacer = new Region();
        VBox.setVgrow(vSpacer, Priority.ALWAYS);

        // Bottom Action Button
        Button joinBtn = new Button();
        joinBtn.setPrefWidth(220);
        joinBtn.setPrefHeight(40);

        boolean isFull = currentJoined >= battle.getMaxParticipants();
        boolean alreadyJoined = AuthController.currentUser != null
                && battle.getParticipants().containsKey(AuthController.currentUser.getUserId());

        if (isFull || alreadyJoined) {
            joinBtn.setText(alreadyJoined ? "Already Joined" : "Lobby Full");
            joinBtn.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: #6B7280; -fx-background-radius: 8;");
            joinBtn.setDisable(true);
        } else {
            joinBtn.setText("Join Lobby (" + String.format("%.0f", battle.getEntryFeeCoins()) + " 🪙)");
            GamerVaultStyles.applyGhostButton(joinBtn);
            joinBtn.setStyle(joinBtn.getStyle() + "-fx-border-color: " + GamerVaultStyles.ACCENT_PURPLE
                    + "; -fx-text-fill: white;");

            joinBtn.setOnAction(e -> {
                joinBtn.setText("Joining...");
                joinBtn.setDisable(true);
                new Thread(() -> {
                    boolean success = BattleController.joinBattle(battle);
                    Platform.runLater(() -> {
                        if (success) {
                            // Route to the Active Room
                            mainScreen.activeBattleId = battle.getBattleId();
                            PlayerDashboardSidebar.pageView = "activeBattleRoom";
                            mainScreen.updateCenter();
                        } else {
                            joinBtn.setText("Join Failed");
                            joinBtn.setStyle(
                                    "-fx-background-color: rgba(239,68,68,0.2); -fx-text-fill: #EF4444; -fx-border-color: #EF4444;");
                        }
                    });
                }).start();
            });
        }

        content.getChildren().addAll(topRow, SizedBox.height(5), typeBox, financialBox, progBox, vSpacer, joinBtn);
        cardBase.getChildren().addAll(bgImg, darkOverlay, content);
        return cardBase;
    }

    // --- HOST DIALOG ---
    private void openHostBattleDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Host Wager Battle");

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setPrefWidth(400);
        root.setStyle(
                "-fx-background-color: #0B0F19; -fx-border-color: rgba(139,92,246,0.3); -fx-border-width: 1; -fx-border-radius: 8;");

        Text title = new Text("Configure Arena Settings");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        ComboBox<String> modeBox = createStyledDropdown("Select Mode", "1V1", "2V2", "4V4");
        modeBox.setValue("1V1");
        modeBox.setPrefWidth(350);

        ComboBox<String> formatBox = createStyledDropdown("Select Format", "BO1", "BO3");
        formatBox.setValue("BO1");
        formatBox.setPrefWidth(350);

        VBox feeWrapper = new VBox(5);
        Text feeLbl = new Text("Entry Fee (Coins)");
        feeLbl.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        feeLbl.setFont(Font.font("Arial", 12));
        TextField feeField = new TextField("50");
        HBox feeInput = GamerVaultStyles.createStyledInput(feeField, GamerVaultStyles.ACCENT_PURPLE);
        feeInput.setPrefHeight(45);
        feeWrapper.getChildren().addAll(feeLbl, feeInput);

        root.getChildren().addAll(title, modeBox, formatBox, feeWrapper);

        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #0B0F19;");

        Node okBtn = dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (okBtn instanceof Button) {
            GamerVaultStyles.applyGradientButton((Button) okBtn, GamerVaultStyles.ACCENT_PURPLE,
                    GamerVaultStyles.ACCENT_PURPLE_DARK, "white");
        }
        Node cancelBtn = dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelBtn instanceof Button) {
            GamerVaultStyles.applyGhostButton((Button) cancelBtn);
        }

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    double fee = Double.parseDouble(feeField.getText().trim());
                    String mode = modeBox.getValue();
                    String format = formatBox.getValue();

                    new Thread(() -> {
                        String newBattleId = BattleController.hostBattle(mode, format, fee);
                        Platform.runLater(() -> {
                            if (newBattleId != null) {
                                // Route to the Active Room
                                mainScreen.activeBattleId = newBattleId;
                                PlayerDashboardSidebar.pageView = "activeBattleRoom";
                                mainScreen.updateCenter();
                            } else {
                                // Handle error (e.g., show an Alert)
                            }
                        });
                    }).start();
                } catch (NumberFormatException ex) {
                    // Invalid fee
                }
            }
        });
    }

    // --- UTILS ---
    private ComboBox<String> createStyledDropdown(String prompt, String... items) {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(items);
        combo.setPromptText(prompt);
        combo.setPrefHeight(40);
        combo.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: white; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8;");
        return combo;
    }
}