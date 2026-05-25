package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.Main;
import cpe223.group8.eggspress.models.InventoryItem;
import cpe223.group8.eggspress.repository.FarmRepository;
import cpe223.group8.eggspress.services.NotificationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.io.IOException;

public class InventoryController {

    @FXML
    private TableView<InventoryItem> inventoryTable;

    @FXML
    private TableColumn<InventoryItem, String> idColumn;

    @FXML
    private TableColumn<InventoryItem, String> nameColumn;

    @FXML
    private TableColumn<InventoryItem, String> categoryColumn;

    @FXML
    private TableColumn<InventoryItem, Double> quantityColumn;

    @FXML
    private TableColumn<InventoryItem, String> unitColumn;

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private TextField quantityField;

    @FXML
    private TextField unitField;

    @FXML
    private TextField adjustmentField;

    @FXML
    private Label feedbackLabel;

    @FXML
    public void initialize() {
        // Setup column bindings
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        // Initialize ComboBox categories
        categoryComboBox.setItems(FXCollections.observableArrayList(
            "Water",
            "Grain",
            "Feed",
            "Others"
        ));
        categoryComboBox.getSelectionModel().selectFirst();

        // Load data
        refreshTable();
    }

    @FXML
    private void handleAddItem() {
        String name = nameField.getText().trim();
        String category = categoryComboBox.getValue();
        String qtyStr = quantityField.getText().trim();
        String unit = unitField.getText().trim();

        if (name.isEmpty() || category == null || category.isEmpty() || qtyStr.isEmpty() || unit.isEmpty()) {
            showError("Please fill out all fields to add an item.");
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(qtyStr);
            if (quantity < 0) {
                showError("Initial quantity cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid numeric value for initial quantity.");
            return;
        }

        // Generate a new ID based on current items count
        int nextIdNum = FarmRepository.getInventoryCount() + 1;
        String id = String.format("INV-%03d", nextIdNum);

        // Save new item
        FarmRepository.addStaticItem(new InventoryItem(id, name, category, quantity, unit));

        // Clear input fields
        nameField.clear();
        categoryComboBox.getSelectionModel().selectFirst();
        quantityField.clear();
        unitField.clear();

        // Refresh table view
        refreshTable();
        showSuccess("Inventory item added successfully.");
    }

    @FXML
    private void handleAddStock() {
        InventoryItem selectedItem = inventoryTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showError("Please select an item from the table first.");
            return;
        }

        String adjustmentStr = adjustmentField.getText().trim();
        if (adjustmentStr.isEmpty()) {
            showError("Please enter a stock amount to add.");
            return;
        }

        double adjustment;
        try {
            adjustment = Double.parseDouble(adjustmentStr);
            if (adjustment <= 0) {
                showError("Please enter a positive value to add stock.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid numeric value for the stock amount.");
            return;
        }

        // Update quantity
        selectedItem.setQuantity(selectedItem.getQuantity() + adjustment);
        // Inside handleAddStock() and handleConsumeStock() right below selectedItem.setQuantity(...)
        FarmRepository.updateItemQuantity(selectedItem);
        NotificationService.getInstance().checkInventoryThresholds(selectedItem);
        adjustmentField.clear();
        inventoryTable.refresh();
        showSuccess(String.format("Added %.2f %s to %s.", adjustment, selectedItem.getUnit(), selectedItem.getName()));
    }

    @FXML
    private void handleConsumeStock() {
        InventoryItem selectedItem = inventoryTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showError("Please select an item from the table first.");
            return;
        }

        String adjustmentStr = adjustmentField.getText().trim();
        if (adjustmentStr.isEmpty()) {
            showError("Please enter a stock amount to consume.");
            return;
        }

        double adjustment;
        try {
            adjustment = Double.parseDouble(adjustmentStr);
            if (adjustment <= 0) {
                showError("Please enter a positive value to consume stock.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid numeric value for the stock amount.");
            return;
        }

        // Check if there is enough stock
        if (selectedItem.getQuantity() < adjustment) {
            showError(String.format("Insufficient stock. Only %.2f %s available.", selectedItem.getQuantity(), selectedItem.getUnit()));
            return;
        }

        // Update quantity
        selectedItem.setQuantity(selectedItem.getQuantity() - adjustment);
        // Inside handleAddStock() and handleConsumeStock() right below selectedItem.setQuantity(...)
        FarmRepository.updateItemQuantity(selectedItem);
        NotificationService.getInstance().checkInventoryThresholds(selectedItem);
        adjustmentField.clear();
        inventoryTable.refresh();
        showSuccess(String.format("Consumed %.2f %s from %s.", adjustment, selectedItem.getUnit(), selectedItem.getName()));
    }

    @FXML
    private void handleDeleteItem() {
        InventoryItem selectedItem = inventoryTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showError("Please select an item from the table first to delete.");
            return;
        }

        FarmRepository.removeStaticItem(selectedItem);
        refreshTable();
        showSuccess(String.format("Deleted %s from inventory.", selectedItem.getName()));
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

    private void refreshTable() {
        ObservableList<InventoryItem> list = FXCollections.observableArrayList(FarmRepository.getAllInventory());
        inventoryTable.setItems(list);
    }

    private void showSuccess(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.setTextFill(Color.GREEN);
    }

    private void showError(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.setTextFill(Color.RED);
    }
}
