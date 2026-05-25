package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.Main;
import cpe223.group8.eggspress.models.FeedingSchedule;
import cpe223.group8.eggspress.models.InventoryItem;
import cpe223.group8.eggspress.models.Automation;
import cpe223.group8.eggspress.repository.FarmRepository;
import cpe223.group8.eggspress.services.NotificationService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.List;
import javafx.event.ActionEvent;

public class AutomationController {

    @FXML
    private ComboBox<String> locationComboBox;

    @FXML
    private ComboBox<InventoryItem> waterSourceComboBox;

    @FXML
    private TextField waterAmountField;

    @FXML
    private Label waterFeedbackLabel;

    // Feeding Schedule Table
    @FXML
    private TableView<FeedingSchedule> scheduleTable;

    @FXML
    private TableColumn<FeedingSchedule, String> categoryCol;

    @FXML
    private TableColumn<FeedingSchedule, String> timeCol;

    @FXML
    private TableColumn<FeedingSchedule, String> feedingTypeCol;

    @FXML
    private TableColumn<FeedingSchedule, String> statusCol;

    // Add Schedule Fields
    @FXML
    private ComboBox<String> scheduleCategoryComboBox;

    @FXML
    private TextField scheduleTimeField;

    @FXML
    private TextField scheduleFeedingTypeField;

    @FXML
    private TextField scheduleStatusField;

    @FXML
    private Label scheduleFeedbackLabel;

    public void initialize() {
        // 1. Force Location Selection Setup (Avoid FXML parsing duplication bugs)
        locationComboBox.getItems().clear();
        locationComboBox.setItems(FXCollections.observableArrayList("Main Coop A", "Breeding Barn B", "Chicks Facility"));
        locationComboBox.getSelectionModel().selectFirst();

        // 2. Setup Schedule Category Dropdown Options
        scheduleCategoryComboBox.setItems(FXCollections.observableArrayList("Water", "Grains", "Feed", "Others"));
        scheduleCategoryComboBox.getSelectionModel().selectFirst();

        // 3. Configure Table Column mappings FIRST
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        feedingTypeCol.setCellValueFactory(new PropertyValueFactory<>("feedingType"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 4. Force-Seed fresh simulations if no manual items exist
        seedSimulatedSchedules();

        // 5. Populate and convert Water Source elements from active database
        refreshWaterSources();
        waterSourceComboBox.setConverter(new StringConverter<InventoryItem>() {
            @Override
            public String toString(InventoryItem item) {
                if (item == null) return "";
                return item.getId() + " - " + item.getName() + " (" + item.getQuantity() + " " + item.getUnit() + ")";
            }

            @Override
            public InventoryItem fromString(String string) {
                return null;
            }
        });

        // 6. Finally, fetch and render everything to the table view
        refreshScheduleTable();
    }

    private void seedSimulatedSchedules() {
        List<FeedingSchedule> existingSchedules = FarmRepository.getAllSchedules();
        
        // If it only contains your 1 manual schedule or is empty, seed the rest!
        if (existingSchedules.size() <= 1) {
            System.out.println("Seeding fresh automated simulations into SQLite...");
            
            // Optional: You can clear old stale rows here if needed via a repository method
            FarmRepository.addSchedule(new FeedingSchedule("Water", "06:00 AM", "Automated Refill", "Completed"));
            FarmRepository.addSchedule(new FeedingSchedule("Grains", "08:30 AM", "Standard Broiler Feed", "Completed"));
            FarmRepository.addSchedule(new FeedingSchedule("Feed", "12:00 PM", "High-Protein Mix", "Pending"));
            FarmRepository.addSchedule(new FeedingSchedule("Water", "04:30 PM", "Hydration Top-Up", "Scheduled"));
        }
    }
    /**
     * Checks if the schedules table has existing entries. 
     * If empty, it pushes a simulated automated timeline into the SQLite DB.
     */
    private void checkForSimulatedSchedules() {
        List<FeedingSchedule> existingSchedules = FarmRepository.getAllSchedules();
        
        if (existingSchedules.isEmpty()) {
            System.out.println("No schedules detected. Seeding automated simulations into Database...");
            
            FarmRepository.addSchedule(new FeedingSchedule("Water", "06:00 AM", "Automated Refill", "Completed"));
            FarmRepository.addSchedule(new FeedingSchedule("Grains", "08:30 AM", "Standard Broiler Feed", "Completed"));
            FarmRepository.addSchedule(new FeedingSchedule("Feed", "12:00 PM", "High-Protein Mix", "Pending"));
            FarmRepository.addSchedule(new FeedingSchedule("Water", "04:30 PM", "Hydration Top-Up", "Scheduled"));
        }
    }

    private void refreshWaterSources() {
        ObservableList<InventoryItem> waterItems = FXCollections.observableArrayList(
            FarmRepository.getInventoryByCategory("Hydration")
        );
        
        waterSourceComboBox.setItems(waterItems);
        if (!waterItems.isEmpty()) {
            waterSourceComboBox.getSelectionModel().selectFirst();
        }
    }

    private void refreshScheduleTable() {
        scheduleTable.setItems(FXCollections.observableArrayList(FarmRepository.getAllSchedules()));
    }

    @FXML
    private void handleSendWater() {
        waterFeedbackLabel.setText("");
        
        InventoryItem selectedWater = waterSourceComboBox.getSelectionModel().getSelectedItem();
        if (selectedWater == null) {
            waterFeedbackLabel.setStyle("-fx-text-fill: red;");
            waterFeedbackLabel.setText("Error: Please select a water source from the inventory.");
            return;
        }

        String amountText = waterAmountField.getText();
        if (amountText == null || amountText.trim().isEmpty()) {
            waterFeedbackLabel.setStyle("-fx-text-fill: red;");
            waterFeedbackLabel.setText("Error: Please enter a water amount.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            waterFeedbackLabel.setStyle("-fx-text-fill: red;");
            waterFeedbackLabel.setText("Error: Invalid amount. Please enter a valid number.");
            return;
        }

        if (amount <= 0) {
            waterFeedbackLabel.setStyle("-fx-text-fill: red;");
            waterFeedbackLabel.setText("Error: Amount must be greater than zero.");
            return;
        }

        if (selectedWater.getQuantity() < amount) {
            waterFeedbackLabel.setStyle("-fx-text-fill: red;");
            waterFeedbackLabel.setText("Error: Insufficient stock. Available: " + selectedWater.getQuantity() + " " + selectedWater.getUnit());
            return;
        }

        double updatedQuantity = selectedWater.getQuantity() - amount;
        boolean updateSuccess = FarmRepository.updateInventoryQuantity(selectedWater.getId(), updatedQuantity);

        if (!updateSuccess) {
            waterFeedbackLabel.setStyle("-fx-text-fill: red;");
            waterFeedbackLabel.setText("Error: Failed to update inventory database records.");
            return;
        }

        selectedWater.setQuantity(updatedQuantity);
        NotificationService.getInstance().checkInventoryThresholds(selectedWater);
        
        String autoId = "AUTO-" + (FarmRepository.getAutomationCount() + 1);
        String location = locationComboBox.getSelectionModel().getSelectedItem();
        Automation log = new Automation(autoId, selectedWater.getName(), location, amount, "Success");
        FarmRepository.addAutomationLog(log);

        waterFeedbackLabel.setStyle("-fx-text-fill: green;");
        waterFeedbackLabel.setText("Success: Dispatched " + amount + " " + selectedWater.getUnit() + " to " + location + ".");
        waterAmountField.clear();

        refreshWaterSources();
    }

    @FXML
    private void handleCreateSchedule() {
        scheduleFeedbackLabel.setText("");

        String category = scheduleCategoryComboBox.getSelectionModel().getSelectedItem();
        String time = scheduleTimeField.getText();
        String feedingType = scheduleFeedingTypeField.getText();
        String status = scheduleStatusField.getText();

        if (time == null || time.trim().isEmpty() ||
            feedingType == null || feedingType.trim().isEmpty() ||
            status == null || status.trim().isEmpty()) {
            scheduleFeedbackLabel.setStyle("-fx-text-fill: red;");
            scheduleFeedbackLabel.setText("Error: All fields are required.");
            return;
        }

        FeedingSchedule newSchedule = new FeedingSchedule(category, time, feedingType, status);
        FarmRepository.addSchedule(newSchedule);

        scheduleFeedbackLabel.setStyle("-fx-text-fill: green;");
        scheduleFeedbackLabel.setText("Success: Added schedule to database successfully.");
        
        scheduleTimeField.clear();
        scheduleFeedingTypeField.clear();
        scheduleStatusField.clear();

        refreshScheduleTable();
    }

    @FXML
    private void handleBackToDashboard() {
        DashboardController dashboard = DashboardController.getInstance();
        if (dashboard != null) {
            dashboard.loadView("overview");
        } else {
            try {
                Main.setRoot("dashboard");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        // 1. Clear any old feedback text
        scheduleFeedbackLabel.setText("");

        // 2. Get the currently selected schedule item from the table view
        FeedingSchedule selectedSchedule = scheduleTable.getSelectionModel().getSelectedItem();

        // 3. Validation: Check if the user actually clicked a row before pressing delete
        if (selectedSchedule == null) {
            scheduleFeedbackLabel.setStyle("-fx-text-fill: red;");
            scheduleFeedbackLabel.setText("Error: Please select a schedule from the table to delete.");
            return;
        }

        // 4. Fire the deletion method in the repository to clean SQLite
        boolean success = FarmRepository.removeSchedule(selectedSchedule);

        if (success) {
            // 5. If database deletion works, pull it out of the UI table list immediately
            scheduleTable.getItems().remove(selectedSchedule);

            scheduleFeedbackLabel.setStyle("-fx-text-fill: green;");
            scheduleFeedbackLabel.setText("Success: Schedule removed from database.");

            // Optional: Reset table selection focus
            scheduleTable.getSelectionModel().clearSelection();
        } else {
            scheduleFeedbackLabel.setStyle("-fx-text-fill: red;");
            scheduleFeedbackLabel.setText("Error: Failed to delete schedule record from database.");
        }
    }
}