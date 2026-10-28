/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.angelml.system.utils;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.TextInputControl;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class AnimationUtils {

    // Constructor privado para evitar instanciación
    private AnimationUtils() {
    }

    // --- Ya existentes ---

    public static void aplicarFadeIn(Node nodo) {
        nodo.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(400), nodo);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    public static void aplicarEfectoHover(Node nodo) {
        ScaleTransition st = new ScaleTransition(Duration.millis(120), nodo);
        nodo.setOnMouseEntered(e -> {
            st.setToX(1.04);
            st.setToY(1.04);
            st.playFromStart();
        });
        nodo.setOnMouseExited(e -> {
            st.setToX(1.0);
            st.setToY(1.0);
            st.playFromStart();
        });
    }

    // --- Nuevas (punto 1: hover extendido reutiliza aplicarEfectoHover arriba) ---

    /**
     * Variante de hover pensada para tarjetas (libro-tarjeta): eleva
     * ligeramente el nodo y añade sombra dorada en vez de solo escalar.
     */
    public static void aplicarEfectoHoverTarjeta(Node nodo) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(150), nodo);
        DropShadow sombra = new DropShadow(18, Color.web("#E0BC88", 0.0));
        nodo.setEffect(sombra);

        Timeline sombraIn = new Timeline(
                new KeyFrame(Duration.millis(150),
                        new KeyValue(sombra.colorProperty(), Color.web("#E0BC88", 0.35))));
        Timeline sombraOut = new Timeline(
                new KeyFrame(Duration.millis(150),
                        new KeyValue(sombra.colorProperty(), Color.web("#E0BC88", 0.0))));

        nodo.setOnMouseEntered(e -> {
            tt.setToY(-4);
            tt.playFromStart();
            sombraIn.playFromStart();
        });
        nodo.setOnMouseExited(e -> {
            tt.setToY(0);
            tt.playFromStart();
            sombraOut.playFromStart();
        });
    }

    // --- Punto 2: entrada deslizante por nodo ---

    /**
     * Entrada con desvanecido + deslizamiento horizontal. desplazamientoX
     * positivo entra desde la derecha, negativo desde la izquierda.
     */
    public static void aplicarSlideIn(Node nodo, double desplazamientoX) {
        nodo.setOpacity(0);
        nodo.setTranslateX(desplazamientoX);

        FadeTransition fade = new FadeTransition(Duration.millis(320), nodo);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(320), nodo);
        slide.setFromX(desplazamientoX);
        slide.setToX(0);

        ParallelTransition entrada = new ParallelTransition(nodo, fade, slide);
        entrada.play();
    }

    // --- Punto 3: foco animado en campos de texto ---

    /**
     * Brillo dorado animado alrededor del campo al enfocar/desenfocar,
     * ya que -fx-border-color no admite transición nativa en CSS de JavaFX.
     */
    public static void aplicarFocoAnimado(TextInputControl campo) {
        DropShadow brillo = new DropShadow(0, Color.web("#E0BC88", 0.0));
        campo.setEffect(brillo);

        campo.focusedProperty().addListener((obs, sinFoco, conFoco) -> {
            Timeline t = new Timeline();
            if (conFoco) {
                t.getKeyFrames().add(new KeyFrame(Duration.millis(180),
                        new KeyValue(brillo.radiusProperty(), 10),
                        new KeyValue(brillo.colorProperty(), Color.web("#E0BC88", 0.55))));
            } else {
                t.getKeyFrames().add(new KeyFrame(Duration.millis(180),
                        new KeyValue(brillo.radiusProperty(), 0),
                        new KeyValue(brillo.colorProperty(), Color.web("#E0BC88", 0.0))));
            }
            t.play();
        });
    }

    // --- Punto 4: sacudida de error ---

    /**
     * Shake horizontal para mensajes o campos de error (login, registro).
     */
    public static void aplicarSacudida(Node nodo) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(60), nodo);
        shake.setFromX(0);
        shake.setByX(8);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> nodo.setTranslateX(0));
        shake.play();
    }
}