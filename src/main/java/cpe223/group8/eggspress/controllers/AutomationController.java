package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.Main;
import cpe223.group8.eggspress.models.FeedingSchedule;
import cpe223.group8.eggspress.repository.FarmRepository;
import cpe223.group8.eggspress.services.NotificationService;
import cpe223.group8.eggspress.services.ThemeManager;
import cpe223.group8.eggspress.services.TooltipHelper;
import cpe223.group8.eggspress.services.SvgIconHelper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.scene.Group;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.util.List;

public class AutomationController {

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

    @FXML
    private TableColumn<FeedingSchedule, Void> actionCol;

    private Timeline telemetryPoller;

    public void initialize() {
        // 1. Configure Table Column mappings
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        feedingTypeCol.setCellValueFactory(new PropertyValueFactory<>("feedingType"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Custom status capsule styling matching the Inventory Management standard
        statusCol.setCellFactory(col -> new TableCell<FeedingSchedule, String>() {
            private final Label statusLabel = new Label();
            {
                statusLabel.getStyleClass().add("status-capsule");
                setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    statusLabel.setText(item);
                    statusLabel.getStyleClass().removeAll("status-optimal", "status-monitoring", "status-critical", "status-inactive");
                    if ("Completed".equalsIgnoreCase(item)) {
                        statusLabel.getStyleClass().add("status-optimal");
                    } else if ("Pending".equalsIgnoreCase(item)) {
                        statusLabel.getStyleClass().add("status-monitoring");
                    } else if ("Scheduled".equalsIgnoreCase(item)) {
                        statusLabel.getStyleClass().add("status-inactive");
                    } else if ("Failed".equalsIgnoreCase(item)) {
                        statusLabel.getStyleClass().add("status-critical");
                    }
                    setGraphic(statusLabel);
                }
            }
        });


        // 2. Setup Action Column cell factory for inline Edit and Delete actions
        actionCol.setCellFactory(col -> new TableCell<FeedingSchedule, Void>() {
            private final Button editBtn = new Button();
            private final Button deleteBtn = new Button();
            private final HBox container = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().addAll("button-secondary", "table-action-btn");
                editBtn.setGraphic(SvgIconHelper.createTableActionIcon(SvgIconHelper.IconType.EDIT, "table-btn-icon"));
                TooltipHelper.installTooltip(editBtn, "Edit Schedule");
                editBtn.setOnAction(e -> {
                    FeedingSchedule item = getTableView().getItems().get(getIndex());
                    handleOpenEditScheduleModal(item);
                });

                deleteBtn.getStyleClass().addAll("button-danger", "table-action-btn");
                deleteBtn.setGraphic(SvgIconHelper.createTableActionIcon(SvgIconHelper.IconType.DELETE, "table-btn-icon"));
                TooltipHelper.installTooltip(deleteBtn, "Delete Schedule");
                deleteBtn.setOnAction(e -> {
                    FeedingSchedule item = getTableView().getItems().get(getIndex());
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

        // 3. Force-Seed fresh simulations if no manual items exist
        seedSimulatedSchedules();

        // 4. Fetch and render schedules to the table view initially
        refreshScheduleTable();

        // 5. Setup a dynamic 2-second poller to keep the schedule table updated
        telemetryPoller = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            refreshScheduleTable();
        }));
        telemetryPoller.setCycleCount(Timeline.INDEFINITE);
        telemetryPoller.play();
    }

    private void seedSimulatedSchedules() {
        List<FeedingSchedule> existingSchedules = FarmRepository.getAllSchedules();
        
        // If it contains <= 1 schedules, seed standard ones to ensure nice starter state
        if (existingSchedules.size() <= 1) {
            System.out.println("Seeding fresh automated simulations into SQLite...");
            
            FarmRepository.addSchedule(new FeedingSchedule("Water", "06:00", "Automated Refill", "Completed"));
            FarmRepository.addSchedule(new FeedingSchedule("Grains", "08:30", "Standard Broiler Feed", "Completed"));
            FarmRepository.addSchedule(new FeedingSchedule("Feed", "12:00", "High-Protein Mix", "Pending"));
            FarmRepository.addSchedule(new FeedingSchedule("Water", "16:30", "Hydration Top-Up", "Scheduled"));
        }
    }

    private void refreshScheduleTable() {
        scheduleTable.setItems(FXCollections.observableArrayList(FarmRepository.getAllSchedules()));
    }

    @FXML
    private void handleOpenCreateScheduleModal() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/createScheduleModal.fxml"));
            Parent root = loader.load();
            ThemeManager.applyTheme(root);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            if (scheduleTable != null && scheduleTable.getScene() != null) {
                stage.initOwner(scheduleTable.getScene().getWindow());
            }
            stage.setTitle("Add Feeding Schedule");
            stage.setResizable(false);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            ThemeManager.applySceneFill(scene);
            stage.setScene(scene);
            stage.showAndWait();

            // Refresh table view after popup closes
            refreshScheduleTable();
        } catch (IOException e) {
            System.err.println("Error loading create schedule modal stage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleOpenEditScheduleModal(FeedingSchedule schedule) {
        if (schedule == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/editScheduleModal.fxml"));
            Parent root = loader.load();
            ThemeManager.applyTheme(root);

            EditScheduleModalController controller = loader.getController();
            controller.setSchedule(schedule);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            if (scheduleTable != null && scheduleTable.getScene() != null) {
                stage.initOwner(scheduleTable.getScene().getWindow());
            }
            stage.setTitle("Edit Feeding Schedule");
            stage.setResizable(false);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            ThemeManager.applySceneFill(scene);
            stage.setScene(scene);
            stage.showAndWait();

            // Refresh table view after popup closes
            refreshScheduleTable();
        } catch (IOException e) {
            System.err.println("Error loading edit schedule modal stage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleConfirmDelete(FeedingSchedule schedule) {
        if (schedule == null) return;

        // Display a themed confirmation alert box to verify deletion approval
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        ThemeManager.applyTheme(alert.getDialogPane());
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Feeding Schedule");
        alert.setContentText(String.format("Are you sure you want to delete '%s' permanently?", schedule.getFeedingType()));

        if (scheduleTable != null && scheduleTable.getScene() != null) {
            alert.initOwner(scheduleTable.getScene().getWindow());
        }

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = FarmRepository.removeSchedule(schedule);
                if (success) {
                    NotificationService.notificationInfo(String.format("Deleted schedule: %s at %s.", schedule.getCategory(), schedule.getTime()));
                    refreshScheduleTable();
                } else {
                    NotificationService.notificationWarning("Failed to delete schedule record from database.", false, 2);
                }
            }
        });
    }

    @FXML
    private void handleBackToDashboard() {
        if (telemetryPoller != null) {
            telemetryPoller.stop();
        }
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

}