package com.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.todoapp.util.DatabaseConnection;

public class VectorSearch {
    private final int currentUserId;

    public VectorSearch(int userId) {
        this.currentUserId = userId;
    }

    public List<Map<String, Object>> searchTasks(String query) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        
        // Check if there are any tasks before proceeding
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM tasks WHERE user_id = ?")) {
            
            pstmt.setInt(1, currentUserId);
            ResultSet countRs = pstmt.executeQuery();
            if (countRs.next() && countRs.getInt(1) == 0) {
                return results;
            }
        }

        String sql = "SELECT id, title, description, due_date, category, priority, is_complete, is_recurring, recurring_interval " +
                    "FROM tasks WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            List<String> descriptions = new ArrayList<>();
            List<Map<String, Object>> taskDetails = new ArrayList<>();

            while (rs.next()) {
                String searchableText = String.format("%s %s %s", 
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("category")
                ).toLowerCase();
                
                descriptions.add(searchableText);
                
                Map<String, Object> task = new HashMap<>();
                task.put("id", rs.getLong("id"));
                task.put("title", rs.getString("title"));
                task.put("description", rs.getString("description"));
                
                // Handle date safely
                String dueDateStr = rs.getString("due_date");
                if (dueDateStr != null) {
                    try {
                        task.put("due_date", LocalDate.parse(dueDateStr));
                    } catch (Exception e) {
                        task.put("due_date", null);
                    }
                } else {
                    task.put("due_date", null);
                }
                
                task.put("category", rs.getString("category"));
                task.put("priority", rs.getString("priority"));
                task.put("is_complete", rs.getBoolean("is_complete"));
                task.put("is_recurring", rs.getBoolean("is_recurring"));
                task.put("recurring_interval", rs.getString("recurring_interval"));
                taskDetails.add(task);
            }

            if (!descriptions.isEmpty()) {
                String[] descArray = descriptions.toArray(new String[0]);
                String response = HuggingFaceClient.generateEmbedding(query, descArray);
                
                if (response != null && !response.isEmpty()) {
                    try {
                        String[] scores = response.replace("[", "").replace("]", "").split(",");
                        for (int i = 0; i < Math.min(scores.length, taskDetails.size()); i++) {
                            double score = Double.parseDouble(scores[i].trim());
                            if (score > 0.22) { // Using 0.2 as threshold like in original code
                                results.add(taskDetails.get(i));
                            }
                        }
                    } catch (NumberFormatException e) {
                        throw new SQLException("Error parsing search results: " + e.getMessage());
                    }
                }
            }
        }
        return results;
    }
}
