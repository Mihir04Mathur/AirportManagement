package airport.ui;

import airport.db.FlightDAO;
import airport.model.Booking;
import airport.model.Flight;

import javax.swing.*;
import java.awt.*;

/** Builds and shows the e-ticket / boarding pass dialog. */
public final class Tickets {
    private Tickets() { }

    public static void show(Component parent, Booking b, String gate, String extra) {
        String dep = "-", arr = "-";
        try {
            Flight f = new FlightDAO().getById(b.getFlightId() != 0 ? b.getFlightId() : 0);
            if (f != null) { dep = f.getDepartureTime(); arr = f.getArrivalTime(); }
        } catch (Exception ignored) { }
        StringBuilder sb = new StringBuilder();
        sb.append(gate == null ? "        ✈  E-TICKET\n" : "        ✈  BOARDING PASS\n");
        sb.append("  ────────────────────────────────\n");
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
        if (extra != null && !extra.isEmpty()) sb.append("  ").append(extra).append('\n');
        sb.append("  ────────────────────────────────\n");
        sb.append(String.format("  Status      : %s%n", gate == null ? b.getStatus() : "CHECKED_IN"));

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setBackground(new Color(0xFFFBEB));
        area.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JOptionPane.showMessageDialog(parent, area, gate == null ? "Ticket" : "Boarding pass", JOptionPane.PLAIN_MESSAGE);
    }
}
