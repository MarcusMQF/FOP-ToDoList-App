package com.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public List<String> searchTasks(String query) {
        List<String> results = new ArrayList<>();
        
        // Check if there are any tasks before proceeding
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM tasks WHERE user_id = ?")) {
            
            pstmt.setInt(1, currentUserId);
            ResultSet countRs = pstmt.executeQuery();
            if (countRs.next() && countRs.getInt(1) == 0) {
                return results; // Return empty list if no tasks exist
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return results;
        }

        String sql = "SELECT title, description, due_date, is_complete, category FROM tasks WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            // Collect descriptions and full task details during the first iteration   
            List<String> descriptions = new ArrayList<>();
            List<Map<String, Object>> taskDetails = new ArrayList<>();

            while (rs.next()) {
                // Combine title, description, and category for better semantic matching
                String searchableText = String.format("%s %s %s", 
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("category")
                ).toLowerCase();
                
                descriptions.add(searchableText);
                Map<String, Object> task = new HashMap<>();

                task.put("title", rs.getString("title"));
                task.put("due_date", rs.getString("due_date"));
                task.put("category", rs.getString("category"));
                task.put("is_complete", rs.getString("is_complete"));

                taskDetails.add(task);
            }

            // Generate similarity scores
            String[] descriptionArray = descriptions.toArray(new String[0]);
            if (descriptions.isEmpty()) {
                System.out.println("\u001b[31mDEBUG:\u001b[0m No task descriptions found to search.");
                return results;
            }

            String response = HuggingFaceClient.generateEmbedding(query, descriptionArray);
            if (response == null) {
                System.out.println("\u001b[31mDEBUG:\u001b[0m HuggingFace API returned null response.");
                return results;
            }
            
            // Parse similarity scores and filter results
            if (response != null && !response.isEmpty()) {

                String sanitizedResponse = response.replace("[", "").replace("]", "");

                // Parse response (mock parsing; adjust based on actual API response)
                String[] scores = sanitizedResponse.split(","); // Adjust parsing logic
                for (int i = 0; i < descriptions.size(); i++) {
                    double similarity = Double.parseDouble(scores[i].trim()); // Trim to remove extra spaces

                    if (similarity > 0.2) {
                        Map<String, Object> task = taskDetails.get(i);
                        String result = String.format(
                            "[%s] %s - Due: %s - Category: %s",
                            "1".equals(task.get("is_complete").toString()) ? "Completed" : "Incomplete",
                            task.get("title"),
                            task.get("due_date"),
                            task.get("category")
                        );
                        results.add(result);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }
}
