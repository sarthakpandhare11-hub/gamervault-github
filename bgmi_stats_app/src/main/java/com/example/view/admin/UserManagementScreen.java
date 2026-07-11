package com.example.view.admin;

import java.util.ArrayList;
import java.util.List;

import com.example.controller.admin.AdminController;
import com.example.model.UserModel;
import com.example.view.util.GamerVaultAnimations;
import com.example.view.util.GamerVaultStyles;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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

public class UserManagementScreen {

    // ADD THESE DYNAMIC CONTAINERS
    private HBox metricsContainer;
    private VBox tableContainer;

    private List<UserModel> allFetchedUsers = new ArrayList<>();

    public BorderPane startUserManagementScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        ScrollPane scroller = new ScrollPane();
        scroller.setContent(createContent());
        GamerVaultStyles.applyStyledScrollPane(scroller);
        scroller.setFitToWidth(true);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setCenter(scroller);
        return root;
    }

    private VBox createContent() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(10, 20, 40, 0));

        HBox headerBox = createHeaderBox();
        HBox filtersRow = createFiltersRow();

        metricsContainer = new HBox(20);

        tableContainer = new VBox();
        GamerVaultStyles.applyGlassCard(tableContainer);
        HBox header = createUserRow(null, "PLAYER", "EMAIL", "ROLE", "JOINED", "STATUS", false, true, true);
        header.setStyle(
                "-fx-border-color: transparent transparent rgba(255,255,255,0.08) transparent; -fx-border-width: 0 0 1 0; -fx-padding: 15 20 15 20;");
        tableContainer.getChildren().add(header);

        container.getChildren().addAll(headerBox, metricsContainer, filtersRow, tableContainer);

        loadUsersData();

        return container;
    }

    /*
     * HEADER UI
     */
    private HBox createHeaderBox() {
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox textCol = new VBox(5);
        Text title = new Text("User Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));

        Text subtitle = new Text("Monitor player accounts, manage roles, and moderate the community ecosystem.");
        subtitle.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        textCol.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button exportBtn = new Button("📥 Export Directory");
        exportBtn.setPrefHeight(45);
        exportBtn.setPrefWidth(160);
        GamerVaultStyles.applyGhostButton(exportBtn);
        // Add a subtle border glow to make it stand out as an action
        exportBtn.setStyle(exportBtn.getStyle() + "-fx-border-color: " + GamerVaultStyles.ACCENT_PURPLE + ";");

        headerRow.getChildren().addAll(textCol, spacer, exportBtn);

        GamerVaultAnimations.fadeInUp(textCol, 0, 500);
        GamerVaultAnimations.fadeInUp(exportBtn, 100, 500);

        return headerRow;
    }

    private void loadUsersData() {
        new Thread(() -> {
            try {
                // Fetch all users from Firebase
                List<UserModel> allUsers = AdminController.fetchAllUsers();

                Platform.runLater(() -> {
                    this.allFetchedUsers = allUsers;
                    // 1. Calculate Real Metrics
                    int totalUsers = allUsers.size();
                    int verifiedCount = 0;
                    int adminCount = 0;

                    for (UserModel u : allUsers) {
                        if (u.isVerified())
                            verifiedCount++;
                        if ("ADMIN".equalsIgnoreCase(u.getRole()))
                            adminCount++;
                    }

                    // 2. Update Metrics Row
                    metricsContainer.getChildren().clear();
                    metricsContainer.getChildren().addAll(
                            createMetricCard("TOTAL USERS", "👥", String.valueOf(totalUsers), "Registered accounts",
                                    GamerVaultStyles.TEXT_MUTED),
                            createMetricCard("VERIFIED", "⭐", String.valueOf(verifiedCount), "Pro/Verified tier",
                                    GamerVaultStyles.ACCENT_ORANGE),
                            createMetricCard("ADMIN TEAM", "🛡", String.valueOf(adminCount), "System Administrators",
                                    GamerVaultStyles.ACCENT_CYAN),
                            createMetricCard("ACTIVE NOW", "🟢", "Live", "System Online",
                                    GamerVaultStyles.ACCENT_GREEN));

                    for (Node n : metricsContainer.getChildren()) {
                        HBox.setHgrow(n, Priority.ALWAYS);
                    }
                    GamerVaultAnimations.staggerFadeInChildren(metricsContainer, 80);

                    // 3. Clear existing table rows (keep the header at index 0)
                    if (tableContainer.getChildren().size() > 1) {
                        tableContainer.getChildren().subList(1, tableContainer.getChildren().size()).clear();
                    }

                    // 4. Populate Dynamic Table Rows
                    int delayIndex = 0;
                    for (UserModel user : allUsers) {

                        String name = user.getIgn() != null && !user.getIgn().isEmpty() ? user.getIgn()
                                : user.getPlayerName();
                        if (name == null)
                            name = "Unknown";

                        String email = user.getEmail() != null ? user.getEmail() : "No Email";
                        String role = user.getRole() != null ? user.getRole() : "PLAYER";

                        // Fallback for older users without a createdAt timestamp
                        String joined = user.getCreatedAt() != null ? user.getCreatedAt() : "Legacy User";

                        // Determine status visually
                        String status = "Active";
                        if ("ADMIN".equalsIgnoreCase(role))
                            status = "Admin";

                        HBox row = createUserRow(user, name, email, role, joined, status, false, false,
                                user.isVerified());
                        tableContainer.getChildren().add(row);

                        // Stagger entrance animation
                        GamerVaultAnimations.fadeInUp(row, delayIndex * 30, 400);
                        delayIndex++;
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.out.println("Failed to load admin user data: " + e.getMessage());
                });
            }
        }).start();
    }

    private VBox createMetricCard(String typeStr, String iconStr, String valueStr, String labelStr,
            String accentColor) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        GamerVaultStyles.applyGlassCard(card);
        GamerVaultAnimations.scaleOnHover(card, 1.03);

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Text icon = new Text(iconStr);
        icon.setFill(Color.web(accentColor));
        icon.setFont(Font.font(16));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Text typeText = new Text(typeStr);
        typeText.setFill(Color.web(accentColor));
        typeText.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        topRow.getChildren().addAll(icon, spacer, typeText);

        VBox valueBox = new VBox(4);
        Text mainValue = new Text(valueStr);
        mainValue.setFill(Color.web(GamerVaultStyles.TEXT_PRIMARY));
        mainValue.setFont(Font.font("Arial", FontWeight.BOLD, 36));

        Text bottomLabel = new Text(labelStr);
        bottomLabel.setFill(Color.web(GamerVaultStyles.TEXT_SECONDARY));
        bottomLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 13));

        valueBox.getChildren().addAll(mainValue, bottomLabel);

        card.getChildren().addAll(topRow, valueBox);
        return card;
    }

    /*
     * SEARCH AND FILTERS SECTION
     */
    private HBox createFiltersRow() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 20, 10, 20));
        row.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 8;");

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by IGN or Email...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAndRenderTable(newValue);
        });

        HBox searchBox = GamerVaultStyles.createStyledInput(searchField, GamerVaultStyles.ACCENT_PURPLE);
        searchBox.setPrefWidth(300);
        searchBox.setPrefHeight(40);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button roleFilter = new Button("Role: All  ▼");
        roleFilter.setPrefHeight(40);
        GamerVaultStyles.applyGhostButton(roleFilter);

        Button statusFilter = new Button("Status: Active  ▼");
        statusFilter.setPrefHeight(40);
        GamerVaultStyles.applyGhostButton(statusFilter);

        row.getChildren().addAll(searchBox, spacer, roleFilter, statusFilter);
        GamerVaultAnimations.fadeInUp(row, 200, 500);

        return row;
    }

    /*
     * TABLE ROW GENERATOR
     */
    private HBox createUserRow(UserModel userObj, String player, String email, String role, String date, String status,
            boolean isHighlighted, boolean isHeader, boolean isVerified) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);

        if (!isHeader) {
            String baseRowStyle = "-fx-padding: 15 20 15 20; -fx-background-color: transparent; -fx-border-color: transparent transparent rgba(255,255,255,0.03) transparent; -fx-border-width: 0 0 1 0;";
            String hoverRowStyle = "-fx-padding: 15 20 15 20; -fx-background-color: rgba(139,92,246,0.05); -fx-border-color: transparent transparent rgba(255,255,255,0.03) transparent; -fx-border-width: 0 0 1 0;";
            row.setStyle(baseRowStyle);
            row.setOnMouseEntered(e -> row.setStyle(hoverRowStyle));
            row.setOnMouseExited(e -> row.setStyle(baseRowStyle));
        }

        Font font = isHeader ? Font.font("Arial", FontWeight.BOLD, 11) : Font.font("Arial", FontWeight.BOLD, 14);
        Color textColor = isHeader ? Color.web(GamerVaultStyles.TEXT_MUTED) : Color.web(GamerVaultStyles.TEXT_PRIMARY);
        Color faintColor = isHeader ? Color.web(GamerVaultStyles.TEXT_MUTED)
                : Color.web(GamerVaultStyles.TEXT_SECONDARY);

        // 1. PLAYER COLUMN (Flexible width)
        HBox playerBox = new HBox(12);
        playerBox.setAlignment(Pos.CENTER_LEFT);
        playerBox.setPrefWidth(220);

        if (!isHeader) {
            StackPane avatarBox = new StackPane();
            avatarBox.setPrefSize(35, 35);
            avatarBox.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 6;");
            Text initial = new Text(player.substring(0, 1));
            initial.setFill(Color.web(GamerVaultStyles.ACCENT_PURPLE_LIGHT));
            initial.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            avatarBox.getChildren().add(initial);

            VBox nameCol = new VBox(2);
            HBox nameBadgeRow = new HBox(5);
            Text nameText = new Text(player);
            nameText.setFont(font);
            nameText.setFill(textColor);
            nameBadgeRow.getChildren().add(nameText);

            if (isVerified) {
                Text badge = new Text("⭐");
                badge.setFont(Font.font(10));
                nameBadgeRow.getChildren().add(badge);
            }

            Text subName = new Text("@GV_" + player.toLowerCase());
            subName.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
            subName.setFill(Color.web(GamerVaultStyles.TEXT_MUTED));

            nameCol.getChildren().addAll(nameBadgeRow, subName);
            playerBox.getChildren().addAll(avatarBox, nameCol);
        } else {
            Text nameHeader = new Text(player);
            nameHeader.setFont(font);
            nameHeader.setFill(textColor);
            playerBox.getChildren().add(nameHeader);
        }

        // 2. EMAIL COLUMN (Fixed Width)
        HBox emailBox = createCell(email, font, faintColor, 200, Pos.CENTER_LEFT);

        // 3. ROLE COLUMN (Fixed Width)
        HBox roleBox = createCell(role, font, textColor, 150, Pos.CENTER_LEFT);

        // 4. JOINED DATE COLUMN (Fixed Width)
        HBox dateBox = createCell(date, font, faintColor, 120, Pos.CENTER_LEFT);

        // 5. STATUS COLUMN
        HBox statusBox = new HBox();
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setPrefWidth(100);

        if (isHeader) {
            Text statusHeader = new Text(status);
            statusHeader.setFont(font);
            statusHeader.setFill(textColor);
            statusBox.getChildren().add(statusHeader);
        } else {
            StackPane badge = new StackPane();
            badge.setPadding(new Insets(4, 10, 4, 10));
            String bgColor, txtColor;

            if (status.equals("Active")) {
                bgColor = GamerVaultStyles.ACCENT_GREEN + "25";
                txtColor = GamerVaultStyles.ACCENT_GREEN;
            } else if (status.equals("Suspended")) {
                bgColor = GamerVaultStyles.ACCENT_ORANGE + "25";
                txtColor = GamerVaultStyles.ACCENT_ORANGE;
            } else {
                bgColor = "#EF444425"; // Red
                txtColor = "#EF4444";
            }

            badge.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 4;");
            Text badgeText = new Text(status);
            badgeText.setFill(Color.web(txtColor));
            badgeText.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            badge.getChildren().add(badgeText);
            statusBox.getChildren().add(badge);
        }

        // 6. ACTIONS COLUMN
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        if (isHeader) {
            Text actionsHeader = new Text("ACTIONS");
            actionsHeader.setFont(font);
            actionsHeader.setFill(textColor);
            actionsBox.getChildren().add(actionsHeader);
        } else {
            Button viewBtn = new Button("👁");
            Button banBtn = new Button("🚫");
            Button delBtn = new Button("🗑");

            // VIEW ACTION: Show a quick popup of the user's details
            viewBtn.setOnAction(e -> {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("User Profile");
                info.setHeaderText("Player: " + player);
                info.setContentText("Email: " + email + "\nRole: " + role + "\nJoined: " + date +
                        "\nStatus: " + (userObj.isSuspended() ? "Suspended" : "Active") +
                        "\nVerified: " + (userObj.isVerified() ? "Yes" : "No"));
                info.showAndWait();
            });

            // BAN/SUSPEND ACTION: Threaded database call
            banBtn.setOnAction(e -> {
                boolean isCurrentlySuspended = userObj.isSuspended();
                String actionStr = isCurrentlySuspended ? "Restore" : "Suspend";

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(actionStr + " User");
                alert.setHeaderText(actionStr + " " + player + "?");
                alert.setContentText(isCurrentlySuspended ? "This will allow the user to log in again."
                        : "This will prevent the user from logging in.");

                alert.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.OK) {
                        banBtn.setDisable(true);
                        new Thread(() -> {
                            // Make sure AdminController.suspendUser is implemented as provided in the
                            // previous step
                            boolean success = AdminController.suspendUser(userObj.getUserId(), !isCurrentlySuspended);

                            Platform.runLater(() -> {
                                banBtn.setDisable(false);
                                if (success) {
                                    userObj.setSuspended(!isCurrentlySuspended);
                                    // Refresh the table to reflect the new status badge visually
                                    loadUsersData();
                                } else {
                                    new Alert(Alert.AlertType.ERROR, "Failed to update suspension status.").show();
                                }
                            });
                        }).start();
                    }
                });
            });

            // DELETE ACTION: Threaded database call
            delBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Delete User");
                alert.setHeaderText("Permanently delete " + player + "?");
                alert.setContentText("This action cannot be undone. All data will be erased.");

                alert.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.OK) {
                        delBtn.setDisable(true);
                        new Thread(() -> {
                            // Make sure AdminController.deleteUser is implemented as provided in the
                            // previous step
                            boolean success = AdminController.deleteUser(userObj.getUserId());

                            Platform.runLater(() -> {
                                if (success) {
                                    // Remove the user from the UI lists dynamically
                                    tableContainer.getChildren().remove(row);
                                    allFetchedUsers.remove(userObj);
                                } else {
                                    delBtn.setDisable(false);
                                    new Alert(Alert.AlertType.ERROR, "Failed to delete user.").show();
                                }
                            });
                        }).start();
                    }
                });
            });

            for (Button btn : new Button[] { viewBtn, banBtn, delBtn }) {
                btn.setPrefSize(32, 32);
                GamerVaultStyles.applyGhostButton(btn);
            }

            banBtn.setOnMouseEntered(e -> banBtn.setStyle(
                    "-fx-background-color: rgba(245,158,11,0.15); -fx-text-fill: #F59E0B; -fx-background-radius: 8; -fx-cursor: hand;"));
            banBtn.setOnMouseExited(e -> GamerVaultStyles.applyGhostButton(banBtn));

            delBtn.setOnMouseEntered(e -> delBtn.setStyle(
                    "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #EF4444; -fx-background-radius: 8; -fx-cursor: hand;"));
            delBtn.setOnMouseExited(e -> GamerVaultStyles.applyGhostButton(delBtn));

            actionsBox.getChildren().addAll(viewBtn, banBtn, delBtn);
        }

        HBox.setHgrow(actionsBox, Priority.ALWAYS); // Push actions to the far right edge

        row.getChildren().addAll(playerBox, emailBox, roleBox, dateBox, statusBox, actionsBox);
        return row;
    }

    private HBox createCell(String text, Font font, Color color, double width, Pos alignment) {
        HBox box = new HBox();
        box.setAlignment(alignment);
        box.setPrefWidth(width);
        Text t = new Text(text);
        t.setFont(font);
        t.setFill(color);
        box.getChildren().add(t);
        return box;
    }

    private void filterAndRenderTable(String searchQuery) {
        String query = searchQuery.toLowerCase().trim();

        // Keep the header, clear the rest
        if (tableContainer.getChildren().size() > 1) {
            tableContainer.getChildren().subList(1, tableContainer.getChildren().size()).clear();
        }

        for (UserModel user : allFetchedUsers) {
            String name = user.getIgn() != null ? user.getIgn().toLowerCase() : user.getPlayerName().toLowerCase();
            String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";

            if (name.contains(query) || email.contains(query)) {

                String displayRole = user.getRole() != null ? user.getRole() : "PLAYER";
                String displayJoined = user.getCreatedAt() != null ? user.getCreatedAt() : "Legacy User";
                String status = "ADMIN".equalsIgnoreCase(displayRole) ? "Admin" : "Active";

                HBox row = createUserRow(user, user.getIgn() != null ? user.getIgn() : user.getPlayerName(),
                        user.getEmail(), displayRole, displayJoined, status, false, false, user.isVerified());

                tableContainer.getChildren().add(row);
            }
        }
    }
}