package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.Main;
import cpe223.group8.eggspress.models.User;
import cpe223.group8.eggspress.repository.UserRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        boolean isAuthenticated = false;
        for (User user : UserRepository.getStaticUsers()) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                isAuthenticated = true;
                break;
            }
        }

        if (isAuthenticated) {
            try {
                Main.setRoot("dashboard");
            } catch (IOException e) {
                messageLabel.setText("Error loading dashboard view.");
                e.printStackTrace();
            }
        } else {
            messageLabel.setText("Invalid credentials");
        }
    }
}
