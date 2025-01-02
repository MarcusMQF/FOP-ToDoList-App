package com.todoapp.service;

import com.todoapp.TaskManager;
import com.todoapp.model.Task;
import com.todoapp.model.User;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskReminderService {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final TaskManager taskManager;
    private final User user;

    public TaskReminderService(TaskManager taskManager, User user) {
        this.taskManager = taskManager;
        this.user = user;
    }

    public void startReminderService() {
        System.out.println("Starting reminder service for user: " + user.getUsername());
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Checking for due tasks...");
                List<Task> dueTasks = taskManager.getTasksDueWithin24Hours();
                System.out.println("Found " + dueTasks.size() + " tasks due within 24 hours");
                
                if (!dueTasks.isEmpty()) {
                    String taskDetails = formatTaskDetails(dueTasks);
                    System.out.println("Sending reminder email to: " + user.getEmail());
                    EmailService.sendTaskReminderEmail(user.getEmail(), user.getUsername(), taskDetails);
                }
            } catch (Exception e) {
                System.err.println("Error in reminder service: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.HOURS);
    }

    private String formatTaskDetails(List<Task> tasks) {
        StringBuilder details = new StringBuilder();
        for (Task task : tasks) {
            details.append(String.format("%s (Due: %s)\n", 
                task.getTitle(), 
                task.getDueDate().toString()));
        }
        return details.toString();
    }

    public void stopReminderService() {
        scheduler.shutdown();
    }
} 