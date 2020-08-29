package org.acme.getting.started;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@ApplicationScoped
@Transactional
public class GreetingResource implements GreetingResourceIf {

    @Inject
    EntityManager em;

    @Inject
    TransactionManager tm;

    @Override
    public Gift getgift(Long id) {
        return em.find(Gift.class, id);
    }

    @Override
    public Gift addgift(String name) {
        Gift gift = new Gift();
        gift.setName(name);
        em.persist(gift);
        return gift;
    }

    @Override
    public Gift cheatgift(String name) throws SystemException {
        Gift gift = new Gift();
        gift.setName(name);
        em.persist(gift);
        em.flush();
        tm.setRollbackOnly();
        return gift;
    }

    @Override
    public int jdbc(int value) {
        return em.unwrap(Session.class).doReturningWork(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TEMP TABLE temptable (a integer)");
            }

            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO temptable VALUES (?)")) {
                statement.setInt(1, value);
                statement.execute();
            }

            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("SELECT * FROM temptable");
                result.next();
                int a = result.getInt(1);
                result.close();
                return a;
            }
        });
    }

    @Override
    public long writeblob(String content) {
        ByteArrayInputStream bytes = new ByteArrayInputStream(content.getBytes());
        return em.unwrap(Session.class).doReturningWork(connection -> {
            connection.setAutoCommit(false);
            LargeObjectHelper largeObjectHelper = new LargeObjectHelper(connection);
            return largeObjectHelper.createBlob(bytes);
        });
    }

    @Override
    public long cheatwriteblob(String content) throws SystemException {
        ByteArrayInputStream bytes = new ByteArrayInputStream(content.getBytes());
        long oid = em.unwrap(Session.class).doReturningWork(connection -> {
            connection.setAutoCommit(false);
            LargeObjectHelper largeObjectHelper = new LargeObjectHelper(connection);
            return largeObjectHelper.createBlob(bytes);
        });

        tm.setRollbackOnly();
        return oid;
    }

    @Override
    public String readblob(long oid) {
        try {
            return em.unwrap(Session.class).doReturningWork(connection -> {
                connection.setAutoCommit(false);
                LargeObjectHelper largeObjectHelper = new LargeObjectHelper(connection);
                byte[] bytes = largeObjectHelper.getBlobAsByteArray(oid);
                return new String(bytes);
            });
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

