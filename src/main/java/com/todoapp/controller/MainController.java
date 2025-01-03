package com.todoapp.controller;

import com.todoapp.TaskManager;
import com.todoapp.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import java.io.IOException;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.Label;
import java.util.Optional;
import com.todoapp.service.AuthService;

public class MainController {
    @FXML private Label userNameLabel;
    @FXML private TextField searchField;
    @FXML private VBox contentArea;
    
    private User currentUser;
    private TaskManager taskManager;
    private AuthService authService;

    @FXML
    public void initialize() {
        authService = new AuthService();
    }

    public void initializeUser(User user) {
        this.currentUser = user;
        userNameLabel.setText(user.getUsername());
        this.taskManager = new TaskManager(user.getId());
        
        // Initialize the dashboard with the taskManager
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/views/dashboard.fxml"));
            Parent dashboard = loader.load();
            DashboardController dashboardController = loader.getController();
            dashboardController.initialize(taskManager);
            contentArea.getChildren().setAll(dashboard);
        } catch (IOException e) {
            showError("Failed to load dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String searchQuery = searchField.getText().trim();
        if (!searchQuery.isEmpty()) {
            try {
                List<Map<String, Object>> results = taskManager.vectorSearchTasks(searchQuery);
                
                Dialog<Void> dialog = new Dialog<>();
                dialog.setTitle("Search Results");
                dialog.setHeaderText("Semantic Search Results");
                
                ListView<Map<String, Object>> listView = new ListView<>();
                listView.setCellFactory(lv -> new ListCell<Map<String, Object>>() {
                    @Override
                    protected void updateItem(Map<String, Object> task, boolean empty) {
                        super.updateItem(task, empty);
                        if (empty || task == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            VBox container = new VBox(5);
                            container.setPadding(new Insets(5, 10, 5, 10));
                            
                            Label titleLabel = new Label(task.get("title").toString());
                            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                            
                            // Handle null date safely
                            String dueDate = task.get("due_date") != null ? task.get("due_date").toString() : "No due date";
                            Label detailsLabel = new Label(String.format("Due: %s | Category: %s | Priority: %s", 
                                dueDate,
                                task.get("category"),
                                task.get("priority")));
                            detailsLabel.setStyle("-fx-text-fill: #666666;");
                            
                            Label descLabel = new Label(task.get("description").toString());
                            descLabel.setWrapText(true);
                            
                            String status = (Boolean)task.get("is_complete") ? "Completed" : "Incomplete";
                            Label statusLabel = new Label("Status: " + status);
                            statusLabel.setStyle("-fx-text-fill: " + ((Boolean)task.get("is_complete") ? "#2ecc71" : "#e74c3c"));
                            
                            container.getChildren().addAll(titleLabel, detailsLabel, descLabel, statusLabel);
                            setGraphic(container);
                        }
                    }
                });
                
                listView.getItems().addAll(results);
                
                DialogPane dialogPane = dialog.getDialogPane();
                dialogPane.setContent(listView);
                dialogPane.setPrefWidth(500);
                dialogPane.setPrefHeight(400);
                dialogPane.getButtonTypes().add(ButtonType.CLOSE);
                
                if (results.isEmpty()) {
                    Label noResultsLabel = new Label("No matching tasks found");
                    noResultsLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");
                    dialogPane.setContent(noResultsLabel);
                }
                
                dialog.showAndWait();
                
            } catch (Exception e) {
                showError("Search failed: " + e.getMessage());
            }
        }
    }

    @FXML
    private void showDashboard() {
        loadView("/fxml/views/dashboard.fxml", DashboardController.class);
    }

    @FXML
    private void showTasks() {
        loadView("/fxml/views/tasks.fxml", TasksController.class);
    }

    @FXML
    private void showCategories() {
        loadView("/fxml/views/categories.fxml", null);
    }

    @FXML
    private void showAnalytics() {
        loadView("/fxml/views/analytics.fxml", AnalyticsController.class);
    }

    @FXML
    private void showSettings() {
        loadView("/fxml/views/settings.fxml", null);
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.setTitle("ToDo List App - Login");
            stage.setScene(scene);
        } catch (IOException e) {
            showError("Failed to logout: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteAccount() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Account");
        confirmDialog.setHeaderText("Are you sure you want to delete your account?");
        confirmDialog.setContentText("This action cannot be undone. All your data will be permanently deleted.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete user from database
                authService.deleteUser(currentUser.getId());
                
                // Switch to register page
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                
                Stage stage = (Stage) contentArea.getScene().getWindow();
                stage.setTitle("ToDo List App - Register");
                stage.setScene(scene);
                
            } catch (Exception e) {
                showError("Failed to delete account: " + e.getMessage());
            }
        }
    }

    private <T> void loadView(String fxmlPath, Class<T> controllerClass) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            if (controllerClass != null) {
                Object controller = loader.getController();
                if (controller instanceof DashboardController) {
                    ((DashboardController) controller).initialize(taskManager);
                } else if (controller instanceof TasksController) {
                    ((TasksController) controller).initialize(taskManager);
                } else if (controller instanceof AnalyticsController) {
                    ((AnalyticsController) controller).initialize(taskManager);
                }
            }
            setContent(view);
        } catch (IOException e) {
            showError("Failed to load view: " + e.getMessage());
        }
    }

    private void setContent(Node content) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public User getCurrentUser() {
        return currentUser;
    }
} 