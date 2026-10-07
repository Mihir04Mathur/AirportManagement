package airport.ui;

import airport.model.Booking;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** All trips of one passenger with totals. */
public final class HistoryDialog {
    private HistoryDialog() { }

    public static void show(Component parent, String passengerName, List<Booking> trips) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(owner, "Trips - " + passengerName, Dialog.ModalityType.APPLICATION_MODAL);
        DefaultTableModel m = new DefaultTableModel(new String[]{"Booking", "Flight", "Route", "Seat", "Class", "Fare", "Refund", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Booking b : trips)
            m.addRow(new Object[]{"#" + b.getId(), b.getFlightNo(), b.getRoute(), b.getSeatNo(), b.getTravelClass(),
                    Theme.money(b.getFare()), b.getRefundAmount() > 0 ? Theme.money(b.getRefundAmount()) : "-", b.getStatus()});
        JTable t = Theme.table(m);
        Theme.widths(t, 60, 70, 190, 50, 80, 100, 100, 90);

        double spent = trips.stream().filter(Booking::isActive).mapToDouble(Booking::getFare).sum();
        double refunds = trips.stream().mapToDouble(Booking::getRefundAmount).sum();
        long active = trips.stream().filter(Booking::isActive).count();
        JLabel summary = new JLabel(trips.size() + " booking(s)   •   " + active + " active   •   spent " + Theme.money(spent)
                + "   •   refunded " + Theme.money(refunds));
        summary.setFont(Theme.font(Font.BOLD, 13));
        summary.setForeground(Theme.NAVY);

        AppButton close = AppButton.secondary("Close");
        close.addActionListener(e -> d.dispose());
        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.add(summary, BorderLayout.WEST);
        foot.add(close, BorderLayout.EAST);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(16, 16, 14, 16));
        JLabel title = Theme.heading(passengerName);
        root.add(title, BorderLayout.NORTH);
        root.add(Theme.scroll(t), BorderLayout.CENTER);
        root.add(foot, BorderLayout.SOUTH);
        d.setContentPane(root);
        d.setSize(820, 420);
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
    }
}
