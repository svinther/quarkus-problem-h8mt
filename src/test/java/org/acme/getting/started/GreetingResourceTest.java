package org.acme.getting.started;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class GreetingResourceTest {

    @Inject
    @RestClient
    GreetingClient greetingRestClient;

    @Test
    public void testGiftEndpoint() {
        Gift gift = greetingRestClient.addgift("Somegift", "tenant1");
        Gift returnedGift = greetingRestClient.getgift(gift.getId(), "tenant1");

        Assertions.assertEquals(gift.getName(), returnedGift.getName());
        Assertions.assertEquals(gift.getId(), returnedGift.getId());
    }


    @Test
    public void testJdbc() {
        //This will test that autocommit is false
        int value = 42;
        given().header("X-tenant", "tenant1").pathParam("value", value)
                .when().put("/jdbc/{value}")
                .then().statusCode(200).body(is(String.valueOf(value)));
    }

    @Test
    public void testWriteReadBlob() {
        String content = "Some content for the blob";
        long looid = greetingRestClient.writeblob(content, "tenant3");
        String content_read = greetingRestClient.readblob(looid, "tenant3");
        Assertions.assertEquals(content, content_read);
    }

    // **************************************************************************************************************
    // The following test cases tests if transactions are working, because a rollback is attempted at the server side
    // **************************************************************************************************************

    @Test
    public void testGiftEndpoint_cheat() {
        //The gift should not be created at the server side, because a rollback is performed
        Gift gift = greetingRestClient.cheatgift("Somegift", "tenant1");
        Gift returnedGift = greetingRestClient.getgift(gift.getId(), "tenant1");

        //If the returned gift is null, then it means that the rollback on server side was successfull
        Assertions.assertNull(returnedGift);
    }

    @Test
    public void testWriteReadBlob_cheat() {
        //The blob should not be created at the server side, because a rollback is performed
        String content = "Some content for the blob";
        long looid = greetingRestClient.cheatwriteblob(content, "tenant3");
        String content_read = greetingRestClient.readblob(looid, "tenant3");
        Assertions.assertNull(content_read);
    }
}