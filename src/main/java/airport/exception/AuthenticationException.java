package airport.exception;

/** Thrown when login fails (wrong password, unknown user, account temporarily locked). */
public class AuthenticationException extends AirportException {
    public AuthenticationException(String message) { super(message); }
}
