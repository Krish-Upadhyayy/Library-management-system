
package com.library.servlet;

import com.library.model.User;
import com.library.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() {
        userService = new UserService();
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Email and password are required."
            );
            return;
        }

        try {
            User user = userService.loginUser(
                    email.trim(),
                    password
            );

            if (user == null) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Invalid email or password."
                );
                return;
            }

            HttpSession session = request.getSession(true);

            // Rotate the session ID after successful authentication.
            request.changeSessionId();

            session.setAttribute("user", user);

            response.sendRedirect(
                    request.getContextPath() + "/dashboard"
            );

        } catch (RuntimeException e) {
            getServletContext().log(
                    "Login failed because of a server-side error.",
                    e
            );

            if (!response.isCommitted()) {
                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Unable to process login right now."
                );
            }
        }
    }
}