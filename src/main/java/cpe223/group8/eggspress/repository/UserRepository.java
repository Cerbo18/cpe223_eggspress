package cpe223.group8.eggspress.repository;

import cpe223.group8.eggspress.config.DatabaseConfig;
import cpe223.group8.eggspress.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository implements BaseRepository<User> {

    // Replaces the old in-memory array list with a direct DB query
    public static List<User> getStaticUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(rs.getString("username"), rs.getString("password")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Replaces the local array append with a direct DB insertion
    public static void addStaticUser(User user) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
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

    @Override
    public void save(User entity) {
        addStaticUser(entity);
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