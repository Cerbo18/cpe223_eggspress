package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.Main;
import cpe223.group8.eggspress.models.User;
import cpe223.group8.eggspress.repository.UserRepository;
import cpe223.group8.eggspress.services.NotificationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;

public class AcountMgmtController {

    @FXML
    private TableView<User> accountsTable;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> passwordColumn;

    @FXML
    private TextField newUsernameField;

    @FXML
    private TextField newPasswordField;

    @FXML
    public void initialize() {
        // Setup column bindings
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));

        // Load data
        refreshTable();
    }

    @FXML
    private void handleCreateAccount() {
        String username = newUsernameField.getText().trim();
        String password = newPasswordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            NotificationService.notificationWarning("Username and Password cannot be empty.", false, 2);
            return;
        }

        // Validate characters
        if (!username.matches("^[a-zA-Z0-9._@-]+$")) {
            NotificationService.notificationWarning("Username contains invalid characters. Only alphanumeric, dots, underscores, dashes, and @ are allowed.", false, 2);
            return;
        }

        if (!password.matches("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?]+$")) {
            NotificationService.notificationWarning("Password contains invalid characters. No spaces or control characters are allowed.", false, 2);
            return;
        }

        // Check if user already exists
        for (User u : UserRepository.getStaticUsers()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                NotificationService.notificationWarning("Account '" + username + "' already exists.", false, 2);
                return;
            }
        }

        // Save new user
        UserRepository.addStaticUser(new User(username, password));

        // Clear input fields
        newUsernameField.clear();
        newPasswordField.clear();

        // Refresh table view
        refreshTable();

        NotificationService.notificationInfo("Created new account: " + username);
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
}
