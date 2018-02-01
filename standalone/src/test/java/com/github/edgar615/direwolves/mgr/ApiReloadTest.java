package com.github.edgar615.direwolves.mgr;

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
public class ApiReloadTest {

  @Test
  public void testOk(TestContext testContext) {
    AtomicBoolean check = new AtomicBoolean();
    Vertx.vertx().createHttpClient().get(9000, "localhost", "/api/reload")
            .handler(resp -> {
              check.set(true);
              System.out.println(resp.statusCode());
              System.out.println(resp.headers().names());
              resp.bodyHandler(body -> System.out.println(body.toString()));
            })
            .setChunked(true)
            .end();
    Awaitility.await().until(() -> check.get());
  }


}
