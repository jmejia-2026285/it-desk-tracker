package org.angelml.system.utils;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.angelml.system.config.AppConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class ImagenUtils {

    private static final long MAX_BYTES = 8L * 1024 * 1024;
    private static final List<String> EXTENSIONS = List.of("png", "jpg", "jpeg", "gif", "bmp");

    private ImagenUtils() {
    }

    public static File chooseImage(Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar imagen del dispositivo");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        return chooser.showOpenDialog(owner);
    }

    public static String store(File source) throws IOException {
        String name = source.getName().toLowerCase(Locale.ROOT);
        String extension = name.substring(name.lastIndexOf('.') + 1);
        if (!name.contains(".") || !EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Solo se permiten imágenes PNG, JPG, GIF o BMP.");
        }
        if (source.length() > MAX_BYTES) {
            throw new IllegalArgumentException("La imagen no puede pesar más de 8 MB.");
        }
        Files.createDirectories(AppConfig.imagesDir());
        String stored = UUID.randomUUID() + "." + extension;
        Files.copy(source.toPath(), AppConfig.imagesDir().resolve(stored), StandardCopyOption.REPLACE_EXISTING);
        return stored;
    }

    public static Image load(String storedName) {
        if (ValidationsUtils.isBlank(storedName)) {
            return null;
        }
        Path path = AppConfig.imagesDir().resolve(storedName).normalize();
        if (!path.startsWith(AppConfig.imagesDir().normalize()) || !Files.exists(path)) {
            return null;
        }
        return new Image(path.toUri().toString(), 480, 320, true, true);
    }

    public static void delete(String storedName) {
        if (ValidationsUtils.isBlank(storedName)) {
            return;
        }
        Path path = AppConfig.imagesDir().resolve(storedName).normalize();
        if (path.startsWith(AppConfig.imagesDir().normalize())) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
                return;
            }
        }
    }
}
