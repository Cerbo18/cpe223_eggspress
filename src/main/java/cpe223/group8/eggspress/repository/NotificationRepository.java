package cpe223.group8.eggspress.repository;

import cpe223.group8.eggspress.config.DatabaseConfig;
import cpe223.group8.eggspress.models.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository implements BaseRepository<Notification> {

    @Override
    public void save(Notification entity) {
        String sql = "INSERT INTO notifications (level, message, is_read) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, entity.getLevel());
            pstmt.setString(2, entity.getMessage());
            pstmt.setInt(3, entity.isRead() ? 1 : 0);
            
            pstmt.executeUpdate();
            
            // Fetch generated primary key ID and populate model
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getInt(1));
                }
            }
            
            // Retrieve generated timestamp from database to keep model synced
            if (entity.getId() > 0) {
                String selectTs = "SELECT timestamp FROM notifications WHERE id = ?";
                try (PreparedStatement tsPstmt = conn.prepareStatement(selectTs)) {
                    tsPstmt.setInt(1, entity.getId());
                    try (ResultSet rs = tsPstmt.executeQuery()) {
                        if (rs.next()) {
                            entity.setTimestamp(rs.getString("timestamp"));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Notification findById(int id) {
        String sql = "SELECT * FROM notifications WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Notification(
                        rs.getInt("id"),
                        rs.getString("timestamp"),
                        rs.getString("level"),
                        rs.getString("message"),
                        rs.getInt("is_read") == 1
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding notification by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Notification> findAll() {
        return findAllForUser("admin");
    }

    @Override
    public void update(Notification entity) {
        String sql = "UPDATE notifications SET level = ?, message = ?, is_read = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entity.getLevel());
            pstmt.setString(2, entity.getMessage());
            pstmt.setInt(3, entity.isRead() ? 1 : 0);
            pstmt.setInt(4, entity.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM notifications WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getUnreadCount() {
        return getUnreadCountForUser("admin");
    }

    public void markAsRead(int id) {
        markAsReadForUser("admin", id);
    }

    public void markAllAsRead() {
        markAllAsReadForUser("admin");
    }

    public void clearAll() {
        clearAllForUser("admin");
    }

    // --- USER SPECIFIC METHODS ---

    public List<Notification> findAllForUser(String username) {
        List<Notification> list = new ArrayList<>();
        String sql = """
            SELECT n.*, 
                   COALESCE(uns.is_read, 0) as user_is_read, 
                   COALESCE(uns.is_cleared, 0) as user_is_cleared 
            FROM notifications n
            LEFT JOIN user_notification_states uns 
              ON n.id = uns.notification_id AND uns.username = ?
            WHERE user_is_cleared = 0
            ORDER BY n.id DESC
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Notification(
                        rs.getInt("id"),
                        rs.getString("timestamp"),
                        rs.getString("level"),
                        rs.getString("message"),
                        rs.getInt("user_is_read") == 1
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding notifications for user: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public int getUnreadCountForUser(String username) {
        String sql = """
            SELECT COUNT(*) 
            FROM notifications n
            LEFT JOIN user_notification_states uns 
              ON n.id = uns.notification_id AND uns.username = ?
            WHERE COALESCE(uns.is_read, 0) = 0 AND COALESCE(uns.is_cleared, 0) = 0
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting unread count for user: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public void markAsReadForUser(String username, int notificationId) {
        String sql = """
            INSERT OR REPLACE INTO user_notification_states (username, notification_id, is_read, is_cleared) 
            VALUES (?, ?, 1, COALESCE((SELECT is_cleared FROM user_notification_states WHERE username = ? AND notification_id = ?), 0))
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, notificationId);
            pstmt.setString(3, username);
            pstmt.setInt(4, notificationId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error marking notification as read for user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void markAllAsReadForUser(String username) {
        String sql = """
            INSERT OR REPLACE INTO user_notification_states (username, notification_id, is_read, is_cleared)
            SELECT ?, n.id, 1, COALESCE(uns.is_cleared, 0)
            FROM notifications n
            LEFT JOIN user_notification_states uns ON n.id = uns.notification_id AND uns.username = ?
            WHERE COALESCE(uns.is_read, 0) = 0 AND COALESCE(uns.is_cleared, 0) = 0
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error marking all notifications read for user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void clearAllForUser(String username) {
        String sql = """
            INSERT OR REPLACE INTO user_notification_states (username, notification_id, is_read, is_cleared)
            SELECT ?, n.id, COALESCE(uns.is_read, 0), 1
            FROM notifications n
            LEFT JOIN user_notification_states uns ON n.id = uns.notification_id AND uns.username = ?
            WHERE COALESCE(uns.is_cleared, 0) = 0
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error clearing all notifications for user: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
