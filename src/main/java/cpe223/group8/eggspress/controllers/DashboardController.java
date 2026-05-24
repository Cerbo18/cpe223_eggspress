package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.Main;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.application.Platform;

public class DashboardController {

    @FXML
    private SplitPane splitPane;

    @FXML
    private VBox sidebar;

    @FXML
    private VBox sidebarContent;

    @FXML
    private Button toggleButton;

    private boolean sidebarExpanded = true;
    private double previousDividerPosition = 0.125; // Default expanded

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            if (splitPane != null) {
                splitPane.setDividerPosition(0, previousDividerPosition);
            }
        });
    }

    @FXML
    private void handleToggleSidebar() {
        sidebarExpanded = !sidebarExpanded;
        if (sidebarExpanded) {
            // Restore sidebar body visibility
            sidebarContent.setVisible(true);
            sidebarContent.setManaged(true);

            // Restore standard sidebar width constraints
            sidebar.setMinWidth(260.0);
            sidebar.setMaxWidth(400.0);

            // Restore divider to previous expanded position
            splitPane.setDividerPosition(0, previousDividerPosition);
        } else {
            // Capture current divider position before collapsing
            double[] positions = splitPane.getDividerPositions();
            if (positions.length > 0 && positions[0] > 0.08) {
                previousDividerPosition = positions[0];
            }

            // Hide the sidebar nav content entirely
            sidebarContent.setVisible(false);
            sidebarContent.setManaged(false);

            // Collapse sidebar to zero — no remnant strip since toggle is in HeaderBar
            sidebar.setMinWidth(0.0);
            sidebar.setMaxWidth(0.0);

            // Snap divider flush to the left edge
            splitPane.setDividerPosition(0, 0.0);
        }
    }

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
    private void handleViewLayout(ActionEvent event) throws IOException {
        Main.setRoot("layout");
    }
}
