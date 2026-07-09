package com.example.view.player;

import com.example.controller.AuthController;
import com.example.dao.BattleDao;
import com.example.model.player.BattleModel;
import com.example.model.player.RoundModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VersusHistoryScreen {

    private VBox historyList;
    private String currentUserId;

    public BorderPane startVersusHistoryScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        currentUserId = AuthController.currentUser != null ? AuthController.currentUser.getUserId() : "";

        VBox container = new VBox(20);
        container.setPadding(new Insets(10, 20, 40, 20));

        Text title = new Text("Versus Match History");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 36));
        title.setFill(Color.WHITE);

        Text subTitle = new Text("Review your past wagers and AI-verified proof summaries.");
        subTitle.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));

        historyList = new VBox(15);
        historyList.setAlignment(Pos.TOP_CENTER);

        ScrollPane scroller = new ScrollPane(historyList);
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setFitToWidth(true);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        container.getChildren().addAll(title, subTitle, scroller);
        root.setCenter(container);

        loadHistory();

        return root;
    }

    private void loadHistory() {
        Text loading = new Text("Fetching historical records...");
        loading.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE));
        historyList.getChildren().add(loading);

        new Thread(() -> {
            List<BattleModel> pastBattles = BattleDao.getBattleHistoryForUser(currentUserId);
            Platform.runLater(() -> {
                historyList.getChildren().clear();
                if (pastBattles.isEmpty()) {
                    Text empty = new Text("No completed wagers found.");
                    empty.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                    historyList.getChildren().add(empty);
                } else {
                    int delay = 0;
                    for (BattleModel battle : pastBattles) {
                        HBox card = createHistoryCard(battle);
                        historyList.getChildren().add(card);
                        GamerVaultAnimations.fadeInUp(card, delay * 50, 400);
                        delay++;
                    }
                }
            });
        }).start();
    }

    private HBox createHistoryCard(BattleModel battle) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER_LEFT);
        GamerVaultStyles.applyGlassCard(card);
        GamerVaultAnimations.scaleOnHover(card, 1.01);

        String myTeam = battle.getParticipants().get(currentUserId);
        boolean isWin = myTeam != null && myTeam.equals(battle.getOverallWinner());
        boolean isCancelled = battle.getStatus().equals("CANCELLED");

        // Status Indicator
        StackPane statusBadge = new StackPane();
        statusBadge.setPrefSize(10, 60);
        statusBadge.setStyle("-fx-background-radius: 4; -fx-background-color: " +
                (isCancelled ? "#9CA3AF" : (isWin ? GamerVaultStyles.ACCENT_GREEN : "#EF4444")) + ";");

        // Details
        VBox details = new VBox(5);
        Text modeText = new Text("BGMI " + battle.getMode() + " (" + battle.getFormat() + ")");
        modeText.setFill(Color.WHITE);
        modeText.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        String dateStr = new SimpleDateFormat("MMM dd, yyyy").format(new Date(battle.getCreatedAt()));
        Text dateText = new Text(dateStr + " • Entry: " + String.format("%.0f 🪙", battle.getEntryFeeCoins()));
        dateText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

        details.getChildren().addAll(modeText, dateText);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Outcome Text
        Text outcomeText = new Text(isCancelled ? "CANCELLED" : (isWin ? "VICTORY" : "DEFEAT"));
        outcomeText.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        outcomeText.setFill(Color.web(isCancelled ? "#9CA3AF" : (isWin ? GamerVaultStyles.ACCENT_GREEN : "#EF4444")));

        // Action Button
        Button proofBtn = new Button("View Proof");
        GamerVaultStyles.applyGhostButton(proofBtn);
        proofBtn.setStyle(
                proofBtn.getStyle() + "-fx-border-color: " + GamerVaultStyles.ACCENT_CYAN + "; -fx-text-fill: white;");
        proofBtn.setOnAction(e -> showProofSummaryDialog(battle));

        card.getChildren().addAll(statusBadge, details, spacer, outcomeText, proofBtn);
        return card;
    }

    private void showProofSummaryDialog(BattleModel battle) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Match Proof Summary");

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setStyle(
                "-fx-background-color: #0B0F19; -fx-border-color: rgba(139,92,246,0.3); -fx-border-width: 1; -fx-border-radius: 8;");
        content.setPrefWidth(700);

        Text header = new Text("AI Verification Summary");
        header.setFill(Color.WHITE);
        header.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        content.getChildren().add(header);

        for (RoundModel round : battle.getRounds()) {
            VBox roundBox = new VBox(10);
            roundBox.setPadding(new Insets(15));
            roundBox.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8;");

            Text rTitle = new Text("Round " + round.getRoundNumber() + " - Status: " + round.getRoundStatus());
            rTitle.setFill(Color.web(GamerVaultStyles.ACCENT_CYAN));
            rTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

            HBox comparisonBox = new HBox(30);
            comparisonBox.setAlignment(Pos.CENTER);

            // Team A Side
            VBox teamABox = createTeamEvidenceBlock("Team A Submission", round.getTeamAScreenshotUrl(),
                    round.getTeamAClaimedOutcome(), round.getTeamAOcrResult());

            // AI Decision Center
            VBox aiDecision = new VBox(5);
            aiDecision.setAlignment(Pos.CENTER);
            Text vs = new Text("VS");
            vs.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
            vs.setFont(Font.font("Arial", FontWeight.BOLD, 18));

            Text aiResult = new Text(
                    "AI Winner:\nTeam " + (round.getWinningTeam() != null ? round.getWinningTeam() : "N/A"));
            aiResult.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
            aiResult.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            aiResult.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            aiDecision.getChildren().addAll(vs, aiResult);

            // Team B Side
            VBox teamBBox = createTeamEvidenceBlock("Team B Submission", round.getTeamBScreenshotUrl(),
                    round.getTeamBClaimedOutcome(), round.getTeamBOcrResult());

            comparisonBox.getChildren().addAll(teamABox, aiDecision, teamBBox);
            roundBox.getChildren().addAll(rTitle, comparisonBox);
            content.getChildren().add(roundBox);
        }

        ScrollPane scroller = new ScrollPane(content);
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setFitToWidth(true);
        scroller.setPrefHeight(500);

        dialog.getDialogPane().setContent(scroller);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle("-fx-background-color: #0B0F19;");

        dialog.showAndWait();
    }

    private VBox createTeamEvidenceBlock(String titleStr, String imgUrl, String claim, String ocr) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(250);

        Text title = new Text(titleStr);
        title.setFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(250, 140);
        imageContainer.setStyle(
                "-fx-background-color: #000000; -fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 4;");

        if (imgUrl != null && !imgUrl.isEmpty() && !imgUrl.equals("UPLOADED_URL")) {
            // Load image in background thread to prevent UI freezing
            ImageView imageView = new ImageView(new Image(imgUrl, true));
            imageView.setFitWidth(250);
            imageView.setFitHeight(140);
            imageView.setPreserveRatio(true);
            imageContainer.getChildren().add(imageView);
        } else {
            Text noImg = new Text("No Image\nSubmitted");
            noImg.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
            noImg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            imageContainer.getChildren().add(noImg);
        }

        Text claimTxt = new Text("Claimed: " + (claim != null ? claim : "None"));
        claimTxt.setFill(Color.WHITE);

        Text ocrTxt = new Text("AI Read: " + (ocr != null ? ocr : "Pending"));
        ocrTxt.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        ocrTxt.setFont(Font.font("Arial", 11));

        box.getChildren().addAll(title, imageContainer, claimTxt, ocrTxt);
        return box;
    }
}