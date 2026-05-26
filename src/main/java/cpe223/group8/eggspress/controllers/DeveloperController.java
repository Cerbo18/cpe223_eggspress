package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.config.DatabaseConfig;
import cpe223.group8.eggspress.services.NotificationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller to handle mock telemetry, diagnostic database state queries,
 * and console log streaming.
 */
public class DeveloperController {

    @FXML
    private Label usersCountLabel;

    @FXML
    private Label inventoryCountLabel;

    @FXML
    private Label coopsCountLabel;

    @FXML
    private Label alertsCountLabel;

    @FXML
    private TextArea consoleArea;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    public void initialize() {
        // Log starting diagnostic session
        log("Diagnostic console initialized successfully.");
        log("Connected to SQLite database at ~/eggspress.db");
        
        handleRefreshCounts();
    }

    private void log(String message) {
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        Platform.runLater(() -> {
            if (consoleArea != null) {
                consoleArea.appendText("[" + timestamp + "] " + message + "\n");
            }
        });
    }

    @FXML
    private void handleRefreshCounts() {
        log("Querying database table counts...");
        
        int usersCount = 0;
        int inventoryCount = 0;
        int coopsCount = 0;
        int alertsCount = 0;

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next()) usersCount = rs.getInt(1);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM inventory")) {
                if (rs.next()) inventoryCount = rs.getInt(1);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM coops")) {
                if (rs.next()) coopsCount = rs.getInt(1);
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM notifications")) {
                if (rs.next()) alertsCount = rs.getInt(1);
            }

            final int u = usersCount;
            final int i = inventoryCount;
            final int c = coopsCount;
            final int a = alertsCount;

            Platform.runLater(() -> {
                usersCountLabel.setText(String.valueOf(u));
                inventoryCountLabel.setText(String.valueOf(i));
                coopsCountLabel.setText(String.valueOf(c));
                alertsCountLabel.setText(String.valueOf(a));
            });

            log("Summary loaded: users=" + u + ", inventory=" + i + ", coops=" + c + ", alerts=" + a);
        } catch (SQLException e) {
            log("CRITICAL error refreshing record counts: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearTerminal() {
        if (consoleArea != null) {
            consoleArea.clear();
        }
        log("Terminal logs cleared.");
    }

    @FXML
    private void handleInjectMockStocks() {
        log("Injecting mock stock telemetry quantities into inventory database...");
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Add 100 kg to Grains stock and 200 L to Water stock
            stmt.execute("UPDATE inventory SET quantity = quantity + 100 WHERE id = 'INV001';");
            stmt.execute("UPDATE inventory SET quantity = quantity + 200 WHERE id = 'INV002';");
            
            log("Telemetry successfully injected: INV001 (+100kg), INV002 (+200L).");
            NotificationService.notificationInfo("Developer Telemetry: Stock quantity increments successfully processed.");
            
            handleRefreshCounts();
        } catch (SQLException e) {
            log("Error injecting mock stocks: " + e.getMessage());
        }
    }

    @FXML
    private void handleInjectMockAlerts() {
        log("Injecting simulated critical alarm alerts into repository feed...");
        
        NotificationService.notificationWarning("Diagnostic Poller: High ammonia concentration detected in Coop B.");
        NotificationService.notificationCritical("Emergency Poller: Water system flow failure detected in breeder facility!");
        
        log("Triggered Info/Warning simulated alarms.");
        handleRefreshCounts();
    }

    @FXML
    private void handleInjectMockSchedules() {
        log("Injecting simulated schedule feeding task...");
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            String insertSql = "INSERT INTO schedules (category, time, feeding_type, status) VALUES " +
                               "('Broiler', '16:00', 'Mixed Grain', 'Pending');";
            stmt.execute(insertSql);
            
            log("Mock schedule successfully appended to database records.");
            NotificationService.notificationInfo("Developer Telemetry: New feeding schedule seeded.");
            
            handleRefreshCounts();
        } catch (SQLException e) {
            log("Error seeding mock schedule: " + e.getMessage());
        }
    }

    @FXML
    private void handleSimulateInfo() {
        log("Simulating Info Notification: Daily feed log generated...");
        NotificationService.notificationInfo("Simulated system info: Daily feed log generated.");
        handleRefreshCounts();
    }

    @FXML
    private void handleSimulateWarning() {
        log("Simulating Warning Notification: Low water tank pressure...");
        NotificationService.notificationWarning("Simulated system alert: Low water tank pressure.");
        handleRefreshCounts();
    }

    @FXML
    private void handleSimulateCritical() {
        log("Simulating Critical Notification: Coop A temperature exceeded 35°C...");
        NotificationService.notificationCritical("Simulated emergency: Coop A temperature exceeded 35°C!");
        handleRefreshCounts();
    }

    @FXML
    private void handleSimulateLowInfo() {
        log("Simulating Low Info Notification: Non-blocking log created...");
        NotificationService.notificationInfo("Simulated low info: Non-blocking log created.", false, 2);
        handleRefreshCounts();
    }

    @FXML
    private void handleSimulateLowWarning() {
        log("Simulating Low Warning Notification: Screen refreshed...");
        NotificationService.notificationWarning("Simulated low warning: Screen refreshed.", false, 2);
        handleRefreshCounts();
    }
}

