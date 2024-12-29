package com.todoapp.service;

import com.todoapp.dao.UserDAO;
import com.todoapp.model.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthenticationService {
    private UserDAO userDAO;
    
    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    
    public User register(String username, String email, String password) throws Exception {
        // Basic validation
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        
        String passwordHash = hashPassword(password);
        User newUser = new User(username, email, passwordHash);
        return userDAO.create(newUser);
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    public User login(String email, String password) throws Exception {
        // Basic validation
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        // First try to find user by email
        User user = userDAO.getByUsername(email); // Using email as username for now
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        
        // Verify password
        String passwordHash = hashPassword(password);
        if (!passwordHash.equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }
        
        return user;
    }
}
