package com.todoapp;

import com.todoapp.model.Task;
import com.search.VectorSearch;
import com.todoapp.dao.TaskDAO;
import com.todoapp.dao.TaskDAOImpl;
import java.time.LocalDate;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class TaskManager {
    private TaskDAO taskDAO;
    private final int currentUserId;
    private VectorSearch vectorSearch;

    public TaskManager(int userId) {
        this.currentUserId = userId;
        this.taskDAO = new TaskDAOImpl(userId);
        this.vectorSearch = new VectorSearch(userId);
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public List<Task> getAllTasks() throws SQLException {
        return taskDAO.getAllByUserId(currentUserId);
    }

    public int getTotalTasks() throws SQLException {
        return taskDAO.getTotalTasks();
    }

    public int getCompletedTasks() throws SQLException {
        return taskDAO.getCompletedTasks();
    }

    public int getPendingTasks() throws SQLException {
        return taskDAO.getPendingTasks();
    }

    public Task createTask(Task task) throws SQLException {
        return taskDAO.create(task, currentUserId);
    }

    public void deleteTask(int taskId) throws SQLException {
        taskDAO.delete(taskId);
    }

    public Map<String, Integer> getCategorySummary() throws SQLException {
        return taskDAO.getCategorySummary();
    }

    public void addTask(Task task) throws SQLException {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        
        // Validate required fields
        if (task.getTitle() == null || task.getTitle().trim().isEmpty() ||
            task.getDescription() == null || task.getDescription().trim().isEmpty() ||
            task.getDueDate() == null ||
            task.getPriority() == null) {
            throw new IllegalArgumentException("All task fields are required");
        }
        
        try {
            // Set default values for non-null constraints
            if (task.getCategory() == null) {
                task.setCategory("Default");
            }
            task.setComplete(false);  // Set default completion status
            
            // Create the task in database with current user ID
            taskDAO.create(task, currentUserId);
            
            // Handle recurring tasks
            // Reference to recurring task code:
            // startLine: 1058
            // endLine: 1073
            
        } catch (SQLException e) {
            if (e.getMessage().contains("CONSTRAINT")) {
                throw new SQLException("Failed to add task: Please ensure all required fields are filled");
            }
            throw e;
        }
    }

    public void updateTask(Task task) throws SQLException {
        taskDAO.update(task);
    }

    public void markTaskComplete(int taskId) throws SQLException {
        Task task = taskDAO.getById(taskId);
        task.setComplete(true);
        taskDAO.update(task);
    }

    public List<Task> searchTasks(String keyword) throws SQLException {
        List<Task> tasks = taskDAO.getAllByUserId(currentUserId);
        return tasks.stream()
            .filter(task -> 
                task.getTitle().toLowerCase().contains(keyword.toLowerCase()) || 
                task.getDescription().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
    }

    public boolean wouldCreateCircularDependency(Integer taskId, Integer dependencyId) throws SQLException {
        if (taskId == null || dependencyId == null) return false;
        
        Task dependency = taskDAO.getById(dependencyId);
        while (dependency != null && dependency.getDependencyId() != null) {
            if (dependency.getDependencyId().equals(taskId)) {
                return true;
            }
            dependency = taskDAO.getById(dependency.getDependencyId());
        }
        return false;
    }

    public List<Task> getTasksDueWithin24Hours() throws SQLException {
        List<Task> allTasks = taskDAO.getAllByUserId(currentUserId);
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        
        return allTasks.stream()
            .filter(task -> {
                if (task.isComplete() || task.getDueDate() == null) {
                    return false;
                }
                LocalDate dueDate = task.getDueDate();
                // Check if due date is either today or tomorrow
                return (dueDate.equals(today) || dueDate.equals(tomorrow));
            })
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> vectorSearchTasks(String query) throws SQLException {
        return vectorSearch.searchTasks(query);
    }
}