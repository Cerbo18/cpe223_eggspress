package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.services.NotificationService;
import cpe223.group8.eggspress.repository.UserRepository;
import cpe223.group8.eggspress.repository.FarmRepository;
import cpe223.group8.eggspress.repository.NotificationRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

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
        
        int usersCount = UserRepository.getUsersCount();
        int inventoryCount = FarmRepository.getInventoryCount();
        int coopsCount = FarmRepository.getCoopsCount();
        int alertsCount = NotificationRepository.getNotificationsCount();

        Platform.runLater(() -> {
            usersCountLabel.setText(String.valueOf(usersCount));
            inventoryCountLabel.setText(String.valueOf(inventoryCount));
            coopsCountLabel.setText(String.valueOf(coopsCount));
            alertsCountLabel.setText(String.valueOf(alertsCount));
        });

        log("Summary loaded: users=" + usersCount + ", inventory=" + inventoryCount + ", coops=" + coopsCount + ", alerts=" + alertsCount);
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
        
        boolean success = FarmRepository.injectDeveloperMockStocks();
        if (success) {
            log("Telemetry successfully injected: INV001 (+100kg), INV002 (+200L).");
            NotificationService.notificationInfo("Developer Telemetry: Stock quantity increments successfully processed.");
            handleRefreshCounts();
        } else {
            log("Error injecting mock stocks.");
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
        
        boolean success = FarmRepository.injectDeveloperMockSchedule();
        if (success) {
            log("Mock schedule successfully appended to database records.");
            NotificationService.notificationInfo("Developer Telemetry: New feeding schedule seeded.");
            handleRefreshCounts();
        } else {
            log("Error seeding mock schedule.");
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
