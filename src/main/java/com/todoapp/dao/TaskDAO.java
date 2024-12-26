package com.todoapp.dao;

import com.todoapp.model.Task;
import java.sql.SQLException;
import java.util.List;

public interface TaskDAO {
    Task create(Task task, int userId) throws SQLException;
    Task getById(int id) throws SQLException;
    List<Task> getAllByUserId(int userId) throws SQLException;
    void update(Task task) throws SQLException;
    void delete(int id) throws SQLException;
    void setTaskDependency(int taskId, int dependsOnTaskId) throws SQLException;
    void removeCompletedDependency(int taskId, int completedDependencyId) throws SQLException;
}
