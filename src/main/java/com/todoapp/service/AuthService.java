package com.todoapp.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.todoapp.util.DatabaseConnection;

public class AuthService {
    public void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
            
            // Also delete all user's tasks
            String deleteTasks = "DELETE FROM tasks WHERE user_id = ?";
            try (PreparedStatement taskStmt = conn.prepareStatement(deleteTasks)) {
                taskStmt.setInt(1, userId);
                taskStmt.executeUpdate();
            }
        }
    }
} 