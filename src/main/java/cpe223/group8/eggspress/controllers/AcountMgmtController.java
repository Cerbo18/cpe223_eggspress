package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.Main;
import cpe223.group8.eggspress.models.User;
import cpe223.group8.eggspress.repository.UserRepository;
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
    private Label feedbackLabel;

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
            feedbackLabel.setText("Username and Password cannot be empty");
            feedbackLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        // Check if user already exists
        for (User u : UserRepository.getStaticUsers()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                feedbackLabel.setText("Account already exists");
                feedbackLabel.setTextFill(javafx.scene.paint.Color.RED);
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

        feedbackLabel.setText("Account created successfully");
        feedbackLabel.setTextFill(javafx.scene.paint.Color.GREEN);
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            Main.setRoot("dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshTable() {
        ObservableList<User> list = FXCollections.observableArrayList(UserRepository.getStaticUsers());
        accountsTable.setItems(list);
    }
}
