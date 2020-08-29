package org.acme.getting.started;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class GreetingResourceTest {

    private static List<String> tenantIds = IntStream.range(1, 10).boxed()
            .map(n -> String.format("tenant%02d", n))
            .collect(Collectors.toList());

    @Inject
    @RestClient
    GreetingClient greetingRestClient;

    @Test
    public void testGiftEndpoint() {
        for (String tenantId : tenantIds) {
            Gift gift = greetingRestClient.addgift("Somegift", tenantId);
            Gift returnedGift = greetingRestClient.getgift(gift.getId(), tenantId);

            Assertions.assertEquals(gift.getName(), returnedGift.getName());
            Assertions.assertEquals(gift.getId(), returnedGift.getId());
        }
    }


    @Test
    public void testJdbc() {
        for (String tenantId : tenantIds) {

            //This will test that autocommit is false
            int value = 42;
            given().header("X-tenant", tenantId).pathParam("value", value)
                    .when().put("/jdbc/{value}")
                    .then().statusCode(200).body(is(String.valueOf(value)));
        }
    }

    @Test
    public void testWriteReadBlob() {
        for (String tenantId : tenantIds) {
            String content = "Some content for the blob";
            long looid = greetingRestClient.writeblob(content, tenantId);
            String content_read = greetingRestClient.readblob(looid, tenantId);
            Assertions.assertEquals(content, content_read);
        }
    }

    // **************************************************************************************************************
    // The following test cases tests if transactions are working, because a rollback is attempted at the server side
    // **************************************************************************************************************

    @Test
    public void testGiftEndpoint_cheat() {
        for (String tenantId : tenantIds) {
            //The gift should not be created at the server side, because a rollback is performed
            Gift gift = greetingRestClient.cheatgift("Somegift", tenantId);
            Gift returnedGift = greetingRestClient.getgift(gift.getId(), tenantId);

            //If the returned gift is null, then it means that the rollback on server side was successfull
            Assertions.assertNull(returnedGift);
        }
    }

    @Test
    public void testWriteReadBlob_cheat() {
        for (String tenantId : tenantIds) {

            //The blob should not be created at the server side, because a rollback is performed
            String content = "Some content for the blob";
            long looid = greetingRestClient.cheatwriteblob(content, tenantId);
            String content_read = greetingRestClient.readblob(looid, tenantId);
            Assertions.assertNull(content_read);
        }
    }
}