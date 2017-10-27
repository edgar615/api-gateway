package com.github.edgar615.dirwolves.benchmark.appkey;

import com.github.edgar615.direwolves.plugin.appkey.discovery.AppKey;
import com.github.edgar615.direwolves.plugin.appkey.discovery.AppKeyDiscovery;
import com.github.edgar615.util.base.Randoms;
import com.github.edgar615.util.vertx.cache.Cache;
import com.github.edgar615.util.vertx.cache.CacheLoader;
import com.github.edgar615.util.vertx.cache.GuavaCache;
import com.github.edgar615.util.vertx.cache.GuavaCacheOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.openjdk.jmh.annotations.*;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by edgar on 17-7-25.
 */
@State(Scope.Benchmark)
public class AppKeyCache2Benchmarks {

  @TearDown(Level.Trial)
  public void tearDown(AppKeyBackend pool) {
    pool.close();
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Fork(1)
  @OperationsPerInvocation(10000)
  public void testApi(AppKeyBackend pool) {
    final CountDownLatch latch = new CountDownLatch(1);
    pool.getAppKey("appkey_" + Randoms.randomNumber(3), ar -> {
      latch.countDown();
    });
    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Fork(1)
  @OperationsPerInvocation(10000)
  public void testAverage(AppKeyBackend backend) {
    final CountDownLatch latch = new CountDownLatch(1);
    backend.getAppKey("appkey_" + Randoms.randomNumber(3), ar -> {
      latch.countDown();
    });
    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @State(Scope.Benchmark)
  public static class AppKeyBackend {
    private Vertx vertx;

    private Cache<String, JsonObject> cache;

    private CacheLoader<String, JsonObject> cacheLoader;

    public AppKeyBackend() {
      vertx = Vertx.vertx();
      AppKeyDiscovery discovery = AppKeyDiscovery.create(vertx, "api");
      this.cache = new GuavaCache<>(vertx, new GuavaCacheOptions()
              .setExpireAfterWrite(1800l));
      for (int i = 0; i < 1000; i++) {
        String appKey = "appkey_" + i;
        JsonObject jsonObject = new JsonObject()
                .put("appKey", appKey)
                .put("appSecret", UUID.randomUUID().toString());
        discovery.publish(new AppKey(appKey, jsonObject), ar -> {

        });
      }
      cacheLoader = (key, handler) -> discovery.getAppKey(key, ar -> {
        if (ar.failed() || ar.result() == null) {
          handler.handle(Future.succeededFuture(null));
          return;
        }
        handler.handle(Future.succeededFuture(ar.result().getJsonObject()));
      });
      try {
        TimeUnit.SECONDS.sleep(3);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    public void getAppKey(String appkey,
                          Handler<AsyncResult<JsonObject>>
                                  handler) {
      cache.get(appkey, handler);
    }

    public void close() {
      vertx.close();
    }
  }

}
