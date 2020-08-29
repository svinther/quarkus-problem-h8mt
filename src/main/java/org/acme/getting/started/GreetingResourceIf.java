package org.acme.getting.started;

import javax.transaction.SystemException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
public interface GreetingResourceIf {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/gift/{id}")
    Gift getgift(@PathParam("id") Long id);

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/gift/{name}")
    Gift addgift(@PathParam("name") String name);

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/cheatgift/{name}")
    Gift cheatgift(@PathParam("name") String name) throws SystemException;

    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/jdbc/{value}")
    int jdbc(@PathParam("value") int value);

    @PUT
    @Path("/blob/{content}")
    @Produces(MediaType.TEXT_PLAIN)
    long writeblob(@PathParam("content") String content);

    @PUT
    @Path("/cheatblob/{content}")
    @Produces(MediaType.TEXT_PLAIN)
    long cheatwriteblob(@PathParam("content") String content) throws SystemException;

    @GET
    @Path("/blob/{oid}")
    @Produces(MediaType.TEXT_PLAIN)
    String readblob(@PathParam("oid") long oid);
}
