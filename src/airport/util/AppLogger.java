package airport.util;

import airport.core.AirportContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.logging.*;

/** Central logging using java.util.logging. Entries go to the console and to logs/airport.log. */
public final class AppLogger {
    public static final Path LOG_FILE = AirportContext.LOG_DIR.resolve("airport.log");
    private static final Logger LOGGER = Logger.getLogger("airport");
    private static final java.time.format.DateTimeFormatter STAMP = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static boolean ready;

    private AppLogger() { }

    /** One line per record:  2026-10-02 10:15:03 | INFO | message */
    private static class LineFormatter extends Formatter {
        @Override
        public String format(LogRecord r) {
            String level = r.getLevel() == Level.WARNING ? "WARN" : r.getLevel() == Level.SEVERE ? "ERROR"
                    : r.getLevel() == Level.FINE ? "DEBUG" : r.getLevel().getName();
            StringBuilder sb = new StringBuilder();
            sb.append(LocalDateTime.now().format(STAMP))
              .append(" | ").append(String.format("%-5s", level)).append(" | ").append(r.getMessage());
            if (r.getThrown() != null)
                sb.append("  [").append(r.getThrown().getClass().getSimpleName()).append(": ")
                  .append(r.getThrown().getMessage()).append("]");
            return sb.append(System.lineSeparator()).toString();
        }
    }

    public static synchronized void init() {
        if (ready) return;
        try {
            Files.createDirectories(AirportContext.LOG_DIR);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create log directory: " + AirportContext.LOG_DIR, e);
        }
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.ALL);
        Handler console = new ConsoleHandler();
        console.setLevel(Level.INFO);
        console.setFormatter(new LineFormatter());
        Handler file = new FileLogHandler(LOG_FILE);
        file.setLevel(Level.ALL);
        file.setFormatter(new LineFormatter());
        LOGGER.addHandler(console);
        LOGGER.addHandler(file);
        ready = true;
    }

    public static void info(String msg)  { init(); LOGGER.info(msg); }
    public static void warn(String msg)  { init(); LOGGER.warning(msg); }
    public static void debug(String msg) { init(); LOGGER.fine(msg); }
    public static void error(String msg, Throwable t) { init(); LOGGER.log(Level.SEVERE, msg, t); }

    /** Reads the whole log file (java.nio.file.Files). */
    public static List<String> readAll() {
        try {
            return Files.exists(LOG_FILE) ? Files.readAllLines(LOG_FILE, StandardCharsets.UTF_8) : Collections.emptyList();
        } catch (IOException e) {
            return List.of("Could not read the log file: " + e.getMessage());
        }
    }

    public static void clear() throws IOException {
        if (Files.exists(LOG_FILE)) Files.writeString(LOG_FILE, "", StandardCharsets.UTF_8);
    }
}
