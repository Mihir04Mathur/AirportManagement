package airport.ui;

import airport.exception.AirportException;
import airport.model.Booking;
import airport.service.BookingService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** View Bookings screen with filter, ticket view and cancellation with refund. */
public class BookingsPanel extends BasePanel {
    private final BookingService dao = new BookingService();
    private final javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
            new String[]{"Booking", "Passenger", "Flight", "Route", "Seat", "Class", "Fare", "Refund", "Payment", "Booked at", "Status"}, 0) {
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
        Theme.hint(keyword, "name, flight or booking no.");

        // status column gets a coloured text
        table.getColumnModel().getColumn(10).setCellRenderer(new Theme.RowRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String s = String.valueOf(v);
                l.setFont(Theme.font(Font.BOLD, 12));
                l.setForeground("CONFIRMED".equals(s) ? Theme.BLUE : "CHECKED_IN".equals(s) ? Theme.GREEN : Theme.RED);
                return l;
            }
        });
        Theme.widths(table, 65, 125, 65, 175, 45, 80, 90, 85, 70, 125, 95);

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
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) viewTicket(); }
        });
        JLabel tip = new JLabel("Tip: double-click a row to open its ticket   •   click a column header to sort");
        tip.setForeground(Theme.MUTED);
        tip.setFont(Theme.font(Font.PLAIN, 12));
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.add(view); btns.add(cancel);
        actions.add(tip, BorderLayout.WEST);
        actions.add(btns, BorderLayout.EAST);

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
                        b.getTravelClass(), Theme.money(b.getFare()), b.getRefundAmount() > 0 ? Theme.money(b.getRefundAmount()) : "—",
                        b.getPaymentMethod(), b.getBookingTime(), b.getStatus()});
            }
            count.setText(data.size() + " booking(s)");
        } catch (AirportException e) {
            Theme.error(this, e.getMessage());
        }
    }

    private Booking selected() {
        int r = table.getSelectedRow();
        if (r < 0) { Theme.error(this, "Select a booking first."); return null; }
        return data.get(table.convertRowIndexToModel(r));
    }

    private void viewTicket() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        Booking b = data.get(table.convertRowIndexToModel(r));
        Tickets.show(this, b, null, "");
    }

    private void cancelBooking() {
        Booking b = selected();
        if (b == null) return;
        try {
            double refund = dao.previewRefund(b);
            int pct = (int) Math.round(refund * 100 / b.getFare());
            if (!Theme.confirm(this, "Cancel booking #" + b.getId() + " for " + b.getPassengerName() + "?\n\n"
                    + "Refund: " + Theme.money(refund) + "  (" + pct + "% of the fare)\n"
                    + "Cancelling 24 hours or more before departure refunds 90%, later refunds 50%.")) return;
            double paid = dao.cancel(b.getId());
            Toast.success(this, "Booking #" + b.getId() + " cancelled - refund " + Theme.money(paid));
            load();
        } catch (AirportException e) {
            Theme.error(this, e.getMessage());
        }
    }
}
