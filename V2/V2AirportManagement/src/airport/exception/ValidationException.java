package airport.exception;

/** Thrown when the user types invalid input (empty field, wrong format, out of range). */
public class ValidationException extends AirportException {
    public ValidationException(String message) { super(message); }
}
