package com.edgar.dirwolves.benchmark;

import com.edgar.direwolves.core.apidiscovery.ApiDiscovery;
import com.edgar.direwolves.core.apidiscovery.ApiDiscoveryOptions;
import com.edgar.direwolves.core.apidiscovery.ApiLocalCache;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.verticle.ApiImporter;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2017/7/12.
 *
 * @author Edgar  Date 2017/7/12
 */
@State(Scope.Benchmark)
public class ApiLocalCacheBenchmarks {

  @State(Scope.Benchmark)
  public static class ApiBackend {
    private Vertx vertx;

    private ApiDiscovery apiDiscovery;

    private ApiLocalCache apiLocalCache;
    public ApiBackend() {
      vertx = Vertx.vertx();
      apiDiscovery = ApiDiscovery.create(vertx, new ApiDiscoveryOptions().setName("app"));
      JsonObject app = new JsonObject()
              .put("path", "H:\\csst\\java-core\\trunk\\06SRC\\iotp-app\\router\\api");
      JsonObject om = new JsonObject()
                      .put("path", "H:\\csst\\java-core\\trunk\\06SRC\\iotp-app\\router\\om");
      JsonObject config = new JsonObject()
              .put("router.dir", new JsonObject().put("app", app).put("om", om));
      new ApiImporter().initialize(vertx, config, Future.<Void>future());
      try {
        TimeUnit.SECONDS.sleep(2);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      apiLocalCache = ApiLocalCache.create(vertx, new ApiDiscoveryOptions().setName("app"));
      try {
        TimeUnit.SECONDS.sleep(2);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    public void getDefinitions(String method, String path,
                               Handler<AsyncResult<List<ApiDefinition>>>
            handler) {
      apiLocalCache.getDefinitions(method, path, handler);
    }

    public void close() {
      vertx.close();
    }
  }

  @TearDown(Level.Trial)
  public void tearDown(ApiBackend pool) {
    pool.close();
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Fork(1)
  @OperationsPerInvocation(100)
  public void testApi(ApiBackend backend) {
    final CountDownLatch latch = new CountDownLatch(1);
    backend.getDefinitions("get", "/devices/1", ar -> {
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
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Fork(1)
  @OperationsPerInvocation(100)
  public void testAverage(ApiBackend backend) {
    final CountDownLatch latch = new CountDownLatch(1);
    backend.getDefinitions("get", "/devices/1", ar -> {
      latch.countDown();
    });
    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.SampleTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Fork(1)
  @OperationsPerInvocation(100)
  public void testSampleTime(ApiBackend backend) {
    final CountDownLatch latch = new CountDownLatch(1);
    backend.getDefinitions("get", "/devices/1", ar -> {
      latch.countDown();
    });
    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
