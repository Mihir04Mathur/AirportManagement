package airport.exception;

/** Thrown when a CSV / report file cannot be written (wraps an IOException). */
public class ExportException extends AirportException {
    public ExportException(String message) { super(message); }
    public ExportException(String message, Throwable cause) { super(message, cause); }
}
