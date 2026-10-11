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
import java.util.List;

@WebServlet("/books")
public class BookListServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        String contextPath = request.getContextPath();
        HttpSession session = request.getSession(false);

        if (session == null || !(session.getAttribute("user") instanceof User)) {
            response.sendRedirect(contextPath + "/login.html");
            return;
        }

        User user = (User) session.getAttribute("user");
        String role = user.getRole();

        if (!"ADMIN".equals(role) && !"MEMBER".equals(role)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You are not authorized to view books."
            );
            return;
        }

        boolean isAdmin = "ADMIN".equals(role);

        List<Book> books;

        try {
            BookService bookService = new BookService();
            books = bookService.getAllBooks();

        } catch (RuntimeException e) {
            getServletContext().log("Error while loading books.", e);
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load books right now. Please try again."
            );
            return;
        }

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Book Catalogue | LibraryHub</title>");
        out.println("<link rel='stylesheet' href='" + contextPath + "/style.css'>");
        out.println("</head>");
        out.println("<body>");

        // Sidebar
        out.println("<aside class='sidebar'>");
        out.println("<div class='sidebar-brand'>");
        out.println("<h2>LibraryHub</h2>");
        out.println("<p>Library Management</p>");
        out.println("</div>");

        out.println("<nav>");

        if (isAdmin) {
            out.println("<a href='" + contextPath + "/admin-dashboard.html'>Dashboard</a>");
            out.println("<a href='" + contextPath + "/books' class='active'>Books</a>");
            out.println("<a href='" + contextPath + "/members'>Members</a>");
            out.println("<a href='" + contextPath + "/issue-book'>Issue Book</a>");
            out.println("<a href='" + contextPath + "/return-book'>Return Book</a>");
            out.println("<a href='" + contextPath + "/borrowing-history'>Borrowing History</a>");
        } else {
            out.println("<a href='" + contextPath + "/member-dashboard.html'>Dashboard</a>");
            out.println("<a href='" + contextPath + "/books' class='active'>Browse Books</a>");
            out.println("<a href='" + contextPath + "/my-borrowed-books'>My Borrowed Books</a>");
            out.println("<a href='" + contextPath + "/my-borrowing-history'>My Borrowing History</a>");
            out.println("<a href='" + contextPath + "/my-fines'>My Fines</a>");
        }

        out.println("</nav>");

        out.println("<div class='sidebar-footer'>");
        out.println("<form action='" + contextPath + "/logout' method='post'>");
        out.println("<button type='submit' class='btn'>Logout</button>");
        out.println("</form>");
        out.println("</div>");
        out.println("</aside>");

        // Main content
        out.println("<main class='main-content'>");
        out.println("<div class='page-container'>");

        out.println("<header class='page-header'>");
        out.println("<div>");
        out.println("<p>LIBRARY COLLECTION</p>");
        out.println("<h1>Book Catalogue</h1>");
        out.println("<p>Browse books and check their availability.</p>");
        out.println("</div>");
        out.println("</header>");

        out.println("<section class='card'>");
        out.println("<div class='page-header'>");
        out.println("<div>");
        out.println("<h2>All Books</h2>");
        out.println("<p>Explore the books currently listed in the library.</p>");
        out.println("</div>");

        if (isAdmin) {
            out.println("<a class='btn' href='" + contextPath + "/add-book.html'>+ Add Book</a>");
        }

        out.println("</div>");

        if (books == null || books.isEmpty()) {
            out.println("<div class='empty-state'>");
            out.println("<h3>No books found</h3>");
            out.println("<p>There are currently no books in the catalogue.</p>");

            if (isAdmin) {
                out.println("<a class='btn' href='" + contextPath + "/add-book.html'>Add Your First Book</a>");
            }

            out.println("</div>");
        } else {
            out.println("<div class='table-wrapper'>");
            out.println("<table>");
            out.println("<thead>");
            out.println("<tr>");
            out.println("<th>ID</th>");
            out.println("<th>Book Title</th>");
            out.println("<th>Author</th>");
            out.println("<th>ISBN</th>");
            out.println("<th>Total Copies</th>");
            out.println("<th>Available</th>");

            if (isAdmin) {
                out.println("<th>Actions</th>");
            }

            out.println("</tr>");
            out.println("</thead>");
            out.println("<tbody>");

            for (Book book : books) {
                out.println("<tr>");

                out.println("<td>" + book.getId() + "</td>");
                out.println("<td><strong>" + escapeHtml(book.getTitle()) + "</strong></td>");
                out.println("<td>" + escapeHtml(book.getAuthor()) + "</td>");
                out.println("<td>" + escapeHtml(book.getIsbn()) + "</td>");
                out.println("<td>" + book.getTotalCopies() + "</td>");
                out.println("<td><span class='status-badge'>"
                        + book.getAvailableCopies() + "</span></td>");

                if (isAdmin) {
                    out.println("<td>");

                    out.println("<div class='table-actions'>");

                    out.println("<a class='btn' href='" + contextPath
                            + "/edit-book?id=" + book.getId()
                            + "'>Edit</a>");

                    out.println("<form action='" + contextPath
                            + "/delete-book' method='post' "
                            + "style='display:inline-block; margin:0;' "
                            + "onsubmit=\"return confirm('Delete this book?');\">");

                    out.println("<input type='hidden' name='id' value='"
                            + book.getId() + "'>");

                    out.println("<button type='submit' class='btn'>Delete</button>");
                    out.println("</form>");

                    out.println("</div>");
                    out.println("</td>");
                }

                out.println("</tr>");
            }

            out.println("</tbody>");
            out.println("</table>");
            out.println("</div>");
        }

        out.println("</section>");
        out.println("</div>");
        out.println("</main>");

        out.println("</body>");
        out.println("</html>");
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
