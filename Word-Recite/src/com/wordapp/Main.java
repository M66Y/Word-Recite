package com.wordapp;

import com.wordapp.dao.UserDAO;
import com.wordapp.model.User;

public class Main {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        String username = "testuser_" + System.currentTimeMillis();
        String password = "password123";

        System.out.println("--- Starting Automated Test ---");

        // 1. Register
        System.out.println("Attempting to register user: " + username);
        boolean registered = userDAO.register(username, password);
        if (registered) {
            System.out.println("Registration successful.");
        } else {
            System.out.println("Registration failed.");
        }

        // 2. Login
        System.out.println("Attempting to login with correct credentials...");
        User user = userDAO.login(username, password);
        if (user != null) {
            System.out.println("Login successful! User ID: " + user.getUserId());
        } else {
            System.out.println("Login failed.");
        }

        // 3. Login with wrong password
        System.out.println("Attempting to login with wrong password...");
        User userWrong = userDAO.login(username, "wrongpassword");
        if (userWrong == null) {
            System.out.println("Login failed as expected.");
        } else {
            System.out.println("Login succeeded unexpectedly!");
        }
        
        System.out.println("--- Test Completed ---");
    }
}
