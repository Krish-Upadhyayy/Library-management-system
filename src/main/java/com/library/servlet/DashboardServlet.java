
package com.library.servlet;

import com.library.model.User;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        String contextPath = request.getContextPath();

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(contextPath + "/login.html");
            return;
        }

        Object sessionUser = session.getAttribute("user");

        if (!(sessionUser instanceof User)) {
            session.invalidate();
            response.sendRedirect(contextPath + "/login.html");
            return;
        }

        User user = (User) sessionUser;
        String role = user.getRole();

        if ("ADMIN".equals(role)) {
            response.sendRedirect(contextPath + "/admin-dashboard.html");

        } else if ("MEMBER".equals(role)) {
            response.sendRedirect(contextPath + "/member-dashboard.html");

        } else {
            session.invalidate();
            response.sendRedirect(contextPath + "/login.html");
        }
    }
}