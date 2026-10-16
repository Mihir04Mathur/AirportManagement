package airport.exception;

/** Thrown for an invalid booking / check-in request. */
public class InvalidBookingException extends AirportException {
    public InvalidBookingException(String message) { super(message); }
}
