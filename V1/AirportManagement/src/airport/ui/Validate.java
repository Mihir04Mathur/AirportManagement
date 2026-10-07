package airport.ui;

import airport.exception.AirportException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Input validation - throws AirportException with a friendly message. */
public final class Validate {
    public static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Validate() { }

    public static String required(String v, String field) throws AirportException {
        if (v == null || v.isBlank()) throw new AirportException(field + " is required.");
        return v.trim();
    }

    public static int integer(String v, String field, int min, int max) throws AirportException {
        try {
            int n = Integer.parseInt(required(v, field));
            if (n < min || n > max) throw new AirportException(field + " must be between " + min + " and " + max + ".");
            return n;
        } catch (NumberFormatException e) {
            throw new AirportException(field + " must be a whole number.");
        }
    }

    public static double decimal(String v, String field, double min) throws AirportException {
        try {
            double d = Double.parseDouble(required(v, field));
            if (d < min) throw new AirportException(field + " must be at least " + min + ".");
            return d;
        } catch (NumberFormatException e) {
            throw new AirportException(field + " must be a number.");
        }
    }

    public static String phone(String v) throws AirportException {
        String p = required(v, "Phone").replaceAll("[\\s-]", "");
        if (!p.matches("\\+?\\d{10,13}")) throw new AirportException("Phone must be 10 to 13 digits.");
        return p;
    }

    /** Email is optional, but must look valid if entered. */
    public static String email(String v) throws AirportException {
        if (v == null || v.isBlank()) return "";
        if (!v.trim().matches("[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)+")) throw new AirportException("Email address is not valid.");
        return v.trim();
    }

    public static LocalDateTime dateTime(String v, String field) throws AirportException {
        try {
            return LocalDateTime.parse(required(v, field), DT);
        } catch (DateTimeParseException e) {
            throw new AirportException(field + " must look like 2026-12-25 14:30.");
        }
    }
}
