package com.edgar.dirwolves.benchmark.appkey;

import com.edgar.direwolves.plugin.appkey.discovery.AppKey;
import com.edgar.direwolves.plugin.appkey.discovery.AppKeyDiscovery;
import com.edgar.direwolves.plugin.appkey.discovery.AppKeyLocalCache;
import com.edgar.util.base.Randoms;
import io.vertx.core.AsyncResult;
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
public class AppKeyCacheBenchmarks {

  @State(Scope.Benchmark)
  public static class AppKeyBackend {
    private Vertx vertx;

    private AppKeyLocalCache cache;

    public AppKeyBackend() {
      vertx = Vertx.vertx();
      AppKeyDiscovery discovery = AppKeyDiscovery.create(vertx, "api");
      cache = AppKeyLocalCache.create(vertx, discovery);
      for (int i = 0; i < 1000; i ++) {
        String appKey = "appkey_" + i;
        JsonObject jsonObject = new JsonObject()
            .put("appKey", appKey)
            .put("appSecret", UUID.randomUUID().toString());
        discovery.publish(new AppKey(appKey, jsonObject), ar -> {

        });
      }
      try {
        TimeUnit.SECONDS.sleep(3);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    public void getAppKey(String appkey,
                               Handler<AsyncResult<AppKey>>
                                   handler) {
      cache.getAppKey(appkey, handler);
    }

    public void close() {
      vertx.close();
    }
  }

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

}
