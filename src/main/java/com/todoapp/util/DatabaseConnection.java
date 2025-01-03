package com.todoapp.util;

import java.sql.*;
import java.io.*;

public class DatabaseConnection {
    private static final String DB_NAME = "todoapp.db";
    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null || isConnectionClosed()) {
            try {
                // Get user's home directory for database storage
                String userHome = System.getProperty("user.home");
                String appFolder = userHome + File.separator + ".todoapp";
                String dbPath = appFolder + File.separator + DB_NAME;
                File dbFile = new File(dbPath);

                // Create application directory if it doesn't exist
                new File(appFolder).mkdirs();

                // If database doesn't exist, create it and initialize tables
                if (!dbFile.exists()) {
                    initializeDatabase(dbPath);
                }

                // Connect to the database
                String jdbcUrl = "jdbc:sqlite:" + dbPath;
                connection = DriverManager.getConnection(jdbcUrl);
                
                return connection;
            } catch (Exception e) {
                System.err.println("Database connection error: " + e.getMessage());
                throw new RuntimeException("Failed to connect to database", e);
            }
        }
        return connection;
    }

    private static boolean isConnectionClosed() {
        try {
            return connection == null || connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    private static void initializeDatabase(String dbPath) {
        try {
            // Create new database connection
            String jdbcUrl = "jdbc:sqlite:" + dbPath;
            connection = DriverManager.getConnection(jdbcUrl);

            // Create tables
            try (Statement stmt = connection.createStatement()) {
                // Create users table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        email TEXT NOT NULL UNIQUE,
                        password_hash TEXT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """);

                // Create tasks table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS tasks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT,
                        due_date DATE NOT NULL,
                        is_complete BOOLEAN DEFAULT 0,
                        category TEXT NOT NULL,
                        priority TEXT NOT NULL,
                        is_recurring BOOLEAN DEFAULT 0,
                        recurring_interval TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES users(id)
                    )
                """);

                // Create task dependencies table
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS task_dependencies (
                        task_id INTEGER NOT NULL,
                        depends_on_task_id INTEGER NOT NULL,
                        PRIMARY KEY (task_id, depends_on_task_id),
                        FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
                        FOREIGN KEY (depends_on_task_id) REFERENCES tasks(id) ON DELETE CASCADE
                    )
                """);
            }
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}