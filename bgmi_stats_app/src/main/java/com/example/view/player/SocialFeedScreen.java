package com.example.view.player;

import com.example.controller.player.GamerMomentController;
import com.example.controller.player.ProfileController;
import com.example.dao.GamerMomentDao;
import com.example.model.player.GamerMomentModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;
import com.example.view.util.SizedBox;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SocialFeedScreen {

    private VBox feedContainer;
    private final SimpleDateFormat timeFormatter = new SimpleDateFormat("MMM dd, hh:mm a");

    public BorderPane startFeedScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(10, 20, 40, 20));

        Text title = new Text("Social Feed");
        title.setFont(Font.font("Poppins", FontWeight.BOLD, 36));
        title.setFill(Color.WHITE);

        // --- CREATE POST BOX ---
        VBox createPostBox = createPublishBox();
        GamerVaultAnimations.fadeInUp(createPostBox, 0, 400);

        // --- FEED CONTAINER ---
        feedContainer = new VBox(15);
        feedContainer.setAlignment(Pos.TOP_CENTER);

        ScrollPane scroller = new ScrollPane(feedContainer);
        scroller.setFitToWidth(true);
        GamerVaultStyles.applyStyledScrollPane(scroller);
        VBox.setVgrow(scroller, Priority.ALWAYS);

        mainLayout.getChildren().addAll(title, createPostBox, scroller);
        root.setCenter(mainLayout);

        loadFeed();

        return root;
    }

    private VBox createPublishBox() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        GamerVaultStyles.applyGlassCard(box);

        TextArea inputArea = new TextArea();
        inputArea.setPromptText("What's on your mind? Share a clutch, LFT status, or thoughts...");
        inputArea.setWrapText(true);
        inputArea.setPrefRowCount(3);
        inputArea.setStyle(
                "-fx-control-inner-background: #0B0F19; -fx-text-fill: white; -fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8;");

        HBox actionRow = new HBox(15);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        ComboBox<String> visibilityCombo = new ComboBox<>();
        visibilityCombo.getItems().addAll("PUBLIC", "CONNECTIONS_ONLY");
        visibilityCombo.setValue("PUBLIC");
        visibilityCombo.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: white;");

        Button publishBtn = new Button("Publish Update");
        publishBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #8B5CF6, #6D28D9); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        publishBtn.setPadding(new Insets(8, 20, 8, 20));

        publishBtn.setOnAction(e -> {
            publishBtn.setText("Publishing...");
            publishBtn.setDisable(true);
            new Thread(() -> {
                boolean success = GamerMomentController.publishPost(inputArea.getText(), visibilityCombo.getValue());
                Platform.runLater(() -> {
                    publishBtn.setText("Publish Update");
                    publishBtn.setDisable(false);
                    if (success) {
                        inputArea.clear();
                        loadFeed(); // Refresh
                    }
                });
            }).start();
        });

        actionRow.getChildren().addAll(visibilityCombo, publishBtn);
        box.getChildren().addAll(inputArea, actionRow);
        return box;
    }

    private void loadFeed() {
        feedContainer.getChildren().clear();
        Text loading = new Text("Syncing Timeline...");
        loading.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE));
        feedContainer.getChildren().add(loading);

        new Thread(() -> {
            List<GamerMomentModel> posts = GamerMomentController.getCuratedFeed();
            Platform.runLater(() -> {
                feedContainer.getChildren().clear();
                if (posts.isEmpty()) {
                    Text empty = new Text("No recent updates from your network.");
                    empty.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
                    feedContainer.getChildren().add(empty);
                } else {
                    int delay = 0;
                    for (GamerMomentModel post : posts) {
                        VBox card = createPostCard(post);
                        feedContainer.getChildren().add(card);
                        GamerVaultAnimations.fadeInUp(card, delay * 50, 400);
                        delay++;
                    }
                }
            });
        }).start();
    }

    private VBox createPostCard(GamerMomentModel post) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        GamerVaultStyles.applyGlassCard(card);

        // Header: Avatar, Name, Visibility
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        avatar.setPrefSize(40, 40);
        avatar.setStyle("-fx-background-color: rgba(139,92,246,0.2); -fx-background-radius: 20;");
        String safeName = (post.getAuthorName() != null && !post.getAuthorName().isEmpty()) ? post.getAuthorName()
                : "U";
        Text init = new Text(safeName.substring(0, 1).toUpperCase());
        init.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
        init.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        avatar.getChildren().add(init);

        // Interactive Routing: Click avatar/name to go to Profile
        avatar.setStyle(avatar.getStyle() + "-fx-cursor: hand;");
        avatar.setOnMouseClicked(e -> routeToProfile(post.getAuthorId()));

        VBox nameBox = new VBox(2);
        Text name = new Text(post.getAuthorName() + " (" + post.getAuthorIgn() + ")");
        name.setFill(Color.WHITE);
        name.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        name.setStyle("-fx-cursor: hand;");
        name.setOnMouseClicked(e -> routeToProfile(post.getAuthorId()));

        Text time = new Text(timeFormatter.format(new Date(post.getCreatedAt())));
        time.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        time.setFont(Font.font("Arial", 11));
        nameBox.getChildren().addAll(name, time);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Text visBadge = new Text("CONNECTIONS_ONLY".equals(post.getVisibility()) ? "🔒 Network" : "🌍 Public");
        visBadge.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        visBadge.setFont(Font.font("Arial", 10));

        header.getChildren().addAll(avatar, nameBox, spacer, visBadge);

        // Content
        Text content = new Text(post.getTextContent());
        content.setFill(Color.WHITE);
        content.setFont(Font.font("Arial", 14));
        content.setWrappingWidth(600);

        // Action Footer
        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(10, 0, 0, 0));
        footer.setStyle("-fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 1 0 0 0;");

        Text likeBtn = new Text("🔥 " + post.getLikesCount() + " Hype");
        likeBtn.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));
        likeBtn.setStyle("-fx-cursor: hand;");

        likeBtn.setOnMouseClicked(e -> {
            if (!likeBtn.isDisabled()) {
                likeBtn.setText("🔥 " + (post.getLikesCount() + 1) + " Hyped");
                likeBtn.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE)); // Highlight purple
                likeBtn.setDisable(true); // Prevent spam clicking

                CompletableFuture.runAsync(() -> {
                    GamerMomentDao.incrementLike(post.getPostId());
                });
            }
        });

        Text commentBtn = new Text("💬 " + post.getCommentsCount() + " Replies");
        commentBtn.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

        footer.getChildren().addAll(likeBtn, commentBtn);

        card.getChildren().addAll(header, content, footer);
        return card;
    }

    private void routeToProfile(String targetId) {
        ProfileController.setTargetProfile(targetId);
        PlayerMainScreen.instance.switchPage("profile");
    }
}