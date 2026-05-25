package cpe223.group8.eggspress.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {

    // Using the user home path configuration that resolved the path issue
    private static final String DB_URL = "jdbc:sqlite:" + System.getProperty("user.home") + "/eggspress.db";

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.err.println("Failed to connect to the SQLite database at: " + DB_URL);
            throw e;
        }
    }

    public static void initializeDatabase() {
        String createUserTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL
            );
        """;

        String createInventoryTable = """
            CREATE TABLE IF NOT EXISTS inventory (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                quantity REAL NOT NULL,
                unit TEXT NOT NULL
            );
        """;

        String createSchedulesTable = """
            CREATE TABLE IF NOT EXISTS schedules (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category TEXT NOT NULL,
                time TEXT NOT NULL,
                feeding_type TEXT NOT NULL,
                status TEXT NOT NULL
            );
        """;

        String createAutomationsTable = """
            CREATE TABLE IF NOT EXISTS automations (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                location TEXT NOT NULL,
                amount REAL NOT NULL,
                status TEXT NOT NULL
            );
        """;

        String createCoopsTable = """
            CREATE TABLE IF NOT EXISTS coops (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                flock_count INTEGER NOT NULL,
                status TEXT NOT NULL
            );
        """;

        String createNotificationsTable = """
            CREATE TABLE IF NOT EXISTS notifications (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
                level TEXT NOT NULL,
                message TEXT NOT NULL,
                is_read INTEGER DEFAULT 0
            );
        """;

        String createUserNotificationStatesTable = """
            CREATE TABLE IF NOT EXISTS user_notification_states (
                username TEXT NOT NULL,
                notification_id INTEGER NOT NULL,
                is_read INTEGER DEFAULT 0,
                is_cleared INTEGER DEFAULT 0,
                PRIMARY KEY (username, notification_id)
            );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 1. Ensure all structural tables exist
            stmt.execute(createUserTable);
            stmt.execute(createInventoryTable);
            stmt.execute(createSchedulesTable);
            stmt.execute(createAutomationsTable);
            stmt.execute(createCoopsTable);
            stmt.execute(createNotificationsTable);
            stmt.execute(createUserNotificationStatesTable);
            
            // 2. Check and seed standard admin user
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.execute("INSERT INTO users (username, password) VALUES ('admin', '123');");
                    System.out.println("Default 'admin' user successfully seeded.");
                }
            }

            // 3. NEW: Check and seed default starting inventory stocks
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM inventory")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String seedInventory = """
                        INSERT INTO inventory (id, name, category, quantity, unit) VALUES 
                        ('INV001', 'Grains', 'Feed', 500.0, 'kg'),
                        ('INV002', 'Water', 'Hydration', 1200.0, 'L'),
                        ('INV003', 'Layers Feed', 'Feed', 250.0, 'kg'),
                        ('INV004', 'Vitamins', 'Medical', 15.0, 'L');
                    """;
                    stmt.execute(seedInventory);
                    System.out.println("Default chicken inventory stocks successfully seeded.");
                }
            }

            // 4. NEW: Check and seed default starting coops
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM coops")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String seedCoops = """
                        INSERT INTO coops (id, name, flock_count, status) VALUES 
                        ('COOP001', 'Main Coop A', 450, 'Optimal'),
                        ('COOP002', 'Chicls Facility', 320, 'Optimal'),
                        ('COOP003', 'Coop B', 400, 'Monitoring'),
                        ('COOP004', 'Breeding Barn', 250, 'Optimal');
                    """;
                    stmt.execute(seedCoops);
                    System.out.println("Default chicken coops successfully seeded.");
                }
            }
            
            System.out.println("Database tables checked and initialized successfully.");
        } catch (SQLException e) {
            System.err.println("CRITICAL Error initializing database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}