package airport.ui;

import airport.service.BookingService;
import airport.exception.AirportException;
import airport.util.Validate;
import airport.model.Booking;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Check-In screen: lists CONFIRMED bookings, records baggage + gate. */
public class CheckInPanel extends BasePanel {
    private final BookingService dao = new BookingService();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Booking", "Passenger", "Flight", "Route", "Seat", "Class"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = Theme.table(model);
    private List<Booking> data = new ArrayList<>();

    private final JLabel selected = new JLabel("Select a booking from the list");
    private final JTextField baggage = Theme.field(10);
    private final JComboBox<String> gate = Theme.combo();

    public CheckInPanel(Runnable goHome) {
        super("Passenger Check-In", "Confirm baggage, allot a gate and issue the boarding pass", goHome);

        Theme.widths(table, 65, 140, 70, 200, 50, 80);
        JPanel left = Theme.card();
        left.setLayout(new BorderLayout(0, 12));
        left.add(Theme.heading("Bookings waiting for check-in"), BorderLayout.NORTH);
        left.add(Theme.scroll(table), BorderLayout.CENTER);

        gate.addItem("Auto-assign");
        for (int i = 1; i <= 12; i++) gate.addItem("G" + i);
        selected.setFont(Theme.font(Font.BOLD, 14));
        selected.setForeground(Theme.NAVY);
        FormBuilder fb = new FormBuilder();
        fb.add("Selected booking", selected);
        Theme.hint(baggage, "e.g. 18.5");
        fb.add("Baggage weight (kg)  -  limit 25 economy / 40 business", baggage);
        fb.add("Boarding gate", gate);
        AppButton go = new AppButton("Check in passenger", Theme.GREEN);
        go.setFont(Theme.font(Font.BOLD, 15));
        go.addActionListener(e -> checkIn());

        JPanel right = Theme.card();
        right.setLayout(new BorderLayout(0, 12));
        right.setPreferredSize(new Dimension(380, 100));
        right.add(Theme.heading("Check-in details"), BorderLayout.NORTH);
        JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false);
        wrap.add(fb.build(), BorderLayout.NORTH);
        right.add(wrap, BorderLayout.CENTER);
        right.add(go, BorderLayout.SOUTH);

        content.add(left, BorderLayout.CENTER);
        content.add(right, BorderLayout.EAST);

        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (!e.getValueIsAdjusting() && r >= 0) selected.setText(data.get(table.convertRowIndexToModel(r)).toString());
        });
    }

    @Override
    public void onShow() {
        try {
            data = dao.getByStatus("CONFIRMED");
            model.setRowCount(0);
            for (Booking b : data)
                model.addRow(new Object[]{"#" + b.getId(), b.getPassengerName(), b.getFlightNo(), b.getRoute(), b.getSeatNo(), b.getTravelClass()});
            selected.setText("Select a booking from the list");
            baggage.setText("");
        } catch (AirportException e) {
            Theme.error(this, e.getMessage());
        }
    }

    private void checkIn() {
        int r = table.getSelectedRow();
        if (r < 0) { Theme.error(this, "Select a booking first."); return; }
        Booking b = data.get(table.convertRowIndexToModel(r));
        try {
            double kg = Validate.decimal(baggage.getText(), "Baggage weight", 0);
            String g = gate.getSelectedIndex() == 0 ? dao.checkIn(b.getId(), kg)          // overload 1: auto gate
                                                    : dao.checkIn(b.getId(), kg, (String) gate.getSelectedItem());  // overload 2
            Tickets.show(this, b, g, String.format("Baggage     : %.1f kg", kg));
            onShow();
        } catch (AirportException e) {
            Theme.error(this, e.getMessage());
        }
    }
}
