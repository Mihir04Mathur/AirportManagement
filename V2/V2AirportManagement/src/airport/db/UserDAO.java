package airport.db;

import airport.exception.AirportException;
import airport.model.User;

import java.sql.*;
import java.util.Optional;

/** JDBC access for login accounts. */
public class UserDAO {
    public int count() throws AirportException {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM user_account")) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) { throw DBConnection.wrap(e); }
    }

    public void add(User u) throws AirportException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO user_account(username, password_hash, full_name, role) VALUES (?,?,?,?)")) {
            ps.setString(1, u.getUsername().toLowerCase());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getFullName());
            ps.setString(4, u.getRole());
            ps.executeUpdate();
        } catch (SQLException e) { throw DBConnection.wrap(e); }
    }

    public Optional<User> findByUsername(String username) throws AirportException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM user_account WHERE username = ?")) {
            ps.setString(1, username.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("password_hash"),
                                               rs.getString("full_name"), rs.getString("role")))
                        : Optional.empty();
            }
        } catch (SQLException e) { throw DBConnection.wrap(e); }
    }
}
