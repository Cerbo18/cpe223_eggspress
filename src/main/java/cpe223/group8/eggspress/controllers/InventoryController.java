package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.Main;
import cpe223.group8.eggspress.models.InventoryItem;
import cpe223.group8.eggspress.models.ChickenHouse;
import cpe223.group8.eggspress.models.MonthlyConsumptionLog;
import cpe223.group8.eggspress.repository.FarmRepository;
import cpe223.group8.eggspress.services.NotificationService;
import cpe223.group8.eggspress.services.SvgIconHelper;
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
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.shape.Line;
import javafx.scene.Node;

import java.io.IOException;

public class InventoryController {

    @FXML
    private TabPane inventoryTabPane;

    @FXML
    private StackPane analyticsStackPane;

    @FXML
    private BarChart<String, Number> stockBarChart;

    @FXML
    private CategoryAxis stockXAxis;

    @FXML
    private NumberAxis stockYAxis;

    private Line committedLineOverlay;

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
                adjustBtn.setGraphic(SvgIconHelper.createTableActionIcon(SvgIconHelper.IconType.EDIT, "table-btn-icon"));
                cpe223.group8.eggspress.services.TooltipHelper.installTooltip(adjustBtn, "Adjust Stock");
                adjustBtn.setOnAction(e -> {
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    handleOpenAdjustModal(item);
                });

                deleteBtn.getStyleClass().addAll("button-danger", "table-action-btn");
                deleteBtn.setGraphic(SvgIconHelper.createTableActionIcon(SvgIconHelper.IconType.DELETE, "table-btn-icon"));
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

        // Setup stock chart sizing and listeners
        updateStockChart();

        // Listen to tab selection to refresh the chart dynamically
        if (inventoryTabPane != null) {
            inventoryTabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
                if (newIdx.intValue() == 2) {
                    updateStockChart();
                }
            });
        }
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

    private void updateStockChart() {
        if (stockBarChart == null) return;
        stockBarChart.getData().clear();

        // 1. Fetch current stocks
        double totalFeed = 0;
        double totalWater = 0;
        java.util.List<InventoryItem> items = FarmRepository.getAllInventory();
        for (InventoryItem item : items) {
            String cat = item.getCategory() != null ? item.getCategory().toLowerCase() : "";
            String name = item.getName() != null ? item.getName().toLowerCase() : "";
            if (cat.contains("feed") || cat.contains("grain")) {
                totalFeed += item.getQuantity();
            } else if (cat.contains("water") || cat.contains("hydration") || name.contains("water")) {
                totalWater += item.getQuantity();
            }
        }

        // 2. Fetch active flock and compute requirements
        int totalFlock = 0;
        java.util.List<ChickenHouse> coops = FarmRepository.getAllCoops();
        for (ChickenHouse coop : coops) {
            if (coop.getStatus() != null && !"Inactive".equalsIgnoreCase(coop.getStatus().trim())) {
                totalFlock += coop.getFlockCount();
            }
        }

        double feedRequirement = totalFlock * 0.12 * 30; // 0.12 kg/day for 30 days
        double waterRequirement = totalFlock * 0.25 * 30; // 0.25 L/day for 30 days

        double feedPct = feedRequirement > 0 ? (totalFeed / feedRequirement) * 100.0 : 0.0;
        double waterPct = waterRequirement > 0 ? (totalWater / waterRequirement) * 100.0 : 0.0;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Current Stock Adequacy");
        series.getData().add(new XYChart.Data<>("Feed (Grains & Layers)", feedPct));
        series.getData().add(new XYChart.Data<>("Water (Hydration)", waterPct));

        stockBarChart.getData().add(series);

        // Styling the bars dynamically
        for (XYChart.Data<String, Number> data : series.getData()) {
            Node bar = data.getNode();
            if (bar != null) {
                if ("Feed (Grains & Layers)".equals(data.getXValue())) {
                    bar.setStyle("-fx-bar-fill: -fx-brand-primary;");
                } else {
                    bar.setStyle("-fx-bar-fill: #007aff;"); // Beautiful blue for water
                }
            }
        }

        // Position the 100% overlay line precisely
        javafx.application.Platform.runLater(this::drawCommittedLine);
    }

    private void drawCommittedLine() {
        if (analyticsStackPane == null || stockBarChart == null || stockYAxis == null) return;

        if (committedLineOverlay != null) {
            analyticsStackPane.getChildren().remove(committedLineOverlay);
        }

        Node plotArea = stockBarChart.lookup(".chart-plot-background");
        if (plotArea != null) {
            double plotHeight = plotArea.getLayoutBounds().getHeight();
            double plotWidth = plotArea.getLayoutBounds().getWidth();

            // Y position of 100% relative to yAxis
            double y100 = stockYAxis.getDisplayPosition(100.0);

            committedLineOverlay = new Line(0, y100, plotWidth, y100);
            committedLineOverlay.setStroke(Color.web("#ff3b30")); // premium red/orange
            committedLineOverlay.setStrokeWidth(2.0);
            committedLineOverlay.getStrokeDashArray().addAll(6.0, 4.0);
            committedLineOverlay.setManaged(false); // don't affect parent layouts
            committedLineOverlay.getStyleClass().add("committed-line-overlay");

            // Position line on top of the plot area
            double plotX = plotArea.localToScene(0, 0).getX() - analyticsStackPane.localToScene(0, 0).getX();
            double plotY = plotArea.localToScene(0, 0).getY() - analyticsStackPane.localToScene(0, 0).getY();

            committedLineOverlay.setLayoutX(plotX);
            committedLineOverlay.setLayoutY(plotY + y100);
            committedLineOverlay.setStartX(0);
            committedLineOverlay.setEndX(plotWidth);
            committedLineOverlay.setStartY(0);
            committedLineOverlay.setEndY(0);

            analyticsStackPane.getChildren().add(committedLineOverlay);

            // Add resize listener once to ensure the line updates on layout changes
            plotArea.layoutBoundsProperty().removeListener((obs, oldVal, newVal) -> {});
            plotArea.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
                javafx.application.Platform.runLater(this::drawCommittedLine);
            });
        }
    }

}
