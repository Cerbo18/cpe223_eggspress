package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.models.InventoryItem;
import cpe223.group8.eggspress.repository.FarmRepository;
import cpe223.group8.eggspress.services.NotificationService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller class for the createInventoryModal.fxml stage view.
 * Handles validation and creation of new InventoryItem database rows.
 */
public class CreateInventoryItemModalController {

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private TextField quantityField;

    @FXML
    private ComboBox<String> unitComboBox;

    @FXML
    public void initialize() {
        // Initialize ComboBox categories mapping inventory subsystem options
        categoryComboBox.setItems(FXCollections.observableArrayList(
            "Water",
            "Grain",
            "Feed",
            "Others"
        ));
        categoryComboBox.getSelectionModel().selectFirst();

        // Initialize ComboBox unit mappings representing allowed tracking metrics
        unitComboBox.setItems(FXCollections.observableArrayList(
            "kg", "L", "pcs", "bags", "vials"
        ));
        unitComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    @FXML
    private void handleCreate() {
        String name = nameField.getText().trim();
        String category = categoryComboBox.getValue();
        String qtyStr = quantityField.getText().trim();
        String unit = unitComboBox.getValue();

        if (name.isEmpty() || category == null || category.isEmpty() || qtyStr.isEmpty() || unit == null || unit.isEmpty()) {
            showError("Please complete all form fields to add the item.");
            return;
        }

        // Validate character formats to prevent SQL anomalies
        if (!name.matches("^[a-zA-Z0-9\\s._-]+$")) {
            showError("Item Name contains invalid characters. Only alphanumeric, spaces, dots, dashes, and underscores are allowed.");
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(qtyStr);
            if (quantity < 0) {
                showError("Initial quantity cannot be a negative value.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid numeric value for the initial quantity.");
            return;
        }

        // Auto-increment the unique inventory identifier keys safely
        int nextIdNum = FarmRepository.getInventoryCount() + 1;
        String id = String.format("INV-%03d", nextIdNum);

        boolean success = FarmRepository.addInventoryItem(new InventoryItem(id, name, category, quantity, unit));
        if (success) {
            NotificationService.notificationInfo(String.format("Inventory item '%s' successfully created.", name));
            closeStage();
        } else {
            showError("SQLite database failure persisting new inventory item.");
        }
    }

    private void closeStage() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void showError(String message) {
        NotificationService.notificationWarning(message, false, 2);
    }
}
