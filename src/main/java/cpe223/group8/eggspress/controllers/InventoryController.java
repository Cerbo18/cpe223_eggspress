package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.Main;
import cpe223.group8.eggspress.models.InventoryItem;
import cpe223.group8.eggspress.models.ChickenHouse;
import cpe223.group8.eggspress.models.MonthlyConsumptionLog;
import cpe223.group8.eggspress.repository.FarmRepository;
import cpe223.group8.eggspress.services.NotificationService;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.Group;
import javafx.scene.shape.SVGPath;
import javafx.scene.layout.StackPane;

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
    private TableColumn<InventoryItem, Void> actionColumn;

    @FXML
    private Button addItemBtn;

    @FXML
    private ComboBox<String> plannerMonthComboBox;

    @FXML
    private TextField plannerYearField;

    @FXML
    private Label plannerFlockLabel;

    @FXML
    private Label plannerEstFeedLabel;

    @FXML
    private Label plannerEstWaterLabel;

    @FXML
    private Label plannerFeedSufficiencyLabel;

    @FXML
    private Label plannerWaterSufficiencyLabel;

    @FXML
    private TextField actualFeedField;

    @FXML
    private TextField actualWaterField;

    @FXML
    private TableView<MonthlyConsumptionLog> monthlyLogsTable;

    @FXML
    private TableColumn<MonthlyConsumptionLog, String> logMonthCol;

    @FXML
    private TableColumn<MonthlyConsumptionLog, Integer> logFlockCol;

    @FXML
    private TableColumn<MonthlyConsumptionLog, Double> logEstFeedCol;

    @FXML
    private TableColumn<MonthlyConsumptionLog, Double> logEstWaterCol;

    @FXML
    private TableColumn<MonthlyConsumptionLog, Double> logActFeedCol;

    @FXML
    private TableColumn<MonthlyConsumptionLog, Double> logActWaterCol;



    @FXML
    public void initialize() {
        // Setup column bindings for regular stock table
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        // Setup Action Column inline buttons for Adjusting and Deleting items in Tab 1
        actionColumn.setCellFactory(col -> new TableCell<InventoryItem, Void>() {
            private final Button adjustBtn = new Button();
            private final Button deleteBtn = new Button();
            private final HBox container = new HBox(8, adjustBtn, deleteBtn);

            {
                adjustBtn.getStyleClass().addAll("button-secondary", "table-action-btn");
                adjustBtn.setGraphic(createSVGIcon(EDIT_PATH, "table-btn-icon"));
                cpe223.group8.eggspress.services.TooltipHelper.installTooltip(adjustBtn, "Adjust Stock");
                adjustBtn.setOnAction(e -> {
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    handleOpenAdjustModal(item);
                });

                deleteBtn.getStyleClass().addAll("button-danger", "table-action-btn");
                deleteBtn.setGraphic(createSVGIcon(DELETE_PATH, "table-btn-icon"));
                cpe223.group8.eggspress.services.TooltipHelper.installTooltip(deleteBtn, "Delete Item");
                deleteBtn.setOnAction(e -> {
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    handleConfirmDelete(item);
                });
                
                container.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });

        // Setup monthly logs TableColumn cell bindings to match MonthlyConsumptionLog fields
        logMonthCol.setCellValueFactory(new PropertyValueFactory<>("monthYear"));
        logFlockCol.setCellValueFactory(new PropertyValueFactory<>("flockCount"));
        logEstFeedCol.setCellValueFactory(new PropertyValueFactory<>("estimatedFeed"));
        logEstWaterCol.setCellValueFactory(new PropertyValueFactory<>("estimatedWater"));
        logActFeedCol.setCellValueFactory(new PropertyValueFactory<>("actualFeed"));
        logActWaterCol.setCellValueFactory(new PropertyValueFactory<>("actualWater"));

        // Setup Month selector items
        plannerMonthComboBox.setItems(FXCollections.observableArrayList(
            "January", "February", "March", "April", "May", "June", 
            "July", "August", "September", "October", "November", "December"
        ));
        
        // Auto-select current month and auto-fill current year based on local time
        LocalDate now = LocalDate.now();
        int monthIndex = now.getMonthValue() - 1;
        plannerMonthComboBox.getSelectionModel().select(monthIndex);
        plannerYearField.setText(String.valueOf(now.getYear()));

        // Wire event listeners to recalculate estimates dynamically on input changes
        plannerMonthComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            calculateMonthlyEstimates();
        });
        plannerYearField.textProperty().addListener((obs, oldVal, newVal) -> {
            calculateMonthlyEstimates();
        });

        // Load tables and initial calculations
        refreshTable();
        refreshMonthlyLogs();
        calculateMonthlyEstimates();
    }

    @FXML
    private void handleOpenCreateItemModal() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(Main.class.getResource("views/createInventoryModal.fxml"));
            Parent root = loader.load();
            cpe223.group8.eggspress.services.ThemeManager.applyTheme(root);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            if (addItemBtn != null && addItemBtn.getScene() != null) {
                stage.initOwner(addItemBtn.getScene().getWindow());
            }
            stage.setTitle("Create Inventory Item");
            stage.setResizable(false);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            cpe223.group8.eggspress.services.ThemeManager.applySceneFill(scene);
            stage.setScene(scene);
            stage.showAndWait();

            // Refresh table view and estimation calculators after popup closes
            refreshTable();
            calculateMonthlyEstimates();
        } catch (IOException e) {
            System.err.println("Error loading create inventory modal stage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleOpenAdjustModal(InventoryItem item) {
        if (item == null) return;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(Main.class.getResource("views/adjustStockModal.fxml"));
            Parent root = loader.load();
            cpe223.group8.eggspress.services.ThemeManager.applyTheme(root);

            AdjustStockModalController controller = loader.getController();
            controller.setItem(item);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            if (addItemBtn != null && addItemBtn.getScene() != null) {
                stage.initOwner(addItemBtn.getScene().getWindow());
            }
            stage.setTitle("Adjust Stock Quantity");
            stage.setResizable(false);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            cpe223.group8.eggspress.services.ThemeManager.applySceneFill(scene);
            stage.setScene(scene);
            stage.showAndWait();

            // Refresh table view and estimates after adjustment
            refreshTable();
            calculateMonthlyEstimates();
        } catch (IOException e) {
            System.err.println("Error loading adjust stock modal stage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleConfirmDelete(InventoryItem item) {
        if (item == null) return;

        // Display a themed confirmation alert box to verify deletion approval
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        cpe223.group8.eggspress.services.ThemeManager.applyTheme(alert.getDialogPane());
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Inventory Item");
        alert.setContentText(String.format("Are you sure you want to delete '%s' permanently?", item.getName()));

        // Resolve alert window owner stage if applicable
        if (addItemBtn != null && addItemBtn.getScene() != null) {
            alert.initOwner(addItemBtn.getScene().getWindow());
        }

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = FarmRepository.removeStaticItem(item);
                if (success) {
                    showSuccess(String.format("Deleted %s from inventory.", item.getName()));
                    refreshTable();
                    calculateMonthlyEstimates();
                } else {
                    showError("SQLite database failure removing selected item.");
                }
            }
        });
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
        NotificationService.notificationInfo(message);
    }

    private void showError(String message) {
        NotificationService.notificationWarning(message, false, 2);
    }

    // Dynamic monthly consumption estimation calculations based on total coop birds count
    private void calculateMonthlyEstimates() {
        // Evaluate live chicken flock counts across active database coops
        int totalFlock = 0;
        java.util.List<ChickenHouse> coops = FarmRepository.getAllCoops();
        for (ChickenHouse coop : coops) {
            if (coop.getStatus() != null && !"Inactive".equalsIgnoreCase(coop.getStatus().trim())) {
                totalFlock += coop.getFlockCount();
            }
        }

        plannerFlockLabel.setText(String.format("%,d Birds", totalFlock));

        // Estimates based on industry standard consumption guidelines
        double feedEstimate = totalFlock * 0.12 * 30; // 0.12 kg per chicken per day
        double waterEstimate = totalFlock * 0.25 * 30; // 0.25 Liters per chicken per day

        plannerEstFeedLabel.setText(String.format("%,.2f kg", feedEstimate));
        plannerEstWaterLabel.setText(String.format("%,.2f L", waterEstimate));

        // Auto-fill form fields if they have not been populated yet
        if (actualFeedField != null && actualFeedField.getText().trim().isEmpty()) {
            actualFeedField.setText(String.format("%.2f", feedEstimate));
        }
        if (actualWaterField != null && actualWaterField.getText().trim().isEmpty()) {
            actualWaterField.setText(String.format("%.2f", waterEstimate));
        }

        // Fetch current live stock volumes from SQLite
        double totalFeedStock = 0;
        double totalWaterStock = 0;
        java.util.List<InventoryItem> items = FarmRepository.getAllInventory();
        for (InventoryItem item : items) {
            String cat = item.getCategory() != null ? item.getCategory().toLowerCase() : "";
            String name = item.getName() != null ? item.getName().toLowerCase() : "";
            if (cat.contains("feed") || cat.contains("grain")) {
                totalFeedStock += item.getQuantity();
            } else if (cat.contains("water") || cat.contains("hydration") || name.contains("water")) {
                totalWaterStock += item.getQuantity();
            }
        }

        // Render stock adequacy pill badges strictly using stylesheet tokens
        if (plannerFeedSufficiencyLabel != null) {
            if (totalFeedStock >= feedEstimate && feedEstimate > 0) {
                updateSufficiencyLabel(plannerFeedSufficiencyLabel, "● Optimal", "status-optimal");
            } else if (totalFeedStock > 0 && feedEstimate > 0) {
                updateSufficiencyLabel(plannerFeedSufficiencyLabel, "⚠ Insufficient", "status-monitoring");
            } else {
                updateSufficiencyLabel(plannerFeedSufficiencyLabel, "⚠ Depleted", "status-critical");
            }
        }

        if (plannerWaterSufficiencyLabel != null) {
            if (totalWaterStock >= waterEstimate && waterEstimate > 0) {
                updateSufficiencyLabel(plannerWaterSufficiencyLabel, "● Optimal", "status-optimal");
            } else if (totalWaterStock > 0 && waterEstimate > 0) {
                updateSufficiencyLabel(plannerWaterSufficiencyLabel, "⚠ Insufficient", "status-monitoring");
            } else {
                updateSufficiencyLabel(plannerWaterSufficiencyLabel, "⚠ Depleted", "status-critical");
            }
        }
    }

    // Safely updates styling class lists on adequacy labels to comply with SoC
    private void updateSufficiencyLabel(Label label, String text, String activeClass) {
        label.setText(text);
        label.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-critical");
        label.getStyleClass().add(activeClass);
    }

    // Refresh historical logs displayed in the TableView
    private void refreshMonthlyLogs() {
        if (monthlyLogsTable != null) {
            ObservableList<MonthlyConsumptionLog> logs = FXCollections.observableArrayList(FarmRepository.getAllMonthlyLogs());
            monthlyLogsTable.setItems(logs);
        }
    }

    @FXML
    private void handleLogMonth() {
        String month = plannerMonthComboBox.getValue();
        String yearStr = plannerYearField.getText().trim();
        String actFeedStr = actualFeedField.getText().trim();
        String actWaterStr = actualWaterField.getText().trim();

        if (month == null || month.isEmpty() || yearStr.isEmpty() || actFeedStr.isEmpty() || actWaterStr.isEmpty()) {
            showError("Please complete all month-logger fields to save the log entry.");
            return;
        }

        if (!yearStr.matches("^\\d{4}$")) {
            showError("Please enter a valid 4-digit numeric year.");
            return;
        }

        double actFeed, actWater;
        try {
            actFeed = Double.parseDouble(actFeedStr);
            actWater = Double.parseDouble(actWaterStr);
            if (actFeed < 0 || actWater < 0) {
                showError("Consumption amounts cannot be negative values.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter valid numeric figures for actual supply usage.");
            return;
        }

        String monthYear = month + " " + yearStr;
        if (FarmRepository.isMonthYearLogged(monthYear)) {
            showError("A logged consumption entry already exists for " + monthYear + ". Please delete it first if you wish to re-log.");
            return;
        }

        // Calculate live estimates to log alongside actual figures
        int totalFlock = 0;
        java.util.List<ChickenHouse> coops = FarmRepository.getAllCoops();
        for (ChickenHouse coop : coops) {
            if (coop.getStatus() != null && !"Inactive".equalsIgnoreCase(coop.getStatus().trim())) {
                totalFlock += coop.getFlockCount();
            }
        }

        double feedEstimate = totalFlock * 0.12 * 30;
        double waterEstimate = totalFlock * 0.25 * 30;

        MonthlyConsumptionLog log = new MonthlyConsumptionLog(monthYear, totalFlock, feedEstimate, waterEstimate, actFeed, actWater);
        boolean success = FarmRepository.addMonthlyLog(log);
        if (success) {
            showSuccess(String.format("Successfully logged consumption for %s.", monthYear));
            actualFeedField.clear();
            actualWaterField.clear();
            refreshMonthlyLogs();
            calculateMonthlyEstimates();
        } else {
            showError("SQLite insertion error logging monthly consumption.");
        }
    }

    @FXML
    private void handleDeleteMonthlyLog() {
        MonthlyConsumptionLog selectedLog = monthlyLogsTable.getSelectionModel().getSelectedItem();
        if (selectedLog == null) {
            showError("Please select a logged entry from the history table to delete.");
            return;
        }

        boolean success = FarmRepository.removeMonthlyLog(selectedLog.getId());
        if (success) {
            showSuccess(String.format("Deleted log entry for %s.", selectedLog.getMonthYear()));
            refreshMonthlyLogs();
            calculateMonthlyEstimates();
        } else {
            showError("SQLite database failure removing selected log entry.");
        }
    }

    private static final String EDIT_PATH = "M7 7h-1a2 2 0 0 0 -2 2v9a2 2 0 0 0 2 2h9a2 2 0 0 0 2 -2v-1 M20.385 6.585a2.1 2.1 0 0 0 -2.97 -2.97l-8.415 8.385v3h3l8.385 -8.415 M16 5l3 3";
    private static final String DELETE_PATH = "M4 7h16 M5 7l1 12a2 2 0 0 0 2 2h8a2 2 0 0 0 2 -2l1 -12 M9 7v-3a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v3 M10 12l4 4m0 -4l-4 4";

    private StackPane createSVGIcon(String pathContent, String styleClass) {
        SVGPath path = new SVGPath();
        path.setContent(pathContent);
        path.getStyleClass().add(styleClass);
        
        Group group = new Group(path);
        group.setScaleX(0.65);
        group.setScaleY(0.65);
        
        StackPane wrapper = new StackPane(group);
        wrapper.setMinWidth(14);
        wrapper.setPrefWidth(14);
        wrapper.setMaxWidth(14);
        wrapper.setMinHeight(14);
        wrapper.setPrefHeight(14);
        wrapper.setMaxHeight(14);
        wrapper.setAlignment(javafx.geometry.Pos.CENTER);
        
        return wrapper;
    }
}
