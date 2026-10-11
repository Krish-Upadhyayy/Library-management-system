
package com.library.dao;

import com.library.model.Member;
import com.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public class MemberDAO {

    private SessionFactory sessionFactory;

    public MemberDAO() {
        sessionFactory = HibernateUtil.getSessionFactory();
    }

    // Get all members
    public List<Member> getAllMembers() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("from Member", Member.class)
                    .getResultList();
        }
    }

    // Get member by ID
    public Member getMemberById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Member.class, id);
        }
    }

    // Update member
    public void updateMember(Member member) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            session.merge(member);

            transaction.commit();

        } catch (RuntimeException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw e;
        }
    }

    // Delete member by ID

    public void deleteMemberById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Member ID must be positive.");
        }

        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Member member = session.get(Member.class, id);

            if (member == null) {
                throw new IllegalStateException("Member does not exist.");
            }

            session.remove(member);
            transaction.commit();

        } catch (RuntimeException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw e;
        }
    }

    // Add member
    public void addMember(Member member) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            session.persist(member);

            transaction.commit();

        } catch (RuntimeException e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw e;
        }
    }

    // Get member by user ID
    public Member getMemberByUserId(int userId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "from Member m where m.userId = :userId",
                            Member.class)
                    .setParameter("userId", userId)
                    .uniqueResult();
        }
    }
}