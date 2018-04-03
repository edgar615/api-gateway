package com.github.edgar615.dirwolves.benchmark.definition;

import com.google.common.collect.Lists;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.definition.HttpEndpoint;
import com.github.edgar615.direwolves.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.direwolves.plugin.acl.AclRestriction;
import com.github.edgar615.direwolves.plugin.acl.AclRestrictionFactory;
import com.github.edgar615.direwolves.plugin.appkey.AppKeyPlugin;
import com.github.edgar615.direwolves.plugin.appkey.AppKeyPluginFactory;
import com.github.edgar615.direwolves.plugin.arg.BodyArgPlugin;
import com.github.edgar615.direwolves.plugin.arg.BodyArgPluginFactory;
import com.github.edgar615.direwolves.plugin.arg.Parameter;
import com.github.edgar615.direwolves.plugin.arg.UrlArgPlugin;
import com.github.edgar615.direwolves.plugin.arg.UrlArgPluginFactory;
import com.github.edgar615.direwolves.plugin.ip.IpRestriction;
import com.github.edgar615.direwolves.plugin.ip.IpRestrictionFactory;
import com.github.edgar615.util.validation.Rule;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2017/7/17.
 *
 * @author Edgar  Date 2017/7/17
 */
@State(Scope.Benchmark)
public class ApiDefinitionDecodeBenchmarks {

  private JsonObject jsonObject;

  @Setup
  public void setup() {

    HttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("device.add", HttpMethod.POST,
                                    "/devices", 80, "localhost");
    ApiDefinition apiDefinition = ApiDefinition.create("device.add", HttpMethod.POST, "/devices",
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

    UrlArgPlugin urlArgPlugin = (UrlArgPlugin) new UrlArgPluginFactory().create();
    parameter = Parameter.create("barcode", null);
    parameter.addRule(Rule.required());
    parameter.addRule(Rule.regex("LH[0-7][0-9a-fA-F]{2}[0-5][0-4][0-9a-fA-F]{12}"));
    urlArgPlugin.add(parameter);
    parameter = Parameter.create("encryptKey", null);
    parameter.addRule(Rule.required());
    parameter.addRule(Rule.regex("[0-9A-F]{16}"));
    urlArgPlugin.add(parameter);
    apiDefinition.addPlugin(urlArgPlugin);

    IpRestriction ipRestriction = (IpRestriction) new IpRestrictionFactory().create();
    apiDefinition.addPlugin(ipRestriction);

    AclRestriction aclRestriction =
            (AclRestriction) new AclRestrictionFactory().create();
    apiDefinition.addPlugin(aclRestriction);

    AppKeyPlugin appKeyPlugin = (AppKeyPlugin) new AppKeyPluginFactory().create();
    apiDefinition.addPlugin(appKeyPlugin);

    jsonObject = apiDefinition.toJson();
    System.out.println(jsonObject.encode());

  }


  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Fork(1)
  @OperationsPerInvocation(10000)
  public ApiDefinition testApi() {
    return ApiDefinition.fromJson(jsonObject);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Fork(1)
  @OperationsPerInvocation(10000)
  public ApiDefinition testAverage() {
    return ApiDefinition.fromJson(jsonObject);
  }


}
