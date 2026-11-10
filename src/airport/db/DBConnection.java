package airport.db;

import airport.core.AirportContext;
import airport.exception.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

/** JDBC: creates connections and prepares the database (tables + sample data). */
public class DBConnection {
    private static final String URL = AirportContext.DB_URL;

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
            // unchecked: a setup problem, not something the user can fix inside the app
            throw new ConfigurationException("SQLite JDBC driver not found. Keep lib/sqlite-jdbc.jar on the classpath.", e);
        }
        try (Connection con = getConnection()) {
            runScript(con, AirportContext.SQL_DIR.resolve("schema.sql"));
            migrate(con);                                  // upgrades databases created by older versions
            if (isEmpty(con)) runScript(con, AirportContext.SQL_DIR.resolve("sample_data.sql"));
        } catch (SQLException e) {
            throw new DatabaseException("Database initialisation failed: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ConfigurationException("Cannot read the sql folder. Start the program from the project folder.", e);
        }
    }

    /** Adds columns that older databases do not have yet (uses the PRAGMA table_info metadata). */
    private static void migrate(Connection con) throws SQLException {
        ensureColumn(con, "booking", "refund_amount", "REAL NOT NULL DEFAULT 0");
    }

    private static void ensureColumn(Connection con, String table, String column, String definition) throws SQLException {
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) if (column.equalsIgnoreCase(rs.getString("name"))) return;
        }
        try (Statement st = con.createStatement()) {
            st.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
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

    /** Converts low level SQL errors into specific, friendly application exceptions. */
    public static AirportException wrap(SQLException e) {
        String m = String.valueOf(e.getMessage());
        if (m.contains("FOREIGN KEY"))
            return new RecordInUseException("This record is linked to other records (e.g. bookings) and cannot be deleted.", e);
        if (m.contains("UNIQUE"))
            return new DuplicateRecordException("A record with the same unique value (flight number, passport number or seat) already exists.", e);
        if (m.contains("CHECK"))
            return new DatabaseException("A value is out of the allowed range.", e);
        return new DatabaseException("Database error: " + m, e);
    }
}
