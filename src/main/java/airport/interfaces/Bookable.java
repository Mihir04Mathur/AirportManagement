package airport.interfaces;

/**
 * INTERFACE: anything that can be booked (implemented by Flight).
 * Shows abstract methods, default methods and a static method.
 */
public interface Bookable {
    boolean isAvailable();
    int getAvailableSeats();
    int getTotalSeats();
    double calculateFare(String travelClass);

    /** Default method (Java 8+): shared behaviour inside an interface. */
    default boolean isFull() { return !isAvailable(); }

    default double getOccupancyPercent() {
        return getTotalSeats() == 0 ? 0 : (getTotalSeats() - getAvailableSeats()) * 100.0 / getTotalSeats();
    }

    /** Static method in an interface. */
    static boolean isValidClass(String travelClass) {
        return "ECONOMY".equalsIgnoreCase(travelClass) || "BUSINESS".equalsIgnoreCase(travelClass);
    }
}
