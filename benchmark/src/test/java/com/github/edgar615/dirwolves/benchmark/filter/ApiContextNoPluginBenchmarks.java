package com.github.edgar615.dirwolves.benchmark.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.definition.HttpEndpoint;
import com.github.edgar615.direwolves.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.util.base.Randoms;
import com.github.edgar615.util.validation.Rule;
import io.vertx.core.http.HttpMethod;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2017/7/17.
 *
 * @author Edgar  Date 2017/7/17
 */
@State(Scope.Benchmark)
@Warmup(iterations = 20)
public class ApiContextNoPluginBenchmarks {

  private ApiContext apiContext;

  @Setup(Level.Invocation)
  public void setup() {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("encryptKey", Randoms.randomAlphabetAndNum(16));
    params.put("barcode", Randoms.randomAlphabetAndNum(19));

    apiContext = ApiContext.create(HttpMethod.POST, "/devices", params, null, null);

    HttpEndpoint httpEndpoint = SimpleHttpEndpoint.http("device.add", HttpMethod.POST,
                                                        "/devices", 80, "localhost");
    ApiDefinition apiDefinition = ApiDefinition.create("device.add", HttpMethod.POST, "/devices",
                                                       Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(apiDefinition);
  }


  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Fork(1)
  @OperationsPerInvocation(10000)
  public ApiContext testApi() {
    return  apiContext.copy();
//    blackhole.consume(copy);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Fork(1)
  @OperationsPerInvocation(10000)
  public ApiContext testAverage() {
    return apiContext.copy();
  }


}
