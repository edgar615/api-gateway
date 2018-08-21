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
public class AppKeyRestrictTest {

    @Test
    public void testBlacklistPluginForbidden(TestContext testContext) {
        String query = new Api()
                .setAppKey("FSG1NLKJqM4UKBsboS2j")
                .setAppSecret("7c102b815d24489eb460a026691a1440")
                .addParam("id", Randoms.randomNumber(5))
                .signTopRequest();
        AtomicBoolean check = new AtomicBoolean();
        Vertx.vertx().createHttpClient().get(9000, "localhost",
                                             "/appkey/blacklist?" + query)
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
                .setChunked(true)
                .end();
        Awaitility.await().until(() -> check.get());
    }

    @Test
    public void testBlacklistPluginPass(TestContext testContext) {
        String query = new Api()
                .setAppKey("EUL6mX7s383HS4SJGkGd")
                .setAppSecret("f7c3fe9887034bdd88a8f4399b0a6788")
                .addParam("id", Randoms.randomNumber(5))
                .signTopRequest();
        AtomicBoolean check = new AtomicBoolean();
        Vertx.vertx().createHttpClient().get(9000, "localhost",
                                             "/appkey/blacklist?" + query)
                .handler(resp -> {
                    testContext.assertEquals(200, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        check.set(true);
                    });
                })
                .setChunked(true)
                .end();
        Awaitility.await().until(() -> check.get());
    }

    @Test
    public void testWhitelistPluginPass(TestContext testContext) {
        String query = new Api()
                .setAppKey("RmOI7jCvDtfZ1RcAkea1")
                .setAppSecret("dbb0f95c8ebf4317942d9f5057d0b38e")
                .addParam("id", Randoms.randomNumber(5))
                .signTopRequest();
        AtomicBoolean check = new AtomicBoolean();
        Vertx.vertx().createHttpClient().get(9000, "localhost",
                                             "/appkey/blacklist?" + query)
                .handler(resp -> {
//              testContext.assertEquals(200, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        check.set(true);
                    });
                })
                .setChunked(true)
                .end();
        Awaitility.await().until(() -> check.get());
    }

    @Test
    public void testGlobalBlacklistForbidden(TestContext testContext) {
        String query = new Api()
                .setAppKey("YM2ILRYlK5GUpheJh63K")
                .setAppSecret("6b9caaad457243fdb5ea5e5444ea3709")
                .addParam("id", Randoms.randomNumber(5))
                .signTopRequest();
        AtomicBoolean check = new AtomicBoolean();
        Vertx.vertx().createHttpClient().get(9000, "localhost",
                                             "/appkey/blacklist?" + query)
                .handler(resp -> {
                    testContext.assertEquals(403, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        JsonObject jsonObject = body.toJsonObject();
                        testContext.assertEquals(DefaultErrorCode.PERMISSION_DENIED.getNumber(),
                                                 body.toJsonObject().getInteger("code"));
                        check.set(true);
                    });
                })
                .setChunked(true)
                .end();
        Awaitility.await().until(() -> check.get());
    }

    @Test
    public void testGlobalWhitelistPluginPass(TestContext testContext) {
        String query = new Api()
                .setAppKey("AEA3C65BFFCDC720AF5")
                .setAppSecret("0bee264bf88642708e653ab282f55074")
                .addParam("id", Randoms.randomNumber(5))
                .signTopRequest();
        AtomicBoolean check = new AtomicBoolean();
        Vertx.vertx().createHttpClient().get(9000, "localhost",
                                             "/appkey/blacklist?" + query)
                .handler(resp -> {
                    testContext.assertEquals(200, resp.statusCode());
                    testContext.assertTrue(resp.headers().contains("x-request-id"));
                    resp.bodyHandler(body -> {
                        System.out.println(body.toString());
                        check.set(true);
                    });
                })
                .setChunked(true)
                .end();
        Awaitility.await().until(() -> check.get());
    }
}
