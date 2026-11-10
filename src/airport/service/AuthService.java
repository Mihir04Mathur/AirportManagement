package airport.service;

import airport.core.BaseService;
import airport.core.Result;
import airport.db.UserDAO;
import airport.exception.AirportException;
import airport.exception.AuthenticationException;
import airport.exception.ValidationException;
import airport.model.User;
import airport.util.PasswordUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Login / logout with a simple lock-out after repeated wrong passwords. */
public class AuthService extends BaseService {
    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_MILLIS = 30_000;
    private static final Map<String, Integer> failures = new HashMap<>();
    private static final Map<String, Long> lockedUntil = new HashMap<>();

    private final UserDAO dao = new UserDAO();

    /** Creates the two demo accounts the first time the program runs. */
    public void seedDefaultUsers() throws AirportException {
        if (dao.count() > 0) return;
        dao.add(new User(0, "admin", PasswordUtil.hash("admin123"), "Administrator", "ADMIN"));
        dao.add(new User(0, "staff", PasswordUtil.hash("staff123"), "Counter Staff", "STAFF"));
        logInfo("Default accounts created (admin, staff)");
    }

    public Result<User> loginResult(String username, String password) {
        try {
            User user = login(username, password);
            return success("Login successful", user);
        } catch (AirportException e) {
            logWarn(e.getMessage());
            return failure(e.getMessage(), e);
        }
    }

    public User login(String username, String password) throws AirportException {
        if (username == null || username.isBlank() || password == null || password.isEmpty())
            throw new ValidationException("Enter your username and password.");
        String key = username.trim().toLowerCase();

        Long until = lockedUntil.get(key);
        if (until != null) {
            long left = (until - System.currentTimeMillis() + 999) / 1000;
            if (left > 0) throw new AuthenticationException("Too many failed attempts. Try again in " + left + " seconds.");
            lockedUntil.remove(key);
            failures.remove(key);
        }

        Optional<User> user = dao.findByUsername(key);
        if (user.isEmpty() || !PasswordUtil.verify(password, user.get().getPasswordHash())) {
            int n = failures.merge(key, 1, Integer::sum);
            logWarn("Failed login for '" + key + "' (attempt " + n + ")");
            if (n >= MAX_ATTEMPTS) {
                lockedUntil.put(key, System.currentTimeMillis() + LOCK_MILLIS);
                throw new AuthenticationException("Too many failed attempts. Account locked for 30 seconds.");
            }
            throw new AuthenticationException("Invalid username or password.");
        }
        failures.remove(key);
        Session.login(user.get());
        logInfo("Login: " + user.get());
        return user.get();
    }

    public void logout() {
        if (Session.isLoggedIn()) logInfo("Logout: " + Session.getUser());
        Session.logout();
    }

    /** Test helper. */
    public static void resetLocks() { failures.clear(); lockedUntil.clear(); }
}
