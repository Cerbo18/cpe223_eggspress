package cpe223.group8.eggspress.controllers;

import cpe223.group8.eggspress.Main;
import cpe223.group8.eggspress.models.Notification;
import cpe223.group8.eggspress.services.NotificationListener;
import cpe223.group8.eggspress.services.NotificationService;
import cpe223.group8.eggspress.services.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.application.Platform;
import java.util.List;

public class DashboardController implements NotificationListener {

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

    @FXML
    private Button notificationBtn;

    @FXML
    private Label notificationBadge;

    private boolean sidebarExpanded = true;
    private double previousDividerPosition = 0.125; // Default expanded

    private Popup notificationPopup;
    private VBox popupContent;

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

        // Initialize Notifications
        NotificationService.getInstance().addListener(this);
        updateUnreadBadgeCount();
    }

    @Override
    public void onNotificationReceived(Notification notification) {
        Platform.runLater(() -> {
            updateUnreadBadgeCount();
            showPushToast(notification);
            if (notificationPopup != null && notificationPopup.isShowing()) {
                refreshNotificationPopupContent();
            }
        });
    }

    private void updateUnreadBadgeCount() {
        String currentUsername = SessionManager.getCurrentUsername();
        int unreadCount = NotificationService.getInstance().getUnreadCountForUser(currentUsername);
        if (unreadCount > 0) {
            notificationBadge.setText(String.valueOf(unreadCount));
            notificationBadge.setVisible(true);
        } else {
            notificationBadge.setVisible(false);
        }
    }

    @FXML
    private void handleToggleNotificationPopup() {
        if (notificationPopup != null && notificationPopup.isShowing()) {
            notificationPopup.hide();
        } else {
            showNotificationPopup();
        }
    }

    private void showNotificationPopup() {
        if (notificationPopup == null) {
            notificationPopup = new Popup();
            notificationPopup.setAutoHide(true);
            
            popupContent = new VBox();
            popupContent.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-padding: 10px; -fx-spacing: 8px; -fx-border-radius: 4px; -fx-background-radius: 4px;");
            popupContent.setMinWidth(350);
            popupContent.setMaxWidth(400);
            
            notificationPopup.getContent().add(popupContent);
        }

        refreshNotificationPopupContent();

        // Position popup below notification bell button safely
        double x = notificationBtn.localToScreen(notificationBtn.getBoundsInLocal()).getMinX();
        double y = notificationBtn.localToScreen(notificationBtn.getBoundsInLocal()).getMaxY() + 5;
        
        // Show slightly offset to the left so it stays nicely aligned
        notificationPopup.show(notificationBtn.getScene().getWindow(), x - 280, y);
    }

    private void refreshNotificationPopupContent() {
        if (popupContent == null) return;
        popupContent.getChildren().clear();

        String currentUsername = SessionManager.getCurrentUsername();

        // 1. Header Area
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);
        
        Label title = new Label("Notifications (" + currentUsername + ")");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #333333;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button markAllBtn = new Button("Mark All Read");
        markAllBtn.setStyle("-fx-font-size: 10px; -fx-padding: 3px 6px;");
        markAllBtn.setOnAction(e -> {
            NotificationService.getInstance().markAllAsReadForUser(currentUsername);
            updateUnreadBadgeCount();
            refreshNotificationPopupContent();
        });

        Button clearBtn = new Button("Clear All");
        clearBtn.setStyle("-fx-font-size: 10px; -fx-padding: 3px 6px;");
        clearBtn.setOnAction(e -> {
            NotificationService.getInstance().clearAllForUser(currentUsername);
            updateUnreadBadgeCount();
            refreshNotificationPopupContent();
        });

        header.getChildren().addAll(title, spacer, markAllBtn, clearBtn);
        popupContent.getChildren().add(header);

        // 2. Notification List in a ScrollPane
        VBox listContainer = new VBox();
        listContainer.setSpacing(6);
        
        List<Notification> notifications = NotificationService.getInstance().getAllNotificationsForUser(currentUsername);
        if (notifications.isEmpty()) {
            Label emptyLabel = new Label("No notifications recorded.");
            emptyLabel.setStyle("-fx-text-fill: #888888; -fx-font-style: italic; -fx-padding: 10px 0;");
            listContainer.getChildren().add(emptyLabel);
        } else {
            for (Notification n : notifications) {
                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_LEFT);
                row.setSpacing(8);
                
                // Styling based on level and unread status
                String borderStyle = n.isRead() ? "-fx-border-color: #eeeeee;" : "-fx-border-color: #007aff; -fx-background-color: #f2f8ff;";
                row.setStyle(borderStyle + " -fx-border-width: 1px; -fx-padding: 6px; -fx-border-radius: 4px; -fx-background-radius: 4px;");
                
                // Severity label
                Label levelLabel = new Label("[" + n.getLevel().toUpperCase() + "]");
                String levelColor = "info".equalsIgnoreCase(n.getLevel()) ? "#555555" : ("warning".equalsIgnoreCase(n.getLevel()) ? "#e67e22" : "#ff3b30");
                levelLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10px; -fx-text-fill: " + levelColor + ";");
                
                // Timestamp and Message
                VBox textDetails = new VBox(2);
                Label msgLabel = new Label(n.getMessage());
                msgLabel.setWrapText(true);
                msgLabel.setMaxWidth(200);
                msgLabel.setStyle(n.isRead() ? "-fx-text-fill: #555555; -fx-font-size: 11px;" : "-fx-text-fill: #000000; -fx-font-weight: bold; -fx-font-size: 11px;");
                
                Label timeLabel = new Label(n.getTimestamp());
                timeLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 9px;");
                
                textDetails.getChildren().addAll(msgLabel, timeLabel);
                row.getChildren().addAll(levelLabel, textDetails);
                
                Region rowSpacer = new Region();
                HBox.setHgrow(rowSpacer, Priority.ALWAYS);
                row.getChildren().add(rowSpacer);
                
                // Mark single as read button
                if (!n.isRead()) {
                    Button readBtn = new Button("✓");
                    readBtn.setStyle("-fx-font-size: 9px; -fx-padding: 2px 4px;");
                    readBtn.setOnAction(e -> {
                        NotificationService.getInstance().markAsReadForUser(currentUsername, n.getId());
                        updateUnreadBadgeCount();
                        refreshNotificationPopupContent();
                    });
                    row.getChildren().add(readBtn);
                }

                listContainer.getChildren().add(row);
            }
        }

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        popupContent.getChildren().add(scrollPane);

        // 3. Test Simulators (User Approved)
        HBox testBox = new HBox();
        testBox.setAlignment(Pos.CENTER_LEFT);
        testBox.setSpacing(6);
        testBox.setStyle("-fx-border-color: #dddddd; -fx-border-width: 1px 0 0 0; -fx-padding: 8px 0 0 0;");
        
        Label testLbl = new Label("Simulate:");
        testLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #666666;");
        
        Button simWarning = new Button("Warning");
        simWarning.setStyle("-fx-font-size: 9px; -fx-padding: 2px 4px;");
        simWarning.setOnAction(e -> NotificationService.getInstance().publish("Warning", "Simulated system alert: Low water tank pressure."));

        Button simCritical = new Button("Critical");
        simCritical.setStyle("-fx-font-size: 9px; -fx-padding: 2px 4px;");
        simCritical.setOnAction(e -> NotificationService.getInstance().publish("Critical", "Simulated emergency: Coop A temperature exceeded 35°C!"));

        testBox.getChildren().addAll(testLbl, simWarning, simCritical);
        popupContent.getChildren().add(testBox);
    }

    private void showPushToast(Notification notification) {
        if (contentArea == null) return;

        HBox toast = new HBox();
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setSpacing(10);
        
        // Restrict bounds so it floats as a compact card in the top-right
        toast.setMaxWidth(380);
        toast.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

        Label levelLabel = new Label("[" + notification.getLevel().toUpperCase() + "]");
        Label msgLabel = new Label(notification.getMessage());
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(240); // Allow room for the close button on the right

        // Add spacer to push the close button to the far right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Add an unstyled dedicated close button
        Button closeBtn = new Button("×");
        closeBtn.setOnAction(e -> contentArea.getChildren().remove(toast));

        toast.getChildren().addAll(levelLabel, msgLabel, spacer, closeBtn);

        StackPane.setAlignment(toast, Pos.TOP_RIGHT);
        // Standard HIG/Fluent consistency margins (e.g. 24px from top and right)
        StackPane.setMargin(toast, new Insets(24, 24, 0, 0));
        
        contentArea.getChildren().add(toast);

        // Auto-remove after 4 seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(4.0));
        delay.setOnFinished(e -> contentArea.getChildren().remove(toast));
        delay.play();
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
            // Remove listener so we don't hold static references leading to leaks on re-entry
            NotificationService.getInstance().removeListener(this);
            SessionManager.logout();
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
