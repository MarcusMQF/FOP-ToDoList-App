package com.todoapp.controller;

import com.todoapp.TaskManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import java.sql.SQLException;
import java.util.Map;
import javafx.collections.FXCollections;

public class AnalyticsController {
    @FXML private PieChart taskDistributionChart;
    @FXML private BarChart<String, Number> categoryChart;
    @FXML private VBox statsContainer;
    @FXML private Label totalTasksValue;
    @FXML private Label completedTasksValue;
    @FXML private Label completionRateValue;
    @FXML private ProgressBar completionProgressBar;
    
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

            // Update summary statistics
            totalTasksValue.setText(String.valueOf(totalTasks));
            completedTasksValue.setText(String.valueOf(completedTasks));
            completionRateValue.setText(String.format("%.1f%%", completionRate * 100));
            completionProgressBar.setProgress(completionRate);

            // Task Distribution Pie Chart
            PieChart.Data completedData = new PieChart.Data("Completed", completedTasks);
            PieChart.Data incompleteData = new PieChart.Data("Incomplete", incompleteTasks);
            taskDistributionChart.setData(FXCollections.observableArrayList(
                completedData, incompleteData
            ));

            // Category Bar Chart
            Map<String, Integer> categorySummary = taskManager.getCategorySummary();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Tasks per Category");
            
            categorySummary.forEach((category, count) -> {
                series.getData().add(new XYChart.Data<>(category, count));
            });
            
            categoryChart.getData().clear();
            categoryChart.getData().add(series);

        } catch (SQLException e) {
            showError("Failed to load analytics: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 