/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package todolistapp;

/**
 *
 * @author Sajeev
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VectorSearch {
    
       public List<String> searchTasks(String query) {
        List<String> results = new ArrayList<>();
        String sql = "SELECT title, description, due_date, category, priority, completion_status FROM vector_tasks";

        try (Connection conn = DatabaseUtil.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            // Collect descriptions and full task details during the first iteration   
            List<String> descriptions = new ArrayList<>();
            List<Map<String, Object>> taskDetails = new ArrayList<>();
            
            while (rs.next()) {
                descriptions.add(rs.getString("description"));
                Map<String, Object> task = new HashMap<>();
                
                task.put("title", rs.getString("title"));
                task.put("due_date", rs.getString("due_date"));
                task.put("category", rs.getString("category"));
                task.put("completion_status", rs.getString("completion_status"));

                taskDetails.add(task);

            }

            // Generate similarity scores
            String[] descriptionArray = descriptions.toArray(new String[0]);
            String response = HuggingFaceClient.generateEmbedding(query, descriptionArray);

            // Parse similarity scores and filter results
            if (response != null && !response.isEmpty()) {
                
                String sanitizedResponse = response.replace("[", "").replace("]", "");

                // Parse response (mock parsing; adjust based on actual API response)
                String[] scores = sanitizedResponse.split(","); // Adjust parsing logic
                for (int i = 0; i < descriptions.size(); i++) {
                    double similarity = Double.parseDouble(scores[i].trim()); // Trim to remove extra spaces

                    if (similarity > 0.8) {
                        Map<String, Object> task = taskDetails.get(i);
                        String result = String.format(
                            "[%s] %s - Due: %s - Category: %s",
                            task.get("completion_status"), // Completion status at the beginning
                            task.get("title"),                        // Title
                            task.get("due_date"),              // Due date
                            task.get("category")                // Category
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


