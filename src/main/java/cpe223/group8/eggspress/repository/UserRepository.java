package cpe223.group8.eggspress.repository;

import cpe223.group8.eggspress.config.DatabaseConfig;
import cpe223.group8.eggspress.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository implements BaseRepository<User> {

    // Replaces the old in-memory array list with a direct DB query mapped with role scopes
    public static List<User> getStaticUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String role = rs.getString("role");
                if (role == null || role.isEmpty()) {
                    role = "Staff";
                }
                users.add(new User(rs.getString("username"), rs.getString("password"), role));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Replaces the local array append with a direct DB insertion preserving role variables
    public static void addStaticUser(User user) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        String clearOldSql = """
            INSERT INTO user_notification_states (username, notification_id, is_read, is_cleared)
            SELECT ?, id, 1, 1 FROM notifications
        """;
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 PreparedStatement pstmtClear = conn.prepareStatement(clearOldSql)) {
                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getRole());
                pstmt.executeUpdate();
                
                pstmtClear.setString(1, user.getUsername());
                pstmtClear.executeUpdate();
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Remove user entry from database by unique username identifier
    public static void deleteStaticUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update role mapping of existing user in persistence layer
    public static void updateStaticUserRole(String username, String newRole) {
        String sql = "UPDATE users SET role = ? WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(User entity) {
        addStaticUser(entity);
    }

    public static int getUsersCount() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public User findById(int id) { return null; }

    @Override
    public List<User> findAll() { return getStaticUsers(); }

    @Override
    public void update(User entity) {}

    @Override
    public void delete(int id) {}
}