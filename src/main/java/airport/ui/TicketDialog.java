package airport.ui;

import airport.exception.ExportException;
import airport.model.Booking;
import airport.model.Flight;
import airport.util.AppLogger;
import airport.util.CsvExporter;
import airport.util.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/** A drawn boarding pass / e-ticket with a tear-off stub and barcode. */
public final class TicketDialog {
    private TicketDialog() { }

    public static void show(Component parent, Booking b, Flight f, String gate, String extra) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(owner, gate == null ? "E-ticket" : "Boarding pass", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(10, 14, 14, 14));
        root.add(new Canvas(b, f, gate), BorderLayout.CENTER);

        AppButton save = new AppButton("Save as file…", Theme.BLUE);
        AppButton close = AppButton.secondary("Close");
        close.addActionListener(e -> d.dispose());
        save.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(new File("exports"));
            fc.setSelectedFile(new File("ticket_" + b.getId() + ".txt"));
            if (fc.showSaveDialog(d) != JFileChooser.APPROVE_OPTION) return;
            try {
                Path p = CsvExporter.writeText(Tickets.buildText(b, f, gate, extra), fc.getSelectedFile().toPath());
                AppLogger.info("Ticket #" + b.getId() + " saved to " + p.toAbsolutePath());
                Toast.success(d.getRootPane(), "Ticket saved: " + p.getFileName());
            } catch (ExportException ex) {
                Theme.error(d, ex.getMessage());
            }
        });
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.add(save); btns.add(close);
        root.add(btns, BorderLayout.SOUTH);

        d.setContentPane(root);
        d.setResizable(false);
        d.pack();
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
    }

    private static class Canvas extends JComponent {
        private final Booking b;
        private final Flight f;
        private final String gate;

        Canvas(Booking b, Flight f, String gate) {
            this.b = b; this.f = f; this.gate = gate;
            setPreferredSize(new Dimension(660, 310));
        }

        private String when(String text) {
            LocalDateTime t = FormatUtil.parse(text);
            return t == null ? "-" : t.format(DateTimeFormatter.ofPattern("dd MMM, HH:mm"));
        }

        private void label(Graphics2D g, String text, int x, int y) {
            g.setColor(Theme.MUTED);
            g.setFont(Theme.font(Font.BOLD, 10));
            g.drawString(text, x, y);
        }

        private void value(Graphics2D g, String text, int x, int y, int size, int maxWidth) {
            g.setColor(Theme.NAVY);
            int s = size;
            Font font = Theme.font(Font.BOLD, s);
            while (s > 10 && g.getFontMetrics(font).stringWidth(text) > maxWidth) font = Theme.font(Font.BOLD, --s);
            g.setFont(font);
            g.drawString(text, x, y);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int x = 14, y = 12, w = 632, h = 282, stubX = x + w - 180;
            g.setColor(new Color(0, 0, 0, 28));
            g.fillRoundRect(x + 3, y + 5, w, h, 26, 26);
            Shape card = new RoundRectangle2D.Float(x, y, w, h, 26, 26);
            g.setColor(Color.WHITE);
            g.fill(card);
            Shape old = g.getClip();
            g.setClip(card);
            g.setColor(Theme.NAVY);
            g.fillRect(x, y, w, 58);
            g.setColor(gate == null ? Theme.BLUE : Theme.GREEN);
            g.fillRect(stubX, y, x + w - stubX, 58);
            g.setClip(old);
            g.setColor(Theme.BORDER);
            g.setStroke(new BasicStroke(1.5f));
            g.draw(card);

            g.setColor(Color.WHITE);
            g.setFont(Theme.font(Font.BOLD, 18));
            g.drawString("✈  AIRPORT MANAGEMENT", x + 24, y + 36);
            String title = gate == null ? "E-TICKET" : "BOARDING PASS";
            g.setFont(Theme.font(Font.BOLD, 14));
            int tw = g.getFontMetrics().stringWidth(title);
            g.drawString(title, stubX + (x + w - stubX - tw) / 2, y + 36);

            // perforation + notches
            g.setColor(Theme.BORDER);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{6f, 6f}, 0f));
            g.drawLine(stubX, y + 62, stubX, y + h - 6);
            g.setColor(Color.WHITE);
            g.fillOval(stubX - 10, y - 10, 20, 20);
            g.fillOval(stubX - 10, y + h - 10, 20, 20);

            // ---- main part ----
            String from = f != null ? f.getOrigin() : String.valueOf(b.getRoute()).split(" → ")[0];
            String to = f != null ? f.getDestination() : (String.valueOf(b.getRoute()).split(" → ").length > 1 ? b.getRoute().split(" → ")[1] : "");
            label(g, "PASSENGER", x + 26, y + 90);
            value(g, String.valueOf(b.getPassengerName()), x + 26, y + 114, 18, 380);
            label(g, "FROM", x + 26, y + 148);
            value(g, from, x + 26, y + 180, 26, 160);
            g.setColor(Theme.BLUE);
            g.setFont(Theme.font(Font.BOLD, 24));
            g.drawString("✈", x + 196, y + 178);
            label(g, "TO", x + 250, y + 148);
            value(g, to, x + 250, y + 180, 26, 170);

            label(g, "FLIGHT", x + 26, y + 222);
            value(g, String.valueOf(b.getFlightNo()), x + 26, y + 246, 16, 90);
            label(g, "DEPARTS", x + 112, y + 222);
            value(g, f == null ? "-" : when(f.getDepartureTime()), x + 112, y + 246, 16, 112);
            label(g, "ARRIVES", x + 236, y + 222);
            value(g, f == null ? "-" : when(f.getArrivalTime()), x + 236, y + 246, 16, 112);
            label(g, gate != null ? "GATE" : "FARE", x + 360, y + 222);
            value(g, gate != null ? gate : FormatUtil.money(b.getFare()), x + 360, y + 246, 16, 88);

            // ---- stub ----
            int sx = stubX + 24;
            label(g, "SEAT", sx, y + 90);
            value(g, String.valueOf(b.getSeatNo()), sx, y + 136, 40, 120);
            label(g, "CLASS", sx, y + 164);
            value(g, FormatUtil.pretty(b.getTravelClass()), sx, y + 184, 15, 120);
            label(g, "BOOKING", sx, y + 208);
            value(g, "#" + b.getId(), sx, y + 228, 15, 120);

            g.setColor(Theme.NAVY);
            Random rnd = new Random(b.getId() * 7919L + 13);
            int px = sx;
            while (px < sx + 128) {
                int bw = 1 + rnd.nextInt(3);
                g.fillRect(px, y + 240, bw, 30);
                px += bw + 1 + rnd.nextInt(3);
            }
            g.dispose();
        }
    }
}
