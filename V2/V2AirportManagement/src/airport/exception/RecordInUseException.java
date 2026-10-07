package airport.exception;

/** Thrown when a record cannot be deleted because other records (bookings) depend on it. */
public class RecordInUseException extends AirportException {
    public RecordInUseException(String message, Throwable cause) { super(message, cause); }
}
