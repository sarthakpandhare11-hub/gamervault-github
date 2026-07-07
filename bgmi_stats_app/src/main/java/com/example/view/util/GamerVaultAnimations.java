package com.example.view.util;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * Centralized animation and atmospheric-effect utility for GamerVault.
 * All methods are static helpers that can be called from any screen.
 * Inspired by the InfluMatch premium dark-theme design language.
 */
public class GamerVaultAnimations {

    // ─── FADE-IN-UP ENTRANCE ───────────────────────────────────────────────
    /**
     * Fades a node in from transparent while sliding it up.
     * Perfect for page-load entrance animations.
     *
     * @param node       The node to animate
     * @param delayMs    Delay before animation starts (for staggering)
     * @param durationMs Total animation duration
     */
    public static void fadeInUp(Node node, double delayMs, double durationMs) {
        node.setOpacity(0);
        node.setTranslateY(25);

        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delayMs));
        fade.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition slide = new TranslateTransition(Duration.millis(durationMs), node);
        slide.setFromY(25);
        slide.setToY(0);
        slide.setDelay(Duration.millis(delayMs));
        slide.setInterpolator(Interpolator.EASE_OUT);

        fade.play();
        slide.play();
    }

    // ─── SCALE ON HOVER ────────────────────────────────────────────────────
    /**
     * Attaches smooth scale-up on mouse enter and scale-down on mouse exit.
     *
     * @param node    The node to animate
     * @param scaleTo Target scale on hover (e.g., 1.03)
     */
    public static void scaleOnHover(Node node, double scaleTo) {
        node.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
            st.setToX(scaleTo);
            st.setToY(scaleTo);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();
        });
        node.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
            st.setToX(1.0);
            st.setToY(1.0);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();
        });
    }

    // ─── SCALE ON PRESS ────────────────────────────────────────────────────
    /**
     * Attaches pressed/released scale bounce for tactile button feedback.
     *
     * @param node The node (usually a Button) to animate
     */
    public static void scaleOnPress(Node node) {
        node.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(80), node);
            st.setToX(0.96);
            st.setToY(0.96);
            st.play();
        });
        node.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(80), node);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    // ─── COMBINED HOVER + PRESS ────────────────────────────────────────────
    /**
     * Combines scaleOnHover and scaleOnPress into a single call.
     * Use this for primary CTA buttons.
     */
    public static void scaleOnHoverAndPress(Node node, double hoverScale) {
        node.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
            st.setToX(hoverScale);
            st.setToY(hoverScale);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();
        });
        node.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
            st.setToX(1.0);
            st.setToY(1.0);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();
        });
        node.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(80), node);
            st.setToX(0.96);
            st.setToY(0.96);
            st.play();
        });
        node.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(80), node);
            st.setToX(hoverScale);
            st.setToY(hoverScale);
            st.play();
        });
    }

    // ─── PULSE GLOW (BREATHING) ────────────────────────────────────────────
    /**
     * Infinite subtle breathing scale animation.
     * Great for logos, hero elements, or accent shapes.
     */
    public static void pulseGlow(Node node, double durationSec) {
        ScaleTransition st = new ScaleTransition(Duration.seconds(durationSec), node);
        st.setFromX(1.0);
        st.setToX(1.06);
        st.setFromY(1.0);
        st.setToY(1.06);
        st.setCycleCount(Animation.INDEFINITE);
        st.setAutoReverse(true);
        st.setInterpolator(Interpolator.EASE_BOTH);
        st.play();
    }

    // ─── ATMOSPHERIC BLOB BUILDER ──────────────────────────────────────────
    /**
     * Creates a Gaussian-blurred atmospheric blob for backgrounds.
     */
    public static Circle buildBlob(double radius, String colorHex, double opacity, double blurRadius) {
        Circle blob = new Circle(radius);
        blob.setFill(Color.web(colorHex, opacity));
        blob.setEffect(new GaussianBlur(blurRadius));
        blob.setMouseTransparent(true);
        return blob;
    }

    // ─── ANIMATE BACKGROUND GLOW ──────────────────────────────────────────
    /**
     * Slowly drifts and scales a blob for an organic, atmospheric feel.
     */
    public static void animateGlow(Circle circle, double durationSec, double minX, double maxX, double minY, double maxY) {
        TranslateTransition tt = new TranslateTransition(Duration.seconds(durationSec), circle);
        tt.setFromX(minX);
        tt.setToX(maxX);
        tt.setFromY(minY);
        tt.setToY(maxY);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.setAutoReverse(true);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();

        ScaleTransition st = new ScaleTransition(Duration.seconds(durationSec * 0.8), circle);
        st.setFromX(0.9);
        st.setToX(1.1);
        st.setFromY(0.9);
        st.setToY(1.1);
        st.setCycleCount(Animation.INDEFINITE);
        st.setAutoReverse(true);
        st.setInterpolator(Interpolator.EASE_BOTH);
        st.play();
    }

    // ─── FLOATING BOUNCE ───────────────────────────────────────────────────
    /**
     * Infinite vertical bounce for floating badges or decorative elements.
     */
    public static void animateFloating(Node node, double durationSec, double fromY, double toY) {
        TranslateTransition tt = new TranslateTransition(Duration.seconds(durationSec), node);
        tt.setFromY(fromY);
        tt.setToY(toY);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.setAutoReverse(true);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }

    // ─── CURSOR GLOW ───────────────────────────────────────────────────────
    /**
     * Creates the faint glow circle that follows the mouse cursor.
     */
    public static Circle buildCursorGlow() {
        Circle glow = new Circle(120);
        glow.setFill(Color.web("#c4c0ff", 0.06));
        glow.setEffect(new GaussianBlur(90));
        glow.setMouseTransparent(true);
        StackPane.setAlignment(glow, Pos.TOP_LEFT);
        return glow;
    }

    /**
     * Attaches a mouse-move listener to a StackPane root so the cursor glow follows the mouse.
     */
    public static void attachCursorGlow(StackPane root, Circle cursorGlow) {
        root.setOnMouseMoved(e -> {
            cursorGlow.setTranslateX(e.getX() - cursorGlow.getRadius());
            cursorGlow.setTranslateY(e.getY() - cursorGlow.getRadius());
        });
    }

    // ─── STAGGER CHILDREN FADE-IN ──────────────────────────────────────────
    /**
     * Animates each child of a Pane with a staggered fadeInUp effect.
     *
     * @param parent         The container whose children to animate
     * @param staggerDelayMs Delay increment between each child
     */
    public static void staggerFadeInChildren(Pane parent, double staggerDelayMs) {
        for (int i = 0; i < parent.getChildren().size(); i++) {
            Node child = parent.getChildren().get(i);
            fadeInUp(child, i * staggerDelayMs, 400);
        }
    }

    // ─── FOCUS GLOW FOR INPUT FIELDS ───────────────────────────────────────
    /**
     * Applies a glowing border effect when an input field gains focus.
     *
     * @param container   The HBox/VBox wrapping the input
     * @param inputField  The TextField or PasswordField
     * @param accentColor The glow accent color hex (e.g., "#8B5CF6")
     */
    public static void applyFocusGlow(Node container, Node inputField, String accentColor) {
        String defaultStyle = "-fx-background-color: #1A1F35; -fx-background-radius: 10; "
                + "-fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 10; "
                + "-fx-border-width: 1; -fx-padding: 0 14;";

        String focusStyle = "-fx-background-color: #1A1F35; -fx-background-radius: 10; "
                + "-fx-border-color: " + accentColor + "; -fx-border-radius: 10; "
                + "-fx-border-width: 1.5; -fx-padding: 0 14; "
                + "-fx-effect: dropshadow(three-pass-box, " + accentColor + "40, 10, 0, 0, 0);";

        container.setStyle(defaultStyle);

        inputField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            container.setStyle(newVal ? focusStyle : defaultStyle);
        });
    }

    // ─── SHIMMER / COLOR CYCLE (for premium accent elements) ───────────────
    /**
     * Creates a subtle color-cycling effect on a DropShadow, useful for logos or badges.
     */
    public static void shimmerEffect(Node node, String color1, String color2, double durationSec) {
        DropShadow glow = new DropShadow(20, Color.web(color1, 0.5));
        node.setEffect(glow);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(glow.colorProperty(), Color.web(color1, 0.5))),
                new KeyFrame(Duration.seconds(durationSec),
                        new KeyValue(glow.colorProperty(), Color.web(color2, 0.5)))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.setAutoReverse(true);
        timeline.play();
    }
}
