package airport.ui;

import javax.swing.*;
import java.awt.*;

/** A text field that shows grey placeholder text while it is empty. */
public class HintField extends JTextField {
    private String hint = "";

    public HintField(int columns) { super(columns); }

    public void setHint(String hint) { this.hint = hint == null ? "" : hint; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!getText().isEmpty() || hint.isEmpty()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(new Color(0xA3ACBD));
        g2.setFont(getFont());
        Insets in = getInsets();
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hint, in.left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
