package com.eggspress.controllers;

import com.eggspress.Main;
import javafx.fxml.FXML;
import java.io.IOException;

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
}
