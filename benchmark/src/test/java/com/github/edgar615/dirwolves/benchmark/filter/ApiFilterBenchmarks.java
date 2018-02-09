package com.github.edgar615.dirwolves.benchmark.filter;

import com.github.edgar615.direwolves.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.direwolves.core.apidiscovery.ApiDiscoveryOptions;
import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.Filters;
import com.github.edgar615.direwolves.filter.ApiMatchFilterFactory;
import com.github.edgar615.direwolves.verticle.FileApiDiscoveryVerticle;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2017/7/17.
 *
 * @author Edgar  Date 2017/7/17
 */
@State(Scope.Benchmark)
public class ApiFilterBenchmarks {
  @TearDown(Level.Trial)
  public void tearDown(ApiFilter pool) {
    pool.close();
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Fork(1)
  @OperationsPerInvocation(100)
  public void testApi(ApiFilter apiFilter) {
    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices/1", null, null, null);

    final CountDownLatch latch = new CountDownLatch(1);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);

    apiFilter.doFilter(task)
            .andThen(context -> {
              latch.countDown();
            }).onFailure(t -> t.printStackTrace());
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
  public void testAverage(ApiFilter apiFilter) {
    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices/1", null, null, null);

    final CountDownLatch latch = new CountDownLatch(1);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);

    apiFilter.doFilter(task)
            .andThen(context -> {
              latch.countDown();
            }).onFailure(t -> t.printStackTrace());
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
  public void testSampleTime(ApiFilter apiFilter) {
    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices/1", null, null, null);

    final CountDownLatch latch = new CountDownLatch(1);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);

    apiFilter.doFilter(task)
            .andThen(context -> {
              latch.countDown();
            }).onFailure(t -> t.printStackTrace());
    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @State(Scope.Benchmark)
  public static class ApiFilter {
    private Vertx vertx;

    private ApiDiscovery apiDiscovery;

    private List<Filter> filters = new ArrayList<>();

    public ApiFilter() {
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
      JsonObject jsonObject = new JsonObject()
              .put("namespace", "app");

      filters.add(new ApiMatchFilterFactory().create(vertx, jsonObject));
      try {
        TimeUnit.SECONDS.sleep(2);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    public void close() {
      vertx.close();
    }

    Task<ApiContext> doFilter(Task<ApiContext> task) {
      return Filters.doFilter(task, filters);
    }
  }
}
