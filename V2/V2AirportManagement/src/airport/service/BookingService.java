package airport.service;

import airport.db.BookingDAO;
import airport.db.FlightDAO;
import airport.db.PassengerDAO;
import airport.exception.*;
import airport.interfaces.Bookable;
import airport.model.Booking;
import airport.model.Flight;
import airport.model.Passenger;
import airport.model.Payment;
import airport.util.AppLogger;
import airport.util.FormatUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Business-rule layer between the GUI and the DAO classes.
 * Adds rules (departed flights, passport for international trips, refund policy), logging and exception handling.
 */
public class BookingService {
    /** Refund policy: 90% of the fare if cancelled 24 hours or more before departure, otherwise 50%. */
    public static final double REFUND_EARLY = 0.90, REFUND_LATE = 0.50;

    private final BookingDAO bookingDao = new BookingDAO();
    private final FlightDAO flightDao = new FlightDAO();
    private final PassengerDAO passengerDao = new PassengerDAO();

    // ---------- helpers ----------
    private boolean hasDeparted(Flight f) {
        LocalDateTime dep = FormatUtil.parse(f.getDepartureTime());
        return dep != null && dep.isBefore(LocalDateTime.now());
    }

    /** Flights that still have seats and have not departed yet, earliest first (Stream API). */
    public List<Flight> getBookableFlights() throws AirportException {
        return flightDao.getAll().stream()
                .filter(Bookable::isAvailable)
                .filter(f -> !hasDeparted(f))
                .sorted(Comparator.comparing(Flight::getDepartureTime))
                .collect(Collectors.toList());
    }

    /** The next few departures for the home screen. */
    public List<Flight> getUpcomingFlights(int limit) throws AirportException {
        return flightDao.getAll().stream()
                .filter(f -> !hasDeparted(f))
                .sorted(Comparator.comparing(Flight::getDepartureTime))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Set<String> getTakenSeats(int flightId) throws AirportException { return bookingDao.getTakenSeats(flightId); }

    // ---------- booking (overloaded) ----------
    public Booking bookTicket(int passengerId, int flightId, String travelClass, Payment payment) throws AirportException {
        return bookTicket(passengerId, flightId, null, travelClass, payment);
    }

    /** Book a specific seat (null = best available seat for the class). */
    public Booking bookTicket(int passengerId, int flightId, String seatNo, String travelClass, Payment payment) throws AirportException {
        try {
            if (!Bookable.isValidClass(travelClass))
                throw new InvalidBookingException("Travel class must be ECONOMY or BUSINESS.");
            Passenger p = passengerDao.findById(passengerId)
                    .orElseThrow(() -> new RecordNotFoundException("Passenger", passengerId));
            Flight f = flightDao.findById(flightId)
                    .orElseThrow(() -> new RecordNotFoundException("Flight", flightId));
            if (hasDeparted(f)) throw new FlightDepartedException(f.getFlightNo(), f.getDepartureTime());
            if ("INTERNATIONAL".equals(f.getType()) && (p.getPassportNo() == null || p.getPassportNo().isBlank()))
                throw new InvalidBookingException("International travel needs a passport number. Add it in Manage Passengers.");

            Booking b = bookingDao.bookTicket(passengerId, flightId, seatNo, travelClass, payment);
            AppLogger.info(String.format("Booking #%d confirmed: %s on %s, seat %s, %s (%s)",
                    b.getId(), p.getName(), f.getFlightNo(), b.getSeatNo(), FormatUtil.money(payment.getTotalCharge()), payment.getMethod()));
            return b;
        } catch (SeatUnavailableException | FlightDepartedException e) {
            AppLogger.warn("Booking refused: " + e.getMessage());
            throw e;
        } catch (PaymentException e) {
            AppLogger.warn("Payment failed: " + e.getMessage());
            throw e;
        } catch (AirportException e) {
            AppLogger.warn("Booking failed: " + e.getMessage());
            throw e;
        } finally {
            AppLogger.debug("bookTicket finished for passenger #" + passengerId);
        }
    }

    /** Overload: book using the flight number instead of the id. */
    public Booking bookTicket(int passengerId, String flightNo, String travelClass, Payment payment) throws AirportException {
        Flight f = flightDao.findByNumber(flightNo)
                .orElseThrow(() -> new RecordNotFoundException("No flight with number " + flightNo + "."));
        return bookTicket(passengerId, f.getId(), null, travelClass, payment);
    }

    // ---------- cancel with refund ----------
    public double getRefundRate(Flight f) {
        LocalDateTime dep = f == null ? null : FormatUtil.parse(f.getDepartureTime());
        boolean early = dep == null || Duration.between(LocalDateTime.now(), dep).toHours() >= 24;
        return early ? REFUND_EARLY : REFUND_LATE;
    }

    /** What the passenger would get back if the booking were cancelled now. */
    public double previewRefund(Booking b) throws AirportException {
        double rate = getRefundRate(flightDao.getById(b.getFlightId()));
        return Math.round(b.getFare() * rate * 100.0) / 100.0;
    }

    /** Cancels the booking and returns the refund amount. */
    public double cancel(int bookingId) throws AirportException {
        Booking b = bookingDao.getById(bookingId);
        if (b == null) throw new RecordNotFoundException("Booking", bookingId);
        Flight f = flightDao.getById(b.getFlightId());
        if (f != null && hasDeparted(f)) throw new FlightDepartedException(f.getFlightNo(), f.getDepartureTime());
        double refund = previewRefund(b);
        bookingDao.cancel(bookingId, refund);
        AppLogger.info("Booking #" + bookingId + " cancelled (" + b.getPassengerName() + ", " + b.getFlightNo()
                + "), refund " + FormatUtil.money(refund));
        return refund;
    }

    // ---------- check-in (overloaded) ----------
    public String checkIn(int bookingId, double baggageKg) throws AirportException {
        guardCheckIn(bookingId);
        String gate = bookingDao.checkIn(bookingId, baggageKg);
        AppLogger.info("Check-in done for booking #" + bookingId + ", gate " + gate + ", baggage " + baggageKg + " kg");
        return gate;
    }

    public String checkIn(int bookingId, double baggageKg, String gate) throws AirportException {
        guardCheckIn(bookingId);
        String g = bookingDao.checkIn(bookingId, baggageKg, gate);
        AppLogger.info("Check-in done for booking #" + bookingId + ", gate " + g + ", baggage " + baggageKg + " kg");
        return g;
    }

    private void guardCheckIn(int bookingId) throws AirportException {
        Booking b = bookingDao.getById(bookingId);
        if (b == null) throw new RecordNotFoundException("Booking", bookingId);
        Flight f = flightDao.getById(b.getFlightId());
        if (f != null && hasDeparted(f))
            throw new InvalidBookingException("Check-in is closed: flight " + f.getFlightNo() + " has already departed.");
    }

    // ---------- queries ----------
    public List<Booking> getAll() throws AirportException { return bookingDao.getAll(); }
    public List<Booking> getByStatus(String status) throws AirportException { return bookingDao.getByStatus(status); }
    public List<Booking> getByPassenger(int passengerId) throws AirportException { return bookingDao.getByPassenger(passengerId); }
    public List<Booking> search(String keyword) throws AirportException { return bookingDao.search(keyword); }
    public int[] getStats() throws AirportException { return bookingDao.getStats(); }
}
