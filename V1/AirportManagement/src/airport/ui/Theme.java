package airport.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.Arrays;

/** Central place for colours, fonts and reusable styled components. */
public final class Theme {
    public static final Color NAVY   = new Color(0x0B2447);
    public static final Color BLUE   = new Color(0x1E63F0);
    public static final Color BG     = new Color(0xF3F6FB);
    public static final Color TEXT   = new Color(0x1F2937);
    public static final Color MUTED  = new Color(0x6B7280);
    public static final Color BORDER = new Color(0xD9E0EC);
    public static final Color GREEN  = new Color(0x16A34A);
    public static final Color RED    = new Color(0xDC2626);
    public static final Color ORANGE = new Color(0xEA8A0C);
    public static final Color PURPLE = new Color(0x7C3AED);
    public static final Color TEAL   = new Color(0x0E9AA7);
    public static final Color SELECT = new Color(0xDCE8FF);
    public static final Color ROW_ALT = new Color(0xF7F9FD);

    private static final String FAMILY = pickFamily();

    private Theme() { }

    private static String pickFamily() {
        String[] wanted = {"Segoe UI", "SF Pro Text", "Helvetica Neue", "Ubuntu", "Roboto", "Arial"};
        java.util.List<String> have = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String f : wanted) if (have.contains(f)) return f;
        return Font.SANS_SERIF;
    }

    public static Font font(int style, int size) { return new Font(FAMILY, style, size); }

    public static void install() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) { }
        for (String k : new String[]{"Label.font", "Button.font", "TextField.font", "ComboBox.font", "TextArea.font",
                "CheckBox.font", "Table.font", "OptionPane.messageFont", "OptionPane.buttonFont", "PasswordField.font"})
            UIManager.put(k, font(Font.PLAIN, 13));
        UIManager.put("TableHeader.font", font(Font.BOLD, 13));
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("Button.disabledText", new Color(0x9CA3AF));
    }

    // ---------- factory helpers ----------
    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.BOLD, 12));
        l.setForeground(MUTED);
        return l;
    }

    public static JTextField field(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(font(Font.PLAIN, 13));
        f.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(6, 9, 6, 9)));
        return f;
    }

    public static <T> JComboBox<T> combo() {
        JComboBox<T> c = new JComboBox<>();
        c.setFont(font(Font.PLAIN, 13));
        c.setBackground(Color.WHITE);
        c.setPreferredSize(new Dimension(200, 32));
        return c;
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(16, 16, 16, 16)));
        return p;
    }

    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(Font.BOLD, 16));
        l.setForeground(NAVY);
        return l;
    }

    public static JTable table(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(32);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setSelectionBackground(SELECT);
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        t.setDefaultRenderer(Object.class, new RowRenderer());
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(100, 36));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, false, false, r, c);
                l.setBackground(NAVY); l.setForeground(Color.WHITE);
                l.setFont(font(Font.BOLD, 12));
                l.setBorder(new EmptyBorder(0, 10, 0, 10));
                l.setOpaque(true);
                return l;
            }
        });
        return t;
    }

    /** Zebra rows with padding; other renderers can extend it. */
    public static class RowRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, false, r, c);
            l.setBorder(new EmptyBorder(0, 10, 0, 10));
            l.setFont(font(Font.PLAIN, 13));
            if (!sel) { l.setBackground(r % 2 == 0 ? Color.WHITE : ROW_ALT); l.setForeground(TEXT); }
            return l;
        }
    }

    /** Sets preferred column widths (columns shrink/grow together to fit). */
    public static void widths(JTable t, int... w) {
        for (int i = 0; i < w.length && i < t.getColumnCount(); i++) t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(BORDER, 1, true));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    public static String money(double v) { return "₹" + String.format("%,.2f", v); }

    // ---------- dialogs ----------
    public static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Airport Management", JOptionPane.INFORMATION_MESSAGE);
    }
    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Please confirm", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
