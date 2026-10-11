
package com.library.servlet;

import com.library.model.Member;
import com.library.service.MemberService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/edit-member")
public class EditMemberServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        String idValue = request.getParameter("id");

        if (idValue == null || idValue.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Member ID is required."
            );
            return;
        }

        int id;

        try {
            id = Integer.parseInt(idValue.trim());
        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid member ID."
            );
            return;
        }

        if (id <= 0) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Member ID must be positive."
            );
            return;
        }

        try {
            MemberService memberService = new MemberService();
            Member member = memberService.getMemberById(id);

            if (member == null) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Member not found."
                );
                return;
            }

            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            out.println("<!DOCTYPE html>");
            out.println("<html><head><title>Edit Member</title></head><body>");
            out.println("<h1>Edit Member</h1>");

            out.println("<form action='"
                    + request.getContextPath()
                    + "/edit-member' method='post'>");

            out.println("<input type='hidden' name='id' value='"
                    + member.getId() + "'>");

            out.println("Membership ID: <input type='text' name='membershipId' value='"
                    + escapeHtml(member.getMembershipId())
                    + "' required><br><br>");

            String status = member.getMembershipStatus();

            out.println("Status: <select name='membershipStatus' required>");
            out.println("<option value='ACTIVE' "
                    + ("ACTIVE".equals(status) ? "selected" : "")
                    + ">ACTIVE</option>");
            out.println("<option value='INACTIVE' "
                    + ("INACTIVE".equals(status) ? "selected" : "")
                    + ">INACTIVE</option>");
            out.println("</select><br><br>");

            out.println("Phone: <input type='tel' name='phone' value='"
                    + escapeHtml(member.getPhone())
                    + "' required><br><br>");

            out.println("Address: <textarea name='address' required>"
                    + escapeHtml(member.getAddress())
                    + "</textarea><br><br>");

            out.println("<button type='submit'>Update Member</button>");
            out.println("</form>");

            out.println("<br><a href='"
                    + request.getContextPath()
                    + "/members'>Back to Members</a>");

            out.println("</body></html>");

        } catch (RuntimeException e) {
            getServletContext().log(
                    "Error while loading member edit form.",
                    e
            );

            if (!response.isCommitted()) {
                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Unable to load member details."
                );
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        String idValue = request.getParameter("id");
        String membershipId = request.getParameter("membershipId");
        String status = request.getParameter("membershipStatus");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");

        if (idValue == null || idValue.isBlank()
                || membershipId == null || membershipId.isBlank()
                || status == null
                || phone == null || phone.isBlank()
                || address == null || address.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "All fields are required."
            );
            return;
        }

        try {
            int id = Integer.parseInt(idValue.trim());

            if (id <= 0) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Member ID must be positive."
                );
                return;
            }

            if (!"ACTIVE".equals(status)
                    && !"INACTIVE".equals(status)) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid membership status."
                );
                return;
            }

            MemberService memberService = new MemberService();
            Member member = memberService.getMemberById(id);

            if (member == null) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Member not found."
                );
                return;
            }

            member.setMembershipId(membershipId.trim());
            member.setMembershipStatus(status);
            member.setPhone(phone.trim());
            member.setAddress(address.trim());

            memberService.updateMember(member);

            response.sendRedirect(
                    request.getContextPath() + "/members"
            );

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid member ID."
            );

        } catch (RuntimeException e) {
            getServletContext().log(
                    "Error while updating member.",
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to update member right now. Please try again."
            );
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