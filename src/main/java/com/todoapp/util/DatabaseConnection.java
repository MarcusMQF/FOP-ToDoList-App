package com.todoapp.util;

import java.sql.*;
import java.io.*;
import java.nio.file.*;

public class DatabaseConnection {
    private static final String DB_NAME = "todoapp.db";
    private static final String RESOURCE_DB_PATH = "src/main/resources/db/todoapp.db";
    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null || isConnectionClosed()) {
            try {
                String dbPath;
                // Check if running from JAR/EXE
                if (isRunningFromJar()) {
                    // Use user's home directory for JAR/EXE execution
                    String userHome = System.getProperty("user.home");
                    String appFolder = userHome + File.separator + ".todoapp";
                    dbPath = appFolder + File.separator + DB_NAME;
                    
                    // Create directory if it doesn't exist
                    new File(appFolder).mkdirs();
                    
                    // Copy database from resources if it doesn't exist
                    if (!new File(dbPath).exists()) {
                        copyDatabaseFromResources(dbPath);
                    }
                } else {
                    // Use resources database for development
                    dbPath = RESOURCE_DB_PATH;
                }

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

    private static boolean isRunningFromJar() {
        String protocol = DatabaseConnection.class.getResource("").getProtocol();
        return "jar".equals(protocol);
    }

    private static void copyDatabaseFromResources(String destPath) throws IOException {
        try (InputStream is = DatabaseConnection.class.getResourceAsStream("/db/todoapp.db")) {
            if (is == null) {
                // If no database in resources, create new one with schema
                createNewDatabase(destPath);
                return;
            }
            Files.copy(is, Paths.get(destPath), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void createNewDatabase(String dbPath) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             Statement stmt = conn.createStatement()) {
            
            // Execute schema.sql content
            String schema = readSchemaFile();
            stmt.executeUpdate(schema);
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create new database", e);
        }
    }

    private static String readSchemaFile() {
        try (InputStream is = DatabaseConnection.class.getResourceAsStream("/db/schema.sql")) {
            if (is == null) {
                throw new RuntimeException("Schema file not found in resources");
            }
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read schema file", e);
        }
    }

    private static boolean isConnectionClosed() {
        try {
            return connection == null || connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }
}