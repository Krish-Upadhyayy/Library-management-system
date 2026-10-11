 package com.library.servlet;

import com.library.model.Member;
import com.library.model.User;
import com.library.service.MemberService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/members")
public class MemberListServlet extends HttpServlet {

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

        if (!"ADMIN".equals(user.getRole())) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Only administrators can manage members."
            );
            return;
        }

        List<Member> members;

        try {
            MemberService memberService = new MemberService();
            members = memberService.getAllMembers();
        } catch (RuntimeException e) {
            getServletContext().log("Error while loading members.", e);
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load members right now. Please try again."
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
        out.println("<title>Members | LibraryHub</title>");
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
        out.println("<a href='" + contextPath + "/members' class='active'>Members</a>");
        out.println("<a href='" + contextPath + "/issue-book'>Issue Book</a>");
        out.println("<a href='" + contextPath + "/return-book'>Return Book</a>");
        out.println("<a href='" + contextPath + "/borrowing-history'>Borrowing History</a>");
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
        out.println("<p>MEMBER MANAGEMENT</p>");
        out.println("<h1>Library Members</h1>");
        out.println("<p>View and manage registered library members.</p>");
        out.println("</div>");
        out.println("<a class='btn' href='" + contextPath
                + "/add-member.html'>+ Add Member</a>");
        out.println("</header>");

        out.println("<section class='card'>");
        out.println("<h2>Member Directory</h2>");
        out.println("<p>Membership details and contact information.</p>");

        if (members == null || members.isEmpty()) {

            out.println("<div class='empty-state'>");
            out.println("<h3>No members found</h3>");
            out.println("<p>Register a member to get started.</p>");
            out.println("<a class='btn' href='" + contextPath
                    + "/add-member.html'>Add First Member</a>");
            out.println("</div>");

        } else {

            out.println("<div class='table-wrapper'>");
            out.println("<table>");
            out.println("<thead>");
            out.println("<tr>");
            out.println("<th>ID</th>");
            out.println("<th>User ID</th>");
            out.println("<th>Membership ID</th>");
            out.println("<th>Membership Date</th>");
            out.println("<th>Status</th>");
            out.println("<th>Phone</th>");
            out.println("<th>Address</th>");
            out.println("<th>Actions</th>");
            out.println("</tr>");
            out.println("</thead>");
            out.println("<tbody>");

            for (Member member : members) {

                out.println("<tr>");

                out.println("<td>" + member.getId() + "</td>");
                out.println("<td>" + member.getUserId() + "</td>");
                out.println("<td>" + escapeHtml(member.getMembershipId()) + "</td>");
                out.println("<td>" + member.getMembershipDate() + "</td>");
                out.println("<td><span class='status-badge'>"
                        + escapeHtml(member.getMembershipStatus()) + "</span></td>");
                out.println("<td>" + escapeHtml(member.getPhone()) + "</td>");
                out.println("<td>" + escapeHtml(member.getAddress()) + "</td>");

                out.println("<td>");
                out.println("<div class='table-actions'>");

                out.println("<a class='btn' href='" + contextPath
                        + "/edit-member?id=" + member.getId()
                        + "'>Edit</a>");

                out.println("<form action='" + contextPath
                        + "/delete-member' method='post' "
                        + "onsubmit=\"return confirm('Delete this member?');\" "
                        + "style='display:inline-block; margin:0;'>");

                out.println("<input type='hidden' name='id' value='"
                        + member.getId() + "'>");

                out.println("<button type='submit' class='btn'>Delete</button>");
                out.println("</form>");

                out.println("</div>");
                out.println("</td>");

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

    private String escapeHtml(Object value) {
        if (value == null) {
            return "";
        }

        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}

