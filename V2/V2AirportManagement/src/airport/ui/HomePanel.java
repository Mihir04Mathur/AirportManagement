package airport.ui;

import airport.exception.AirportException;
import airport.model.Flight;
import airport.model.User;
import airport.service.BookingService;
import airport.service.Session;
import airport.util.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Main menu: eight cards, live statistics, signed-in user and the next departures. */
public class HomePanel extends JPanel {
    private final JLabel[] stats = new JLabel[4];
    private final JLabel userLabel = new JLabel();
    private final BookingService service = new BookingService();
    private final List<MenuCard> cards = new ArrayList<>();
    private final JPanel strip = new JPanel();
    private final Consumer<String> navigate;

    public HomePanel(Consumer<String> navigate, Runnable logout) {
        this.navigate = navigate;
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        // ---- banner ----
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(Theme.NAVY);
        banner.setBorder(new EmptyBorder(18, 32, 18, 32));
        JLabel title = new JLabel("✈  Airport Management System");
        title.setFont(Theme.font(Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Flights  •  Passengers  •  Bookings  •  Check-in  •  Staff");
        sub.setFont(Theme.font(Font.PLAIN, 14));
        sub.setForeground(new Color(0xA9BDE0));
        JPanel t = new JPanel(new GridLayout(2, 1, 0, 4));
        t.setOpaque(false); t.add(title); t.add(sub);
        banner.add(t, BorderLayout.WEST);

        userLabel.setFont(Theme.font(Font.BOLD, 13));
        userLabel.setForeground(Color.WHITE);
        AppButton out = new AppButton("Sign out", new Color(0x2B4A80));
        out.setBorder(new EmptyBorder(5, 14, 5, 14));
        out.addActionListener(e -> logout.run());
        JPanel userRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userRow.setOpaque(false);
        userRow.add(userLabel); userRow.add(out);

        JPanel statRow = new JPanel(new GridLayout(1, 4, 10, 0));
        statRow.setOpaque(false);
        String[] names = {"Flights", "Passengers", "Active bookings", "Employees"};
        for (int i = 0; i < 4; i++) {
            stats[i] = new JLabel("-", SwingConstants.CENTER);
            stats[i].setFont(Theme.font(Font.BOLD, 22));
            stats[i].setForeground(Color.WHITE);
            JLabel n = new JLabel(names[i], SwingConstants.CENTER);
            n.setFont(Theme.font(Font.PLAIN, 11));
            n.setForeground(new Color(0xA9BDE0));
            JPanel tile = new JPanel(new GridLayout(2, 1));
            tile.setBackground(new Color(0x16376B));
            tile.setBorder(new EmptyBorder(6, 14, 6, 14));
            tile.add(stats[i]); tile.add(n);
            statRow.add(tile);
        }
        JPanel east = new JPanel(new BorderLayout(0, 10));
        east.setOpaque(false);
        east.add(userRow, BorderLayout.NORTH);
        east.add(statRow, BorderLayout.CENTER);
        banner.add(east, BorderLayout.EAST);
        add(banner, BorderLayout.NORTH);

        // ---- menu cards ----
        JPanel grid = new JPanel(new GridLayout(2, 4, 18, 18));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(24, 32, 12, 32));
        addCard(grid, "flights", "Manage Flights", "Schedule and edit domestic & international flights", Theme.BLUE, "✈");
        addCard(grid, "passengers", "Manage Passengers", "Register and maintain passenger records", Theme.TEAL, "☺");
        addCard(grid, "book", "Book Ticket", "Reserve a seat and pay by card, UPI or cash", Theme.GREEN, "✎");
        addCard(grid, "checkin", "Check-In", "Baggage, gate allotment and boarding pass", Theme.ORANGE, "✔");
        addCard(grid, "employees", "Manage Employees", "Pilots, cabin crew and airport staff", Theme.PURPLE, "★");
        addCard(grid, "bookings", "View Bookings", "Track, view and cancel tickets", Theme.RED, "☰");
        addCard(grid, "reports", "Reports & Export", "Statistics, sorting and CSV export", Theme.TEAL, "▤");
        addCard(grid, "log", "Activity Log", "Bookings, errors and exports", new Color(0x475569), "⚙");
        add(grid, BorderLayout.CENTER);

        // ---- next departures ----
        JLabel stripTitle = new JLabel("Next departures");
        stripTitle.setFont(Theme.font(Font.BOLD, 14));
        stripTitle.setForeground(Theme.NAVY);
        strip.setOpaque(false);
        strip.setLayout(new BoxLayout(strip, BoxLayout.X_AXIS));
        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        south.setBorder(new EmptyBorder(4, 32, 20, 32));
        south.add(stripTitle, BorderLayout.NORTH);
        south.add(strip, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    private void addCard(JPanel grid, String key, String title, String subtitle, Color accent, String icon) {
        MenuCard c = new MenuCard(key, title, subtitle, accent, icon, () -> navigate.accept(key));
        cards.add(c);
        grid.add(c);
    }

    public void onShow() {
        try {
            int[] s = service.getStats();
            for (int i = 0; i < 4; i++) stats[i].setText(String.valueOf(s[i]));
        } catch (AirportException e) {
            for (JLabel l : stats) l.setText("-");
        }
        User u = Session.getUser();
        userLabel.setText(u == null ? "" : "👤 " + u.getFullName() + "  (" + u.getRole().toLowerCase() + ")");
        for (MenuCard c : cards) c.setLocked(!Session.canAccess(c.key));
        loadDepartures();
    }

    private void loadDepartures() {
        strip.removeAll();
        try {
            List<Flight> next = service.getUpcomingFlights(4);
            if (next.isEmpty()) {
                JLabel none = new JLabel("No upcoming flights - add one in Manage Flights.");
                none.setForeground(Theme.MUTED);
                strip.add(none);
            }
            for (Flight f : next) { strip.add(chip(f)); strip.add(Box.createHorizontalStrut(12)); }
            strip.add(Box.createHorizontalGlue());
        } catch (AirportException e) {
            strip.add(new JLabel("Could not load departures: " + e.getMessage()));
        }
        strip.revalidate();
        strip.repaint();
    }

    private JPanel chip(Flight f) {
        JPanel p = Theme.card();
        p.setLayout(new GridLayout(3, 1, 0, 2));
        p.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.LineBorder(Theme.BORDER, 1, true), new EmptyBorder(8, 14, 8, 14)));
        p.setPreferredSize(new Dimension(280, 82));
        p.setMaximumSize(new Dimension(280, 82));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel l1 = new JLabel(f.getFlightNo() + "  ·  " + f.getAirline());
        l1.setFont(Theme.font(Font.BOLD, 13));
        l1.setForeground(Theme.NAVY);
        JLabel l2 = new JLabel(f.getOrigin() + " → " + f.getDestination());
        l2.setFont(Theme.font(Font.PLAIN, 13));
        LocalDateTime dep = FormatUtil.parse(f.getDepartureTime());
        String when = dep == null ? f.getDepartureTime() : dep.format(DateTimeFormatter.ofPattern("EEE dd MMM · HH:mm"));
        boolean few = f.getAvailableSeats() * 10 <= f.getTotalSeats();
        JLabel l3 = new JLabel(when + "   |   " + (f.isFull() ? "sold out" : f.getAvailableSeats() + " seats"));
        l3.setFont(Theme.font(Font.PLAIN, 12));
        l3.setForeground(f.isFull() ? Theme.RED : few ? Theme.ORANGE : Theme.MUTED);
        p.add(l1); p.add(l2); p.add(l3);
        p.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { navigate.accept("book"); }
        });
        return p;
    }

    /** A clickable rounded card; greyed out when the signed-in user may not open it. */
    private static class MenuCard extends JPanel {
        final String key;
        private final Color baseAccent;
        private final String subtitleText;
        private final JLabel icon, sub;
        private Color accent;
        private boolean hover, locked;

        MenuCard(String key, String title, String subtitle, Color accent, String iconText, Runnable action) {
            this.key = key;
            this.baseAccent = accent;
            this.accent = accent;
            this.subtitleText = "<html><body style='width:125px'>" + subtitle + "</body></html>";
            setOpaque(false);
            setLayout(new BorderLayout(12, 0));
            setBorder(new EmptyBorder(14, 24, 14, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            icon = new JLabel(iconText, SwingConstants.CENTER);
            icon.setFont(Theme.font(Font.BOLD, 26));
            icon.setForeground(Color.WHITE);
            icon.setOpaque(true);
            icon.setBackground(accent);
            icon.setPreferredSize(new Dimension(54, 54));
            JPanel icWrap = new JPanel(new GridBagLayout());
            icWrap.setOpaque(false);
            icWrap.add(icon);

            JLabel t = new JLabel(title);
            t.setFont(Theme.font(Font.BOLD, 15));
            t.setForeground(Theme.NAVY);
            sub = new JLabel(subtitleText);
            sub.setFont(Theme.font(Font.PLAIN, 12));
            sub.setForeground(Theme.MUTED);
            JPanel text = new JPanel(new GridBagLayout());
            text.setOpaque(false);
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = 0; c.weightx = 1.0; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(0, 0, 6, 0);
            text.add(t, c);
            c.gridy = 1; c.insets = new Insets(0, 0, 0, 0);
            text.add(sub, c);

            add(icWrap, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { action.run(); }
            });
        }

        void setLocked(boolean l) {
            if (locked == l) return;
            locked = l;
            accent = l ? new Color(0xB6BDCB) : baseAccent;
            icon.setBackground(accent);
            sub.setText(l ? "<html><body style='width:125px'>🔒 Administrator access only</body></html>" : subtitleText);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(hover && !locked ? accent : Theme.BORDER);
            g2.fillRoundRect(0, 0, w, h, 20, 20);
            g2.setColor(locked ? new Color(0xF4F5F8) : hover ? new Color(0xF4F8FF) : Color.WHITE);
            g2.fillRoundRect(2, 2, w - 4, h - 4, 18, 18);
            g2.setColor(accent);
            g2.fillRoundRect(0, 18, 8, h - 36, 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
