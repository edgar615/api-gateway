package com.github.edgar615.gateway.redis;

import com.github.edgar615.util.vertx.cache.Cache;
import com.github.edgar615.util.vertx.cache.CacheOptions;
import com.github.edgar615.util.vertx.redis.RedisClientHelper;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/9/5.
 *
 * @author Edgar  Date 2017/9/5
 */
@RunWith(VertxUnitRunner.class)
public class RedisCacheTest {

    private Vertx vertx;

    private Cache<String, JsonObject> cache;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        JsonObject config = new JsonObject()
                .put("host", "127.0.0.1")
                .put("port", 6379)
                .put("auth", "yangzp");

        AtomicBoolean check = new AtomicBoolean();
        vertx.deployVerticle(RedisVerticle.class.getName(), new DeploymentOptions()
                .setConfig(new JsonObject().put("redis", config)), ar -> {
            cache = new RedisCache(RedisClientHelper.getShared(vertx), "test",
                                   new CacheOptions(new JsonObject().put("expireAfterWrite", 5)));
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
                testContext.assertNull(ar.result());
                async.complete();
            } else {
                ar.cause().printStackTrace();
                testContext.fail();
            }
        });
    }

    @Test
    public void testGetLoad(TestContext testContext) {
        String key = UUID.randomUUID().toString();
        AtomicBoolean check1 = new AtomicBoolean();
        cache.get(key, ar -> {
            if (ar.succeeded()) {
                testContext.assertNull(ar.result());
                check1.set(true);
            } else {
                ar.cause().printStackTrace();
                testContext.fail();
            }
        });
        Awaitility.await().until(() -> check1.get());

        AtomicBoolean check2 = new AtomicBoolean();
        cache.get(key, (key1, asyncResultHandler) -> asyncResultHandler
                .handle(Future.succeededFuture(new JsonObject().put("val", key1))), ar -> {
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
    public void testGetLoadFailed(TestContext testContext) {
        String key = UUID.randomUUID().toString();
        AtomicBoolean check1 = new AtomicBoolean();
        cache.get(key, ar -> {
            if (ar.succeeded()) {
                testContext.assertNull(ar.result());
                check1.set(true);
            } else {
                ar.cause().printStackTrace();
                testContext.fail();
            }
        });
        Awaitility.await().until(() -> check1.get());

        AtomicBoolean check2 = new AtomicBoolean();
        cache.get(key, (key1, asyncResultHandler) -> asyncResultHandler
                .handle(Future.failedFuture("error")), ar -> {
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
                testContext.assertNull(ar.result());
                check3.set(true);
            } else {
                ar.cause().printStackTrace();
                testContext.fail();
            }
        });
        Awaitility.await().until(() -> check3.get());
    }

    @Test
    public void testPut(TestContext testContext) {
        String key = UUID.randomUUID().toString();
        AtomicBoolean check1 = new AtomicBoolean();
        cache.get(key, ar -> {
            if (ar.succeeded()) {
                testContext.assertNull(ar.result());
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

        AtomicBoolean check4 = new AtomicBoolean();
        cache.evict(key, ar -> {
            if (ar.succeeded()) {
                check4.set(true);
            } else {
                ar.cause().printStackTrace();
                testContext.fail();
            }
        });
        Awaitility.await().until(() -> check4.get());

        AtomicBoolean check5 = new AtomicBoolean();
        cache.get(key, ar -> {
            if (ar.succeeded()) {
                testContext.assertNull(ar.result());
                check5.set(true);
            } else {
                ar.cause().printStackTrace();
                testContext.fail();
            }
        });
        Awaitility.await().until(() -> check5.get());
    }
}
