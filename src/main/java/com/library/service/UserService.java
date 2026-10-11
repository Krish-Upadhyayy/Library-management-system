package com.library.service;

import com.library.dao.UserDAO;
import com.library.model.User;

import java.util.List;

public class UserService {

    private UserDAO userDAO;

    public UserService() {
        userDAO = new UserDAO();
    }

    public void addUser(User user) {
        userDAO.addUser(user);
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public User getUserById(int id) {
        return userDAO.getUserById(id);
    }

    public void updateUser(User user) {
        userDAO.updateUser(user);
    }
    public void deleteUser(int id) {
        userDAO.deleteUser(id);
    }
    public User loginUser(String email, String password) {
        return userDAO.findUserByEmailAndPassword(email, password);
    }
    public boolean registerUser(User user) {
        User existingUser = userDAO.findUserByEmail(user.getEmail());
        if (existingUser != null) {return false;}
        userDAO.addUser(user);
        return true;
    }
}