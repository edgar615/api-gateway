package com.github.edgar615.gateway.test.appkey;

import com.github.edgar615.gateway.standalone.Api;
import com.github.edgar615.util.base.Randoms;
import com.github.edgar615.util.exception.DefaultErrorCode;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
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
public class AppKeyTest {

    @Test
    public void missArgShouldThrowInvalidArg(TestContext testContext) {
        AtomicBoolean check = new AtomicBoolean();
        Vertx.vertx().createHttpClient().get(9000, "localhost", "/appkey")
                .handler(resp -> {
                    testContext.assertEquals(400, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        testContext.assertEquals(DefaultErrorCode.INVALID_ARGS.getNumber(),
                                                 body.toJsonObject().getInteger("code"));
                        JsonObject details =
                                body.toJsonObject().getJsonObject("details", new JsonObject());
                        testContext.assertEquals(4, details.size());
                        testContext.assertTrue(details.containsKey("sign"));
                        testContext.assertTrue(details.containsKey("appKey"));
                        testContext.assertTrue(details.containsKey("nonce"));
                        testContext.assertTrue(details.containsKey("signMethod"));
                        check.set(true);
                    });
                })
                .setChunked(true)
                .end();
        Awaitility.await().until(() -> check.get());
    }

    @Test
    public void invalidSignMethodShouldThrowInvalidArg(TestContext testContext) {
        AtomicBoolean check = new AtomicBoolean();
        Vertx.vertx().createHttpClient().get(9000, "localhost",
                                             "/appkey?appKey=1&nonce=2&sign=3&signMethod=4")
                .handler(resp -> {
                    testContext.assertEquals(400, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        testContext.assertEquals(DefaultErrorCode.INVALID_ARGS.getNumber(),
                                                 body.toJsonObject().getInteger("code"));
                        JsonObject details =
                                body.toJsonObject().getJsonObject("details", new JsonObject());
                        testContext.assertEquals(1, details.size());
                        testContext.assertTrue(details.containsKey("signMethod"));
                        check.set(true);
                    });
                })
                .setChunked(true)
                .end();
        Awaitility.await().until(() -> check.get());
    }

    @Test
    public void invalidAppKeyShouldThrowInvalidArg(TestContext testContext) {
        AtomicBoolean check = new AtomicBoolean();
        Vertx.vertx().createHttpClient().get(9000, "localhost",
                                             "/appkey?appKey=1&nonce=2&sign=3&signMethod=HMACMD5")
                .handler(resp -> {
                    testContext.assertEquals(400, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        testContext.assertEquals(DefaultErrorCode.INVALID_REQ.getNumber(),
                                                 body.toJsonObject().getInteger("code"));
                        check.set(true);
                    });
                })
                .setChunked(true)
                .end();
        Awaitility.await().until(() -> check.get());
    }

    @Test
    public void invalidSignShouldThrowInvalidArg(TestContext testContext) {
        AtomicBoolean check = new AtomicBoolean();
        Vertx.vertx().createHttpClient().get(9000, "localhost",
                                             "/appkey?appKey=RmOI7jCvDtfZ1RcAkea1&nonce=2&sign=3&signMethod=HMACMD5")
                .handler(resp -> {
                    testContext.assertEquals(400, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        testContext.assertEquals(DefaultErrorCode.INVALID_REQ.getNumber(),
                                                 body.toJsonObject().getInteger("code"));
                        check.set(true);
                    });
                })
                .setChunked(true)
                .end();
        Awaitility.await().until(() -> check.get());
    }

    @Test
    public void testSignSuccess(TestContext testContext) {
        String query = new Api()
                .setAppKey("RmOI7jCvDtfZ1RcAkea1")
                .setAppSecret("dbb0f95c8ebf4317942d9f5057d0b38e")
                .addParam("id", Randoms.randomNumber(5))
                .signTopRequest();
        AtomicBoolean check = new AtomicBoolean();
        Vertx.vertx().createHttpClient().get(9000, "localhost",
                                             "/appkey?" + query)
                .handler(resp -> {
                    testContext.assertEquals(200, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        JsonObject jsonObject = body.toJsonObject();
                        testContext.assertEquals("RmOI7jCvDtfZ1RcAkea1",
                                                 jsonObject.getString("appKey"));
                        testContext.assertEquals(0, jsonObject.getValue("appId"));
//                testContext.assertTrue(details.containsKey("signMethod"));
                        check.set(true);
                    });
                })
                .setChunked(true)
                .end();
        Awaitility.await().until(() -> check.get());
    }

    @Test
    public void testSignWithBodySuccess(TestContext testContext) {
        String query = new Api()
                .setAppKey("RmOI7jCvDtfZ1RcAkea1")
                .setAppSecret("dbb0f95c8ebf4317942d9f5057d0b38e")
                .addParam("id", Randoms.randomNumber(5))
                .addBody("foo", "bar")
                .addBody("bar", "foo")
                .signTopRequest();
        AtomicBoolean check = new AtomicBoolean();
        Vertx.vertx().createHttpClient().post(9000, "localhost",
                                              "/appkey?" + query)
                .handler(resp -> {
//              testContext.assertEquals(200, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        JsonObject jsonObject = body.toJsonObject();
                        testContext.assertEquals("RmOI7jCvDtfZ1RcAkea1",
                                                 jsonObject.getString("appKey"));
                        testContext.assertEquals(0, jsonObject.getValue("appId"));
//                testContext.assertTrue(details.containsKey("signMethod"));
                        check.set(true);
                    });
                })
                .setChunked(true)
                .end(new JsonObject().put("foo", "bar").put("bar", "foo").encode());
        Awaitility.await().until(() -> check.get());
    }

    @Test
    public void testLoadAppKeySuccess(TestContext testContext) {
        String query = new Api()
                .setAppKey("pyuywmyijucuzlfkhxvs")
                .setAppSecret("5416cc11b35d403bba9505a05954517a")
                .addParam("id", Randoms.randomNumber(5))
                .signTopRequest();
        AtomicBoolean check = new AtomicBoolean();
        Vertx.vertx().createHttpClient().get(9000, "localhost",
                                             "/appkey?" + query)
                .handler(resp -> {
                    testContext.assertEquals(200, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        JsonObject jsonObject = body.toJsonObject();
                        testContext.assertEquals("pyuywmyijucuzlfkhxvs",
                                                 jsonObject.getString("appKey"));
                        testContext.assertEquals(100, jsonObject.getValue("appId"));
//                testContext.assertTrue(details.containsKey("signMethod"));
                        check.set(true);
                    });
                })
                .setChunked(true)
                .end();
        Awaitility.await().until(() -> check.get());
    }
}
