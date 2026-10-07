package airport.exception;

/** Thrown when a booking / cancellation / check-in is attempted after departure. */
public class FlightDepartedException extends InvalidBookingException {
    public FlightDepartedException(String flightNo, String departure) {
        super("Flight " + flightNo + " has already departed (" + departure + ").");
    }
}
