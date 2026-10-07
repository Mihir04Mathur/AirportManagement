package airport.model;

/** A login account. Role ADMIN can open every screen; STAFF cannot open employees, reports or the log. */
public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String role;

    public User(int id, String username, String passwordHash, String fullName, String role) {
        this.id = id; this.username = username; this.passwordHash = passwordHash;
        this.fullName = fullName; this.role = role;
    }

    public boolean isAdmin() { return "ADMIN".equals(role); }
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    @Override public String toString() { return fullName + " (" + role + ")"; }
}
