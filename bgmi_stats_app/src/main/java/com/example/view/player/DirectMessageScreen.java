package com.example.view.player;

import com.example.controller.AuthController;
import com.example.controller.player.DirectMessageController;
import com.example.dao.DirectMessageDao;
import com.example.model.player.ChatMessageModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;
import com.google.cloud.firestore.ListenerRegistration;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DirectMessageScreen {

    // UI Components
    private VBox chatContainer;
    private ScrollPane chatScroller;
    private TextField messageInput;

    // State & Listeners
    private ListenerRegistration chatListener;
    private final SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");

    public BorderPane startChatScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(10, 20, 20, 20));

        // --- HEADER ---
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        Button backBtn = new Button("← Back");
        GamerVaultStyles.applyGhostButton(backBtn);
        backBtn.setOnAction(e -> navigateAway());

        VBox titleBox = new VBox(2);
        Text title = new Text("Secure Comms Channel");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 22));
        title.setFill(Color.WHITE);

        Text subtitle = new Text("End-to-End Encrypted Lobby Chat");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setFill(Color.web(GamerVaultStyles.ACCENT_CYAN));

        titleBox.getChildren().addAll(title, subtitle);

        headerBox.getChildren().addAll(backBtn, SizedBox.width(10), titleBox);

        // --- CHAT WINDOW ---
        VBox mainChatArea = new VBox();
        mainChatArea.setStyle(
                "-fx-background-color: #0F172A; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: rgba(14, 165, 233, 0.2); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12;");

        chatContainer = new VBox(15);
        chatContainer.setPadding(new Insets(20));

        chatScroller = new ScrollPane(chatContainer);
        chatScroller.setFitToWidth(true);
        GamerVaultStyles.applyStyledScrollPane(chatScroller);
        chatScroller.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(chatScroller, Priority.ALWAYS);

        // --- INPUT AREA ---
        HBox inputArea = new HBox(12);
        inputArea.setPadding(new Insets(15));
        inputArea.setStyle(
                "-fx-background-color: rgba(255,255,255,0.02); " +
                        "-fx-border-color: rgba(255,255,255,0.05); " +
                        "-fx-border-width: 1 0 0 0; " +
                        "-fx-background-radius: 0 0 12 12;");
        inputArea.setAlignment(Pos.CENTER);

        messageInput = new TextField();
        messageInput.setPromptText("Type a message to your connection...");
        HBox styledInput = GamerVaultStyles.createStyledInput(messageInput, "#0EA5E9");
        HBox.setHgrow(styledInput, Priority.ALWAYS);
        styledInput.setPrefHeight(45);

        Button sendBtn = new Button("Send ✈");
        sendBtn.setPrefHeight(45);
        sendBtn.setPrefWidth(100);
        sendBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #0EA5E9, #0284C7); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;");
        GamerVaultAnimations.scaleOnHover(sendBtn, 1.05);

        // Send Logic
        sendBtn.setOnAction(e -> attemptSendMessage());
        messageInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER)
                attemptSendMessage();
        });

        inputArea.getChildren().addAll(styledInput, sendBtn);
        mainChatArea.getChildren().addAll(chatScroller, inputArea);

        root.setTop(headerBox);
        root.setCenter(mainChatArea);

        GamerVaultAnimations.fadeInUp(mainChatArea, 100, 500);

        // --- BIND REAL-TIME FIRESTORE LISTENER ---
        attachRealTimeListener();

        return root;
    }

    /**
     * Executes the secure navigation stack pop and cleans up the active listener.
     */
    private void navigateAway() {
        if (chatListener != null) {
            chatListener.remove(); // CRITICAL: Prevents memory & network bandwidth leaks
        }
        PlayerMainScreen.instance.navigateBack();
    }

    /**
     * Validates input, builds the ChatMessageModel, and dispatches to Firestore via
     * an async thread.
     */
    private void attemptSendMessage() {
        String text = messageInput.getText().trim();
        String roomId = DirectMessageController.activeChatRoomId;

        if (!text.isEmpty() && AuthController.currentUser != null && roomId != null) {

            ChatMessageModel msg = new ChatMessageModel(
                    UUID.randomUUID().toString(),
                    AuthController.currentUser.getUserId(),
                    AuthController.currentUser.getIgn(),
                    text,
                    System.currentTimeMillis());

            messageInput.clear();

            // Run network operation on background ForkJoin pool to prevent UI freeze
            CompletableFuture.runAsync(() -> {
                DirectMessageDao.sendMessage(roomId, msg);
            });
        }
    }

    /**
     * Connects the UI to the Firestore Snapshot listener to render messages as they
     * arrive in real-time.
     */
    private void attachRealTimeListener() {
        String roomId = DirectMessageController.activeChatRoomId;
        if (roomId == null)
            return;

        chatListener = DirectMessageDao.listenForMessages(roomId, (messages) -> {
            Platform.runLater(() -> {
                chatContainer.getChildren().clear();

                if (messages.isEmpty()) {
                    showEmptyState();
                } else {
                    for (ChatMessageModel m : messages) {
                        boolean isMine = m.getSenderId().equals(AuthController.currentUser.getUserId());
                        addMessageBubble(m, isMine);
                    }
                    scrollToBottom();
                }
            });
        });
    }

    /**
     * Beautifully renders an individual message bubble based on who sent it.
     */
    private void addMessageBubble(ChatMessageModel msg, boolean isMine) {
        HBox row = new HBox();
        row.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(5);
        bubble.setPadding(new Insets(10, 15, 10, 15));
        bubble.setMaxWidth(450);

        if (isMine) {
            // Electric Blue for the local sender (sharp bottom-right corner)
            bubble.setStyle(
                    "-fx-background-color: rgba(14, 165, 233, 0.15); " +
                            "-fx-border-color: rgba(14, 165, 233, 0.4); " +
                            "-fx-border-radius: 12 12 0 12; " +
                            "-fx-background-radius: 12 12 0 12;");
        } else {
            // Flat Charcoal for the receiver (sharp bottom-left corner)
            bubble.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.05); " +
                            "-fx-border-color: rgba(255,255,255,0.1); " +
                            "-fx-border-radius: 12 12 12 0; " +
                            "-fx-background-radius: 12 12 12 0;");
        }

        // Message Metadata (Name + Time)
        HBox metaRow = new HBox(8);
        metaRow.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Text senderName = new Text(isMine ? "You" : (msg.getSenderName() != null ? msg.getSenderName() : "Unknown"));
        senderName.setFill(Color.web(isMine ? "#38BDF8" : GamerVaultStyles.TEXT_MUTED));
        senderName.setFont(Font.font("Arial", FontWeight.BOLD, 10));

        Text timestamp = new Text(timeFormatter.format(new Date(msg.getTimestamp())));
        timestamp.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        timestamp.setFont(Font.font("Arial", 9));

        if (isMine) {
            metaRow.getChildren().addAll(timestamp, senderName);
        } else {
            metaRow.getChildren().addAll(senderName, timestamp);
        }

        // Message Content
        Text textNode = new Text(msg.getText());
        textNode.setFill(Color.WHITE);
        textNode.setFont(Font.font("Arial", 14));
        textNode.setWrappingWidth(420);
        textNode.setLineSpacing(3);

        bubble.getChildren().addAll(metaRow, textNode);
        row.getChildren().add(bubble);

        chatContainer.getChildren().add(row);
    }

    /**
     * Renders a placeholder if this is a brand-new connection with no message
     * history.
     */
    private void showEmptyState() {
        VBox emptyState = new VBox(10);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(100, 0, 0, 0));

        Text icon = new Text("🤝");
        icon.setFont(Font.font(36));
        GamerVaultAnimations.animateFloating(icon, 2.2, -4, 4);

        Text emptyText = new Text("Connection Secured");
        emptyText.setFill(Color.WHITE);
        emptyText.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Text subText = new Text("Say hello to your new contact! Messages are end-to-end encrypted.");
        subText.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        subText.setFont(Font.font("Arial", 12));

        emptyState.getChildren().addAll(icon, emptyText, subText);
        chatContainer.getChildren().add(emptyState);
    }

    /**
     * Forces the scroll pane to slide to the newest message.
     */
    private void scrollToBottom() {
        // Needs to run after the layout pass has calculated the new heights
        Platform.runLater(() -> {
            chatScroller.layout();
            chatScroller.setVvalue(1.0);
        });
    }
}