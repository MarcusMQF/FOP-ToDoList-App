package com.todoapp.dao;

import com.todoapp.model.Task;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface TaskDAO {
    Task create(Task task, int userId) throws SQLException;
    Task getById(int id) throws SQLException;
    List<Task> getAllByUserId(int userId) throws SQLException;
    void update(Task task) throws SQLException;
    void delete(int id) throws SQLException;
    void setTaskDependency(int taskId, int dependsOnTaskId) throws SQLException;
    void removeCompletedDependency(int taskId, int completedDependencyId) throws SQLException;
    int getTotalTasks() throws SQLException;
    int getCompletedTasks() throws SQLException;
    int getPendingTasks() throws SQLException;
    Map<String, Integer> getCategorySummary() throws SQLException;
}
