package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.models.InventoryItem;
import cpe223.group8.eggspress.repository.FarmRepository;
import cpe223.group8.eggspress.services.NotificationService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller class for the adjustStockModal.fxml stage view.
 * Coordinates additions and consumptions on a pre-selected InventoryItem.
 */
public class AdjustStockModalController {

    @FXML
    private Label itemDetailsLabel;

    @FXML
    private TextField adjustmentField;

    private InventoryItem currentItem;

    /**
     * Injects the targeted InventoryItem data model reference into this controller context.
     * Updates labels to display the selected item name, quantity, and unit.
     */
    public void setItem(InventoryItem item) {
        this.currentItem = item;
        if (item != null) {
            itemDetailsLabel.setText(String.format("Selected Item: %s (%.2f %s)", 
                item.getName(), item.getQuantity(), item.getUnit()));
        }
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    @FXML
    private void handleConsume() {
        if (currentItem == null) {
            showError("No active item selected for adjustment.");
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
            showError("Please enter a valid numeric value for the adjustment amount.");
            return;
        }

        // Verify that the inventory has sufficient volume to prevent negative storage
        if (currentItem.getQuantity() < adjustment) {
            showError(String.format("Insufficient stock. Only %.2f %s available.", 
                currentItem.getQuantity(), currentItem.getUnit()));
            return;
        }

        currentItem.setQuantity(currentItem.getQuantity() - adjustment);
        boolean success = FarmRepository.updateItemQuantity(currentItem);
        if (success) {
            NotificationService.notificationInfo(String.format("Consumed %.2f %s from %s.", 
                adjustment, currentItem.getUnit(), currentItem.getName()));
            NotificationService.getInstance().checkInventoryThresholds(currentItem);
            closeStage();
        } else {
            showError("SQLite database failure updating consumed item quantity.");
        }
    }

    @FXML
    private void handleAddStock() {
        if (currentItem == null) {
            showError("No active item selected for adjustment.");
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
            showError("Please enter a valid numeric value for the adjustment amount.");
            return;
        }

        currentItem.setQuantity(currentItem.getQuantity() + adjustment);
        boolean success = FarmRepository.updateItemQuantity(currentItem);
        if (success) {
            NotificationService.notificationInfo(String.format("Added %.2f %s to %s.", 
                adjustment, currentItem.getUnit(), currentItem.getName()));
            NotificationService.getInstance().checkInventoryThresholds(currentItem);
            closeStage();
        } else {
            showError("SQLite database failure updating top-up item quantity.");
        }
    }

    private void closeStage() {
        Stage stage = (Stage) adjustmentField.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void showError(String message) {
        NotificationService.notificationWarning(message, false, 2);
    }
}
