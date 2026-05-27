package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.models.User;
import cpe223.group8.eggspress.repository.UserRepository;
import cpe223.group8.eggspress.services.NotificationService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller to handle validations and operations for creating a new user account
 * from the themed modal dialog stage.
 */
public class CreateAccountModalController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private ComboBox<String> roleSelector;

    @FXML
    public void initialize() {
        // Populates role choices for credential scopes inside the farm platform
        roleSelector.getItems().addAll("Staff", "Admin", "Manager");
        roleSelector.setValue("Staff");
    }

    @FXML
    private void handleCreate() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleSelector.getValue();

        // Validate empty states to prevent blank credentials in persistence
        if (username.isEmpty() || password.isEmpty()) {
            NotificationService.notificationWarning("Username and Password cannot be empty.", false, 2);
            return;
        }

        // Validate character sets to block injection or carriage return characters
        if (!username.matches("^[a-zA-Z0-9._@-]+$")) {
            NotificationService.notificationWarning("Username contains invalid characters. Only alphanumeric, dots, underscores, dashes, and @ are allowed.", false, 2);
            return;
        }

        if (!password.matches("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?]+$")) {
            NotificationService.notificationWarning("Password contains invalid characters. No spaces or control characters are allowed.", false, 2);
            return;
        }

        // Check if user already exists to prevent duplicate username constraints in database
        for (User u : UserRepository.getStaticUsers()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                NotificationService.notificationWarning("Account '" + username + "' already exists.", false, 2);
                return;
            }
        }

        // Save new user profile
        UserRepository.addStaticUser(new User(username, password, role));

        NotificationService.notificationInfo("Created new account: " + username);

        closeStage();
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void closeStage() {
        if (usernameField != null && usernameField.getScene() != null) {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }
}
