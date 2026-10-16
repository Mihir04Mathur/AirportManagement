package airport.ui;

import airport.db.PassengerDAO;
import airport.exception.AirportException;
import airport.model.*;
import airport.service.BookingService;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/** Book Ticket screen: passenger + flight + class + seat, then pay by Card / UPI / Cash. */
public class BookTicketPanel extends BasePanel {
    private final PassengerDAO passengerDao = new PassengerDAO();
    private final BookingService service = new BookingService();

    private final JComboBox<Passenger> passengerBox = Theme.combo();
    private final JComboBox<Flight> flightBox = Theme.combo();
    private final JComboBox<String> classBox = Theme.combo();
    private final JComboBox<String> methodBox = Theme.combo();

    private final CardLayout payLayout = new CardLayout();
    private final JPanel payPanel = new JPanel(payLayout);
    private final JTextField cardHolder = Theme.field(15), cardNo = Theme.field(15), cvv = Theme.field(15),
            upiId = Theme.field(15), cash = Theme.field(15);

    private final JLabel seatLabel = new JLabel();
    private String selectedSeat;                       // null = auto-assign
    private final JLabel summary = new JLabel();

    public BookTicketPanel(Runnable goHome) {
        super("Book Ticket", "Select passenger and flight, pick a seat, then take the payment", goHome);
        classBox.addItem("ECONOMY"); classBox.addItem("BUSINESS");
        methodBox.addItem("CARD"); methodBox.addItem("UPI"); methodBox.addItem("CASH");
        Theme.hint(cardHolder, "name on the card");
        Theme.hint(cardNo, "16 digits");
        Theme.hint(cvv, "3 digits");
        Theme.hint(upiId, "name@okbank");
        Theme.hint(cash, "amount received");

        // ---- seat picker row ----
        seatLabel.setFont(Theme.font(Font.BOLD, 14));
        seatLabel.setForeground(Theme.NAVY);
        AppButton choose = new AppButton("Choose seat…", Theme.PURPLE);
        AppButton auto = AppButton.secondary("Auto");
        choose.addActionListener(e -> chooseSeat());
        auto.addActionListener(e -> { selectedSeat = null; refreshSummary(); });
        JPanel seatRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        seatRow.setOpaque(false);
        seatRow.add(choose); seatRow.add(auto); seatRow.add(seatLabel);

        // ---- left: booking form ----
        FormBuilder fb = new FormBuilder();
        fb.add("Passenger", passengerBox);
        fb.add("Flight", flightBox);
        fb.add("Travel class", classBox);
        fb.add("Seat", seatRow);
        fb.add("Payment method", methodBox);

        FormBuilder cardForm = new FormBuilder();
        cardForm.add("Card holder name", cardHolder);
        cardForm.add("Card number (16 digits)", cardNo);
        cardForm.add("CVV (3 digits)", cvv);
        FormBuilder upiForm = new FormBuilder();
        upiForm.add("UPI ID (e.g. name@okbank)", upiId);
        FormBuilder cashForm = new FormBuilder();
        cashForm.add("Cash received (₹)", cash);
        payPanel.setOpaque(false);
        payPanel.add(cardForm.build(), "CARD");
        payPanel.add(upiForm.build(), "UPI");
        payPanel.add(cashForm.build(), "CASH");
        fb.addGroup(payPanel);

        JPanel left = Theme.card();
        left.setLayout(new BorderLayout(0, 12));
        left.add(Theme.heading("Booking details"), BorderLayout.NORTH);
        JPanel formWrap = new JPanel(new BorderLayout());
        formWrap.setOpaque(false);
        formWrap.add(fb.build(), BorderLayout.NORTH);
        left.add(formWrap, BorderLayout.CENTER);

        // ---- right: summary + confirm ----
        summary.setVerticalAlignment(SwingConstants.TOP);
        summary.setFont(Theme.font(Font.PLAIN, 13));
        AppButton confirm = new AppButton("Confirm booking & pay", Theme.GREEN);
        confirm.setFont(Theme.font(Font.BOLD, 15));
        confirm.addActionListener(e -> book());
        JPanel right = Theme.card();
        right.setLayout(new BorderLayout(0, 12));
        right.setPreferredSize(new Dimension(400, 100));
        right.add(Theme.heading("Fare summary"), BorderLayout.NORTH);
        right.add(summary, BorderLayout.CENTER);
        right.add(confirm, BorderLayout.SOUTH);

        content.add(left, BorderLayout.CENTER);
        content.add(right, BorderLayout.EAST);

        methodBox.addActionListener(e -> { payLayout.show(payPanel, (String) methodBox.getSelectedItem()); refreshSummary(); });
        classBox.addActionListener(e -> refreshSummary());
        flightBox.addActionListener(e -> { selectedSeat = null; refreshSummary(); });   // a seat belongs to one flight
        cash.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { refreshSummary(); }
        });
    }

    @Override
    public void onShow() {
        try {
            passengerBox.removeAllItems();
            for (Passenger p : passengerDao.getAll()) passengerBox.addItem(p);
            flightBox.removeAllItems();
            for (Flight f : service.getBookableFlights()) flightBox.addItem(f);   // only upcoming flights with seats
        } catch (AirportException e) {
            Theme.error(this, e.getMessage());
        }
        selectedSeat = null;
        refreshSummary();
    }

    private void chooseSeat() {
        Flight f = (Flight) flightBox.getSelectedItem();
        if (f == null) { Theme.error(this, "No flight with free seats is available."); return; }
        try {
            Set<String> taken = service.getTakenSeats(f.getId());
            SeatMapDialog.Result r = SeatMapDialog.choose(this, f, taken, selectedSeat);
            if (!r.cancelled) { selectedSeat = r.seat; refreshSummary(); }
        } catch (AirportException e) {
            Theme.error(this, e.getMessage());
        }
    }

    /** Builds the right Payment subclass from the form (polymorphism). */
    private Payment buildPayment(double fare) {
        switch ((String) methodBox.getSelectedItem()) {
            case "CARD": return new CardPayment(fare, cardHolder.getText(), cardNo.getText(), cvv.getText());
            case "UPI":  return new UPIPayment(fare, upiId.getText());
            default:     return new CashPayment(fare, parseCash());
        }
    }

    private double parseCash() {
        try { return Double.parseDouble(cash.getText().trim()); } catch (NumberFormatException e) { return 0; }
    }

    private void refreshSummary() {
        seatLabel.setText(selectedSeat == null ? "best available" : selectedSeat);
        Flight f = (Flight) flightBox.getSelectedItem();
        if (f == null) {
            summary.setText("<html><body style='width:320px'>No upcoming flight with free seats is available.</body></html>");
            return;
        }
        String cls = (String) classBox.getSelectedItem();
        double fare = f.calculateFare(cls);
        Payment pay = buildPayment(fare);
        summary.setText("<html><body style='width:340px'>"
                + "<div style='font-size:15px;color:#0B2447'><b>" + f.getFlightNo() + "</b> &nbsp; " + f.getAirline() + "</div>"
                + "<div style='padding:2px 0 8px 0'>" + f.getOrigin() + " → " + f.getDestination() + "</div>"
                + "<table cellpadding='3' cellspacing='0'>"
                + row("Type", f.getType()) + row("Info", f.getExtraInfo())
                + row("Departure", f.getDepartureTime()) + row("Arrival", f.getArrivalTime())
                + row("Seats left", String.valueOf(f.getAvailableSeats()))
                + row("Seat", selectedSeat == null ? "Best available" : selectedSeat)
                + row("Base fare", Theme.money(f.getBaseFare()))
                + row("Ticket fare (" + cls.toLowerCase() + ")", Theme.money(fare))
                + row("Fee (" + pay.getMethod() + ")", Theme.money(pay.getProcessingFee()))
                + "</table><div style='padding-top:10px;font-size:17px;color:#16A34A'><b>Total: "
                + Theme.money(pay.getTotalCharge()) + "</b></div></body></html>");
    }

    private String row(String k, String v) {
        return "<tr><td width='175' style='color:#6B7280'>" + k + "</td><td width='165' align='right'>" + v + "</td></tr>";
    }

    private void book() {
        Passenger p = (Passenger) passengerBox.getSelectedItem();
        Flight f = (Flight) flightBox.getSelectedItem();
        if (p == null) { Theme.error(this, "Add a passenger first (Manage Passengers)."); return; }
        if (f == null) { Theme.error(this, "No upcoming flight with free seats is available."); return; }
        String cls = (String) classBox.getSelectedItem();
        Payment pay = buildPayment(f.calculateFare(cls));
        try {
            Booking b = service.bookTicket(p.getId(), f.getId(), selectedSeat, cls, pay);
            String extra = "Total paid   : " + Theme.money(pay.getTotalCharge());
            if (pay instanceof CashPayment) extra += "\nChange       : " + Theme.money(((CashPayment) pay).getChange());
            cardHolder.setText(""); cardNo.setText(""); cvv.setText(""); upiId.setText(""); cash.setText("");
            onShow();
            Toast.success(this, "Booking #" + b.getId() + " confirmed - seat " + b.getSeatNo());
            Tickets.show(this, b, null, extra);
        } catch (AirportException e) {
            Theme.error(this, e.getMessage());
            onShow();
        }
    }
}
