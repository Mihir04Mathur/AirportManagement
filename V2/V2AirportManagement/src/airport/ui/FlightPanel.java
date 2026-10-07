package airport.ui;

import airport.db.EmployeeDAO;
import airport.db.FlightDAO;
import airport.exception.AirportException;
import airport.model.*;
import airport.util.FormatUtil;
import airport.util.Validate;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Manage Flights screen (Domestic / International). */
public class FlightPanel extends CrudPanel {
    private final FlightDAO dao = new FlightDAO();
    private final EmployeeDAO empDao = new EmployeeDAO();
    private List<Flight> data = new ArrayList<>();

    private final JComboBox<String> type = Theme.combo();
    private final JTextField flightNo = Theme.field(15), airline = Theme.field(15), origin = Theme.field(15),
            destination = Theme.field(15), seats = Theme.field(15), fare = Theme.field(15), terminal = Theme.field(15);
    private final DateTimeSpinner departure = new DateTimeSpinner(25), arrival = new DateTimeSpinner(27);
    private final JCheckBox visa = new JCheckBox("Visa required");
    private final JComboBox<Object> pilot = Theme.combo();
    private JPanel terminalBox, visaBox;

    // route search fields
    private final JTextField from = Theme.field(6), to = Theme.field(6), date = Theme.field(8);

    public FlightPanel(Runnable goHome) {
        super("Manage Flights", "Schedule domestic and international flights",
                new String[]{"ID", "Flight", "Airline", "Route", "Departure", "Type", "Seats", "Base fare", "Pilot", "Status"}, goHome);
        type.addItem("Domestic"); type.addItem("International");
        type.addActionListener(e -> updateVisibility());
        visa.setOpaque(false);
        visa.setFont(Theme.font(Font.PLAIN, 13));
        setFormTitle("Flight details");
        Theme.hint(flightNo, "e.g. AI101");
        Theme.hint(airline, "e.g. Air India");
        Theme.hint(origin, "city");
        Theme.hint(destination, "city");
        Theme.hint(seats, "e.g. 180");
        Theme.hint(fare, "e.g. 5200");
        Theme.hint(terminal, "e.g. T1");
        Theme.hint(from, "from"); Theme.hint(to, "to"); Theme.hint(date, "yyyy-mm-dd");
        init();
        updateVisibility();
        table.getColumnModel().getColumn(9).setCellRenderer(new StatusRenderer());
    }

    /** Colours the Status column. */
    private static class StatusRenderer extends Theme.RowRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            String s = String.valueOf(v);
            l.setFont(Theme.font(Font.BOLD, 12));
            l.setForeground("Open".equals(s) ? Theme.GREEN : "Few seats".equals(s) ? Theme.ORANGE
                    : "Sold out".equals(s) ? Theme.RED : Theme.MUTED);
            return l;
        }
    }

    @Override
    protected void buildSearchBar(JPanel bar) {
        keyword.setColumns(7);
        keyword.setToolTipText("Flight no, airline or city");
        Theme.hint(keyword, "search…");
        AppButton search = new AppButton("Search", Theme.BLUE);
        AppButton reset = AppButton.secondary("Show all");
        search.addActionListener(e -> refresh());
        reset.addActionListener(e -> { clearSearch(); refresh(); });
        bar.add(keyword);
        bar.add(from); bar.add(to); bar.add(date);
        bar.add(search); bar.add(reset);
    }

    @Override protected void setWidths() { Theme.widths(table, 40, 65, 105, 165, 130, 150, 80, 90, 130, 80); }

    @Override protected void clearSearch() { keyword.setText(""); from.setText(""); to.setText(""); date.setText(""); }

    private String statusOf(Flight f) {
        LocalDateTime dep = FormatUtil.parse(f.getDepartureTime());
        if (dep != null && dep.isBefore(LocalDateTime.now())) return "Departed";
        if (f.isFull()) return "Sold out";
        return f.getAvailableSeats() * 10 <= f.getTotalSeats() ? "Few seats" : "Open";
    }

    @Override
    protected List<Object[]> fetchRows() throws AirportException {
        // uses the three OVERLOADED search methods of FlightDAO
        if (!from.getText().isBlank() || !to.getText().isBlank() || !date.getText().isBlank()) {
            data = date.getText().isBlank() ? dao.search(from.getText(), to.getText())
                                            : dao.search(from.getText(), to.getText(), date.getText());
        } else {
            data = keyword.getText().isBlank() ? dao.getAll() : dao.search(keyword.getText());
        }
        Map<Integer, String> pilots = empDao.getPilots().stream().collect(Collectors.toMap(Employee::getId, Employee::getName));
        List<Object[]> rows = new ArrayList<>();
        for (Flight f : data)
            rows.add(new Object[]{f.getId(), f.getFlightNo(), f.getAirline(), f.getOrigin() + " → " + f.getDestination(),
                    f.getDepartureTime(), FormatUtil.pretty(f.getType()) + " · " + f.getExtraInfo().replace("Terminal ", ""),
                    f.getAvailableSeats() + " / " + f.getTotalSeats(), Theme.money(f.getBaseFare()),
                    f.getPilotId() == null ? "—" : pilots.getOrDefault(f.getPilotId(), "—"), statusOf(f)});
        return rows;
    }

    @Override protected void beforeShow() { loadPilots(); }

    private void loadPilots() {
        Object selected = pilot.getSelectedItem();
        pilot.removeAllItems();
        pilot.addItem("— Not assigned —");
        try { for (Employee p : empDao.getPilots()) pilot.addItem(p); } catch (AirportException e) { Theme.error(this, e.getMessage()); }
        if (selected != null) pilot.setSelectedItem(selected);
    }

    @Override
    protected void buildForm(FormBuilder fb) {
        fb.add("Flight type", type);
        fb.add("Flight number", flightNo);
        fb.add("Airline", airline);
        fb.add("From", origin);
        fb.add("To", destination);
        fb.add("Departure  (arrow keys change the selected part)", departure);
        fb.add("Arrival", arrival);
        fb.add("Total seats", seats);
        fb.add("Base fare (₹)", fare);
        terminalBox = fb.add("Terminal", terminal);
        JPanel v = new JPanel(new BorderLayout()); v.setOpaque(false); v.add(visa);
        visaBox = fb.addGroup(v);
        fb.add("Pilot", pilot);
    }

    private void updateVisibility() {
        boolean dom = type.getSelectedIndex() == 0;
        terminalBox.setVisible(dom);
        visaBox.setVisible(!dom);
    }

    @Override
    protected void fillForm(int row) {
        Flight f = data.get(row);
        flightNo.setText(f.getFlightNo()); airline.setText(f.getAirline());
        origin.setText(f.getOrigin()); destination.setText(f.getDestination());
        departure.setText(f.getDepartureTime()); arrival.setText(f.getArrivalTime());
        seats.setText(String.valueOf(f.getTotalSeats())); fare.setText(String.valueOf(f.getBaseFare()));
        if (f instanceof DomesticFlight) {
            type.setSelectedIndex(0);
            String t = ((DomesticFlight) f).getTerminal();
            terminal.setText(t == null ? "" : t);
        } else {
            type.setSelectedIndex(1);
            visa.setSelected(((InternationalFlight) f).isVisaRequired());
        }
        pilot.setSelectedIndex(0);
        if (f.getPilotId() != null)
            for (int i = 1; i < pilot.getItemCount(); i++)
                if (((Employee) pilot.getItemAt(i)).getId() == f.getPilotId()) { pilot.setSelectedIndex(i); break; }
    }

    @Override
    protected void clearForm() {
        flightNo.setText(""); airline.setText(""); origin.setText(""); destination.setText("");
        departure.reset(); arrival.reset(); seats.setText(""); fare.setText(""); terminal.setText("");
        visa.setSelected(false); type.setSelectedIndex(0); pilot.setSelectedIndex(0);
    }

    private Flight readForm(int id) throws AirportException {
        String no = Validate.required(flightNo.getText(), "Flight number").toUpperCase();
        String al = Validate.required(airline.getText(), "Airline");
        String o = Validate.required(origin.getText(), "From"), d = Validate.required(destination.getText(), "To");
        if (o.equalsIgnoreCase(d)) throw new airport.exception.ValidationException("Origin and destination cannot be the same.");
        var dep = Validate.dateTime(departure.getText(), "Departure");
        var arr = Validate.dateTime(arrival.getText(), "Arrival");
        if (!arr.isAfter(dep)) throw new airport.exception.ValidationException("Arrival must be after departure.");
        int total = Validate.integer(seats.getText(), "Total seats", 1, 900);
        double base = Validate.decimal(fare.getText(), "Base fare", 1);
        Integer pid = pilot.getSelectedItem() instanceof Employee ? ((Employee) pilot.getSelectedItem()).getId() : null;
        String depS = dep.format(Validate.DT), arrS = arr.format(Validate.DT);
        if (type.getSelectedIndex() == 0)
            return new DomesticFlight(id, no, al, o, d, depS, arrS, total, total, base, pid, Validate.required(terminal.getText(), "Terminal"));
        return new InternationalFlight(id, no, al, o, d, depS, arrS, total, total, base, pid, visa.isSelected());
    }

    @Override protected void add() throws AirportException { dao.add(readForm(0)); }
    @Override protected void update(int id) throws AirportException { dao.update(readForm(id)); }
    @Override protected void delete(int id) throws AirportException { dao.delete(id); }
}
