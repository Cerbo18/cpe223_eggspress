package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.models.User;
import cpe223.group8.eggspress.repository.UserRepository;
import cpe223.group8.eggspress.services.NotificationService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller to manage role modifications on existing user profiles
 * through a themed modal dialog stage.
 */
public class EditAccountModalController {

    @FXML
    private TextField usernameField;

    @FXML
    private ComboBox<String> roleSelector;

    @FXML
    public void initialize() {
        // Populates role choices for credential scopes inside the farm platform
        roleSelector.getItems().addAll("Staff", "Admin", "Manager");
    }

    /**
     * Initializes the view elements with the selected user parameters.
     */
    public void setUser(User user) {
        if (user != null) {
            usernameField.setText(user.getUsername());
            roleSelector.setValue(user.getRole());
        }
    }

    @FXML
    private void handleSave() {
        String username = usernameField.getText();
        String role = roleSelector.getValue();

        if (role == null || role.isEmpty()) {
            NotificationService.notificationWarning("A role must be selected.", false, 2);
            return;
        }

        // Persist role update changes
        UserRepository.updateStaticUserRole(username, role);

        NotificationService.notificationInfo("Updated role for " + username + " to: " + role);

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
