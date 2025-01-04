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
        
        // Check for direct circular dependency
        if (taskId.equals(dependencyId)) return true;
        
        // Set to keep track of visited task IDs
        Set<Integer> visited = new HashSet<>();
        visited.add(taskId);
        
        // Check for indirect circular dependencies using DFS
        return hasCircularDependency(dependencyId, visited);
    }

    private boolean hasCircularDependency(Integer currentTaskId, Set<Integer> visited) throws SQLException {
        Task currentTask = taskDAO.getById(currentTaskId);
        if (currentTask == null) return false;
        
        // Get all dependencies of current task
        List<Task> dependencies = currentTask.getDependencies();
        
        for (Task dependency : dependencies) {
            Integer dependencyId = dependency.getId();
            
            // If we've already visited this task, we found a cycle
            if (visited.contains(dependencyId)) {
                return true;
            }
            
            // Add current task to visited set
            visited.add(dependencyId);
            
            // Recursively check dependencies
            if (hasCircularDependency(dependencyId, visited)) {
                return true;
            }
            
            // Remove current task from visited set when backtracking
            visited.remove(dependencyId);
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

    public void setTaskDependency(int taskId, int dependsOnTaskId) throws SQLException {
        if (taskId == dependsOnTaskId) {
            throw new IllegalArgumentException("A task cannot depend on itself");
        }
        taskDAO.setTaskDependency(taskId, dependsOnTaskId);
    }
}