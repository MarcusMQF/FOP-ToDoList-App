package com.todoapp.dao;

import com.todoapp.model.User;
import java.sql.SQLException;
import java.util.List;

public interface UserDAO {
    User create(User user) throws SQLException;
    User getById(int id) throws SQLException;
    User getByUsername(String username) throws SQLException;
    void update(User user) throws SQLException;
    List<User> getAll() throws SQLException;
    void delete(int id) throws SQLException;
} 