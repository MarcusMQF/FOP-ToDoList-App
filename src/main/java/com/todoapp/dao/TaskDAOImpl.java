package com.todoapp.dao;

import com.todoapp.model.Task;
import com.todoapp.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskDAOImpl implements TaskDAO {
    private final int currentUserId;
    
    public TaskDAOImpl(int userId) {
        this.currentUserId = userId;
    }

    @Override
    public Task create(Task task, int userId) throws SQLException {
        String sql = "INSERT INTO tasks (user_id, title, description, due_date, category, priority, is_recurring, recurring_interval) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, task.getTitle());
            pstmt.setString(3, task.getDescription());
            pstmt.setString(4, task.getDueDate().toString());
            pstmt.setString(5, task.getCategory());
            pstmt.setString(6, task.getPriority());
            pstmt.setBoolean(7, task.isRecurring());
            pstmt.setString(8, task.getRecurringInterval());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating task failed, no rows affected.");
            }
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    task.setId(rs.getInt(1));
                }
            }
            
            return task;
        }
    }

    @Override
    public Task getById(int id) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE id = ? AND user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.setInt(2, currentUserId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTask(rs);
                }
                return null;
            }
        }
    }

    @Override
    public List<Task> getAllByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE user_id = ?";
        List<Task> tasks = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        }
        return tasks;
    }

    @Override
    public void update(Task task) throws SQLException {
        String sql = "UPDATE tasks SET title = ?, description = ?, due_date = ?, " +
                    "is_complete = ?, category = ?, priority = ?, is_recurring = ?, " +
                    "recurring_interval = ? WHERE id = ? AND user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, task.getDueDate().toString());
            pstmt.setBoolean(4, task.isComplete());
            pstmt.setString(5, task.getCategory());
            pstmt.setString(6, task.getPriority());
            pstmt.setBoolean(7, task.isRecurring());
            pstmt.setString(8, task.getRecurringInterval());
            pstmt.setInt(9, task.getId());
            pstmt.setInt(10, currentUserId);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating task failed, no rows affected.");
            }
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ? AND user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.setInt(2, currentUserId);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting task failed, no rows affected.");
            }
        }
    }

    @Override
    public void setTaskDependency(int taskId, int dependsOnTaskId) throws SQLException {
        // First verify both tasks belong to the current user
        String verifySQL = "SELECT COUNT(*) FROM tasks WHERE id IN (?, ?) AND user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement verifyStmt = conn.prepareStatement(verifySQL)) {
            
            verifyStmt.setInt(1, taskId);
            verifyStmt.setInt(2, dependsOnTaskId);
            verifyStmt.setInt(3, currentUserId);
            
            try (ResultSet rs = verifyStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) != 2) {
                    throw new SQLException("Cannot set dependency: one or both tasks do not belong to the current user");
                }
            }
            
            String sql = "INSERT INTO task_dependencies (task_id, depends_on_task_id) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, taskId);
                pstmt.setInt(2, dependsOnTaskId);
                pstmt.executeUpdate();
            }
        }
    }

    @Override
    public void removeCompletedDependency(int taskId, int completedDependencyId) throws SQLException {
        String sql = "DELETE FROM task_dependencies WHERE task_id = ? AND depends_on_task_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, taskId);
            pstmt.setInt(2, completedDependencyId);
            
            pstmt.executeUpdate();
        }
    }

    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task(
            rs.getString("title"),
            rs.getString("description"),
            LocalDate.parse(rs.getString("due_date")),
            rs.getString("category"),
            rs.getString("priority"),
            rs.getBoolean("is_recurring"),
            rs.getString("recurring_interval")
        );
        task.setId(rs.getInt("id"));
        task.setComplete(rs.getBoolean("is_complete"));
        
        // Load all dependencies
        try (PreparedStatement depStmt = rs.getStatement().getConnection()
                .prepareStatement("SELECT t.* FROM tasks t " +
                                "JOIN task_dependencies td ON t.id = td.depends_on_task_id " +
                                "WHERE td.task_id = ?")) {
            depStmt.setInt(1, task.getId());
            ResultSet depRs = depStmt.executeQuery();
            while (depRs.next()) {  // Changed from if to while to get all dependencies
                Task dependsOn = new Task(
                    depRs.getString("title"),
                    depRs.getString("description"),
                    LocalDate.parse(depRs.getString("due_date")),
                    depRs.getString("category"),
                    depRs.getString("priority"),
                    depRs.getBoolean("is_recurring"),
                    depRs.getString("recurring_interval")
                );
                dependsOn.setId(depRs.getInt("id"));
                dependsOn.setComplete(depRs.getBoolean("is_complete"));
                task.addDependency(dependsOn);
            }
        }
        
        return task;
    }
}