package com.eggspress.controllers;

import com.eggspress.Main;
import com.eggspress.models.FeedingSchedule;
import com.eggspress.models.InventoryItem;
import com.eggspress.models.Automation;
import com.eggspress.repository.FarmRepository;

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

public class AutomationController {

    @FXML
    private ComboBox<InventoryItem> waterSourceComboBox;

    @FXML
    private ComboBox<String> locationComboBox;

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

    @FXML
    public void initialize() {
        // Setup Location dropdown with only "Not Available" per prompt instructions
        locationComboBox.setItems(FXCollections.observableArrayList("Not Available"));
        locationComboBox.getSelectionModel().selectFirst();

        // Setup Schedule Category dropdown
        scheduleCategoryComboBox.setItems(FXCollections.observableArrayList("Water", "Grains", "Feed", "Others"));
        scheduleCategoryComboBox.getSelectionModel().selectFirst();

        // Populate and convert Water Source dropdown
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

        // Configure Table Columns
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        feedingTypeCol.setCellValueFactory(new PropertyValueFactory<>("feedingType"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Populate Table
        refreshScheduleTable();
    }

    private void refreshWaterSources() {
        ObservableList<InventoryItem> waterItems = FXCollections.observableArrayList();
        for (InventoryItem item : FarmRepository.getStaticInventory()) {
            if ("Water".equalsIgnoreCase(item.getCategory())) {
                waterItems.add(item);
            }
        }
        waterSourceComboBox.setItems(waterItems);
        if (!waterItems.isEmpty()) {
            waterSourceComboBox.getSelectionModel().selectFirst();
        }
    }

    private void refreshScheduleTable() {
        scheduleTable.setItems(FXCollections.observableArrayList(FarmRepository.getStaticSchedules()));
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
            waterFeedbackLabel.setText("Error: Insufficient stock. Available: " + selectedWater.getQuantity() + " Liters.");
            return;
        }

        // Deduct water quantity from the selected inventory item in static repository
        selectedWater.setQuantity(selectedWater.getQuantity() - amount);
        
        // Log automation run
        String autoId = "AUTO-" + (FarmRepository.getStaticAutomations().size() + 1);
        String location = locationComboBox.getSelectionModel().getSelectedItem();
        FarmRepository.addStaticAutomation(new Automation(autoId, selectedWater.getName(), location, amount, "Success"));

        // Feedback to user
        waterFeedbackLabel.setStyle("-fx-text-fill: green;");
        waterFeedbackLabel.setText("Success: Dispatched " + amount + " Liters to " + location + ". Stock updated.");
        waterAmountField.clear();

        // Refresh dropdown to reflect updated quantity
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

        // Add to static list in FarmRepository
        FeedingSchedule newSchedule = new FeedingSchedule(category, time, feedingType, status);
        FarmRepository.addStaticSchedule(newSchedule);

        // Feedback and refresh
        scheduleFeedbackLabel.setStyle("-fx-text-fill: green;");
        scheduleFeedbackLabel.setText("Success: Added schedule successfully.");
        
        scheduleTimeField.clear();
        scheduleFeedingTypeField.clear();
        scheduleStatusField.clear();

        refreshScheduleTable();
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            Main.setRoot("dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
