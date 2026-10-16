package airport.ui;

import airport.exception.AirportException;
import airport.exception.AuthenticationException;
import airport.exception.ValidationException;
import airport.service.AuthService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Arrays;

/** Sign-in screen shown first. */
public class LoginPanel extends JPanel {
    private final AuthService auth;
    private final Runnable onSuccess;
    private final JTextField user = Theme.field(18);
    private final JPasswordField pass = new JPasswordField(18);
    private final JLabel error = new JLabel(" ");

    public LoginPanel(AuthService auth, Runnable onSuccess) {
        this.auth = auth;
        this.onSuccess = onSuccess;
        setLayout(new GridBagLayout());

        Theme.hint(user, "admin or staff");
        pass.setFont(Theme.font(Font.PLAIN, 13));
        pass.setEchoChar('•');
        pass.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER, 1, true), new EmptyBorder(6, 9, 6, 9)));

        JLabel logo = new JLabel("✈", SwingConstants.CENTER);
        logo.setOpaque(true);
        logo.setBackground(Theme.BLUE);
        logo.setForeground(Color.WHITE);
        logo.setFont(Theme.font(Font.BOLD, 30));
        logo.setPreferredSize(new Dimension(64, 64));
        JPanel logoWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoWrap.setOpaque(false);
        logoWrap.add(logo);

        JLabel title = new JLabel("Airport Management", SwingConstants.CENTER);
        title.setFont(Theme.font(Font.BOLD, 22));
        title.setForeground(Theme.NAVY);
        JLabel sub = new JLabel("Sign in to continue", SwingConstants.CENTER);
        sub.setForeground(Theme.MUTED);

        JCheckBox show = new JCheckBox("Show password");
        show.setOpaque(false);
        show.setFont(Theme.font(Font.PLAIN, 12));
        show.addActionListener(e -> pass.setEchoChar(show.isSelected() ? (char) 0 : '•'));

        error.setForeground(Theme.RED);
        error.setFont(Theme.font(Font.BOLD, 12));
        error.setHorizontalAlignment(SwingConstants.CENTER);

        AppButton signIn = new AppButton("Sign in", Theme.BLUE);
        signIn.setFont(Theme.font(Font.BOLD, 15));
        signIn.addActionListener(e -> doLogin());
        user.addActionListener(e -> pass.requestFocusInWindow());
        pass.addActionListener(e -> doLogin());

        JLabel demo = new JLabel("<html><div style='text-align:center'>Demo accounts<br>admin / admin123 &nbsp;•&nbsp; staff / staff123</div></html>", SwingConstants.CENTER);
        demo.setFont(Theme.font(Font.PLAIN, 11));
        demo.setForeground(Theme.MUTED);

        FormBuilder fb = new FormBuilder();
        fb.add("Username", user);
        fb.add("Password", pass);
        JPanel showWrap = new JPanel(new BorderLayout());
        showWrap.setOpaque(false);
        showWrap.add(show, BorderLayout.WEST);
        fb.addGroup(showWrap);

        JPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(400, 500));
        for (JComponent c : new JComponent[]{logoWrap, title, sub, error, signIn, demo}) c.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel form = fb.build();
        form.setAlignmentX(Component.CENTER_ALIGNMENT);
        signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        error.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        card.add(logoWrap);
        card.add(Box.createVerticalStrut(12));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(22));
        card.add(form);
        card.add(error);
        card.add(Box.createVerticalStrut(8));
        card.add(signIn);
        card.add(Box.createVerticalStrut(14));
        card.add(demo);
        add(card);
    }

    public void onShow() {
        user.setText("");
        pass.setText("");
        error.setText(" ");
        SwingUtilities.invokeLater(user::requestFocusInWindow);
    }

    private void doLogin() {
        char[] pw = pass.getPassword();
        try {
            auth.login(user.getText(), new String(pw));
            error.setText(" ");
            onSuccess.run();
        } catch (AuthenticationException | ValidationException e) {
            error.setText(e.getMessage());
            pass.setText("");
            pass.requestFocusInWindow();
        } catch (AirportException e) {
            error.setText("Sign-in is unavailable: " + e.getMessage());
        } finally {
            Arrays.fill(pw, '\0');            // do not keep the password in memory longer than needed
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, Theme.NAVY, getWidth(), getHeight(), new Color(0x1E40AF)));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(new Color(255, 255, 255, 14));
        g2.fillOval(-120, getHeight() - 260, 480, 480);
        g2.fillOval(getWidth() - 340, -160, 520, 520);
        g2.setFont(Theme.font(Font.BOLD, 220));
        g2.setColor(new Color(255, 255, 255, 18));
        g2.drawString("✈", getWidth() - 330, getHeight() - 40);
        g2.dispose();
    }
}
