package com.github.edgar615.gateway.test.endpoint;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/2/7.
 *
 * @author Edgar  Date 2017/2/7
 */
@RunWith(VertxUnitRunner.class)
public class DummyTest {

    @Test
    public void testOk(TestContext testContext) {
        AtomicBoolean check = new AtomicBoolean();
        Vertx.vertx().createHttpClient().get(9000, "localhost", "/ping")
                .handler(resp -> {
                    testContext.assertEquals(200, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        testContext.assertTrue(body.toJsonObject().isEmpty());
                        check.set(true);
                    });
                })
                .setChunked(true)
                .end();
        Awaitility.await().until(() -> check.get());
    }


}
