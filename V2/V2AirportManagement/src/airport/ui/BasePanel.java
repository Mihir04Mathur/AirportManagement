package airport.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Common screen layout: navy header with a Home button + content area. */
public abstract class BasePanel extends JPanel {
    protected final JPanel content = new JPanel(new BorderLayout(16, 16));

    protected BasePanel(String title, String subtitle, Runnable goHome) {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setBackground(Theme.NAVY);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        AppButton home = new AppButton("←  Home", Theme.BLUE);
        home.addActionListener(e -> goHome.run());
        header.add(home, BorderLayout.WEST);

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(Theme.font(Font.BOLD, 22));
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel(subtitle);
        s.setFont(Theme.font(Font.PLAIN, 12));
        s.setForeground(new Color(0xA9BDE0));
        titles.add(t);
        titles.add(s);
        header.add(titles, BorderLayout.CENTER);

        content.setOpaque(false);
        content.setBorder(new EmptyBorder(18, 20, 20, 20));
        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    /** Called every time this screen is shown - reload data here. */
    public void onShow() { }
}
