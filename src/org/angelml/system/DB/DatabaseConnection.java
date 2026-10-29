package org.angelml.system.DB;

import org.angelml.system.config.AppConfig;

import org.sqlite.SQLiteConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;


public final class DatabaseConnection {

    private static final int SCHEMA_VERSION = 1;
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final List<String> SCHEMA = List.of(
            """
            create table if not exists tecnico (
                id_tecnico integer primary key autoincrement,
                nombre text not null,
                email text not null unique,
                password text not null,
                rol text not null check (rol in ('ADMIN', 'TECNICO')),
                activo integer not null default 1 check (activo in (0, 1))
            )""",
            """
            create table if not exists dispositivo (
                id_dispositivo integer primary key autoincrement,
                tipo text not null,
                marca text not null,
                modelo text not null,
                serie text,
                cliente text not null,
                telefono text not null,
                correo text,
                problematica text not null,
                diagnostico text,
                accesorios text,
                notas text,
                imagen text,
                estado text not null default 'PENDIENTE' check (estado in ('PENDIENTE', 'EN_PROCESO', 'COMPLETADO')),
                fecha_ingreso text not null default (datetime('now', 'localtime')),
                fecha_estimada text,
                id_tecnico_recibe integer references tecnico (id_tecnico) on delete set null
            )""",
            """
            create table if not exists ticket (
                id_ticket integer primary key autoincrement,
                id_dispositivo integer not null references dispositivo (id_dispositivo) on delete cascade,
                id_tecnico integer references tecnico (id_tecnico) on delete set null,
                folio text not null unique,
                descripcion text not null,
                estado text not null default 'PENDIENTE' check (estado in ('PENDIENTE', 'EN_PROCESO', 'COMPLETADO')),
                fecha_ingreso text not null default (datetime('now', 'localtime')),
                fecha_completado text,
                impreso integer not null default 0 check (impreso in (0, 1)),
                fecha_impresion text
            )""",
            """
            create table if not exists impresion_ticket (
                id_impresion integer primary key autoincrement,
                id_ticket integer not null references ticket (id_ticket) on delete cascade,
                tipo text not null check (tipo in ('EQUIPO', 'CLIENTE')),
                id_tecnico integer references tecnico (id_tecnico) on delete set null,
                fecha_impresion text not null default (datetime('now', 'localtime'))
            )""",
            """
            create table if not exists cita (
                id_cita integer primary key autoincrement,
                id_tecnico integer not null references tecnico (id_tecnico) on delete cascade,
                titulo text not null,
                descripcion text,
                fecha text not null,
                hora text not null,
                completada integer not null default 0 check (completada in (0, 1))
            )""",
            "create index if not exists idx_dispositivo_estado on dispositivo (estado)",
            "create index if not exists idx_dispositivo_cliente on dispositivo (cliente)",
            "create index if not exists idx_ticket_dispositivo on ticket (id_dispositivo)",
            "create index if not exists idx_ticket_estado on ticket (estado)",
            "create index if not exists idx_cita_fecha on cita (fecha, hora)"
    );

    private static DatabaseConnection instance;

    private final Path file;
    private final String url;
    private final Properties properties;

    private DatabaseConnection() {
        this.file = AppConfig.databasePath().toAbsolutePath();
        try {
            Files.createDirectories(file.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear la carpeta de datos: " + file.getParent(), e);
        }
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        config.setJournalMode(SQLiteConfig.JournalMode.WAL);
        config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);
        config.setBusyTimeout(5000);
        this.url = "jdbc:sqlite:" + file;
        this.properties = config.toProperties();
        prepareSchema();
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public static synchronized void reset() {
        instance = null;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, properties);
    }

    public Path getFile() {
        return file;
    }

    public static int lastInsertId(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("select last_insert_rowid()")) {
            result.next();
            return result.getInt(1);
        }
    }

    public static LocalDateTime toDateTime(String text) {
        return text == null ? null : LocalDateTime.parse(text, DATE_TIME);
    }

    public static LocalDate toDate(String text) {
        return text == null ? null : LocalDate.parse(text);
    }

    public static LocalTime toTime(String text) {
        return text == null ? null : LocalTime.parse(text);
    }

    private void prepareSchema() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            int version = 0;
            try (ResultSet result = statement.executeQuery("pragma user_version")) {
                if (result.next()) {
                    version = result.getInt(1);
                }
            }
            if (version >= SCHEMA_VERSION) {
                return;
            }
            connection.setAutoCommit(false);
            try {
                for (String sentence : SCHEMA) {
                    statement.execute(sentence);
                }
                statement.execute("pragma user_version = " + SCHEMA_VERSION);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo abrir la base de datos en " + file, e);
        }
    }
}
