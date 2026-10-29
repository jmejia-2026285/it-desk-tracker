package org.angelml.system.utils;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public final class AnimationUtils {

    private AnimationUtils() {
    }

    public static void fadeIn(Node node) {
        node.setOpacity(0);
        FadeTransition transition = new FadeTransition(Duration.millis(220), node);
        transition.setToValue(1);
        transition.play();
    }

    public static void shake(Node node) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(60), node);
        transition.setByX(8);
        transition.setCycleCount(6);
        transition.setAutoReverse(true);
        transition.setOnFinished(event -> node.setTranslateX(0));
        transition.play();
    }
}
