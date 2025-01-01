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
import java.sql.SQLException;

public class TaskDAO {

    public void insertTask(String title, String description, String dueDate, String category, String priority, String completionStatus) {
        String sql = "INSERT INTO vector_tasks (title, description, due_date, category, priority, completion_status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, dueDate);
            pstmt.setString(4, category);
            pstmt.setString(5, priority);
            pstmt.setString(6, completionStatus);

            pstmt.executeUpdate();
            System.out.println("Task added successfully to the database!");

        } catch (SQLException e) {
            System.out.println("Error adding task to database: " + e.getMessage());
        }
    }
}




