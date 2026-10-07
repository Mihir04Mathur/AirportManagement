package airport.db;

import airport.exception.AirportException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

/** JDBC: creates connections and prepares the database (tables + sample data). */
public class DBConnection {
    private static final String URL = "jdbc:sqlite:airport.db";

    private DBConnection() { }

    public static Connection getConnection() throws SQLException {
        Connection con = DriverManager.getConnection(URL);
        try (Statement st = con.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");   // enforce foreign keys
        }
        return con;
    }

    /** Runs sql/schema.sql and, if the DB is empty, sql/sample_data.sql. */
    public static void initialize() throws AirportException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new AirportException("SQLite JDBC driver not found. Keep lib/sqlite-jdbc.jar on the classpath.", e);
        }
        try (Connection con = getConnection()) {
            runScript(con, Path.of("sql", "schema.sql"));
            if (isEmpty(con)) runScript(con, Path.of("sql", "sample_data.sql"));
        } catch (SQLException | IOException e) {
            throw new AirportException("Database initialisation failed: " + e.getMessage(), e);
        }
    }

    private static boolean isEmpty(Connection con) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT (SELECT COUNT(*) FROM flight) + (SELECT COUNT(*) FROM passenger) + (SELECT COUNT(*) FROM employee)")) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    private static void runScript(Connection con, Path file) throws IOException, SQLException {
        StringBuilder sb = new StringBuilder();
        for (String line : Files.readAllLines(file)) {
            int c = line.indexOf("--");
            sb.append(c >= 0 ? line.substring(0, c) : line).append('\n');
        }
        try (Statement st = con.createStatement()) {
            for (String sql : sb.toString().split(";")) {
                if (!sql.isBlank()) st.execute(sql.trim());
            }
        }
    }

    /** Converts low level SQL errors into friendly application errors. */
    public static AirportException wrap(SQLException e) {
        String m = String.valueOf(e.getMessage());
        if (m.contains("FOREIGN KEY")) return new AirportException("This record is linked to other records (e.g. bookings) and cannot be deleted.", e);
        if (m.contains("UNIQUE")) return new AirportException("A record with the same unique value (flight number / passport) already exists.", e);
        if (m.contains("CHECK")) return new AirportException("A value is out of the allowed range.", e);
        return new AirportException("Database error: " + m, e);
    }
}
