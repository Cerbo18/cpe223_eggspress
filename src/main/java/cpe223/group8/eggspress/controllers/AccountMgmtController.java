package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.Main;
import cpe223.group8.eggspress.models.User;
import cpe223.group8.eggspress.repository.UserRepository;
import cpe223.group8.eggspress.services.NotificationService;
import cpe223.group8.eggspress.services.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Controller to handle account management TableViews, password masking/unmasking,
 * user deletions, and invoking modal screens.
 */
public class AccountMgmtController {

    // User-provided SVG Path constants for pixel-perfect dpi-independent icon rendering
    private static final String EYE_PATH = "M10 12a2 2 0 1 0 4 0a2 2 0 0 0 -4 0 M21 12c-2.4 4 -5.4 6 -9 6c-3.6 0 -6.6 -2 -9 -6c2.4 -4 5.4 -6 9 -6c3.6 0 6.6 2 9 6";
    private static final String EYE_OFF_PATH_1 = "M10.585 10.587a2 2 0 0 0 2.829 2.828";
    private static final String EYE_OFF_PATH_2 = "M16.681 16.673a8.717 8.717 0 0 1 -4.681 1.327c-3.6 0 -6.6 -2 -9 -6c1.272 -2.12 2.712 -3.678 4.32 -4.674m2.86 -1.146a9.055 9.055 0 0 1 1.82 -.18c3.6 0 6.6 2 9 6c-.666 1.11 -1.379 2.067 -2.138 2.87";
    private static final String EYE_OFF_PATH_3 = "M3 3l18 18";

    private static final String EDIT_PATH_1 = "M7 7h-1a2 2 0 0 0 -2 2v9a2 2 0 0 0 2 2h9a2 2 0 0 0 2 -2v-1";
    private static final String EDIT_PATH_2 = "M20.385 6.585a2.1 2.1 0 0 0 -2.97 -2.97l-8.415 8.385v3h3l8.385 -8.415";
    private static final String EDIT_PATH_3 = "M16 5l3 3";

    private static final String TRASH_PATH_1 = "M4 7h16";
    private static final String TRASH_PATH_2 = "M5 7l1 12a2 2 0 0 0 2 2h8a2 2 0 0 0 2 -2l1 -12";
    private static final String TRASH_PATH_3 = "M9 7v-3a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v3";
    private static final String TRASH_PATH_4 = "M10 12l4 4m0 -4l-4 4";

    @FXML
    private TableView<User> accountsTable;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> passwordColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, Void> actionColumn;

    // Stores names of users whose password string is temporarily toggled to plain text
    private final Set<String> revealedUsernames = new HashSet<>();

    @FXML
    public void initialize() {
        // Setup column bindings mapped to getters in User model
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Configure custom CellFactory for Password unmasking toggle buttons
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        passwordColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String password, boolean empty) {
                super.updateItem(password, empty);
                if (empty || password == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    HBox container = new HBox();
                    container.getStyleClass().add("password-cell-container");

                    Label label = new Label();
                    label.getStyleClass().add("password-text-label");

                    Button toggleBtn = new Button();
                    toggleBtn.getStyleClass().add("password-toggle-btn");

                    // Bind visual display depending on unmasked state cache
                    if (revealedUsernames.contains(user.getUsername())) {
                        label.setText(password);
                        toggleBtn.setGraphic(createSvgIcon(EYE_OFF_PATH_1, EYE_OFF_PATH_2, EYE_OFF_PATH_3));
                        cpe223.group8.eggspress.services.TooltipHelper.installTooltip(toggleBtn, "Mask password characters");
                    } else {
                        label.setText("••••••••");
                        toggleBtn.setGraphic(createSvgIcon(EYE_PATH));
                        cpe223.group8.eggspress.services.TooltipHelper.installTooltip(toggleBtn, "Reveal password characters");
                    }

                    toggleBtn.setOnAction(event -> {
                        if (revealedUsernames.contains(user.getUsername())) {
                            revealedUsernames.remove(user.getUsername());
                        } else {
                            revealedUsernames.add(user.getUsername());
                        }
                        // Request table refresh to trigger updateItem binding pass
                        getTableView().refresh();
                    });

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    container.getChildren().addAll(label, spacer, toggleBtn);
                    setGraphic(container);
                    setText(null);
                }
            }
        });

        // Configure custom CellFactory for Edit and Delete action controls
        actionColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    HBox container = new HBox();
                    container.getStyleClass().add("action-cell-container");

                    // 1. Edit Role action button
                    Button editBtn = new Button();
                    editBtn.getStyleClass().add("action-btn-circle");
                    editBtn.setGraphic(createSvgIcon(EDIT_PATH_1, EDIT_PATH_2, EDIT_PATH_3));
                    cpe223.group8.eggspress.services.TooltipHelper.installTooltip(editBtn, "Edit user permission role");
                    editBtn.setOnAction(event -> handleShowEditPopup(user));

                    // 2. Delete User action button with safety lockout guards
                    Button deleteBtn = new Button();
                    deleteBtn.getStyleClass().add("delete-action-btn");
                    deleteBtn.setGraphic(createSvgIcon(TRASH_PATH_1, TRASH_PATH_2, TRASH_PATH_3, TRASH_PATH_4));
                    
                    String currentUsername = SessionManager.getCurrentUsername();
                    if (user.getUsername().equalsIgnoreCase(currentUsername)) {
                        deleteBtn.setDisable(true);
                        cpe223.group8.eggspress.services.TooltipHelper.installTooltip(deleteBtn, "Self-deletion is blocked to prevent admin lockout states");
                    } else {
                        cpe223.group8.eggspress.services.TooltipHelper.installTooltip(deleteBtn, "Permanently remove user credentials");
                        deleteBtn.setOnAction(event -> handleDeleteUser(user));
                    }

                    container.getChildren().addAll(editBtn, deleteBtn);
                    setGraphic(container);
                }
            }
        });

        // Load data on startup
        refreshTable();
    }

    @FXML
    private void handleShowCreatePopup() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(Main.class.getResource("views/createAccountModal.fxml"));
            javafx.scene.Parent root = loader.load();
            
            // Apply visual theme matching dynamically
            cpe223.group8.eggspress.services.ThemeManager.applyTheme(root);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            if (accountsTable.getScene() != null && accountsTable.getScene().getWindow() != null) {
                stage.initOwner(accountsTable.getScene().getWindow());
            }
            stage.setTitle("Create New Account");
            stage.setResizable(false);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            cpe223.group8.eggspress.services.ThemeManager.applySceneFill(scene);
            stage.setScene(scene);
            stage.showAndWait();

            // Refresh table dynamically when the modal is closed
            refreshTable();
        } catch (IOException e) {
            e.printStackTrace();
            NotificationService.notificationWarning("Error loading account creation modal: " + e.getMessage(), false, 2);
        }
    }

    private void handleShowEditPopup(User user) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(Main.class.getResource("views/editAccountModal.fxml"));
            javafx.scene.Parent root = loader.load();
            
            // Apply visual theme matching dynamically
            cpe223.group8.eggspress.services.ThemeManager.applyTheme(root);

            // Populate the controller parameters with selected user parameters
            EditAccountModalController controller = loader.getController();
            controller.setUser(user);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            if (accountsTable.getScene() != null && accountsTable.getScene().getWindow() != null) {
                stage.initOwner(accountsTable.getScene().getWindow());
            }
            stage.setTitle("Edit Account Role");
            stage.setResizable(false);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            cpe223.group8.eggspress.services.ThemeManager.applySceneFill(scene);
            stage.setScene(scene);
            stage.showAndWait();

            // Refresh table dynamically when the modal is closed
            refreshTable();
        } catch (IOException e) {
            e.printStackTrace();
            NotificationService.notificationWarning("Error loading role edit modal: " + e.getMessage(), false, 2);
        }
    }

    private void handleDeleteUser(User user) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Account '" + user.getUsername() + "'");
        alert.setContentText("Are you sure you want to permanently delete this account? This action cannot be undone.");

        if (accountsTable.getScene() != null && accountsTable.getScene().getWindow() != null) {
            alert.initOwner(accountsTable.getScene().getWindow());
        }

        // Apply active CSS stylesheets to dialog pane dynamically to adapt to current theme mode
        javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
        cpe223.group8.eggspress.services.ThemeManager.applyTheme(dialogPane);
        dialogPane.getStyleClass().add("content-card");

        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            UserRepository.deleteStaticUser(user.getUsername());
            NotificationService.notificationInfo("Deleted account: " + user.getUsername());
            refreshTable();
        }
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
        ObservableList<User> list = FXCollections.observableArrayList(UserRepository.getStaticUsers());
        accountsTable.setItems(list);
    }

    // Creates SVG group graphics to preserve crisp outlines across varying DPI scalings
    private javafx.scene.Group createSvgIcon(String... paths) {
        javafx.scene.Group group = new javafx.scene.Group();
        // Scale elements to keep table cell actions reasonably proportioned
        group.setScaleX(0.7);
        group.setScaleY(0.7);
        for (String path : paths) {
            javafx.scene.shape.SVGPath svgPath = new javafx.scene.shape.SVGPath();
            svgPath.setContent(path);
            svgPath.getStyleClass().add("table-svg-icon");
            group.getChildren().add(svgPath);
        }
        return group;
    }
}
