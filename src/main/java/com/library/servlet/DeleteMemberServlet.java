
package com.library.servlet;

import com.library.service.MemberService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/delete-member")
public class DeleteMemberServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
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

        try {
            int id = Integer.parseInt(idValue.trim());

            if (id <= 0) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Member ID must be positive."
                );
                return;
            }

            MemberService memberService = new MemberService();

            memberService.deleteMember(id);

            response.sendRedirect(
                    request.getContextPath() + "/members"
            );

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid member ID."
            );

        } catch (IllegalStateException e) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Member does not exist."
            );

        } catch (IllegalArgumentException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid member ID."
            );

        } catch (RuntimeException e) {
            getServletContext().log(
                    "Error while deleting member.",
                    e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to delete member right now. Please try again."
            );
        }
    }
}