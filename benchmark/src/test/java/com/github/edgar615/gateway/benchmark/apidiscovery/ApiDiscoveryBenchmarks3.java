package com.github.edgar615.gateway.benchmark.apidiscovery;

import com.github.edgar615.gateway.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.gateway.core.apidiscovery.ApiDiscoveryOptions;
import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.verticle.FileApiDiscoveryVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2017/7/12.
 *
 * @author Edgar  Date 2017/7/12
 */
@State(Scope.Benchmark)
public class ApiDiscoveryBenchmarks3 {

  @TearDown(Level.Trial)
  public void tearDown(ApiBackend pool) {
    pool.close();
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Fork(1)
  @OperationsPerInvocation(10000)
  public void testApi(ApiBackend pool) {
    final CountDownLatch latch = new CountDownLatch(1);
    pool.getDefinition("device.get.1.0.0", ar -> {
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
  public void testAverage(ApiBackend backend) {
    final CountDownLatch latch = new CountDownLatch(1);
    backend.getDefinition("device.get.1.0.0", ar -> {
      latch.countDown();
    });
    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

//  @Benchmark
//  @BenchmarkMode(Mode.SampleTime)
//  @OutputTimeUnit(TimeUnit.NANOSECONDS)
//  @Fork(1)
//  @OperationsPerInvocation(10000)
//  public void testSampleTime(ApiBackend backend) {
//    final CountDownLatch latch = new CountDownLatch(1);
//    backend.getDefinition("device.get.1.0.0", ar -> {
//      latch.countDown();
//    });
//    try {
//      latch.await();
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }
//  }

  @State(Scope.Benchmark)
  public static class ApiBackend {
    private Vertx vertx;

    private ApiDiscovery apiDiscovery;

    public ApiBackend() {
      vertx = Vertx.vertx();
      apiDiscovery = ApiDiscovery.create(vertx, new ApiDiscoveryOptions().setName("app"));
      JsonObject apiConfig = new JsonObject()
              .put("name", "app");
      JsonObject config = new JsonObject()
              .put("api.discovery", apiConfig)
              .put("file", "H:\\csst\\java-core\\trunk\\06SRC\\iotp-app\\router\\api");
      vertx.deployVerticle(FileApiDiscoveryVerticle.class,
                           new DeploymentOptions().setConfig(config),
                           Future.future());
      try {
        TimeUnit.SECONDS.sleep(3);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    public void getDefinition(String name,
                              Handler<AsyncResult<ApiDefinition>>
                                      handler) {
      apiDiscovery.getDefinition(name, handler);
    }

    public void close() {
      vertx.close();
    }
  }
}
