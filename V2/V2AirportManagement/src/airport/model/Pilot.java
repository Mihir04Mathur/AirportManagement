package airport.model;

public class Pilot extends Employee {
    private String licenseNo;
    private int flyingHours;

    public Pilot() { }

    public Pilot(int id, String name, int age, String phone, String email,
                 double salary, String licenseNo, int flyingHours) {
        super(id, name, age, phone, email, salary);
        this.licenseNo = licenseNo;
        this.flyingHours = flyingHours;
    }

    @Override public String getRole() { return "Pilot"; }
    @Override public String getRoleCode() { return "PILOT"; }
    @Override public String getDetails() { return "License " + licenseNo + " | " + flyingHours + " hrs"; }

    public String getLicenseNo() { return licenseNo; }
    public void setLicenseNo(String licenseNo) { this.licenseNo = licenseNo; }
    public int getFlyingHours() { return flyingHours; }
    public void setFlyingHours(int flyingHours) { this.flyingHours = flyingHours; }

    @Override public String toString() { return getId() + " - " + getName(); }
}
