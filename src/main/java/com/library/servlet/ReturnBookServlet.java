
package com.library.servlet;

import com.library.model.User;
import com.library.service.BookIssueService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

@WebServlet("/return-book")
public class ReturnBookServlet extends HttpServlet {

    private BookIssueService bookIssueService;

    @Override
    public void init() {
        bookIssueService = new BookIssueService();
    }

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
                    "Only administrators can return books."
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

        String issueIdValue = request.getParameter("issueId");

        if (issueIdValue == null || issueIdValue.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Issue ID is required."
            );
            return;
        }

        int issueId;

        try {
            issueId = Integer.parseInt(issueIdValue.trim());
        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Please enter a valid numeric Issue ID."
            );
            return;
        }

        if (issueId <= 0) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Issue ID must be positive."
            );
            return;
        }

        LocalDate returnDate = LocalDate.now();

        try {
            boolean returned = bookIssueService.returnBook(
                    issueId,
                    returnDate.toString()
            );

            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            out.println("<!DOCTYPE html>");
            out.println("<html><head><meta charset='UTF-8'>");
            out.println("<title>Return Book</title></head><body>");

            if (returned) {
                out.println("<h2>Book returned successfully!</h2>");
                out.println("<p>Return Date: " + returnDate + "</p>");
            } else {
                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST
                );
                out.println("<h2>Return failed</h2>");
                out.println(
                        "<p>Issue ID does not exist or the book is already returned.</p>"
                );
            }

            out.println("<p><a href='"
                    + request.getContextPath()
                    + "/return-book'>Back to Return Book</a></p>");

            out.println("</body></html>");

        } catch (IllegalArgumentException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid return request."
            );

        } catch (IllegalStateException e) {
            getServletContext().log(
                    "Return rejected due to an invalid library state.",
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_CONFLICT,
                    "The book could not be returned in its current state."
            );

        } catch (RuntimeException e) {
            getServletContext().log("Failed to return book.", e);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Book could not be returned due to a system problem."
            );
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        if (!isAdmin(request, response)) {
            return;
        }

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();
        String contextPath = request.getContextPath();

        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'>");
        out.println("<title>Return Book</title></head><body>");

        out.println("<h2>Return a Book</h2>");

        out.println("<form method='post' action='"
                + contextPath + "/return-book'>");

        out.println("<label for='issueId'>Issue ID:</label>");
        out.println("<input type='number' id='issueId' name='issueId' "
                + "min='1' required>");

        out.println("<button type='submit'>Return Book</button>");
        out.println("</form>");

        out.println("<p><a href='"
                + contextPath
                + "/issue-book'>View Issued Books</a></p>");

        out.println("<p><a href='"
                + contextPath
                + "/admin-dashboard.html'>Back to Dashboard</a></p>");

        out.println("</body></html>");
    }
}