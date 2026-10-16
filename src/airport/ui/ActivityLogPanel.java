package airport.ui;

import airport.util.AppLogger;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/** Shows logs/airport.log - every booking, check-in, error and export is recorded here. */
public class ActivityLogPanel extends BasePanel {
    private final JTextArea area = new JTextArea();
    private final JLabel info = new JLabel();

    public ActivityLogPanel(Runnable goHome) {
        super("Activity Log", "Everything the system did, written with java.util.logging", goHome);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setMargin(new Insets(10, 12, 10, 12));

        AppButton refresh = new AppButton("Refresh", Theme.BLUE);
        AppButton clear = new AppButton("Clear log", Theme.RED);
        refresh.addActionListener(e -> onShow());
        clear.addActionListener(e -> {
            if (!Theme.confirm(this, "Delete all log entries?")) return;
            try {
                AppLogger.clear();
                AppLogger.info("Log cleared by user");
                onShow();
                Toast.success(this, "Log cleared");
            } catch (IOException ex) {
                Theme.error(this, "Could not clear the log: " + ex.getMessage());
            }
        });
        info.setForeground(Theme.MUTED);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(refresh); buttons.add(clear);
        top.add(buttons, BorderLayout.WEST);
        top.add(info, BorderLayout.EAST);

        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 12));
        card.add(top, BorderLayout.NORTH);
        card.add(Theme.scroll(area), BorderLayout.CENTER);
        content.add(card, BorderLayout.CENTER);
    }

    @Override
    public void onShow() {
        List<String> lines = AppLogger.readAll();
        area.setText(String.join("\n", lines));
        area.setCaretPosition(area.getDocument().getLength());      // jump to the newest entry
        info.setText(lines.size() + " entries  |  " + AppLogger.LOG_FILE);
    }
}
