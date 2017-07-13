package com.edgar.dirwolves.benchmark;

import com.edgar.direwolves.core.apidiscovery.ApiDiscoveryOptions;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.apidiscovery.ApiDiscovery;
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
public class ApiDiscoveryBenchmarks2 {

  @State(Scope.Benchmark)
  public static class ApiBackend {
    private Vertx vertx;

    private ApiDiscovery apiDiscovery;

    public ApiBackend() {
      vertx = Vertx.vertx();
      apiDiscovery = ApiDiscovery.create(vertx, new ApiDiscoveryOptions().setName("app"));
      JsonObject app = new JsonObject()
              .put("path", "H:\\csst\\java-core\\trunk\\06SRC\\iotp-app\\router\\api\\device");
      JsonObject config = new JsonObject()
              .put("router.dir", new JsonObject().put("app", app));
      new ApiImporter().initialize(vertx, config, Future.<Void>future());
      try {
        TimeUnit.SECONDS.sleep(3);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    public void getDefinitions(JsonObject jsonObject,
                               Handler<AsyncResult<List<ApiDefinition>>>
            handler) {
      apiDiscovery.getDefinitions(jsonObject, handler);
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
  public void testApi(ApiBackend pool) {
    final CountDownLatch latch = new CountDownLatch(1);
    pool.getDefinitions(new JsonObject().put("method", "GET").put("path", "/devices/1"), ar -> {
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
    backend.getDefinitions(new JsonObject().put("method", "GET").put("path", "/devices/1"), ar -> {
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
    backend.getDefinitions(new JsonObject().put("method", "GET").put("path", "/devices/1"), ar -> {
      latch.countDown();
    });
    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
