package airport.ui;

import airport.exception.AirportException;
import airport.exception.ExportException;
import airport.service.ReportService;
import airport.util.AppLogger;
import airport.util.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/** Reports & Export: statistics (Streams / Collections) and CSV / text export (java.io). */
public class ReportsPanel extends BasePanel {
    private final ReportService service = new ReportService();

    private final JPanel kpiGrid = new JPanel(new GridLayout(3, 3, 14, 14));
    private final DefaultTableModel flightModel = readOnly("Flight", "Route", "Departure", "Duration", "Type", "Booked", "Occupancy", "Revenue");
    private final DefaultTableModel breakModel = readOnly("Category", "Item", "Count");
    private final JComboBox<String> sortBox = Theme.combo();

    public ReportsPanel(Runnable goHome) {
        super("Reports & Export", "Statistics, sorted flight reports and CSV / text export", goHome);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.font(Font.BOLD, 13));
        tabs.setBackground(Color.WHITE);

        // ----- Overview -----
        kpiGrid.setOpaque(false);
        JPanel overview = Theme.card();
        overview.setLayout(new BorderLayout(0, 14));
        overview.add(Theme.heading("Key numbers"), BorderLayout.NORTH);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(kpiGrid, BorderLayout.NORTH);
        overview.add(top, BorderLayout.CENTER);
        tabs.addTab("  Overview  ", overview);

        // ----- Flights -----
        for (String s : ReportService.SORT_OPTIONS) sortBox.addItem(s);
        sortBox.setPreferredSize(new Dimension(230, 32));
        sortBox.addActionListener(e -> loadFlights());
        JTable flightTable = Theme.table(flightModel);
        Theme.widths(flightTable, 70, 190, 130, 80, 100, 80, 90, 110);
        JPanel flights = Theme.card();
        flights.setLayout(new BorderLayout(0, 12));
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);
        bar.add(Theme.label("Sort by")); bar.add(sortBox);
        flights.add(bar, BorderLayout.NORTH);
        flights.add(Theme.scroll(flightTable), BorderLayout.CENTER);
        tabs.addTab("  Flights  ", flights);

        // ----- Breakdowns -----
        JTable breakTable = Theme.table(breakModel);
        Theme.widths(breakTable, 200, 250, 80);
        JPanel breaks = Theme.card();
        breaks.setLayout(new BorderLayout());
        breaks.add(Theme.scroll(breakTable), BorderLayout.CENTER);
        tabs.addTab("  Breakdowns  ", breaks);

        // ----- Export -----
        tabs.addTab("  Export  ", buildExportTab());

        content.add(tabs, BorderLayout.CENTER);
    }

    private static DefaultTableModel readOnly(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    @Override
    public void onShow() {
        try {
            kpiGrid.removeAll();
            for (Map.Entry<String, String> e : service.kpis().entrySet()) kpiGrid.add(tile(e.getKey(), e.getValue()));
            kpiGrid.revalidate(); kpiGrid.repaint();

            breakModel.setRowCount(0);
            for (String[] r : service.breakdown()) breakModel.addRow(new Object[]{r[0], r[1], r[2]});
        } catch (AirportException e) {
            Theme.error(this, e.getMessage());
        }
        loadFlights();
    }

    private void loadFlights() {
        try {
            flightModel.setRowCount(0);
            for (ReportService.FlightStat s : service.flightStats(Math.max(0, sortBox.getSelectedIndex())))
                flightModel.addRow(new Object[]{s.flightNo, s.route, s.departure, s.duration, s.type,
                        s.booked + " / " + s.total, FormatUtil.percent(s.occupancy), FormatUtil.money(s.revenue)});
        } catch (AirportException e) {
            Theme.error(this, e.getMessage());
        }
    }

    private JPanel tile(String label, String value) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 4));
        p.setBackground(Theme.BG);
        p.setBorder(new EmptyBorder(14, 16, 14, 16));
        JLabel v = new JLabel(value);
        v.setFont(Theme.font(Font.BOLD, 20));
        v.setForeground(Theme.NAVY);
        JLabel l = new JLabel(label);
        l.setFont(Theme.font(Font.PLAIN, 12));
        l.setForeground(Theme.MUTED);
        p.add(v); p.add(l);
        return p;
    }

    // ---------------- export tab ----------------
    private interface Exporter { Path run(Path file) throws AirportException; }

    private JPanel buildExportTab() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 14));
        card.add(Theme.heading("Export data to files"), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 14, 14));
        grid.setOpaque(false);
        grid.add(exportButton("Export Passengers (CSV)", Theme.TEAL, "passengers", ".csv", service::exportPassengers));
        grid.add(exportButton("Export Flights (CSV)", Theme.BLUE, "flights", ".csv", service::exportFlights));
        grid.add(exportButton("Export Employees (CSV)", Theme.PURPLE, "employees", ".csv", service::exportEmployees));
        grid.add(exportButton("Export Bookings (CSV)", Theme.GREEN, "bookings", ".csv", service::exportBookings));
        grid.add(exportButton("Export Full Report (TXT)", Theme.ORANGE, "airport_report", ".txt", service::exportTextReport));

        JLabel hint = new JLabel("<html><body style='width:560px'>CSV files open in Excel. Each export asks where to save the file "
                + "and is written to the Activity Log.</body></html>");
        hint.setForeground(Theme.MUTED);
        JPanel wrap = new JPanel(new BorderLayout(0, 14));
        wrap.setOpaque(false);
        wrap.add(grid, BorderLayout.NORTH);
        JPanel hintBox = new JPanel(new BorderLayout());
        hintBox.setOpaque(false);
        hintBox.add(hint, BorderLayout.NORTH);
        wrap.add(hintBox, BorderLayout.CENTER);
        card.add(wrap, BorderLayout.CENTER);
        return card;
    }

    private AppButton exportButton(String text, Color color, String baseName, String ext, Exporter exporter) {
        AppButton b = new AppButton(text, color);
        b.setFont(Theme.font(Font.BOLD, 14));
        b.setBorder(new EmptyBorder(16, 18, 16, 18));
        b.addActionListener(e -> {
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
            JFileChooser fc = new JFileChooser(new File("exports"));
            fc.setSelectedFile(new File(baseName + "_" + stamp + ext));
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            try {
                Path saved = exporter.run(fc.getSelectedFile().toPath());
                Theme.info(this, "Saved successfully:\n" + saved.toAbsolutePath());
            } catch (ExportException ex) {
                AppLogger.warn("Export failed: " + ex.getMessage());
                Theme.error(this, ex.getMessage());
            } catch (AirportException ex) {
                Theme.error(this, ex.getMessage());
            }
        });
        return b;
    }
}
