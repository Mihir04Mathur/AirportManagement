package airport;

import airport.db.DBConnection;
import airport.exception.AirportException;
import airport.ui.MainFrame;
import airport.ui.Theme;

import javax.swing.*;

/** Program entry point. */
public class Main {
    public static void main(String[] args) {
        try {
            DBConnection.initialize();          // creates tables + sample data on first run
        } catch (AirportException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Startup error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            new MainFrame().setVisible(true);
        });
    }
}
