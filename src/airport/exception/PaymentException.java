package airport.exception;

/** Thrown when payment details are invalid or payment fails. */
public class PaymentException extends AirportException {
    public PaymentException(String message) { super(message); }
}
