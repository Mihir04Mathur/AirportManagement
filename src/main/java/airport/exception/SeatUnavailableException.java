package airport.exception;

/** Thrown when a flight has no free seats. A special kind of InvalidBookingException. */
public class SeatUnavailableException extends InvalidBookingException {
    public SeatUnavailableException(String flightNo) {
        super("No seats available on flight " + flightNo + ".");
    }
}
