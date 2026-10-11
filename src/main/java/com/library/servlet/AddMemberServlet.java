
package com.library.servlet;

import com.library.model.Member;
import com.library.model.User;
import com.library.service.MemberService;
import com.library.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/add-member")
public class AddMemberServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String userIdValue = request.getParameter("userId");
        String membershipId = request.getParameter("membershipId");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");

        // Validate required fields
        if (userIdValue == null || userIdValue.isBlank()
                || membershipId == null || membershipId.isBlank()
                || phone == null || phone.isBlank()
                || address == null || address.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "All fields are required."
            );
            return;
        }

        try {
            int userId = Integer.parseInt(userIdValue.trim());

            if (userId <= 0) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "User ID must be positive."
                );
                return;
            }

            UserService userService = new UserService();
            User user = userService.getUserById(userId);

            if (user == null) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "User does not exist."
                );
                return;
            }

            if (!"MEMBER".equalsIgnoreCase(user.getRole())) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Only users with MEMBER role can get membership."
                );
                return;
            }

            Member member = new Member();
            member.setUserId(userId);
            member.setMembershipId(membershipId.trim());
            member.setMembershipDate(LocalDate.now().toString());
            member.setMembershipStatus("ACTIVE");
            member.setPhone(phone.trim());
            member.setAddress(address.trim());

            MemberService memberService = new MemberService();
            memberService.addMember(member);

            response.sendRedirect(
                    request.getContextPath() + "/members"
            );

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid User ID."
            );

        } catch (RuntimeException e) {
            getServletContext().log(
                    "Error while adding a member.",
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to add the member right now. Please try again."
            );
        }
    }
}