package com.edgar.direwolves.redis;

import com.edgar.direwolves.core.cache.RedisProvider;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Edgar on 2017/6/23.
 *
 * @author Edgar  Date 2017/6/23
 */
@RunWith(VertxUnitRunner.class)
public class TokenBucketTest {

  private RedisClient redisClient;

  private Vertx vertx;

  RedisProvider redisProvider;
  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    JsonObject config = new JsonObject()
            .put("redis.host","10.11.0.31");
    redisProvider = new RedisProviderFactory().create(vertx, config);
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testBucket3Refill1In2000(TestContext testContext) {
    AtomicInteger req = new AtomicInteger();
    List<JsonObject> result = new ArrayList<>();
    String subject = UUID.randomUUID().toString();
    JsonObject rule = new JsonObject()
            .put("subject", subject)
            .put("burst", 3l)
            .put("refillTime", 2000)
            .put("refillAmount", 1);
    JsonArray rules = new JsonArray();
    rules.add(rule);
    redisProvider.acquireToken(rules, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
        testContext.fail();
      } else {
        req.incrementAndGet();
        result.add(ar.result());
      }
    });
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    redisProvider.acquireToken(rules, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
        testContext.fail();
      } else {
        req.incrementAndGet();
        result.add(ar.result());
      }
    });
    redisProvider.acquireToken(rules, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
        testContext.fail();
      } else {
        req.incrementAndGet();
        result.add(ar.result());
      }
    });
    try {
      TimeUnit.SECONDS.sleep(2);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    redisProvider.acquireToken(rules, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
        testContext.fail();
      } else {
        req.incrementAndGet();
        result.add(ar.result());
      }
    });

    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    redisProvider.acquireToken(rules, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
        testContext.fail();
      } else {
        req.incrementAndGet();
        result.add(ar.result());
      }
    });

    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    redisProvider.acquireToken(rules, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
        testContext.fail();
      } else {
        req.incrementAndGet();
        result.add(ar.result());
      }
    });
    Awaitility.await().until(() -> req.get() == 6);
    System.out.println(result);

    Assert.assertEquals(4, result.stream().filter(resp -> resp.getBoolean("passed")).count());
    Assert.assertEquals(2, result.get(0).getJsonArray("details").getJsonObject(0).getLong
            ("remaining"), 0);
    Assert.assertTrue(result.get(0).getJsonArray("details").getJsonObject(0).getBoolean("passed"));

    Assert.assertEquals(1, result.get(1).getJsonArray("details").getJsonObject(0).getLong
            ("remaining"), 0);
    Assert.assertTrue(result.get(1).getJsonArray("details").getJsonObject(0).getBoolean("passed"));

    Assert.assertEquals(0l, result.get(2).getJsonArray("details").getJsonObject(0).getLong
            ("remaining"), 0);
    Assert.assertTrue(result.get(2).getJsonArray("details").getJsonObject(0).getBoolean("passed"));

    Assert.assertEquals(0l, result.get(3).getJsonArray("details").getJsonObject(0).getLong
            ("remaining"), 0);
    Assert.assertFalse(result.get(3).getJsonArray("details").getJsonObject(0).getBoolean("passed"));

    Assert.assertEquals(0l, result.get(4).getJsonArray("details").getJsonObject(0).getLong
            ("remaining"), 0);
    Assert.assertFalse(result.get(4).getJsonArray("details").getJsonObject(0).getBoolean("passed"));

    Assert.assertEquals(2, result.get(5).getJsonArray("details").getJsonObject(0).getLong
            ("remaining"), 0);
    Assert.assertTrue(result.get(5).getJsonArray("details").getJsonObject(0).getBoolean("passed"));
  }

  @Test
  public void testBucket3Refill3In1000AndBucket5Refill1In2000(TestContext testContext) {
    AtomicInteger req = new AtomicInteger();
    List<JsonObject> result = new ArrayList<>();
    String subject = UUID.randomUUID().toString();
    JsonObject rule1 = new JsonObject()
            .put("subject", subject)
            .put("burst", 3l)
            .put("refillTime", 2000)
            .put("refillAmount", 1);

    JsonObject rule2= new JsonObject()
            .put("subject", UUID.randomUUID().toString())
            .put("burst", 5l)
            .put("refillTime", 2000)
            .put("refillAmount", 1);
    JsonArray rules = new JsonArray();
    rules.add(rule1);
    rules.add(rule2);
    redisProvider.acquireToken(rules, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
        testContext.fail();
      } else {
        req.incrementAndGet();
        result.add(ar.result());
      }
    });
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    redisProvider.acquireToken(rules, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
        testContext.fail();
      } else {
        req.incrementAndGet();
        result.add(ar.result());
      }
    });
    redisProvider.acquireToken(rules, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
        testContext.fail();
      } else {
        req.incrementAndGet();
        result.add(ar.result());
      }
    });
    try {
      TimeUnit.SECONDS.sleep(2);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    redisProvider.acquireToken(rules, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
        testContext.fail();
      } else {
        req.incrementAndGet();
        result.add(ar.result());
      }
    });

    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    redisProvider.acquireToken(rules, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
        testContext.fail();
      } else {
        req.incrementAndGet();
        result.add(ar.result());
      }
    });

    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    redisProvider.acquireToken(rules, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
        testContext.fail();
      } else {
        req.incrementAndGet();
        result.add(ar.result());
      }
    });

    try {
      TimeUnit.MILLISECONDS.sleep(200);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    redisProvider.acquireToken(rules, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
        testContext.fail();
      } else {
        req.incrementAndGet();
        result.add(ar.result());
      }
    });

    try {
      TimeUnit.MILLISECONDS.sleep(200);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    redisProvider.acquireToken(rules, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
        testContext.fail();
      } else {
        req.incrementAndGet();
        result.add(ar.result());
      }
    });

    Awaitility.await().until(() -> req.get() == 8);
    System.out.println(result);

    Assert.assertEquals(5, result.stream().filter(resp -> resp.getBoolean("passed")).count());
    Assert.assertEquals(2, result.get(0).getJsonArray("details").getJsonObject(0).getLong
            ("remaining"), 0);
    Assert.assertTrue(result.get(0).getJsonArray("details").getJsonObject(0).getBoolean("passed"));
    Assert.assertEquals(4, result.get(0).getJsonArray("details").getJsonObject(1).getLong
            ("remaining"), 0);
    Assert.assertTrue(result.get(0).getJsonArray("details").getJsonObject(1).getBoolean("passed"));

    Assert.assertEquals(1, result.get(1).getJsonArray("details").getJsonObject(0).getLong
            ("remaining"), 0);
    Assert.assertTrue(result.get(1).getJsonArray("details").getJsonObject(0).getBoolean("passed"));
    Assert.assertEquals(3, result.get(1).getJsonArray("details").getJsonObject(1).getLong
            ("remaining"), 0);
    Assert.assertTrue(result.get(1).getJsonArray("details").getJsonObject(1).getBoolean("passed"));

    Assert.assertEquals(0l, result.get(2).getJsonArray("details").getJsonObject(0).getLong
            ("remaining"), 0);
    Assert.assertTrue(result.get(2).getJsonArray("details").getJsonObject(0).getBoolean("passed"));
    Assert.assertEquals(2, result.get(2).getJsonArray("details").getJsonObject(1).getLong
            ("remaining"), 0);
    Assert.assertTrue(result.get(2).getJsonArray("details").getJsonObject(1).getBoolean("passed"));

    Assert.assertEquals(0l, result.get(3).getJsonArray("details").getJsonObject(0).getLong
            ("remaining"), 0);
    Assert.assertFalse(result.get(3).getJsonArray("details").getJsonObject(0).getBoolean("passed"));
    Assert.assertEquals(2, result.get(3).getJsonArray("details").getJsonObject(1).getLong
            ("remaining"), 0);
    Assert.assertTrue(result.get(3).getJsonArray("details").getJsonObject(1).getBoolean("passed"));

    Assert.assertEquals(0l, result.get(4).getJsonArray("details").getJsonObject(0).getLong
            ("remaining"), 0);
    Assert.assertFalse(result.get(4).getJsonArray("details").getJsonObject(0).getBoolean("passed"));
    Assert.assertEquals(2, result.get(4).getJsonArray("details").getJsonObject(1).getLong
            ("remaining"), 0);
    Assert.assertTrue(result.get(4).getJsonArray("details").getJsonObject(1).getBoolean("passed"));

    Assert.assertEquals(2, result.get(5).getJsonArray("details").getJsonObject(0).getLong
            ("remaining"), 0);
    Assert.assertTrue(result.get(5).getJsonArray("details").getJsonObject(0).getBoolean("passed"));
    Assert.assertEquals(1, result.get(5).getJsonArray("details").getJsonObject(1).getLong
            ("remaining"), 0);
    Assert.assertTrue(result.get(5).getJsonArray("details").getJsonObject(1).getBoolean("passed"));

    Assert.assertEquals(1, result.get(6).getJsonArray("details").getJsonObject(0).getLong
            ("remaining"), 0);
    Assert.assertTrue(result.get(6).getJsonArray("details").getJsonObject(0).getBoolean("passed"));
    Assert.assertEquals(0, result.get(6).getJsonArray("details").getJsonObject(1).getLong
            ("remaining"), 0);
    Assert.assertTrue(result.get(6).getJsonArray("details").getJsonObject(1).getBoolean("passed"));

    Assert.assertEquals(1, result.get(7).getJsonArray("details").getJsonObject(0).getLong
            ("remaining"), 0);
    Assert.assertTrue(result.get(7).getJsonArray("details").getJsonObject(0).getBoolean("passed"));
    Assert.assertEquals(0, result.get(7).getJsonArray("details").getJsonObject(1).getLong
            ("remaining"), 0);
    Assert.assertFalse(result.get(7).getJsonArray("details").getJsonObject(1).getBoolean("passed"));
  }
}
