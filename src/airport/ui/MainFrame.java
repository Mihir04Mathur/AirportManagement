package airport.ui;

import airport.model.User;
import airport.service.AuthService;
import airport.service.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/** The application window: login screen, home menu and the eight screens, with shortcuts and a status footer. */
public class MainFrame extends JFrame {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("EEE dd MMM yyyy  HH:mm:ss");

    private final CardLayout layout = new CardLayout();
    private final JPanel root = new JPanel(layout);
    private final Map<String, BasePanel> screens = new HashMap<>();
    private final AuthService auth = new AuthService();
    private final LoginPanel login;
    private final HomePanel home;
    private final JLabel userInfo = new JLabel();
    private final JLabel clock = new JLabel();
    private String current = "login";

    public MainFrame() {
        super("Airport Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1320, 820);
        setMinimumSize(new Dimension(1200, 740));
        setLocationRelativeTo(null);
        setIconImage(makeIcon());

        Runnable goHome = () -> show("home");
        login = new LoginPanel(auth, () -> {
            show("home");
            User u = Session.getUser();
            Toast.success(getRootPane(), "Welcome, " + (u == null ? "" : u.getFullName()));
        });
        home = new HomePanel(this::show, this::logout);
        root.add(login, "login");
        root.add(home, "home");
        register("flights", new FlightPanel(goHome));
        register("passengers", new PassengerPanel(goHome));
        register("book", new BookTicketPanel(goHome));
        register("checkin", new CheckInPanel(goHome));
        register("employees", new EmployeePanel(goHome));
        register("bookings", new BookingsPanel(goHome));
        register("reports", new ReportsPanel(goHome));
        register("log", new ActivityLogPanel(goHome));

        setLayout(new BorderLayout());
        add(root, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
        installShortcuts();
        startClock();
        show("login");
    }

    private void register(String name, BasePanel p) {
        screens.put(name, p);
        root.add(p, name);
    }

    /** Switches screen. Not signed in -> login; admin-only screens are refused for staff. */
    private void show(String name) {
        if (!"login".equals(name) && !Session.isLoggedIn()) name = "login";
        if (!"login".equals(name) && !"home".equals(name) && !Session.canAccess(name)) {
            Toast.error(getRootPane(), "This screen is for administrators only.");
            return;
        }
        current = name;
        if ("login".equals(name)) login.onShow();
        else if ("home".equals(name)) home.onShow();
        else screens.get(name).onShow();
        layout.show(root, name);
        updateUserInfo();
    }

    private void logout() {
        auth.logout();
        show("login");
    }

    // ---------- footer: shortcut hints, user, clock ----------
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new javax.swing.border.CompoundBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER), new EmptyBorder(6, 16, 6, 16)));
        JLabel hints = new JLabel("Esc  Home      F5  Refresh      Ctrl+L  Sign out      Click a table header to sort");
        hints.setFont(Theme.font(Font.PLAIN, 11));
        hints.setForeground(Theme.MUTED);
        userInfo.setFont(Theme.font(Font.BOLD, 11));
        userInfo.setForeground(Theme.NAVY);
        clock.setFont(Theme.font(Font.PLAIN, 11));
        clock.setForeground(Theme.MUTED);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);
        right.add(userInfo); right.add(clock);
        footer.add(hints, BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    private void updateUserInfo() {
        User u = Session.getUser();
        userInfo.setText(u == null ? "Not signed in" : u.getFullName() + " · " + u.getRole().toLowerCase());
    }

    private void startClock() {
        Timer t = new Timer(1000, e -> clock.setText(LocalDateTime.now().format(CLOCK)));
        t.setInitialDelay(0);
        t.start();
    }

    private void installShortcuts() {
        JRootPane rp = getRootPane();
        InputMap im = rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = rp.getActionMap();
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "goHome");
        im.put(KeyStroke.getKeyStroke("F5"), "refresh");
        im.put(KeyStroke.getKeyStroke("control L"), "signOut");
        am.put("goHome", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { if (Session.isLoggedIn() && !"home".equals(current)) show("home"); }
        });
        am.put("refresh", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (Session.isLoggedIn()) { show(current); Toast.info(getRootPane(), "Refreshed"); }
            }
        });
        am.put("signOut", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { if (Session.isLoggedIn()) logout(); }
        });
    }

    private static Image makeIcon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Theme.BLUE);
        g.fillOval(0, 0, 64, 64);
        g.setColor(Color.WHITE);
        g.setFont(Theme.font(Font.BOLD, 36));
        FontMetrics fm = g.getFontMetrics();
        g.drawString("✈", (64 - fm.stringWidth("✈")) / 2, (64 - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
        return img;
    }
}
