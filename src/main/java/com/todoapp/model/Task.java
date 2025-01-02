package com.todoapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private Integer id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private boolean isComplete;
    private String category;
    private String priority;
    private List<Task> dependencies = new ArrayList<>();
    private boolean isRecurring;
    private String recurringInterval;
    private LocalDateTime createdAt;
    private Integer dependencyId;
    
    // Constructor
    public Task() {
        this.dependencies = new ArrayList<>();
        this.isComplete = false;
        this.isRecurring = false;
    }
    
    public Task(String title, String description, LocalDate dueDate, String category, String priority, boolean isRecurring, String recurringInterval) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.isComplete = false;
        this.category = category;
        this.priority = priority;
        this.dependencies = new ArrayList<>();
        this.isRecurring = isRecurring;
        this.recurringInterval = recurringInterval;
    }

    // Getters
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public boolean isComplete() {
        return isComplete;
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public List<Task> getDependencies() {
        return dependencies;
    }
    
    public boolean isRecurring() {
        return isRecurring;
    }
    
    public String getRecurringInterval() {
        return recurringInterval;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    // Setters
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public void setComplete(boolean complete) {
        isComplete = complete;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public void addDependency(Task task) {
        if (!dependencies.contains(task)) {
            dependencies.add(task);
        }
    }
    
    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }
    
    public void setRecurringInterval(String recurringInterval) {
        this.recurringInterval = recurringInterval;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDependencyId() {
        return dependencyId;
    }

    public void setDependencyId(Integer dependencyId) {
        this.dependencyId = dependencyId;
    }

    public void setDependencies(List<Task> dependencies) {
        this.dependencies = dependencies;
    }

    public boolean canComplete() {
        return dependencies.stream().allMatch(Task::isComplete);
    }
}