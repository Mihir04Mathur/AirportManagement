package airport.util;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/** Java library helpers: java.text.NumberFormat, java.time (LocalDateTime, Duration) and String utilities. */
public final class FormatUtil {
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Locale INDIA = Locale.forLanguageTag("en-IN");

    private FormatUtil() { }

    /**
     * 1234567.5 -> ₹12,34,567.50  (Indian lakh/crore grouping).
     * NumberFormat only supports one grouping size, so the integer part is grouped by hand with a StringBuilder.
     */
    public static String money(double amount) {
        String plain = String.format(Locale.US, "%.2f", Math.abs(amount));          // "1234567.50"
        String whole = plain.substring(0, plain.indexOf('.'));
        String fraction = plain.substring(plain.indexOf('.'));
        StringBuilder sb = new StringBuilder();
        int n = whole.length();
        for (int i = 0; i < n; i++) {
            int remaining = n - i;                                                  // digits still to write
            if (i > 0 && (remaining == 3 || (remaining > 3 && (remaining - 3) % 2 == 0))) sb.append(',');
            sb.append(whole.charAt(i));
        }
        return (amount < 0 ? "-" : "") + "₹" + sb + fraction;
    }

    /** Plain number with 2 decimals using java.text.NumberFormat (used where no currency sign is wanted). */
    public static String number(double value) {
        NumberFormat nf = NumberFormat.getNumberInstance(INDIA);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(value);
    }

    public static String percent(double value) { return String.format("%.1f%%", value); }

    /** Parses "2026-11-05 06:30"; returns null if the text is not a valid date-time. */
    public static LocalDateTime parse(String text) {
        try {
            return LocalDateTime.parse(text, DATE_TIME);
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }

    /** Flight duration such as "2h 45m". */
    public static String duration(String departure, String arrival) {
        LocalDateTime d = parse(departure), a = parse(arrival);
        if (d == null || a == null) return "-";
        Duration dur = Duration.between(d, a);
        return dur.toHours() + "h " + String.format("%02d", dur.toMinutesPart()) + "m";
    }

    /** CABIN_CREW -> Cabin Crew (uses String, StringBuilder). */
    public static String pretty(String code) {
        if (code == null || code.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String word : code.toLowerCase().split("_")) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }
}
