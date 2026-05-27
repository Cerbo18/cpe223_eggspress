package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.models.FeedingSchedule;
import cpe223.group8.eggspress.repository.FarmRepository;
import cpe223.group8.eggspress.services.NotificationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller class for the editScheduleModal.fxml stage view.
 * Pre-populates fields with selected schedule data and updates the SQLite database.
 */
public class EditScheduleModalController {

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

    private FeedingSchedule oldSchedule;

    @FXML
    public void initialize() {
        // Initialize ComboBox categories
        categoryComboBox.setItems(FXCollections.observableArrayList(
            "Water",
            "Grains",
            "Feed",
            "Others"
        ));

        // Populate Hours dropdown in 24-hour format (00 to 23)
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }
        hourComboBox.setItems(hours);

        // Populate Minutes dropdown (00 to 59)
        ObservableList<String> minutes = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i++) {
            minutes.add(String.format("%02d", i));
        }
        minuteComboBox.setItems(minutes);

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

        // Populate Status dropdown options
        statusComboBox.setItems(FXCollections.observableArrayList(
            "Scheduled",
            "Pending",
            "Completed",
            "Failed"
        ));
    }

    /**
     * Pre-populates the modal fields with the data of the schedule being edited.
     */
    public void setSchedule(FeedingSchedule schedule) {
        if (schedule == null) return;
        this.oldSchedule = schedule;

        categoryComboBox.setValue(schedule.getCategory());
        feedingTypeComboBox.setValue(schedule.getFeedingType());
        statusComboBox.setValue(schedule.getStatus());

        // Parse schedule time safely into hour and minute dropdowns
        String timeStr = schedule.getTime();
        if (timeStr != null) {
            String[] parts = timeStr.split(":");
            if (parts.length == 2 && parts[0].length() == 2 && parts[1].length() == 2) {
                // Already in standard HH:mm format
                hourComboBox.setValue(parts[0]);
                minuteComboBox.setValue(parts[1]);
            } else {
                // Attempt parsing legacy or unusual formats
                LocalTime parsedTime = parseLegacyTime(timeStr);
                if (parsedTime != null) {
                    hourComboBox.setValue(String.format("%02d", parsedTime.getHour()));
                    minuteComboBox.setValue(String.format("%02d", parsedTime.getMinute()));
                } else {
                    // Fail-safe defaults
                    hourComboBox.setValue("08");
                    minuteComboBox.setValue("00");
                }
            }
        }
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    @FXML
    private void handleSave() {
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
            showError("Please complete all form fields to save changes.");
            return;
        }

        // Standardize time strictly to HH:mm format
        String time = String.format("%s:%s", hour, minute);

        FeedingSchedule newSchedule = new FeedingSchedule(category, time, feedingType, status);
        
        boolean success = FarmRepository.updateSchedule(oldSchedule, newSchedule);
        if (success) {
            NotificationService.notificationInfo(String.format("Updated feeding schedule details for '%s'.", feedingType));
            closeStage();
        } else {
            showError("Failed to persist schedule updates inside database.");
        }
    }

    private LocalTime parseLegacyTime(String timeStr) {
        if (timeStr == null) return null;
        timeStr = timeStr.trim().toUpperCase();
        try {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e1) {
            try {
                return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("H:mm"));
            } catch (Exception e2) {
                try {
                    return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("hh:mm a"));
                } catch (Exception e3) {
                    try {
                        return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("h:mm a"));
                    } catch (Exception e4) {
                        return null;
                    }
                }
            }
        }
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
