package airport.db;

import airport.exception.AirportException;
import airport.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** JDBC data access for the employee table (Pilot / CabinCrew / AirportStaff). */
public class EmployeeDAO {

    /** Factory: builds the right subclass depending on the role column. */
    private Employee map(ResultSet rs) throws SQLException {
        int id = rs.getInt("employee_id");
        String name = rs.getString("name"), phone = rs.getString("phone"), email = rs.getString("email");
        int age = rs.getInt("age");
        double salary = rs.getDouble("salary");
        switch (rs.getString("role")) {
            case "PILOT":
                return new Pilot(id, name, age, phone, email, salary, rs.getString("license_no"), rs.getInt("flying_hours"));
            case "CABIN_CREW":
                return new CabinCrew(id, name, age, phone, email, salary, rs.getString("crew_rank"));
            default:
                return new AirportStaff(id, name, age, phone, email, salary, rs.getString("department"));
        }
    }

    public void add(Employee e) throws AirportException {
        String sql = "INSERT INTO employee(name, age, phone, email, salary, role, license_no, flying_hours, crew_rank, department) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, e);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) e.setId(k.getInt(1)); }
        } catch (SQLException ex) { throw DBConnection.wrap(ex); }
    }

    public void update(Employee e) throws AirportException {
        String sql = "UPDATE employee SET name=?, age=?, phone=?, email=?, salary=?, role=?, license_no=?, flying_hours=?, crew_rank=?, department=? WHERE employee_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, e);
            ps.setInt(11, e.getId());
            if (ps.executeUpdate() == 0) throw new AirportException("Employee not found.");
        } catch (SQLException ex) { throw DBConnection.wrap(ex); }
    }

    public void delete(int id) throws AirportException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM employee WHERE employee_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) { throw DBConnection.wrap(ex); }
    }

    public List<Employee> getAll() throws AirportException { return query("SELECT * FROM employee ORDER BY employee_id"); }

    public List<Employee> getPilots() throws AirportException {
        return query("SELECT * FROM employee WHERE role='PILOT' ORDER BY name");
    }

    private List<Employee> query(String sql) throws AirportException {
        List<Employee> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException ex) { throw DBConnection.wrap(ex); }
        return list;
    }

    private void bind(PreparedStatement ps, Employee e) throws SQLException {
        ps.setString(1, e.getName());
        ps.setInt(2, e.getAge());
        ps.setString(3, e.getPhone());
        ps.setString(4, PassengerDAO.emptyToNull(e.getEmail()));
        ps.setDouble(5, e.getSalary());
        ps.setString(6, e.getRoleCode());
        ps.setNull(7, Types.VARCHAR); ps.setNull(8, Types.INTEGER);
        ps.setNull(9, Types.VARCHAR); ps.setNull(10, Types.VARCHAR);
        if (e instanceof Pilot) {
            Pilot p = (Pilot) e;
            ps.setString(7, p.getLicenseNo());
            ps.setInt(8, p.getFlyingHours());
        } else if (e instanceof CabinCrew) {
            ps.setString(9, ((CabinCrew) e).getCrewRank());
        } else if (e instanceof AirportStaff) {
            ps.setString(10, ((AirportStaff) e).getDepartment());
        }
    }
}
