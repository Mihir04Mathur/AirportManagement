package airport.core;

import java.nio.file.Path;

/**
 * Central project configuration for shared resources.
 * This keeps file and database locations consistent across all modules.
 */
public final class AirportContext {
    public static final Path APP_ROOT = Path.of("").toAbsolutePath().normalize();
    public static final Path DB_FILE = APP_ROOT.resolve("airport.db");
    public static final Path SQL_DIR = APP_ROOT.resolve("sql");
    public static final Path LOG_DIR = APP_ROOT.resolve("logs");
    public static final String DB_URL = "jdbc:sqlite:" + DB_FILE.toAbsolutePath().normalize();

    private AirportContext() {
    }
}
