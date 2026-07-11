package com.example.view.admin;

import com.example.dao.BattleDao;
import com.example.keys.BattleFirebaseKeys;
import com.example.model.player.BattleModel;
import com.example.model.player.RoundModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;

public class AdminDisputeScreen {

    private VBox disputeListContainer;

    public BorderPane startAdminDisputeScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        VBox header = new VBox(5);
        header.setPadding(new Insets(10, 20, 20, 20));
        Text title = new Text("Dispute Resolution Queue");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 32));
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

        Text subtitle = new Text("Review conflicting evidence and securely release locked funds.");
        subtitle.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        header.getChildren().addAll(title, subtitle);

        disputeListContainer = new VBox(15);
        disputeListContainer.setPadding(new Insets(10, 20, 40, 20));

        ScrollPane scroller = new ScrollPane(disputeListContainer);
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setFitToWidth(true);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setTop(header);
        root.setCenter(scroller);

        loadDisputes();
        return root;
    }

    private void loadDisputes() {
        disputeListContainer.getChildren().clear();
        Text loading = new Text("Fetching active disputes...");
        loading.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE));
        disputeListContainer.getChildren().add(loading);

        new Thread(() -> {
            List<BattleModel> disputes = BattleDao.getDisputedBattles();
            Platform.runLater(() -> {
                disputeListContainer.getChildren().clear();
                if (disputes.isEmpty()) {
                    Text empty = new Text("No active disputes. The queue is clear.");
                    empty.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                    disputeListContainer.getChildren().add(empty);
                    return;
                }

                int delay = 0;
                for (BattleModel b : disputes) {
                    VBox card = buildDisputeCard(b);
                    disputeListContainer.getChildren().add(card);
                    GamerVaultAnimations.fadeInUp(card, delay * 50, 400);
                    delay++;
                }
            });
        }).start();
    }

    private VBox buildDisputeCard(BattleModel battle) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        GamerVaultStyles.applyGlassCard(card);

        Text battleTitle = new Text(battle.getGameTitle() + " " + battle.getMode()
                + " — Pool: " + String.format("%.0f 🪙", (battle.getEntryFeeCoins() * battle.getMaxParticipants())));
        battleTitle.setFill(Color.WHITE);
        battleTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        card.getChildren().add(battleTitle);

        for (RoundModel round : battle.getRounds()) {
            if (round.getTeamAScreenshotUrl() == null && round.getTeamBScreenshotUrl() == null)
                continue;

            HBox comparisonBox = new HBox(30);
            comparisonBox.setAlignment(Pos.CENTER);

            // Reusing evidence block logic
            comparisonBox.getChildren().addAll(
                    evidenceBlock("Team A Submission", round.getTeamAScreenshotUrl(), round.getTeamAClaimedOutcome(),
                            round.getTeamAOcrResult(), round.getTeamASubmittedBy()),
                    evidenceBlock("Team B Submission", round.getTeamBScreenshotUrl(), round.getTeamBClaimedOutcome(),
                            round.getTeamBOcrResult(), round.getTeamBSubmittedBy()));
            card.getChildren().add(comparisonBox);

            // Admin Action Buttons
            HBox actions = new HBox(15);
            actions.setAlignment(Pos.CENTER);
            actions.setPadding(new Insets(10, 0, 0, 0));

            Button winA = new Button("Force Win — Team A");
            Button winB = new Button("Force Win — Team B");
            Button voidBtn = new Button("Void Match & Refund 80%");

            winA.setStyle(
                    "-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
            winB.setStyle(
                    "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
            voidBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-border-color: #EF4444; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");

            winA.setOnAction(e -> resolveAction(battle.getBattleId(), "A", false, card));
            winB.setOnAction(e -> resolveAction(battle.getBattleId(), "B", false, card));
            voidBtn.setOnAction(e -> resolveAction(battle.getBattleId(), null, true, card));

            actions.getChildren().addAll(winA, winB, voidBtn);

            Button chatBtn = new Button("💬 Open Chat");
            GamerVaultStyles.applyGhostButton(chatBtn);
            chatBtn.setStyle(chatBtn.getStyle() + "-fx-border-color: #F59E0B; -fx-text-fill: #F59E0B;");

            chatBtn.setOnAction(e -> openAdminChatDialog(battle.getBattleId()));

            actions.getChildren().add(0, chatBtn);
            card.getChildren().add(actions);
        }
        return card;
    }

    private void openAdminChatDialog(String battleId) {
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Dispute Investigation Chat");

        VBox chatBox = new VBox(10);
        chatBox.setPadding(new Insets(10));
        ScrollPane scroller = new ScrollPane(chatBox);
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setPrefSize(500, 300);

        HBox inputBox = new HBox(10);
        javafx.scene.control.TextField messageField = new javafx.scene.control.TextField();
        messageField.setPrefWidth(400);
        Button sendBtn = new Button("Send as Admin");
        sendBtn.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-font-weight: bold;");

        sendBtn.setOnAction(e -> {
            if (messageField.getText().trim().isEmpty())
                return;
            com.example.model.player.ChatMessageModel msg = new com.example.model.player.ChatMessageModel(
                    java.util.UUID.randomUUID().toString(), "admin_system", "ADMIN", messageField.getText().trim(),
                    System.currentTimeMillis());
            com.example.dao.DisputeChatDao.sendMessage(battleId, msg);
            messageField.clear();
        });

        // Listen for live messages
        com.google.cloud.firestore.ListenerRegistration listener = com.example.dao.DisputeChatDao.listenToChat(battleId,
                messages -> {
                    Platform.runLater(() -> {
                        chatBox.getChildren().clear();
                        for (com.example.model.player.ChatMessageModel m : messages) {
                            Text senderTxt = new Text(m.getSenderName() + ": ");
                            senderTxt.setFill(m.getSenderName().equals("ADMIN") ? Color.web("#F59E0B") : Color.WHITE);
                            senderTxt.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                            Text msgTxt = new Text(m.getText());
                            msgTxt.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
                            chatBox.getChildren().add(new HBox(senderTxt, msgTxt));
                        }
                        scroller.setVvalue(1.0);
                    });
                });

        inputBox.getChildren().addAll(messageField, sendBtn);
        VBox root = new VBox(15, scroller, inputBox);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #0B0F19;");

        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        dialog.setOnHidden(e -> listener.remove()); // Clean up listener when closed
        dialog.show();
    }

    private VBox evidenceBlock(String label, String imgUrl, String claim, String ocr, String submitterId) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(260);

        Text title = new Text(label);
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        StackPane imgFrame = new StackPane();
        imgFrame.setPrefSize(260, 146);
        imgFrame.setStyle("-fx-background-color: #000; -fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 4;");

        if (imgUrl != null && !imgUrl.isEmpty() && !imgUrl.equals("UPLOADED_URL")) {
            ImageView iv = new ImageView(new Image(imgUrl, true));
            iv.setFitWidth(260);
            iv.setFitHeight(146);
            iv.setPreserveRatio(true);
            imgFrame.getChildren().add(iv);
        } else {
            Text noImg = new Text("No Evidence Uploaded");
            noImg.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
            imgFrame.getChildren().add(noImg);
        }

        Text claimTxt = new Text("Claimed: " + (claim != null ? claim : "None"));
        claimTxt.setFill(Color.WHITE);

        Text ocrTxt = new Text("AI Read: " + (ocr != null ? ocr : "Pending"));
        ocrTxt.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));

        Text userTxt = new Text("Submitted By: "
                + (submitterId != null ? submitterId.substring(0, Math.min(submitterId.length(), 8)) + "..."
                        : "Unknown"));
        userTxt.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        userTxt.setFont(Font.font("Arial", 10));

        box.getChildren().addAll(title, imgFrame, claimTxt, ocrTxt, userTxt);
        return box;
    }

    private void resolveAction(String battleId, String winningTeam, boolean isRefund, VBox cardUI) {
        // Optimistic UI updates
        cardUI.setDisable(true);
        cardUI.setOpacity(0.5);

        new Thread(() -> {
            // Directly calls the safe financial transaction logic
            boolean success = BattleDao.resolveBattleFinancials(
                    battleId,
                    isRefund ? BattleFirebaseKeys.STATUS_CANCELLED : BattleFirebaseKeys.STATUS_COMPLETED,
                    winningTeam,
                    isRefund);

            Platform.runLater(() -> {
                if (success) {
                    // Remove from queue visually
                    disputeListContainer.getChildren().remove(cardUI);
                    if (disputeListContainer.getChildren().isEmpty()) {
                        Text empty = new Text("No active disputes. The queue is clear.");
                        empty.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                        disputeListContainer.getChildren().add(empty);
                    }
                } else {
                    cardUI.setDisable(false);
                    cardUI.setOpacity(1.0);
                    Alert alert = new Alert(AlertType.ERROR,
                            "Resolution failed. This battle may have already been resolved.");
                    alert.showAndWait();
                }
            });
        }).start();
    }
}