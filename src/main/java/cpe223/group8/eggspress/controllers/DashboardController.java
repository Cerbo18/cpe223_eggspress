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
            String prevText = notificationBadge.getText();
            notificationBadge.setText(String.valueOf(unreadCount));
            if (!notificationBadge.isVisible() || !prevText.equals(String.valueOf(unreadCount))) {
                notificationBadge.setVisible(true);
                // Trigger dynamic scale-in animation on badge updates
                javafx.animation.ScaleTransition anim = new javafx.animation.ScaleTransition(Duration.millis(200), notificationBadge);
                anim.setFromX(0.5);
                anim.setFromY(0.5);
                anim.setToX(1.0);
                anim.setToY(1.0);
                anim.play();
            }
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
            popupContent.getStyleClass().add("notification-popup-container");
            popupContent.setMinWidth(360);
            popupContent.setMaxWidth(360);
            popupContent.setMaxHeight(450);

            // Popup nodes live in a separate scene graph and do not inherit the
            // main application's stylesheets. Load them explicitly so all CSS
            // class selectors resolve against this detached scene.
            String[] cssResources = {
                "css/global.css",
                "css/light.css",
                "css/dashboard/dashboard.css",
                "css/dashboard/light.css"
            };
            for (String resource : cssResources) {
                java.net.URL url = Main.class.getResource(resource);
                if (url != null) {
                    popupContent.getStylesheets().add(url.toExternalForm());
                }
            }

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
        header.getStyleClass().add("notification-header");
        
        Label title = new Label("Notifications (" + currentUsername + ")");
        title.getStyleClass().add("notification-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button markAllBtn = new Button("Mark All Read");
        markAllBtn.getStyleClass().add("notification-action-btn");
        markAllBtn.setGraphic(createSvgIcon(
            "M10 12a2 2 0 1 0 4 0a2 2 0 0 0 -4 0",
            "M21 12c-2.4 4 -5.4 6 -9 6c-3.6 0 -6.6 -2 -9 -6c2.4 -4 5.4 -6 9 -6c3.6 0 6.6 2 9 6"
        ));
        markAllBtn.setOnAction(e -> {
            NotificationService.getInstance().markAllAsReadForUser(currentUsername);
            updateUnreadBadgeCount();
            refreshNotificationPopupContent();
        });

        Button clearBtn = new Button("Clear All");
        clearBtn.getStyleClass().add("notification-action-btn");
        clearBtn.setGraphic(createSvgIcon(
            "M8 6h12",
            "M6 12h12",
            "M4 18h12"
        ));
        clearBtn.setOnAction(e -> {
            NotificationService.getInstance().clearAllForUser(currentUsername);
            updateUnreadBadgeCount();
            refreshNotificationPopupContent();
        });

        header.getChildren().addAll(title, spacer, markAllBtn, clearBtn);
        popupContent.getChildren().add(header);

        // 2. Notification List in a ScrollPane
        VBox listContainer = new VBox();
        listContainer.getStyleClass().add("notification-list-container");
        
        List<Notification> notifications = NotificationService.getInstance().getAllNotificationsForUser(currentUsername);
        if (notifications.isEmpty()) {
            Label emptyLabel = new Label("No notifications recorded.");
            emptyLabel.getStyleClass().add("notification-empty-label");
            listContainer.getChildren().add(emptyLabel);
        } else {
            for (Notification n : notifications) {
                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_LEFT);
                row.setSpacing(8);
                row.getStyleClass().add("notification-row");
                if (!n.isRead()) {
                    row.getStyleClass().add("unread");
                }
                
                // Severity label styled as capsule pill badge without raw brackets
                Label levelLabel = new Label(n.getLevel().toUpperCase());
                levelLabel.getStyleClass().addAll("notification-level-badge", n.getLevel().toLowerCase());
                if ("warning".equalsIgnoreCase(n.getLevel()) || "critical".equalsIgnoreCase(n.getLevel())) {
                    levelLabel.setGraphic(createSvgIcon(
                        "M12 9v4",
                        "M10.363 3.591l-8.106 13.534a1.914 1.914 0 0 0 1.636 2.871h16.214a1.914 1.914 0 0 0 1.636 -2.87l-8.106 -13.536a1.914 1.914 0 0 0 -3.274 0",
                        "M12 16h.01"
                    ));
                    levelLabel.setGraphicTextGap(4);
                }
                
                // Timestamp and Message
                VBox textDetails = new VBox(2);
                Label msgLabel = new Label(n.getMessage());
                msgLabel.setWrapText(true);
                msgLabel.setMaxWidth(200);
                msgLabel.getStyleClass().add("notification-message");
                
                Label timeLabel = new Label(n.getTimestamp());
                timeLabel.getStyleClass().add("notification-time");
                
                textDetails.getChildren().addAll(msgLabel, timeLabel);
                row.getChildren().addAll(levelLabel, textDetails);
                
                Region rowSpacer = new Region();
                HBox.setHgrow(rowSpacer, Priority.ALWAYS);
                row.getChildren().add(rowSpacer);
                
                // Mark single as read button
                if (!n.isRead()) {
                    Button readBtn = new Button();
                    readBtn.getStyleClass().add("notification-read-btn");
                    readBtn.setGraphic(createSvgIcon(
                        "M10 12a2 2 0 1 0 4 0a2 2 0 0 0 -4 0",
                        "M11.102 17.957c-3.204 -.307 -5.904 -2.294 -8.102 -5.957c2.4 -4 5.4 -6 9 -6c3.6 0 6.6 2 9 6a19.5 19.5 0 0 1 -.663 1.032",
                        "M15 19l2 2l4 -4"
                    ));
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
        scrollPane.getStyleClass().add("notification-scroll-pane");
        popupContent.getChildren().add(scrollPane);

        // 3. Test Simulators (User Approved)
        HBox testBox = new HBox();
        testBox.getStyleClass().add("notification-test-box");
        testBox.setAlignment(Pos.CENTER_LEFT);
        testBox.setSpacing(6);
        
        Label testLbl = new Label("Simulate:");
        testLbl.getStyleClass().add("notification-test-label");
        
        Button simWarning = new Button("Warning");
        simWarning.getStyleClass().addAll("notification-sim-btn", "warning");
        simWarning.setOnAction(e -> NotificationService.getInstance().publish("Warning", "Simulated system alert: Low water tank pressure."));

        Button simCritical = new Button("Critical");
        simCritical.getStyleClass().addAll("notification-sim-btn", "critical");
        simCritical.setOnAction(e -> NotificationService.getInstance().publish("Critical", "Simulated emergency: Coop A temperature exceeded 35°C!"));

        testBox.getChildren().addAll(testLbl, simWarning, simCritical);
        popupContent.getChildren().add(testBox);
    }

    private void showPushToast(Notification notification) {
        if (contentArea == null) return;

        HBox toast = new HBox();
        toast.getStyleClass().addAll("notification-toast", notification.getLevel().toLowerCase());
        toast.setMinWidth(320);
        toast.setMaxWidth(320);
        toast.setMaxHeight(Region.USE_PREF_SIZE);

        Label levelLabel = new Label(notification.getLevel().toUpperCase());
        levelLabel.getStyleClass().addAll("notification-level-badge", notification.getLevel().toLowerCase());
        if ("warning".equalsIgnoreCase(notification.getLevel()) || "critical".equalsIgnoreCase(notification.getLevel())) {
            levelLabel.setGraphic(createSvgIcon(
                "M12 9v4",
                "M10.363 3.591l-8.106 13.534a1.914 1.914 0 0 0 1.636 2.871h16.214a1.914 1.914 0 0 0 1.636 -2.87l-8.106 -13.536a1.914 1.914 0 0 0 -3.274 0",
                "M12 16h.01"
            ));
            levelLabel.setGraphicTextGap(4);
        }

        Label msgLabel = new Label(notification.getMessage());
        msgLabel.setWrapText(true);
        msgLabel.getStyleClass().add("notification-toast-message");

        // Spacer pushes the close button to the trailing edge
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Dismiss button
        Button closeBtn = new Button();
        closeBtn.getStyleClass().add("notification-toast-close-btn");
        closeBtn.setGraphic(createSvgIcon(
            "M18 6l-12 12",
            "M6 6l12 12"
        ));
        closeBtn.setOnAction(e -> contentArea.getChildren().remove(toast));

        toast.getChildren().addAll(levelLabel, msgLabel, spacer, closeBtn);

        StackPane.setAlignment(toast, Pos.TOP_RIGHT);
        // Set spatial margin offset of 24px from the top-right container boundary
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

    private javafx.scene.Group createSvgIcon(String path1) {
        return createSvgIcon(new String[]{path1});
    }

    private javafx.scene.Group createSvgIcon(String path1, String path2) {
        return createSvgIcon(new String[]{path1, path2});
    }

    private javafx.scene.Group createSvgIcon(String path1, String path2, String path3) {
        return createSvgIcon(new String[]{path1, path2, path3});
    }

    private javafx.scene.Group createSvgIcon(String[] paths) {
        javafx.scene.Group group = new javafx.scene.Group();
        group.setScaleX(0.7);
        group.setScaleY(0.7);
        for (String path : paths) {
            javafx.scene.shape.SVGPath svgPath = new javafx.scene.shape.SVGPath();
            svgPath.setContent(path);
            svgPath.getStyleClass().add("notification-icon-path");
            group.getChildren().add(svgPath);
        }
        return group;
    }
}
