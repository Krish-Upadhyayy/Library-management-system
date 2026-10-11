package com.library;

import com.library.model.User;
import com.library.service.UserService;

public class TestHibernate {

    public static void main(String[] args) {

        UserService userService = new UserService();

        // First user
        User user1 = new User();

        user1.setName("Krish");
        user1.setEmail("krish@gmail.com");
        user1.setPassword("12345");
        user1.setRole("MEMBER");

        boolean result1 = userService.registerUser(user1);

        if (result1) {
            System.out.println("User 1 registered successfully!");
        } else {
            System.out.println("User 1 registration failed!");
        }


        // Second user with same email
        User user2 = new User();

        user2.setName("Rahul");
        user2.setEmail("krish@gmail.com");
        user2.setPassword("67890");
        user2.setRole("MEMBER");

        boolean result2 = userService.registerUser(user2);

        if (result2) {
            System.out.println("User 2 registered successfully!");
        } else {
            System.out.println("User 2 registration failed!");
        }
    }
}