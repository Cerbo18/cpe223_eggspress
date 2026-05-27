package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.Main;
import cpe223.group8.eggspress.models.FeedingSchedule;
import cpe223.group8.eggspress.repository.FarmRepository;
import cpe223.group8.eggspress.services.NotificationService;
import cpe223.group8.eggspress.services.ThemeManager;
import cpe223.group8.eggspress.services.TooltipHelper;
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

    private static final String EDIT_PATH = "M7 7h-1a2 2 0 0 0 -2 2v9a2 2 0 0 0 2 2h9a2 2 0 0 0 2 -2v-1 M20.385 6.585a2.1 2.1 0 0 0 -2.97 -2.97l-8.415 8.385v3h3l8.385 -8.415 M16 5l3 3";
    private static final String DELETE_PATH = "M4 7h16 M5 7l1 12a2 2 0 0 0 2 2h8a2 2 0 0 0 2 -2l1 -12 M9 7v-3a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v3 M10 12l4 4m0 -4l-4 4";

    public void initialize() {
        // 1. Configure Table Column mappings
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        feedingTypeCol.setCellValueFactory(new PropertyValueFactory<>("feedingType"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 2. Setup Action Column cell factory for inline Edit and Delete actions
        actionCol.setCellFactory(col -> new TableCell<FeedingSchedule, Void>() {
            private final Button editBtn = new Button();
            private final Button deleteBtn = new Button();
            private final HBox container = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().addAll("button-secondary", "table-action-btn");
                editBtn.setGraphic(createSVGIcon(EDIT_PATH, "table-btn-icon"));
                TooltipHelper.installTooltip(editBtn, "Edit Schedule");
                editBtn.setOnAction(e -> {
                    FeedingSchedule item = getTableView().getItems().get(getIndex());
                    handleOpenEditScheduleModal(item);
                });

                deleteBtn.getStyleClass().addAll("button-danger", "table-action-btn");
                deleteBtn.setGraphic(createSVGIcon(DELETE_PATH, "table-btn-icon"));
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