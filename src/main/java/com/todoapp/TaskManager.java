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

    public void addTask(Scanner scanner) {
        System.out.println("\u001B[31m=== Add a New Task ===\u001B[0m");

        // Title validation with minimum length
        String title;
        while(true) {
            System.out.print("Enter task title: ");
            title = scanner.nextLine().trim();
            if(title.isEmpty()) {
                System.out.println("\u001b[31mERROR:\u001b[0m Title cannot be empty. Please enter a title.");
                System.out.println();
            } else if (title.length() < 3) {  // Minimum 3 characters
                System.out.println("\u001b[31mERROR:\u001b[0m Title must be at least 3 characters long.");
                System.out.println();
            } else {
                break;
            }
        }
        
        // Description validation with minimum length
        String description;
        while(true) {
            System.out.print("Enter task description: ");
            description = scanner.nextLine().trim();
            if(description.isEmpty()) {
                System.out.println("\u001b[31mERROR:\u001b[0m Description cannot be empty. Please enter a description.");
                System.out.println();
            } else if(description.length() < 5) {  // Minimum 5 characters
                System.out.println("\u001b[31mERROR:\u001b[0m Description must be at least 5 characters long.");
                System.out.println();
            } else {
                break;
            }
        }

        // Due date validation
        LocalDate dueDate;
        while(true) {
            try {
                System.out.print("Enter due date (YYYY-MM-DD): ");
                String dateInput = scanner.nextLine().trim();
        
                if(dateInput.isEmpty()) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Date cannot be empty. Please enter a date.");
                    System.out.println();
                    continue;
                }

                dateInput = dateInput.replace("/", "-");
                dueDate = LocalDate.parse(dateInput);
        
                if(dueDate.isBefore(LocalDate.now())) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Due date cannot be in the past!");
                    System.out.println();
                    continue;
                }
                break;
            } catch(Exception e) {
                System.out.println("\u001b[31mERROR:\u001b[0m Invalid date format! Please use YYYY-MM-DD.");
                System.out.println();
            }
        }

        // Category validation
        String category;
        while(true) {
            System.out.print("Enter task category (Homework, Personal, Work): ");
            category = scanner.nextLine();
            if(category.equalsIgnoreCase("Homework") || 
               category.equalsIgnoreCase("Personal") || 
               category.equalsIgnoreCase("Work")) {
               category = capitalize(category);
               break;
            }
            System.out.println("\u001b[31mERROR:\u001b[0m Invalid Category! Please choose Homework, Personal or Work.");
            System.out.println();
        }

        // Priority validation
        String priority;
        while(true) {
            System.out.print("Enter task priority (High, Medium, Low): ");
            priority = scanner.nextLine();
            if(priority.equalsIgnoreCase("High") ||
               priority.equalsIgnoreCase("Medium") ||
               priority.equalsIgnoreCase("Low")) {
               priority = capitalize(priority);
               break;
            }
            System.out.println("\u001b[31mERROR:\u001b[0m Invalid Priority! Please choose High, Medium or Low.");
            System.out.println();
        }
        
        // Recurring task validation
        boolean isRecurring = false;
        String recurringInterval = null;
        while (true) {
            System.out.print("Is this task recurring? (yes/no): ");
            String recurringInput = scanner.nextLine().trim().toLowerCase();
            
            if (recurringInput.isEmpty()) {
                System.out.println("\u001b[31mERROR:\u001b[0m Please enter 'yes' or 'no'.");
                System.out.println();
                continue;
            }
            
            if (!recurringInput.equals("yes") && !recurringInput.equals("no")) {
                System.out.println("\u001b[31mERROR:\u001b[0m Invalid input. Please enter 'yes' or 'no'.");
                System.out.println();
                continue;
            }
            
            isRecurring = recurringInput.equals("yes");
            
            if (isRecurring) {
                while (true) {
                    System.out.print("Enter recurring interval (Daily, Weekly, Monthly): ");
                    recurringInterval = scanner.nextLine().trim();
                    
                    if (recurringInterval.isEmpty()) {
                        System.out.println("\u001b[31mERROR:\u001b[0m Interval cannot be empty.");
                        System.out.println();
                        continue;
                    }
                    
                    String normalizedInterval = recurringInterval.toLowerCase();
                    if (!normalizedInterval.equals("daily") && 
                        !normalizedInterval.equals("weekly") && 
                        !normalizedInterval.equals("monthly")) {
                        System.out.println("\u001b[31mERROR:\u001b[0m Invalid interval. Please enter Daily, Weekly, or Monthly.");
                        System.out.println();
                        continue;
                    }
                    
                    recurringInterval = capitalize(recurringInterval);
                    break;
                }
            }
            
            try {
                Task newTask = new Task(title, description, dueDate, category, priority, isRecurring, recurringInterval);
                taskDAO.create(newTask, currentUserId);
                System.out.println("\n\u001b[32mTask [" + title + "] added successfully!\u001b[0m");
            } catch (SQLException e) {
                System.out.println("\u001b[31mERROR:\u001b[0m Failed to save task to database: " + e.getMessage());
            }
            break;
        }
    }

    public void displayTasks() {
        try {
            List<Task> tasks = taskDAO.getAllByUserId(currentUserId);
            if (tasks.isEmpty()) {
                System.out.println("\u001b[31mWARNING:\u001b[0m No tasks found.");
                return;
            }

            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                System.out.printf("%d. [%s] %s%n", 
                    i + 1,
                    task.isComplete() ? "Completed" : "Incomplete",
                    task.getTitle());
                System.out.printf("   Due: %s | Category: %s | Priority: %s%s%n", 
                    task.getDueDate(),
                    task.getCategory(),
                    task.getPriority(),
                    formatDependencies(task));
            }
        } catch (SQLException e) {
            System.out.println("\u001b[31mERROR:\u001b[0m Database error: " + e.getMessage());
        }
    }

    public void viewAllTasks() {
        try {
            List<Task> tasks = taskDAO.getAllByUserId(currentUserId);

            System.out.println("\u001B[31m=== View All Tasks ===\u001B[0m");
            if (tasks.isEmpty()) {
                System.out.println("\u001b[31mWARNING:\u001b[0m No tasks found.");
                return;
            }

            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                System.out.printf("%d. [%s] %s%n", 
                    i + 1,
                    task.isComplete() ? "Completed" : "Incomplete",
                    task.getTitle());
                System.out.printf("    Due: %s | Category: %s | Priority: %s%s%n", 
                    task.getDueDate(),
                    task.getCategory(),
                    task.getPriority(),
                    formatDependencies(task));
            }

            System.out.println("\nThe total number of tasks created is " + tasks.size() + ".");
        } catch (SQLException e) {
            System.out.println("\u001b[31mERROR:\u001b[0m Database error: " + e.getMessage());
        }
    }

    public void markTaskComplete(Scanner scanner) {
        System.out.println("\u001B[31m=== Mark Task as Complete ===\u001B[0m");
        try {
            List<Task> tasks = taskDAO.getAllByUserId(currentUserId);
            if (tasks.isEmpty()) {
                System.out.println("\u001b[31mWARNING:\u001b[0m No tasks found.");
                return;
            }
            
            displayTasks();

            while (true) {
                try {
                    System.out.print("\nEnter the task number to mark as complete: ");
                    String input = scanner.nextLine().trim();
                    
                    if (input.isEmpty()) {
                        System.out.println("\u001b[31mERROR:\u001b[0m Task number cannot be empty.");
                        continue;
                    }

                    int taskNum = Integer.parseInt(input);
                    
                    if (taskNum < 1 || taskNum > tasks.size()) {
                        System.out.println("\u001b[31mERROR:\u001b[0m Invalid task number.");
                        continue;
                    }
                    
                    Task task = tasks.get(taskNum - 1);
                    
                    // Check if the task's dependency is completed
                    if (task.getDependencies().stream().anyMatch(t -> !t.isComplete())) {
                        System.out.println("\u001b[31mWARNING:\u001b[0m Task [" + task.getTitle() + 
                            "] cannot be marked as complete because it depends on [" + 
                            task.getDependencies().stream()
                                .filter(t -> !t.isComplete())
                                .map(Task::getTitle)
                                .collect(Collectors.joining(", ")) + "]. Please complete them first.");
                        return;
                    }
                    
                    task.setComplete(true);
                    taskDAO.update(task);
                    
                    cleanupDependenciesForCompletedTask(task);
                    
                    System.out.println("\n\u001b[32mTask [" + task.getTitle() + "] marked as completed!\u001b[0m");
                    
                    if (task.isRecurring()) {
                        LocalDate newDueDate = calculateNewDueDate(task.getDueDate(), task.getRecurringInterval());
                        Task recurringTask = new Task(task.getTitle(), task.getDescription(), newDueDate,
                            task.getCategory(), task.getPriority(), true, task.getRecurringInterval());
                        taskDAO.create(recurringTask, currentUserId);
                        System.out.println("\nRecurring task created with new due date: " + newDueDate);
                    }
                    
                    break;
                    
                } catch (SQLException e) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Database error: " + e.getMessage());
                    break;
                }
            }
        } catch (SQLException e) {
            System.out.println("\u001b[31mERROR:\u001b[0m Database error: " + e.getMessage());
        }
    }

    public void deleteTask(Scanner scanner) {
        System.out.println("\u001B[31m=== Delete a Task ===\u001B[0m");
        try {
            List<Task> tasks = taskDAO.getAllByUserId(currentUserId);
            if (tasks.isEmpty()) {
                System.out.println("\u001b[31mWARNING:\u001b[0m No tasks found.");
                return;
            }
            
            displayTasks();

            while (true) {
                try {
                    System.out.print("\nEnter the task number to delete: ");
                    String input = scanner.nextLine().trim();
                    
                    if (input.isEmpty()) {
                        System.out.println("\u001b[31mERROR:\u001b[0m Task number cannot be empty.");
                        continue;
                    }

                    int taskNum = Integer.parseInt(input);
                    
                    if (taskNum < 1 || taskNum > tasks.size()) {
                        System.out.println("\u001b[31mERROR:\u001b[0m Invalid task number.");
                        continue;
                    }
                    
                    Task task = tasks.get(taskNum - 1);
                    taskDAO.delete(task.getId());
                    System.out.println("\n\u001b[32mTask [" + task.getTitle() + "] deleted successfully!\u001b[0m");
                    break;
                    
                } catch (NumberFormatException e) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Please enter a valid number.");
                }
            }
        } catch (SQLException e) {
            System.out.println("\u001b[31mERROR:\u001b[0m Database error: " + e.getMessage());
        }
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private LocalDate calculateNewDueDate(LocalDate currentDueDate, String interval) {
        return switch (interval.toUpperCase()) {
            case "DAILY" -> currentDueDate.plusDays(1);
            case "WEEKLY" -> currentDueDate.plusWeeks(1);
            case "MONTHLY" -> currentDueDate.plusMonths(1);
            default -> throw new IllegalArgumentException("Invalid recurring interval");
        };
    }

    public void sortTasks(Scanner scanner) {
        System.out.println("\u001B[31m=== Sort Tasks ===\u001B[0m");
        System.out.println("Sort by:");
        System.out.println("1. Due Date (Ascending)");
        System.out.println("2. Due Date (Descending)");
        System.out.println("3. Priority (High to Low)");
        System.out.println("4. Priority (Low to High)");
        
        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                System.out.println("\u001b[31mERROR:\u001b[0m Your choice cannot be empty.");
                continue; // Repeat the prompt for a valid choice
            }

            try {
                int choice = Integer.parseInt(input);
                List<Task> tasks = taskDAO.getAllByUserId(currentUserId);
                String choiceName = "";

                switch (choice) {
                    case 1:
                        tasks.sort(Comparator.comparing(Task::getDueDate));
                        choiceName = "Due Date (Ascending)";
                        break;
                    case 2:
                        tasks.sort(Comparator.comparing(Task::getDueDate).reversed());
                        choiceName = "Due Date (Descending)";
                        break;
                    case 3:
                        tasks.sort((t1, t2) -> comparePriority(t2.getPriority(), t1.getPriority()));
                        choiceName = "Priority (High to Low)";
                        break;
                    case 4:
                        tasks.sort((t1, t2) -> comparePriority(t1.getPriority(), t2.getPriority()));
                        choiceName = "Priority (Low to High)";
                        break;
                    default:
                        System.out.println("\u001b[31mERROR:\u001b[0m Please input a valid choice between 1-4.");
                        continue; // Repeat the prompt for a valid choice
                }

                System.out.printf("\n\u001b[32mTasks sorted by %s!\u001b[0m\n\n", choiceName);
                System.out.println("\u001B[31m=== Tasks Sorted ===\u001B[0m");
                displaySortedTasks(tasks);
                break; // Exit the loop after successful sorting
            } catch (NumberFormatException e) {
                System.out.println("\u001b[31mERROR:\u001b[0m Please input a valid choice between 1-4.");
            } catch (SQLException e) {
                System.out.println("\u001b[31mERROR:\u001b[0m Database error: " + e.getMessage());
            }
        }
    }

    private int comparePriority(String p1, String p2) {
        Map<String, Integer> priorityMap = new HashMap<>();
        priorityMap.put("High", 3);
        priorityMap.put("Medium", 2);
        priorityMap.put("Low", 1);
        
        // Get priority values, defaulting to 0 if priority is not found
        int priority1 = priorityMap.getOrDefault(p1, 0);
        int priority2 = priorityMap.getOrDefault(p2, 0);
        
        return Integer.compare(priority1, priority2);
    }

    private void displaySortedTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("\u001b[31mWARNING:\u001b[0m No tasks found.");
            return;
        }
        
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            System.out.printf("%d. [%s] %s%n", 
                i + 1,
                task.isComplete() ? "Completed" : "Incomplete",
                task.getTitle());
            System.out.printf("   Due: %s | Category: %s | Priority: %s%s%n", 
                task.getDueDate(),
                task.getCategory(),
                task.getPriority(),
                formatDependencies(task));
        }
    }

    public void searchTasks(Scanner scanner) {
        System.out.println("\u001B[31m=== Search Tasks ===\u001B[0m");
        System.out.print("Enter a keyword to search by title or description: ");
        String keyword = scanner.nextLine().toLowerCase();
        
        try {
            List<Task> tasks = taskDAO.getAllByUserId(currentUserId);
            List<Task> matchingTasks = tasks.stream()
                .filter(task -> 
                    task.getTitle().toLowerCase().contains(keyword) || 
                    task.getDescription().toLowerCase().contains(keyword))
                .collect(Collectors.toList());

            System.out.println("\n\u001b[31m=== Search Results ===\u001b[0m");
            if (matchingTasks.isEmpty()) {
                System.out.println("\u001b[31mWARNING:\u001b[0m No tasks found matching your search.");
                return;
            }

            for (int i = 0; i < matchingTasks.size(); i++) {
                Task task = matchingTasks.get(i);
                System.out.printf("%d. [%s] %s%n", 
                    i + 1,
                    task.isComplete() ? "Completed" : "Incomplete",
                    task.getTitle());
                System.out.printf("   Description: %s%n", task.getDescription());
                System.out.printf("   Due: %s | Category: %s | Priority: %s%s%n", 
                    task.getDueDate(),
                    task.getCategory(),
                    task.getPriority(),
                    formatDependencies(task));
            }
        } catch (SQLException e) {
            System.out.println("\u001b[31mERROR:\u001b[0m Database error: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> vectorSearchTasks(String query) {
        try {
            return vectorSearch.searchTasks(query);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to perform vector search: " + e.getMessage(), e);
        }
    }

    public void editTask(Scanner scanner) {
        System.out.println("\u001B[31m=== Edit Task ===\u001B[0m");
        try {
            List<Task> tasks = taskDAO.getAllByUserId(currentUserId);
            if (tasks.isEmpty()) {
                System.out.println("\u001b[31mWARNING:\u001b[0m No tasks found.");
                return;
            }
            
            displayTasks();

            while (true) {
                try {
                    System.out.print("\nEnter task number to edit: ");
                    String input = scanner.nextLine().trim();
                    
                    if (input.isEmpty()) {
                        System.out.println("\u001b[31mERROR:\u001b[0m Task number cannot be empty.");
                        continue;
                    }

                    int taskNum = Integer.parseInt(input);
                    
                    if (taskNum < 1 || taskNum > tasks.size()) {
                        System.out.println("\u001b[31mERROR:\u001b[0m Invalid task number.");
                        continue;
                    }

                    Task task = tasks.get(taskNum - 1);
                    System.out.println("\nWhat would you like to edit?");
                    System.out.println("1. Title");
                    System.out.println("2. Description");
                    System.out.println("3. Due Date");
                    System.out.println("4. Category");
                    System.out.println("5. Priority");
                    System.out.println("6. Set Task Dependency");
                    System.out.println("7. Cancel");
                    System.out.print("\n> ");
                    
                    while (true) {
                        String choiceInput = scanner.nextLine().trim();
                        
                        if (choiceInput.isEmpty()) {
                            System.out.println("\u001b[31mERROR:\u001b[0m Please enter a valid choice.");
                            System.out.println();
                            System.out.print("> ");
                            continue; // Prompt again for input
                        }

                        try {
                            int choice = Integer.parseInt(choiceInput);
                            if (choice < 1 || choice > 7) {
                                System.out.println("\u001b[31mERROR:\u001b[0m Please enter a valid choice.");
                                System.out.println();
                                System.out.print("> ");
                                continue; // Prompt again for input
                            }

                            switch (choice) {
                                case 1:
                                    while (true) {
                                        System.out.print("\nEnter new title: ");
                                        String title = scanner.nextLine().trim();
                                        if (title.length() >= 3) {
                                            task.setTitle(title);
                                            break;
                                        }
                                        System.out.println("\u001b[31mERROR:\u001b[0m Title must be at least 3 characters long.");
                                    }
                                    break;
                                case 2:
                                    while (true) {
                                        System.out.print("\nEnter new description: ");
                                        String description = scanner.nextLine().trim();
                                        if (description.length() >= 5) {
                                            task.setDescription(description);
                                            break;
                                        }
                                        System.out.println("\u001b[31mERROR:\u001b[0m Description must be at least 5 characters long.");
                                    }
                                    break;
                                case 3:
                                    while (true) {
                                        try {
                                            System.out.print("\nEnter new due date (YYYY-MM-DD): ");
                                            LocalDate dueDate = LocalDate.parse(scanner.nextLine().trim());
                                            if (!dueDate.isBefore(LocalDate.now())) {
                                                task.setDueDate(dueDate);
                                                break;
                                            }
                                            System.out.println("\u001b[31mERROR:\u001b[0m Due date cannot be in the past!");
                                        } catch (Exception e) {
                                            System.out.println("\u001b[31mERROR:\u001b[0m Invalid date format! Please use YYYY-MM-DD.");
                                        }
                                    }
                                    break;
                                case 4:
                                    while (true) {
                                        System.out.print("\nEnter new category (Homework, Personal, Work): ");
                                        String category = scanner.nextLine().trim();
                                        if (category.equalsIgnoreCase("Homework") || 
                                            category.equalsIgnoreCase("Personal") || 
                                            category.equalsIgnoreCase("Work")) {
                                            task.setCategory(capitalize(category));
                                            break;
                                        }
                                        System.out.println("\u001b[31mERROR:\u001b[0m Invalid category.");
                                    }
                                    break;
                                case 5:
                                    while (true) {
                                        System.out.print("\nEnter new priority (High, Medium, Low): ");
                                        String priority = scanner.nextLine().trim();
                                        if (priority.equalsIgnoreCase("High") || 
                                            priority.equalsIgnoreCase("Medium") || 
                                            priority.equalsIgnoreCase("Low")) {
                                            task.setPriority(capitalize(priority));
                                            break;
                                        }
                                        System.out.println("\u001b[31mERROR:\u001b[0m Invalid priority.");
                                    }
                                    break;
                                case 6:
                                    System.out.println();
                                    boolean dependencySet = handleSetDependencyInEdit(scanner, task, tasks);
                                    if (!dependencySet) {
                                        return; // Return without showing "Task updated successfully"
                                    }
                                    break;
                                case 7:
                                    return; // Exit if canceling
                            }

                            taskDAO.update(task);
                            System.out.println("\n\u001b[32mTask updated successfully!\u001b[0m");
                            return; // Exit the editTask method after successful update

                        } catch (NumberFormatException e) {
                            System.out.println("\u001b[31mERROR:\u001b[0m Please enter a valid choice.");
                            System.out.println();
                            System.out.print("> ");
                        }
                    }

                } catch (NumberFormatException e) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Please enter a valid number.");
                } catch (SQLException e) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Database error: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("\u001b[31mERROR:\u001b[0m Database error: " + e.getMessage());
        }
    }

    public void viewTaskDetails(Scanner scanner) {
        System.out.println("\u001B[31m=== View Task Details ===\u001B[0m");
        try {
            List<Task> tasks = taskDAO.getAllByUserId(currentUserId);
            if (tasks.isEmpty()) {
                System.out.println("\u001b[31mWARNING:\u001b[0m No tasks found.");
                return;
            }
            
            displayTasks();

            while (true) {
                try {
                    System.out.print("\nEnter task number to view details: ");
                    String input = scanner.nextLine().trim();
                    
                    if (input.isEmpty()) {
                        System.out.println("\u001b[31mERROR:\u001b[0m Task Number cannot be empty.");
                        continue;
                    }

                    int taskNum = Integer.parseInt(input);
                    
                    if (taskNum < 1 || taskNum > tasks.size()) {
                        System.out.println("\u001b[31mERROR:\u001b[0m Invalid Task Number.");
                        continue;
                    }

                    Task task = tasks.get(taskNum - 1);
                    System.out.println("\n\u001b[35mTask Details:\u001b[0m");
                    System.out.println("Title: " + task.getTitle());
                    System.out.println("Description: " + task.getDescription());
                    System.out.println("Due Date: " + task.getDueDate());
                    System.out.println("Category: " + task.getCategory());
                    System.out.println("Priority: " + task.getPriority());
                    System.out.println("Status: " + (task.isComplete() ? "Completed" : "Incomplete"));
                    System.out.println("Recurring: " + (task.isRecurring() ? "Yes" : "No"));
                    if (task.isRecurring()) {
                        System.out.println("Recurring Interval: " + task.getRecurringInterval());
                    }
                    if (task.getDependencies().stream().anyMatch(t -> !t.isComplete())) {
                        System.out.println("Depends on: " + task.getDependencies().stream()
                            .filter(t -> !t.isComplete())
                            .map(Task::getTitle)
                            .collect(Collectors.joining(", ")));
                    }
                    break;
                    
                } catch (NumberFormatException e) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Please enter a valid number.");
                }
            }
        } catch (SQLException e) {
            System.out.println("\u001b[31mERROR:\u001b[0m Database error: " + e.getMessage());
        }
    }

    private boolean wouldCreateCycle(Task precedingTask, Task dependentTask, List<Task> allTasks) throws SQLException {
        // First check for direct cycle
        if (precedingTask.getId() == dependentTask.getId()) {
            return true;
        }

        // Create a map to simulate the dependency chain including the new dependency
        Map<Integer, List<Task>> dependencyGraph = new HashMap<>();
        
        // Build dependency graph for all tasks
        for (Task task : allTasks) {
            dependencyGraph.put(task.getId(), new ArrayList<>(task.getDependencies()));
        }
        
        // Add the proposed new dependency to the graph
        if (!dependencyGraph.containsKey(dependentTask.getId())) {
            dependencyGraph.put(dependentTask.getId(), new ArrayList<>());
        }
        dependencyGraph.get(dependentTask.getId()).add(precedingTask);

        // Start from the preceding task
        Task tortoise = precedingTask;
        Task hare = precedingTask;
        
        while (true) {
            // Move tortoise one step
            List<Task> tortoiseDeps = dependencyGraph.get(tortoise.getId());
            if (tortoiseDeps.isEmpty()) {
                return false; // No cycle found - reached end of chain
            }
            tortoise = tortoiseDeps.get(0);
            
            // Move hare two steps
            for (int i = 0; i < 2; i++) {
                List<Task> hareDeps = dependencyGraph.get(hare.getId());
                if (hareDeps.isEmpty()) {
                    return false; // No cycle found - reached end of chain
                }
                hare = hareDeps.get(0);
                
                // Check if we've found the dependent task in the chain
                if (hare.getId() == dependentTask.getId()) {
                    return true; // Found a cycle
                }
            }
            
            // If tortoise meets hare, we found a cycle
            if (tortoise.getId() == hare.getId()) {
                return true;
            }
        }
    }

    public void setTaskDependency(Scanner scanner) {
        System.out.println("\u001B[31m=== Set Task Dependency ===\u001B[0m");
        try {
            List<Task> tasks = taskDAO.getAllByUserId(currentUserId);
            if (tasks.size() < 2) {
                System.out.println("\u001b[31mERROR:\u001b[0m You need at least 2 tasks to set up dependencies.");
                return;
            }

            displayTasks();
            
            while (true) {
                System.out.print("\nEnter task number that depends on another task: ");
                String input = scanner.nextLine().trim();
                
                if (input.isEmpty()) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Please choose between 1-" + tasks.size() + ".");
                    continue;
                }

                try {
                    int dependentTaskNum = Integer.parseInt(input);
                    
                    if (dependentTaskNum < 1 || dependentTaskNum > tasks.size()) {
                        System.out.println("\u001b[31mERROR:\u001b[0m Invalid task number.");
                        continue;
                    }

                    Task dependentTask = tasks.get(dependentTaskNum - 1);
                    
                    // Validate dependent task first
                    if (!validateDependentTask(dependentTask)) {
                        return;
                    }
                    
                    System.out.print("Enter the task number it depends on: ");
                    int precedingTaskNum = Integer.parseInt(scanner.nextLine().trim());
                    
                    if (precedingTaskNum < 1 || precedingTaskNum > tasks.size()) {
                        System.out.println("\u001b[31mERROR:\u001b[0m Invalid task number.");
                        continue;
                    }

                    Task precedingTask = tasks.get(precedingTaskNum - 1);

                    // Validate depends-on task
                    if (!validateDependsOnTask(precedingTask)) {
                        return;
                    }

                    if (dependentTaskNum == precedingTaskNum) {
                        System.out.println("\u001b[31mERROR:\u001b[0m A task cannot depend on itself.");
                        return;
                    }

                    // Check for existing dependency
                    if (hasExistingDependency(dependentTask, precedingTask)) {
                        System.out.println("\u001b[31mWARNING:\u001b[0m Task [" + dependentTask.getTitle() + 
                            "] already depends on [" + precedingTask.getTitle() + "]!");
                        return;
                    }

                    // Check for circular dependency (both direct and indirect)
                    if (wouldCreateCycle(precedingTask, dependentTask, tasks)) {
                        System.out.println("\u001b[31mERROR:\u001b[0m This would create a circular dependency!");
                        return;
                    }

                    try {
                        dependentTask.addDependency(precedingTask);
                        taskDAO.setTaskDependency(dependentTask.getId(), precedingTask.getId());
                        
                        String dependencyList = dependentTask.getDependencies().stream()
                            .map(Task::getTitle)
                            .collect(Collectors.joining(", "));
                        System.out.printf("\n\u001b[32mTask [%s] now depends on: %s\u001b[0m\n", 
                            dependentTask.getTitle(), dependencyList);
                        break;
                    } catch (SQLException e) {
                        if (e.getMessage().contains("UNIQUE constraint failed")) {
                            System.out.println("\u001b[31mWARNING:\u001b[0m Task [" + dependentTask.getTitle() + 
                                "] already depends on [" + precedingTask.getTitle() + "]!");
                        } else {
                            throw e;
                        }
                        return;
                    }

                } catch (NumberFormatException e) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Please enter a valid number.");
                }
            }
        } catch (SQLException e) {
            System.out.println("\u001b[31mERROR:\u001b[0m Database error: " + e.getMessage());
        }
    }

    private String formatDependencies(Task task) {
        if (task.getDependencies().isEmpty()) {
            return "";
        }
        
        // Show all dependencies regardless of completion status
        return " | Depends on: " + task.getDependencies().stream()
            .map(Task::getTitle)
            .collect(Collectors.joining(", "));
    }

    private boolean hasExistingDependency(Task task, Task dependency) {
        return task.getDependencies().stream()
            .filter(t -> !t.isComplete())
            .anyMatch(t -> t.getId() == dependency.getId());
    }

    private boolean handleSetDependencyInEdit(Scanner scanner, Task task, List<Task> allTasks) throws SQLException {
        System.out.println("\u001B[31m=== Set Task Dependency ===\u001B[0m");
        
        if (allTasks.size() < 2) {
            System.out.println("\u001b[31mERROR:\u001b[0m You need at least 2 tasks to set up dependencies.");
            return false;
        }
        
        displayTasks();
        
        // Validate dependent task first
        if (!validateDependentTask(task)) {
            return false;
        }
        
        System.out.print("\nEnter the task number it depends on: ");
        int precedingTaskNum = Integer.parseInt(scanner.nextLine().trim());
        
        if (precedingTaskNum < 1 || precedingTaskNum > allTasks.size()) {
            System.out.println("\u001b[31mERROR:\u001b[0m Invalid task number.");
            return false;
        }

        Task precedingTask = allTasks.get(precedingTaskNum - 1);

        // Validate depends-on task
        if (!validateDependsOnTask(precedingTask)) {
            return false;
        }

        if (task.getId() == precedingTask.getId()) {
            System.out.println("\u001b[31mERROR:\u001b[0m A task cannot depend on itself.");
            return false;
        }

        // Check for existing dependency
        if (hasExistingDependency(task, precedingTask)) {
            System.out.println("\u001b[33mWARNING:\u001b[0m Task [" + task.getTitle() + 
                "] already depends on [" + precedingTask.getTitle() + "]!");
            return false;
        }

        // Check for circular dependency
        if (wouldCreateCycle(precedingTask, task, allTasks)) {
            System.out.println("\u001b[31mERROR:\u001b[0m This would create a circular dependency!");
            return false;
        }

        try {
            task.addDependency(precedingTask);
            taskDAO.setTaskDependency(task.getId(), precedingTask.getId());
            
            String dependencyList = task.getDependencies().stream()
                .map(Task::getTitle)
                .collect(Collectors.joining(", "));
            System.out.printf("\n\u001b[32mTask [%s] now depends on: %s\u001b[0m\n", 
                task.getTitle(), dependencyList);
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("\u001b[31mWARNING:\u001b[0m Task [" + task.getTitle() + 
                    "] already depends on [" + precedingTask.getTitle() + "]!");
            } else {
                throw e;
            }
            return false;
        }
    }

    private void cleanupDependenciesForCompletedTask(Task completedTask) throws SQLException {
        // Get all tasks that might depend on the completed task
        List<Task> allTasks = taskDAO.getAllByUserId(currentUserId);
        
        for (Task task : allTasks) {
            // Check if this task depends on the completed task
            for (Task dependency : task.getDependencies()) {
                if (dependency.getId() == completedTask.getId()) {
                    // Remove the completed dependency from database
                    taskDAO.removeCompletedDependency(task.getId(), completedTask.getId());
                    // Remove from memory
                    task.getDependencies().remove(dependency);
                    break;
                }
            }
        }
    }

    private boolean validateDependentTask(Task task) {
        if (task.isComplete()) {
            System.out.println("\u001b[31mWARNING:\u001b[0m Cannot set task [" + task.getTitle() + 
                "] as dependent since it is already completed.");
            return false;
        }
        return true;
    }

    private boolean validateDependsOnTask(Task task) {
        if (task.isComplete()) {
            System.out.println("\u001b[31mWARNING:\u001b[0m Cannot set dependency on [" + task.getTitle() + 
                "] as it is already completed.");
            return false;
        }
        return true;
    }
    public void displayAnalytics() {
        try {
            // Fetch analytics
            int totalTasks = taskDAO.getTotalTasks();
            int completedTasks = taskDAO.getCompletedTasks();
            int pendingTasks = taskDAO.getPendingTasks();
            Map<String, Integer> categorySummary = taskDAO.getCategorySummary();

            // Compute completion rate
            double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;

            // Format category summary
            StringBuilder categorySummaryString = new StringBuilder();
            for (Map.Entry<String, Integer> entry : categorySummary.entrySet()) {
                categorySummaryString.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
            }
            if (categorySummaryString.length() > 0) {
                categorySummaryString.setLength(categorySummaryString.length() - 2);
            }

            // Display analytics
            System.out.println("\u001B[31m=== Analytics Dashboard ===\u001B[0m");
            System.out.println("- Total Tasks: " + totalTasks);
            System.out.println("- Completed: " + completedTasks);
            System.out.println("- Incomplete: " + pendingTasks);
            System.out.printf("- Completion Rate: %.2f%%\n", completionRate);
            System.out.println("- Task Categories: " + categorySummaryString);

        } catch (SQLException e) {
            System.out.println("Error generating analytics: " + e.getMessage());
            e.printStackTrace();
        }
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
}