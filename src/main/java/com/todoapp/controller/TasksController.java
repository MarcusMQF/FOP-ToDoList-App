package com.todoapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.beans.property.SimpleStringProperty;

import com.todoapp.model.Task;
import com.todoapp.TaskManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Comparator;

public class TasksController {
    @FXML private TableView<Task> tasksTable;
    @FXML private TableColumn<Task, String> taskTitleColumn;
    @FXML private TableColumn<Task, String> taskDescriptionColumn;
    @FXML private TableColumn<Task, LocalDate> dueDateColumn;
    @FXML private TableColumn<Task, String> categoryColumn;
    @FXML private TableColumn<Task, String> priorityColumn;
    @FXML private TableColumn<Task, String> statusColumn;
    @FXML private ComboBox<String> filterCategory;
    @FXML private ComboBox<String> sortBy;
    
    private TaskManager taskManager;

    public void initialize(TaskManager taskManager) {
        this.taskManager = taskManager;
        setupTable();
        setupFilters();
        loadTasks();
    }

    private void setupTable() {
        // Set cell factories for all columns
        taskTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        taskTitleColumn.setCellFactory(col -> createCenteredCell());

        taskDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        taskDescriptionColumn.setCellFactory(col -> createCenteredCell());

        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueDateColumn.setCellFactory(col -> createCenteredCell());

        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryColumn.setCellFactory(col -> createCenteredCell());

        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityColumn.setCellFactory(col -> createCenteredCell());

        // Status column with Completed/Incomplete text
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isComplete() ? "Completed" : "Incomplete"));
        statusColumn.setCellFactory(col -> {
            TableCell<Task, String> cell = new TableCell<Task, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                    }
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            };
            cell.setAlignment(javafx.geometry.Pos.CENTER);
            return cell;
        });

        // Add action buttons column
        addActionButtons();
    }

    @FXML
    private void showAddTaskDialog() {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Add New Task");
        
        // Create dialog content
        GridPane grid = new GridPane();
        TextField titleField = new TextField();
        TextArea descField = new TextArea();
        DatePicker dueDatePicker = new DatePicker();
        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("High", "Medium", "Low");
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Due Date:"), 0, 2);
        grid.add(dueDatePicker, 1, 2);
        grid.add(new Label("Priority:"), 0, 3);
        grid.add(priorityBox, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                Task newTask = new Task();
                newTask.setTitle(titleField.getText());
                newTask.setDescription(descField.getText());
                newTask.setDueDate(dueDatePicker.getValue());
                newTask.setPriority(priorityBox.getValue());
                return newTask;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(task -> {
            try {
                taskManager.addTask(task);
                loadTasks();
            } catch (SQLException e) {
                showError("Failed to add task: " + e.getMessage());
            }
        });
    }

    private void loadTasks() {
        try {
            tasksTable.getItems().clear();
            tasksTable.getItems().addAll(taskManager.getAllTasks());
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error dialog
        }
    }

    private void addActionButtons() {
        TableColumn<Task, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<Task, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button completeBtn = new Button("Complete");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn, completeBtn);
            
            {
                editBtn.setOnAction(e -> showEditTaskDialog(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> handleDeleteTask(getTableRow().getItem()));
                completeBtn.setOnAction(e -> handleCompleteTask(getTableRow().getItem()));
                // Center the buttons container
                buttons.setAlignment(javafx.geometry.Pos.CENTER);
                setAlignment(javafx.geometry.Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        tasksTable.getColumns().add(actionsCol);
    }

    private void handleDeleteTask(Task task) {
        try {
            taskManager.deleteTask(task.getId());
            loadTasks();
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error dialog
        }
    }

    private void setupFilters() {
        // Setup category filter
        filterCategory.getItems().addAll("All", "Work", "Personal", "Homework");
        filterCategory.setValue("All");
        filterCategory.setOnAction(e -> filterTasks());
        
        // Setup sort options
        sortBy.getItems().addAll("Due Date", "Priority", "Status");
        sortBy.setValue("Due Date");
        sortBy.setOnAction(e -> sortTasks());
    }

    private void filterTasks() {
        try {
            List<Task> tasks = taskManager.getAllTasks();
            if (!filterCategory.getValue().equals("All")) {
                tasks = tasks.stream()
                    .filter(task -> task.getCategory().equals(filterCategory.getValue()))
                    .collect(Collectors.toList());
            }
            tasksTable.getItems().setAll(tasks);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sortTasks() {
        try {
            List<Task> tasks = new ArrayList<>(tasksTable.getItems());
            switch (sortBy.getValue()) {
                case "Due Date":
                    tasks.sort(Comparator.comparing(Task::getDueDate));
                    break;
                case "Priority":
                    tasks.sort((t1, t2) -> {
                        Map<String, Integer> priorityMap = Map.of(
                            "High", 3, "Medium", 2, "Low", 1
                        );
                        return priorityMap.get(t2.getPriority()) - priorityMap.get(t1.getPriority());
                    });
                    break;
                case "Status":
                    tasks.sort(Comparator.comparing(Task::isComplete));
                    break;
            }
            tasksTable.getItems().setAll(tasks);
        } catch (Exception e) {
            showError("Failed to sort tasks: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showEditTaskDialog(Task task) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");
        
        GridPane grid = new GridPane();
        TextField titleField = new TextField(task.getTitle());
        TextArea descField = new TextArea(task.getDescription());
        DatePicker dueDatePicker = new DatePicker(task.getDueDate());
        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("High", "Medium", "Low");
        priorityBox.setValue(task.getPriority());
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Due Date:"), 0, 2);
        grid.add(dueDatePicker, 1, 2);
        grid.add(new Label("Priority:"), 0, 3);
        grid.add(priorityBox, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                task.setTitle(titleField.getText());
                task.setDescription(descField.getText());
                task.setDueDate(dueDatePicker.getValue());
                task.setPriority(priorityBox.getValue());
                return task;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(updatedTask -> {
            try {
                taskManager.updateTask(updatedTask);
                loadTasks();
            } catch (SQLException e) {
                showError("Failed to update task: " + e.getMessage());
            }
        });
    }

    private void handleCompleteTask(Task task) {
        try {
            taskManager.markTaskComplete(task.getId());
            loadTasks();
        } catch (SQLException e) {
            showError("Failed to complete task: " + e.getMessage());
        }
    }

    private <T> TableCell<Task, T> createCenteredCell() {
        return new TableCell<Task, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        };
    }
}
