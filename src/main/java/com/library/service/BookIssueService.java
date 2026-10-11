
package com.library.service;

import com.library.dao.BookIssueDAO;
import com.library.model.BookIssue;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BookIssueService {

    private final BookIssueDAO bookIssueDAO;

    public BookIssueService() {
        bookIssueDAO = new BookIssueDAO();
    }

    public boolean returnBook(int issueId, String returnDate) {
        if (issueId <= 0) {
            throw new IllegalArgumentException(
                    "Issue ID must be positive."
            );
        }

        if (returnDate == null || returnDate.isBlank()) {
            throw new IllegalArgumentException(
                    "Return date is required."
            );
        }

        LocalDate parsedReturnDate;

        try {
            parsedReturnDate = LocalDate.parse(returnDate.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Return date must use yyyy-MM-dd format.",
                    e
            );
        }

        if (parsedReturnDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Return date cannot be in the future."
            );
        }

        return bookIssueDAO.returnBook(
                issueId,
                parsedReturnDate.toString()
        );
    }

    public void addBookIssue(BookIssue bookIssue) {
        if (bookIssue == null) {
            throw new IllegalArgumentException(
                    "Book issue cannot be null."
            );
        }

        if (bookIssue.getMemberId() <= 0
                || bookIssue.getBookId() <= 0) {
            throw new IllegalArgumentException(
                    "Valid member and book IDs are required."
            );
        }

        bookIssueDAO.addBookIssue(bookIssue);
    }

    public List<BookIssue> getAllBookIssues() {
        return bookIssueDAO.getAllBookIssues();
    }

    public List<BookIssue> getActiveIssuesByMember(int memberId) {
        if (memberId <= 0) {
            throw new IllegalArgumentException(
                    "Member ID must be positive."
            );
        }

        return bookIssueDAO.getActiveIssuesByMember(memberId);
    }

    public List<BookIssue> getIssueHistoryByMember(int memberId) {
        if (memberId <= 0) {
            throw new IllegalArgumentException(
                    "Member ID must be positive."
            );
        }

        return bookIssueDAO.getIssueHistoryByMember(memberId);
    }

    public BookIssue getBookIssueById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Issue ID must be positive."
            );
        }

        return bookIssueDAO.getBookIssueById(id);
    }

    public boolean canIssueBook(int memberId, int bookId) {
        if (memberId <= 0 || bookId <= 0) {
            return false;
        }

        long activeBooks =
                bookIssueDAO.countActiveIssuesByMember(memberId);

        boolean noAvailableCopies =
                bookIssueDAO.isBookAlreadyIssued(bookId);

        return activeBooks < 5 && !noAvailableCopies;
    }

    public long calculateFine(BookIssue bookIssue) {
        if (bookIssue == null
                || bookIssue.getDueDate() == null
                || bookIssue.getDueDate().isBlank()) {
            return 0;
        }

        try {
            LocalDate dueDate =
                    LocalDate.parse(bookIssue.getDueDate());

            LocalDate effectiveDate;

            if ("RETURNED".equals(bookIssue.getStatus())
                    && bookIssue.getReturnDate() != null
                    && !bookIssue.getReturnDate().isBlank()) {

                effectiveDate =
                        LocalDate.parse(bookIssue.getReturnDate());

            } else {
                effectiveDate = LocalDate.now();
            }

            long lateDays =
                    ChronoUnit.DAYS.between(dueDate, effectiveDate);

            if (lateDays <= 0) {
                return 0;
            }

            return lateDays * 5;

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid issue due date or return date.",
                    e
            );
        }
    }
}