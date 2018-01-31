package com.github.edgar615.direwolves.test.arg;

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
public class UrlArgTest {

  @Test
  public void missArgShouldThrowInvalidArg(TestContext testContext) {
    AtomicBoolean check = new AtomicBoolean();
    Vertx.vertx().createHttpClient().get(9000, "localhost", "/arg/url")
            .handler(resp -> {
              testContext.assertEquals(400, resp.statusCode());
              testContext.assertTrue(resp.headers().contains("x-request-id"));
              resp.bodyHandler(body -> {
                System.out.println(body.toString());
                testContext.assertEquals(DefaultErrorCode.INVALID_ARGS.getNumber(),
                                         body.toJsonObject().getInteger("code"));
                JsonObject details = body.toJsonObject().getJsonObject("details", new JsonObject());
                testContext.assertEquals(1, details.size());
                testContext.assertTrue(details.containsKey("deviceType"));
                check.set(true);
              });
            })
            .setChunked(true)
            .end();
    Awaitility.await().until(() -> check.get());
  }

  @Test
  public void testSuccess(TestContext testContext) {
    AtomicBoolean check = new AtomicBoolean();
    String url = "/arg/url?deviceType=1&start=5";
    Vertx.vertx().createHttpClient().get(9000, "localhost", url)
            .handler(resp -> {
              testContext.assertEquals(200, resp.statusCode());
              testContext.assertTrue(resp.headers().contains("x-request-id"));
              resp.bodyHandler(body -> {
                System.out.println(body.toString());
                JsonObject jsonObject = body.toJsonObject();
                testContext.assertEquals(5, jsonObject.getValue("start"));
                testContext.assertEquals(10, jsonObject.getValue("limit"));
                check.set(true);
              });
            })
            .setChunked(true)
            .end();
    Awaitility.await().until(() -> check.get());
  }

}
