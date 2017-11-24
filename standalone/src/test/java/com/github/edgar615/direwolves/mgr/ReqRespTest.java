package com.github.edgar615.direwolves.mgr;

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
public class ReqRespTest {

  @Test
  public void testOk(TestContext testContext) {
    AtomicBoolean check = new AtomicBoolean();
    JsonObject data = new JsonObject()
            .put("username", "1")
            .put("password", "2");
    Vertx.vertx().createHttpClient().post(9000, "localhost", "/user/login")
            .handler(resp -> {
              check.set(true);
              System.out.println(resp.statusCode());
              System.out.println(resp.headers().names());
              resp.bodyHandler(body -> System.out.println(body.toString()));
            })
            .setChunked(true)
            .end(data.encode());
    Awaitility.await().until(() -> check.get());
  }


}
