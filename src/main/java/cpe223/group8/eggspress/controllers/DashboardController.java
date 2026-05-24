package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.Main;
import javafx.fxml.FXML;
import java.io.IOException;
import javafx.event.ActionEvent;

public class DashboardController {

    @FXML
    private void handleAccountManagement() {
        try {
            Main.setRoot("acountMgmt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleInventoryManagement() {
        try {
            Main.setRoot("inventory");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAutomationManagement() {
        try {
            Main.setRoot("automation");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Main.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewLayout(ActionEvent event)throws IOException {
        Main.setRoot("layout");
    }
}
