package com.todoapp.controller;

import com.todoapp.TaskManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.SQLException;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

public class AnalyticsController {
    @FXML private ProgressIndicator totalTasksProgress;
    @FXML private ProgressIndicator incompleteTasksProgress;
    @FXML private ProgressIndicator completedTasksProgress;
    @FXML private ProgressIndicator completionRateProgress;
    
    @FXML private Label totalTasksLabel;
    @FXML private Label incompleteTasksLabel;
    @FXML private Label completedTasksLabel;
    @FXML private Label completionRateLabel;
    
    @FXML private FlowPane categoryContainer;
    
    private TaskManager taskManager;

    public void initialize(TaskManager taskManager) {
        this.taskManager = taskManager;
        loadAnalytics();
    }

    private void loadAnalytics() {
        try {
            int totalTasks = taskManager.getTotalTasks();
            int completedTasks = taskManager.getCompletedTasks();
            int incompleteTasks = totalTasks - completedTasks;
            double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks : 0;

            // Update progress indicators
            totalTasksProgress.setProgress(1.0);
            completedTasksProgress.setProgress((double) completedTasks / totalTasks);
            incompleteTasksProgress.setProgress((double) incompleteTasks / totalTasks);
            completionRateProgress.setProgress(completionRate);

            // Update labels
            totalTasksLabel.setText(String.valueOf(totalTasks));
            completedTasksLabel.setText(String.valueOf(completedTasks));
            incompleteTasksLabel.setText(String.valueOf(incompleteTasks));
            completionRateLabel.setText(String.format("%.1f%%", completionRate * 100));

            // Load category distribution
            Map<String, Integer> categorySummary = taskManager.getCategorySummary();
            categoryContainer.getChildren().clear();
            
            categorySummary.forEach((category, count) -> {
                VBox categoryCard = createCategoryCard(category, count, totalTasks);
                categoryContainer.getChildren().add(categoryCard);
            });

        } catch (SQLException e) {
            showError("Failed to load analytics: " + e.getMessage());
        }
    }

    private VBox createCategoryCard(String category, int count, int total) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("category-card");
        
        ProgressIndicator progress = new ProgressIndicator((double) count / total);
        progress.getStyleClass().add("category-progress");
        
        Label nameLabel = new Label(category);
        nameLabel.getStyleClass().add("category-name");
        VBox.setMargin(nameLabel, new Insets(-5, 0, 0, 0));
        
        Label countLabel = new Label(String.valueOf(count));
        countLabel.getStyleClass().add("category-count");
        
        card.getChildren().addAll(progress, nameLabel, countLabel);
        return card;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 