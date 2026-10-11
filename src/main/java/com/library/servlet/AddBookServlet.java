
package com.library.servlet;

import com.library.model.Book;
import com.library.service.BookService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/add-book")
public class AddBookServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String isbn = request.getParameter("isbn");
        String category = request.getParameter("categoryId");
        String copies = request.getParameter("totalCopies");

        // Validate required text fields
        if (title == null || title.isBlank()
                || author == null || author.isBlank()
                || isbn == null || isbn.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Title, author and ISBN are required."
            );
            return;
        }

        // Validate required numeric fields
        if (category == null || category.isBlank()
                || copies == null || copies.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Category ID and total copies are required."
            );
            return;
        }

        try {
            int categoryId = Integer.parseInt(category.trim());
            int totalCopies = Integer.parseInt(copies.trim());

            if (categoryId <= 0 || totalCopies <= 0) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Category ID and total copies must be positive numbers."
                );
                return;
            }

            Book book = new Book();
            book.setTitle(title.trim());
            book.setAuthor(author.trim());
            book.setIsbn(isbn.trim());
            book.setCategoryId(categoryId);
            book.setTotalCopies(totalCopies);
            book.setAvailableCopies(totalCopies);

            BookService bookService = new BookService();
            bookService.addBook(book);

            response.sendRedirect(
                    request.getContextPath() + "/books"
            );

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Category ID and total copies must be valid numbers."
            );

        } catch (RuntimeException e) {
            getServletContext().log(
                    "Error while adding a book.",
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to add the book right now. Please try again."
            );
        }
    }
}