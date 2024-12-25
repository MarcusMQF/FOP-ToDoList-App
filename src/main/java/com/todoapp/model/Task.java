package com.todoapp.model;

import java.time.LocalDate;

public class Task {
    private Integer id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private boolean isComplete;
    private String category;
    private String priority;
    private Task dependsOn;
    private boolean isRecurring;
    private String recurringInterval;
    
    // Constructor
    public Task(String title, String description, LocalDate dueDate, String category, String priority, boolean isRecurring, String recurringInterval) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.isComplete = false;
        this.category = category;
        this.priority = priority;
        this.dependsOn = null;
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
    
    public Task getDependsOn() {
        return dependsOn;
    }
    
    public boolean isRecurring() {
        return isRecurring;
    }
    
    public String getRecurringInterval() {
        return recurringInterval;
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
    
    public void setDependsOn(Task dependsOn) {
        this.dependsOn = dependsOn;
    }
    
    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }
    
    public void setRecurringInterval(String recurringInterval) {
        this.recurringInterval = recurringInterval;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}