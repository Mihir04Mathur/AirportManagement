package airport.db;

import airport.exception.AirportException;
import airport.model.Passenger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** JDBC data access for the passenger table. */
public class PassengerDAO {

    private Passenger map(ResultSet rs) throws SQLException {
        return new Passenger(rs.getInt("passenger_id"), rs.getString("name"), rs.getInt("age"),
                rs.getString("gender"), rs.getString("phone"), rs.getString("email"),
                rs.getString("passport_no"), rs.getString("nationality"));
    }

    public void add(Passenger p) throws AirportException {
        String sql = "INSERT INTO passenger(name, age, gender, phone, email, passport_no, nationality) VALUES (?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, p);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) p.setId(k.getInt(1)); }
        } catch (SQLException e) { throw DBConnection.wrap(e); }
    }

    public void update(Passenger p) throws AirportException {
        String sql = "UPDATE passenger SET name=?, age=?, gender=?, phone=?, email=?, passport_no=?, nationality=? WHERE passenger_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, p);
            ps.setInt(8, p.getId());
            if (ps.executeUpdate() == 0) throw new AirportException("Passenger not found.");
        } catch (SQLException e) { throw DBConnection.wrap(e); }
    }

    public void delete(int id) throws AirportException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM passenger WHERE passenger_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw DBConnection.wrap(e); }
    }

    public List<Passenger> getAll() throws AirportException { return search(""); }

    /** Search by name / phone / passport. */
    public List<Passenger> search(String keyword) throws AirportException {
        List<Passenger> list = new ArrayList<>();
        String sql = "SELECT * FROM passenger WHERE name LIKE ? OR phone LIKE ? OR IFNULL(passport_no,'') LIKE ? ORDER BY passenger_id";
        String k = "%" + keyword.trim() + "%";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, k); ps.setString(2, k); ps.setString(3, k);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { throw DBConnection.wrap(e); }
        return list;
    }

    private void bind(PreparedStatement ps, Passenger p) throws SQLException {
        ps.setString(1, p.getName());
        ps.setInt(2, p.getAge());
        ps.setString(3, p.getGender());
        ps.setString(4, p.getPhone());
        ps.setString(5, emptyToNull(p.getEmail()));
        ps.setString(6, emptyToNull(p.getPassportNo()));
        ps.setString(7, p.getNationality());
    }

    static String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
}
