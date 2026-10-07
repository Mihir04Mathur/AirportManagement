package airport.exception;

/** Thrown when a flight has no free seats. */
public class SeatUnavailableException extends AirportException {
    public SeatUnavailableException(String flightNo) {
        super("No seats available on flight " + flightNo + ".");
    }
}
