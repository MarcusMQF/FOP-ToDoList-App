import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TaskManager {
    private List<Task> tasks;
    private int taskCounter;

    public TaskManager() {
        tasks = new ArrayList<>();
        taskCounter = 0;
    }

    public void addTask(Scanner scanner) {
        System.out.println("\u001B[31m=== Add a New Task ===\u001B[0m");

        // Title validation with minimum length
        String title;
        while(true){
            System.out.print("Enter task title: ");
            title = scanner.nextLine().trim();
            if(title.isEmpty()){
                System.out.println("\u001b[31mERROR:\u001b[0m Title cannot be empty. Please enter a title.");
                System.out.println();
            }else if (title.length() < 3){  // Minimum 3 characters
                System.out.println("\u001b[31mERROR:\u001b[0m Title must be at least 3 characters long.");
                System.out.println();
            }else{
                break;
            }
        }
        
        // Description validation with minimum length
        String description;
        while(true){
            System.out.print("Enter task description: ");
            description = scanner.nextLine().trim();
            if(description.isEmpty()) {
                System.out.println("\u001b[31mERROR:\u001b[0m Description cannot be empty. Please enter a description.");
                System.out.println();
            }else if(description.length() < 5){  // Minimum 5 characters
                System.out.println("\u001b[31mERROR:\u001b[0m Description must be at least 5 characters long.");
                System.out.println();
            }else{
                break;
            }
        }

        // Error Handling (Input Validation)
        LocalDate dueDate;
        while(true){
            try{
                System.out.print("Enter due date (YYYY-MM-DD): ");
                String dateInput = scanner.nextLine().trim();
        
                // Check for empty input
                if(dateInput.isEmpty()){
                    System.out.println("\u001b[31mERROR:\u001b[0m Date cannot be empty. Please enter a date.");
                    System.out.println();
                    continue;  // Skip rest of loop and start over
                }

                // Replace "/" with "-" if user uses slashes
                dateInput = dateInput.replace("/", "-");
        
                // Parse the date
                dueDate = LocalDate.parse(dateInput);
        
                // Check if date is in the past
                if(dueDate.isBefore(LocalDate.now())){
                    System.out.println("\u001b[31mERROR:\u001b[0m Due date cannot be in the past!");
                    System.out.println();
                    continue;  // Skip rest of loop and start over
                }
        
                break;  // Valid date, exit loop
        
            }catch(Exception e){
                System.out.println("\u001b[31mERROR:\u001b[0m Invalid date format! Please use YYYY-MM-DD.");
                System.out.println();
            }
        }
        // parse() request user to enter valid date type, 
        // It automatically throws a "DateTimeParseException" if the format is wrong
        // Use error handling to
        //-Catch exception
        //-Print user friendly's error messege
        //-Allowing User to try again

        // Error Handling (Input Validation)
        String category;
        while(true){
            System.out.print("Enter task category (Homework, Personal, Work): ");
            category = scanner.nextLine();
            if(category.equalsIgnoreCase("Homework") || 
               category.equalsIgnoreCase("Personal") || 
               category.equalsIgnoreCase("Work")){
               category = capitalize(category);  // Normalize to "Homework", "Personal", "Work"
               break;
            }
            System.out.println("\u001b[31mERROR:\u001b[0m Invalid Category! Please choose Homework, Personal or Work.");
            System.out.println();
        }

        String priority;
        while(true){
            System.out.print("Enter task priority (High, Medium, Low): ");
            priority = scanner.nextLine();
            if(priority.equalsIgnoreCase("High") ||
               priority.equalsIgnoreCase("Medium") ||
               priority.equalsIgnoreCase("Low")){
               priority = capitalize(priority);  // Normalize to "High", "Medium", "Low"
               break;
            }
            System.out.println("\u001b[31mError:\u001b[0m Invalid Priority! Please choose High, Medium or Low.");
            System.out.println();
        }
        
        // Recurring task validation
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
            
            boolean isRecurring = recurringInput.equals("yes");
            
            if (isRecurring) {
                while (true) {
                    System.out.print("Enter recurring interval (Daily, Weekly, Monthly): ");
                    recurringInterval = scanner.nextLine().trim();
                    
                    if (recurringInterval.isEmpty()) {
                        System.out.println("\u001b[31mERROR:\u001b[0m Interval cannot be empty.");
                        System.out.println();
                        continue;
                    }
                    
                    // Normalize input to lowercase for comparison
                    String normalizedInterval = recurringInterval.toLowerCase();
                    if (!normalizedInterval.equals("daily") && 
                        !normalizedInterval.equals("weekly") && 
                        !normalizedInterval.equals("monthly")) {
                        System.out.println("\u001b[31mERROR:\u001b[0m Invalid interval. Please enter Daily, Weekly, or Monthly.");
                        System.out.println();
                        continue;
                    }
                    
                    // Capitalize first letter for consistency
                    recurringInterval = recurringInterval.substring(0, 1).toUpperCase() + 
                                      recurringInterval.substring(1).toLowerCase();
                    break;
                }
            }
            
            Task newTask = new Task(title, description, dueDate, category, priority, isRecurring, recurringInterval);
            tasks.add(newTask);
            taskCounter++;
            System.out.println("\n\u001b[32mTask [" + title + "] added successfully!\u001b[0m");
            break;
        }
    }

    public void markTaskComplete(Scanner scanner) {
        System.out.println("\u001B[31m=== Mark Task as Complete ===\u001B[0m");
        displayTasks();

        while (true) {  // Add while loop to allow retries
            try {
                System.out.print("Enter the task number you want to mark as complete: ");
                String input = scanner.nextLine().trim();
                
                // Check for empty input
                if (input.isEmpty()) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Task number cannot be empty.");
                    System.out.println();
                    continue;  // Go back to start of loop
                }

                // Convert to integer after checking for empty
                int taskNum = Integer.parseInt(input) - 1;
                
                // Check for out of range
                if (taskNum < 0 || taskNum >= tasks.size()) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Invalid task number. Please enter a valid number.");
                    System.out.println();
                    continue;  // Go back to start of loop
                }
                
                Task task = tasks.get(taskNum);

                // Check dependencies
                if (task.getDependsOn() != null && !task.getDependsOn().isComplete()) {
                    System.out.println("\u001b[31mWARNING:\u001b[0m Task \"" + task.getTitle() + 
                        "\" cannot be marked as complete because it depends on [" + 
                        task.getDependsOn().getTitle() + "]. Please complete [" + 
                        task.getDependsOn().getTitle() + "] first.");
                    System.out.println();
                    return;  // Return here as this is a logical constraint, not an input error
                }
                
                task.setComplete(true);
                System.out.println("\n\u001b[32mTask [" + task.getTitle() + "] marked as completed!\u001b[0m");

                if (task.isRecurring()) {
                    LocalDate newDueDate = calculateNewDueDate(task.getDueDate(), task.getRecurringInterval());
                    Task recurringTask = new Task(task.getTitle(), task.getDescription(), newDueDate, 
                        task.getCategory(), task.getPriority(), true, task.getRecurringInterval());
                    tasks.add(recurringTask);
                    System.out.println("\nRecurring task \"" + recurringTask.getTitle() + 
                        "\" added with new due date: " + newDueDate);
                }
                
                break;  // Exit loop on success
                
            } catch (NumberFormatException e) {
                System.out.println("\u001b[31mERROR:\u001b[0m Please enter a valid number.");
                System.out.println();
                continue;  // Go back to start of loop
            }
        }
    }

    public void deleteTask(Scanner scanner) {
        System.out.println("\u001B[31m=== Delete a Task ===\u001B[0m");
        displayTasks();

        while (true) {  // Add while loop for retries
            try {
                System.out.print("Enter the task number you want to delete: ");
                String input = scanner.nextLine().trim();
                
                // Check for empty input
                if (input.isEmpty()) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Task number cannot be empty.");
                    System.out.println();
                    continue;
                }

                // Convert to integer after checking for empty
                int taskNum = Integer.parseInt(input) - 1;
                
                // Check for out of range
                if (taskNum < 0 || taskNum >= tasks.size()) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Invalid task number. Please enter a valid number.");
                    System.out.println();
                    continue;
                }
                
                // Original delete logic
                Task removedTask = tasks.remove(taskNum);
                taskCounter--;
                System.out.println("\u001b[32mTask [" + removedTask.getTitle() + "] deleted successfully!\u001b[0m");
                break;  // Exit loop after successful deletion
                
            } catch (NumberFormatException e) {
                System.out.println("\u001b[31mERROR:\u001b[0m Please enter a valid number.");
                System.out.println();
                continue;
            }
        }
    }

    public void sortTasks(Scanner scanner) {
        System.out.println("\u001B[31m=== Sort Tasks ===\u001B[0m");
        System.out.println("Sort by:");
        System.out.println("1. Due Date (Ascending)");
        System.out.println("2. Due Date (Descending)");
        System.out.println("3. Priority (High to Low)");
        System.out.println("4. Priority (Low to High)");
        System.out.print("\n> ");
        
        int choice = Integer.parseInt(scanner.nextLine());
        String choiceName = "";
        
        switch (choice) {
            case 1:
                tasks.sort(Comparator.comparing(Task::getDueDate));
                break;
            case 2:
                tasks.sort(Comparator.comparing(Task::getDueDate).reversed());
                break;
            case 3:
                tasks.sort((t1, t2) -> comparePriority(t2.getPriority(), t1.getPriority()));
                break;
            case 4:
                tasks.sort((t1, t2) -> comparePriority(t1.getPriority(), t2.getPriority()));
                break;
        }

        if(choice == 1){
            choiceName = "Due Date (Ascending)";
        }else if(choice ==2){
            choiceName = "Due Date (Descending)";
        }else if(choice == 3){
            choiceName = "Priority (High to Low)";
        }else if(choice == 4){
            choiceName = "Priority (Low to High)";
        }

        System.out.printf("\n\u001b[32mTasks sorted by %s!\u001b[0m", choiceName);
        System.out.println();
    }

    private int comparePriority(String p1, String p2) {
        Map<String, Integer> priorityMap = Map.of(
            "High", 3,
            "Medium", 2,
            "Low", 1
        );
        return priorityMap.get(p1) - priorityMap.get(p2);
    }

    // Helper method to capitalize input
    private String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    public void searchTasks(Scanner scanner) {
        System.out.println("\u001B[31m=== Search Tasks ===\u001B[0m");
        System.out.print("Enter a keyword to search by title or description: ");
        String keyword = scanner.nextLine().toLowerCase();
        
        System.out.println("\n\u001b[31m=== Search Results ===\u001b[0m");
        boolean found = false;
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.getTitle().toLowerCase().contains(keyword) || 
                task.getDescription().toLowerCase().contains(keyword)) {
                printTask(task, i + 1);
                found = true;
            }
        }
        if (!found) {
            System.out.println("\u001b[31mWARNING:\u001b[0m No tasks found matching your search.");
        }
    }

    public void setTaskDependency(Scanner scanner) {
        System.out.println("\u001B[31m=== Set Task Dependency ===\u001B[0m");
        displayTasks();
        
        // Check if there are enough tasks
        if (tasks.size() < 2) {
            System.out.println("\u001b[31mERROR:\u001b[0m You need at least 2 tasks to set up dependencies.");
            return;
        }

        while (true) {  // Add while loop for retries
            try {
                System.out.print("Enter task number that depends on another task: ");
                String input = scanner.nextLine().trim();
                
                // Check for empty input
                if (input.isEmpty()) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Task number cannot be empty.");
                    System.out.println();
                    continue;
                }

                // Convert to integer after checking for empty
                int dependentTaskNum = Integer.parseInt(input) - 1;
                
                // Validate first task number
                if (dependentTaskNum < 0 || dependentTaskNum >= tasks.size()) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Invalid task number. Please enter a number between 1 and " + tasks.size() + ".");
                    System.out.println();
                    continue;
                }
                
                System.out.print("Enter the task number it depends on: ");
                input = scanner.nextLine().trim();
                
                // Check for empty input for second task
                if (input.isEmpty()) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Task number cannot be empty.");
                    System.out.println();
                    continue;
                }
                
                // Convert to integer after checking for empty
                int precedingTaskNum = Integer.parseInt(input) - 1;
                
                // Validate second task number
                if (precedingTaskNum < 0 || precedingTaskNum >= tasks.size()) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Invalid task number. Please enter a number between 1 and " + tasks.size() + ".");
                    System.out.println();
                    continue;
                }
                
                // Check if trying to make a task depend on itself
                if (dependentTaskNum == precedingTaskNum) {
                    System.out.println("\u001b[31mERROR:\u001b[0m A task cannot depend on itself.");
                    System.out.println();
                    continue;
                }

                Task dependentTask = tasks.get(dependentTaskNum);
                Task precedingTask = tasks.get(precedingTaskNum);
                
                // Check for circular dependency
                if (wouldCreateCycle(precedingTask, dependentTask)) {
                    System.out.println("\u001b[31mERROR:\u001b[0m This would create a circular dependency!");
                    System.out.println();
                    continue;
                }
                
                dependentTask.setDependsOn(precedingTask);
                System.out.println("\n\u001b[32mTask [" + dependentTask.getTitle() + 
                    "] now depends on [" + precedingTask.getTitle() + "].\u001b[0m");
                break;  // Exit loop after successful dependency set
                
            } catch (NumberFormatException e) {
                System.out.println("\u001b[31mERROR:\u001b[0m Please enter a valid number.");
                System.out.println();
                continue;
            }
        }
    }

    private boolean wouldCreateCycle(Task start, Task newDependency) {
        // Create an empty HashSet to track visited tasks
        Set<Task> visited = new HashSet<>();

        // Start with the first task in the chain
        Task current = start;

        // Keep going until we reach a task with no dependencies (null)
        while (current != null) {
            // Try to add current task to visited set
            // visited.add() returns false if task is already in set
            if (!visited.add(current)) {
                return true; // Found a cycle - task was already visited
            }
            if (current == newDependency) {
                return true; // Found a cycle - would create circular dependency
            }
            current = current.getDependsOn();
        }
        return false; // No cycle found
    }

    public void editTask(Scanner scanner) {
        System.out.println("\u001B[31m=== Edit Task ===\u001B[0m");
        displayTasks();

        while (true) {  // Add while loop for retries
            try {
                System.out.print("Enter the task number you want to edit: ");
                String input = scanner.nextLine().trim();
                
                // Check for empty input
                if (input.isEmpty()) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Task number cannot be empty.");
                    System.out.println();
                    continue;
                }

                // Convert to integer after checking for empty
                int taskNum = Integer.parseInt(input) - 1;
                
                // Check for out of range
                if (taskNum < 0 || taskNum >= tasks.size()) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Invalid task number. Please enter a valid number.");
                    System.out.println();
                    continue;
                }
                
                // Original edit logic
                Task task = tasks.get(taskNum);
                System.out.println("What would you like to edit?");
                System.out.println("1. Title");
                System.out.println("2. Description");
                System.out.println("3. Due Date");
                System.out.println("4. Category");
                System.out.println("5. Priority");
                System.out.println("6. Set Task Dependency");
                System.out.println("7. Cancel");
                System.out.print("\n> ");
                
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1:
                        System.out.print("Enter new title: ");
                        task.setTitle(scanner.nextLine());
                        break;
                    case 2:
                        System.out.print("Enter new description: ");
                        task.setDescription(scanner.nextLine());
                        break;
                    case 3:
                        System.out.print("Enter new due date (YYYY-MM-DD): ");
                        task.setDueDate(LocalDate.parse(scanner.nextLine()));
                        break;
                    case 4:
                        System.out.print("Enter new category: ");
                        task.setCategory(scanner.nextLine());
                        break;
                    case 5:
                        System.out.print("Enter new priority (Low, Medium, High): ");
                        task.setPriority(scanner.nextLine());
                        break;
                    case 6:
                        setTaskDependency(scanner);
                        break;
                    case 7:
                        return;
                }
                System.out.println("\n\u001b[32mTask updated successfully!\u001b[0m");
                break;  // Exit loop after successful edit
                
            } catch (NumberFormatException e) {
                System.out.println("\u001b[31mERROR:\u001b[0m Please enter a valid number.");
                System.out.println();
                continue;
            }
        }
    }

    public void viewAllTasks() {
        System.out.println("\u001B[31m=== View All Tasks ===\u001B[0m");
        if (tasks.isEmpty()) {
            System.out.println("\n\u001b[31mWARNING:\u001b[0m No tasks found.");
            return;
        }
        
        for (int i = 0; i < tasks.size(); i++) {
            printTask(tasks.get(i), i + 1);
        }

        System.out.println("\nThe total number of tasks created is " + taskCounter + ".");
    }

    public void viewTaskDetails(Scanner scanner) {
        System.out.println("\u001B[31m=== View Task Details ===\u001B[0m");
        displayTasks();
        
        if (tasks.isEmpty()) {
            return;
        }
        
        while (true) {  // Add while loop for retries
            try {
                System.out.print("Enter task number to view details: ");
                String input = scanner.nextLine().trim();
                
                // Check for empty input
                if (input.isEmpty()) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Task number cannot be empty.");
                    System.out.println();
                    continue;
                }

                // Convert to integer after checking for empty
                int taskNum = Integer.parseInt(input) - 1;
                
                // Check for out of range
                if (taskNum < 0 || taskNum >= tasks.size()) {
                    System.out.println("\u001b[31mERROR:\u001b[0m Invalid task number. Please enter a valid number.");
                    System.out.println();
                    continue;
                }
                
                // Original view details logic
                Task task = tasks.get(taskNum);
                System.out.println("\n\u001b[35mTask Details:\u001b[0m");
                System.out.println("Title: " + task.getTitle());
                System.out.println("Description: " + task.getDescription());
                System.out.println("Due Date: " + task.getDueDate());
                System.out.println("Category: " + task.getCategory());
                System.out.println("Priority: " + task.getPriority());
                System.out.println("Status: " + (task.isComplete() ? "Completed" : "Incomplete"));
                if (task.getDependsOn() != null) {
                    System.out.println("Depends on: " + task.getDependsOn().getTitle());
                }
                if (task.isRecurring()) {
                    System.out.println("Recurring: Yes (" + task.getRecurringInterval() + ")");
                }
                break;  // Exit loop after successful view
                
            } catch (NumberFormatException e) {
                System.out.println("\u001b[31mERROR:\u001b[0m Please enter a valid number.");
                System.out.println();
                continue;
            }
        }
    }

    private void printTask(Task task, int taskNum) {
        System.out.printf("%d. [%s] %s - Due: %s - Category: %s - Priority: %s%s%n",
            taskNum,
            task.isComplete() ? "Completed" : "Incomplete",
            task.getTitle(),
            task.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
            task.getCategory(),
            task.getPriority(),
            task.getDependsOn() != null ? " (Depends on: " + task.getDependsOn().getTitle() + ")" : ""
        );
    }

    // To display the tasks only
    private void displayTasks() {
        if (tasks.isEmpty()) {
            System.out.println("\u001b[31mWARNING:\u001b[0m No tasks found.");
            return;
        }
        
        for (int i = 0; i < tasks.size(); i++) {
            printTask(tasks.get(i), i + 1);
        }
        System.out.println();
    }    

    private LocalDate calculateNewDueDate(LocalDate currentDueDate, String interval) {
        switch (interval.toLowerCase()) {
            case "daily":
                return currentDueDate.plusDays(1);
            case "weekly":
                return currentDueDate.plusWeeks(1);
            case "monthly":
                return currentDueDate.plusMonths(1);
            default:
                throw new IllegalArgumentException("\nInvalid recurring interval");
        }
    }
}
