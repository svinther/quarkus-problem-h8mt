package org.acme.getting.started;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@QuarkusTest
public class GreetingResourceTest {

    @Inject
    @RestClient
    GreetingRestClient greetingRestClient;

    @Test
    public void testGiftEndpoint() {
        Gift gift = greetingRestClient.addgift("Somegift", "tenant1");
        Gift returnedGift = greetingRestClient.getgift(gift.getId(), "tenant1");

        Assertions.assertEquals(gift.getName(), returnedGift.getName());
        Assertions.assertEquals(gift.getId(), returnedGift.getId());
    }

    @Test
    public void testGiftEndpoint_cheat() {
        //The gift should not be created at the server side, because a rollback is performed
        Gift gift = greetingRestClient.cheatgift("Somegift", "tenant1");
        Gift returnedGift = greetingRestClient.getgift(gift.getId(), "tenant1");

        //If the returned gift is null, then it means that the rollback on server side was successfull
        Assertions.assertNull(returnedGift);
    }




}