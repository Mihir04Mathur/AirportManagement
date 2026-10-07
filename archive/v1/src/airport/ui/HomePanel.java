package airport.ui;

import airport.db.BookingDAO;
import airport.exception.AirportException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Main menu with six big cards and live statistics. */
public class HomePanel extends JPanel {
    private final JLabel[] stats = new JLabel[4];
    private final BookingDAO dao = new BookingDAO();

    public HomePanel(java.util.function.Consumer<String> navigate) {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        // ---- banner ----
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(Theme.NAVY);
        banner.setBorder(new EmptyBorder(26, 36, 26, 36));
        JLabel title = new JLabel("✈  Airport Management System");
        title.setFont(Theme.font(Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Flights  •  Passengers  •  Bookings  •  Check-in  •  Staff");
        sub.setFont(Theme.font(Font.PLAIN, 14));
        sub.setForeground(new Color(0xA9BDE0));
        JPanel t = new JPanel(new GridLayout(2, 1, 0, 4));
        t.setOpaque(false); t.add(title); t.add(sub);
        banner.add(t, BorderLayout.WEST);

        JPanel statRow = new JPanel(new GridLayout(1, 4, 12, 0));
        statRow.setOpaque(false);
        String[] names = {"Flights", "Passengers", "Active bookings", "Employees"};
        for (int i = 0; i < 4; i++) {
            stats[i] = new JLabel("-", SwingConstants.CENTER);
            stats[i].setFont(Theme.font(Font.BOLD, 24));
            stats[i].setForeground(Color.WHITE);
            JLabel n = new JLabel(names[i], SwingConstants.CENTER);
            n.setFont(Theme.font(Font.PLAIN, 11));
            n.setForeground(new Color(0xA9BDE0));
            JPanel tile = new JPanel(new GridLayout(2, 1));
            tile.setBackground(new Color(0x16376B));
            tile.setBorder(new EmptyBorder(8, 18, 8, 18));
            tile.add(stats[i]); tile.add(n);
            statRow.add(tile);
        }
        banner.add(statRow, BorderLayout.EAST);
        add(banner, BorderLayout.NORTH);

        // ---- menu cards ----
        JPanel grid = new JPanel(new GridLayout(2, 3, 22, 22));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(30, 36, 36, 36));
        grid.add(new MenuCard("Manage Flights", "Schedule and edit domestic & international flights", Theme.BLUE, "✈", () -> navigate.accept("flights")));
        grid.add(new MenuCard("Manage Passengers", "Register and maintain passenger records", Theme.TEAL, "☺", () -> navigate.accept("passengers")));
        grid.add(new MenuCard("Book Ticket", "Reserve a seat and pay by card, UPI or cash", Theme.GREEN, "✎", () -> navigate.accept("book")));
        grid.add(new MenuCard("Check-In", "Baggage, gate allotment and boarding pass", Theme.ORANGE, "✔", () -> navigate.accept("checkin")));
        grid.add(new MenuCard("Manage Employees", "Pilots, cabin crew and airport staff", Theme.PURPLE, "★", () -> navigate.accept("employees")));
        grid.add(new MenuCard("View Bookings", "Track, view and cancel tickets", Theme.RED, "☰", () -> navigate.accept("bookings")));
        add(grid, BorderLayout.CENTER);
    }

    public void onShow() {
        try {
            int[] s = dao.getStats();
            for (int i = 0; i < 4; i++) stats[i].setText(String.valueOf(s[i]));
        } catch (AirportException e) {
            for (JLabel l : stats) l.setText("-");
        }
    }

    /** A clickable rounded card. */
    private static class MenuCard extends JPanel {
        private boolean hover;
        private final Color accent;

        MenuCard(String title, String subtitle, Color accent, String icon, Runnable action) {
            this.accent = accent;
            setOpaque(false);
            setLayout(new BorderLayout(16, 0));
            setBorder(new EmptyBorder(20, 28, 20, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel ic = new JLabel(icon, SwingConstants.CENTER);
            ic.setFont(Theme.font(Font.BOLD, 30));
            ic.setForeground(Color.WHITE);
            ic.setOpaque(true);
            ic.setBackground(accent);
            ic.setPreferredSize(new Dimension(64, 64));
            JPanel icWrap = new JPanel(new GridBagLayout());
            icWrap.setOpaque(false);
            icWrap.add(ic);

            JLabel t = new JLabel(title);
            t.setFont(Theme.font(Font.BOLD, 19));
            t.setForeground(Theme.NAVY);
            JLabel s = new JLabel("<html><body style='width:190px'>" + subtitle + "</body></html>");
            s.setFont(Theme.font(Font.PLAIN, 12));
            s.setForeground(Theme.MUTED);
            JPanel text = new JPanel(new GridBagLayout());
            text.setOpaque(false);
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(0, 0, 6, 0);
            text.add(t, c);
            c.gridy = 1; c.insets = new Insets(0, 0, 0, 0);
            text.add(s, c);

            add(icWrap, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { action.run(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(hover ? accent : Theme.BORDER);
            g2.fillRoundRect(0, 0, w, h, 20, 20);
            g2.setColor(hover ? new Color(0xF4F8FF) : Color.WHITE);
            g2.fillRoundRect(2, 2, w - 4, h - 4, 18, 18);
            g2.setColor(accent);
            g2.fillRoundRect(0, 18, 8, h - 36, 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
