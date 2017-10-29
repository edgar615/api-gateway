package com.github.edgar615.direwolves.redis;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/9/5.
 *
 * @author Edgar  Date 2017/9/5
 */
@RunWith(VertxUnitRunner.class)
public class RedisCache2Test {

  private Vertx vertx;

  private Cache cache;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    JsonObject config = new JsonObject()
            .put("host", "test.ihorn.com.cn")
            .put("port", 32770)
            .put("auth", "7CBF5FBEF855F16F");

    AtomicBoolean check = new AtomicBoolean();
    vertx.deployVerticle(RedisVerticle.class.getName(), new DeploymentOptions()
            .setConfig(new JsonObject().put("redis", config)), ar -> {
      cache = new RedisCacheFactory()
              .create(vertx, "test", new JsonObject().put("expireAfterWrite", 3));
      check.set(true);
    });

    Awaitility.await().until(() -> check.get());

  }

  @Test
  public void testNotExists(TestContext testContext) {
    String key = UUID.randomUUID().toString();
    Async async = testContext.async();
    cache.get(key, ar -> {
      if (ar.succeeded()) {
        testContext.assertTrue(ar.result().isEmpty());
        async.complete();
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
  }

  @Test
  public void testPut(TestContext testContext) {
    String key = UUID.randomUUID().toString();
    AtomicBoolean check1 = new AtomicBoolean();
    cache.get(key, ar -> {
      if (ar.succeeded()) {
        testContext.assertTrue(ar.result().isEmpty());
        check1.set(true);
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> check1.get());

    AtomicBoolean check2 = new AtomicBoolean();
    cache.put(key, new JsonObject().put("val", key), ar -> {
      if (ar.succeeded()) {
        check2.set(true);
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> check2.get());

    AtomicBoolean check3 = new AtomicBoolean();
    cache.get(key, ar -> {
      if (ar.succeeded()) {
        testContext.assertEquals(key, ar.result().getString("val"));
        check3.set(true);
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> check3.get());
  }

  @Test
  public void testExpire(TestContext testContext) {
    String key = UUID.randomUUID().toString();
    AtomicBoolean check1 = new AtomicBoolean();
    cache.get(key, ar -> {
      if (ar.succeeded()) {
        testContext.assertTrue(ar.result().isEmpty());
        check1.set(true);
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> check1.get());

    AtomicBoolean check2 = new AtomicBoolean();
    cache.put(key, new JsonObject().put("val", key), ar -> {
      if (ar.succeeded()) {
        check2.set(true);
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> check2.get());

    try {
      TimeUnit.SECONDS.sleep(5);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    AtomicBoolean check3 = new AtomicBoolean();
    cache.get(key, ar -> {
      if (ar.succeeded()) {
        testContext.assertTrue(ar.result().isEmpty());
        check3.set(true);
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
    Awaitility.await().until(() -> check3.get());
  }
}
