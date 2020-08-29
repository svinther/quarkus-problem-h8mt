package org.acme.getting.started;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.transaction.SystemException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(baseUri = "http://localhost:8081")
public interface GreetingClient {
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/gift/{name}")
    Gift addgift(@PathParam("name") String name, @HeaderParam("X-tenant") String tenant);

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/cheatgift/{name}")
    Gift cheatgift(@PathParam("name") String name, @HeaderParam("X-tenant") String tenant);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/gift/{id}")
    Gift getgift(@PathParam("id") Long id, @HeaderParam("X-tenant") String tenant);

    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/jdbc/{value}")
    int jdbc(@PathParam("value") int value, @HeaderParam("X-tenant") String tenant);

    @PUT
    @Path("/blob/{content}")
    @Produces(MediaType.TEXT_PLAIN)
    long writeblob(@PathParam("content") String content, @HeaderParam("X-tenant") String tenant);

    @PUT
    @Path("/cheatblob/{content}")
    @Produces(MediaType.TEXT_PLAIN)
    long cheatwriteblob(@PathParam("content") String content, @HeaderParam("X-tenant") String tenant);

    @GET
    @Path("/blob/{oid}")
    @Produces(MediaType.TEXT_PLAIN)
    String readblob(@PathParam("oid") long oid, @HeaderParam("X-tenant") String tenant);
}