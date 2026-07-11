// package com.example.view.player;

// import java.io.File;
// import java.util.UUID;

// import com.example.controller.AuthController;
// import com.example.controller.player.BattleRoomController;
// import com.example.dao.BattleDao;
// import com.example.dao.DisputeChatDao;
// import com.example.keys.BattleFirebaseKeys;
// import com.example.model.player.BattleModel;
// import com.example.model.player.ChatMessageModel;
// import com.example.model.player.RoundModel;
// import com.example.view.util.GamerVaultAnimations;
// import com.example.view.util.GamerVaultStyles;
// import com.example.view.util.SizedBox;

// import javafx.application.Platform;
// import javafx.geometry.Insets;
// import javafx.geometry.Pos;
// import javafx.scene.Node;
// import javafx.scene.control.Button;
// import javafx.scene.control.ButtonType;
// import javafx.scene.control.ComboBox;
// import javafx.scene.control.Dialog;
// import javafx.scene.control.Label;
// import javafx.scene.control.TextArea;
// import javafx.scene.effect.GaussianBlur;
// import javafx.scene.input.Clipboard;
// import javafx.scene.input.ClipboardContent;
// import javafx.scene.layout.*;
// import javafx.scene.paint.Color;
// import javafx.scene.text.Font;
// import javafx.scene.text.FontWeight;
// import javafx.scene.text.Text;
// import javafx.stage.FileChooser;

// public class ActiveBattleRoomScreen {

//     private PlayerMainScreen mainScreen;
//     private BattleRoomController roomController;
//     private String currentBattleId;

//     // Dynamic UI Sections
//     private VBox mainContainer;
//     private HBox rosterBox;
//     private StackPane vaultBox;
//     private VBox evidenceDeskBox;

//     private Label statusLabel;

//     public BorderPane startBattleRoomScreen(PlayerMainScreen mainScreen, String battleId) {
//         this.mainScreen = mainScreen;
//         this.currentBattleId = battleId;
//         this.roomController = new BattleRoomController();

//         BorderPane root = new BorderPane();
//         root.setStyle("-fx-background-color: transparent;");

//         // Top Navigation
//         HBox topNav = new HBox(15);
//         topNav.setAlignment(Pos.CENTER_LEFT);

//         Button backBtn = new Button("← Back to Arena");
//         GamerVaultStyles.applyGhostButton(backBtn);
//         backBtn.setOnAction(e -> {
//             roomController.stopListening();
//             PlayerDashboardSidebar.pageView = "battleArena";
//             mainScreen.updateCenter();
//         });

//         Region spacer = new Region();
//         HBox.setHgrow(spacer, Priority.ALWAYS);

//         Button reportBtn = new Button("🚨 Report Issue");
//         reportBtn.setStyle(
//                 "-fx-background-color: rgba(239, 68, 68, 0.1); -fx-text-fill: #EF4444; -fx-border-color: rgba(239, 68, 68, 0.3); -fx-border-radius: 6; -fx-cursor: hand;");
//         reportBtn.setOnAction(e -> {
//             Dialog<ButtonType> reportDialog = new Dialog<>();
//             reportDialog.setTitle("Report Match Issue");

//             VBox formRoot = new VBox(15);
//             formRoot.setPadding(new Insets(20));
//             formRoot.setStyle(
//                     "-fx-background-color: #0B0F19; -fx-border-color: #EF4444; -fx-border-width: 1; -fx-border-radius: 8;");

//             Text headerTxt = new Text("What is the issue?");
//             headerTxt.setFill(Color.WHITE);
//             headerTxt.setFont(Font.font("Arial", FontWeight.BOLD, 18));

//             ComboBox<String> issueType = new ComboBox<>();
//             issueType.getItems().addAll(
//                     "Opponent Uploaded Fake Evidence",
//                     "Opponent Used Hacks/Cheats",
//                     "Opponent Refusing to Start Match",
//                     "Toxic Behavior / Harassment",
//                     "Other");
//             issueType.setPromptText("Select a reason...");
//             issueType.setStyle(
//                     "-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: white; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 6;");
//             issueType.setPrefWidth(300);

//             TextArea detailArea = new TextArea();
//             detailArea.setPromptText("Provide specific details for the admin review...");
//             detailArea.setPrefRowCount(4);
//             detailArea.setStyle("-fx-control-inner-background: #111827; -fx-text-fill: white;");

//             formRoot.getChildren().addAll(headerTxt, issueType, detailArea);
//             reportDialog.getDialogPane().setContent(formRoot);
//             reportDialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY,
//                     ButtonType.CANCEL);
//             reportDialog.getDialogPane().setStyle("-fx-background-color: #0B0F19;");

//             // Style buttons
//             Node submitBtnNode = reportDialog.getDialogPane().lookupButton(ButtonType.APPLY);
//             if (submitBtnNode instanceof Button) {
//                 ((Button) submitBtnNode).setText("Submit Report");
//                 ((Button) submitBtnNode)
//                         .setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold;");
//             }
//             Node cancelBtnNode = reportDialog.getDialogPane().lookupButton(ButtonType.CANCEL);
//             if (cancelBtnNode instanceof Button)
//                 GamerVaultStyles.applyGhostButton((Button) cancelBtnNode);

//             reportDialog.showAndWait().ifPresent(response -> {
//                 if (response == ButtonType.APPLY) {
//                     new Thread(() -> {
//                         // In Phase 2: Save to a "Disputes" collection. For now, freeze the room.
//                         BattleDao.updateBattleStatus(currentBattleId,
//                                 BattleFirebaseKeys.STATUS_DISPUTED);
//                     }).start();
//                 }
//             });
//         });

//         topNav.getChildren().addAll(backBtn, spacer, reportBtn);

//         mainContainer = new VBox(20);
//         mainContainer.setPadding(new Insets(20, 40, 40, 40));
//         mainContainer.setAlignment(Pos.TOP_CENTER);

//         mainContainer.getChildren().add(topNav); // Add the new Top Nav

//         root.setCenter(mainContainer);

//         // Start listening to the database in real-time
//         roomController.listenToBattle(currentBattleId, this::updateUI);

//         return root;
//     }

//     /**
//      * This method is called automatically EVERY TIME the Firebase document changes.
//      */
//     private void updateUI(BattleModel battle) {
//         Platform.runLater(() -> {

//             boolean isActiveMatch = battle.getStatus().equals(BattleFirebaseKeys.STATUS_LOCKED) ||
//                     battle.getStatus().equals(BattleFirebaseKeys.STATUS_IN_PROGRESS);

//             long startTime = battle.getLockedAt() > 0 ? battle.getLockedAt() : battle.getCreatedAt();
//             if (isActiveMatch && System.currentTimeMillis() > startTime + (45 * 60 * 1000)) {
//                 new Thread(() -> BattleRoomController.evaluateRound(battle, 0)).start();
//                 return; // Let the screen refresh on the next snapshot once evaluated
//             }

//             // Clear old UI (except the back button)
//             if (mainContainer.getChildren().size() > 1) {
//                 mainContainer.getChildren().remove(1, mainContainer.getChildren().size());
//             }

//             // 1. Header & Status
//             VBox header = new VBox(5);
//             header.setAlignment(Pos.CENTER);
//             Text title = new Text("Arena: " + battle.getMode() + " (" + battle.getFormat() + ")");
//             title.setFont(Font.font("Poppins", FontWeight.BOLD, 32));
//             title.setFill(Color.WHITE);

//             statusLabel = new Label("STATUS: " + battle.getStatus());
//             statusLabel.setStyle("-fx-text-fill: " + getStatusColor(battle.getStatus())
//                     + "; -fx-font-weight: bold; -fx-font-size: 16px;");

//             header.getChildren().addAll(title, statusLabel);

//             // 2. The Roster (Team A vs Team B)
//             rosterBox = createRosterBox(battle);

//             // 3. The Vault (Room Credentials)
//             vaultBox = createTheVault(battle);

//             // 4. Evidence Desk (Only visible during/after match)
//             evidenceDeskBox = createEvidenceDesk(battle);

//             mainContainer.getChildren().addAll(header, SizedBox.height(10), rosterBox, vaultBox, evidenceDeskBox);
//         });
//     }

//     private HBox createRosterBox(BattleModel battle) {
//         HBox roster = new HBox(40);
//         roster.setAlignment(Pos.CENTER);

//         VBox teamA = createTeamColumn("TEAM A", battle, "A", GamerVaultStyles.ACCENT_CYAN);
//         Text vs = new Text("VS");
//         vs.setFont(Font.font("Arial", FontWeight.BOLD, 24));
//         vs.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
//         VBox teamB = createTeamColumn("TEAM B", battle, "B", GamerVaultStyles.ACCENT_ORANGE);

//         roster.getChildren().addAll(teamA, vs, teamB);
//         return roster;
//     }

//     private VBox createTeamColumn(String teamName, BattleModel battle, String targetSide, String accentColor) {
//         VBox col = new VBox(10);
//         col.setAlignment(Pos.TOP_CENTER);
//         col.setPrefWidth(200);

//         Text tName = new Text(teamName);
//         tName.setFill(Color.web(accentColor));
//         tName.setFont(Font.font("Arial", FontWeight.BOLD, 18));
//         col.getChildren().add(tName);

//         // Calculate how many slots are needed for this side based on mode
//         int slotsPerSide = battle.getMaxParticipants() / 2;
//         int currentCount = 0;

//         // Render filled slots
//         if (battle.getParticipants() != null) {
//             for (String userId : battle.getParticipants().keySet()) {
//                 if (battle.getParticipants().get(userId).equals(targetSide)) {
//                     col.getChildren().add(createPlayerSlot(userId, true));
//                     currentCount++;
//                 }
//             }
//         }

//         // Render empty (waiting) slots
//         while (currentCount < slotsPerSide) {
//             col.getChildren().add(createPlayerSlot("Waiting...", false));
//             currentCount++;
//         }

//         return col;
//     }

//     private StackPane createPlayerSlot(String name, boolean isFilled) {
//         StackPane slot = new StackPane();
//         slot.setPrefHeight(45);
//         slot.setStyle(
//                 "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8;");

//         Text txt = new Text(isFilled ? "Player Joined" : "Waiting for player...");
//         txt.setFill(Color.web(isFilled ? "#FFFFFF" : GamerVaultStyles.TEXT_MUTED));
//         txt.setFont(Font.font("Arial", isFilled ? FontWeight.BOLD : FontWeight.NORMAL, 14));

//         if (!isFilled)
//             GamerVaultAnimations.pulseGlow(txt, 2.0); // Pulsing animation for empty slots

//         slot.getChildren().add(txt);
//         return slot;
//     }

//     /**
//      * THE VAULT: Secures room ID/Password until lobby is full.
//      */
//     private StackPane createTheVault(BattleModel battle) {
//         StackPane vault = new StackPane();
//         vault.setPadding(new Insets(30));
//         vault.setPrefWidth(500);
//         vault.setMaxWidth(500);
//         GamerVaultStyles.applyGlassCard(vault);

//         boolean isLockedState = battle.getStatus().equals(BattleFirebaseKeys.STATUS_OPEN)
//                 || battle.getStatus().equals("DRAFT");

//         VBox content = new VBox(15);
//         content.setAlignment(Pos.CENTER);

//         if (isLockedState) {
//             // Blurred State
//             Text icon = new Text("🔒");
//             icon.setFont(Font.font(48));
//             Text msg = new Text("The Vault is Locked");
//             msg.setFill(Color.WHITE);
//             msg.setFont(Font.font("Arial", FontWeight.BOLD, 20));
//             Text sub = new Text("BGMI Room Credentials will be revealed here once the lobby is fully populated.");
//             sub.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
//             content.getChildren().addAll(icon, msg, sub);

//             vault.setEffect(new GaussianBlur(5)); // Slight blur
//             vault.getChildren().add(content);
//         } else {
//             // Revealed State
//             vault.setEffect(null);
//             vault.setStyle(
//                     "-fx-background-color: rgba(16, 185, 129, 0.1); -fx-border-color: #10B981; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12;");

//             Text msg = new Text("Vault Unlocked! Proceed to BGMI.");
//             msg.setFill(Color.web(GamerVaultStyles.ACCENT_GREEN));
//             msg.setFont(Font.font("Arial", FontWeight.BOLD, 18));

//             HBox idBox = new HBox(15);
//             idBox.setAlignment(Pos.CENTER);
//             Label idLbl = new Label("Room ID: " + (battle.getRoomId() != null ? battle.getRoomId() : "1234567"));
//             idLbl.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

//             Button copyIdBtn = new Button("Copy");
//             GamerVaultStyles.applyGhostButton(copyIdBtn);
//             copyIdBtn.setOnAction(e -> copyToClipboard(idLbl.getText().replace("Room ID: ", "")));

//             idBox.getChildren().addAll(idLbl, copyIdBtn);

//             HBox passBox = new HBox(15);
//             passBox.setAlignment(Pos.CENTER);
//             Label passLbl = new Label(
//                     "Password: " + (battle.getRoomPassword() != null ? battle.getRoomPassword() : "admin123"));
//             passLbl.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

//             Button copyPassBtn = new Button("Copy");
//             GamerVaultStyles.applyGhostButton(copyPassBtn);
//             copyPassBtn.setOnAction(e -> copyToClipboard(passLbl.getText().replace("Password: ", "")));

//             passBox.getChildren().addAll(passLbl, copyPassBtn);

//             content.getChildren().addAll(msg, idBox, passBox);
//             vault.getChildren().add(content);
//             GamerVaultAnimations.pulseGlow(vault, 3.0);
//         }

//         return vault;
//     }

//     private VBox createEvidenceDesk(BattleModel battle) {

//         if (battle.getStatus().equals(BattleFirebaseKeys.STATUS_DISPUTED)) {
//             return createDisputeChatDesk(battle);
//         }
//         if (battle.getStatus().equals("PENDING_FORFEIT")) {
//             VBox desk = new VBox(15);
//             desk.setAlignment(Pos.CENTER);
//             desk.setPadding(new Insets(30));
//             GamerVaultStyles.applyGlassCard(desk);

//             Text t = new Text("Opponent Timed Out. Validating Forfeit...");
//             t.setFont(Font.font("Arial", FontWeight.BOLD, 18));
//             t.setFill(Color.web(GamerVaultStyles.ACCENT_CYAN));

//             Text sub = new Text("The AI is currently analyzing your evidence to award the victory.");
//             sub.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

//             desk.getChildren().addAll(t, sub);
//             return desk;
//         }

//         if (battle.getStatus().equals("RESULT_PENDING")) {
//             return createResultPendingDesk(battle);
//         }

//         VBox desk = new VBox(15);
//         desk.setAlignment(Pos.CENTER);
//         desk.setPadding(new Insets(20));

//         if (battle.getStatus().equals(BattleFirebaseKeys.STATUS_OPEN) || battle.getStatus().equals("DRAFT")) {
//             desk.setVisible(false);
//             return desk;
//         }

//         Text title = new Text("Evidence Desk");
//         title.setFill(Color.WHITE);
//         title.setFont(Font.font("Arial", FontWeight.BOLD, 22));

//         // IF CAUGHT IN FRAUD / DISPUTE
//         if (battle.getStatus().equals(BattleFirebaseKeys.STATUS_DISPUTED)) {
//             VBox disputeBox = new VBox(10);
//             disputeBox.setAlignment(Pos.CENTER);
//             disputeBox.setPadding(new Insets(20));
//             disputeBox.setStyle(
//                     "-fx-background-color: rgba(239, 68, 68, 0.1); -fx-border-color: #EF4444; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12;");

//             Text dTitle = new Text("🚨 MATCH DISPUTED");
//             dTitle.setFill(Color.web("#EF4444"));
//             dTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));

//             Text dSub = new Text(
//                     "Conflicting evidence detected. An admin is currently reviewing the uploaded screenshots.");
//             dSub.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

//             disputeBox.getChildren().addAll(dTitle, dSub);
//             desk.getChildren().addAll(title, disputeBox);
//             return desk;
//         }

//         HBox roundBox = new HBox(20);
//         roundBox.setAlignment(Pos.CENTER);

//         int totalRounds = battle.getFormat().equals("BO3") ? 3 : 1;
//         String currentUserId = AuthController.currentUser.getUserId();
//         String myTeamSide = battle.getParticipants().get(currentUserId);

//         for (int i = 1; i <= totalRounds; i++) {
//             final int roundNum = i;
//             VBox uploadBox = new VBox(10);
//             uploadBox.setAlignment(Pos.CENTER);
//             uploadBox.setPadding(new Insets(20));
//             GamerVaultStyles.applyGlassCard(uploadBox);

//             Text rLbl = new Text("Round " + i);
//             rLbl.setFill(Color.WHITE);
//             rLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));

//             Button winBtn = new Button("Upload Victory 🏆");
//             winBtn.setStyle(
//                     "-fx-background-color: rgba(16, 185, 129, 0.2); -fx-text-fill: #10B981; -fx-border-color: #10B981; -fx-border-radius: 6; -fx-cursor: hand;");

//             Button lossBtn = new Button("Upload Defeat 💀");
//             lossBtn.setStyle(
//                     "-fx-background-color: rgba(239, 68, 68, 0.2); -fx-text-fill: #EF4444; -fx-border-color: #EF4444; -fx-border-radius: 6; -fx-cursor: hand;");

//             // Check if this user already uploaded for this round
//             RoundModel currentRound = battle.getRounds().size() >= i
//                     ? battle.getRounds().get(i - 1)
//                     : null;
//             boolean alreadySubmitted = false;
//             if (currentRound != null) {
//                 if (myTeamSide.equals("A") && currentRound.getTeamAClaimedOutcome() != null)
//                     alreadySubmitted = true;
//                 if (myTeamSide.equals("B") && currentRound.getTeamBClaimedOutcome() != null)
//                     alreadySubmitted = true;
//             }

//             if (alreadySubmitted) {
//                 Text submittedTxt = new Text("Evidence Submitted ✓");
//                 submittedTxt.setFill(Color.web(GamerVaultStyles.ACCENT_CYAN));
//                 uploadBox.getChildren().addAll(rLbl, submittedTxt);
//             } else {
//                 winBtn.setOnAction(e -> handleEvidenceUpload(winBtn, battle, roundNum, myTeamSide, "WIN"));
//                 lossBtn.setOnAction(e -> handleEvidenceUpload(lossBtn, battle, roundNum, myTeamSide, "LOSS"));
//                 uploadBox.getChildren().addAll(rLbl, winBtn, lossBtn);
//             }

//             roundBox.getChildren().add(uploadBox);
//         }

//         desk.getChildren().addAll(title, roundBox);
//         return desk;
//     }

//     private com.google.cloud.firestore.ListenerRegistration chatListener;

//     private VBox createDisputeChatDesk(BattleModel battle) {
//         VBox desk = new VBox(10);
//         desk.setPadding(new Insets(20));
//         desk.setPrefHeight(400);
//         GamerVaultStyles.applyGlassCard(desk);

//         Text title = new Text("🚨 Admin Dispute Chat");
//         title.setFill(Color.web("#EF4444"));
//         title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

//         Text sub = new Text("An admin has joined the room. Please explain the issue.");
//         sub.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

//         // Chat History Box
//         VBox chatBox = new VBox(10);
//         chatBox.setPadding(new Insets(10));
//         javafx.scene.control.ScrollPane scroller = new javafx.scene.control.ScrollPane(chatBox);
//         GamerVaultStyles.applyStyledScrollPane(scroller);
//         scroller.setPrefHeight(250);
//         scroller.setVvalue(1.0); // Auto-scroll to bottom

//         // Input Area
//         HBox inputBox = new HBox(10);
//         javafx.scene.control.TextField messageField = new javafx.scene.control.TextField();
//         messageField.setPromptText("Type your message to the Admin...");
//         messageField.setPrefWidth(400);
//         messageField.setStyle(
//                 "-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: white; -fx-padding: 10; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 6;");

//         Button sendBtn = new Button("Send");
//         sendBtn.setStyle(
//                 "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");

//         String myUserId = AuthController.currentUser.getUserId();
//         String myTeam = battle.getParticipants().get(myUserId);
//         String senderName = "Team " + (myTeam != null ? myTeam : "Unknown");

//         sendBtn.setOnAction(e -> {
//             if (messageField.getText().trim().isEmpty())
//                 return;
//             ChatMessageModel msg = new ChatMessageModel(
//                     UUID.randomUUID().toString(), myUserId, senderName, messageField.getText().trim(),
//                     System.currentTimeMillis());
//             DisputeChatDao.sendMessage(battle.getBattleId(), msg);
//             messageField.clear();
//         });

//         // Start listening to live messages
//         if (chatListener != null)
//             chatListener.remove();
//         chatListener = DisputeChatDao.listenToChat(battle.getBattleId(), messages -> {
//             Platform.runLater(() -> {
//                 chatBox.getChildren().clear();
//                 for (ChatMessageModel m : messages) {
//                     Text senderTxt = new Text(m.getSenderName() + ": ");
//                     senderTxt.setFill(m.getSenderName().equals("ADMIN") ? Color.web("#F59E0B")
//                             : Color.web(GamerVaultStyles.ACCENT_CYAN));
//                     senderTxt.setFont(Font.font("Arial", FontWeight.BOLD, 14));

//                     Text msgTxt = new Text(m.getText());
//                     msgTxt.setFill(Color.WHITE);
//                     msgTxt.setWrappingWidth(380);

//                     HBox msgRow = new HBox(senderTxt, msgTxt);
//                     chatBox.getChildren().add(msgRow);
//                 }
//                 scroller.setVvalue(1.0); // Keep scrolled to bottom
//             });
//         });

//         inputBox.getChildren().addAll(messageField, sendBtn);
//         desk.getChildren().addAll(title, sub, scroller, inputBox);
//         return desk;
//     }

//     private VBox createResultPendingDesk(BattleModel battle) {
//         VBox desk = new VBox(15);
//         desk.setAlignment(Pos.CENTER);
//         desk.setPadding(new Insets(20));
//         GamerVaultStyles.applyGlassCard(desk);

//         Text title = new Text("AI Verification Complete");
//         title.setFill(Color.WHITE);
//         title.setFont(Font.font("Arial", FontWeight.BOLD, 22));

//         // Get the latest round
//         com.example.model.player.RoundModel currentRound = battle.getRounds().get(battle.getRounds().size() - 1);
//         String myTeamSide = battle.getParticipants().get(AuthController.currentUser.getUserId());
//         boolean isWinner = myTeamSide != null && myTeamSide.equals(currentRound.getWinningTeam());

//         Text resultText = new Text("The AI has declared Team " + currentRound.getWinningTeam() + " as the winner.");
//         resultText.setFill(Color.web(GamerVaultStyles.ACCENT_CYAN));
//         resultText.setFont(Font.font("Arial", FontWeight.BOLD, 18));

//         Text subText = new Text(isWinner ? "Congratulations! Please accept to receive your payout."
//                 : "Better luck next time. Please accept to finalize the match.");
//         subText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

//         HBox actions = new HBox(20);
//         actions.setAlignment(Pos.CENTER);
//         actions.setPadding(new Insets(15, 0, 0, 0));

//         Button acceptBtn = new Button("Accept Result");
//         acceptBtn.setStyle(
//                 "-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 20;");

//         Button disputeBtn = new Button("Report / Dispute");
//         disputeBtn.setStyle(
//                 "-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-border-color: #EF4444; -fx-border-radius: 6; -fx-cursor: hand; -fx-padding: 10 20;");

//         // Action: Accept
//         acceptBtn.setOnAction(e -> {
//             acceptBtn.setDisable(true);
//             disputeBtn.setDisable(true);
//             acceptBtn.setText("Processing...");
//             new Thread(() -> {
//                 // If accepted, immediately process payout
//                 BattleDao.resolveBattleFinancials(battle.getBattleId(),
//                         BattleFirebaseKeys.STATUS_COMPLETED, currentRound.getWinningTeam(), false);
//             }).start();
//         });

//         // Action: Dispute
//         disputeBtn.setOnAction(e -> {
//            acceptBtn.setDisable(true);
//             disputeBtn.setDisable(true);
//             disputeBtn.setText("Escalating...");
//             new Thread(() -> {
//                 // Shift to disputed to freeze funds and summon admin properly
//                 BattleDao.escalateToDispute(battle.getBattleId(), battle.getRounds().size() - 1);
//             }).start();
//         });

//         actions.getChildren().addAll(acceptBtn, disputeBtn);
//         desk.getChildren().addAll(title, resultText, subText, actions);
//         return desk;
//     }

//     private void copyToClipboard(String text) {
//         Clipboard clipboard = Clipboard.getSystemClipboard();
//         ClipboardContent content = new ClipboardContent();
//         content.putString(text);
//         clipboard.setContent(content);
//     }

//     private String getStatusColor(String status) {
//         switch (status) {
//             case BattleFirebaseKeys.STATUS_OPEN:
//                 return "#F59E0B"; // Orange
//             case BattleFirebaseKeys.STATUS_LOCKED:
//                 return "#10B981"; // Green
//             case BattleFirebaseKeys.STATUS_IN_PROGRESS:
//                 return "#3B82F6"; // Blue
//             case BattleFirebaseKeys.STATUS_COMPLETED:
//                 return GamerVaultStyles.ACCENT_PURPLE_LIGHT;
//             default:
//                 return "#9CA3AF";
//         }
//     }

//     private void handleEvidenceUpload(Button btn, BattleModel battle, int roundNum, String teamSide,
//             String claimedOutcome) {
//         FileChooser chooser = new FileChooser();
//         chooser.getExtensionFilters()
//                 .add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
//         File file = chooser.showOpenDialog(btn.getScene().getWindow());

//         if (file == null)
//             return;

//         btn.setDisable(true);
//         btn.setText("Analyzing...");

//         roomController.submitRoundEvidence(battle, roundNum, teamSide, claimedOutcome, file, success -> {
//             Platform.runLater(() -> {
//                 btn.setText(success ? "Submitted ✓" : "Failed — retry");
//                 btn.setDisable(success); // Keep disabled if successful
//             });
//         });
//     }
// }

package com.example.view.player;

import java.io.File;
import java.util.UUID;

import com.example.controller.AuthController;
import com.example.controller.player.BattleRoomController;
import com.example.dao.BattleDao;
import com.example.dao.DisputeChatDao;
import com.example.keys.BattleFirebaseKeys;
import com.example.model.player.BattleModel;
import com.example.model.player.ChatMessageModel;
import com.example.model.player.RoundModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;
import com.google.cloud.firestore.ListenerRegistration;

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
    private ListenerRegistration chatListener;

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
            if (chatListener != null)
                chatListener.remove(); // Prevent memory leaks
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
            reportDialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
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
                        // CRITICAL FIX: Ensure BOTH the Battle and the active Round are marked as
                        // disputed
                        BattleModel freshBattle = BattleDao.getBattleById(currentBattleId);
                        if (freshBattle != null && freshBattle.getRounds() != null
                                && !freshBattle.getRounds().isEmpty()) {
                            int activeRound = freshBattle.getRounds().size() - 1;
                            BattleDao.escalateToDispute(currentBattleId, activeRound);
                        }
                    }).start();
                }
            });
        });

        topNav.getChildren().addAll(backBtn, spacer, reportBtn);

        mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20, 40, 40, 40));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        mainContainer.getChildren().add(topNav);

        root.setCenter(mainContainer);

        // Start listening to the database in real-time
        roomController.listenToBattle(currentBattleId, this::updateUI);

        return root;
    }

    private void updateUI(BattleModel battle) {
        Platform.runLater(() -> {
            boolean isActiveMatch = battle.getStatus().equals(BattleFirebaseKeys.STATUS_LOCKED) ||
                    battle.getStatus().equals(BattleFirebaseKeys.STATUS_IN_PROGRESS);

            long startTime = battle.getLockedAt() > 0 ? battle.getLockedAt() : battle.getCreatedAt();

            // Dynamic round evaluation check
            if (isActiveMatch && System.currentTimeMillis() > startTime + (45 * 60 * 1000)) {
                int currentRoundIndex = (battle.getRounds() != null && !battle.getRounds().isEmpty())
                        ? battle.getRounds().size() - 1
                        : 0;
                new Thread(() -> BattleRoomController.evaluateRound(battle, currentRoundIndex)).start();
                return;
            }

            // Clear old UI (except the top nav)
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

            // 4. Evidence Desk
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

        int slotsPerSide = battle.getMaxParticipants() / 2;
        int currentCount = 0;

        if (battle.getParticipants() != null) {
            for (String userId : battle.getParticipants().keySet()) {
                if (battle.getParticipants().get(userId).equals(targetSide)) {
                    col.getChildren().add(createPlayerSlot(userId, true));
                    currentCount++;
                }
            }
        }

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
            GamerVaultAnimations.pulseGlow(txt, 2.0);

        slot.getChildren().add(txt);
        return slot;
    }

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
            Text icon = new Text("🔒");
            icon.setFont(Font.font(48));
            Text msg = new Text("The Vault is Locked");
            msg.setFill(Color.WHITE);
            msg.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            Text sub = new Text("BGMI Room Credentials will be revealed here once the lobby is fully populated.");
            sub.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
            content.getChildren().addAll(icon, msg, sub);

            vault.setEffect(new GaussianBlur(5));
            vault.getChildren().add(content);
        } else {
            vault.setEffect(null);
            vault.setStyle(
                    "-fx-background-color: rgba(16, 185, 129, 0.1); -fx-border-color: #10B981; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12;");

            Text msg = new Text("Vault Unlocked! Proceed to Match.");
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

        // State 1: Finished or Cancelled
        if (battle.getStatus().equals(BattleFirebaseKeys.STATUS_COMPLETED)
                || battle.getStatus().equals(BattleFirebaseKeys.STATUS_CANCELLED)) {
            return createMatchFinishedDesk(battle);
        }
        // State 2: Disputed
        if (battle.getStatus().equals(BattleFirebaseKeys.STATUS_DISPUTED)) {
            return createDisputeChatDesk(battle);
        }
        // State 3: Forfeit Check
        if (battle.getStatus().equals("PENDING_FORFEIT")) {
            VBox desk = new VBox(15);
            desk.setAlignment(Pos.CENTER);
            desk.setPadding(new Insets(30));
            GamerVaultStyles.applyGlassCard(desk);

            Text t = new Text("Opponent Timed Out. Validating Forfeit...");
            t.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            t.setFill(Color.web(GamerVaultStyles.ACCENT_CYAN));

            Text sub = new Text("The AI is currently analyzing your evidence to award the victory.");
            sub.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

            desk.getChildren().addAll(t, sub);
            return desk;
        }
        // State 4: AI verification complete, waiting for player confirmation
        if (battle.getStatus().equals("RESULT_PENDING")) {
            return createResultPendingDesk(battle);
        }

        // Default State: Open for evidence upload
        VBox desk = new VBox(15);
        desk.setAlignment(Pos.CENTER);
        desk.setPadding(new Insets(20));

        if (battle.getStatus().equals(BattleFirebaseKeys.STATUS_OPEN) || battle.getStatus().equals("DRAFT")) {
            desk.setVisible(false); // Hide entirely if lobby is still open
            return desk;
        }

        Text title = new Text("Evidence Desk");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));

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

            RoundModel currentRound = battle.getRounds().size() >= i ? battle.getRounds().get(i - 1) : null;
            boolean alreadySubmitted = false;
            if (currentRound != null && myTeamSide != null) {
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

    // MISSING METHOD FIX: Renders the final verdict
    private VBox createMatchFinishedDesk(BattleModel battle) {
        VBox desk = new VBox(15);
        desk.setAlignment(Pos.CENTER);
        desk.setPadding(new Insets(30));
        GamerVaultStyles.applyGlassCard(desk);

        boolean isCancelled = battle.getStatus().equals(BattleFirebaseKeys.STATUS_CANCELLED);
        Text title = new Text(isCancelled ? "Match Cancelled" : "Match Completed");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setFill(Color.web(isCancelled ? GamerVaultStyles.TEXT_MUTED : GamerVaultStyles.ACCENT_GREEN));

        String myTeam = battle.getParticipants().get(AuthController.currentUser.getUserId());
        boolean iWon = myTeam != null && myTeam.equals(battle.getOverallWinner());

        String sub = isCancelled ? "Funds have been refunded (80%)."
                : (iWon ? "Victory verified! Winnings credited to your wallet."
                        : "Defeat verified. Better luck next time!");

        Text subText = new Text(sub);
        subText.setFill(Color.WHITE);

        desk.getChildren().addAll(title, subText);
        return desk;
    }

    private VBox createDisputeChatDesk(BattleModel battle) {
        VBox desk = new VBox(10);
        desk.setPadding(new Insets(20));
        desk.setPrefHeight(400);
        GamerVaultStyles.applyGlassCard(desk);

        Text title = new Text("🚨 Admin Dispute Chat");
        title.setFill(Color.web("#EF4444"));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Text sub = new Text("An admin has joined the room. Please explain the issue.");
        sub.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

        VBox chatBox = new VBox(10);
        chatBox.setPadding(new Insets(10));
        javafx.scene.control.ScrollPane scroller = new javafx.scene.control.ScrollPane(chatBox);
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setPrefHeight(250);
        scroller.setVvalue(1.0);

        HBox inputBox = new HBox(10);
        javafx.scene.control.TextField messageField = new javafx.scene.control.TextField();
        messageField.setPromptText("Type your message to the Admin...");
        messageField.setPrefWidth(400);
        messageField.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: white; -fx-padding: 10; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 6;");

        Button sendBtn = new Button("Send");
        sendBtn.setStyle(
                "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");

        String myUserId = AuthController.currentUser.getUserId();
        String myTeam = battle.getParticipants().get(myUserId);
        String senderName = "Team " + (myTeam != null ? myTeam : "Unknown");

        sendBtn.setOnAction(e -> {
            if (messageField.getText().trim().isEmpty())
                return;
            ChatMessageModel msg = new ChatMessageModel(
                    UUID.randomUUID().toString(), myUserId, senderName, messageField.getText().trim(),
                    System.currentTimeMillis());
            DisputeChatDao.sendMessage(battle.getBattleId(), msg);
            messageField.clear();
        });

        if (chatListener != null)
            chatListener.remove();
        chatListener = DisputeChatDao.listenToChat(battle.getBattleId(), messages -> {
            Platform.runLater(() -> {
                chatBox.getChildren().clear();
                for (ChatMessageModel m : messages) {
                    Text senderTxt = new Text(m.getSenderName() + ": ");
                    senderTxt.setFill(m.getSenderName().equals("ADMIN") ? Color.web("#F59E0B")
                            : Color.web(GamerVaultStyles.ACCENT_CYAN));
                    senderTxt.setFont(Font.font("Arial", FontWeight.BOLD, 14));

                    Text msgTxt = new Text(m.getText());
                    msgTxt.setFill(Color.WHITE);
                    msgTxt.setWrappingWidth(380);

                    HBox msgRow = new HBox(senderTxt, msgTxt);
                    chatBox.getChildren().add(msgRow);
                }
                scroller.setVvalue(1.0);
            });
        });

        inputBox.getChildren().addAll(messageField, sendBtn);
        desk.getChildren().addAll(title, sub, scroller, inputBox);
        return desk;
    }

    private VBox createResultPendingDesk(BattleModel battle) {
        VBox desk = new VBox(15);
        desk.setAlignment(Pos.CENTER);
        desk.setPadding(new Insets(20));
        GamerVaultStyles.applyGlassCard(desk);

        Text title = new Text("AI Verification Complete");
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        RoundModel currentRound = battle.getRounds().get(battle.getRounds().size() - 1);
        String myTeamSide = battle.getParticipants().get(AuthController.currentUser.getUserId());
        boolean isWinner = myTeamSide != null && myTeamSide.equals(currentRound.getWinningTeam());

        Text resultText = new Text("The AI has declared Team " + currentRound.getWinningTeam() + " as the winner.");
        resultText.setFill(Color.web(GamerVaultStyles.ACCENT_CYAN));
        resultText.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Text subText = new Text(isWinner ? "Congratulations! Please accept to receive your payout."
                : "Better luck next time. Please accept to finalize the match.");
        subText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

        HBox actions = new HBox(20);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(15, 0, 0, 0));

        Button acceptBtn = new Button("Accept Result");
        acceptBtn.setStyle(
                "-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 20;");

        Button disputeBtn = new Button("Report / Dispute");
        disputeBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-border-color: #EF4444; -fx-border-radius: 6; -fx-cursor: hand; -fx-padding: 10 20;");

        acceptBtn.setOnAction(e -> {
            acceptBtn.setDisable(true);
            disputeBtn.setDisable(true);
            acceptBtn.setText("Processing...");
            new Thread(() -> {
                BattleDao.resolveBattleFinancials(battle.getBattleId(),
                        BattleFirebaseKeys.STATUS_COMPLETED, currentRound.getWinningTeam(), false);
            }).start();
        });

        disputeBtn.setOnAction(e -> {
            acceptBtn.setDisable(true);
            disputeBtn.setDisable(true);
            disputeBtn.setText("Escalating...");
            new Thread(() -> {
                BattleDao.escalateToDispute(battle.getBattleId(), battle.getRounds().size() - 1);
            }).start();
        });

        actions.getChildren().addAll(acceptBtn, disputeBtn);
        desk.getChildren().addAll(title, resultText, subText, actions);
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
                return "#F59E0B";
            case BattleFirebaseKeys.STATUS_LOCKED:
                return "#10B981";
            case BattleFirebaseKeys.STATUS_IN_PROGRESS:
                return "#3B82F6";
            case BattleFirebaseKeys.STATUS_COMPLETED:
                return GamerVaultStyles.ACCENT_PURPLE_LIGHT;
            case BattleFirebaseKeys.STATUS_CANCELLED:
                return "#9CA3AF";
            default:
                return "#EF4444"; // Red for Disputed/Forfeit
        }
    }

    private void handleEvidenceUpload(Button btn, BattleModel battle, int roundNum, String teamSide,
            String claimedOutcome) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(btn.getScene().getWindow());

        if (file == null)
            return;

        btn.setDisable(true);
        btn.setText("Analyzing...");

        roomController.submitRoundEvidence(battle, roundNum, teamSide, claimedOutcome, file, success -> {
            Platform.runLater(() -> {
                btn.setText(success ? "Submitted ✓" : "Failed — retry");
                btn.setDisable(success);
            });
        });
    }
}