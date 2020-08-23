package org.acme.getting.started;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(baseUri = "http://localhost:8081")
public interface GreetingRestClient {
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
}