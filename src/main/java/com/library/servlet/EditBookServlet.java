
package com.library.servlet;

import com.library.model.Book;
import com.library.model.User;
import com.library.service.BookService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/edit-book")
public class EditBookServlet extends HttpServlet {

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
                    "Only administrators can edit books."
            );
            return false;
        }

        return true;
    }

    @Override
    protected void doGet(
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

        int id;

        try {
            id = Integer.parseInt(idValue.trim());
        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid book ID."
            );
            return;
        }

        if (id <= 0) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Book ID must be positive."
            );
            return;
        }

        try {
            BookService bookService = new BookService();
            Book book = bookService.getBookById(id);

            if (book == null) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Book does not exist."
                );
                return;
            }

            response.setContentType("text/html;charset=UTF-8");

            PrintWriter out = response.getWriter();
            String contextPath = request.getContextPath();

            out.println("<!DOCTYPE html>");
            out.println("<html><head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<title>Edit Book</title>");
            out.println("</head><body>");

            out.println("<h1>Edit Book</h1>");

            out.println("<form action='"
                    + contextPath
                    + "/update-book' method='post'>");

            out.println("<input type='hidden' name='id' value='"
                    + book.getId() + "'>");

            out.println("<label>Title:</label>");
            out.println("<input type='text' name='title' maxlength='255' value='"
                    + escapeHtml(book.getTitle())
                    + "' required><br><br>");

            out.println("<label>Author:</label>");
            out.println("<input type='text' name='author' maxlength='255' value='"
                    + escapeHtml(book.getAuthor())
                    + "' required><br><br>");

            out.println("<label>ISBN:</label>");
            out.println("<input type='text' name='isbn' maxlength='50' value='"
                    + escapeHtml(book.getIsbn())
                    + "' required><br><br>");

            out.println("<label>Category ID:</label>");
            out.println("<input type='number' name='categoryId' value='"
                    + book.getCategoryId()
                    + "' min='1' required><br><br>");

            out.println("<label>Total Copies:</label>");
            out.println("<input type='number' name='totalCopies' value='"
                    + book.getTotalCopies()
                    + "' min='1' required><br><br>");

            out.println("<button type='submit'>Update Book</button>");
            out.println("</form>");

            out.println("<p><a href='"
                    + contextPath
                    + "/books'>Back to Book List</a></p>");

            out.println("</body></html>");

        } catch (RuntimeException e) {
            getServletContext().log(
                    "Error while loading the edit book form.",
                    e
            );

            if (!response.isCommitted()) {
                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Unable to load the book right now."
                );
            }
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}