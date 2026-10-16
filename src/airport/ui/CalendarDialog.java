package airport.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * A month-grid calendar plus an hour/minute picker, shown as a modal dialog.
 * Click a day to pick the date; Today and the chosen day are highlighted. No typing needed.
 */
public final class CalendarDialog {
    private CalendarDialog() { }

    /** Shows the picker and returns the chosen date-time, or null if the user cancelled. */
    public static LocalDateTime pick(Component parent, LocalDateTime initial) {
        Picker p = new Picker(SwingUtilities.getWindowAncestor(parent), initial == null ? LocalDateTime.now() : initial);
        p.setVisible(true);                 // modal: blocks until closed
        return p.result;
    }

    private static class Picker extends JDialog {
        private YearMonth shown;
        private LocalDate selectedDate;
        private LocalDateTime result;       // null unless the user confirms

        private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
        private final JPanel grid = new JPanel(new GridLayout(0, 7, 4, 4));
        private final JSpinner hour = new JSpinner(new SpinnerNumberModel(0, 0, 23, 1));
        private final JSpinner minute = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
        private final AppButton ok = new AppButton("Select", Theme.GREEN);

        Picker(Window owner, LocalDateTime initial) {
            super(owner, "Choose date & time", ModalityType.APPLICATION_MODAL);
            this.shown = YearMonth.from(initial);
            this.selectedDate = initial.toLocalDate();

            JPanel root = new JPanel(new BorderLayout(0, 14));
            root.setBackground(Color.WHITE);
            root.setBorder(new EmptyBorder(16, 18, 14, 18));

            // ---- month header with prev/next ----
            AppButton prev = AppButton.secondary("<");
            AppButton next = AppButton.secondary(">");
            prev.setFont(Theme.font(Font.BOLD, 15));
            next.setFont(Theme.font(Font.BOLD, 15));
            prev.setBorder(new EmptyBorder(4, 4, 4, 4));                 // default AppButton padding is too wide for a 38px button
            next.setBorder(new EmptyBorder(4, 4, 4, 4));
            prev.setPreferredSize(new Dimension(38, 34));
            next.setPreferredSize(new Dimension(38, 34));
            prev.addActionListener(e -> { shown = shown.minusMonths(1); renderGrid(); });
            next.addActionListener(e -> { shown = shown.plusMonths(1); renderGrid(); });
            monthLabel.setFont(Theme.font(Font.BOLD, 16));
            monthLabel.setForeground(Theme.NAVY);
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.add(prev, BorderLayout.WEST);
            header.add(monthLabel, BorderLayout.CENTER);
            header.add(next, BorderLayout.EAST);

            // ---- weekday row + day grid ----
            grid.setOpaque(false);
            JPanel gridWrap = new JPanel(new BorderLayout());
            gridWrap.setOpaque(false);
            gridWrap.setPreferredSize(new Dimension(320, 250));
            gridWrap.add(grid, BorderLayout.CENTER);

            // ---- time row ----
            styleSpinner(hour);
            styleSpinner(minute);
            JLabel colon = new JLabel(":");
            colon.setFont(Theme.font(Font.BOLD, 18));
            AppButton now = AppButton.secondary("Now");
            now.addActionListener(e -> {
                LocalTime t = LocalTime.now();
                hour.setValue(t.getHour()); minute.setValue((t.getMinute() / 5) * 5);
            });
            JPanel timeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            timeRow.setOpaque(false);
            JLabel timeLbl = Theme.label("TIME");
            timeRow.add(timeLbl); timeRow.add(hour); timeRow.add(colon); timeRow.add(minute); timeRow.add(now);

            // ---- footer ----
            AppButton cancel = AppButton.secondary("Cancel");
            cancel.addActionListener(e -> dispose());
            ok.addActionListener(e -> {
                result = LocalDateTime.of(selectedDate, LocalTime.of((Integer) hour.getValue(), (Integer) minute.getValue()));
                dispose();
            });
            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            foot.setOpaque(false);
            foot.add(cancel); foot.add(ok);

            root.add(header, BorderLayout.NORTH);
            root.add(gridWrap, BorderLayout.CENTER);
            JPanel south = new JPanel(new BorderLayout(0, 10));
            south.setOpaque(false);
            south.add(timeRow, BorderLayout.NORTH);
            south.add(foot, BorderLayout.SOUTH);
            root.add(south, BorderLayout.SOUTH);

            hour.setValue(initial.getHour());
            minute.setValue(initial.getMinute());
            setContentPane(root);
            renderGrid();
            pack();
            setResizable(false);
            setLocationRelativeTo(owner);
        }

        private void styleSpinner(JSpinner s) {
            s.setFont(Theme.font(Font.BOLD, 16));
            s.setPreferredSize(new Dimension(64, 36));
            s.setEditor(new JSpinner.NumberEditor(s, "00"));              // always two digits, e.g. 03
            JComponent editor = s.getEditor();
            if (editor instanceof JSpinner.DefaultEditor)
                ((JSpinner.DefaultEditor) editor).getTextField().setHorizontalAlignment(JTextField.CENTER);
        }

        private void renderGrid() {
            grid.removeAll();
            monthLabel.setText(shown.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + shown.getYear());

            for (java.time.DayOfWeek d : java.time.DayOfWeek.values()) {
                JLabel l = new JLabel(d.getDisplayName(TextStyle.NARROW, Locale.ENGLISH), SwingConstants.CENTER);
                l.setFont(Theme.font(Font.BOLD, 11));
                l.setForeground(Theme.MUTED);
                grid.add(l);
            }

            LocalDate first = shown.atDay(1);
            int lead = first.getDayOfWeek().getValue() % 7;                 // Monday=1..Sunday=7 -> 0..6, Sunday first column
            lead = (first.getDayOfWeek().getValue() - 1 + 7) % 7;           // Monday-first week
            for (int i = 0; i < lead; i++) grid.add(new JLabel(""));

            LocalDate today = LocalDate.now();
            for (int day = 1; day <= shown.lengthOfMonth(); day++) {
                LocalDate date = shown.atDay(day);
                grid.add(new DayButton(date, date.equals(today), date.equals(selectedDate)));
            }
            grid.revalidate();
            grid.repaint();
        }

        /** One clickable day cell. */
        private class DayButton extends JButton {
            DayButton(LocalDate date, boolean today, boolean selected) {
                super(String.valueOf(date.getDayOfMonth()));
                setFont(Theme.font(selected ? Font.BOLD : Font.PLAIN, 13));
                setFocusPainted(false);
                setContentAreaFilled(false);
                setOpaque(true);
                setBorder(today && !selected ? new LineBorder(Theme.BLUE, 1, true) : new EmptyBorder(1, 1, 1, 1));
                setBackground(selected ? Theme.BLUE : Color.WHITE);
                setForeground(selected ? Color.WHITE : Theme.TEXT);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setPreferredSize(new Dimension(38, 34));
                addActionListener(e -> { selectedDate = date; renderGrid(); });
            }
        }
    }
}
