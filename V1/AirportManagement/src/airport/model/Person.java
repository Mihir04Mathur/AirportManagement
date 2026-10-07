package airport.model;

/** ABSTRACTION + ENCAPSULATION: base class for Passenger and Employee. */
public abstract class Person {
    private int id;
    private String name;
    private int age;
    private String phone;
    private String email;

    public Person() { }

    public Person(int id, String name, int age, String phone, String email) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.phone = phone;
        this.email = email;
    }

    /** Abstract method - every subclass describes itself differently. */
    public abstract String getRole();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() { return name + " (" + getRole() + ")"; }
}
