package com.library.servlet;

import com.library.dao.MemberDAO;
import com.library.model.BookIssue;
import com.library.model.Member;
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

@WebServlet("/my-borrowing-history")
public class MyBorrowingHistoryServlet extends HttpServlet {

    private BookIssueService bookIssueService;
    private MemberDAO memberDAO;

    @Override
    public void init() {
        bookIssueService = new BookIssueService();
        memberDAO = new MemberDAO();
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

        if (!"MEMBER".equals(user.getRole())) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Only members can access this page."
            );
            return;
        }

        try {
            Member member = memberDAO.getMemberByUserId(user.getId());

            List<BookIssue> allIssues = null;

            if (member != null) {
                allIssues =
                        bookIssueService.getIssueHistoryByMember(member.getId());
            }

            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            out.println("<!DOCTYPE html>");
            out.println("<html lang='en'>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>My Borrowing History | LibraryHub</title>");
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
            out.println("<a href='" + contextPath + "/member-dashboard.html'>Dashboard</a>");
            out.println("<a href='" + contextPath + "/books'>Browse Books</a>");
            out.println("<a href='" + contextPath + "/my-borrowed-books'>My Borrowed Books</a>");
            out.println("<a href='" + contextPath + "/my-borrowing-history' class='active'>My Borrowing History</a>");
            out.println("<a href='" + contextPath + "/my-fines'>My Fines</a>");
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
            out.println("<p>YOUR LIBRARY ACCOUNT</p>");
            out.println("<h1>My Borrowing History</h1>");
            out.println("<p>Review the books you have borrowed, including returned books.</p>");
            out.println("</div>");
            out.println("<a class='btn' href='" + contextPath + "/books'>Browse Catalogue</a>");
            out.println("</header>");

            out.println("<section class='card'>");
            out.println("<h2>Past and Current Borrowings</h2>");
            out.println("<p>Your borrowing activity in one place.</p>");

            if (member == null) {

                out.println("<div class='empty-state'>");
                out.println("<h3>Membership not found</h3>");
                out.println("<p>No library membership is linked to your account. Please contact the library administrator.</p>");
                out.println("</div>");

            } else if (allIssues == null || allIssues.isEmpty()) {

                out.println("<div class='empty-state'>");
                out.println("<h3>No borrowing history yet</h3>");
                out.println("<p>Your borrowing records will appear here when you borrow a book.</p>");
                out.println("<a class='btn' href='" + contextPath + "/books'>Explore Books</a>");
                out.println("</div>");

            } else {

                out.println("<div class='table-wrapper'>");
                out.println("<table>");
                out.println("<thead>");
                out.println("<tr>");
                out.println("<th>Issue ID</th>");
                out.println("<th>Book ID</th>");
                out.println("<th>Issue Date</th>");
                out.println("<th>Due Date</th>");
                out.println("<th>Return Date</th>");
                out.println("<th>Status</th>");
                out.println("</tr>");
                out.println("</thead>");
                out.println("<tbody>");

                for (BookIssue issue : allIssues) {

                    String returnDate = issue.getReturnDate() == null
                            ? "Not returned"
                            : issue.getReturnDate();

                    out.println("<tr>");
                    out.println("<td>" + issue.getId() + "</td>");
                    out.println("<td>" + issue.getBookId() + "</td>");
                    out.println("<td>" + escapeHtml(issue.getIssueDate()) + "</td>");
                    out.println("<td>" + escapeHtml(issue.getDueDate()) + "</td>");
                    out.println("<td>" + escapeHtml(returnDate) + "</td>");
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
                    "Failed to load borrowing history for user ID " + user.getId(),
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

