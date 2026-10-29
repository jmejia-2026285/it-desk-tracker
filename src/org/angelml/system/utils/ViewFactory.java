package org.angelml.system.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

public final class ViewFactory {

    private static final String VIEWS_PATH = "/org/angelml/system/view/";

    public record Loaded<T>(Parent root, T controller) {
    }

    private ViewFactory() {
    }

    public static <T> Loaded<T> load(String viewName) {
        URL location = ViewFactory.class.getResource(VIEWS_PATH + viewName + ".fxml");
        if (location == null) {
            throw new IllegalArgumentException("No existe la vista " + viewName);
        }
        FXMLLoader loader = new FXMLLoader(location);
        try {
            Parent root = loader.load();
            return new Loaded<>(root, loader.getController());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo cargar la vista " + viewName, e);
        }
    }
}
