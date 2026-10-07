package airport.ui;

import airport.db.BookingDAO;
import airport.exception.AirportException;
import airport.model.Booking;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** View Bookings screen with filter, ticket view and cancellation. */
public class BookingsPanel extends BasePanel {
    private final BookingDAO dao = new BookingDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Booking", "Passenger", "Flight", "Route", "Seat", "Class", "Fare", "Payment", "Booked at", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = Theme.table(model);
    private List<Booking> data = new ArrayList<>();
    private final JTextField keyword = Theme.field(16);
    private final JComboBox<String> filter = Theme.combo();
    private final JLabel count = new JLabel();

    public BookingsPanel(Runnable goHome) {
        super("View Bookings", "All tickets with payment and check-in status", goHome);
        for (String s : new String[]{"ALL", "CONFIRMED", "CHECKED_IN", "CANCELLED"}) filter.addItem(s);
        filter.setPreferredSize(new Dimension(150, 34));

        // status column gets a coloured pill-like text
        table.getColumnModel().getColumn(9).setCellRenderer(new Theme.RowRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String s = String.valueOf(v);
                l.setFont(Theme.font(Font.BOLD, 12));
                l.setForeground("CONFIRMED".equals(s) ? Theme.BLUE : "CHECKED_IN".equals(s) ? Theme.GREEN : Theme.RED);
                return l;
            }
        });
        Theme.widths(table, 65, 130, 65, 185, 45, 80, 95, 70, 130, 95);

        AppButton search = new AppButton("Search", Theme.BLUE);
        AppButton reset = AppButton.secondary("Reset");
        search.addActionListener(e -> load());
        keyword.addActionListener(e -> load());
        filter.addActionListener(e -> load());
        reset.addActionListener(e -> { keyword.setText(""); filter.setSelectedIndex(0); load(); });

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);
        bar.add(keyword); bar.add(search); bar.add(Theme.label("Status")); bar.add(filter); bar.add(reset);
        count.setForeground(Theme.MUTED);

        AppButton view = new AppButton("View ticket", Theme.BLUE);
        AppButton cancel = new AppButton("Cancel booking", Theme.RED);
        view.addActionListener(e -> viewTicket());
        cancel.addActionListener(e -> cancelBooking());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(view); actions.add(cancel);

        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 12));
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        top.add(bar, BorderLayout.WEST); top.add(count, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);
        card.add(Theme.scroll(table), BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        content.add(card, BorderLayout.CENTER);
    }

    @Override public void onShow() { load(); }

    private void load() {
        try {
            List<Booking> all = keyword.getText().isBlank() ? dao.getAll() : dao.search(keyword.getText());
            String f = (String) filter.getSelectedItem();
            data = new ArrayList<>();
            model.setRowCount(0);
            for (Booking b : all) {
                if (!"ALL".equals(f) && !b.getStatus().equals(f)) continue;
                data.add(b);
                model.addRow(new Object[]{"#" + b.getId(), b.getPassengerName(), b.getFlightNo(), b.getRoute(), b.getSeatNo(),
                        b.getTravelClass(), Theme.money(b.getFare()), b.getPaymentMethod(), b.getBookingTime(), b.getStatus()});
            }
            count.setText(data.size() + " booking(s)");
        } catch (AirportException e) {
            Theme.error(this, e.getMessage());
        }
    }

    private Booking selected() {
        int r = table.getSelectedRow();
        if (r < 0) { Theme.error(this, "Select a booking first."); return null; }
        return data.get(r);
    }

    private void viewTicket() {
        Booking b = selected();
        if (b != null) Tickets.show(this, b, null, "");
    }

    private void cancelBooking() {
        Booking b = selected();
        if (b == null) return;
        if (!Theme.confirm(this, "Cancel booking #" + b.getId() + " for " + b.getPassengerName() + "?")) return;
        try {
            dao.cancel(b.getId());
            Theme.info(this, "Booking cancelled. The seat is available again.");
            load();
        } catch (AirportException e) {
            Theme.error(this, e.getMessage());
        }
    }
}
