package airport.ui;

import airport.db.BookingDAO;
import airport.model.Flight;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Visual seat map: pick a free seat (red/grey = taken, blue tint = business rows). */
public class SeatMapDialog extends JDialog {
    /** What the user decided. */
    public static class Result {
        public final boolean cancelled;
        public final String seat;           // null = "auto-assign"
        Result(boolean cancelled, String seat) { this.cancelled = cancelled; this.seat = seat; }
    }

    private boolean cancelled = true;
    private String selected;
    private final Map<String, SeatButton> buttons = new HashMap<>();
    private final JLabel selectedLabel = new JLabel();
    private final AppButton confirm = new AppButton("Confirm seat", Theme.GREEN);

    public static Result choose(Component parent, Flight flight, Set<String> taken, String current) {
        SeatMapDialog d = new SeatMapDialog(SwingUtilities.getWindowAncestor(parent), flight, taken, current);
        d.setVisible(true);                                  // modal: blocks until closed
        return new Result(d.cancelled, d.selected);
    }

    private SeatMapDialog(Window owner, Flight flight, Set<String> taken, String current) {
        super(owner, "Choose your seat", ModalityType.APPLICATION_MODAL);
        if (current != null && !taken.contains(current)) selected = current;

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(16, 18, 14, 18));

        // ---- header + legend ----
        JPanel head = new JPanel(new GridLayout(3, 1, 0, 4));
        head.setOpaque(false);
        JLabel title = new JLabel(flight.getFlightNo() + "   " + flight.getOrigin() + " → " + flight.getDestination());
        title.setFont(Theme.font(Font.BOLD, 17));
        title.setForeground(Theme.NAVY);
        JLabel sub = new JLabel(flight.getAvailableSeats() + " of " + flight.getTotalSeats() + " seats free   •   rows 1-3 are business class");
        sub.setForeground(Theme.MUTED);
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        legend.setOpaque(false);
        legend.add(legendItem("Free", SeatButton.State.FREE, false));
        legend.add(legendItem("Business", SeatButton.State.FREE, true));
        legend.add(legendItem("Taken", SeatButton.State.TAKEN, false));
        legend.add(legendItem("Yours", SeatButton.State.SELECTED, false));
        head.add(title); head.add(sub); head.add(legend);
        root.add(head, BorderLayout.NORTH);

        // ---- seat grid: row no | A B C | aisle | D E F ----
        int total = flight.getTotalSeats();
        int rows = (total + BookingDAO.SEATS_PER_ROW - 1) / BookingDAO.SEATS_PER_ROW;
        JPanel grid = new JPanel(new GridLayout(rows + 1, 8, 6, 6));
        grid.setOpaque(false);
        String[] heads = {"", "A", "B", "C", "", "D", "E", "F"};
        for (String h : heads) grid.add(centered(h, Theme.MUTED, Font.BOLD));
        for (int r = 0; r < rows; r++) {
            boolean businessRow = r * BookingDAO.SEATS_PER_ROW < Math.min(BookingDAO.BUSINESS_SEATS, total);
            grid.add(centered(String.valueOf(r + 1), businessRow ? Theme.BLUE : Theme.MUTED, Font.BOLD));
            for (int c = 0; c < 6; c++) {
                if (c == 3) grid.add(new JLabel());                          // aisle
                int idx = r * BookingDAO.SEATS_PER_ROW + c;
                if (idx >= total) { grid.add(new JLabel()); continue; }      // last, partly filled row
                String label = BookingDAO.seatLabel(idx);
                boolean isTaken = taken.contains(label);
                SeatButton b = new SeatButton(String.valueOf(label.charAt(label.length() - 1)),
                        BookingDAO.isBusinessSeat(idx, total), isTaken);
                if (isTaken) b.setToolTipText("Seat " + label + " is taken");
                else { b.setToolTipText("Seat " + label); b.addActionListener(e -> select(label)); }
                buttons.put(label, b);
                grid.add(b);
            }
        }
        JPanel gridWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        gridWrap.setOpaque(false);
        gridWrap.add(grid);
        JScrollPane scroll = new JScrollPane(gridWrap);
        scroll.setBorder(new javax.swing.border.LineBorder(Theme.BORDER, 1, true));
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.setPreferredSize(new Dimension(540, 400));
        root.add(scroll, BorderLayout.CENTER);

        // ---- footer ----
        selectedLabel.setFont(Theme.font(Font.BOLD, 14));
        selectedLabel.setForeground(Theme.NAVY);
        AppButton auto = AppButton.secondary("Auto-assign");
        AppButton cancel = AppButton.secondary("Cancel");
        auto.addActionListener(e -> { selected = null; cancelled = false; dispose(); });
        cancel.addActionListener(e -> dispose());
        confirm.addActionListener(e -> { cancelled = false; dispose(); });
        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.add(auto); btns.add(cancel); btns.add(confirm);
        foot.add(selectedLabel, BorderLayout.WEST);
        foot.add(btns, BorderLayout.EAST);
        root.add(foot, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);
        refreshSelection();
        if (selected != null && buttons.containsKey(selected))
            SwingUtilities.invokeLater(() -> buttons.get(selected).scrollRectToVisible(new Rectangle(0, -80, 40, 200)));
    }

    private void select(String label) { selected = label; refreshSelection(); }

    private void refreshSelection() {
        for (Map.Entry<String, SeatButton> e : buttons.entrySet())
            if (e.getValue().getState() != SeatButton.State.TAKEN)
                e.getValue().setState(e.getKey().equals(selected) ? SeatButton.State.SELECTED : SeatButton.State.FREE);
        selectedLabel.setText(selected == null ? "No seat selected" : "Seat " + selected);
        confirm.setEnabled(selected != null);
    }

    private static JLabel centered(String text, Color c, int style) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(Theme.font(style, 12));
        l.setForeground(c);
        return l;
    }

    private JPanel legendItem(String text, SeatButton.State state, boolean business) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);
        SeatButton b = new SeatButton("", business, state == SeatButton.State.TAKEN);
        b.setPreferredSize(new Dimension(20, 18));
        b.setState(state);
        p.add(b);
        JLabel l = new JLabel(text);
        l.setFont(Theme.font(Font.PLAIN, 12));
        l.setForeground(Theme.MUTED);
        p.add(l);
        return p;
    }

    /** One seat, drawn as a rounded square. */
    private static class SeatButton extends JButton {
        enum State { FREE, TAKEN, SELECTED }
        private State state = State.FREE;
        private final boolean business;

        SeatButton(String letter, boolean business, boolean taken) {
            super(letter);
            this.business = business;
            if (taken) state = State.TAKEN;
            setPreferredSize(new Dimension(42, 36));
            setMargin(new Insets(0, 0, 0, 0));
            setBorder(null);
            setFont(Theme.font(Font.BOLD, 12));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(taken ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            applyColors();
        }

        State getState() { return state; }
        void setState(State s) { state = s; applyColors(); repaint(); }

        private void applyColors() {
            setForeground(state == State.SELECTED ? Color.WHITE : state == State.TAKEN ? new Color(0x9CA3AF) : Theme.NAVY);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill, line;
            switch (state) {
                case SELECTED: fill = Theme.GREEN; line = Theme.GREEN.darker(); break;
                case TAKEN:    fill = new Color(0xE3E7EE); line = new Color(0xD1D6DF); break;
                default:       fill = business ? new Color(0xE3EDFF) : Color.WHITE; line = business ? new Color(0x9DB8F5) : Theme.BORDER;
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.setColor(line);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
