package com.todoapp.controller;

import com.todoapp.dao.UserDAOImpl;
import com.todoapp.ToDoApp;
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
            ToDoApp.initializeTaskManager(user.getId());
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            
            MainController mainController = loader.getController();
            mainController.initializeUser(user);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("ToDo List App - Dashboard");
            
            // Set window size to match login/register pages
            stage.setWidth(900);  // Adjust these values as needed
            stage.setHeight(600); // Adjust these values as needed
            stage.setScene(scene);
            
            stage.setOnCloseRequest(event -> {
                ToDoApp.setGuiCompleted(true);
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
