package io.quarkus.vertx.http.router;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class RouterEventTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClasses(RouteProducer.class));

    @BeforeAll
    public static void setup() {
        RouteProducer.counter = 0;
    }

    @Test
    public void testRoute() {
        RestAssured.when().get("/boom").then().statusCode(200).body(is("ok"));
        assertEquals(1, RouteProducer.counter);

        RestAssured.given()
                .body("An example body")
                .post("/post")
                .then()
                .body(is("An example body"));
    }

    @Singleton
    public static class RouteProducer {

        private static int counter;

        void observeRouter(@Observes Router router) {
            counter++;
            router.get("/boom").handler(ctx -> ctx.response().setStatusCode(200).end("ok"));
            Route post = router.post("/post");
            post.handler(BodyHandler.create());
            post.handler(ctx -> ctx.response().end(ctx.getBody()));
        }

    }

}
