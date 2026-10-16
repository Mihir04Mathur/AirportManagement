package airport.ui;

import airport.util.FormatUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date + time picker. Clicking the field or the calendar button opens a month-grid calendar
 * (CalendarDialog) so the date is picked, not typed. Keeps the same public API as before
 * (getText / setText / reset) so the rest of the app does not need to change.
 */
public class DateTimeSpinner extends JPanel {
    private static final DateTimeFormatter SHOWN = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final int defaultHoursAhead;
    private LocalDateTime value;
    private final JLabel display = new JLabel();

    public DateTimeSpinner(int defaultHoursAhead) {
        this.defaultHoursAhead = defaultHoursAhead;
        setLayout(new BorderLayout(6, 0));
        setOpaque(false);

        display.setFont(Theme.font(Font.PLAIN, 12));
        display.setForeground(Theme.TEXT);
        display.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER, 1, true), new EmptyBorder(6, 9, 6, 9)));
        display.setOpaque(true);
        display.setBackground(Color.WHITE);
        display.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        AppButton pick = new AppButton("📅", Theme.BLUE);
        pick.setPreferredSize(new Dimension(36, 34));
        pick.setBorder(new EmptyBorder(4, 6, 4, 6));
        pick.setFont(Theme.font(Font.PLAIN, 14));

        java.awt.event.ActionListener open = e -> openPicker();
        pick.addActionListener(open);
        display.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { openPicker(); }
        });

        display.setPreferredSize(new Dimension(130, 34));
        add(display, BorderLayout.CENTER);
        add(pick, BorderLayout.EAST);
        setPreferredSize(new Dimension(172, 34));
        reset();
    }

    private void openPicker() {
        LocalDateTime chosen = CalendarDialog.pick(this, value);
        if (chosen != null) { value = chosen; refresh(); }
    }

    public void reset() {
        value = LocalDateTime.now().plusHours(defaultHoursAhead).withMinute(0).withSecond(0).withNano(0);
        refresh();
    }

    /** Value as "yyyy-MM-dd HH:mm". */
    public String getText() {
        return value.format(FormatUtil.DATE_TIME);
    }

    public void setText(String text) {
        LocalDateTime t = FormatUtil.parse(text);
        value = t != null ? t : LocalDateTime.now().plusHours(defaultHoursAhead);
        refresh();
    }

    private void refresh() {
        display.setText(value.format(SHOWN));
    }
}
