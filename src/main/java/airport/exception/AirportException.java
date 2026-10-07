package airport.exception;

/** Base checked exception for the whole application. */
public class AirportException extends Exception {
    public AirportException(String message) { super(message); }
    public AirportException(String message, Throwable cause) { super(message, cause); }
}
