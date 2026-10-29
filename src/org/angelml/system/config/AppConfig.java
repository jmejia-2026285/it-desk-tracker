package org.angelml.system.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppConfig {

    public static final String APP_NAME = "IT Desk Tracker";
    public static final String FOLIO_PREFIX = "NEX";
    public static final int MIN_PASSWORD_LENGTH = 8;

    private static final String DATABASE_FILE = "it-desk-tracker.db";
    private static final String DATA_FOLDER = "ITDeskTracker";

    private AppConfig() {
    }

    public static Path dataDir() {
        String appData = System.getenv("APPDATA");
        return appData != null && !appData.isBlank()
                ? Paths.get(appData, DATA_FOLDER)
                : Paths.get(System.getProperty("user.home"), "." + DATA_FOLDER.toLowerCase());
    }

    public static Path databasePath() {
        String override = System.getProperty("itdesk.db");
        return override != null && !override.isBlank() ? Paths.get(override) : dataDir().resolve(DATABASE_FILE);
    }

    public static Path imagesDir() {
        return databasePath().toAbsolutePath().getParent().resolve("imagenes");
    }
}
