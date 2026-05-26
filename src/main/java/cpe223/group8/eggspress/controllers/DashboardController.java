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
import javafx.scene.shape.SVGPath;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
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
    private Button developerBtn;

    @FXML
    private Button notificationBtn;

    @FXML
    private Label notificationBadge;

    @FXML
    private Button themeToggleBtn;

    @FXML
    private Button settingsBtn;

    @FXML
    private SVGPath themeToggleIcon;

    private Parent currentSubView;

    private boolean sidebarExpanded = true;
    private double previousDividerPosition = 0.125; // Default expanded

    private Popup notificationPopup;
    private VBox popupContent;
    private VBox toastContainer;

    // Edge resize state variables
    private boolean isResizing = false;
    private String resizeType = "";
    private double startX = 0;
    private double startY = 0;
    private double startWidth = 0;
    private double startHeight = 0;
    private double startWinX = 0;

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
            if (splitPane != null && splitPane.getScene() != null) {
                Parent root = splitPane.getScene().getRoot();
                cpe223.group8.eggspress.services.ThemeManager.applyTheme(root);
                if (themeToggleIcon != null) {
                    if (cpe223.group8.eggspress.services.ThemeManager.isDarkMode()) {
                        themeToggleIcon.setContent("M8 12a4 4 0 1 0 8 0a4 4 0 1 0 -8 0 M3 12h1m8 -9v1m8 8h1m-9 8v1m-6.4 -15.4l.7 .7m12.1 -.7l-.7 .7m0 11.4l.7 .7m-12.1 -.7l-.7 .7");
                    } else {
                        themeToggleIcon.setContent("M12 3c.132 0 .263 0 .393 0a7.5 7.5 0 0 0 7.92 12.446a9 9 0 1 1 -8.313 -12.454l0 .008");
                    }
                }
            }
        });

        // Register keyboard shortcuts when scene becomes available
        if (splitPane != null) {
            splitPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    setupKeyboardAccelerators(newScene);
                }
            });
        }

        // Initialize Notifications
        if (notificationBadge != null) {
            notificationBadge.setMinWidth(Region.USE_PREF_SIZE);
        }
        NotificationService.getInstance().addListener(this);
        updateUnreadBadgeCount();

        // Install premium themed tooltips on navigation and action controls
        cpe223.group8.eggspress.services.TooltipHelper.installTooltip(themeToggleBtn, "Switch visual theme mode");
        cpe223.group8.eggspress.services.TooltipHelper.installTooltip(settingsBtn, "Open application configurations and database state settings");
        cpe223.group8.eggspress.services.TooltipHelper.installTooltip(notificationBtn, "View system alerts and notifications");
        cpe223.group8.eggspress.services.TooltipHelper.installTooltip(toggleButton, "Collapse or expand left navigation sidebar");
        cpe223.group8.eggspress.services.TooltipHelper.installTooltip(overviewBtn, "Overview dashboard metrics and telemetry");
        cpe223.group8.eggspress.services.TooltipHelper.installTooltip(accountMgmtBtn, "Manage staff and database access credentials");
        cpe223.group8.eggspress.services.TooltipHelper.installTooltip(inventoryBtn, "Monitor feed, water, and flock resources");
        cpe223.group8.eggspress.services.TooltipHelper.installTooltip(automationBtn, "Configure environmental rules and schedules");
        cpe223.group8.eggspress.services.TooltipHelper.installTooltip(layoutBtn, "Interactive coop layouts and map blueprint");
        cpe223.group8.eggspress.services.TooltipHelper.installTooltip(developerBtn, "Open developer mock injector and diagnostics console");

        // Restore Developer Panel menu button status based on cached preferences
        toggleDeveloperModeSidebarButton(cpe223.group8.eggspress.services.PreferencesManager.isDeveloperMode());
    }

    private void setupKeyboardAccelerators(javafx.scene.Scene scene) {
        if (scene == null) return;
        scene.getAccelerators().put(
            new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.DIGIT1, javafx.scene.input.KeyCombination.CONTROL_DOWN),
            () -> Platform.runLater(this::handleOverview)
        );
        scene.getAccelerators().put(
            new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.DIGIT2, javafx.scene.input.KeyCombination.CONTROL_DOWN),
            () -> Platform.runLater(this::handleAccountManagement)
        );
        scene.getAccelerators().put(
            new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.DIGIT3, javafx.scene.input.KeyCombination.CONTROL_DOWN),
            () -> Platform.runLater(this::handleInventoryManagement)
        );
        scene.getAccelerators().put(
            new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.DIGIT4, javafx.scene.input.KeyCombination.CONTROL_DOWN),
            () -> Platform.runLater(this::handleAutomationManagement)
        );
        scene.getAccelerators().put(
            new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.DIGIT5, javafx.scene.input.KeyCombination.CONTROL_DOWN),
            () -> Platform.runLater(() -> handleViewLayout(null))
        );
    }

    @Override
    public void onNotificationReceived(Notification notification) {
        String currentUsername = SessionManager.getCurrentUsername();
        if (notification.getUsername() != null && !notification.getUsername().equalsIgnoreCase(currentUsername)) {
            // Local notification targeted at a different user; filter and ignore
            return;
        }
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
        if (notificationBtn != null) {
            notificationBtn.getStyleClass().removeAll("has-unread", "all-read");
        }
        
        if (unreadCount > 0) {
            if (notificationBtn != null) {
                notificationBtn.getStyleClass().add("has-unread");
            }
            String textVal = unreadCount >= 100 ? "99+" : String.valueOf(unreadCount);
            String prevText = notificationBadge.getText();
            notificationBadge.setText(textVal);
            if (!notificationBadge.isVisible() || !prevText.equals(textVal)) {
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
            if (notificationBtn != null) {
                notificationBtn.getStyleClass().add("all-read");
            }
            notificationBadge.setVisible(false);
        }
    }

    @FXML
    private void handleToggleTheme() {
        boolean dark = !cpe223.group8.eggspress.services.ThemeManager.isDarkMode();
        cpe223.group8.eggspress.services.ThemeManager.setDarkMode(dark);
        
        applyVisualThemeChange();
        
        // Dynamically save the toggled starting theme
        cpe223.group8.eggspress.services.PreferencesManager.setStartingTheme(dark ? "DARK" : "LIGHT");
    }

    public void applyVisualThemeChange() {
        boolean dark = cpe223.group8.eggspress.services.ThemeManager.isDarkMode();

        // 1. Apply theme to dashboard root (includes sidebar, header, etc.)
        if (splitPane != null && splitPane.getScene() != null) {
            Parent root = splitPane.getScene().getRoot();
            cpe223.group8.eggspress.services.ThemeManager.applyTheme(root);
        }

        // 2. Apply theme to currently loaded sub-view
        if (currentSubView != null) {
            cpe223.group8.eggspress.services.ThemeManager.applyTheme(currentSubView);
        }

        // 3. Apply theme to notification popup content if it exists
        if (popupContent != null) {
            cpe223.group8.eggspress.services.ThemeManager.applyTheme(popupContent);
        }

        // 4. Update toggle button icon path (Moon for Light Mode, Sun for Dark Mode)
        if (themeToggleIcon != null) {
            if (dark) {
                themeToggleIcon.setContent("M8 12a4 4 0 1 0 8 0a4 4 0 1 0 -8 0 M3 12h1m8 -9v1m8 8h1m-9 8v1m-6.4 -15.4l.7 .7m12.1 -.7l-.7 .7m0 11.4l.7 .7m-12.1 -.7l-.7 .7");
            } else {
                themeToggleIcon.setContent("M12 3c.132 0 .263 0 .393 0a7.5 7.5 0 0 0 7.92 12.446a9 9 0 1 1 -8.313 -12.454l0 .008");
            }
        }
    }

    @FXML
    private void handleSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/settings.fxml"));
            Parent root = loader.load();
            cpe223.group8.eggspress.services.ThemeManager.applyTheme(root);

            javafx.stage.Stage settingsStage = new javafx.stage.Stage();
            settingsStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            if (splitPane != null && splitPane.getScene() != null) {
                settingsStage.initOwner(splitPane.getScene().getWindow());
            }
            settingsStage.setTitle("Settings");
            settingsStage.setResizable(false);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            cpe223.group8.eggspress.services.ThemeManager.applySceneFill(scene);
            settingsStage.setScene(scene);
            settingsStage.showAndWait();
        } catch (IOException e) {
            System.err.println("Error loading settings modal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void toggleDeveloperModeSidebarButton(boolean enable) {
        Platform.runLater(() -> {
            if (developerBtn != null) {
                developerBtn.setVisible(enable);
                developerBtn.setManaged(enable);
            }
        });
    }

    @FXML
    private void handleDeveloperPanel() {
        loadView("developer");
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
            popupContent.setPrefWidth(562);
            popupContent.setMinWidth(360);
            popupContent.setMaxWidth(800);
            popupContent.setPrefHeight(562);
            popupContent.setMinHeight(450);
            popupContent.setMaxHeight(900);
            makeResizable(popupContent);

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

        cpe223.group8.eggspress.services.ThemeManager.applyTheme(popupContent);
        refreshNotificationPopupContent();

        // Position popup below notification bell button safely
        double x = notificationBtn.localToScreen(notificationBtn.getBoundsInLocal()).getMinX();
        double y = notificationBtn.localToScreen(notificationBtn.getBoundsInLocal()).getMaxY() + 5;

        // Show slightly offset to the left dynamically based on pref width so it stays nicely aligned
        notificationPopup.show(notificationBtn.getScene().getWindow(), x - (popupContent.getPrefWidth() - 80), y);
    }


    private void refreshNotificationPopupContent() {
        if (popupContent == null) return;
        popupContent.getChildren().clear();

        String currentUsername = SessionManager.getCurrentUsername();

        // 1. Header Area
        HBox header = new HBox();
        header.getStyleClass().add("notification-header");
        
        Label title = new Label("Notifications");
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
            Label emptyLabel = new Label("No notifications.");
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
                } else if ("info".equalsIgnoreCase(n.getLevel())) {
                    levelLabel.setGraphic(createSvgIcon(
                        "M3 12a9 9 0 1 0 18 0a9 9 0 0 0 -18 0",
                        "M12 8v4",
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
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        scrollPane.getStyleClass().add("notification-scroll-pane");
        popupContent.getChildren().add(scrollPane);

        // 3. Test Simulators (User Approved)
        HBox testBox = new HBox();
        testBox.getStyleClass().add("notification-test-box");
        testBox.setAlignment(Pos.CENTER_LEFT);
        testBox.setSpacing(6);
        
        Label testLbl = new Label("Simulate:");
        testLbl.getStyleClass().add("notification-test-label");
        
        Button simInfo = new Button("Info");
        simInfo.getStyleClass().addAll("notification-sim-btn", "info");
        simInfo.setOnAction(e -> NotificationService.notificationInfo("Simulated system info: Daily feed log generated."));

        Button simWarning = new Button("Warning");
        simWarning.getStyleClass().addAll("notification-sim-btn", "warning");
        simWarning.setOnAction(e -> NotificationService.notificationWarning("Simulated system alert: Low water tank pressure."));

        Button simCritical = new Button("Critical");
        simCritical.getStyleClass().addAll("notification-sim-btn", "critical");
        simCritical.setOnAction(e -> NotificationService.notificationCritical("Simulated emergency: Coop A temperature exceeded 35°C!"));

        Button simLowInfo = new Button("Low Info");
        simLowInfo.getStyleClass().addAll("notification-sim-btn", "info");
        simLowInfo.setOnAction(e -> NotificationService.notificationInfo("Simulated low info: Non-blocking log created.", false, 2));

        Button simLowWarning = new Button("Low Warning");
        simLowWarning.getStyleClass().addAll("notification-sim-btn", "warning");
        simLowWarning.setOnAction(e -> NotificationService.notificationWarning("Simulated low warning: Screen refreshed.", false, 2));

        testBox.getChildren().addAll(testLbl, simInfo, simWarning, simCritical, simLowInfo, simLowWarning);
        popupContent.getChildren().add(testBox);
    }

    private void showPushToast(Notification notification) {
        if (contentArea == null) return;

        // Check if push toasts are enabled in user configurations
        if (!cpe223.group8.eggspress.services.PreferencesManager.isPushToastsEnabled()) {
            return;
        }

        // Apply severity level priority filtering
        String severityFilter = cpe223.group8.eggspress.services.PreferencesManager.getToastPriorityFilter();
        String notificationLevel = notification.getLevel() != null ? notification.getLevel().toUpperCase() : "INFO";
        if ("CRITICAL".equals(severityFilter)) {
            if (!"CRITICAL".equals(notificationLevel)) {
                return;
            }
        } else if ("WARNING".equals(severityFilter)) {
            if ("INFO".equals(notificationLevel)) {
                return;
            }
        }

        ensureToastContainer();

        VBox toast = new VBox(0);
        toast.getStyleClass().addAll("notification-toast", notification.getLevel().toLowerCase());
        toast.setMinWidth(320);
        toast.setMaxWidth(320);
        toast.setMaxHeight(Region.USE_PREF_SIZE);

        HBox content = new HBox(10);
        content.getStyleClass().add("notification-toast-content");
        content.setAlignment(Pos.CENTER_LEFT);

        Label levelLabel = new Label();
        levelLabel.getStyleClass().addAll("notification-level-badge", notification.getLevel().toLowerCase());
        if ("warning".equalsIgnoreCase(notification.getLevel()) || "critical".equalsIgnoreCase(notification.getLevel())) {
            levelLabel.setGraphic(createSvgIcon(
                "M12 9v4",
                "M10.363 3.591l-8.106 13.534a1.914 1.914 0 0 0 1.636 2.871h16.214a1.914 1.914 0 0 0 1.636 -2.87l-8.106 -13.536a1.914 1.914 0 0 0 -3.274 0",
                "M12 16h.01"
            ));
        } else {
            levelLabel.setGraphic(createSvgIcon(
                "M3 12a9 9 0 1 0 18 0a9 9 0 0 0 -18 0",
                "M12 8v4",
                "M12 16h.01"
            ));
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

        content.getChildren().addAll(levelLabel, msgLabel, spacer, closeBtn);

        // Draining progress bar Region at the footer
        Region progressBar = new Region();
        progressBar.getStyleClass().add("notification-toast-progressbar");
        progressBar.setPrefWidth(318); // Start at full width inside the 320px container (accounting for 1px borders)
        progressBar.setMinWidth(0);
        progressBar.maxWidthProperty().bind(progressBar.prefWidthProperty());

        toast.getChildren().addAll(content, progressBar);

        // Timeline to animate progress bar width to 0
        javafx.animation.Timeline progressTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(Duration.ZERO, new javafx.animation.KeyValue(progressBar.prefWidthProperty(), 318)),
            new javafx.animation.KeyFrame(Duration.seconds(4.0), new javafx.animation.KeyValue(progressBar.prefWidthProperty(), 0))
        );

        PauseTransition delay = new PauseTransition(Duration.seconds(4.0));

        closeBtn.setOnAction(e -> {
            delay.stop();
            progressTimeline.stop();
            if (toastContainer != null) {
                toastContainer.getChildren().remove(toast);
            }
            if (notification.getPriority() == 1) {
                String currentUsername = SessionManager.getCurrentUsername();
                NotificationService.getInstance().markAsReadForUser(currentUsername, notification.getId());
                Platform.runLater(() -> {
                    updateUnreadBadgeCount();
                    if (notificationPopup != null && notificationPopup.isShowing()) {
                        refreshNotificationPopupContent();
                    }
                });
            }
        });

        delay.setOnFinished(e -> {
            progressTimeline.stop();
            if (toastContainer != null) {
                toastContainer.getChildren().remove(toast);
            }
        });

        if (toastContainer != null) {
            toastContainer.getChildren().add(toast);
        }

        progressTimeline.play();
        delay.play();
    }

    private void ensureToastContainer() {
        if (toastContainer == null) {
            toastContainer = new VBox(10);
            toastContainer.setAlignment(Pos.TOP_RIGHT);
            toastContainer.setPickOnBounds(false);
            toastContainer.setMaxWidth(Region.USE_PREF_SIZE);
            toastContainer.setMaxHeight(Region.USE_PREF_SIZE);
            StackPane.setAlignment(toastContainer, Pos.TOP_RIGHT);
            StackPane.setMargin(toastContainer, new Insets(24, 24, 0, 0));
        }
        if (contentArea != null && !contentArea.getChildren().contains(toastContainer)) {
            contentArea.getChildren().add(toastContainer);
        }
    }

    private void makeResizable(Region region) {
        region.setOnMouseMoved(e -> {
            if (isResizing) return;
            double x = e.getX();
            double y = e.getY();
            double w = region.getWidth();
            double h = region.getHeight();
            boolean nearRight = (x >= w - 10);
            boolean nearLeft = (x <= 10);
            boolean nearBottom = (y >= h - 10);
            
            if (nearLeft && nearBottom) {
                region.setCursor(javafx.scene.Cursor.SW_RESIZE);
            } else if (nearRight && nearBottom) {
                region.setCursor(javafx.scene.Cursor.SE_RESIZE);
            } else if (nearLeft) {
                region.setCursor(javafx.scene.Cursor.W_RESIZE);
            } else if (nearRight) {
                region.setCursor(javafx.scene.Cursor.E_RESIZE);
            } else if (nearBottom) {
                region.setCursor(javafx.scene.Cursor.S_RESIZE);
            } else {
                region.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });

        region.setOnMousePressed(e -> {
            double x = e.getX();
            double y = e.getY();
            double w = region.getWidth();
            double h = region.getHeight();
            boolean nearRight = (x >= w - 10);
            boolean nearLeft = (x <= 10);
            boolean nearBottom = (y >= h - 10);
            
            if (nearRight || nearLeft || nearBottom) {
                isResizing = true;
                startX = e.getScreenX();
                startY = e.getScreenY();
                startWidth = region.getWidth();
                startHeight = region.getHeight();
                if (region.getScene() != null && region.getScene().getWindow() != null) {
                    startWinX = region.getScene().getWindow().getX();
                }
                
                if (nearLeft && nearBottom) {
                    resizeType = "SW";
                } else if (nearRight && nearBottom) {
                    resizeType = "SE";
                } else if (nearLeft) {
                    resizeType = "W";
                } else if (nearRight) {
                    resizeType = "E";
                } else {
                    resizeType = "S";
                }
                e.consume();
            }
        });

        region.setOnMouseDragged(e -> {
            if (!isResizing) return;
            
            double deltaX = e.getScreenX() - startX;
            double deltaY = e.getScreenY() - startY;
            
            // Sizing constraints matching the minimum (360) and maximum (800) limits
            double minW = 360;
            double maxW = 800;
            
            if ("E".equals(resizeType) || "SE".equals(resizeType)) {
                double newWidth = Math.max(minW, Math.min(maxW, startWidth + deltaX));
                region.setPrefWidth(newWidth);
            }
            
            if ("W".equals(resizeType) || "SW".equals(resizeType)) {
                double maxNewWidth = startWidth - deltaX;
                double constrainedWidth = Math.max(minW, Math.min(maxW, maxNewWidth));
                double actualDeltaW = constrainedWidth - startWidth;
                region.setPrefWidth(constrainedWidth);
                if (region.getScene() != null && region.getScene().getWindow() != null) {
                    region.getScene().getWindow().setX(startWinX - actualDeltaW);
                }
            }
            
            // Sizing constraints matching the minimum (450) and maximum (900) limits
            double minH = 450;
            double maxH = 900;
            
            if ("S".equals(resizeType) || "SE".equals(resizeType) || "SW".equals(resizeType)) {
                double newHeight = Math.max(minH, Math.min(maxH, startHeight + deltaY));
                region.setPrefHeight(newHeight);
            }
            e.consume();
        });

        region.setOnMouseReleased(e -> {
            isResizing = false;
            resizeType = "";
            region.setCursor(javafx.scene.Cursor.DEFAULT);
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
        clearActiveStyle(developerBtn);

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
        } else if ("developer".equals(fxmlName)) {
            setActiveStyle(developerBtn);
        }

        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/" + fxmlName + ".fxml"));
            Parent view = loader.load();
            currentSubView = view;
            cpe223.group8.eggspress.services.ThemeManager.applyTheme(view);

            // Wrap in a ScrollPane to ensure usability and scrolling compatibility
            ScrollPane scrollPane = new ScrollPane(view);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

            if (toastContainer != null && contentArea.getChildren().contains(toastContainer)) {
                contentArea.getChildren().setAll(scrollPane, toastContainer);
            } else {
                contentArea.getChildren().setAll(scrollPane);
            }
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
