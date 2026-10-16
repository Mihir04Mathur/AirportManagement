package airport.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/** A java.util.logging Handler that appends every log line to a text file. */
public class FileLogHandler extends Handler {
    private final Path file;

    public FileLogHandler(Path file) { this.file = file; }

    @Override
    public synchronized void publish(LogRecord record) {
        if (!isLoggable(record)) return;
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.writeString(file, getFormatter().format(record), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            reportError("Could not write log file", e, ErrorManager.WRITE_FAILURE);
        }
    }

    @Override public void flush() { }
    @Override public void close() { }
}
