package com.todoapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ToDoApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        // Set consistent window size
        primaryStage.setWidth(900);
        primaryStage.setHeight(600);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        primaryStage.getIcons().add(new Image("images/todo-icon.png"));
        
        primaryStage.setTitle("ToDo List App - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
