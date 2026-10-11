
package com.library.servlet;

import com.library.model.User;
import com.library.service.BookService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/delete-book")
public class DeleteBookServlet extends HttpServlet {

    private boolean isAdmin(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        HttpSession session = request.getSession(false);
        String contextPath = request.getContextPath();

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(contextPath + "/login.html");
            return false;
        }

        Object sessionUser = session.getAttribute("user");

        if (!(sessionUser instanceof User)) {
            session.invalidate();
            response.sendRedirect(contextPath + "/login.html");
            return false;
        }

        User user = (User) sessionUser;

        if (!"ADMIN".equals(user.getRole())) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Only administrators can delete books."
            );
            return false;
        }

        return true;
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        if (!isAdmin(request, response)) {
            return;
        }

        String idValue = request.getParameter("id");

        if (idValue == null || idValue.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Book ID is required."
            );
            return;
        }

        try {
            int id = Integer.parseInt(idValue.trim());

            if (id <= 0) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Book ID must be a positive number."
                );
                return;
            }

            BookService bookService = new BookService();
            bookService.deleteBook(id);

            response.sendRedirect(
                    request.getContextPath() + "/books"
            );

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid book ID."
            );

        } catch (IllegalArgumentException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid book ID."
            );

        } catch (IllegalStateException e) {
            getServletContext().log(
                    "Book deletion rejected.",
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_CONFLICT,
                    "This book cannot be deleted. It may have existing borrowing records."
            );

        } catch (RuntimeException e) {
            getServletContext().log(
                    "Error while deleting book.",
                    e
            );

            if (!response.isCommitted()) {
                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Unable to delete book right now. Please try again."
                );
            }
        }
    }
}