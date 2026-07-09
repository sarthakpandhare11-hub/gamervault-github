package com.example.view.player;

import java.io.File;

import com.example.controller.AuthController;
import com.example.controller.player.BattleRoomController;
import com.example.dao.BattleDao;
import com.example.keys.BattleFirebaseKeys;
import com.example.model.player.BattleModel;
import com.example.model.player.RoundModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

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
        HBox topNav = new HBox(15);
        topNav.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("← Back to Arena");
        GamerVaultStyles.applyGhostButton(backBtn);
        backBtn.setOnAction(e -> {
            roomController.stopListening();
            PlayerDashboardSidebar.pageView = "battleArena";
            mainScreen.updateCenter();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button reportBtn = new Button("🚨 Report Issue");
        reportBtn.setStyle(
                "-fx-background-color: rgba(239, 68, 68, 0.1); -fx-text-fill: #EF4444; -fx-border-color: rgba(239, 68, 68, 0.3); -fx-border-radius: 6; -fx-cursor: hand;");
        reportBtn.setOnAction(e -> {
            Dialog<ButtonType> reportDialog = new Dialog<>();
            reportDialog.setTitle("Report Match Issue");

            VBox formRoot = new VBox(15);
            formRoot.setPadding(new Insets(20));
            formRoot.setStyle(
                    "-fx-background-color: #0B0F19; -fx-border-color: #EF4444; -fx-border-width: 1; -fx-border-radius: 8;");

            Text headerTxt = new Text("What is the issue?");
            headerTxt.setFill(Color.WHITE);
            headerTxt.setFont(Font.font("Arial", FontWeight.BOLD, 18));

            ComboBox<String> issueType = new ComboBox<>();
            issueType.getItems().addAll(
                    "Opponent Uploaded Fake Evidence",
                    "Opponent Used Hacks/Cheats",
                    "Opponent Refusing to Start Match",
                    "Toxic Behavior / Harassment",
                    "Other");
            issueType.setPromptText("Select a reason...");
            issueType.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: white; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 6;");
            issueType.setPrefWidth(300);

            TextArea detailArea = new TextArea();
            detailArea.setPromptText("Provide specific details for the admin review...");
            detailArea.setPrefRowCount(4);
            detailArea.setStyle("-fx-control-inner-background: #111827; -fx-text-fill: white;");

            formRoot.getChildren().addAll(headerTxt, issueType, detailArea);
            reportDialog.getDialogPane().setContent(formRoot);
            reportDialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY,
                    ButtonType.CANCEL);
            reportDialog.getDialogPane().setStyle("-fx-background-color: #0B0F19;");

            // Style buttons
            Node submitBtnNode = reportDialog.getDialogPane().lookupButton(ButtonType.APPLY);
            if (submitBtnNode instanceof Button) {
                ((Button) submitBtnNode).setText("Submit Report");
                ((Button) submitBtnNode)
                        .setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold;");
            }
            Node cancelBtnNode = reportDialog.getDialogPane().lookupButton(ButtonType.CANCEL);
            if (cancelBtnNode instanceof Button)
                GamerVaultStyles.applyGhostButton((Button) cancelBtnNode);

            reportDialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.APPLY) {
                    new Thread(() -> {
                        // In Phase 2: Save to a "Disputes" collection. For now, freeze the room.
                        BattleDao.updateBattleStatus(currentBattleId,
                                BattleFirebaseKeys.STATUS_DISPUTED);
                    }).start();
                }
            });
        });

        topNav.getChildren().addAll(backBtn, spacer, reportBtn);

        mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20, 40, 40, 40));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        mainContainer.getChildren().add(topNav); // Add the new Top Nav

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

            boolean isActiveMatch = battle.getStatus().equals(BattleFirebaseKeys.STATUS_LOCKED) ||
                    battle.getStatus().equals(BattleFirebaseKeys.STATUS_IN_PROGRESS);

            long startTime = battle.getLockedAt() > 0 ? battle.getLockedAt() : battle.getCreatedAt();
            if (isActiveMatch && System.currentTimeMillis() > startTime + (45 * 60 * 1000)) {
                new Thread(() -> BattleRoomController.evaluateRound(battle, 0)).start();
                return; // Let the screen refresh on the next snapshot once evaluated
            }

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

        if (battle.getStatus().equals(BattleFirebaseKeys.STATUS_OPEN) || battle.getStatus().equals("DRAFT")) {
            desk.setVisible(false);
            return desk;
        }

        Text title = new Text("Evidence Desk");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        // IF CAUGHT IN FRAUD / DISPUTE
        if (battle.getStatus().equals(BattleFirebaseKeys.STATUS_DISPUTED)) {
            VBox disputeBox = new VBox(10);
            disputeBox.setAlignment(Pos.CENTER);
            disputeBox.setPadding(new Insets(20));
            disputeBox.setStyle(
                    "-fx-background-color: rgba(239, 68, 68, 0.1); -fx-border-color: #EF4444; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12;");

            Text dTitle = new Text("🚨 MATCH DISPUTED");
            dTitle.setFill(Color.web("#EF4444"));
            dTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));

            Text dSub = new Text(
                    "Conflicting evidence detected. An admin is currently reviewing the uploaded screenshots.");
            dSub.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

            disputeBox.getChildren().addAll(dTitle, dSub);
            desk.getChildren().addAll(title, disputeBox);
            return desk;
        }

        HBox roundBox = new HBox(20);
        roundBox.setAlignment(Pos.CENTER);

        int totalRounds = battle.getFormat().equals("BO3") ? 3 : 1;
        String currentUserId = AuthController.currentUser.getUserId();
        String myTeamSide = battle.getParticipants().get(currentUserId);

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

            // Check if this user already uploaded for this round
            RoundModel currentRound = battle.getRounds().size() >= i
                    ? battle.getRounds().get(i - 1)
                    : null;
            boolean alreadySubmitted = false;
            if (currentRound != null) {
                if (myTeamSide.equals("A") && currentRound.getTeamAClaimedOutcome() != null)
                    alreadySubmitted = true;
                if (myTeamSide.equals("B") && currentRound.getTeamBClaimedOutcome() != null)
                    alreadySubmitted = true;
            }

            if (alreadySubmitted) {
                Text submittedTxt = new Text("Evidence Submitted ✓");
                submittedTxt.setFill(Color.web(GamerVaultStyles.ACCENT_CYAN));
                uploadBox.getChildren().addAll(rLbl, submittedTxt);
            } else {
                winBtn.setOnAction(e -> handleEvidenceUpload(winBtn, battle, roundNum, myTeamSide, "WIN"));
                lossBtn.setOnAction(e -> handleEvidenceUpload(lossBtn, battle, roundNum, myTeamSide, "LOSS"));
                uploadBox.getChildren().addAll(rLbl, winBtn, lossBtn);
            }

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

    private void handleEvidenceUpload(Button btn, BattleModel battle, int roundNum, String teamSide,
            String claimedOutcome) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(btn.getScene().getWindow());

        if (file == null)
            return;

        btn.setDisable(true);
        btn.setText("Analyzing...");

        roomController.submitRoundEvidence(battle, roundNum, teamSide, claimedOutcome, file, success -> {
            Platform.runLater(() -> {
                btn.setText(success ? "Submitted ✓" : "Failed — retry");
                btn.setDisable(success); // Keep disabled if successful
            });
        });
    }
}