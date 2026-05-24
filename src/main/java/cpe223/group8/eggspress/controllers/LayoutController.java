package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.Main;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import cpe223.group8.eggspress.models.FeedingSchedule;
import cpe223.group8.eggspress.models.InventoryItem;
import cpe223.group8.eggspress.repository.FarmRepository;
import java.io.IOException;
import javafx.event.ActionEvent;

public class LayoutController implements Initializable {

    @FXML
    private TableView<FeedingSchedule> ScheduleTable;

    @FXML
    private TableColumn<FeedingSchedule, String> CategoryCol;
    
    @FXML
    private TableColumn<FeedingSchedule, String> TimeCol;
    
    @FXML
    private TableColumn<FeedingSchedule, String> FeedingCol;
    
    @FXML
    private TableColumn<FeedingSchedule, String> StatusCol;

    @FXML
    private BarChart<String, Number> WaterChart; // Specified data types for clean charting

    @FXML
    private BarChart<String, Number> FeedChart;  // Specified data types for clean charting

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Configure Table Column bindings to match FeedingSchedule property fields
        CategoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        TimeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        FeedingCol.setCellValueFactory(new PropertyValueFactory<>("feedingType"));
        StatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 2. Load and render Data Elements
        refreshDashboardData();
    }    
    
    public void refreshDashboardData() {
        // --- POPULATE TABLE ---
        List<FeedingSchedule> schedules = FarmRepository.getAllSchedules();
        ScheduleTable.setItems(FXCollections.observableArrayList(schedules));

        // --- POPULATE CHARTS FROM LIVE SQLITE INVENTORY ---
        // Clear old chart data blocks to avoid overlapping duplicates on reload
        WaterChart.getData().clear();
        FeedChart.getData().clear();

        // Create discrete data series lines
        XYChart.Series<String, Number> waterSeries = new XYChart.Series<>();
        waterSeries.setName("Current Stock (L)");

        XYChart.Series<String, Number> feedSeries = new XYChart.Series<>();
        feedSeries.setName("Current Stock (kg)");

        // Fetch current live stock volumes
        List<InventoryItem> fullInventory = FarmRepository.getAllInventory();

        for (InventoryItem item : fullInventory) {
            String category = item.getCategory().toLowerCase();
            String name = item.getName().toLowerCase();

            // Filter logic matches categories safely
            if (category.equals("water") || category.equals("hydration") || name.equals("water")) {
                waterSeries.getData().add(new XYChart.Data<>(item.getName(), item.getQuantity()));
            } 
            else if (category.equals("feed") || category.equals("grain")) {
                feedSeries.getData().add(new XYChart.Data<>(item.getName(), item.getQuantity()));
            }
        }

        // Push data values onto the UI elements
        if (!waterSeries.getData().isEmpty()) {
            WaterChart.getData().add(waterSeries);
        }
        if (!feedSeries.getData().isEmpty()) {
            FeedChart.getData().add(feedSeries);
        }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        DashboardController dashboard = DashboardController.getInstance();
        if (dashboard != null) {
            dashboard.loadView("overview");
        } else {
            Main.setRoot("dashboard");
        }
    }
}