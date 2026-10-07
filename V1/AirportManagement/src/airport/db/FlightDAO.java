package airport.db;

import airport.exception.AirportException;
import airport.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** JDBC data access for the flight table. */
public class FlightDAO {

    /** Factory: builds DomesticFlight or InternationalFlight (polymorphism). */
    static Flight map(ResultSet rs) throws SQLException {
        int pid = rs.getInt("pilot_id");
        Integer pilotId = rs.wasNull() ? null : pid;
        if ("DOMESTIC".equals(rs.getString("flight_type"))) {
            return new DomesticFlight(rs.getInt("flight_id"), rs.getString("flight_no"), rs.getString("airline"),
                    rs.getString("origin"), rs.getString("destination"), rs.getString("departure_time"),
                    rs.getString("arrival_time"), rs.getInt("total_seats"), rs.getInt("available_seats"),
                    rs.getDouble("base_fare"), pilotId, rs.getString("terminal"));
        }
        return new InternationalFlight(rs.getInt("flight_id"), rs.getString("flight_no"), rs.getString("airline"),
                rs.getString("origin"), rs.getString("destination"), rs.getString("departure_time"),
                rs.getString("arrival_time"), rs.getInt("total_seats"), rs.getInt("available_seats"),
                rs.getDouble("base_fare"), pilotId, rs.getInt("visa_required") == 1);
    }

    public void add(Flight f) throws AirportException {
        String sql = "INSERT INTO flight(flight_no, airline, origin, destination, departure_time, arrival_time, total_seats, available_seats, base_fare, flight_type, terminal, visa_required, pilot_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, f.getFlightNo());
            ps.setString(2, f.getAirline());
            ps.setString(3, f.getOrigin());
            ps.setString(4, f.getDestination());
            ps.setString(5, f.getDepartureTime());
            ps.setString(6, f.getArrivalTime());
            ps.setInt(7, f.getTotalSeats());
            ps.setInt(8, f.getTotalSeats());
            ps.setDouble(9, f.getBaseFare());
            bindType(ps, f, 10);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) f.setId(k.getInt(1)); }
        } catch (SQLException e) { throw DBConnection.wrap(e); }
    }

    public void update(Flight f) throws AirportException {
        // available = newTotal - (already booked seats); booked = old total - old available
        String sql = "UPDATE flight SET flight_no=?, airline=?, origin=?, destination=?, departure_time=?, arrival_time=?, " +
                "available_seats = ? - (total_seats - available_seats), total_seats=?, base_fare=?, flight_type=?, terminal=?, visa_required=?, pilot_id=? WHERE flight_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, f.getFlightNo());
            ps.setString(2, f.getAirline());
            ps.setString(3, f.getOrigin());
            ps.setString(4, f.getDestination());
            ps.setString(5, f.getDepartureTime());
            ps.setString(6, f.getArrivalTime());
            ps.setInt(7, f.getTotalSeats());
            ps.setInt(8, f.getTotalSeats());
            ps.setDouble(9, f.getBaseFare());
            bindType(ps, f, 10);
            ps.setInt(14, f.getId());
            if (ps.executeUpdate() == 0) throw new AirportException("Flight not found.");
        } catch (SQLException e) {
            if (String.valueOf(e.getMessage()).contains("CHECK"))
                throw new AirportException("Total seats cannot be lower than the seats already booked.", e);
            throw DBConnection.wrap(e);
        }
    }

    /** Binds flight_type, terminal, visa_required, pilot_id starting at index i. */
    private void bindType(PreparedStatement ps, Flight f, int i) throws SQLException {
        ps.setString(i, f.getType());
        if (f instanceof DomesticFlight) {
            ps.setString(i + 1, ((DomesticFlight) f).getTerminal());
            ps.setInt(i + 2, 0);
        } else {
            ps.setNull(i + 1, Types.VARCHAR);
            ps.setInt(i + 2, ((InternationalFlight) f).isVisaRequired() ? 1 : 0);
        }
        if (f.getPilotId() == null) ps.setNull(i + 3, Types.INTEGER); else ps.setInt(i + 3, f.getPilotId());
    }

    public void delete(int id) throws AirportException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM flight WHERE flight_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw DBConnection.wrap(e); }
    }

    public List<Flight> getAll() throws AirportException {
        return run("SELECT * FROM flight ORDER BY departure_time");
    }

    public Flight getById(int id) throws AirportException {
        try (Connection con = DBConnection.getConnection()) {
            return getById(con, id);
        } catch (SQLException e) { throw DBConnection.wrap(e); }
    }

    static Flight getById(Connection con, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM flight WHERE flight_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    // ---------- METHOD OVERLOADING: three ways to search flights ----------

    /** Search by a keyword (flight no / airline / city). */
    public List<Flight> search(String keyword) throws AirportException {
        String k = "%" + keyword.trim() + "%";
        return run("SELECT * FROM flight WHERE flight_no LIKE ? OR airline LIKE ? OR origin LIKE ? OR destination LIKE ? ORDER BY departure_time", k, k, k, k);
    }

    /** Search by route. */
    public List<Flight> search(String origin, String destination) throws AirportException {
        return run("SELECT * FROM flight WHERE origin LIKE ? AND destination LIKE ? ORDER BY departure_time",
                "%" + origin.trim() + "%", "%" + destination.trim() + "%");
    }

    /** Search by route and date (yyyy-MM-dd). */
    public List<Flight> search(String origin, String destination, String date) throws AirportException {
        return run("SELECT * FROM flight WHERE origin LIKE ? AND destination LIKE ? AND departure_time LIKE ? ORDER BY departure_time",
                "%" + origin.trim() + "%", "%" + destination.trim() + "%", date.trim() + "%");
    }

    private List<Flight> run(String sql, String... params) throws AirportException {
        List<Flight> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setString(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { throw DBConnection.wrap(e); }
        return list;
    }
}
