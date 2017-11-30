package com.github.edgar615.direwolves.test.authentication;

import com.github.edgar615.direwolves.standalone.Api;
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
public class JwtTest {

  @Test
  public void testNoUserNoToken(TestContext testContext) {
    JsonObject data = new JsonObject()
            .put("userId", Randoms.randomNumber(5))
            .put("foo", "bar");
    AtomicBoolean check = new AtomicBoolean();
    Vertx.vertx().createHttpClient().post(9000, "localhost",
                                         "/notoken")
            .handler(resp -> {
//              testContext.assertEquals(200, resp.statusCode());
              testContext.assertTrue(resp.headers().contains("x-request-id"));
              resp.bodyHandler(body -> {
                System.out.println(body.toString());
                JsonObject jsonObject = body.toJsonObject();
                testContext.assertFalse(jsonObject.containsKey("token"));
                check.set(true);
              });
            })
            .setChunked(true)
            .end(data.encode());
    Awaitility.await().until(() -> check.get());
  }

  @Test
  public void testCreateToken(TestContext testContext) {
    JsonObject data = new JsonObject()
            .put("userId", Randoms.randomNumber(5))
            .put("foo", "bar");
    AtomicBoolean check = new AtomicBoolean();
    Vertx.vertx().createHttpClient().post(9000, "localhost",
                                          "/token")
            .handler(resp -> {
//              testContext.assertEquals(200, resp.statusCode());
              testContext.assertTrue(resp.headers().contains("x-request-id"));
              resp.bodyHandler(body -> {
                System.out.println(body.toString());
                JsonObject jsonObject = body.toJsonObject();
                testContext.assertTrue(jsonObject.containsKey("token"));
                check.set(true);
              });
            })
            .setChunked(true)
            .end(data.encode());
    Awaitility.await().until(() -> check.get());
  }

  @Test
  public void missTokenShouldThrowMissArg(TestContext testContext) {
    AtomicBoolean check = new AtomicBoolean();
    Vertx.vertx().createHttpClient().get(9000, "localhost",
                                         "/jwt")
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
}
