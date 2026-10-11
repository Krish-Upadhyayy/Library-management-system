package com.library.servlet;

import com.library.model.BookIssue;
import com.library.model.User;
import com.library.service.BookIssueService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/borrowing-history")
public class BorrowingHistoryServlet extends HttpServlet {

    private BookIssueService bookIssueService;

    @Override
    public void init() {
        bookIssueService = new BookIssueService();
    }

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

        if (!"ADMIN".equals(user.getRole())) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Only administrators can access borrowing history."
            );
            return;
        }

        try {
            List<BookIssue> issues =
                    bookIssueService.getAllBookIssues();

            response.setContentType("text/html;charset=UTF-8");

            PrintWriter out = response.getWriter();

            out.println("<!DOCTYPE html>");
            out.println("<html lang='en'>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>Borrowing History | LibraryHub</title>");
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
            out.println("<a href='" + contextPath + "/admin-dashboard.html'>Dashboard</a>");
            out.println("<a href='" + contextPath + "/books'>Books</a>");
            out.println("<a href='" + contextPath + "/members'>Members</a>");
            out.println("<a href='" + contextPath + "/issue-book'>Issue Book</a>");
            out.println("<a href='" + contextPath + "/return-book'>Return Book</a>");
            out.println("<a href='" + contextPath + "/borrowing-history' class='active'>Borrowing History</a>");
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
            out.println("<p>LIBRARY ACTIVITY</p>");
            out.println("<h1>Borrowing History</h1>");
            out.println("<p>Track book issues, due dates and returns.</p>");
            out.println("</div>");
            out.println("</header>");

            out.println("<section class='card'>");
            out.println("<h2>All Borrowing Records</h2>");
            out.println("<p>Complete history of book transactions.</p>");

            if (issues == null || issues.isEmpty()) {

                out.println("<div class='empty-state'>");
                out.println("<h3>No borrowing records</h3>");
                out.println("<p>Borrowing activity will appear here once a book is issued.</p>");
                out.println("</div>");

            } else {

                out.println("<div class='table-wrapper'>");
                out.println("<table>");
                out.println("<thead>");
                out.println("<tr>");
                out.println("<th>Issue ID</th>");
                out.println("<th>Book ID</th>");
                out.println("<th>Member ID</th>");
                out.println("<th>Issue Date</th>");
                out.println("<th>Due Date</th>");
                out.println("<th>Return Date</th>");
                out.println("<th>Status</th>");
                out.println("</tr>");
                out.println("</thead>");
                out.println("<tbody>");

                for (BookIssue issue : issues) {

                    out.println("<tr>");
                    out.println("<td>" + issue.getId() + "</td>");
                    out.println("<td>" + issue.getBookId() + "</td>");
                    out.println("<td>" + issue.getMemberId() + "</td>");

                    out.println("<td>"
                            + escapeHtml(issue.getIssueDate())
                            + "</td>");

                    out.println("<td>"
                            + escapeHtml(issue.getDueDate())
                            + "</td>");

                    String returnDate = issue.getReturnDate() == null
                            ? "Not returned"
                            : issue.getReturnDate();

                    out.println("<td>"
                            + escapeHtml(returnDate)
                            + "</td>");

                    out.println("<td><span class='status-badge'>"
                            + escapeHtml(issue.getStatus())
                            + "</span></td>");

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

        } catch (RuntimeException e) {
            getServletContext().log(
                    "Failed to load complete borrowing history.",
                    e
            );

            if (!response.isCommitted()) {
                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Unable to load borrowing history right now."
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

