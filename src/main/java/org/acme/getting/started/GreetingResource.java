package org.acme.getting.started;

import org.hibernate.Session;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.sql.Statement;

@ApplicationScoped
@Path("/")
@Transactional
public class GreetingResource {

    @Inject
    EntityManager em;

    @Inject
    TransactionManager tm;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/gift/{id}")
    public Gift getgift(@PathParam("id") Long id) {
        return em.find(Gift.class, id);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/gift/{name}")
    public Gift addgift(@PathParam("name") String name) {
        Gift gift = new Gift();
        gift.setName(name);
        em.persist(gift);
        return gift;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/cheatgift/{name}")
    public Gift cheatgift(@PathParam("name") String name) throws SystemException {
        Gift gift = new Gift();
        gift.setName(name);
        em.persist(gift);
        em.flush();
        tm.setRollbackOnly();
        return gift;
    }

    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/jdbc")
    public int jdbc() {
        return em.unwrap(Session.class).doReturningWork(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TEMP TABLE temptable (a integer)");
            }

            try (Statement statement = connection.createStatement()) {
                statement.execute("INSERT INTO temptable VALUES (42)");
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

    @PUT
    @Path("/blob/{content}")
    @Produces(MediaType.TEXT_PLAIN)
    public long writeblob(@PathParam("content") String content) {
        ByteArrayInputStream bytes = new ByteArrayInputStream(content.getBytes());
        return em.unwrap(Session.class).doReturningWork(connection -> {
            connection.setAutoCommit(false);
            LargeObjectHelper largeObjectHelper = new LargeObjectHelper(connection);
            return largeObjectHelper.createBlob(bytes);
        });
    }

    @GET
    @Path("/blob/{oid}")
    @Produces(MediaType.TEXT_PLAIN)
    public String readblob(@PathParam("oid") long oid) {
        return em.unwrap(Session.class).doReturningWork(connection -> {
            connection.setAutoCommit(false);
            LargeObjectHelper largeObjectHelper = new LargeObjectHelper(connection);
            byte[] bytes = largeObjectHelper.getBlobAsByteArray(oid);
            return new String(bytes);
        });
    }


}

