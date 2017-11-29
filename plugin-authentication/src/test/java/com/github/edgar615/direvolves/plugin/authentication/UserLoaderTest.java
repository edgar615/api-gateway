package com.github.edgar615.direvolves.plugin.authentication;

import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.util.base.Randoms;
import com.github.edgar615.util.vertx.cache.Cache;
import com.github.edgar615.util.vertx.cache.CacheOptions;
import com.github.edgar615.util.vertx.cache.GuavaCache;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by edgar on 16-10-31.
 */
@RunWith(VertxUnitRunner.class)
public class UserLoaderTest {

  String id = UUID.randomUUID().toString();

  private Vertx vertx;

  int port = Integer.parseInt(Randoms.randomNumber(4));

  @Before
  public void setUp(TestContext testContext) {
    vertx = Vertx.vertx();
    AtomicBoolean completed = new AtomicBoolean();

    vertx.createHttpServer().requestHandler(req -> {
      String userId = req.getParam("userId");
      if (id.equalsIgnoreCase(userId)) {
        JsonObject jsonObject = new JsonObject()
                .put("userId", userId)
                .put("username", "edgar615");
        req.response().end(jsonObject.encode());
      } else {
        req.response().setStatusCode(404)
                .end();
      }

    }).listen(port, ar -> {
      if (ar.succeeded()) {
        completed.set(true);
      } else {
        ar.cause().printStackTrace();
      }
    });

    Awaitility.await().until(() -> completed.get());

  }

  @Test
  public void testLoadSuccess(TestContext testContext) {

    String notExists = UUID.randomUUID().toString();
    JsonObject config = new JsonObject()
            .put("notExistsKey", notExists)
            .put("port", port)
            .put("url", "/test");
    String prefix = UUID.randomUUID().toString() + ":";
    UserLoader userLoader = new UserLoader(vertx, prefix, config);
    Cache<String, JsonObject> cache = new GuavaCache<>(vertx, "userCache", new CacheOptions());
    AtomicBoolean completed = new AtomicBoolean();
    cache.get(prefix + id, userLoader, ar -> {
      if (ar.succeeded()) {
        System.out.println(ar.result());
        testContext.assertFalse(ar.result().containsKey(notExists));
        testContext.assertEquals("edgar615", ar.result().getString("username"));
        completed.set(true);
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> completed.get());
  }

  @Test
  public void testLoadFaild(TestContext testContext) {

    String notExists = UUID.randomUUID().toString();
    JsonObject config = new JsonObject()
            .put("notExistsKey", notExists)
            .put("port", port)
            .put("url", "/test");
    String prefix = UUID.randomUUID().toString() + ":";
    String random = UUID.randomUUID().toString();
    UserLoader userLoader = new UserLoader(vertx, prefix, config);
    Cache<String, JsonObject> cache = new GuavaCache<>(vertx, "userCache", new CacheOptions());
    AtomicBoolean completed = new AtomicBoolean();
    cache.get(prefix + random, userLoader, ar -> {
      if (ar.succeeded()) {
        System.out.println(ar.result());
        testContext.assertTrue(ar.result().containsKey(notExists));
        completed.set(true);
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> completed.get());
  }
}
