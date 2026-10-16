package airport.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Flat rounded button with hover effect. */
public class AppButton extends JButton {
    private final Color base;
    private boolean hover;

    public AppButton(String text, Color base) { this(text, base, Color.WHITE); }

    public AppButton(String text, Color base, Color textColor) {
        super(text);
        this.base = base;
        setForeground(textColor);
        setFont(Theme.font(Font.BOLD, 13));
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setBorder(new EmptyBorder(9, 18, 9, 18));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
        });
    }

    /** Light grey secondary button. */
    public static AppButton secondary(String text) {
        return new AppButton(text, new Color(0xE6EBF4), Theme.TEXT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color c = base;
        if (!isEnabled()) c = new Color(0xD5DBE6);
        else if (getModel().isPressed()) c = base.darker().darker();
        else if (hover) c = base.darker();
        g2.setColor(c);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.dispose();
        super.paintComponent(g);
    }
}
