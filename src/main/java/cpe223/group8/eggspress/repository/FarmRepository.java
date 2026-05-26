package cpe223.group8.eggspress.repository;

import cpe223.group8.eggspress.config.DatabaseConfig;
import cpe223.group8.eggspress.models.Automation;
import cpe223.group8.eggspress.models.FeedingSchedule;
import cpe223.group8.eggspress.models.InventoryItem;
import cpe223.group8.eggspress.models.ChickenHouse;
import cpe223.group8.eggspress.models.MonthlyConsumptionLog;
import cpe223.group8.eggspress.models.ChickenGrowthPoint;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FarmRepository {

    // --- Temporary Static Fallbacks to prevent other dashboard files from breaking ---
    private static final List<InventoryItem> staticInventory = new ArrayList<>();
    private static final List<FeedingSchedule> staticSchedules = new ArrayList<>();
    private static final List<Automation> staticAutomations = new ArrayList<>();

    public static List<InventoryItem> getStaticInventory() { return staticInventory; }
    public static List<FeedingSchedule> getStaticSchedules() { return staticSchedules; }
    public static List<Automation> getStaticAutomations() { return staticAutomations; }
    public static void addStaticSchedule(FeedingSchedule s) { staticSchedules.add(s); }
    public static void addStaticAutomation(Automation a) { staticAutomations.add(a); }

    // --- LIVE SQLITE DATABASE METHODS ---

    // 1. Fetch Inventory filtered by category type
    public static List<InventoryItem> getInventoryByCategory(String category) {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM inventory WHERE category = ? OR name = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            pstmt.setString(2, "Water"); 
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new InventoryItem(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("quantity"),
                        rs.getString("unit")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // 2. Perform inventory deduction updates inside SQLite
    public static boolean updateInventoryQuantity(String id, double newQuantity) {
        String sql = "UPDATE inventory SET quantity = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newQuantity);
            pstmt.setString(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2b. Fetch an inventory item by ID
    public static InventoryItem getInventoryItemById(String id) {
        String sql = "SELECT * FROM inventory WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new InventoryItem(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("quantity"),
                        rs.getString("unit")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 2c. Update a schedule's status in SQLite
    public static boolean updateScheduleStatus(String time, String feedingType, String category, String newStatus) {
        String sql = "UPDATE schedules SET status = ? WHERE time = ? AND feeding_type = ? AND category = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setString(2, time);
            pstmt.setString(3, feedingType);
            pstmt.setString(4, category);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating schedule status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 3. Fetch all recorded automation schedules
    public static List<FeedingSchedule> getAllSchedules() {
        List<FeedingSchedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedules";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                schedules.add(new FeedingSchedule(
                    rs.getString("category"),
                    rs.getString("time"),
                    rs.getString("feeding_type"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

    public static void addSchedule(FeedingSchedule schedule) {
    String sql = "INSERT INTO schedules (category, time, feeding_type, status) VALUES (?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection()) {
        
        // Force auto-commit to TRUE to ensure data writes immediately
        conn.setAutoCommit(true); 
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, schedule.getCategory());
            pstmt.setString(2, schedule.getTime());
            pstmt.setString(3, schedule.getFeedingType());
            pstmt.setString(4, schedule.getStatus());
            pstmt.executeUpdate();
        }
        System.out.println("Inserted schedule successfully: " + schedule.getFeedingType());
    } catch (SQLException e) {
        System.err.println("Database insert error: " + e.getMessage());
        e.printStackTrace();
    }
}

    // 5. Append Automation Tracking Metrics Log
    public static void addAutomationLog(Automation auto) {
        String sql = "INSERT INTO automations (id, name, location, amount, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, auto.getId());
            
            // CHANGED: Match your model's exact getter name here!
            pstmt.setString(2, auto.getWaterSource()); 
            
            pstmt.setString(3, auto.getLocation());
            pstmt.setDouble(4, auto.getAmount());
            pstmt.setString(5, auto.getStatus());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 6. Get Automation log row counts to auto-increment log IDs safely
    public static int getAutomationCount() {
        String sql = "SELECT COUNT(*) FROM automations";
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
    
    // 7. NEW: Fetch ALL items currently stored inside the SQLite inventory table
    public static List<InventoryItem> getAllInventory() {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM inventory";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new InventoryItem(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("quantity"),
                    rs.getString("unit")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all inventory items: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }
    
    // Modified to ensure your items write directly to SQLite instead of a temporary static list
    public static void addStaticItem(InventoryItem item) {
        boolean success = addInventoryItem(item);
        if (success) {
            System.out.println("Item successfully persisted to SQLite database: " + item.getName());
        } else {
            System.err.println("Failed to persist item to SQLite database: " + item.getName());
        }
    }

    // 8. LIVE DATABASE METHOD: Save a newly created item straight into SQLite
    public static boolean addInventoryItem(InventoryItem item) {
        String sql = "INSERT INTO inventory (id, name, category, quantity, unit) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getName());
            pstmt.setString(3, item.getCategory());
            pstmt.setDouble(4, item.getQuantity());
            pstmt.setString(5, item.getUnit());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving inventory item to DB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // 1. Update an item's absolute quantity based on model state
    public static boolean updateItemQuantity(InventoryItem item) {
        String sql = "UPDATE inventory SET quantity = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, item.getQuantity());
            pstmt.setString(2, item.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating item quantity: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 2. Remove an entire item row from the database completely
    public static boolean removeStaticItem(InventoryItem item) {
        String sql = "DELETE FROM inventory WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting item from database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 3. Get total items row count to ensure safe automated incremental ID creation
    public static int getInventoryCount() {
        String sql = "SELECT COUNT(*) FROM inventory";
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
    
    public static boolean removeSchedule(FeedingSchedule schedule) {
    String sql = "DELETE FROM schedules WHERE time = ? AND feeding_type = ? AND category = ?";
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, schedule.getTime());
        pstmt.setString(2, schedule.getFeedingType());
        pstmt.setString(3, schedule.getCategory());
        
        return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
        System.err.println("Error deleting schedule from database: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

    // 9. Fetch all recorded active coops (ChickenHouses) inside SQLite
    public static List<ChickenHouse> getAllCoops() {
        List<ChickenHouse> coops = new ArrayList<>();
        String sql = "SELECT * FROM coops";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                coops.add(new ChickenHouse(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getInt("flock_count"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all coops from DB: " + e.getMessage());
            e.printStackTrace();
        }
        return coops;
    }

    // Retrieves all historical monthly logs from the monthly_consumption_logs table sorted by date descending
    public static List<MonthlyConsumptionLog> getAllMonthlyLogs() {
        List<MonthlyConsumptionLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM monthly_consumption_logs ORDER BY month_year DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(new MonthlyConsumptionLog(
                    rs.getInt("id"),
                    rs.getString("month_year"),
                    rs.getInt("flock_count"),
                    rs.getDouble("estimated_feed"),
                    rs.getDouble("estimated_water"),
                    rs.getDouble("actual_feed"),
                    rs.getDouble("actual_water"),
                    rs.getString("logged_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Database query failure fetching all monthly consumption logs: " + e.getMessage());
            e.printStackTrace();
        }
        return logs;
    }

    // Persists a newly completed monthly consumption log into the SQLite database
    public static boolean addMonthlyLog(MonthlyConsumptionLog log) {
        String sql = "INSERT INTO monthly_consumption_logs (month_year, flock_count, estimated_feed, estimated_water, actual_feed, actual_water) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, log.getMonthYear());
            pstmt.setInt(2, log.getFlockCount());
            pstmt.setDouble(3, log.getEstimatedFeed());
            pstmt.setDouble(4, log.getEstimatedWater());
            pstmt.setDouble(5, log.getActualFeed());
            pstmt.setDouble(6, log.getActualWater());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database execution error saving monthly consumption log entry: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Deletes an existing monthly log record from database using its unique identifier key
    public static boolean removeMonthlyLog(int id) {
        String sql = "DELETE FROM monthly_consumption_logs WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database delete operations failure for monthly log record ID: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Evaluates if a monthly ledger key already exists to prevent duplicate entries
    public static boolean isMonthYearLogged(String monthYear) {
        String sql = "SELECT COUNT(*) FROM monthly_consumption_logs WHERE month_year = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, monthYear);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed checking month existence constraints: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // 10. Add a new coop to SQLite
    public static boolean addCoop(ChickenHouse coop) {
        String sql = "INSERT INTO coops (id, name, flock_count, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, coop.getId());
            pstmt.setString(2, coop.getName());
            pstmt.setInt(3, coop.getFlockCount());
            pstmt.setString(4, coop.getStatus());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding coop to DB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 11. Remove an existing coop from SQLite
    public static boolean removeCoop(String id) {
        String sql = "DELETE FROM coops WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing coop from DB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 11b. Update an existing coop in SQLite
    public static boolean updateCoop(ChickenHouse coop) {
        String sql = "UPDATE coops SET name = ?, flock_count = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, coop.getName());
            pstmt.setInt(2, coop.getFlockCount());
            pstmt.setString(3, coop.getStatus());
            pstmt.setString(4, coop.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating coop in DB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 11c. Update flock count of a coop and record/synchronize the total farm flock size historically
    public static boolean updateCoopFlockCountAndLogHistory(String coopId, int newFlockCount, String dateStr) {
        String updateCoopSql = "UPDATE coops SET flock_count = ? WHERE id = ?";
        String sumFlockSql = "SELECT SUM(flock_count) FROM coops";
        String logHistorySql = """
            INSERT INTO chicken_growth (record_date, flock_count, average_weight)
            VALUES (?, ?, 1.8)
            ON CONFLICT(record_date) DO UPDATE SET flock_count = excluded.flock_count;
        """;
        
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);
            
            // A. Update specific coop flock count
            try (PreparedStatement pstmt1 = conn.prepareStatement(updateCoopSql)) {
                pstmt1.setInt(1, newFlockCount);
                pstmt1.setString(2, coopId);
                pstmt1.executeUpdate();
            }
            
            // B. Sum flock counts across all coops to get total farm population
            int totalFlock = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sumFlockSql)) {
                if (rs.next()) {
                    totalFlock = rs.getInt(1);
                }
            }
            
            // C. Insert or update record in historical chicken_growth for this date
            try (PreparedStatement pstmt2 = conn.prepareStatement(logHistorySql)) {
                pstmt2.setString(1, dateStr);
                pstmt2.setInt(2, totalFlock);
                pstmt2.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Transaction failed for flock count update and historical logging: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // 12. Fetch historical chicken growth data points filtered by start/end dates
    public static List<ChickenGrowthPoint> getGrowthHistory(String startDate, String endDate) {
        List<ChickenGrowthPoint> history = new ArrayList<>();
        String sql = "SELECT * FROM chicken_growth WHERE record_date >= ? AND record_date <= ? ORDER BY record_date ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new ChickenGrowthPoint(
                        rs.getString("record_date"),
                        rs.getInt("flock_count"),
                        rs.getDouble("average_weight")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching chicken growth history: " + e.getMessage());
            e.printStackTrace();
        }
        return history;
    }

    public static int getCoopsCount() {
        String sql = "SELECT COUNT(*) FROM coops";
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

    public static boolean injectDeveloperMockStocks() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE inventory SET quantity = quantity + 100 WHERE id = 'INV001';");
            stmt.execute("UPDATE inventory SET quantity = quantity + 200 WHERE id = 'INV002';");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean injectDeveloperMockSchedule() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            String insertSql = "INSERT INTO schedules (category, time, feeding_type, status) VALUES " +
                               "('Broiler', '16:00', 'Mixed Grain', 'Pending');";
            stmt.execute(insertSql);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}