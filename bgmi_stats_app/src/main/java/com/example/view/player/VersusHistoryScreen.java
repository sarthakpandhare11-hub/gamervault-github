package com.example.view.player;

import com.example.controller.AuthController;
import com.example.controller.player.ProfileController;
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

        currentUserId = ProfileController.getActiveProfileId();

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
        HBox loadingRow = new HBox(10);
        loadingRow.setAlignment(Pos.CENTER);
        loadingRow.setPadding(new Insets(40, 0, 0, 0));
        Text loadingIcon = new Text("📡");
        loadingIcon.setFont(Font.font(20));
        Text loading = new Text("Fetching historical records...");
        loading.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE));
        loadingRow.getChildren().addAll(loadingIcon, loading);
        historyList.getChildren().add(loadingRow);

        new Thread(() -> {
            List<BattleModel> pastBattles = BattleDao.getBattleHistoryForUser(currentUserId);
            Platform.runLater(() -> {
                historyList.getChildren().clear();
                if (pastBattles.isEmpty()) {
                    VBox emptyState = new VBox(10);
                    emptyState.setAlignment(Pos.CENTER);
                    emptyState.setPadding(new Insets(50, 0, 50, 0));
                    Text emptyIcon = new Text("🗒️");
                    emptyIcon.setFont(Font.font(36));
                    GamerVaultAnimations.animateFloating(emptyIcon, 2.2, -4, 4);
                    Text empty = new Text("No completed wagers found.");
                    empty.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                    empty.setFont(Font.font("Arial", FontWeight.BOLD, 13));
                    emptyState.getChildren().addAll(emptyIcon, empty);
                    historyList.getChildren().add(emptyState);
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
        attachRippleEffect(proofBtn, "#FFFFFF");
        proofBtn.setOnAction(e -> showProofSummaryDialog(battle));

        card.getChildren().addAll(statusBadge, details, spacer, outcomeText, proofBtn);
        return card;
    }

    /*
     * Same self-contained ripple pattern used across the other Battle Arena
     * screens for visual consistency.
     */
    private void attachRippleEffect(Region button, String colorHex) {
        button.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            javafx.scene.Node parent = button.getParent();
            if (!(parent instanceof Pane))
                return;
            Pane parentPane = (Pane) parent;

            javafx.scene.shape.Circle ripple = new javafx.scene.shape.Circle(4, Color.web(colorHex, 0.35));
            ripple.setManaged(false);
            ripple.setMouseTransparent(true);

            javafx.geometry.Point2D clickInParent = button.localToParent(e.getX(), e.getY());
            ripple.setCenterX(clickInParent.getX());
            ripple.setCenterY(clickInParent.getY());
            parentPane.getChildren().add(ripple);

            double targetRadius = Math.max(button.getWidth(), button.getHeight()) * 0.9;
            javafx.animation.Timeline grow = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.ZERO,
                            new javafx.animation.KeyValue(ripple.radiusProperty(), 4)),
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(450),
                            new javafx.animation.KeyValue(ripple.radiusProperty(), targetRadius,
                                    javafx.animation.Interpolator.EASE_OUT)));

            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(450), ripple);
            fade.setFromValue(0.7);
            fade.setToValue(0);

            javafx.animation.ParallelTransition anim = new javafx.animation.ParallelTransition(grow, fade);
            anim.setOnFinished(ev -> parentPane.getChildren().remove(ripple));
            anim.play();
        });
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
            VBox roundBox = new VBox(12);
            roundBox.setPadding(new Insets(15));
            roundBox.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8;");

            HBox rTitleRow = new HBox(10);
            rTitleRow.setAlignment(Pos.CENTER_LEFT);
            Text rTitle = new Text("Round " + round.getRoundNumber());
            rTitle.setFill(Color.web(GamerVaultStyles.ACCENT_CYAN));
            rTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

            String statusColor = "DISPUTED".equals(round.getRoundStatus()) ? "#EF4444"
                    : ("COMPLETED".equals(round.getRoundStatus()) ? GamerVaultStyles.ACCENT_GREEN : "#F59E0B");
            HBox statusBadge = new HBox();
            statusBadge.setPadding(new Insets(3, 10, 3, 10));
            statusBadge.setStyle(
                    "-fx-background-color: " + statusColor + "22; -fx-background-radius: 10; -fx-border-color: "
                            + statusColor + "55; -fx-border-radius: 10;");
            Text statusTxt = new Text(round.getRoundStatus());
            statusTxt.setFill(Color.web(statusColor));
            statusTxt.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            statusBadge.getChildren().add(statusTxt);
            rTitleRow.getChildren().addAll(rTitle, statusBadge);

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

            // Extracted stats strips - already-captured data (score, kills, F/D,
            // assists, MVP IGN) that this dialog never displayed before.
            HBox statsRow = new HBox(30);
            statsRow.setAlignment(Pos.CENTER);
            statsRow.getChildren().addAll(
                    createStatStrip(round.getTeamAScore(), round.getTeamAKills(), round.getTeamAFdRatio(),
                            round.getTeamAAssists(), round.getTeamAMvpIgn(), GamerVaultStyles.ACCENT_CYAN),
                    createStatStrip(round.getTeamBScore(), round.getTeamBKills(), round.getTeamBFdRatio(),
                            round.getTeamBAssists(), round.getTeamBMvpIgn(), GamerVaultStyles.ACCENT_ORANGE));

            roundBox.getChildren().addAll(rTitleRow, comparisonBox, statsRow);
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

        HBox claimChip = createInfoChip("Claimed", claim != null ? claim : "None",
                "WIN".equals(claim) ? GamerVaultStyles.ACCENT_GREEN : "#EF4444");
        HBox ocrChip = createInfoChip("AI Read", ocr != null ? ocr : "Pending", GamerVaultStyles.ACCENT_CYAN);

        box.getChildren().addAll(title, imageContainer, claimChip, ocrChip);
        return box;
    }

    /*
     * Small chip for a label+value pair, replacing plain "Label: value" text
     * with a colored badge consistent with the rest of the app's card language.
     */
    private HBox createInfoChip(String label, String value, String accentColor) {
        HBox chip = new HBox(6);
        chip.setAlignment(Pos.CENTER);
        chip.setPadding(new Insets(4, 10, 4, 10));
        chip.setStyle("-fx-background-color: " + accentColor + "1A; -fx-background-radius: 8; -fx-border-color: "
                + accentColor + "55; -fx-border-radius: 8;");
        Text l = new Text(label + ":");
        l.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        l.setFont(Font.font("Arial", 10));
        Text v = new Text(value);
        v.setFill(Color.web(accentColor));
        v.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        chip.getChildren().addAll(l, v);
        return chip;
    }

    /*
     * Displays the extracted stats (score, kills, F/D ratio, assists, MVP IGN)
     * that RoundModel already carries for each side but that this screen never
     * surfaced before. No new data fetch - this data was already on the
     * RoundModel object passed into showProofSummaryDialog.
     */
    private VBox createStatStrip(int score, int kills, double fdRatio, int assists, String mvpIgn,
            String accentColor) {
        VBox strip = new VBox(6);
        strip.setAlignment(Pos.CENTER);
        strip.setPadding(new Insets(10));
        strip.setPrefWidth(250);
        strip.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 8; -fx-border-color: "
                + accentColor + "33; -fx-border-radius: 8;");

        if (mvpIgn != null && !mvpIgn.isEmpty() && !mvpIgn.equals("N/A")) {
            Text mvp = new Text("👑 " + mvpIgn);
            mvp.setFill(Color.web(accentColor));
            mvp.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            strip.getChildren().add(mvp);
        }

        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER);
        row.getChildren().addAll(
                createMiniStatCol("SCORE", String.valueOf(score)),
                createMiniStatCol("KILLS", String.valueOf(kills)),
                createMiniStatCol("F/D", String.format("%.1f", fdRatio)),
                createMiniStatCol("ASSISTS", String.valueOf(assists)));
        strip.getChildren().add(row);
        return strip;
    }

    private VBox createMiniStatCol(String label, String value) {
        VBox col = new VBox(2);
        col.setAlignment(Pos.CENTER);
        Text l = new Text(label);
        l.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        l.setFont(Font.font("Arial", FontWeight.BOLD, 8));
        Text v = new Text(value);
        v.setFill(Color.WHITE);
        v.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        col.getChildren().addAll(l, v);
        return col;
    }
}