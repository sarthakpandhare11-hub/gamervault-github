package com.example.view.util;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class GamerVaultStyles {

    // ─── GLOBAL COLOR PALETTE ──────────────────────────────────────────────
    public static final String BASE_BG = "#0A0E1A";
    public static final String SIDEBAR_BG = "#0B1324";

    // Accents
    public static final String ACCENT_PURPLE = "#8B5CF6";
    public static final String ACCENT_PURPLE_DARK = "#6D28D9";
    public static final String ACCENT_PURPLE_LIGHT = "#C084FC";
    public static final String ACCENT_GREEN = "#10B981";
    public static final String ACCENT_ORANGE = "#F59E0B";
    public static final String ACCENT_CYAN = "#00FFFF";

    // Typography
    public static final String TEXT_PRIMARY = "#FFFFFF";
    public static final String TEXT_SECONDARY = "#c7c4d8";
    public static final String TEXT_MUTED = "#918fa1";

    // UI Elements
    public static final String INPUT_BG = "#1A1F35";
    public static final String CARD_BG = "rgba(17,24,39,0.85)";
    public static final String CARD_BORDER = "rgba(255,255,255,0.08)";

    // ─── GLASSMORPHISM & ELITE CARD STYLES ─────────────────────────────────

    /**
     * Standard Glassmorphism Card Style
     */
    public static void applyGlassCard(Region card) {
        String style = "-fx-background-color: " + CARD_BG + "; " +
                "-fx-background-radius: 14; " +
                "-fx-border-color: " + CARD_BORDER + "; " +
                "-fx-border-radius: 14; " +
                "-fx-border-width: 1;";
        card.setStyle(style);

        DropShadow dropShadow = new DropShadow(25, Color.rgb(0, 0, 0, 0.6));
        dropShadow.setOffsetY(10);
        card.setEffect(dropShadow);
    }

    /**
     * Interactive Glassmorphism Card (1 Parameter - Default Hover)
     */
    public static void applyInteractiveGlassCard(Region card) {
        applyInteractiveGlassCard(card, "rgba(255,255,255,0.15)"); // Default bright border
    }

    /**
     * Interactive Glassmorphism Card (2 Parameters - Custom Hover Color)
     */
    public static void applyInteractiveGlassCard(Region card, String hoverBorderColorHex) {
        applyGlassCard(card);

        String hoverStyle = "-fx-background-color: rgba(25, 33, 50, 0.95); " +
                "-fx-background-radius: 14; " +
                "-fx-border-color: " + hoverBorderColorHex + "; " +
                "-fx-border-radius: 14; " +
                "-fx-border-width: 1;";

        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> applyGlassCard(card));
    }

    /**
     * Original Elite Static Card Style (Milled solid look)
     */
    public static void applyEliteCard(Region card) {
        String defaultStyle = "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #161D2D, #0A0F18); " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: linear-gradient(from 0% 0% to 100% 100%, rgba(255,255,255,0.08), rgba(255,255,255,0.01)); "
                +
                "-fx-border-width: 1.5; " +
                "-fx-border-radius: 12;";

        card.setStyle(defaultStyle);

        DropShadow dropShadow = new DropShadow(20, Color.rgb(0, 0, 0, 0.8));
        dropShadow.setOffsetY(10);

        InnerShadow innerBevel = new InnerShadow(4, Color.rgb(255, 255, 255, 0.05));
        innerBevel.setOffsetY(1);
        innerBevel.setOffsetX(1);

        dropShadow.setInput(innerBevel);
        card.setEffect(dropShadow);
    }

    /**
     * Aggressive neon hover effect for cards.
     */
    public static void applyInteractiveNeonCard(Region card, String neonAccentHex) {
        applyEliteCard(card);

        String hoverStyle = "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1A2235, #0A0F18); " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: " + neonAccentHex + "; " +
                "-fx-border-width: 1.5; " +
                "-fx-border-radius: 12;";

        DropShadow hoverGlow = new DropShadow(35, Color.web(neonAccentHex, 0.6));
        hoverGlow.setOffsetY(5);

        card.setOnMouseEntered(e -> {
            card.setStyle(hoverStyle);
            card.setEffect(hoverGlow);
        });

        card.setOnMouseExited(e -> applyEliteCard(card));
    }

    // ─── UI COMPONENTS & HELPERS ───────────────────────────────────────────

    /**
     * Wraps a TextField in a styled HBox that glows when the field is focused.
     */
    public static HBox createStyledInput(TextField inputField, String accentColorHex) {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER_LEFT);

        String defaultStyle = "-fx-background-color: " + INPUT_BG + "; -fx-background-radius: 10; " +
                "-fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 10; " +
                "-fx-padding: 0 15 0 15; -fx-border-width: 1;";

        String focusStyle = "-fx-background-color: " + INPUT_BG + "; -fx-background-radius: 10; " +
                "-fx-border-color: " + accentColorHex + "; -fx-border-radius: 10; " +
                "-fx-padding: 0 15 0 15; -fx-border-width: 1.5; " +
                "-fx-effect: dropshadow(three-pass-box, " + accentColorHex + "60, 10, 0, 0, 0);";

        container.setStyle(defaultStyle);

        inputField.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-prompt-text-fill: #6B7280;");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        inputField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            container.setStyle(newVal ? focusStyle : defaultStyle);
        });

        container.getChildren().add(inputField);
        return container;
    }

    /**
     * Styles transparent "Ghost" buttons.
     */
    public static void applyGhostButton(Button btn) {
        String baseStyle = "-fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.1); " +
                "-fx-border-radius: 8; -fx-text-fill: " + TEXT_SECONDARY
                + "; -fx-cursor: hand; -fx-background-radius: 8;";

        String hoverStyle = "-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.15); " +
                "-fx-border-radius: 8; -fx-text-fill: " + TEXT_PRIMARY
                + "; -fx-cursor: hand; -fx-background-radius: 8;";

        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
    }

    /**
     * Styles filled neon buttons.
     */
    public static void applyNeonButton(Button btn, String baseDarkHex, String neonHighlightHex) {
        String baseStyle = "-fx-background-color: " + baseDarkHex + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; " +
                "-fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8; -fx-cursor: hand;";

        String hoverStyle = "-fx-background-color: linear-gradient(to bottom right, " + neonHighlightHex + ", "
                + baseDarkHex + "); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; " +
                "-fx-background-radius: 8; -fx-border-color: " + neonHighlightHex + "; " +
                "-fx-border-radius: 8; -fx-cursor: hand;";

        DropShadow buttonGlow = new DropShadow(20, Color.web(neonHighlightHex, 0.5));

        btn.setStyle(baseStyle);

        btn.setOnMouseEntered(e -> {
            btn.setStyle(hoverStyle);
            btn.setEffect(buttonGlow);
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(baseStyle);
            btn.setEffect(null);
        });
    }

    /**
     * Styles gradient buttons with hover glow and custom text color.
     */
    public static void applyGradientButton(Button btn, String colorStart, String colorEnd, String textColor) {
        String baseStyle = "-fx-background-color: linear-gradient(to right, " + colorStart + ", " + colorEnd + "); " +
                "-fx-text-fill: " + textColor + "; -fx-font-weight: bold; -fx-font-size: 14px; " +
                "-fx-background-radius: 8; -fx-cursor: hand;";

        String hoverStyle = "-fx-background-color: linear-gradient(to right, " + colorStart + "DD, " + colorEnd
                + "DD); " +
                "-fx-text-fill: " + textColor + "; -fx-font-weight: bold; -fx-font-size: 14px; " +
                "-fx-background-radius: 8; -fx-cursor: hand;";

        DropShadow glow = new DropShadow(20, Color.web(colorStart, 0.5));

        btn.setStyle(baseStyle);

        btn.setOnMouseEntered(e -> {
            btn.setStyle(hoverStyle);
            btn.setEffect(glow);
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(baseStyle);
            btn.setEffect(null);
        });
    }

    /**
     * Strips the default gray background from a ScrollPane.
     */
    public static void applyStyledScrollPane(ScrollPane scroller) {
        scroller.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
    }

    /**
     * Applies an animated, glowing atmospheric background to a StackPane.
     * Inserts the glowing blobs at the lowest Z-index (back) so they sit behind the
     * UI.
     */
    public static void applyAtmosphericBackground(StackPane rootStack) {
        // Blob 1: Primary Purple (Top Left)
        Circle blobPurple = buildBlob(280, ACCENT_PURPLE, 0.12, 160);
        StackPane.setAlignment(blobPurple, Pos.TOP_LEFT);
        blobPurple.setTranslateX(-80);
        blobPurple.setTranslateY(-80);

        // Blob 2: Accent Green (Bottom Right)
        Circle blobGreen = buildBlob(320, ACCENT_GREEN, 0.08, 180);
        StackPane.setAlignment(blobGreen, Pos.BOTTOM_RIGHT);
        blobGreen.setTranslateX(100);
        blobGreen.setTranslateY(100);

        // Blob 3: Elite Orange (Center Left)
        Circle blobOrange = buildBlob(220, ACCENT_ORANGE, 0.06, 130);
        StackPane.setAlignment(blobOrange, Pos.CENTER_LEFT);
        blobOrange.setTranslateX(-50);
        blobOrange.setTranslateY(50);

        // Animate the blobs (Smooth Translation)
        animateFloating(blobPurple, 15, -80, 20, -80, 30);
        animateFloating(blobGreen, 18, 100, 40, 100, 20);
        animateFloating(blobOrange, 22, -50, 30, 50, -20);

        // Add blobs to the very back of the StackPane (indexes 0, 1, 2)
        // This ensures they render behind your actual UI components
        rootStack.getChildren().add(0, blobPurple);
        rootStack.getChildren().add(1, blobGreen);
        rootStack.getChildren().add(2, blobOrange);
    }

    // ─── PRIVATE HELPERS FOR ATMOSPHERIC BACKGROUND ────────────────────────

    /**
     * Builds the blurred background shape
     */
    private static Circle buildBlob(double radius, String colorHex, double opacity, double blurRadius) {
        Circle blob = new Circle(radius);
        blob.setFill(Color.web(colorHex, opacity));
        blob.setEffect(new GaussianBlur(blurRadius));
        blob.setMouseTransparent(true); // CRITICAL: Ensures it doesn't block mouse clicks on the UI
        return blob;
    }

    /**
     * Animates the floating motion infinitely
     */
    private static void animateFloating(Node node, double durationSec, double fromX, double toX,
            double fromY, double toY) {
        TranslateTransition tt = new TranslateTransition(
                Duration.seconds(durationSec), node);
        tt.setFromX(fromX);
        tt.setToX(toX);
        tt.setFromY(fromY);
        tt.setToY(toY);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.setAutoReverse(true);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }
}