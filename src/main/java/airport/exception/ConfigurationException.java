package airport.exception;

/**
 * UNCHECKED exception (extends RuntimeException): a setup problem such as a missing
 * driver jar or missing sql folder. Callers are not forced to catch it.
 */
public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message) { super(message); }
    public ConfigurationException(String message, Throwable cause) { super(message, cause); }
}
