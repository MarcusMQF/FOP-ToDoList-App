package com.todoapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
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
    
    private TaskManager taskManager;

    public void initialize(TaskManager taskManager) {
        this.taskManager = taskManager;
        setupTable();
        setupFilters();
        loadTasks();
    }

    private void setupTable() {
        // Add No column at the beginning
        noColumn.setCellValueFactory(column -> 
            new SimpleObjectProperty<>(tasksTable.getItems().indexOf(column.getValue()) + 1));
        noColumn.setCellFactory(col -> {
            TableCell<Task, Integer> cell = new TableCell<Task, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.valueOf(item));
                    }
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            };
            cell.setAlignment(javafx.geometry.Pos.CENTER);
            return cell;
        });

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
