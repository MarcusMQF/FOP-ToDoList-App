package com.todoapp.util;

import java.sql.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:src/main/resources/db/todoapp.db";
    private static Connection connection = null;
    private static boolean isInitialized = false;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            // Create db directory if it doesn't exist
            new File("src/main/resources/db").mkdirs();
            
            // Create or get connection
            connection = DriverManager.getConnection(DB_URL);
            
            // Initialize database if needed
            if (!isInitialized) {
                initializeDatabase();
            }
        }
        return connection;
    }

    private static void initializeDatabase() throws SQLException {
        // Check if tables exist
        if (!tablesExist()) {
            // Execute schema only if tables don't exist
            executeSchema();
        }
        isInitialized = true;
    }

    private static boolean tablesExist() throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet tables = meta.getTables(null, null, "tasks", null)) {
            return tables.next();
        }
    }

    private static void executeSchema() throws SQLException {
        try {
            InputStream inputStream = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("db/schema.sql");
            
            if (inputStream == null) {
                throw new SQLException("Could not find schema.sql");
            }

            String schema = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            
            try (Statement stmt = connection.createStatement()) {
                for (String statement : schema.split(";")) {
                    if (!statement.trim().isEmpty()) {
                        stmt.execute(statement.trim());
                    }
                }
            }
        } catch (IOException e) {
            throw new SQLException("Error reading schema file: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}