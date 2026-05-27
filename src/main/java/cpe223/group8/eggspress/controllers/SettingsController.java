package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.config.DatabaseConfig;
import cpe223.group8.eggspress.services.PreferencesManager;
import cpe223.group8.eggspress.services.ThemeManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller to handle application settings, database diagnostics,
 * and user preferences persistence.
 */
public class SettingsController {

    @FXML
    private ComboBox<String> themeSelector;

    @FXML
    private CheckBox enableToastsCheckbox;

    @FXML
    private ComboBox<String> severityFilterSelector;

    @FXML
    private CheckBox developerModeCheckbox;

    @FXML
    private TextField dbPathField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button testConnectionBtn;

    @FXML
    private Button resetDbBtn;

    @FXML
    public void initialize() {
        // Initialize dropdown items
        themeSelector.getItems().addAll("Light Mode", "Dark Mode");
        severityFilterSelector.getItems().addAll("INFO", "WARNING", "CRITICAL");

        // Load cached configurations from preferences manager
        String activeTheme = PreferencesManager.getStartingTheme();
        themeSelector.setValue("DARK".equalsIgnoreCase(activeTheme) ? "Dark Mode" : "Light Mode");

        enableToastsCheckbox.setSelected(PreferencesManager.isPushToastsEnabled());
        
        String filterLevel = PreferencesManager.getToastPriorityFilter();
        severityFilterSelector.setValue(filterLevel);

        developerModeCheckbox.setSelected(PreferencesManager.isDeveloperMode());

        // Display SQLite database connection URL path
        String homePath = System.getProperty("user.home");
        dbPathField.setText(homePath + "/eggspress.db");

        // Install tooltips on action elements
        cpe223.group8.eggspress.services.TooltipHelper.installTooltip(testConnectionBtn, "Test connectivity to local SQLite database");
        cpe223.group8.eggspress.services.TooltipHelper.installTooltip(resetDbBtn, "Reset and reseed database tables to fresh startup state");
    }

    @FXML
    private void handleTestConnection() {
        statusLabel.setVisible(true);
        statusLabel.getStyleClass().removeAll("feedback-error", "feedback-message");
        statusLabel.setStyle(""); // Clear any old inline styles

        boolean success = DatabaseConfig.testConnection();
        if (success) {
            statusLabel.setText("Database connection tested successfully. Status: Optimal.");
            statusLabel.getStyleClass().add("feedback-message");
        } else {
            statusLabel.setText("Failed to establish active database connection. Status: Offline.");
            statusLabel.getStyleClass().add("feedback-error");
        }
    }

    @FXML
    private void handleResetDatabase() {
        statusLabel.setVisible(true);
        statusLabel.getStyleClass().removeAll("feedback-error", "feedback-message");
        statusLabel.setStyle(""); // Clear any old inline styles

        try {
            DatabaseConfig.resetDatabase();
            statusLabel.setText("Database tables successfully reset and reseeded.");
            statusLabel.getStyleClass().add("feedback-message");
        } catch (Exception e) {
            statusLabel.setText("Failed to reset database: " + e.getMessage());
            statusLabel.getStyleClass().add("feedback-error");
        }
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    @FXML
    private void handleSave() {
        // Save values to properties cache
        String selectedTheme = themeSelector.getValue();
        boolean isDark = "Dark Mode".equals(selectedTheme);
        PreferencesManager.setStartingTheme(isDark ? "DARK" : "LIGHT");

        PreferencesManager.setPushToastsEnabled(enableToastsCheckbox.isSelected());
        PreferencesManager.setToastPriorityFilter(severityFilterSelector.getValue());

        boolean isDevMode = developerModeCheckbox.isSelected();
        PreferencesManager.setDeveloperMode(isDevMode);

        PreferencesManager.saveSettings();

        // Dynamically apply visual theme changes
        ThemeManager.setDarkMode(isDark);
        
        DashboardController dashboard = DashboardController.getInstance();
        if (dashboard != null) {
            dashboard.applyVisualThemeChange();
            dashboard.toggleDeveloperModeSidebarButton(isDevMode);
        }

        closeStage();
    }

    private void closeStage() {
        if (dbPathField != null && dbPathField.getScene() != null) {
            Stage stage = (Stage) dbPathField.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }
}
