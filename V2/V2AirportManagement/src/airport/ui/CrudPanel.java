package airport.ui;

import airport.exception.AirportException;
import airport.exception.ValidationException;
import airport.util.AppLogger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Reusable "table + form + Add/Update/Delete" screen. Subclasses supply the details. */
public abstract class CrudPanel extends BasePanel {
    protected final DefaultTableModel model;
    protected final JTable table;
    protected final JTextField keyword = Theme.field(18);
    protected final JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final JLabel formTitle = Theme.heading("Details");
    private final JLabel countLabel = new JLabel();

    protected CrudPanel(String title, String subtitle, String[] columns, Runnable goHome) {
        super(title, subtitle, goHome);
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = Theme.table(model);
    }

    /** Must be called at the end of every subclass constructor. */
    protected final void init() {
        // ----- left: search + table -----
        JPanel left = Theme.card();
        left.setLayout(new BorderLayout(0, 12));
        searchBar.setOpaque(false);
        buildSearchBar(searchBar);
        countLabel.setForeground(Theme.MUTED);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(searchBar, BorderLayout.WEST);
        top.add(countLabel, BorderLayout.EAST);
        left.add(top, BorderLayout.NORTH);
        left.add(Theme.scroll(table), BorderLayout.CENTER);

        // ----- right: form -----
        FormBuilder fb = new FormBuilder();
        buildForm(fb);
        JScrollPane formScroll = new JScrollPane(fb.build());
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);

        AppButton add = new AppButton("Add", Theme.GREEN);
        AppButton upd = new AppButton("Update", Theme.BLUE);
        AppButton del = new AppButton("Delete", Theme.RED);
        AppButton clr = AppButton.secondary("Clear");
        add.addActionListener(e -> act(this::add, "Added successfully.", true));
        upd.addActionListener(e -> {
            if (selectedId() < 0) { Theme.error(this, "Select a row in the table first."); return; }
            act(() -> update(selectedId()), "Updated successfully.", false);
        });
        del.addActionListener(e -> {
            if (selectedId() < 0) { Theme.error(this, "Select a row in the table first."); return; }
            if (Theme.confirm(this, "Delete the selected record?")) act(() -> delete(selectedId()), "Deleted.", true);
        });
        clr.addActionListener(e -> { table.clearSelection(); clearForm(); });
        JPanel buttons = new JPanel(new GridLayout(0, 2, 8, 8));
        buttons.setOpaque(false);
        buttons.add(add); buttons.add(upd); buttons.add(del); buttons.add(clr);
        addExtraButtons(buttons);

        JPanel right = Theme.card();
        right.setLayout(new BorderLayout(0, 12));
        right.setPreferredSize(new Dimension(335, 100));
        right.add(formTitle, BorderLayout.NORTH);
        right.add(formScroll, BorderLayout.CENTER);
        right.add(buttons, BorderLayout.SOUTH);

        content.add(left, BorderLayout.CENTER);
        content.add(right, BorderLayout.EAST);

        setWidths();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) fillForm(table.convertRowIndexToModel(table.getSelectedRow()));
        });
    }

    /** Default search bar (keyword). Flights override this. */
    protected void buildSearchBar(JPanel bar) {
        keyword.addActionListener(e -> refresh());
        AppButton search = new AppButton("Search", Theme.BLUE);
        AppButton reset = AppButton.secondary("Show all");
        search.addActionListener(e -> refresh());
        reset.addActionListener(e -> { clearSearch(); refresh(); });
        bar.add(keyword); bar.add(search); bar.add(reset);
    }

    protected void clearSearch() { keyword.setText(""); }

    protected int selectedId() {
        int r = table.getSelectedRow();
        return r < 0 ? -1 : (Integer) model.getValueAt(table.convertRowIndexToModel(r), 0);   // table may be sorted
    }

    protected void setFormTitle(String t) { formTitle.setText(t); }

    /** Reloads the table from the database. */
    public void refresh() {
        try {
            List<Object[]> rows = fetchRows();
            model.setRowCount(0);
            for (Object[] r : rows) model.addRow(r);
            countLabel.setText(rows.size() + " record(s)");
        } catch (AirportException e) {
            Theme.error(this, e.getMessage());
        }
    }

    @Override public void onShow() { beforeShow(); refresh(); }

    /** Hook: subclasses may add more buttons under Add / Update / Delete / Clear. */
    protected void addExtraButtons(JPanel buttons) { }

    /** Hook: subclasses set column widths here. */
    protected void setWidths() { }

    /** Hook: reload things like combo boxes. */
    protected void beforeShow() { }

    private interface Action { void run() throws AirportException; }

    private void act(Action a, String okMsg, boolean clearAfter) {
        try {
            a.run();
            refresh();
            if (clearAfter) { table.clearSelection(); clearForm(); }
            AppLogger.info(getClass().getSimpleName() + ": " + okMsg);
            Theme.success(this, okMsg);
        } catch (ValidationException e) {            // the user typed something wrong
            AppLogger.debug(getClass().getSimpleName() + ": invalid input - " + e.getMessage());
            Theme.error(this, e.getMessage());
        } catch (AirportException e) {               // any other expected application error
            AppLogger.warn(getClass().getSimpleName() + ": " + e.getMessage());
            Theme.error(this, e.getMessage());
        } catch (RuntimeException e) {               // unexpected bug: log it, never crash the window
            AppLogger.error(getClass().getSimpleName() + ": unexpected error", e);
            Theme.error(this, "Unexpected error: " + e);
        }
    }

    protected abstract List<Object[]> fetchRows() throws AirportException;
    protected abstract void buildForm(FormBuilder fb);
    protected abstract void fillForm(int row);
    protected abstract void clearForm();
    protected abstract void add() throws AirportException;
    protected abstract void update(int id) throws AirportException;
    protected abstract void delete(int id) throws AirportException;
}
