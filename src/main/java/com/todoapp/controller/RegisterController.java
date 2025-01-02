package com.todoapp.controller;

import com.todoapp.dao.UserDAOImpl;
import com.todoapp.service.AuthenticationService;
import com.todoapp.service.EmailService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import java.util.concurrent.CompletableFuture;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    
    private AuthenticationService authService;
    
    public void initialize() {
        authService = new AuthenticationService(new UserDAOImpl());
    }
    
    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("All fields are required");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }
        
        try {
            // Register the user
            authService.register(username, email, password);
            
            // Switch to login page immediately
            switchToLogin();
            
            // Send welcome email in background
            CompletableFuture.runAsync(() -> {
                try {
                    EmailService.sendWelcomeEmail(email, username);
                } catch (Exception e) {
                    System.err.println("Failed to send welcome email: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error during registration: " + e.getMessage());
            e.printStackTrace();
            if (!e.getMessage().contains("not implemented")) {
                showError(e.getMessage());
            } else {
                switchToLogin();
            }
        }
    }
    
    @FXML
    private void switchToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(RegisterController.class.getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle("ToDo List App - Login");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading login page: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
