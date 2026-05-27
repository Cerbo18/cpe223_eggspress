package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.models.ChickenHouse;
import cpe223.group8.eggspress.models.InventoryItem;
import cpe223.group8.eggspress.repository.FarmRepository;
import cpe223.group8.eggspress.simulation.AutomationService;
import cpe223.group8.eggspress.simulation.CoopTelemetry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.format.DateTimeFormatter;

public class OverviewController {

    @FXML
    private VBox viewRoot;

    @FXML
    private TableView<ChickenHouse> coopsTable;

    @FXML
    private TableColumn<ChickenHouse, String> coopIdCol;

    @FXML
    private TableColumn<ChickenHouse, String> coopNameCol;

    @FXML
    private TableColumn<ChickenHouse, Integer> coopFlockCol;

    @FXML
    private TableColumn<ChickenHouse, String> coopStatusCol;

    @FXML
    private Label telemetryTime;

    @FXML
    private LineChart<String, Number> telemetryChart;

    @FXML
    private CategoryAxis telemetryXAxis;

    @FXML
    private NumberAxis telemetryYAxis;

    @FXML
    private Label detailsTitle;

    @FXML
    private Label detailsSubtitle;

    @FXML
    private Label detailTemp;

    @FXML
    private Label detailHumid;

    @FXML
    private Label detailFan;

    @FXML
    private Label detailMister;

    @FXML
    private Label detailFeeder;

    @FXML
    private Label detailLabelRow1;

    @FXML
    private Label detailLabelRow2;

    @FXML
    private Label detailLabelRow3;

    @FXML
    private Label detailLabelRow4;

    @FXML
    private Label detailLabelRow5;

    // FXML Radial Progress Arcs
    @FXML
    private Arc tempProgress;

    @FXML
    private Arc humidProgress;

    @FXML
    private Arc waterProgress;

    @FXML
    private Arc feedProgress;

    // FXML Radial Value Labels
    @FXML
    private Label tempValueLabel;

    @FXML
    private Label humidValueLabel;

    @FXML
    private Label waterValueLabel;

    @FXML
    private Label feedValueLabel;

    // Sliding window registries to preserve real-time histories
    private final Map<String, List<Double>> tempHistory = new HashMap<>();
    private final Map<String, List<Double>> humidHistory = new HashMap<>();

    private XYChart.Series<String, Number> tempSeries;
    private XYChart.Series<String, Number> humidSeries;
    private Timeline telemetryTimeline;

    @FXML
    public void initialize() {
        // Bind TableView columns to ChickenHouse model fields
        coopIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        coopNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        coopFlockCol.setCellValueFactory(new PropertyValueFactory<>("flockCount"));
        coopStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Table status cell custom styling resolving visually through class definitions
        coopStatusCol.setCellFactory(column -> new TableCell<ChickenHouse, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");
                } else {
                    setText(item);
                    getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");
                    if ("Optimal".equalsIgnoreCase(item)) {
                        getStyleClass().add("status-optimal");
                    } else if ("Monitoring".equalsIgnoreCase(item)) {
                        getStyleClass().add("status-monitoring");
                    } else {
                        getStyleClass().add("status-inactive");
                    }
                }
            }
        });

        // Initialize dynamic LineChart series parameters
        tempSeries = new XYChart.Series<>();
        tempSeries.setName("Temperature (°C)");
        humidSeries = new XYChart.Series<>();
        humidSeries.setName("Humidity (%)");
        
        telemetryChart.getData().addAll(tempSeries, humidSeries);
        telemetryYAxis.setAutoRanging(true);

        // Seed dynamic parameters to render visual trend lines instantly upon startup
        seedInitialTelemetryHistory();

        // Bind TableView selection listeners to coordinate focus changes
        coopsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateTelemetryDetails(newVal);
            updateRadialGauges(newVal);
            refreshChart();
        });

        // Set initial telemetry clock time and values
        telemetryTime.setText("Real Time Sensor | " + java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
        updateRadialGauges(null);

        // Populate dynamic statistics counts
        refreshData();

        // Display default farm-wide aggregates
        updateTelemetryDetails(null);
        refreshChart();

        // Initialize poller timeline execution
        setupRealTimePoller();

        // Stop poller timelines when container unloads to safeguard resources and prevent memory leaks
        viewRoot.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                if (telemetryTimeline != null) {
                    telemetryTimeline.stop();
                }
            }
        });
    }

    private void seedInitialTelemetryHistory() {
        List<ChickenHouse> coops = FarmRepository.getAllCoops();
        double sumTemp = 0;
        double sumHumid = 0;
        int activeCount = 0;

        for (ChickenHouse coop : coops) {
            CoopTelemetry telemetry = AutomationService.getInstance().getTelemetryForCoop(coop.getId());
            if (telemetry != null) {
                double temp = telemetry.getTemperature();
                double humid = telemetry.getHumidity();

                // Append 10 historical values with standard offset variance math
                for (int i = 9; i >= 0; i--) {
                    double varianceTemp = (Math.sin(i * 0.5) * 0.4) + ((i % 3 - 1) * 0.2);
                    double varianceHumid = (Math.cos(i * 0.5) * 1.5) + ((i % 3 - 1) * 0.8);
                    appendHistoryPoint(coop.getId(), temp - varianceTemp, humid - varianceHumid);
                }

                sumTemp += temp;
                sumHumid += humid;
                activeCount++;
            }
        }

        // Seed average farm trends history
        if (activeCount > 0) {
            double avgTemp = sumTemp / activeCount;
            double avgHumid = sumHumid / activeCount;
            for (int i = 9; i >= 0; i--) {
                double varianceTemp = (Math.sin(i * 0.5) * 0.4) + ((i % 3 - 1) * 0.2);
                double varianceHumid = (Math.cos(i * 0.5) * 1.5) + ((i % 3 - 1) * 0.8);
                appendHistoryPoint("_average", avgTemp - varianceTemp, avgHumid - varianceHumid);
            }
        }
    }

    private void appendHistoryPoint(String coopId, double temp, double humid) {
        tempHistory.putIfAbsent(coopId, new ArrayList<>());
        humidHistory.putIfAbsent(coopId, new ArrayList<>());

        List<Double> temps = tempHistory.get(coopId);
        List<Double> humids = humidHistory.get(coopId);

        temps.add(temp);
        humids.add(humid);

        // Keep sliding window bounds capped at a standard count of 10 points
        if (temps.size() > 10) {
            temps.remove(0);
        }
        if (humids.size() > 10) {
            humids.remove(0);
        }
    }

    private void pollTelemetryAndAppendHistory() {
        List<ChickenHouse> coops = FarmRepository.getAllCoops();
        double sumTemp = 0;
        double sumHumid = 0;
        int activeCount = 0;

        for (ChickenHouse coop : coops) {
            CoopTelemetry telemetry = AutomationService.getInstance().getTelemetryForCoop(coop.getId());
            if (telemetry != null) {
                appendHistoryPoint(coop.getId(), telemetry.getTemperature(), telemetry.getHumidity());
                sumTemp += telemetry.getTemperature();
                sumHumid += telemetry.getHumidity();
                activeCount++;
            }
        }

        if (activeCount > 0) {
            appendHistoryPoint("_average", sumTemp / activeCount, sumHumid / activeCount);
        }
    }

    private void refreshChart() {
        tempSeries.getData().clear();
        humidSeries.getData().clear();

        ChickenHouse selected = coopsTable.getSelectionModel().getSelectedItem();
        String targetKey = (selected != null) ? selected.getId() : "_average";

        List<Double> temps = tempHistory.getOrDefault(targetKey, new ArrayList<>());
        List<Double> humids = humidHistory.getOrDefault(targetKey, new ArrayList<>());

        int size = temps.size();
        for (int i = 0; i < size; i++) {
            // Render category step intervals representing progression relative to time (T-9 to T-0)
            String category = "T-" + (size - 1 - i);
            tempSeries.getData().add(new XYChart.Data<>(category, temps.get(i)));
            humidSeries.getData().add(new XYChart.Data<>(category, humids.get(i)));
        }
    }

    private void updateTelemetryDetails(ChickenHouse selected) {
        if (selected != null) {
            detailsTitle.setText(selected.getName() + " Telemetry");
            detailsSubtitle.setText("Real-time sensor feed for " + selected.getId());

            // Reset dynamic labels to climate parameters
            detailLabelRow1.setText("Temperature");
            detailLabelRow2.setText("Humidity");
            detailLabelRow3.setText("Ventilation Fan");
            detailLabelRow4.setText("Mister Status");
            detailLabelRow5.setText("Automatic Feeder");

            // Clear status styling from all values in the grid to prevent styles leaking
            detailTemp.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");
            detailHumid.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");
            detailFan.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");
            detailMister.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");
            detailFeeder.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");

            CoopTelemetry telemetry = AutomationService.getInstance().getTelemetryForCoop(selected.getId());
            if (telemetry != null) {
                detailTemp.setText(String.format("%.1f °C", telemetry.getTemperature()));
                detailHumid.setText(String.format("%.1f %%", telemetry.getHumidity()));
                detailFan.setText(telemetry.getFanSpeed());
                detailMister.setText(telemetry.getMisterStatus());
                detailFeeder.setText(telemetry.getFeederStatus());
            } else {
                detailTemp.setText("--.- °C");
                detailHumid.setText("--.- %");
                detailFan.setText("STANDBY");
                detailMister.setText("STANDBY");
                detailFeeder.setText("STANDBY");
            }
        } else {
            detailsTitle.setText("Farm Operations Summary");
            detailsSubtitle.setText("Overview of all active structures and resources");

            // Reset dynamic labels to operations metrics
            detailLabelRow1.setText("Total Flock Size");
            detailLabelRow2.setText("Coops Status");
            detailLabelRow3.setText("Active Schedules");
            detailLabelRow4.setText("Inventory Items");
            detailLabelRow5.setText("System Status");

            // Clear status styling from all values in the grid to prevent styles leaking
            detailTemp.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");
            detailHumid.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");
            detailFan.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");
            detailMister.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");
            detailFeeder.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");

            List<ChickenHouse> coops = FarmRepository.getAllCoops();
            int totalFlock = 0;
            int optimalCount = 0;
            int monitoringCount = 0;
            int inactiveCount = 0;
            for (ChickenHouse coop : coops) {
                totalFlock += coop.getFlockCount();
                String st = coop.getStatus();
                if ("Optimal".equalsIgnoreCase(st)) {
                    optimalCount++;
                } else if ("Monitoring".equalsIgnoreCase(st)) {
                    monitoringCount++;
                } else {
                    inactiveCount++;
                }
            }

            int scheduleCount = FarmRepository.getAllSchedules().size();
            int inventoryCount = FarmRepository.getAllInventory().size();

            detailTemp.setText(String.format("%,d Birds", totalFlock));
            detailHumid.setText(String.format("%d Opt | %d Mon | %d Inact", optimalCount, monitoringCount, inactiveCount));
            detailFan.setText(String.format("%d Active", scheduleCount));
            detailMister.setText(String.format("%d Items in Stock", inventoryCount));
            detailFeeder.setText("ONLINE");

            // Style system status to green
            detailFeeder.getStyleClass().add("status-optimal");
        }
    }

    private void updateRadialGauges(ChickenHouse selected) {
        double currentTemp = 0;
        double currentHumid = 0;
        
        if (selected != null) {
            CoopTelemetry telemetry = AutomationService.getInstance().getTelemetryForCoop(selected.getId());
            if (telemetry != null) {
                currentTemp = telemetry.getTemperature();
                currentHumid = telemetry.getHumidity();
            }
        } else {
            // Aggregate averages across all coops
            List<ChickenHouse> coops = FarmRepository.getAllCoops();
            double sumTemp = 0;
            double sumHumid = 0;
            int count = 0;
            for (ChickenHouse coop : coops) {
                CoopTelemetry telemetry = AutomationService.getInstance().getTelemetryForCoop(coop.getId());
                if (telemetry != null) {
                    sumTemp += telemetry.getTemperature();
                    sumHumid += telemetry.getHumidity();
                    count++;
                }
            }
            if (count > 0) {
                currentTemp = sumTemp / count;
                currentHumid = sumHumid / count;
            }
        }

        // 1. Update Temperature Radial gauge
        double tempPct = Math.max(0.0, Math.min(1.0, currentTemp / 50.0));
        tempProgress.setLength(tempPct * -360.0);
        tempValueLabel.setText(String.format("%.1f °C", currentTemp));
        
        tempProgress.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");
        if (currentTemp >= 18.0 && currentTemp <= 29.0) {
            tempProgress.getStyleClass().add("status-optimal");
        } else if ((currentTemp >= 29.0 && currentTemp <= 32.0) || (currentTemp >= 15.0 && currentTemp < 18.0)) {
            tempProgress.getStyleClass().add("status-monitoring");
        } else {
            tempProgress.getStyleClass().add("status-inactive");
        }

        // 2. Update Humidity Radial gauge
        double humidPct = Math.max(0.0, Math.min(1.0, currentHumid / 100.0));
        humidProgress.setLength(humidPct * -360.0);
        humidValueLabel.setText(String.format("%.1f %%", currentHumid));

        humidProgress.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");
        if (currentHumid >= 40.0 && currentHumid <= 75.0) {
            humidProgress.getStyleClass().add("status-optimal");
        } else if ((currentHumid >= 75.0 && currentHumid <= 82.0) || (currentHumid >= 35.0 && currentHumid < 40.0)) {
            humidProgress.getStyleClass().add("status-monitoring");
        } else {
            humidProgress.getStyleClass().add("status-inactive");
        }

        // 3. Update Water & Feed Radials (Farm-Wide Inventory Averages)
        List<InventoryItem> items = FarmRepository.getAllInventory();
        double totalFeed = 0;
        double totalWater = 0;

        for (InventoryItem item : items) {
            String cat = item.getCategory().toLowerCase();
            String name = item.getName().toLowerCase();
            if (cat.contains("feed") || cat.contains("grain")) {
                totalFeed += item.getQuantity();
            } else if (cat.contains("hydration") || cat.contains("water") || name.contains("water")) {
                totalWater += item.getQuantity();
            }
        }

        double waterPct = Math.max(0.0, Math.min(1.0, totalWater / 2000.0));
        waterProgress.setLength(waterPct * -360.0);
        waterValueLabel.setText(String.format("%.0f %%", waterPct * 100.0));

        waterProgress.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");
        if (totalWater > 500.0) {
            waterProgress.getStyleClass().add("status-optimal");
        } else {
            waterProgress.getStyleClass().add("status-inactive");
        }

        double feedPct = Math.max(0.0, Math.min(1.0, totalFeed / 1000.0));
        feedProgress.setLength(feedPct * -360.0);
        feedValueLabel.setText(String.format("%.0f %%", feedPct * 100.0));

        feedProgress.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");
        if (totalFeed > 200.0) {
            feedProgress.getStyleClass().add("status-optimal");
        } else {
            feedProgress.getStyleClass().add("status-inactive");
        }
    }

    private void setupRealTimePoller() {
        telemetryTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            // 1. Refresh active coops table
            refreshData();

            // 2. Poll live telemetry and append reading histories
            pollTelemetryAndAppendHistory();

            // 3. Update right details card view
            ChickenHouse selected = coopsTable.getSelectionModel().getSelectedItem();
            updateTelemetryDetails(selected);

            // 4. Update dynamic circular gauges
            updateRadialGauges(selected);

            // 5. Update trend chart view
            refreshChart();

            // 6. Update system clock time dynamically
            telemetryTime.setText("Real Time Sensor | " + java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
        }));
        telemetryTimeline.setCycleCount(Timeline.INDEFINITE);
        telemetryTimeline.play();
    }

    public void refreshData() {
        // Retrieve selection states before list recreation to prevent selection loss
        ChickenHouse selected = coopsTable.getSelectionModel().getSelectedItem();
        String selectedId = (selected != null) ? selected.getId() : null;

        // 1. Fetch live coop structures from Repository
        List<ChickenHouse> coops = FarmRepository.getAllCoops();
        coopsTable.setItems(FXCollections.observableArrayList(coops));

        // Restore table selection safely after recreation
        if (selectedId != null) {
            for (ChickenHouse coop : coopsTable.getItems()) {
                if (coop.getId().equals(selectedId)) {
                    coopsTable.getSelectionModel().select(coop);
                    break;
                }
            }
        }
    }
}
