package com.library.dao;

import com.library.model.Book;
import com.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public class BookDAO {

    private SessionFactory sessionFactory;

    public BookDAO() {
        sessionFactory = HibernateUtil.getSessionFactory();
    }


    public void addBook(Book book) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {

            // Step 1: Start transaction
            transaction = session.beginTransaction();

            // Step 2: Save the book
            session.persist(book);

            // Step 3: Commit the transaction
            transaction.commit();

        } catch (RuntimeException e) {

            // Step 4: Roll back if the transaction is active
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            // Step 5: Pass the error to the calling layer
            throw e;
        }
    }

    public List<Book> getAllBooks() {
        try (Session session = sessionFactory.openSession()) {

            return session
                    .createQuery("from Book", Book.class)
                    .getResultList();

        }
    }

    public Book getBookById(int id) {
        if (id <= 0) {
            return null;
        }

        try (Session session = sessionFactory.openSession()) {
            return session.get(Book.class, id);
        }
    }

    public void updateBook(Book book) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {

            // Step 1: Start transaction
            transaction = session.beginTransaction();

            // Step 2: Update the book
            session.merge(book);

            // Step 3: Commit changes
            transaction.commit();

        } catch (RuntimeException e) {

            // Step 4: Roll back if the transaction is active
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            // Step 5: Pass the error to the calling layer
            throw e;
        }
    }

    public void deleteBookById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Book ID must be positive.");
        }

        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Book book = session.get(Book.class, id);

            if (book == null) {
                throw new IllegalStateException("Book does not exist.");
            }

            session.remove(book);
            transaction.commit();

        } catch (RuntimeException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw e;
        }
    }

}