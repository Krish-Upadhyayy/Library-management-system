
package com.library.dao;

import com.library.model.Book;
import com.library.model.BookIssue;
import com.library.model.Member;
import com.library.util.HibernateUtil;

import jakarta.persistence.LockModeType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public class BookIssueDAO {

    private final SessionFactory sessionFactory;

    public BookIssueDAO() {
        sessionFactory = HibernateUtil.getSessionFactory();
    }

    // Issue a book and decrease available copies in one transaction
    public void addBookIssue(BookIssue bookIssue) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            // Lock member to protect the five-book limit
            Member member = session.find(
                    Member.class,
                    bookIssue.getMemberId(),
                    LockModeType.PESSIMISTIC_WRITE
            );

            if (member == null) {
                throw new IllegalStateException("Member does not exist.");
            }

            long activeIssues = session.createQuery(
                            "select count(bi) from BookIssue bi "
                                    + "where bi.memberId = :memberId "
                                    + "and bi.status = :status",
                            Long.class
                    )
                    .setParameter("memberId", bookIssue.getMemberId())
                    .setParameter("status", "ISSUED")
                    .getSingleResult();

            if (activeIssues >= 5) {
                throw new IllegalStateException(
                        "Member already has the maximum of 5 active books."
                );
            }

            // Lock book so simultaneous requests cannot issue the same last copy
            Book book = session.find(
                    Book.class,
                    bookIssue.getBookId(),
                    LockModeType.PESSIMISTIC_WRITE
            );

            if (book == null) {
                throw new IllegalStateException("Book does not exist.");
            }

            if (book.getAvailableCopies() <= 0) {
                throw new IllegalStateException(
                        "No copies of this book are currently available."
                );
            }

            book.setAvailableCopies(book.getAvailableCopies() - 1);
            session.persist(bookIssue);

            transaction.commit();

        } catch (RuntimeException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<BookIssue> getAllBookIssues() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "from BookIssue", BookIssue.class
            ).getResultList();
        }
    }

    public BookIssue getBookIssueById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(BookIssue.class, id);
        }
    }

    public long countActiveIssuesByMember(int memberId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "select count(bi) from BookIssue bi "
                                    + "where bi.memberId = :memberId "
                                    + "and bi.status = :status",
                            Long.class
                    )
                    .setParameter("memberId", memberId)
                    .setParameter("status", "ISSUED")
                    .getSingleResult();
        }
    }

    // True when the title has no available copies
    public boolean isBookAlreadyIssued(int bookId) {
        try (Session session = sessionFactory.openSession()) {
            Book book = session.get(Book.class, bookId);
            return book == null || book.getAvailableCopies() <= 0;
        }
    }

    // Return a book and restore availability in one transaction
    public boolean returnBook(int issueId, String returnDate) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            BookIssue bookIssue = session.find(
                    BookIssue.class,
                    issueId,
                    LockModeType.PESSIMISTIC_WRITE
            );

            if (bookIssue == null
                    || !"ISSUED".equals(bookIssue.getStatus())) {
                transaction.rollback();
                return false;
            }

            Book book = session.find(
                    Book.class,
                    bookIssue.getBookId(),
                    LockModeType.PESSIMISTIC_WRITE
            );

            if (book == null) {
                throw new IllegalStateException(
                        "The book record for this issue could not be found."
                );
            }

            if (book.getAvailableCopies() >= book.getTotalCopies()) {
                throw new IllegalStateException(
                        "Available copies cannot exceed total copies."
                );
            }

            bookIssue.setReturnDate(returnDate);
            bookIssue.setStatus("RETURNED");
            book.setAvailableCopies(book.getAvailableCopies() + 1);

            transaction.commit();
            return true;

        } catch (RuntimeException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<BookIssue> getActiveIssuesByMember(int memberId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "from BookIssue bi "
                                    + "where bi.memberId = :memberId "
                                    + "and bi.status = :status",
                            BookIssue.class
                    )
                    .setParameter("memberId", memberId)
                    .setParameter("status", "ISSUED")
                    .getResultList();
        }
    }

    public List<BookIssue> getIssueHistoryByMember(int memberId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "from BookIssue bi "
                                    + "where bi.memberId = :memberId "
                                    + "order by bi.id desc",
                            BookIssue.class
                    )
                    .setParameter("memberId", memberId)
                    .getResultList();
        }
    }
}