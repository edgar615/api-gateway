package com.github.edgar615.dirwolves.benchmark.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.definition.HttpEndpoint;
import com.github.edgar615.direwolves.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.Filters;
import com.github.edgar615.direwolves.plugin.arg.Parameter;
import com.github.edgar615.direwolves.plugin.arg.UrlArgFilterFactory;
import com.github.edgar615.direwolves.plugin.arg.UrlArgPlugin;
import com.github.edgar615.direwolves.plugin.arg.UrlArgPluginFactory;
import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.vertx.task.Task;
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
 * <p>
 * <pre>
 *   Benchmark                            Mode  Cnt       Score       Error   Units
 * UrlArgFilterBenchmarks.testApi      thrpt   20  894706.305 ± 11643.738  ops/ms
 * UrlArgFilterBenchmarks.testAverage   avgt   20       1.026 ±     0.024   ns/op
 * </pre>
 *
 * @author Edgar  Date 2017/7/17
 */
@State(Scope.Benchmark)
public class UrlArgFilterBenchmarks {
  @TearDown(Level.Trial)
  public void tearDown(ApiFilter pool) {
    pool.close();
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Fork(1)
  @OperationsPerInvocation(10000)
  public void testApi(ApiFilter apiFilter, ApiContextBuilder builder) {
    ApiContext apiContext = builder.apiContext();

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
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Fork(1)
  @OperationsPerInvocation(10000)
  public void testAverage(ApiFilter apiFilter, ApiContextBuilder builder) {
    ApiContext apiContext = builder.apiContext();

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
  public static class ApiContextBuilder {
    private ApiContext apiContext;

    public ApiContextBuilder() {
      Multimap<String, String> params = ArrayListMultimap.create();
      params.put("encryptKey", "0000000000000000");
      params.put("barcode", "LH10312ACCF23C4F3A5");

//      JsonObject body = new JsonObject()
//              .put("encryptKey", "0000000000000000")
//              .put("barcode", "LH10312ACCF23C4F3A5");
      apiContext = ApiContext.create(HttpMethod.POST, "/devices", null, params, null);

      HttpEndpoint httpEndpoint = SimpleHttpEndpoint.http("device.add", HttpMethod.POST,
              "/devices", 80, "localhost");
      ApiDefinition apiDefinition = ApiDefinition.create("device.add", HttpMethod.POST, "/devices",
              Lists.newArrayList(httpEndpoint));
      UrlArgPlugin plugin = (UrlArgPlugin) new UrlArgPluginFactory().create();
      Parameter parameter = Parameter.create("barcode", null);
      parameter.addRule(Rule.required());
      parameter.addRule(Rule.regex("LH[0-7][0-9a-fA-F]{2}[0-5][0-4][0-9a-fA-F]{12}"));
      plugin.add(parameter);
      parameter = Parameter.create("encryptKey", null);
      parameter.addRule(Rule.required());
      parameter.addRule(Rule.regex("[0-9A-F]{16}"));
      plugin.add(parameter);
      apiDefinition.addPlugin(plugin);
      apiContext.setApiDefinition(apiDefinition);
    }

    public ApiContext apiContext() {
      return apiContext;
    }
  }

  @State(Scope.Benchmark)
  public static class ApiFilter {
    private Vertx vertx;

    private List<Filter> filters = new ArrayList<>();

    public ApiFilter() {
      vertx = Vertx.vertx();

      filters.add(new UrlArgFilterFactory().create(vertx, new JsonObject()));
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
