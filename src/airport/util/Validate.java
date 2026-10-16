package airport.util;

import airport.exception.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Input validation (regex, parsing) - throws ValidationException with a friendly message. */
public final class Validate {
    public static final DateTimeFormatter DT = FormatUtil.DATE_TIME;

    private Validate() { }

    public static String required(String v, String field) throws ValidationException {
        if (v == null || v.isBlank()) throw new ValidationException(field + " is required.");
        return v.trim();
    }

    public static int integer(String v, String field, int min, int max) throws ValidationException {
        int n;
        try {
            n = Integer.parseInt(required(v, field));
        } catch (NumberFormatException e) {
            throw new ValidationException(field + " must be a whole number.");
        }
        if (n < min || n > max) throw new ValidationException(field + " must be between " + min + " and " + max + ".");
        return n;
    }

    public static double decimal(String v, String field, double min) throws ValidationException {
        double d;
        try {
            d = Double.parseDouble(required(v, field));
        } catch (NumberFormatException e) {
            throw new ValidationException(field + " must be a number.");
        }
        if (d < min) throw new ValidationException(field + " must be at least " + min + ".");
        return d;
    }

    public static String phone(String v) throws ValidationException {
        String p = required(v, "Phone").replaceAll("[\\s-]", "");
        if (!p.matches("\\+?\\d{10,13}")) throw new ValidationException("Phone must be 10 to 13 digits.");
        return p;
    }

    /** Email is optional, but must look valid if entered. */
    public static String email(String v) throws ValidationException {
        if (v == null || v.isBlank()) return "";
        if (!v.trim().matches("[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)+")) throw new ValidationException("Email address is not valid.");
        return v.trim();
    }

    public static LocalDateTime dateTime(String v, String field) throws ValidationException {
        try {
            return LocalDateTime.parse(required(v, field), DT);
        } catch (DateTimeParseException e) {
            throw new ValidationException(field + " must look like 2026-12-25 14:30.");
        }
    }
}
