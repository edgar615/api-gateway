package com.github.edgar615.direwolves;

import com.google.common.collect.Lists;

import com.github.edgar615.direwolves.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.definition.Endpoint;
import com.github.edgar615.direwolves.core.definition.SimpleHttpEndpoint;
import io.vertx.core.http.HttpMethod;
import org.awaitility.Awaitility;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Edgar on 2017/1/9.
 *
 * @author Edgar  Date 2017/1/9
 */
public class ApiUtils {
  public static void registerApi(ApiDiscovery apiDiscovery, int devicePort) {
    AtomicInteger seq = new AtomicInteger();
    Endpoint
          httpEndpoint = SimpleHttpEndpoint.http("add_device", HttpMethod.POST, "/devices",
                                                 devicePort, "localhost");
    ApiDefinition apiDefinition = ApiDefinition.create("add_device", HttpMethod.POST, "/devices",
                                                       Lists.newArrayList(httpEndpoint));
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    httpEndpoint = SimpleHttpEndpoint.http("list_device", HttpMethod.GET, "/devices",
                                           devicePort, "localhost");
    apiDefinition = ApiDefinition.create("list_device", HttpMethod.GET, "/devices",
                                         Lists.newArrayList(httpEndpoint));
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    httpEndpoint = SimpleHttpEndpoint.http("list_device", HttpMethod.GET, "/devices",
                                           devicePort, "localhost");
    apiDefinition = ApiDefinition.create("list_device2", HttpMethod.GET, "/devices",
                                         Lists.newArrayList(httpEndpoint));
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    httpEndpoint = SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "/devices/$var.param0",
                                           devicePort, "localhost");
    apiDefinition = ApiDefinition.createRegex("get_device", HttpMethod.GET, "/devices/([\\d+]+)",
                                         Lists.newArrayList(httpEndpoint));
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    httpEndpoint = SimpleHttpEndpoint.http("add_device", HttpMethod.POST, "/devices",
                                           devicePort, "localhost");
    apiDefinition = ApiDefinition.create("add_device", HttpMethod.POST, "/devices",
                                         Lists.newArrayList(httpEndpoint));
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });
    httpEndpoint = SimpleHttpEndpoint.http("update_device", HttpMethod.PUT, "/devices/$var.param0",
                                           devicePort, "localhost");
    apiDefinition = ApiDefinition.createRegex("update_device", HttpMethod.PUT, "/devices/([\\d+]+)",
                                         Lists.newArrayList(httpEndpoint));
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    httpEndpoint = SimpleHttpEndpoint.http("error_device", HttpMethod.GET, "/devices/error",
                                           devicePort, "localhost");
    apiDefinition = ApiDefinition.create("error_device", HttpMethod.GET, "/devices/failed",
                                         Lists.newArrayList(httpEndpoint));
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    httpEndpoint = SimpleHttpEndpoint.http("list_device", HttpMethod.GET, "/devices",
                                           devicePort, "localhost");
    apiDefinition = ApiDefinition.create("list_device_version2", HttpMethod.GET, "/v2/devices",
                                         Lists.newArrayList(httpEndpoint));
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    httpEndpoint = SimpleHttpEndpoint.http("get_user", HttpMethod.GET, "/user",
                                           devicePort, "localhost");
    apiDefinition = ApiDefinition.createRegex("get_user", HttpMethod.GET, "/user/([\\d+]+)",
                                         Lists.newArrayList(httpEndpoint));
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    httpEndpoint = SimpleHttpEndpoint.http("userAnt", HttpMethod.GET, "/user/",
                                           devicePort, "localhost");
    apiDefinition = ApiDefinition.createAnt("userAnt", HttpMethod.GET, "/user/**",
                                         Lists.newArrayList(httpEndpoint));
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    Awaitility.await().until(() -> seq.get() == 10);
  }

}
