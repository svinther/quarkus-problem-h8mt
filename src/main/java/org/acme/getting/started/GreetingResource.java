package org.acme.getting.started;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RequestScoped
@Path("/hello")
public class GreetingResource {

    @Inject
    EntityManager em;
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello " + em;
    }
}

