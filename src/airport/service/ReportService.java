package airport.service;

import airport.db.BookingDAO;
import airport.db.EmployeeDAO;
import airport.db.FlightDAO;
import airport.db.PassengerDAO;
import airport.exception.AirportException;
import airport.model.Booking;
import airport.model.Employee;
import airport.model.Flight;
import airport.model.Passenger;
import airport.util.AppLogger;
import airport.util.CsvExporter;
import airport.util.FormatUtil;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Statistics and exports. Shows the Java Collections framework, Streams, Comparator, Optional and java.time. */
public class ReportService {
    public static final String[] SORT_OPTIONS = {
            "Departure time", "Flight number", "Occupancy (high to low)", "Revenue (high to low)", "Base fare (low to high)"};

    private final FlightDAO flightDao = new FlightDAO();
    private final BookingDAO bookingDao = new BookingDAO();
    private final PassengerDAO passengerDao = new PassengerDAO();
    private final EmployeeDAO employeeDao = new EmployeeDAO();

    /** One row of the flight report. */
    public static class FlightStat {
        public final String flightNo, route, departure, duration, type;
        public final int booked, total;
        public final double occupancy, revenue, baseFare;

        FlightStat(String flightNo, String route, String departure, String duration, String type,
                   int booked, int total, double occupancy, double revenue, double baseFare) {
            this.flightNo = flightNo; this.route = route; this.departure = departure; this.duration = duration;
            this.type = type; this.booked = booked; this.total = total;
            this.occupancy = occupancy; this.revenue = revenue; this.baseFare = baseFare;
        }
    }

    private List<Booking> activeBookings() throws AirportException {
        return bookingDao.getAll().stream().filter(Booking::isActive).collect(Collectors.toList());
    }

    /** Flight report, sorted with a Comparator chosen from SORT_OPTIONS. */
    public List<FlightStat> flightStats(int sortIndex) throws AirportException {
        Map<Integer, Double> revenue = activeBookings().stream()
                .collect(Collectors.groupingBy(Booking::getFlightId, Collectors.summingDouble(Booking::getFare)));
        List<FlightStat> list = new ArrayList<>();
        for (Flight f : flightDao.getAll()) {
            list.add(new FlightStat(f.getFlightNo(), f.getOrigin() + " → " + f.getDestination(), f.getDepartureTime(),
                    FormatUtil.duration(f.getDepartureTime(), f.getArrivalTime()), FormatUtil.pretty(f.getType()),
                    f.getTotalSeats() - f.getAvailableSeats(), f.getTotalSeats(), f.getOccupancyPercent(),
                    revenue.getOrDefault(f.getId(), 0.0), f.getBaseFare()));
        }
        Comparator<FlightStat> cmp;
        switch (sortIndex) {
            case 1:  cmp = Comparator.comparing((FlightStat s) -> s.flightNo); break;
            case 2:  cmp = Comparator.comparingDouble((FlightStat s) -> s.occupancy).reversed(); break;
            case 3:  cmp = Comparator.comparingDouble((FlightStat s) -> s.revenue).reversed(); break;
            case 4:  cmp = Comparator.comparingDouble((FlightStat s) -> s.baseFare); break;
            default: cmp = Comparator.comparing((FlightStat s) -> s.departure);
        }
        list.sort(cmp);
        return list;
    }

    /** Headline numbers as label -> formatted value (LinkedHashMap keeps the order). */
    public Map<String, String> kpis() throws AirportException {
        List<Booking> all = bookingDao.getAll();
        List<Booking> active = all.stream().filter(Booking::isActive).collect(Collectors.toList());
        List<Flight> flights = flightDao.getAll();

        double revenue = active.stream().mapToDouble(Booking::getFare).sum();
        double avgFare = active.stream().mapToDouble(Booking::getFare).average().orElse(0);
        double avgOcc = flights.stream().mapToDouble(Flight::getOccupancyPercent).average().orElse(0);
        double payroll = employeeDao.getAll().stream().mapToDouble(Employee::getSalary).sum();
        long cancelled = all.size() - active.size();
        double refunds = all.stream().mapToDouble(Booking::getRefundAmount).sum();

        Optional<String> popular = active.stream()
                .collect(Collectors.groupingBy(Booking::getFlightNo, Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue())
                .map(e -> e.getKey() + " (" + e.getValue() + ")");

        Map<String, String> map = new LinkedHashMap<>();
        map.put("Total revenue", FormatUtil.money(revenue));
        map.put("Average ticket fare", FormatUtil.money(avgFare));
        map.put("Active bookings", String.valueOf(active.size()));
        map.put("Cancelled bookings", String.valueOf(cancelled));
        map.put("Refunds issued", FormatUtil.money(refunds));
        map.put("Average occupancy", FormatUtil.percent(avgOcc));
        map.put("Most booked flight", popular.orElse("-"));
        map.put("Monthly payroll", FormatUtil.money(payroll));
        map.put("Flights scheduled", String.valueOf(flights.size()));
        return map;
    }

    /** Rows of {category, item, count} - grouped with Collectors.groupingBy into sorted TreeMaps. */
    public List<String[]> breakdown() throws AirportException {
        List<Booking> all = bookingDao.getAll();
        List<Booking> active = all.stream().filter(Booking::isActive).collect(Collectors.toList());
        List<String[]> rows = new ArrayList<>();
        addGroup(rows, "Booking status", count(all, Booking::getStatus));
        addGroup(rows, "Payment method", count(active.stream().filter(b -> b.getPaymentMethod() != null).collect(Collectors.toList()), Booking::getPaymentMethod));
        addGroup(rows, "Travel class", count(active, Booking::getTravelClass));
        addGroup(rows, "Flight type", count(flightDao.getAll(), f -> FormatUtil.pretty(f.getType())));
        addGroup(rows, "Employee role", count(employeeDao.getAll(), Employee::getRole));
        addGroup(rows, "Passenger gender", count(passengerDao.getAll(), Passenger::getGender));
        addGroup(rows, "Passenger nationality", count(passengerDao.getAll(), Passenger::getNationality));
        return rows;
    }

    private static <T> Map<String, Long> count(List<T> list, Function<T, String> key) {
        return list.stream().collect(Collectors.groupingBy(key, TreeMap::new, Collectors.counting()));
    }

    private static void addGroup(List<String[]> rows, String category, Map<String, Long> counts) {
        counts.forEach((item, n) -> rows.add(new String[]{category, item, String.valueOf(n)}));
    }

    // ---------- exports ----------
    public Path exportPassengers(Path file) throws AirportException { return done("passengers", CsvExporter.export(passengerDao.getAll(), file)); }
    public Path exportEmployees(Path file) throws AirportException { return done("employees", CsvExporter.export(employeeDao.getAll(), file)); }
    public Path exportFlights(Path file) throws AirportException { return done("flights", CsvExporter.export(flightDao.getAll(), file)); }
    public Path exportBookings(Path file) throws AirportException { return done("bookings", CsvExporter.export(bookingDao.getAll(), file)); }
    public Path exportTextReport(Path file) throws AirportException { return done("text report", CsvExporter.writeText(buildTextReport(), file)); }

    private Path done(String what, Path file) {
        AppLogger.info("Exported " + what + " to " + file.toAbsolutePath());
        return file;
    }

    /** Builds the full report with StringBuilder and String.format. */
    public String buildTextReport() throws AirportException {
        StringBuilder sb = new StringBuilder();
        String line = "=".repeat(72);
        sb.append(line).append("\n AIRPORT MANAGEMENT SYSTEM - SUMMARY REPORT\n")
          .append(" Generated: ").append(LocalDateTime.now().format(FormatUtil.DATE_TIME)).append('\n')
          .append(line).append("\n\nKEY NUMBERS\n");
        kpis().forEach((k, v) -> sb.append(String.format("  %-24s %s%n", k, v)));

        sb.append("\nFLIGHTS\n").append(String.format("  %-8s %-26s %-17s %-9s %-7s %s%n",
                "Flight", "Route", "Departure", "Duration", "Booked", "Revenue"));
        for (FlightStat s : flightStats(0))
            sb.append(String.format("  %-8s %-26s %-17s %-9s %3d/%-3d %s%n",
                    s.flightNo, s.route, s.departure, s.duration, s.booked, s.total, FormatUtil.money(s.revenue)));

        sb.append("\nBREAKDOWNS\n");
        String last = "";
        for (String[] r : breakdown()) {
            if (!r[0].equals(last)) { sb.append("  ").append(r[0]).append(":\n"); last = r[0]; }
            sb.append(String.format("    %-24s %s%n", r[1], r[2]));
        }
        return sb.append('\n').append(line).append('\n').toString();
    }
}
