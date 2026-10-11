package com.library.dao;

import com.library.model.User;
import com.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;


public class UserDAO{

        private SessionFactory sessionFactory;

        public UserDAO(){
            sessionFactory = HibernateUtil.getSessionFactory();
        }

        public void addUser(User user){
            Session session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
            session.close();
        }

        public List<User> getAllUsers(){
            Session session = sessionFactory.openSession();
            List<User> users = session
                    .createQuery("from User", User.class)
                    .getResultList();
            session.close();
            return users;
        }
        public User getUserById(int id){
            Session session = sessionFactory.openSession();
            User user = session
                    .createQuery("from User where id=:id", User.class)
                    .setParameter("id",id)
                    .getSingleResult();

            session.close();
            return user;
        }
        public void updateUser(User user){
            Session session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();
            session.close();
        }
        public void deleteUser(int id){
            Session session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();
            User user = session.get(User.class,id);
            if (user != null) {session.remove(user);}
            transaction.commit();
            session.close();
        }
    public User findUserByEmailAndPassword(String email, String password) {
        Session session = sessionFactory.openSession();
        List users = session
                .createQuery("from User where email = :email and password = :password", User.class)
                .setParameter("email", email)
                .setParameter("password", password)
                .getResultList();
        session.close();
        if (users.isEmpty()) {
            return null;
        }
        return (User) users.get(0);
    }
    public User findUserByEmail(String email) {
        Session session = sessionFactory.openSession();
        List<User> users = session
                .createQuery("from User where email = :email", User.class)
                .setParameter("email", email)
                .getResultList();
        session.close();

        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }


}


