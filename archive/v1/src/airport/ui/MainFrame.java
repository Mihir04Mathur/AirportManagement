package airport.ui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/** The application window: switches between the home menu and the six screens. */
public class MainFrame extends JFrame {
    private final CardLayout layout = new CardLayout();
    private final JPanel root = new JPanel(layout);
    private final Map<String, BasePanel> screens = new HashMap<>();
    private final HomePanel home;

    public MainFrame() {
        super("Airport Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1320, 800);
        setMinimumSize(new Dimension(1200, 720));
        setLocationRelativeTo(null);

        Runnable goHome = () -> show("home");
        home = new HomePanel(this::show);
        root.add(home, "home");
        register("flights", new FlightPanel(goHome));
        register("passengers", new PassengerPanel(goHome));
        register("book", new BookTicketPanel(goHome));
        register("checkin", new CheckInPanel(goHome));
        register("employees", new EmployeePanel(goHome));
        register("bookings", new BookingsPanel(goHome));
        add(root);
        show("home");
    }

    private void register(String name, BasePanel p) {
        screens.put(name, p);
        root.add(p, name);
    }

    private void show(String name) {
        if ("home".equals(name)) home.onShow(); else screens.get(name).onShow();
        layout.show(root, name);
    }
}
