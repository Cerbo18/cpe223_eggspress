package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.models.FeedingSchedule;
import cpe223.group8.eggspress.repository.FarmRepository;
import cpe223.group8.eggspress.services.NotificationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

/**
 * Controller class for the createScheduleModal.fxml stage view.
 * Handles validation and creation of new FeedingSchedule database rows.
 */
public class CreateScheduleModalController {

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private ComboBox<String> hourComboBox;

    @FXML
    private ComboBox<String> minuteComboBox;

    @FXML
    private ComboBox<String> feedingTypeComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    public void initialize() {
        // Initialize ComboBox categories
        categoryComboBox.setItems(FXCollections.observableArrayList(
            "Water",
            "Grains",
            "Feed",
            "Others"
        ));
        categoryComboBox.getSelectionModel().selectFirst();

        // Populate Hours dropdown in 24-hour format (00 to 23)
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }
        hourComboBox.setItems(hours);
        hourComboBox.getSelectionModel().select(8); // Default to 08:00

        // Populate Minutes dropdown (00 to 59)
        ObservableList<String> minutes = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i++) {
            minutes.add(String.format("%02d", i));
        }
        minuteComboBox.setItems(minutes);
        minuteComboBox.getSelectionModel().select(0); // Default to XX:00

        // Populate curated Action / Feeding Type dropdown
        feedingTypeComboBox.setItems(FXCollections.observableArrayList(
            "Automated Refill",
            "Standard Broiler Feed",
            "High-Protein Mix",
            "Hydration Top-Up",
            "Morning Feed",
            "Evening Feed",
            "Nutrient Supplement"
        ));
        feedingTypeComboBox.getSelectionModel().selectFirst();

        // Populate Status dropdown options
        statusComboBox.setItems(FXCollections.observableArrayList(
            "Scheduled",
            "Pending",
            "Completed",
            "Failed"
        ));
        statusComboBox.getSelectionModel().selectFirst(); // Default to Scheduled
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    @FXML
    private void handleCreate() {
        String category = categoryComboBox.getValue();
        String hour = hourComboBox.getValue();
        String minute = minuteComboBox.getValue();
        String feedingType = feedingTypeComboBox.getValue();
        String status = statusComboBox.getValue();

        if (category == null || category.isEmpty() ||
            hour == null || hour.isEmpty() ||
            minute == null || minute.isEmpty() ||
            feedingType == null || feedingType.isEmpty() ||
            status == null || status.isEmpty()) {
            showError("Please complete all form fields to add the schedule.");
            return;
        }

        // Standardize time strictly to HH:mm format
        String time = String.format("%s:%s", hour, minute);

        FeedingSchedule newSchedule = new FeedingSchedule(category, time, feedingType, status);
        FarmRepository.addSchedule(newSchedule);

        NotificationService.notificationInfo(String.format("Created feeding schedule: %s at %s.", feedingType, time));
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) categoryComboBox.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void showError(String message) {
        NotificationService.notificationWarning(message, false, 2);
    }
}
