package com.todoapp.controller;

import com.todoapp.dao.UserDAOImpl;
import com.todoapp.service.AuthenticationService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import com.todoapp.model.User;
import com.todoapp.model.Task;
import com.todoapp.service.EmailService;
import java.util.List;
import java.util.stream.Collectors;
import com.todoapp.TaskManager;
import java.util.concurrent.CompletableFuture;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    
    private AuthenticationService authService;
    
    public void initialize() {
        authService = new AuthenticationService(new UserDAOImpl());
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required");
            return;
        }
        
        try {
            User user = authService.login(username, password);
            TaskManager taskManager = new TaskManager(user.getId());
            
            // Load dashboard immediately
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            
            MainController mainController = loader.getController();
            mainController.initializeUser(user);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("ToDo List App - Dashboard");
            stage.setScene(scene);
            
            // Run task check and email sending in background
            CompletableFuture.runAsync(() -> {
                try {
                    List<Task> dueTasks = taskManager.getTasksDueWithin24Hours();
                    if (!dueTasks.isEmpty()) {
                        String taskDetails = dueTasks.stream()
                            .map(task -> String.format("%s (Due: %s)", 
                                task.getTitle(), 
                                task.getDueDate().toString()))
                            .collect(Collectors.joining("\n"));
                        
                        EmailService.sendTaskReminderEmail(user.getEmail(), user.getUsername(), taskDetails);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }
    
    @FXML
    private void switchToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/fxml/register.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("ToDo List App - Register");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading register page: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
