package com.github.edgar615.direwolves.test.endpoint;

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
public class PointToPointTest {

  @Test
  public void testOk(TestContext testContext) {
    AtomicBoolean check = new AtomicBoolean();
    JsonObject data = new JsonObject()
            .put("lat", "1")
            .put("lng", "2");
    Vertx.vertx().createHttpClient().post(9000, "localhost", "/user/location")
            .handler(resp -> {
              testContext.assertEquals(200, resp.statusCode());
              testContext.assertTrue(resp.headers().contains("x-request-id"));
              resp.bodyHandler(body -> {
                System.out.println(body.toString());
                testContext.assertEquals(1,body.toJsonObject().getInteger("result"));
                check.set(true);
              });
            })
            .setChunked(true)
            .end(data.encode());
    Awaitility.await().until(() -> check.get());
  }


}
