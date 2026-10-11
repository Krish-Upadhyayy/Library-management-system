
package com.library.servlet;

import com.library.model.BookIssue;
import com.library.service.BookIssueService;
import com.library.service.BookService;
import com.library.service.MemberService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/issue-book")
public class BookIssueServlet extends HttpServlet {

    private BookIssueService bookIssueService;
    private MemberService memberService;
    private BookService bookService;

    @Override
    public void init() {
        bookIssueService = new BookIssueService();
        memberService = new MemberService();
        bookService = new BookService();
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        String memberIdValue = request.getParameter("memberId");
        String bookIdValue = request.getParameter("bookId");

        if (memberIdValue == null || memberIdValue.isBlank()
                || bookIdValue == null || bookIdValue.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Member ID and Book ID are required."
            );
            return;
        }

        int memberId;
        int bookId;

        try {
            memberId = Integer.parseInt(memberIdValue.trim());
            bookId = Integer.parseInt(bookIdValue.trim());

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Enter valid numeric IDs."
            );
            return;
        }

        if (memberId <= 0 || bookId <= 0) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "IDs must be positive numbers."
            );
            return;
        }

        try {
            if (memberService.getMemberById(memberId) == null) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Member does not exist."
                );
                return;
            }

            if (bookService.getBookById(bookId) == null) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Book does not exist."
                );
                return;
            }

            if (!bookIssueService.canIssueBook(memberId, bookId)) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Book cannot be issued. Check the 5-book limit "
                                + "and available copies."
                );
                return;
            }

            LocalDate issueDate = LocalDate.now();
            LocalDate dueDate = issueDate.plusDays(14);

            BookIssue bookIssue = new BookIssue();
            bookIssue.setMemberId(memberId);
            bookIssue.setBookId(bookId);
            bookIssue.setIssueDate(issueDate.toString());
            bookIssue.setDueDate(dueDate.toString());
            bookIssue.setReturnDate(null);
            bookIssue.setStatus("ISSUED");

            // DAO performs the final transactional checks.
            bookIssueService.addBookIssue(bookIssue);

            response.setContentType("text/plain;charset=UTF-8");

            PrintWriter out = response.getWriter();
            out.println("Book issued successfully!");
            out.println("Issue Date: " + issueDate);
            out.println("Due Date: " + dueDate);

        } catch (IllegalStateException e) {
            getServletContext().log(
                    "Book issue rejected by business rules.",
                    e
            );

            if (!response.isCommitted()) {
                response.sendError(
                        HttpServletResponse.SC_CONFLICT,
                        "The book cannot be issued in its current state."
                );
            }

        } catch (RuntimeException e) {
            getServletContext().log(
                    "Failed to issue book.",
                    e
            );

            if (!response.isCommitted()) {
                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Book could not be issued due to a system problem."
                );
            }
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        List<BookIssue> issues;

        try {
            issues = bookIssueService.getAllBookIssues();

        } catch (RuntimeException e) {
            getServletContext().log(
                    "Failed to load issued books.",
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load issued books."
            );
            return;
        }

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();
        String contextPath = request.getContextPath();

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head><title>Issued Books</title></head>");
        out.println("<body>");

        out.println("<h2>Issued Books</h2>");

        out.println("<table border='1' cellpadding='8'>");
        out.println("<tr>");
        out.println("<th>Issue ID</th>");
        out.println("<th>Book ID</th>");
        out.println("<th>Member ID</th>");
        out.println("<th>Issue Date</th>");
        out.println("<th>Due Date</th>");
        out.println("<th>Status</th>");
        out.println("<th>Fine (₹)</th>");
        out.println("</tr>");

        for (BookIssue issue : issues) {
            out.println("<tr>");
            out.println("<td>" + issue.getId() + "</td>");
            out.println("<td>" + issue.getBookId() + "</td>");
            out.println("<td>" + issue.getMemberId() + "</td>");
            out.println("<td>" + escapeHtml(issue.getIssueDate()) + "</td>");
            out.println("<td>" + escapeHtml(issue.getDueDate()) + "</td>");
            out.println("<td>" + escapeHtml(issue.getStatus()) + "</td>");

            try {
                long fine = bookIssueService.calculateFine(issue);
                out.println("<td>" + fine + "</td>");
            } catch (RuntimeException e) {
                getServletContext().log(
                        "Failed to calculate fine for issue ID "
                                + issue.getId(),
                        e
                );
                out.println("<td>Unavailable</td>");
            }

            out.println("</tr>");
        }

        out.println("</table>");

        out.println("<p><a href='" + contextPath
                + "/return-book'>Return a Book</a></p>");

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