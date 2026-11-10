package airport;

import airport.core.AirportContext;
import airport.db.DBConnection;
import airport.exception.AirportException;
import airport.exception.ConfigurationException;
import airport.service.AuthService;
import airport.ui.MainFrame;
import airport.ui.Theme;
import airport.util.AppLogger;

import javax.swing.*;

/** Program entry point. */
public class Main {
    public static void main(String[] args) {
        AppLogger.init();

        // Last line of defence: any exception nobody caught is logged and shown instead of silently killing a thread.
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            AppLogger.error("Uncaught exception in thread " + thread.getName(), error);
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "Something unexpected happened:\n" + error, "Unexpected error", JOptionPane.ERROR_MESSAGE));
        });

        try {
            DBConnection.initialize();                 // creates tables + sample data on first run
            new AuthService().seedDefaultUsers();      // demo accounts (admin / staff) on first run
            AppLogger.info("Application started in " + AirportContext.APP_ROOT);
        } catch (AirportException | ConfigurationException e) {     // multi-catch: checked + unchecked
            AppLogger.error("Startup failed", e);
            JOptionPane.showMessageDialog(null, e.getMessage(), "Startup error", JOptionPane.ERROR_MESSAGE);
            return;
        } finally {
            AppLogger.debug("Startup sequence finished");
        }

        SwingUtilities.invokeLater(() -> {
            Theme.install();
            new MainFrame().setVisible(true);
        });
    }
}
