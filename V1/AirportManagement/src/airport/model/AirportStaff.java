package airport.model;

public class AirportStaff extends Employee {
    private String department;

    public AirportStaff() { }

    public AirportStaff(int id, String name, int age, String phone, String email,
                        double salary, String department) {
        super(id, name, age, phone, email, salary);
        this.department = department;
    }

    @Override public String getRole() { return "Airport Staff"; }
    @Override public String getRoleCode() { return "AIRPORT_STAFF"; }
    @Override public String getDetails() { return "Dept: " + department; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
