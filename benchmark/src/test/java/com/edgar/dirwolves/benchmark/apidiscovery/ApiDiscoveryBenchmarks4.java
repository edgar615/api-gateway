package com.edgar.dirwolves.benchmark.apidiscovery;

import com.edgar.direwolves.core.apidiscovery.ApiDiscoveryOptions;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.apidiscovery.ApiDiscovery;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.direwolves.verticle.ImportApi;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
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
public class ApiDiscoveryBenchmarks4 {

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
    pool.getDefinitions(ar -> {
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
    backend.getDefinitions(ar -> {
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
//  @OutputTimeUnit(TimeUnit.MILLISECONDS)
//  @Fork(1)
//  @OperationsPerInvocation(10000)
//  public void testSampleTime(ApiBackend backend) {
//    final CountDownLatch latch = new CountDownLatch(1);
//    backend.getDefinitions(ar -> {
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

    private ApiDefinitionRegistry registry = ApiDefinitionRegistry.create();

    public ApiBackend() {
      vertx = Vertx.vertx();
      apiDiscovery = ApiDiscovery.create(vertx, new ApiDiscoveryOptions().setName("app"));
      JsonObject app = new JsonObject()
              .put("file", "H:\\csst\\java-core\\trunk\\06SRC\\iotp-app\\router\\api");
      JsonObject om = new JsonObject()
              .put("file", "H:\\csst\\java-core\\trunk\\06SRC\\iotp-app\\router\\om");
      JsonObject config = new JsonObject()
              .put("importer", new JsonObject().put("app", app).put("om", om));
      new ImportApi().initialize(vertx, new JsonObject().put("api.discovery", config), Future
              .<Void>future());

      try {
        TimeUnit.SECONDS.sleep(3);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      apiDiscovery.getDefinitions(r -> true, ar -> {
        ar.result().forEach(d -> registry.add(d));
      });
      try {
        TimeUnit.SECONDS.sleep(2);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    public void getDefinitions(
            Handler<AsyncResult<List<ApiDefinition>>>
                    handler) {
      List<ApiDefinition> definitions = registry.match(HttpMethod.GET, "/devices/1");
      handler.handle(Future.succeededFuture(definitions));
    }

    public void close() {
      vertx.close();
    }
  }
}
