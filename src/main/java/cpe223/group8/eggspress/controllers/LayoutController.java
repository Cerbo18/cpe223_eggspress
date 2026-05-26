package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.Main;
import cpe223.group8.eggspress.models.ChickenHouse;
import cpe223.group8.eggspress.models.ChickenGrowthPoint;
import cpe223.group8.eggspress.repository.FarmRepository;
import cpe223.group8.eggspress.services.NotificationService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class LayoutController implements Initializable {

    @FXML
    private FlowPane CoopsContainer;

    @FXML
    private VBox MonitoringCard;

    @FXML
    private VBox NoSelectionPane;

    @FXML
    private VBox TelemetryPane;

    @FXML
    private Label SelectedCoopName;

    @FXML
    private Label SelectedCoopStatus;

    @FXML
    private Label TempMetric;

    @FXML
    private Label HumidMetric;

    @FXML
    private Label FlockMetric;

    @FXML
    private Label VentMetric;

    @FXML
    private Button DeleteCoopBtn;

    @FXML
    private Button UpdateCoopHeaderBtn;

    @FXML
    private Button AddCoopToggleBtn;

    @FXML
    private VBox AddCoopForm;

    @FXML
    private TextField NewCoopIdField;

    @FXML
    private TextField NewCoopNameField;

    @FXML
    private TextField NewFlockCountField;

    @FXML
    private ComboBox<String> NewCoopStatusCombo;

    @FXML
    private Label FormTitleLabel;

    @FXML
    private Button SaveCoopBtn;

    @FXML
    private VBox NewFlockCountContainer;

    @FXML
    private VBox UpdateFlockForm;

    @FXML
    private TextField FlockCoopNameField;

    @FXML
    private TextField FlockCurrentCountField;

    @FXML
    private ComboBox<String> FlockUpdateTypeCombo;

    @FXML
    private TextField FlockDeltaField;

    @FXML
    private DatePicker FlockUpdateDatePicker;

    @FXML
    private Label FlockFormTitleLabel;

    @FXML
    private Button SaveFlockUpdateBtn;

    @FXML
    private DatePicker StartDatePicker;

    @FXML
    private DatePicker EndDatePicker;

    @FXML
    private LineChart<String, Number> GrowthChart;

    @FXML
    private CategoryAxis GrowthXAxis;

    @FXML
    private NumberAxis GrowthYAxis;

    private ChickenHouse selectedCoop = null;
    private boolean isEditMode = false;
    private final Random random = new Random();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup Coop Status choices
        NewCoopStatusCombo.setItems(FXCollections.observableArrayList("Optimal", "Monitoring", "Inactive"));
        NewCoopStatusCombo.getSelectionModel().selectFirst();

        // Setup Flock Update Type choices
        FlockUpdateTypeCombo.setItems(FXCollections.observableArrayList("New Chickens Born / Added", "Chickens Lost / Died"));
        FlockUpdateTypeCombo.getSelectionModel().selectFirst();

        // Setup default date ranges: 2021-01-01 to today
        StartDatePicker.setValue(LocalDate.of(2021, 1, 1));
        EndDatePicker.setValue(LocalDate.now());

        // Disable header update button until a coop is selected
        if (UpdateCoopHeaderBtn != null) {
            UpdateCoopHeaderBtn.setDisable(true);
        }

        // Initial loading of coops list and growth history
        refreshCoopsMap();
        refreshGrowthChart();
    }

    private void refreshCoopsMap() {
        CoopsContainer.getChildren().clear();
        List<ChickenHouse> coops = FarmRepository.getAllCoops();

        for (ChickenHouse coop : coops) {
            VBox coopCard = new VBox();
            coopCard.getStyleClass().add("facility-badge");
            
            // Set styles based on selection
            if (selectedCoop != null && selectedCoop.getId().equals(coop.getId())) {
                coopCard.getStyleClass().add("selected-card");
            }

            Label nameLabel = new Label(coop.getName());
            nameLabel.getStyleClass().add("facility-name");

            Label statusLabel = new Label("Status: " + coop.getStatus());
            if ("Optimal".equalsIgnoreCase(coop.getStatus())) {
                statusLabel.getStyleClass().add("facility-status-optimal");
            } else if ("Monitoring".equalsIgnoreCase(coop.getStatus())) {
                statusLabel.getStyleClass().add("facility-status-monitoring");
            } else {
                statusLabel.getStyleClass().add("facility-status-inactive");
            }

            Label flockLabel = new Label(coop.getFlockCount() + " Birds");
            flockLabel.getStyleClass().add("facility-flock");

            coopCard.getChildren().addAll(nameLabel, statusLabel, flockLabel);
            coopCard.setOnMouseClicked(e -> selectCoop(coop));

            CoopsContainer.getChildren().add(coopCard);
        }
    }

    private void selectCoop(ChickenHouse coop) {
        selectedCoop = coop;
        
        // Update views visibility
        NoSelectionPane.setVisible(false);
        NoSelectionPane.setManaged(false);
        TelemetryPane.setVisible(true);
        TelemetryPane.setManaged(true);

        // Populate selected coop details
        SelectedCoopName.setText(coop.getName());
        SelectedCoopStatus.setText(coop.getStatus().toUpperCase());
        SelectedCoopStatus.getStyleClass().removeAll("optimal", "monitoring", "inactive");
        SelectedCoopStatus.getStyleClass().add(coop.getStatus().toLowerCase());

        FlockMetric.setText(coop.getFlockCount() + " Birds");

        // Simulate sensor telemetry based on coop status
        double baseTemp = 24.2;
        double baseHumid = 60.0;
        String ventStatus = "ACTIVE";

        if ("Monitoring".equalsIgnoreCase(coop.getStatus())) {
            baseTemp = 28.5; // elevated temp
            baseHumid = 72.0; // high humidity
            ventStatus = "MAX SPEED";
        } else if ("Inactive".equalsIgnoreCase(coop.getStatus())) {
            baseTemp = 18.0; // cool
            baseHumid = 45.0; // dry
            ventStatus = "STANDBY";
        }

        // Random jitter for realism
        double temp = baseTemp + (random.nextDouble() - 0.5) * 1.5;
        double humid = baseHumid + (random.nextDouble() - 0.5) * 5.0;

        TempMetric.setText(String.format("%.1f °C", temp));
        HumidMetric.setText(String.format("%.1f %%", humid));
        VentMetric.setText(ventStatus);

        // Enable header update button now that a coop is selected
        if (UpdateCoopHeaderBtn != null) {
            UpdateCoopHeaderBtn.setDisable(false);
        }

        // Highlight selected card in container
        refreshCoopsMap();
    }

    @FXML
    private void handleDeleteCoop() {
        if (selectedCoop == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Remove Coop Blueprint");
        confirmAlert.setHeaderText("Remove " + selectedCoop.getName() + "?");
        confirmAlert.setContentText("This will permanently remove this coop and delete all active telemetry configurations from the sqlite database.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = FarmRepository.removeCoop(selectedCoop.getId());
                if (success) {
                    NotificationService.notificationInfo("Coop " + selectedCoop.getName() + " deleted successfully.");
                    selectedCoop = null;
                    TelemetryPane.setVisible(false);
                    TelemetryPane.setManaged(false);
                    NoSelectionPane.setVisible(true);
                    NoSelectionPane.setManaged(true);
                    
                    // Disable header update button since no coop is selected anymore
                    if (UpdateCoopHeaderBtn != null) {
                        UpdateCoopHeaderBtn.setDisable(true);
                    }

                    refreshCoopsMap();
                } else {
                    NotificationService.notificationWarning("Failed to remove coop from database.", false, 2);
                }
            }
        });
    }

    @FXML
    private void toggleAddForm(ActionEvent event) {
        isEditMode = false;
        FormTitleLabel.setText("New Chicken Coop Registration");
        SaveCoopBtn.setText("Save Coop Blueprint");
        NewCoopIdField.setDisable(false);
        
        NewCoopIdField.clear();
        NewCoopNameField.clear();
        NewFlockCountField.clear();
        NewCoopStatusCombo.getSelectionModel().selectFirst();

        NewFlockCountContainer.setVisible(true);
        NewFlockCountContainer.setManaged(true);

        MonitoringCard.setVisible(false);
        MonitoringCard.setManaged(false);
        UpdateFlockForm.setVisible(false);
        UpdateFlockForm.setManaged(false);
        AddCoopForm.setVisible(true);
        AddCoopForm.setManaged(true);
    }

    @FXML
    private void handleEditCoop(ActionEvent event) {
        if (selectedCoop == null) return;
        isEditMode = true;
        FormTitleLabel.setText("Edit Coop Details: " + selectedCoop.getId());
        SaveCoopBtn.setText("Update Coop Details");
        NewCoopIdField.setDisable(true);
        
        NewCoopIdField.setText(selectedCoop.getId());
        NewCoopNameField.setText(selectedCoop.getName());
        NewCoopStatusCombo.getSelectionModel().select(selectedCoop.getStatus());

        NewFlockCountContainer.setVisible(false);
        NewFlockCountContainer.setManaged(false);

        MonitoringCard.setVisible(false);
        MonitoringCard.setManaged(false);
        UpdateFlockForm.setVisible(false);
        UpdateFlockForm.setManaged(false);
        AddCoopForm.setVisible(true);
        AddCoopForm.setManaged(true);
    }

    @FXML
    private void cancelAddForm(ActionEvent event) {
        AddCoopForm.setVisible(false);
        AddCoopForm.setManaged(false);
        MonitoringCard.setVisible(true);
        MonitoringCard.setManaged(true);
    }

    @FXML
    private void saveNewCoop(ActionEvent event) {
        String id = NewCoopIdField.getText().trim();
        String name = NewCoopNameField.getText().trim();
        String status = NewCoopStatusCombo.getValue();

        if (id.isEmpty() || name.isEmpty() || status == null) {
            NotificationService.notificationWarning("All blueprint fields are required.", false, 2);
            return;
        }

        if (!id.matches("^[a-zA-Z0-9_-]+$")) {
            NotificationService.notificationWarning("Coop ID contains invalid characters.", false, 2);
            return;
        }

        int flockCount = 0;
        if (isEditMode) {
            flockCount = selectedCoop.getFlockCount();
        } else {
            String flockStr = NewFlockCountField.getText().trim();
            if (flockStr.isEmpty()) {
                NotificationService.notificationWarning("Starting flock count is required.", false, 2);
                return;
            }
            try {
                flockCount = Integer.parseInt(flockStr);
                if (flockCount < 0) {
                    NotificationService.notificationWarning("Flock count cannot be negative.", false, 2);
                    return;
                }
            } catch (NumberFormatException e) {
                NotificationService.notificationWarning("Please enter a valid flock number.", false, 2);
                return;
            }
        }

        ChickenHouse coopObj = new ChickenHouse(id, name, flockCount, status);
        boolean success;
        
        if (isEditMode) {
            success = FarmRepository.updateCoop(coopObj);
        } else {
            success = FarmRepository.addCoop(coopObj);
        }

        if (success) {
            if (isEditMode) {
                NotificationService.notificationInfo("Coop details updated successfully.");
            } else {
                NotificationService.notificationInfo("New Coop registered successfully.");
            }
            
            // Reset fields
            NewCoopIdField.clear();
            NewCoopNameField.clear();
            NewFlockCountField.clear();
            NewCoopStatusCombo.getSelectionModel().selectFirst();

            // Return to monitoring view
            cancelAddForm(null);
            
            // Select newly added/updated coop
            selectCoop(coopObj);
        } else {
            if (isEditMode) {
                NotificationService.notificationWarning("Failed to update coop details in SQLite database.", false, 2);
            } else {
                NotificationService.notificationWarning("Failed to save coop. Ensure Unique ID isn't duplicated.", false, 2);
            }
        }
    }

    @FXML
    private void handleUpdateFlock(ActionEvent event) {
        if (selectedCoop == null) return;

        FlockCoopNameField.setText(selectedCoop.getName());
        FlockCurrentCountField.setText(String.valueOf(selectedCoop.getFlockCount()));
        FlockDeltaField.clear();
        FlockUpdateDatePicker.setValue(LocalDate.now());

        MonitoringCard.setVisible(false);
        MonitoringCard.setManaged(false);
        AddCoopForm.setVisible(false);
        AddCoopForm.setManaged(false);
        UpdateFlockForm.setVisible(true);
        UpdateFlockForm.setManaged(true);
    }

    @FXML
    private void cancelFlockForm(ActionEvent event) {
        UpdateFlockForm.setVisible(false);
        UpdateFlockForm.setManaged(false);
        MonitoringCard.setVisible(true);
        MonitoringCard.setManaged(true);
    }

    @FXML
    private void saveFlockUpdate(ActionEvent event) {
        if (selectedCoop == null) return;

        String deltaStr = FlockDeltaField.getText().trim();
        if (deltaStr.isEmpty()) {
            NotificationService.notificationWarning("Number of chickens is required.", false, 2);
            return;
        }

        int delta;
        try {
            delta = Integer.parseInt(deltaStr);
            if (delta <= 0) {
                NotificationService.notificationWarning("Number of chickens must be positive.", false, 2);
                return;
            }
        } catch (NumberFormatException e) {
            NotificationService.notificationWarning("Please enter a valid number of chickens.", false, 2);
            return;
        }

        String updateType = FlockUpdateTypeCombo.getValue();
        if (updateType == null) {
            NotificationService.notificationWarning("Please select an update type.", false, 2);
            return;
        }

        int newFlockCount;
        if ("New Chickens Born / Added".equals(updateType)) {
            newFlockCount = selectedCoop.getFlockCount() + delta;
        } else {
            newFlockCount = selectedCoop.getFlockCount() - delta;
            if (newFlockCount < 0) {
                NotificationService.notificationWarning("Flock count cannot drop below zero. Current count: " + selectedCoop.getFlockCount(), false, 2);
                return;
            }
        }

        LocalDate dateVal = FlockUpdateDatePicker.getValue();
        if (dateVal == null) {
            NotificationService.notificationWarning("Please specify a valid event date.", false, 2);
            return;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateStr = dateVal.format(dtf);

        boolean success = FarmRepository.updateCoopFlockCountAndLogHistory(selectedCoop.getId(), newFlockCount, dateStr);
        if (success) {
            NotificationService.notificationInfo("Flock size updated successfully.");
            selectedCoop.setFlockCount(newFlockCount);
            
            // Return to monitoring view
            cancelFlockForm(null);

            // Refresh selections, list, and chart
            selectCoop(selectedCoop);
            refreshGrowthChart();
        } else {
            NotificationService.notificationWarning("Failed to commit flock update to SQLite database.", false, 2);
        }
    }

    @FXML
    private void handleFilterGrowth(ActionEvent event) {
        refreshGrowthChart();
    }

    private void refreshGrowthChart() {
        LocalDate start = StartDatePicker.getValue();
        LocalDate end = EndDatePicker.getValue();

        if (start == null || end == null) {
            NotificationService.notificationWarning("Please specify both date boundaries.", false, 2);
            return;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startStr = start.format(dtf);
        String endStr = end.format(dtf);

        List<ChickenGrowthPoint> points = FarmRepository.getGrowthHistory(startStr, endStr);

        GrowthChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Flock Size over Years");

        for (ChickenGrowthPoint pt : points) {
            series.getData().add(new XYChart.Data<>(pt.getRecordDate(), pt.getFlockCount()));
        }

        GrowthChart.getData().add(series);
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