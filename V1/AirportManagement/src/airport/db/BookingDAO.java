package airport.db;

import airport.exception.*;
import airport.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** JDBC data access for bookings, payments and check-in (uses transactions). */
public class BookingDAO {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String SELECT =
            "SELECT b.*, p.name AS pname, f.flight_no, f.origin || ' → ' || f.destination AS route, pay.method " +
            "FROM booking b JOIN passenger p ON p.passenger_id=b.passenger_id " +
            "JOIN flight f ON f.flight_id=b.flight_id LEFT JOIN payment pay ON pay.booking_id=b.booking_id ";

    private Booking map(ResultSet rs) throws SQLException {
        Booking b = new Booking(rs.getInt("passenger_id"), rs.getInt("flight_id"), rs.getString("travel_class"), rs.getDouble("fare"));
        b.setId(rs.getInt("booking_id"));
        b.setSeatNo(rs.getString("seat_no"));
        b.setBookingTime(rs.getString("booking_time"));
        b.setStatus(rs.getString("status"));
        b.setPassengerName(rs.getString("pname"));
        b.setFlightNo(rs.getString("flight_no"));
        b.setRoute(rs.getString("route"));
        b.setPaymentMethod(rs.getString("method"));
        return b;
    }

    // ---------- METHOD OVERLOADING: two ways to book ----------

    /** Book using the flight id. */
    public Booking bookTicket(int passengerId, int flightId, String travelClass, Payment payment) throws AirportException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                Booking b = doBook(con, passengerId, flightId, travelClass, payment);
                con.commit();
                return b;
            } catch (AirportException | SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) { throw DBConnection.wrap(e); }
    }

    /** Book using the flight number (e.g. "AI101"). */
    public Booking bookTicket(int passengerId, String flightNo, String travelClass, Payment payment) throws AirportException {
        int flightId;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT flight_id FROM flight WHERE flight_no=?")) {
            ps.setString(1, flightNo.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new InvalidBookingException("No flight with number " + flightNo + ".");
                flightId = rs.getInt(1);
            }
        } catch (SQLException e) { throw DBConnection.wrap(e); }
        return bookTicket(passengerId, flightId, travelClass, payment);   // connection is closed before booking
    }

    private Booking doBook(Connection con, int passengerId, int flightId, String travelClass, Payment payment)
            throws SQLException, AirportException {
        Flight flight = FlightDAO.getById(con, flightId);
        if (flight == null) throw new InvalidBookingException("Selected flight does not exist.");
        if (!exists(con, "SELECT 1 FROM passenger WHERE passenger_id=?", passengerId))
            throw new InvalidBookingException("Selected passenger does not exist.");
        if (!flight.isAvailable()) throw new SeatUnavailableException(flight.getFlightNo());
        if (exists(con, "SELECT 1 FROM booking WHERE passenger_id=? AND flight_id=? AND status<>'CANCELLED'", passengerId, flightId))
            throw new InvalidBookingException("This passenger already has an active booking on " + flight.getFlightNo() + ".");

        double fare = flight.calculateFare(travelClass);
        if (Math.abs(fare - payment.getAmount()) > 0.01)
            throw new InvalidBookingException("Payment amount does not match the ticket fare.");
        payment.pay();   // validates card / UPI / cash (polymorphic call)

        String seat = nextFreeSeat(con, flight);
        String now = LocalDateTime.now().format(FMT);
        int bookingId;
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO booking(passenger_id, flight_id, seat_no, travel_class, fare, booking_time, status) VALUES (?,?,?,?,?,?, 'CONFIRMED')",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, passengerId); ps.setInt(2, flightId); ps.setString(3, seat);
            ps.setString(4, travelClass.toUpperCase()); ps.setDouble(5, fare); ps.setString(6, now);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { k.next(); bookingId = k.getInt(1); }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO payment(booking_id, method, amount, paid_at, card_holder, card_last4, upi_id, cash_tendered) VALUES (?,?,?,?,?,?,?,?)")) {
            ps.setInt(1, bookingId); ps.setString(2, payment.getMethod());
            ps.setDouble(3, payment.getTotalCharge()); ps.setString(4, now);
            ps.setNull(5, Types.VARCHAR); ps.setNull(6, Types.VARCHAR); ps.setNull(7, Types.VARCHAR); ps.setNull(8, Types.REAL);
            if (payment instanceof CardPayment) {
                ps.setString(5, ((CardPayment) payment).getCardHolder());
                ps.setString(6, ((CardPayment) payment).getLast4());
            } else if (payment instanceof UPIPayment) {
                ps.setString(7, ((UPIPayment) payment).getUpiId());
            } else if (payment instanceof CashPayment) {
                ps.setDouble(8, ((CashPayment) payment).getTendered());
            }
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE flight SET available_seats = available_seats - 1 WHERE flight_id=? AND available_seats > 0")) {
            ps.setInt(1, flightId);
            if (ps.executeUpdate() != 1) throw new SeatUnavailableException(flight.getFlightNo());
        }
        Booking b = new Booking(passengerId, flightId, travelClass.toUpperCase(), fare);
        b.setId(bookingId); b.setSeatNo(seat); b.setBookingTime(now); b.setStatus("CONFIRMED");
        b.setPassengerName(nameOf(con, passengerId));
        b.setFlightNo(flight.getFlightNo()); b.setRoute(flight.getOrigin() + " → " + flight.getDestination());
        b.setPaymentMethod(payment.getMethod());
        return b;
    }

    /** Seats are named 1A..1F, 2A..2F ... ; picks the first one not taken. */
    private String nextFreeSeat(Connection con, Flight flight) throws SQLException, AirportException {
        Set<String> taken = new HashSet<>();
        try (PreparedStatement ps = con.prepareStatement("SELECT seat_no FROM booking WHERE flight_id=? AND status<>'CANCELLED'")) {
            ps.setInt(1, flight.getId());
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) taken.add(rs.getString(1)); }
        }
        String letters = "ABCDEF";
        for (int n = 0; n < flight.getTotalSeats(); n++) {
            String seat = (n / 6 + 1) + "" + letters.charAt(n % 6);
            if (!taken.contains(seat)) return seat;
        }
        throw new SeatUnavailableException(flight.getFlightNo());
    }

    private String nameOf(Connection con, int passengerId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT name FROM passenger WHERE passenger_id=?")) {
            ps.setInt(1, passengerId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getString(1) : ""; }
        }
    }

    private boolean exists(Connection con, String sql, int... args) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) ps.setInt(i + 1, args[i]);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    // ---------- Cancel ----------
    public void cancel(int bookingId) throws AirportException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                Booking b = getById(con, bookingId);
                if (b == null) throw new InvalidBookingException("Booking #" + bookingId + " not found.");
                if ("CANCELLED".equals(b.getStatus())) throw new InvalidBookingException("Booking is already cancelled.");
                if ("CHECKED_IN".equals(b.getStatus())) throw new InvalidBookingException("A checked-in booking cannot be cancelled.");
                try (PreparedStatement ps = con.prepareStatement("UPDATE booking SET status='CANCELLED' WHERE booking_id=?")) {
                    ps.setInt(1, bookingId); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE flight SET available_seats = available_seats + 1 WHERE flight_id=?")) {
                    ps.setInt(1, b.getFlightId()); ps.executeUpdate();
                }
                con.commit();
            } catch (AirportException | SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) { throw DBConnection.wrap(e); }
    }

    // ---------- METHOD OVERLOADING: two ways to check in ----------

    /** Check-in with an automatically allotted gate. */
    public String checkIn(int bookingId, double baggageKg) throws AirportException {
        return checkIn(bookingId, baggageKg, "G" + (bookingId % 12 + 1));
    }

    /** Check-in with a chosen gate. Returns the gate. */
    public String checkIn(int bookingId, double baggageKg, String gate) throws AirportException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                Booking b = getById(con, bookingId);
                if (b == null) throw new InvalidBookingException("Booking #" + bookingId + " not found.");
                if ("CANCELLED".equals(b.getStatus())) throw new InvalidBookingException("A cancelled booking cannot be checked in.");
                if ("CHECKED_IN".equals(b.getStatus())) throw new InvalidBookingException("Passenger is already checked in.");
                double limit = "BUSINESS".equals(b.getTravelClass()) ? 40 : 25;
                if (baggageKg < 0) throw new InvalidBookingException("Baggage weight cannot be negative.");
                if (baggageKg > limit)
                    throw new InvalidBookingException(String.format("Baggage limit for %s class is %.0f kg.", b.getTravelClass().toLowerCase(), limit));
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO checkin(booking_id, checkin_time, baggage_kg, gate) VALUES (?,?,?,?)")) {
                    ps.setInt(1, bookingId); ps.setString(2, LocalDateTime.now().format(FMT));
                    ps.setDouble(3, baggageKg); ps.setString(4, gate);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE booking SET status='CHECKED_IN' WHERE booking_id=?")) {
                    ps.setInt(1, bookingId); ps.executeUpdate();
                }
                con.commit();
                return gate;
            } catch (AirportException | SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) { throw DBConnection.wrap(e); }
    }

    // ---------- Queries ----------
    private Booking getById(Connection con, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT + "WHERE b.booking_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public Booking getById(int id) throws AirportException {
        try (Connection con = DBConnection.getConnection()) { return getById(con, id); }
        catch (SQLException e) { throw DBConnection.wrap(e); }
    }

    public List<Booking> getAll() throws AirportException {
        return query(SELECT + "ORDER BY b.booking_id DESC");
    }

    public List<Booking> getByStatus(String status) throws AirportException {
        return query(SELECT + "WHERE b.status=? ORDER BY b.booking_id DESC", status);
    }

    public List<Booking> search(String keyword) throws AirportException {
        String k = "%" + keyword.trim() + "%";
        return query(SELECT + "WHERE p.name LIKE ? OR f.flight_no LIKE ? OR CAST(b.booking_id AS TEXT) LIKE ? ORDER BY b.booking_id DESC", k, k, k);
    }

    private List<Booking> query(String sql, String... params) throws AirportException {
        List<Booking> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setString(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { throw DBConnection.wrap(e); }
        return list;
    }

    /** Small dashboard numbers for the home screen. */
    public int[] getStats() throws AirportException {
        String sql = "SELECT (SELECT COUNT(*) FROM flight), (SELECT COUNT(*) FROM passenger), " +
                "(SELECT COUNT(*) FROM booking WHERE status<>'CANCELLED'), (SELECT COUNT(*) FROM employee)";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return new int[]{rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4)};
        } catch (SQLException e) { throw DBConnection.wrap(e); }
    }
}
