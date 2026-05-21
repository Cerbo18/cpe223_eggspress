package com.eggspress.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    // [todo]

    private static Connection getConnection() throws SQLException {
        // [todo]
        final String DB_URL = "jdbc:sqlite:database/eggspress.db";

        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.err.println("Failed to connect to the SQLite database at: " + DB_URL);
            throw e;
        }
        
    }
}
