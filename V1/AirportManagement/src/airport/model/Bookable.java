package airport.model;

/** INTERFACE: anything that can be booked (implemented by Flight). */
public interface Bookable {
    boolean isAvailable();
    int getAvailableSeats();
    double calculateFare(String travelClass);
}
