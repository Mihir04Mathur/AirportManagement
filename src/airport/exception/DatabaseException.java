package airport.exception;

import java.sql.SQLException;

/** Wraps a low level SQLException (exception chaining) with a friendly message. */
public class DatabaseException extends AirportException {
    private final int errorCode;

    public DatabaseException(String message, SQLException cause) {
        super(message, cause);
        this.errorCode = cause.getErrorCode();
    }

    public int getErrorCode() { return errorCode; }
}
