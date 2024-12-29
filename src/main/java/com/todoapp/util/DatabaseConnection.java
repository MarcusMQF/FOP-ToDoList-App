package com.todoapp.util;

import java.sql.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:src/main/resources/db/todoapp.db";
    private static Connection connection = null;
    private static boolean isInitialized = false;
    
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL);
            
            // Only initialize once
            if (!isInitialized) {
                checkAndInitializeDatabase();
                isInitialized = true;
            }
        }
        return connection;
    }
    
    private static void checkAndInitializeDatabase() throws SQLException {
        // Check if tables exist
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet tables = metaData.getTables(null, null, "users", null);
        
        if (!tables.next()) {
            // Tables don't exist, initialize them
            try (InputStream inputStream = DatabaseConnection.class.getClassLoader()
                    .getResourceAsStream("db/schema.sql")) {
                
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
                throw new SQLException("Error reading schema file", e);
            }
        }
        tables.close();
    }
    
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
                isInitialized = false;
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}