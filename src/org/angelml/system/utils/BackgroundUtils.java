package org.angelml.system.utils;

import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.net.URL;

/**
 * Pone fondo.jpg detras de una vista. Se carga por classpath desde Java, asi no
 * depende de como JavaFX resuelva la ruta relativa dentro del CSS.
 */
public final class BackgroundUtils {

    private static final String FONDO = "/org/angelml/system/resources/media/background/fondo-montanas.jpg";
    private static Image imagen;

    private BackgroundUtils() {
    }

    private static Image cargar() {
        if (imagen == null) {
            URL url = BackgroundUtils.class.getResource(FONDO);
            if (url == null) {
                System.err.println("[BackgroundUtils] No se encontro en el classpath: " + FONDO);
                return null;
            }
            System.out.println("[BackgroundUtils] Fondo cargado desde: " + url);
            // 2200 px de ancho: suficiente para pantalla completa y evita cargar 3506x2329
            imagen = new Image(url.toExternalForm(), 2200, 0, true, true);
            if (imagen.isError()) {
                System.err.println("[BackgroundUtils] Error leyendo la imagen: " + imagen.getException());
                imagen = null;
            }
        }
        return imagen;
    }

    /** Devuelve un StackPane con el fondo detras de la vista (la vista debe tener fondo transparente). */
    public static Parent withBackground(Parent view) {
        Region capa = new Region();
        capa.setMouseTransparent(true);
        Image img = cargar();
        if (img != null) {
            capa.setBackground(new Background(new BackgroundImage(
                    img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(1.0, 1.0, true, true, false, true)))); // cover
        }
        StackPane raiz = new StackPane(capa, view);
        raiz.setStyle("-fx-background-color: #1B2417;");
        // La raiz de la escena ahora es este panel: necesita el css para que
        // .root (fuente, colores del cristal) siga aplicando a la vista.
        URL css = BackgroundUtils.class.getResource("/org/angelml/system/resources/styles/styles.css");
        if (css != null) {
            raiz.getStylesheets().add(css.toExternalForm());
        }
        return raiz;
    }
}
