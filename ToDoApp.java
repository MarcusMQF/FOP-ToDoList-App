import java.util.Scanner;

// Main application class
public class ToDoApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TaskManager taskManager = new TaskManager();
        boolean running = true;

        while (running) {
            System.out.println("\n\u001B[33m[JAVA TODO LIST APP (OOP)]\u001B[0m");
            System.out.println();
            System.out.println("1. Add a task");
            System.out.println("2. View all tasks");
            System.out.println("3. Mark task as completed");
            System.out.println("4. Delete a task");
            System.out.println("5. Task Sorting");
            System.out.println("6. Search tasks");
            System.out.println("7. Set task dependency");
            System.out.println("8. Edit task");
            System.out.println("9. View Task Details");
            System.out.println("10. Exit");
            System.out.print("\nEnter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                System.out.println();

                switch (choice) {
                    case 1:
                        taskManager.addTask(scanner);
                        break;
                    case 2:
                        taskManager.viewAllTasks();
                        break;
                    case 3:
                        taskManager.markTaskComplete(scanner);
                        break;
                    case 4:
                        taskManager.deleteTask(scanner);
                        break;
                    case 5:
                        taskManager.sortTasks(scanner);
                        break;
                    case 6:
                        taskManager.searchTasks(scanner);
                        break;
                    case 7:
                        taskManager.setTaskDependency(scanner);
                        break;
                    case 8:
                        taskManager.editTask(scanner);
                        break;
                    case 9:
                        taskManager.viewTaskDetails(scanner);
                        break;
                    case 10:
                        running = false;
                        System.out.println("\u001b[32mThank you for using the TODO List App!\u001b[0m");
                        break;
                    default:
                        System.out.println("\\u001b[31mError:\\u001b[0m Invalid choice. Please choose between 1-9.");
                }
                if (running) {
                    System.out.print("\nPress Enter to continue...");
                    scanner.nextLine();
                }
                
                System.out.println();
            } catch (Exception e) {
                System.out.println("\u001b[31mError:\u001b[0m " + e.getMessage() +". Please choose between 1-9.");
                System.out.println();
            } // Can use NumberFormatException(non numeric input: abc) of IllegalArgumentException(outside valid range)
        }
        
        scanner.close();
    }
}



