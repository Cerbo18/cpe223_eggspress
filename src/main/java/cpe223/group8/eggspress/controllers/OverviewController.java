package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.models.ChickenHouse;
import cpe223.group8.eggspress.models.InventoryItem;
import cpe223.group8.eggspress.repository.FarmRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class OverviewController {

    @FXML
    private Label coopsStat;

    @FXML
    private Label coopsIndicator;

    @FXML
    private Label flockStat;

    @FXML
    private Label flockIndicator;

    @FXML
    private Label feedStat;

    @FXML
    private Label feedIndicator;

    @FXML
    private Label waterStat;

    @FXML
    private Label waterIndicator;

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
    public void initialize() {
        // Bind table column cell factories to ChickenHouse properties
        coopIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        coopNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        coopFlockCol.setCellValueFactory(new PropertyValueFactory<>("flockCount"));
        coopStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add beautiful custom status styling to table cells
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

        // Query the live SQLite database to populate dynamic components
        refreshData();
    }

    public void refreshData() {
        // 1. Fetch live coop structures
        List<ChickenHouse> coops = FarmRepository.getAllCoops();
        coopsTable.setItems(FXCollections.observableArrayList(coops));

        // 2. Calculate farm statistics dynamically
        int totalCoops = coops.size();
        int activeCoops = 0;
        int totalFlock = 0;
        
        for (ChickenHouse coop : coops) {
            totalFlock += coop.getFlockCount();
            if (!"Inactive".equalsIgnoreCase(coop.getStatus())) {
                activeCoops++;
            }
        }
        
        coopsStat.setText(totalCoops + " Coops");
        
        if (totalCoops > 0) {
            int pct = (activeCoops * 100) / totalCoops;
            coopsIndicator.setText("● " + pct + "% Operational");
            coopsIndicator.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");
            if (pct >= 90) {
                coopsIndicator.getStyleClass().add("status-optimal");
            } else if (pct >= 50) {
                coopsIndicator.getStyleClass().add("status-monitoring");
            } else {
                coopsIndicator.getStyleClass().add("status-inactive");
            }
        } else {
            coopsIndicator.setText("● 0% Operational");
            coopsIndicator.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");
            coopsIndicator.getStyleClass().add("status-inactive");
        }

        flockStat.setText(String.format("%,d Birds", totalFlock));
        flockIndicator.setText("▲ Stable Stock Level");

        // 3. Fetch live feed and hydration levels from inventory
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

        feedStat.setText(String.format("%,.0f kg", totalFeed));
        feedIndicator.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");
        if (totalFeed > 200) {
            feedIndicator.setText("● Optimal Stock");
            feedIndicator.getStyleClass().add("status-optimal");
        } else {
            feedIndicator.setText("⚠ Low Stock Warning");
            feedIndicator.getStyleClass().add("status-inactive");
        }

        waterStat.setText(String.format("%,.0f L", totalWater));
        waterIndicator.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-inactive");
        if (totalWater > 500) {
            waterIndicator.setText("● Optimal Stock");
            waterIndicator.getStyleClass().add("status-optimal");
        } else {
            waterIndicator.setText("⚠ Low Stock Warning");
            waterIndicator.getStyleClass().add("status-inactive");
        }
    }
}
