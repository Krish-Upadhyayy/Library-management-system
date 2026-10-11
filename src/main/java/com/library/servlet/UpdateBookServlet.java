
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

@WebServlet("/update-book")
public class UpdateBookServlet extends HttpServlet {

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
                    "Only administrators can update books."
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

        request.setCharacterEncoding("UTF-8");

        String idValue = request.getParameter("id");
        String titleValue = request.getParameter("title");
        String authorValue = request.getParameter("author");
        String isbnValue = request.getParameter("isbn");
        String categoryValue = request.getParameter("categoryId");
        String copiesValue = request.getParameter("totalCopies");

        if (idValue == null || idValue.isBlank()
                || titleValue == null || titleValue.isBlank()
                || authorValue == null || authorValue.isBlank()
                || isbnValue == null || isbnValue.isBlank()
                || categoryValue == null || categoryValue.isBlank()
                || copiesValue == null || copiesValue.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "All fields are required."
            );
            return;
        }

        String title = titleValue.trim();
        String author = authorValue.trim();
        String isbn = isbnValue.trim();

        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()
                || title.length() > 255
                || author.length() > 255
                || isbn.length() > 50) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Please check the title, author and ISBN lengths."
            );
            return;
        }

        try {
            int id = Integer.parseInt(idValue.trim());
            int categoryId = Integer.parseInt(categoryValue.trim());
            int totalCopies = Integer.parseInt(copiesValue.trim());

            if (id <= 0 || categoryId <= 0 || totalCopies <= 0) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Book ID, category ID and total copies must be positive."
                );
                return;
            }

            BookService bookService = new BookService();
            Book book = bookService.getBookById(id);

            if (book == null) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Book not found."
                );
                return;
            }

            int borrowedCopies =
                    book.getTotalCopies() - book.getAvailableCopies();

            if (borrowedCopies < 0
                    || totalCopies < borrowedCopies) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Total copies cannot be less than borrowed copies."
                );
                return;
            }

            book.setTitle(title);
            book.setAuthor(author);
            book.setIsbn(isbn);
            book.setCategoryId(categoryId);
            book.setTotalCopies(totalCopies);
            book.setAvailableCopies(totalCopies - borrowedCopies);

            bookService.updateBook(book);

            response.sendRedirect(
                    request.getContextPath() + "/books"
            );

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid numeric input."
            );

        } catch (RuntimeException e) {
            getServletContext().log(
                    "Error while updating book.",
                    e
            );

            if (!response.isCommitted()) {
                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Unable to update the book right now. Please try again."
                );
            }
        }
    }
}