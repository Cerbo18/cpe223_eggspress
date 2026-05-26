package cpe223.group8.eggspress.services;

import cpe223.group8.eggspress.config.DatabaseConfig;
import cpe223.group8.eggspress.models.InventoryItem;
import cpe223.group8.eggspress.models.Notification;
import cpe223.group8.eggspress.repository.NotificationRepository;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private static NotificationService instance;
    private final NotificationRepository repository;
    private final List<NotificationListener> listeners;
    private int lastKnownId = 0;
    private Timeline poller;

    private NotificationService() {
        this.repository = new NotificationRepository();
        this.listeners = new ArrayList<>();
        
        // 1. Initialize lastKnownId to current max ID in DB to avoid alerting old notifications on boot
        initLastKnownId();
        
        // 2. Start the periodic database poller to sync notifications across running apps
        Platform.runLater(this::startPoller);
    }

    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    private void initLastKnownId() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM notifications")) {
            if (rs.next()) {
                this.lastKnownId = rs.getInt(1);
            }
            System.out.println("Initialized Notification Poller: Last Known ID is " + lastKnownId);
        } catch (Exception e) {
            System.err.println("Error initializing last known notification ID: " + e.getMessage());
        }
    }

    private void startPoller() {
        if (poller == null) {
            poller = new Timeline(new KeyFrame(Duration.seconds(3), event -> pollForNewNotifications()));
            poller.setCycleCount(Timeline.INDEFINITE);
            poller.play();
            System.out.println("Started Notification database sync poller (3s interval).");
        }
    }
    private void pollForNewNotifications() {
        String sql = "SELECT * FROM notifications WHERE id > ? ORDER BY id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, lastKnownId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification(
                        rs.getInt("id"),
                        rs.getString("timestamp"),
                        rs.getString("level"),
                        rs.getString("message"),
                        false,
                        rs.getString("username"),
                        rs.getInt("priority")
                    );
                    
                    if (n.getId() > lastKnownId) {
                        lastKnownId = n.getId();
                    }
                    
                    // Dispatch to all local listeners reactively
                    synchronized (this) {
                        for (NotificationListener listener : listeners) {
                            listener.onNotificationReceived(n);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error polling database for notifications: " + e.getMessage());
        }
    }

    public synchronized void addListener(NotificationListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void removeListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    public void publish(String level, String message, boolean isGlobal, int priority) {
        String targetUser = isGlobal ? null : SessionManager.getCurrentUsername();
        Notification notification = new Notification(level, message, targetUser, priority);
        
        // Save to DB (creates timestamp and generated ID)
        repository.save(notification);

        // Update local lastKnownId to prevent this poller from repeating this notification
        if (notification.getId() > lastKnownId) {
            lastKnownId = notification.getId();
        }

        // Notify local listeners immediately on JavaFX thread
        synchronized (this) {
            for (NotificationListener listener : listeners) {
                Platform.runLater(() -> listener.onNotificationReceived(notification));
            }
        }
    }

    public void publish(String level, String message, boolean isGlobal) {
        publish(level, message, isGlobal, 1);
    }

    public void publish(String level, String message) {
        // By default, preserve backward compatibility by publishing globally
        publish(level, message, true, 1);
    }

    // ---------------------------------------------------------
    // Abstracted Static Notification API
    // ---------------------------------------------------------

    /**
     * Publishes an Info notification. Local to the active user session by default.
     */
    public static void notificationInfo(String message) {
        getInstance().publish("Info", message, false);
    }

    /**
     * Publishes an Info notification with explicit global or local scope.
     */
    public static void notificationInfo(String message, boolean isGlobal) {
        getInstance().publish("Info", message, isGlobal);
    }

    /**
     * Publishes an Info notification with explicit scope and priority.
     */
    public static void notificationInfo(String message, boolean isGlobal, int priority) {
        getInstance().publish("Info", message, isGlobal, priority);
    }

    /**
     * Publishes a Warning notification. Global to all users by default.
     */
    public static void notificationWarning(String message) {
        getInstance().publish("Warning", message, true);
    }

    /**
     * Publishes a Warning notification with explicit global or local scope.
     */
    public static void notificationWarning(String message, boolean isGlobal) {
        getInstance().publish("Warning", message, isGlobal);
    }

    /**
     * Publishes a Warning notification with explicit scope and priority.
     */
    public static void notificationWarning(String message, boolean isGlobal, int priority) {
        getInstance().publish("Warning", message, isGlobal, priority);
    }

    /**
     * Publishes a Critical notification. Global to all users by default.
     */
    public static void notificationCritical(String message) {
        getInstance().publish("Critical", message, true);
    }

    /**
     * Publishes a Critical notification with explicit global or local scope.
     */
    public static void notificationCritical(String message, boolean isGlobal) {
        getInstance().publish("Critical", message, isGlobal);
    }

    /**
     * Publishes a Critical notification with explicit scope and priority.
     */
    public static void notificationCritical(String message, boolean isGlobal, int priority) {
        getInstance().publish("Critical", message, isGlobal, priority);
    }

    public List<Notification> getAllNotifications() {
        return repository.findAll();
    }

    public List<Notification> getAllNotificationsForUser(String username) {
        return repository.findAllForUser(username);
    }

    public int getUnreadCount() {
        return repository.getUnreadCount();
    }

    public int getUnreadCountForUser(String username) {
        return repository.getUnreadCountForUser(username);
    }

    public void markAsRead(int id) {
        repository.markAsRead(id);
    }

    public void markAsReadForUser(String username, int id) {
        repository.markAsReadForUser(username, id);
    }

    public void markAllAsRead() {
        repository.markAllAsRead();
    }

    public void markAllAsReadForUser(String username) {
        repository.markAllAsReadForUser(username);
    }

    public void clearAll() {
        repository.clearAll();
    }

    public void clearAllForUser(String username) {
        repository.clearAllForUser(username);
    }

    /**
     * Proactively monitors inventory stock level drops and triggers persistent low-stock events.
     */
    public void checkInventoryThresholds(InventoryItem item) {
        if (item == null) return;

        String name = item.getName().toLowerCase();
        double qty = item.getQuantity();

        if (name.contains("grains") || name.equals("grains")) {
            if (qty < 200.0) {
                publish("Critical", "Grains stock critically low: " + qty + " " + item.getUnit() + " remaining (threshold < 200).");
            }
        } else if (name.contains("water") || name.equals("water")) {
            if (qty < 500.0) {
                publish("Critical", "Water stock critically low: " + qty + " " + item.getUnit() + " remaining (threshold < 500).");
            }
        } else if (name.contains("layers feed") || name.equals("layers feed")) {
            if (qty < 100.0) {
                publish("Warning", "Layers Feed stock is low: " + qty + " " + item.getUnit() + " remaining (threshold < 100).");
            }
        } else if (qty < 50.0) {
            publish("Warning", item.getName() + " stock is low: " + qty + " " + item.getUnit() + " remaining.");
        }
    }
}
