package airport.service;

import airport.model.User;

import java.util.Set;

/** Remembers who is logged in and which screens that user may open. */
public final class Session {
    private static final Set<String> ADMIN_ONLY = Set.of("employees", "reports", "log");
    private static User current;

    private Session() { }

    public static void login(User u) { current = u; }
    public static void logout() { current = null; }
    public static User getUser() { return current; }
    public static boolean isLoggedIn() { return current != null; }

    public static boolean canAccess(String screen) {
        if (current == null) return false;
        return current.isAdmin() || !ADMIN_ONLY.contains(screen);
    }
}
