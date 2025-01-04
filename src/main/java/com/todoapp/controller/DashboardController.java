package com.todoapp.controller;

import com.todoapp.TaskManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import java.sql.SQLException;
import java.util.List;
import com.todoapp.model.Task;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ButtonType;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.beans.property.SimpleObjectProperty;

public class DashboardController {
    @FXML private Label totalTasksLabel;
    @FXML private Label pendingTasksLabel;
    @FXML private Label completedTasksLabel;
    @FXML private TableView<Task> recentTasksTable;
    @FXML private TableColumn<Task, Integer> noColumn;
    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, LocalDate> dueDateColumn;
    @FXML private TableColumn<Task, String> statusColumn;
    
    private TaskManager taskManager;

    public void initialize(TaskManager taskManager) {
        this.taskManager = taskManager;
        
        // Remove any programmatic column width settings
        recentTasksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        setupTableColumns();
        loadDashboardData();
    }

    private void setupTableColumns() {
        // Add No column
        noColumn.setCellValueFactory(column -> 
            new SimpleObjectProperty<>(recentTasksTable.getItems().indexOf(column.getValue()) + 1));
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

        // Basic columns with center alignment
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setCellFactory(col -> {
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

        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueDateColumn.setCellFactory(col -> {
            TableCell<Task, LocalDate> cell = new TableCell<Task, LocalDate>() {
                @Override
                protected void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.toString());
                    }
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            };
            cell.setAlignment(javafx.geometry.Pos.CENTER);
            return cell;
        });

        // Status column with center alignment
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

        // Recurring column with center alignment
        TableColumn<Task, String> recurringColumn = new TableColumn<>("Recurring");
        recurringColumn.setCellValueFactory(cellData -> {
            Task task = cellData.getValue();
            if (!task.isRecurring()) {
                return new SimpleStringProperty("No");
            } else {
                return new SimpleStringProperty("Yes (" + task.getRecurringInterval() + ")");
            }
        });
        recurringColumn.setCellFactory(col -> {
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

        // Complete action column
        TableColumn<Task, Void> completeCol = new TableColumn<>("Mark as Completed");
        completeCol.setCellFactory(col -> new TableCell<Task, Void>() {
            private final Button completeBtn = new Button("✓");
            {
                completeBtn.setOnAction(e -> {
                    Task task = getTableRow().getItem();
                    if (task != null) handleCompleteTask(task);
                });
                setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null || getTableRow().getItem().isComplete()) {
                    setGraphic(null);
                } else {
                    setGraphic(completeBtn);
                }
            }
        });

        // Delete action column
        TableColumn<Task, Void> deleteCol = new TableColumn<>("Delete");
        deleteCol.setCellFactory(col -> new TableCell<Task, Void>() {
            private final Button deleteBtn = new Button("Delete");
            {
                deleteBtn.setOnAction(e -> {
                    Task task = getTableRow().getItem();
                    if (task != null) handleDeleteTask(task);
                });
                setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        // Update columns list to remove created time
        recentTasksTable.getColumns().clear();
        List<TableColumn<Task, ?>> columns = List.of(
            noColumn,
            titleColumn,
            dueDateColumn,
            statusColumn,
            recurringColumn,
            completeCol,
            deleteCol
        );
        recentTasksTable.getColumns().addAll(columns);
    }

    private void loadDashboardData() {
        try {
            List<Task> tasks = taskManager.getAllTasks();
            
            // Update statistics
            int total = tasks.size();
            long completed = tasks.stream().filter(Task::isComplete).count();
            long incomplete = total - completed;
            
            totalTasksLabel.setText(String.valueOf(total));
            completedTasksLabel.setText(String.valueOf(completed));
            pendingTasksLabel.setText(String.valueOf(incomplete));
            
            // Show all tasks sorted by due date
            List<Task> sortedTasks = tasks.stream()
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());
            
            recentTasksTable.getItems().setAll(sortedTasks);
            
            // Refresh the No column after data changes
            recentTasksTable.refresh();
        } catch (SQLException e) {
            showError("Failed to load dashboard data: " + e.getMessage());
        }
    }

    private void handleCompleteTask(Task task) {
        try {
            taskManager.markTaskComplete(task.getId());
            
            if (task.isRecurring()) {
                LocalDate newDueDate = calculateNewDueDate(task.getDueDate(), task.getRecurringInterval());
                Task newTask = new Task();
                newTask.setTitle(task.getTitle());
                newTask.setDescription(task.getDescription());
                newTask.setCategory(task.getCategory());
                newTask.setDueDate(newDueDate);
                newTask.setPriority(task.getPriority());
                newTask.setRecurring(true);
                newTask.setRecurringInterval(task.getRecurringInterval());
                
                taskManager.addTask(newTask);
            }
            
            refreshDashboard();
        } catch (SQLException e) {
            showError("Failed to complete task: " + e.getMessage());
        }
    }

    private void handleDeleteTask(Task task) {
        try {
            taskManager.deleteTask(task.getId());
            loadDashboardData();
        } catch (SQLException e) {
            showError("Failed to delete task: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleAddTask() {
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
        addButton.getStyleClass().add("button");
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.getStyleClass().addAll("button", "cancel");

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
                refreshDashboard();
            } catch (SQLException e) {
                showError("Failed to add task: " + e.getMessage());
            }
        });
    }

    @FXML
    private void refreshDashboard() {
        loadDashboardData();
    }

    private LocalDate calculateNewDueDate(LocalDate currentDueDate, String interval) {
        return switch (interval.toUpperCase()) {
            case "DAILY" -> currentDueDate.plusDays(1);
            case "WEEKLY" -> currentDueDate.plusWeeks(1);
            case "MONTHLY" -> currentDueDate.plusMonths(1);
            default -> throw new IllegalArgumentException("Invalid recurring interval");
        };
    }
} 