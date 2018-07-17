package com.github.edgar615.gateway.benchmark.appkey;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.definition.HttpEndpoint;
import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.utils.Filters;
import com.github.edgar615.gateway.plugin.appkey.AppKeyFilterFactory;
import com.github.edgar615.gateway.plugin.appkey.AppKeyPlugin;
import com.github.edgar615.gateway.plugin.appkey.AppKeyPluginFactory;
import com.github.edgar615.util.base.EncryptUtils;
import com.github.edgar615.util.base.Randoms;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2017/7/17.
 * Benchmark                            Mode  Cnt       Score      Error   Units
 * AppKeyFilterBenchmarks.testApi      thrpt   20  450407.791 ± 5983.659  ops/ms
 * AppKeyFilterBenchmarks.testAverage   avgt   20       2.208 ±    0.039   ns/op
 *
 * @author Edgar  Date 2017/7/17
 */
@State(Scope.Benchmark)
public class AppKeyFilterBenchmarks {

  private static String appKey = UUID.randomUUID().toString();

  private static String appSecret = UUID.randomUUID().toString();

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Fork(1)
  @OperationsPerInvocation(10000)
  public void testApi(AppKeyFilter filter, ApiContextBuilder builder) {
    final CountDownLatch latch = new CountDownLatch(1);

    Task<ApiContext> task = Task.create();
    task.complete(builder.apiContext);

    filter.doFilter(task)
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
  public void testAverage(AppKeyFilter apiFilter, ApiContextBuilder builder) {
    final CountDownLatch latch = new CountDownLatch(1);

    Task<ApiContext> task = Task.create();
    task.complete(builder.apiContext);

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
      params.put("appKey", appKey);
      params.put("nonce", Randoms.randomAlphabetAndNum(10));
      params.put("signMethod", "HMACMD5");
      params.put("v", "1.0");
      params.put("deviceId", "1");

      JsonObject body = new JsonObject()
              .put("name", "$#$%$%$%")
              .put("code", 123434);

      params.put("body", body.encode());
      params.put("sign", signTopRequest(params, appSecret, "HMACMD5"));
      params.removeAll("body");

      this.apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, params, body);

      HttpEndpoint httpEndpoint = SimpleHttpEndpoint.http("device.add", HttpMethod.POST,
              "/devices", 80, "localhost");
      ApiDefinition apiDefinition = ApiDefinition.create("device.add", HttpMethod.POST, "/devices",
              Lists.newArrayList(httpEndpoint));
      AppKeyPlugin plugin = (AppKeyPlugin) new AppKeyPluginFactory().create();
      apiDefinition.addPlugin(plugin);
      apiContext.setApiDefinition(apiDefinition);
    }

    public ApiContext apiContext() {
      return apiContext;
    }

    private String getFirst(Multimap<String, String> params, String paramName) {
      return Lists.newArrayList(params.get(paramName)).get(0);
    }

    private String signTopRequest(Multimap<String, String> params, String secret, String signMethod) {
      // 第一步：检查参数是否已经排序
      String[] keys = params.keySet().toArray(new String[0]);
      Arrays.sort(keys);

      // 第二步：把所有参数名和参数值串在一起
      List<String> query = new ArrayList<>(params.size());
      for (String key : keys) {
        String value = getFirst(params, key);
        if (!Strings.isNullOrEmpty(value)) {
          query.add(key + "=" + value);
        }
      }
      String queryString = Joiner.on("&").join(query);
      String sign = null;
      try {
        if (EncryptUtils.HMACMD5.equalsIgnoreCase(signMethod)) {
          sign = EncryptUtils.encryptHmacMd5(queryString, secret);
        } else if (EncryptUtils.HMACSHA256.equalsIgnoreCase(signMethod)) {
          sign = EncryptUtils.encryptHmacSha256(queryString, secret);
        } else if (EncryptUtils.HMACSHA512.equalsIgnoreCase(signMethod)) {
          sign = EncryptUtils.encryptHmacSha512(queryString, secret);
        } else if (EncryptUtils.MD5.equalsIgnoreCase(signMethod)) {
          sign = EncryptUtils.encryptMD5(secret + queryString + secret);
        }
      } catch (IOException e) {

      }
      return sign;
    }
  }

  @State(Scope.Benchmark)
  public static class AppKeyFilter {
    private Vertx vertx;

    private List<Filter> filters = new ArrayList<>();

    public AppKeyFilter() {
      vertx = Vertx.vertx();
      JsonObject origin = new JsonObject()
              .put("appSecret", appSecret)
              .put("clientCode", 0)
              .put("appKey", appKey);
      JsonObject config = new JsonObject()
              .put("secretKey", "appSecret")
              .put("codeKey", "clientCode")
              .put("data", new JsonArray().add(origin));
      JsonObject jsonObject = new JsonObject()
              .put("appkey", config)
              .put("namespace", "app");

      filters.add(new AppKeyFilterFactory().create(vertx, jsonObject));
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
