package com.todoapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import java.time.LocalDate;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.event.ActionEvent;

import com.todoapp.model.Task;
import com.todoapp.TaskManager;
import java.sql.SQLException;
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
    @FXML private TableColumn<Task, Integer> noColumn;
    @FXML private TableColumn<Task, String> dependsOnColumn;
    @FXML private TableColumn<Task, Void> actionsColumn;
    
    private TaskManager taskManager;

    public void initialize(TaskManager taskManager) {
        this.taskManager = taskManager;
        setupTable();
        setupFilters();
        loadTasks();
    }

    private void setupTable() {
        // No column setup (existing code)
        noColumn.setCellValueFactory(column -> 
            new SimpleObjectProperty<>(tasksTable.getItems().indexOf(column.getValue()) + 1));
        noColumn.setCellFactory(col -> createCenteredCell());

        // Title column
        taskTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        // Description column
        taskDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Due date column
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueDateColumn.setCellFactory(col -> createCenteredCell());
        
        // Category column
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryColumn.setCellFactory(col -> createCenteredCell());
        
        // Priority column
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityColumn.setCellFactory(col -> createCenteredCell());
        
        // Depends on column
        dependsOnColumn.setCellValueFactory(cellData -> {
            Task task = cellData.getValue();
            if (!task.getDependencies().isEmpty()) {
                return new SimpleStringProperty(task.getDependencies().stream()
                    .map(Task::getTitle)
                    .collect(Collectors.joining(", ")));
            }
            return new SimpleStringProperty("-");
        });
        
        // Status column
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isComplete() ? "Completed" : "Incomplete"));
        statusColumn.setCellFactory(col -> createCenteredCell());
        
        // Actions column (Set Dependency, Edit, Delete)
        actionsColumn.setCellFactory(col -> new TableCell<Task, Void>() {
            private final Button setDependencyBtn = new Button("Set Dependency");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox container = new HBox(5, setDependencyBtn, editBtn, deleteBtn);
            
            {
                setDependencyBtn.setOnAction(e -> {
                    Task task = getTableRow().getItem();
                    if (task != null) showSetDependencyDialog(task);
                });
                
                editBtn.setOnAction(e -> {
                    Task task = getTableRow().getItem();
                    if (task != null) showEditTaskDialog(task);
                });
                
                deleteBtn.setOnAction(e -> {
                    Task task = getTableRow().getItem();
                    if (task != null) handleDeleteTask(task);
                });
                
                container.setAlignment(javafx.geometry.Pos.CENTER);
                setDependencyBtn.getStyleClass().add("action-button");
                editBtn.getStyleClass().add("action-button");
                deleteBtn.getStyleClass().add("action-button");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Task task = getTableRow().getItem();
                    if (task != null) {
                        setDependencyBtn.setDisable(task.isComplete());
                        editBtn.setDisable(task.isComplete());
                        deleteBtn.setDisable(false);
                        setGraphic(container);
                    }
                }
            }
        });

        // Complete column
        TableColumn<Task, Void> completeColumn = new TableColumn<>("Complete");
        completeColumn.setCellFactory(col -> new TableCell<Task, Void>() {
            private final Button completeBtn = new Button("Complete");
            {
                completeBtn.setOnAction(e -> {
                    Task task = getTableRow().getItem();
                    if (task != null) handleCompleteTask(task);
                });
                completeBtn.getStyleClass().add("action-button");
                setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Task task = getTableRow().getItem();
                    if (task != null) {
                        completeBtn.setDisable(!task.canComplete());
                        setGraphic(task.isComplete() ? null : completeBtn);
                    }
                }
            }
        });

        // Set up the table columns in the correct order
        tasksTable.getColumns().clear();
        tasksTable.getColumns().addAll(List.of(
            noColumn,
            taskTitleColumn,
            taskDescriptionColumn,
            dueDateColumn,
            categoryColumn,
            priorityColumn,
            dependsOnColumn,
            statusColumn,
            actionsColumn,
            completeColumn
        ));
    }

    @FXML
    private void showAddTaskDialog() {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Add New Task");
        dialog.getDialogPane().getStyleClass().add("custom-dialog");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField();
        TextArea descField = new TextArea();
        DatePicker dueDatePicker = new DatePicker();
        
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Work", "Personal", "Homework");
        
        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("High", "Medium", "Low");
        
        CheckBox isRecurringBox = new CheckBox("Recurring Task");
        ComboBox<String> recurringIntervalBox = new ComboBox<>();
        recurringIntervalBox.getItems().addAll("Daily", "Weekly", "Monthly");
        recurringIntervalBox.setDisable(true);
        
        isRecurringBox.setOnAction(e -> 
            recurringIntervalBox.setDisable(!isRecurringBox.isSelected()));

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(false);

        // Style the input fields
        titleField.getStyleClass().add("custom-dialog-field");
        descField.getStyleClass().add("custom-dialog-field");
        dueDatePicker.getStyleClass().add("custom-dialog-field");
        categoryBox.getStyleClass().add("custom-dialog-field");
        priorityBox.getStyleClass().add("custom-dialog-field");
        recurringIntervalBox.getStyleClass().add("custom-dialog-field");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryBox, 1, 2);
        grid.add(new Label("Due Date:"), 0, 3);
        grid.add(dueDatePicker, 1, 3);
        grid.add(new Label("Priority:"), 0, 4);
        grid.add(priorityBox, 1, 4);
        grid.add(isRecurringBox, 0, 5);
        grid.add(recurringIntervalBox, 1, 5);
        grid.add(errorLabel, 0, 6, 2, 1);

        dialog.getDialogPane().setContent(grid);
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.addEventFilter(ActionEvent.ANY, event -> {
            // Validate all fields
            if (titleField.getText().trim().isEmpty() || 
                descField.getText().trim().isEmpty() || 
                categoryBox.getValue() == null ||
                dueDatePicker.getValue() == null || 
                priorityBox.getValue() == null ||
                (isRecurringBox.isSelected() && recurringIntervalBox.getValue() == null)) {
                
                errorLabel.setText("All fields are required!");
                errorLabel.setVisible(true);
                event.consume();
                return;
            }

            // Validate due date
            if (dueDatePicker.getValue().isBefore(LocalDate.now())) {
                errorLabel.setText("Due date cannot be in the past!");
                errorLabel.setVisible(true);
                event.consume();
                return;
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Task task = new Task();
                task.setTitle(titleField.getText().trim());
                task.setDescription(descField.getText().trim());
                task.setCategory(categoryBox.getValue());
                task.setDueDate(dueDatePicker.getValue());
                task.setPriority(priorityBox.getValue());
                task.setRecurring(isRecurringBox.isSelected());
                if (isRecurringBox.isSelected()) {
                    task.setRecurringInterval(recurringIntervalBox.getValue());
                }
                return task;
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
        dialog.getDialogPane().getStyleClass().add("custom-dialog");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField(task.getTitle());
        TextArea descField = new TextArea(task.getDescription());
        DatePicker dueDatePicker = new DatePicker(task.getDueDate());
        
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Work", "Personal", "Homework");
        categoryBox.setValue(task.getCategory());
        
        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("High", "Medium", "Low");
        priorityBox.setValue(task.getPriority());
        
        CheckBox isRecurringBox = new CheckBox("Recurring Task");
        isRecurringBox.setSelected(task.isRecurring());
        ComboBox<String> recurringIntervalBox = new ComboBox<>();
        recurringIntervalBox.getItems().addAll("Daily", "Weekly", "Monthly");
        recurringIntervalBox.setValue(task.getRecurringInterval());
        recurringIntervalBox.setDisable(!task.isRecurring());
        
        isRecurringBox.setOnAction(e -> 
            recurringIntervalBox.setDisable(!isRecurringBox.isSelected()));

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(false);

        // Style the input fields
        titleField.getStyleClass().add("custom-dialog-field");
        descField.getStyleClass().add("custom-dialog-field");
        dueDatePicker.getStyleClass().add("custom-dialog-field");
        categoryBox.getStyleClass().add("custom-dialog-field");
        priorityBox.getStyleClass().add("custom-dialog-field");
        recurringIntervalBox.getStyleClass().add("custom-dialog-field");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryBox, 1, 2);
        grid.add(new Label("Due Date:"), 0, 3);
        grid.add(dueDatePicker, 1, 3);
        grid.add(new Label("Priority:"), 0, 4);
        grid.add(priorityBox, 1, 4);
        grid.add(isRecurringBox, 0, 5);
        grid.add(recurringIntervalBox, 1, 5);
        grid.add(errorLabel, 0, 6, 2, 1);

        dialog.getDialogPane().setContent(grid);
        ButtonType editButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(editButtonType, ButtonType.CANCEL);

        Node editButton = dialog.getDialogPane().lookupButton(editButtonType);
        editButton.getStyleClass().add("button");
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.getStyleClass().addAll("button", "cancel");

        editButton.addEventFilter(ActionEvent.ANY, event -> {
            // Validate all fields
            if (titleField.getText().trim().isEmpty() || 
                descField.getText().trim().isEmpty() || 
                categoryBox.getValue() == null ||
                dueDatePicker.getValue() == null || 
                priorityBox.getValue() == null ||
                (isRecurringBox.isSelected() && recurringIntervalBox.getValue() == null)) {
                
                errorLabel.setText("All fields are required!");
                errorLabel.setVisible(true);
                event.consume();
                return;
            }

            // Validate due date
            if (dueDatePicker.getValue().isBefore(LocalDate.now())) {
                errorLabel.setText("Due date cannot be in the past!");
                errorLabel.setVisible(true);
                event.consume();
                return;
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == editButtonType) {
                task.setTitle(titleField.getText().trim());
                task.setDescription(descField.getText().trim());
                task.setCategory(categoryBox.getValue());
                task.setDueDate(dueDatePicker.getValue());
                task.setPriority(priorityBox.getValue());
                task.setRecurring(isRecurringBox.isSelected());
                if (isRecurringBox.isSelected()) {
                    task.setRecurringInterval(recurringIntervalBox.getValue());
                }
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
            if (!task.canComplete()) {
                showError("Cannot complete this task until its dependencies are completed!");
                return;
            }
            taskManager.markTaskComplete(task.getId());
            loadTasks();
        } catch (SQLException e) {
            showError("Failed to complete task: " + e.getMessage());
        }
    }

    private void showSetDependencyDialog(Task task) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Set Task Dependency");
        dialog.setHeaderText("Select a task that must be completed before this task:");
        
        ComboBox<Task> taskComboBox = new ComboBox<>();
        try {
            List<Task> availableTasks = taskManager.getAllTasks().stream()
                .filter(t -> !t.isComplete() && !t.getId().equals(task.getId()))
                .collect(Collectors.toList());
            
            taskComboBox.getItems().addAll(availableTasks);
            
            // Set the display format for the ComboBox items
            taskComboBox.setButtonCell(new ListCell<Task>() {
                @Override
                protected void updateItem(Task item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getTitle());
                }
            });
            
            taskComboBox.setCellFactory(lv -> new ListCell<Task>() {
                @Override
                protected void updateItem(Task item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getTitle());
                }
            });
        } catch (SQLException e) {
            showError("Failed to load available tasks: " + e.getMessage());
            return;
        }
        
        VBox content = new VBox(10);
        content.getChildren().addAll(new Label("Depends on:"), taskComboBox);
        dialog.getDialogPane().setContent(content);
        
        ButtonType setButtonType = new ButtonType("Set Dependency", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(setButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == setButtonType && taskComboBox.getValue() != null) {
                try {
                    Task selectedDependency = taskComboBox.getValue();
                    
                    // Check for circular dependency
                    if (taskManager.wouldCreateCircularDependency(task.getId(), selectedDependency.getId())) {
                        showError("Cannot set this dependency as it would create a circular dependency chain!");
                        return null;
                    }
                    
                    taskManager.setTaskDependency(task.getId(), selectedDependency.getId());
                    loadTasks(); // Refresh the table
                } catch (SQLException e) {
                    showError("Failed to set task dependency: " + e.getMessage());
                }
            }
            return null;
        });
        
        dialog.showAndWait();
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
