package com.github.edgar615.gateway.test.authentication;

import com.github.edgar615.util.exception.DefaultErrorCode;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/2/7.
 *
 * @author Edgar  Date 2017/2/7
 */
@RunWith(VertxUnitRunner.class)
public class JwtRestrictionTest {

    @Test
    public void testBlacklistPluginShouldForbidden(TestContext testContext) {
        JsonObject data = new JsonObject()
                .put("userId", 3)
                .put("foo", "bar");
        List<String> tokens = new CopyOnWriteArrayList<>();
        Vertx.vertx().createHttpClient().post(9000, "localhost",
                                              "/token")
                .handler(resp -> {
                    testContext.assertEquals(200, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        JsonObject jsonObject = body.toJsonObject();
                        testContext.assertTrue(jsonObject.containsKey("token"));
                        tokens.add(jsonObject.getString("token"));
                    });
                })
                .setChunked(true)
                .end(data.encode());
        Awaitility.await().until(() -> tokens.size() == 1);


        AtomicBoolean check = new AtomicBoolean();
        Vertx.vertx().createHttpClient().get(9000, "localhost",
                                             "/jwt/blacklist")
                .handler(resp -> {
                    testContext.assertEquals(403, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        testContext.assertEquals(DefaultErrorCode.PERMISSION_DENIED.getNumber(),
                                                 body.toJsonObject().getInteger("code"));
                        check.set(true);
                    });
                })
                .putHeader("Authorization", "Bearer " + tokens.get(0))
                .setChunked(true)
                .end();
        Awaitility.await().until(() -> check.get());
    }

    @Test
    public void testGlobalBlacklistShouldForbidden(TestContext testContext) {
        JsonObject data = new JsonObject()
                .put("userId", 8)
                .put("foo", "bar");
        List<String> tokens = new CopyOnWriteArrayList<>();
        Vertx.vertx().createHttpClient().post(9000, "localhost",
                                              "/token")
                .handler(resp -> {
                    testContext.assertEquals(200, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        JsonObject jsonObject = body.toJsonObject();
                        testContext.assertTrue(jsonObject.containsKey("token"));
                        tokens.add(jsonObject.getString("token"));
                    });
                })
                .setChunked(true)
                .end(data.encode());
        Awaitility.await().until(() -> tokens.size() == 1);


        AtomicBoolean check = new AtomicBoolean();
        Vertx.vertx().createHttpClient().get(9000, "localhost",
                                             "/jwt/blacklist")
                .handler(resp -> {
                    testContext.assertEquals(403, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        testContext.assertEquals(DefaultErrorCode.PERMISSION_DENIED.getNumber(),
                                                 body.toJsonObject().getInteger("code"));
                        check.set(true);
                    });
                })
                .putHeader("Authorization", "Bearer " + tokens.get(0))
                .setChunked(true)
                .end();
        Awaitility.await().until(() -> check.get());
    }

    @Test
    public void testWhitelistPluginShouldSuccess(TestContext testContext) {
        JsonObject data = new JsonObject()
                .put("userId", 2)
                .put("foo", "bar");
        List<String> tokens = new CopyOnWriteArrayList<>();
        Vertx.vertx().createHttpClient().post(9000, "localhost",
                                              "/token")
                .handler(resp -> {
                    testContext.assertEquals(200, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        JsonObject jsonObject = body.toJsonObject();
                        testContext.assertTrue(jsonObject.containsKey("token"));
                        tokens.add(jsonObject.getString("token"));
                    });
                })
                .setChunked(true)
                .end(data.encode());
        Awaitility.await().until(() -> tokens.size() == 1);


        AtomicBoolean check = new AtomicBoolean();
        Vertx.vertx().createHttpClient().get(9000, "localhost",
                                             "/jwt/blacklist")
                .handler(resp -> {
                    testContext.assertEquals(200, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        check.set(true);
                    });
                })
                .putHeader("Authorization", "Bearer " + tokens.get(0))
                .setChunked(true)
                .end();
        Awaitility.await().until(() -> check.get());
    }

    @Test
    public void testGlobalWhitelistShouldSuccess(TestContext testContext) {
        JsonObject data = new JsonObject()
                .put("userId", 4)
                .put("foo", "bar");
        List<String> tokens = new CopyOnWriteArrayList<>();
        Vertx.vertx().createHttpClient().post(9000, "localhost",
                                              "/token")
                .handler(resp -> {
                    testContext.assertEquals(200, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        JsonObject jsonObject = body.toJsonObject();
                        testContext.assertTrue(jsonObject.containsKey("token"));
                        tokens.add(jsonObject.getString("token"));
                    });
                })
                .setChunked(true)
                .end(data.encode());
        Awaitility.await().until(() -> tokens.size() == 1);


        AtomicBoolean check = new AtomicBoolean();
        Vertx.vertx().createHttpClient().get(9000, "localhost",
                                             "/jwt/blacklist")
                .handler(resp -> {
                    testContext.assertEquals(200, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        check.set(true);
                    });
                })
                .putHeader("Authorization", "Bearer " + tokens.get(0))
                .setChunked(true)
                .end();
        Awaitility.await().until(() -> check.get());
    }

}
