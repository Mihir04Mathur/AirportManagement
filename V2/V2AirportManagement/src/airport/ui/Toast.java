package airport.ui;

import javax.swing.*;
import java.awt.*;

/** Small non-blocking message that appears at the bottom of the window and fades away by itself. */
public final class Toast {
    private static final String KEY = "airport.toast";

    private Toast() { }

    public static void success(Component anchor, String msg) { show(anchor, msg, Theme.GREEN); }
    public static void error(Component anchor, String msg)   { show(anchor, msg, Theme.RED); }
    public static void info(Component anchor, String msg)    { show(anchor, msg, Theme.NAVY); }

    private static void show(Component anchor, String msg, Color color) {
        JRootPane root = anchor == null ? null : SwingUtilities.getRootPane(anchor);
        if (root == null || msg == null) return;                     // e.g. screen not on a window yet
        JLayeredPane layer = root.getLayeredPane();
        Object old = layer.getClientProperty(KEY);
        if (old instanceof Component) layer.remove((Component) old);

        ToastView view = new ToastView(msg, color);
        Dimension d = view.getPreferredSize();
        view.setBounds((layer.getWidth() - d.width) / 2, layer.getHeight() - d.height - 52, d.width, d.height);
        layer.add(view, JLayeredPane.POPUP_LAYER);
        layer.putClientProperty(KEY, view);
        layer.repaint();

        Timer t = new Timer(3000, e -> {
            layer.remove(view);
            if (layer.getClientProperty(KEY) == view) layer.putClientProperty(KEY, null);
            layer.repaint();
        });
        t.setRepeats(false);
        t.start();
    }

    private static class ToastView extends JComponent {
        private final String text;
        private final Color color;

        ToastView(String text, Color color) {
            this.text = text; this.color = color;
            setFont(Theme.font(Font.BOLD, 14));
            FontMetrics fm = getFontMetrics(getFont());
            setPreferredSize(new Dimension(Math.min(fm.stringWidth(text) + 64, 760), 46));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 24, 24);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 24, 24);
            g2.setColor(Color.WHITE);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            String shown = text;
            while (shown.length() > 4 && fm.stringWidth(shown) > getWidth() - 40) shown = shown.substring(0, shown.length() - 2) + "…";
            g2.drawString(shown, (getWidth() - 4 - fm.stringWidth(shown)) / 2, (getHeight() - 4 - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
