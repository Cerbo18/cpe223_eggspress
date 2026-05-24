package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.application.Platform;

public class DashboardController {

    private static DashboardController instance;

    public static DashboardController getInstance() {
        return instance;
    }

    @FXML
    private SplitPane splitPane;

    @FXML
    private VBox sidebar;

    @FXML
    private VBox sidebarContent;

    @FXML
    private Button toggleButton;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button overviewBtn;

    @FXML
    private Button accountMgmtBtn;

    @FXML
    private Button inventoryBtn;

    @FXML
    private Button automationBtn;

    @FXML
    private Button layoutBtn;

    private boolean sidebarExpanded = true;
    private double previousDividerPosition = 0.125; // Default expanded

    public DashboardController() {
        instance = this;
    }

    private void clearActiveStyle(Button btn) {
        if (btn != null) {
            while (btn.getStyleClass().remove("active"));
        }
    }

    private void setActiveStyle(Button btn) {
        if (btn != null) {
            clearActiveStyle(btn); // Ensure no remnants first
            btn.getStyleClass().add("active");
        }
    }

    @FXML
    public void initialize() {
        // Set overview button as active by default on startup safely
        setActiveStyle(overviewBtn);
        loadView("overview");
        Platform.runLater(() -> {
            if (splitPane != null) {
                splitPane.setDividerPosition(0, previousDividerPosition);
            }
        });
    }

    /**
     * Loads the specified FXML sub-view into the SplitPane content area.
     * If fxmlName is "overview", it restores the default dashboard overview.
     */
    public void loadView(String fxmlName) {
        // Reset active style class safely for all sidebar buttons
        clearActiveStyle(overviewBtn);
        clearActiveStyle(accountMgmtBtn);
        clearActiveStyle(inventoryBtn);
        clearActiveStyle(automationBtn);
        clearActiveStyle(layoutBtn);

        // Set the current view button as active
        if ("overview".equals(fxmlName)) {
            setActiveStyle(overviewBtn);
        } else if ("acountMgmt".equals(fxmlName)) {
            setActiveStyle(accountMgmtBtn);
        } else if ("inventory".equals(fxmlName)) {
            setActiveStyle(inventoryBtn);
        } else if ("automation".equals(fxmlName)) {
            setActiveStyle(automationBtn);
        } else if ("layout".equals(fxmlName)) {
            setActiveStyle(layoutBtn);
        }

        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/" + fxmlName + ".fxml"));
            Parent view = loader.load();

            // Wrap in a ScrollPane to ensure usability and scrolling compatibility
            ScrollPane scrollPane = new ScrollPane(view);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

            contentArea.getChildren().setAll(scrollPane);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading sub-view: " + fxmlName);
        }
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
    private void handleOverview() {
        loadView("overview");
    }

    @FXML
    private void handleAccountManagement() {
        loadView("acountMgmt");
    }

    @FXML
    private void handleInventoryManagement() {
        loadView("inventory");
    }

    @FXML
    private void handleAutomationManagement() {
        loadView("automation");
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
    private void handleViewLayout(ActionEvent event) {
        loadView("layout");
    }
}
