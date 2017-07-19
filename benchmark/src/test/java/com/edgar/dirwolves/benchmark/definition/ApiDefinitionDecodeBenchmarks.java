package com.edgar.dirwolves.benchmark.definition;

import com.google.common.collect.Lists;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.plugin.acl.AclRestrictionFactory;
import com.edgar.direwolves.plugin.acl.AclRestrictionPlugin;
import com.edgar.direwolves.plugin.appkey.AppKeyPlugin;
import com.edgar.direwolves.plugin.appkey.AppKeyPluginFactory;
import com.edgar.direwolves.plugin.arg.BodyArgPlugin;
import com.edgar.direwolves.plugin.arg.BodyArgPluginFactory;
import com.edgar.direwolves.plugin.arg.Parameter;
import com.edgar.direwolves.plugin.arg.UrlArgPlugin;
import com.edgar.direwolves.plugin.arg.UrlArgPluginFactory;
import com.edgar.direwolves.plugin.ip.IpRestriction;
import com.edgar.direwolves.plugin.ip.IpRestrictionFactory;
import com.edgar.util.validation.Rule;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import org.openjdk.jmh.annotations.*;

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
            HttpEndpoint.http("device.add", HttpMethod.POST, "/devices", "device");
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

    AclRestrictionPlugin aclRestrictionPlugin =
            (AclRestrictionPlugin) new AclRestrictionFactory().create();
    apiDefinition.addPlugin(aclRestrictionPlugin);

    AppKeyPlugin appKeyPlugin = (AppKeyPlugin) new AppKeyPluginFactory().create();
    apiDefinition.addPlugin(appKeyPlugin);

    jsonObject = apiDefinition.toJson();
    System.out.println(jsonObject.encode());

  }


  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Fork(1)
  @OperationsPerInvocation(100)
  public ApiDefinition testApi() {
    return ApiDefinition.fromJson(jsonObject);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @Fork(1)
  @OperationsPerInvocation(100)
  public ApiDefinition testAverage() {
    return ApiDefinition.fromJson(jsonObject);
  }


}
