package airport.exception;

/** Thrown when a UNIQUE value already exists (flight number, passport number, seat). */
public class DuplicateRecordException extends AirportException {
    public DuplicateRecordException(String message, Throwable cause) { super(message, cause); }
}
