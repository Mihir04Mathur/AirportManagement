package airport.model;

import airport.interfaces.Exportable;

/** INHERITANCE: Passenger extends Person. */
public class Passenger extends Person implements Exportable {
    private String gender;
    private String passportNo;
    private String nationality;

    public Passenger() { }

    public Passenger(int id, String name, int age, String gender, String phone,
                     String email, String passportNo, String nationality) {
        super(id, name, age, phone, email);
        this.gender = gender;
        this.passportNo = passportNo;
        this.nationality = nationality;
    }

    @Override
    public String getRole() { return "Passenger"; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPassportNo() { return passportNo; }
    public void setPassportNo(String passportNo) { this.passportNo = passportNo; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    @Override
    public String toString() { return getId() + " - " + getName(); }

    @Override
    public String[] csvHeader() {
        return new String[]{"ID", "Name", "Age", "Gender", "Phone", "Email", "Passport", "Nationality"};
    }

    @Override
    public String[] csvRow() {
        return new String[]{String.valueOf(getId()), getName(), String.valueOf(getAge()), gender, getPhone(),
                getEmail() == null ? "" : getEmail(), passportNo == null ? "" : passportNo, nationality};
    }
}
