package airport.model;

/** Abstract Employee: parent of Pilot, CabinCrew and AirportStaff. */
public abstract class Employee extends Person {
    private double salary;

    public Employee() { }

    public Employee(int id, String name, int age, String phone, String email, double salary) {
        super(id, name, age, phone, email);
        this.salary = salary;
    }

    /** Type code stored in the database (PILOT / CABIN_CREW / AIRPORT_STAFF). */
    public abstract String getRoleCode();

    /** Role specific detail shown in the GUI. */
    public abstract String getDetails();

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }
}
