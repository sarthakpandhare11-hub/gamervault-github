package com.example.view.player;

import com.example.controller.player.ConnectionController;
import com.example.model.player.ConnectionModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;

/**
 * Inbox for incoming connection requests - the piece that was missing
 * entirely from the Connection feature. Without this, a sent request had
 * a Firestore document but no way to ever be seen, accepted, or declined
 * by the person it was sent to.
 */
public class ConnectionRequestsScreen {

    private VBox inboxContainer;

    public BorderPane startConnectionRequestsScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        VBox headerBox = new VBox(5);
        headerBox.setPadding(new Insets(10, 20, 20, 20));

        Text title = new Text("Connection Requests");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 36));
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

        Text subtitle = new Text("Accept a request to unlock full profile visibility with that player.");
        subtitle.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        subtitle.setFont(Font.font("Poppins", FontWeight.NORMAL, 15));

        headerBox.getChildren().addAll(title, subtitle);
        GamerVaultAnimations.fadeInUp(title, 0, 500);
        GamerVaultAnimations.fadeInUp(subtitle, 150, 500);

        inboxContainer = new VBox(15);
        inboxContainer.setPadding(new Insets(10, 20, 40, 20));

        ScrollPane scroller = new ScrollPane(inboxContainer);
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setFitToWidth(true);
        scroller.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.setTop(headerBox);
        root.setCenter(scroller);

        loadRequests();
        return root;
    }

    private void loadRequests() {
        inboxContainer.getChildren().clear();

        HBox loadingBox = new HBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(50));
        Text loadingText = new Text("🔍 Checking for pending requests...");
        loadingText.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
        loadingText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        loadingBox.getChildren().add(loadingText);
        inboxContainer.getChildren().add(loadingBox);

        new Thread(() -> {
            List<ConnectionModel> requests = ConnectionController.fetchPendingRequests();
            Platform.runLater(() -> {
                inboxContainer.getChildren().clear();
                if (requests == null || requests.isEmpty()) {
                    VBox emptyBox = new VBox(15);
                    emptyBox.setAlignment(Pos.CENTER);
                    emptyBox.setPadding(new Insets(100, 0, 0, 0));

                    Text icon = new Text("🤝");
                    icon.setFont(Font.font(48));
                    Text empty = new Text("NO PENDING REQUESTS");
                    empty.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                    empty.setFont(Font.font("Poppins", FontWeight.BOLD, 24));
                    Text subEmpty = new Text("You're all caught up - nobody is waiting on a response.");
                    subEmpty.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));

                    emptyBox.getChildren().addAll(icon, empty, subEmpty);
                    inboxContainer.getChildren().add(emptyBox);
                } else {
                    int delay = 0;
                    for (ConnectionModel req : requests) {
                        HBox card = createRequestCard(req);
                        inboxContainer.getChildren().add(card);
                        GamerVaultAnimations.fadeInUp(card, delay * 80, 400);
                        delay++;
                    }
                }
            });
        }).start();
    }

    private HBox createRequestCard(ConnectionModel request) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.03); " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: rgba(255,255,255,0.08); " +
                        "-fx-border-radius: 12;");
        GamerVaultAnimations.scaleOnHover(card, 1.02);

        HBox identityBox = new HBox(15);
        identityBox.setAlignment(Pos.CENTER_LEFT);
        identityBox.setPrefWidth(300);

        StackPane avatar = new StackPane();
        avatar.setPrefSize(46, 46);
        avatar.setStyle(
                "-fx-background-color: rgba(139,92,246,0.15); -fx-background-radius: 10; -fx-border-color: rgba(139,92,246,0.4); -fx-border-radius: 10;");
        Text avatarInitial = new Text("?");
        avatarInitial.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
        avatarInitial.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        avatar.getChildren().add(avatarInitial);

        Text nameLabel = new Text("Loading name...");
        nameLabel.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        identityBox.getChildren().addAll(avatar, nameLabel);

        // Resolve the requester's real name in the background - ConnectionModel
        // only stores the ID, same pattern used across the rest of the app.
        new Thread(() -> {
            String resolvedName = ConnectionController.resolveRequesterName(request.getRequesterId());
            Platform.runLater(() -> {
                nameLabel.setText(resolvedName);
                if (!resolvedName.isEmpty()) {
                    avatarInitial.setText(resolvedName.substring(0, 1).toUpperCase());
                }
            });
        }).start();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionBox = new HBox(12);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Button declineBtn = new Button("Decline");
        declineBtn.setPrefHeight(38);
        declineBtn.setPrefWidth(100);
        declineBtn.setStyle(
                "-fx-background-color: rgba(239, 68, 68, 0.1); -fx-text-fill: #ef4444; -fx-border-color: rgba(239, 68, 68, 0.4); -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
        declineBtn.setOnMouseEntered(e -> declineBtn.setStyle(
                "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-border-color: #ef4444; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;"));
        declineBtn.setOnMouseExited(e -> declineBtn.setStyle(
                "-fx-background-color: rgba(239, 68, 68, 0.1); -fx-text-fill: #ef4444; -fx-border-color: rgba(239, 68, 68, 0.4); -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;"));
        declineBtn.setOnAction(e -> handleAction(request, false, card));

        Button acceptBtn = new Button("Accept");
        acceptBtn.setPrefHeight(38);
        acceptBtn.setPrefWidth(120);
        acceptBtn.setStyle(
                "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        acceptBtn.setOnMouseEntered(e -> acceptBtn.setStyle(
                "-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;"));
        acceptBtn.setOnMouseExited(e -> acceptBtn.setStyle(
                "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;"));
        acceptBtn.setOnAction(e -> handleAction(request, true, card));

        actionBox.getChildren().addAll(declineBtn, acceptBtn);
        card.getChildren().addAll(identityBox, spacer, actionBox);
        return card;
    }

    private void handleAction(ConnectionModel request, boolean accept, HBox cardUI) {
        cardUI.setDisable(true);
        cardUI.setOpacity(0.5);

        new Thread(() -> {
            boolean success = accept ? ConnectionController.acceptRequest(request)
                    : ConnectionController.declineRequest(request.getConnectionId());
            Platform.runLater(() -> {
                if (success) {
                    inboxContainer.getChildren().remove(cardUI);
                    if (inboxContainer.getChildren().isEmpty()) {
                        loadRequests();
                    }
                } else {
                    cardUI.setDisable(false);
                    cardUI.setOpacity(1.0);
                }
            });
        }).start();
    }
}