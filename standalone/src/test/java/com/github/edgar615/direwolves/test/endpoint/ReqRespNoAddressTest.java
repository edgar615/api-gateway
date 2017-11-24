package com.github.edgar615.direwolves.test.endpoint;

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
public class ReqRespNoAddressTest {

  @Test
  public void testOk(TestContext testContext) {
    AtomicBoolean check = new AtomicBoolean();
    JsonObject data = new JsonObject()
            .put("username", "1")
            .put("password", "2");
    Vertx.vertx().createHttpClient().post(9000, "localhost", "/user/login-error")
            .handler(resp -> {
              testContext.assertEquals(503, resp.statusCode());
              testContext.assertTrue(resp.headers().contains("x-request-id"));
              resp.bodyHandler(body -> {
                System.out.println(body.toString());
                testContext.assertEquals(DefaultErrorCode.SERVICE_UNAVAILABLE.getNumber(),
                                         body.toJsonObject().getInteger("code"));
                check.set(true);
              });
            })
            .setChunked(true)
            .end(data.encode());
    Awaitility.await().until(() -> check.get());
  }


}
