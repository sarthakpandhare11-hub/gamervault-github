package com.example.view.player;

import com.example.controller.AuthController;
import com.example.controller.player.BattleRoomController;
import com.example.keys.BattleFirebaseKeys;
import com.example.model.player.BattleModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class ActiveBattleRoomScreen {

    private PlayerMainScreen mainScreen;
    private BattleRoomController roomController;
    private String currentBattleId;

    // Dynamic UI Sections
    private VBox mainContainer;
    private HBox rosterBox;
    private StackPane vaultBox;
    private VBox evidenceDeskBox;

    private Label statusLabel;

    public BorderPane startBattleRoomScreen(PlayerMainScreen mainScreen, String battleId) {
        this.mainScreen = mainScreen;
        this.currentBattleId = battleId;
        this.roomController = new BattleRoomController();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        // Top Navigation
        Button backBtn = new Button("← Leave Lobby");
        GamerVaultStyles.applyGhostButton(backBtn);
        backBtn.setOnAction(e -> {
            roomController.stopListening();
            PlayerDashboardSidebar.pageView = "battleArena";
            mainScreen.updateCenter();
        });

        mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20, 40, 40, 40));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        mainContainer.getChildren().add(backBtn);

        root.setCenter(mainContainer);

        // Start listening to the database in real-time
        roomController.listenToBattle(currentBattleId, this::updateUI);

        return root;
    }

    /**
     * This method is called automatically EVERY TIME the Firebase document changes.
     */
    private void updateUI(BattleModel battle) {
        Platform.runLater(() -> {
            // Clear old UI (except the back button)
            if (mainContainer.getChildren().size() > 1) {
                mainContainer.getChildren().remove(1, mainContainer.getChildren().size());
            }

            // 1. Header & Status
            VBox header = new VBox(5);
            header.setAlignment(Pos.CENTER);
            Text title = new Text("Arena: " + battle.getMode() + " (" + battle.getFormat() + ")");
            title.setFont(Font.font("Poppins", FontWeight.BOLD, 32));
            title.setFill(Color.WHITE);

            statusLabel = new Label("STATUS: " + battle.getStatus());
            statusLabel.setStyle("-fx-text-fill: " + getStatusColor(battle.getStatus())
                    + "; -fx-font-weight: bold; -fx-font-size: 16px;");

            header.getChildren().addAll(title, statusLabel);

            // 2. The Roster (Team A vs Team B)
            rosterBox = createRosterBox(battle);

            // 3. The Vault (Room Credentials)
            vaultBox = createTheVault(battle);

            // 4. Evidence Desk (Only visible during/after match)
            evidenceDeskBox = createEvidenceDesk(battle);

            mainContainer.getChildren().addAll(header, SizedBox.height(10), rosterBox, vaultBox, evidenceDeskBox);
        });
    }

    private HBox createRosterBox(BattleModel battle) {
        HBox roster = new HBox(40);
        roster.setAlignment(Pos.CENTER);

        VBox teamA = createTeamColumn("TEAM A", battle, "A", GamerVaultStyles.ACCENT_CYAN);
        Text vs = new Text("VS");
        vs.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        vs.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        VBox teamB = createTeamColumn("TEAM B", battle, "B", GamerVaultStyles.ACCENT_ORANGE);

        roster.getChildren().addAll(teamA, vs, teamB);
        return roster;
    }

    private VBox createTeamColumn(String teamName, BattleModel battle, String targetSide, String accentColor) {
        VBox col = new VBox(10);
        col.setAlignment(Pos.TOP_CENTER);
        col.setPrefWidth(200);

        Text tName = new Text(teamName);
        tName.setFill(Color.web(accentColor));
        tName.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        col.getChildren().add(tName);

        // Calculate how many slots are needed for this side based on mode
        int slotsPerSide = battle.getMaxParticipants() / 2;
        int currentCount = 0;

        // Render filled slots
        if (battle.getParticipants() != null) {
            for (String userId : battle.getParticipants().keySet()) {
                if (battle.getParticipants().get(userId).equals(targetSide)) {
                    col.getChildren().add(createPlayerSlot(userId, true));
                    currentCount++;
                }
            }
        }

        // Render empty (waiting) slots
        while (currentCount < slotsPerSide) {
            col.getChildren().add(createPlayerSlot("Waiting...", false));
            currentCount++;
        }

        return col;
    }

    private StackPane createPlayerSlot(String name, boolean isFilled) {
        StackPane slot = new StackPane();
        slot.setPrefHeight(45);
        slot.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8;");

        Text txt = new Text(isFilled ? "Player Joined" : "Waiting for player...");
        txt.setFill(Color.web(isFilled ? "#FFFFFF" : GamerVaultStyles.TEXT_MUTED));
        txt.setFont(Font.font("Arial", isFilled ? FontWeight.BOLD : FontWeight.NORMAL, 14));

        if (!isFilled)
            GamerVaultAnimations.pulseGlow(txt, 2.0); // Pulsing animation for empty slots

        slot.getChildren().add(txt);
        return slot;
    }

    /**
     * THE VAULT: Secures room ID/Password until lobby is full.
     */
    private StackPane createTheVault(BattleModel battle) {
        StackPane vault = new StackPane();
        vault.setPadding(new Insets(30));
        vault.setPrefWidth(500);
        vault.setMaxWidth(500);
        GamerVaultStyles.applyGlassCard(vault);

        boolean isLockedState = battle.getStatus().equals(BattleFirebaseKeys.STATUS_OPEN)
                || battle.getStatus().equals("DRAFT");

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);

        if (isLockedState) {
            // Blurred State
            Text icon = new Text("🔒");
            icon.setFont(Font.font(48));
            Text msg = new Text("The Vault is Locked");
            msg.setFill(Color.WHITE);
            msg.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            Text sub = new Text("BGMI Room Credentials will be revealed here once the lobby is fully populated.");
            sub.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
            content.getChildren().addAll(icon, msg, sub);

            vault.setEffect(new GaussianBlur(5)); // Slight blur
            vault.getChildren().add(content);
        } else {
            // Revealed State
            vault.setEffect(null);
            vault.setStyle(
                    "-fx-background-color: rgba(16, 185, 129, 0.1); -fx-border-color: #10B981; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12;");

            Text msg = new Text("Vault Unlocked! Proceed to BGMI.");
            msg.setFill(Color.web(GamerVaultStyles.ACCENT_GREEN));
            msg.setFont(Font.font("Arial", FontWeight.BOLD, 18));

            HBox idBox = new HBox(15);
            idBox.setAlignment(Pos.CENTER);
            Label idLbl = new Label("Room ID: " + (battle.getRoomId() != null ? battle.getRoomId() : "1234567"));
            idLbl.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

            Button copyIdBtn = new Button("Copy");
            GamerVaultStyles.applyGhostButton(copyIdBtn);
            copyIdBtn.setOnAction(e -> copyToClipboard(idLbl.getText().replace("Room ID: ", "")));

            idBox.getChildren().addAll(idLbl, copyIdBtn);

            HBox passBox = new HBox(15);
            passBox.setAlignment(Pos.CENTER);
            Label passLbl = new Label(
                    "Password: " + (battle.getRoomPassword() != null ? battle.getRoomPassword() : "admin123"));
            passLbl.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

            Button copyPassBtn = new Button("Copy");
            GamerVaultStyles.applyGhostButton(copyPassBtn);
            copyPassBtn.setOnAction(e -> copyToClipboard(passLbl.getText().replace("Password: ", "")));

            passBox.getChildren().addAll(passLbl, copyPassBtn);

            content.getChildren().addAll(msg, idBox, passBox);
            vault.getChildren().add(content);
            GamerVaultAnimations.pulseGlow(vault, 3.0);
        }

        return vault;
    }

    private VBox createEvidenceDesk(BattleModel battle) {
        VBox desk = new VBox(15);
        desk.setAlignment(Pos.CENTER);
        desk.setPadding(new Insets(20));

        // Hide evidence desk if the match hasn't even started yet
        if (battle.getStatus().equals(BattleFirebaseKeys.STATUS_OPEN)) {
            desk.setVisible(false);
            return desk;
        }

        Text title = new Text("Evidence Desk");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        HBox roundBox = new HBox(20);
        roundBox.setAlignment(Pos.CENTER);

        // Example: If BO1, show 1 upload box. If BO3, show 3.
        int totalRounds = battle.getFormat().equals("BO3") ? 3 : 1;

        for (int i = 1; i <= totalRounds; i++) {
            final int roundNum = i;
            VBox uploadBox = new VBox(10);
            uploadBox.setAlignment(Pos.CENTER);
            uploadBox.setPadding(new Insets(20));
            GamerVaultStyles.applyGlassCard(uploadBox);

            Text rLbl = new Text("Round " + i);
            rLbl.setFill(Color.WHITE);
            rLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));

            Button winBtn = new Button("Upload Victory 🏆");
            winBtn.setStyle(
                    "-fx-background-color: rgba(16, 185, 129, 0.2); -fx-text-fill: #10B981; -fx-border-color: #10B981; -fx-border-radius: 6; -fx-cursor: hand;");

            Button lossBtn = new Button("Upload Defeat 💀");
            lossBtn.setStyle(
                    "-fx-background-color: rgba(239, 68, 68, 0.2); -fx-text-fill: #EF4444; -fx-border-color: #EF4444; -fx-border-radius: 6; -fx-cursor: hand;");

            // Example Action: Opens file picker and calls Controller
            winBtn.setOnAction(e -> {
                winBtn.setText("Analyzing...");
                // Note: Implement FileChooser here as done in UploadMatchScreen
                // roomController.submitRoundEvidence(battle, roundNum, "A", "WIN", file,
                // success -> { ... });
            });

            uploadBox.getChildren().addAll(rLbl, winBtn, lossBtn);
            roundBox.getChildren().add(uploadBox);
        }

        desk.getChildren().addAll(title, roundBox);
        return desk;
    }

    private void copyToClipboard(String text) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    private String getStatusColor(String status) {
        switch (status) {
            case BattleFirebaseKeys.STATUS_OPEN:
                return "#F59E0B"; // Orange
            case BattleFirebaseKeys.STATUS_LOCKED:
                return "#10B981"; // Green
            case BattleFirebaseKeys.STATUS_IN_PROGRESS:
                return "#3B82F6"; // Blue
            case BattleFirebaseKeys.STATUS_COMPLETED:
                return GamerVaultStyles.ACCENT_PURPLE_LIGHT;
            default:
                return "#9CA3AF";
        }
    }
}