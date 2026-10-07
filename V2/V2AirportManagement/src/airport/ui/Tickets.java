package airport.ui;

import airport.db.FlightDAO;
import airport.model.Booking;
import airport.model.Flight;

import java.awt.*;

/** Entry point for showing an e-ticket / boarding pass, plus the plain-text version used for "Save as file". */
public final class Tickets {
    private Tickets() { }

    public static void show(Component parent, Booking b, String gate, String extra) {
        Flight f = null;
        try { f = new FlightDAO().getById(b.getFlightId()); } catch (Exception ignored) { /* ticket still shows without times */ }
        TicketDialog.show(parent, b, f, gate, extra);
    }

    static String buildText(Booking b, Flight f, String gate, String extra) {
        String dep = f == null ? "-" : f.getDepartureTime(), arr = f == null ? "-" : f.getArrivalTime();
        StringBuilder sb = new StringBuilder();
        sb.append(gate == null ? "        E-TICKET\n" : "        BOARDING PASS\n");
        sb.append("  --------------------------------\n");
        sb.append(String.format("  Booking ID  : #%d%n", b.getId()));
        sb.append(String.format("  Passenger   : %s%n", b.getPassengerName()));
        sb.append(String.format("  Flight      : %s%n", b.getFlightNo()));
        sb.append(String.format("  Route       : %s%n", b.getRoute()));
        sb.append(String.format("  Departure   : %s%n", dep));
        sb.append(String.format("  Arrival     : %s%n", arr));
        sb.append(String.format("  Seat        : %s   (%s)%n", b.getSeatNo(), b.getTravelClass()));
        if (gate != null) sb.append(String.format("  Gate        : %s%n", gate));
        sb.append(String.format("  Fare        : %s%n", Theme.money(b.getFare())));
        if (b.getPaymentMethod() != null) sb.append(String.format("  Paid by     : %s%n", b.getPaymentMethod()));
        if (extra != null && !extra.isBlank()) sb.append("  ").append(extra.trim().replace("\n", "\n  ")).append('\n');
        sb.append("  --------------------------------\n");
        sb.append(String.format("  Status      : %s%n", gate == null ? b.getStatus() : "CHECKED_IN"));
        return sb.toString();
    }
}
