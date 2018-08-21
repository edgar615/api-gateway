package com.github.edgar615.gateway.benchmark.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.definition.Endpoint;
import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.plugin.arg.BodyArgPlugin;
import com.github.edgar615.gateway.plugin.arg.BodyArgPluginFactory;
import com.github.edgar615.gateway.plugin.arg.Parameter;
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
public class ApiContextOnPluginBenchmarks {

    private ApiContext apiContext;

    @Setup(Level.Invocation)
    public void setup() {
        Multimap<String, String> params = ArrayListMultimap.create();
        params.put("encryptKey", Randoms.randomAlphabetAndNum(16));
        params.put("barcode", Randoms.randomAlphabetAndNum(19));

//      JsonObject body = new JsonObject()
//              .put("encryptKey", "0000000000000000")
//              .put("barcode", "LH10312ACCF23C4F3A5");
        apiContext = ApiContext.create(HttpMethod.POST, "/devices", params, null, null);

        Endpoint httpEndpoint = SimpleHttpEndpoint.http("device.add", HttpMethod.POST,
                                                        "/devices", 80, "loclahost");
        ApiDefinition apiDefinition =
                ApiDefinition.create("device.add", HttpMethod.POST, "/devices",
                                     Lists.newArrayList(httpEndpoint));
        BodyArgPlugin plugin = (BodyArgPlugin) new BodyArgPluginFactory().create();
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


    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(1)
    @OperationsPerInvocation(10000)
    public ApiContext testApi() {
        return apiContext.copy();
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
