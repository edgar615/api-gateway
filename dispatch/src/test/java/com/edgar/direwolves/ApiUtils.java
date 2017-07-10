package com.edgar.direwolves;

import com.google.common.collect.Lists;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiDiscovery;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import io.vertx.core.http.HttpMethod;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Edgar on 2017/1/9.
 *
 * @author Edgar  Date 2017/1/9
 */
public class ApiUtils {
  public static void registerApi(ApiDiscovery apiDiscovery) {
    AtomicInteger seq = new AtomicInteger();
    Endpoint
          httpEndpoint = HttpEndpoint.http("add_device", HttpMethod.POST, "/devices",
      "device");
    ApiDefinition apiDefinition = ApiDefinition.create("add_device", HttpMethod.POST, "/devices",
                                                       Lists.newArrayList(httpEndpoint));
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    httpEndpoint = HttpEndpoint.http("list_device", HttpMethod.GET, "/devices",
                                 "device");
    apiDefinition = ApiDefinition.create("list_device", HttpMethod.GET, "/devices",
                                         Lists.newArrayList(httpEndpoint));
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    httpEndpoint = HttpEndpoint.http("get_device", HttpMethod.GET, "/devices/$var.param0",
                                 "device");
    apiDefinition = ApiDefinition.create("get_device", HttpMethod.GET, "/devices/([\\d+]+)",
                                         Lists.newArrayList(httpEndpoint));
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    httpEndpoint = HttpEndpoint.http("add_device", HttpMethod.POST, "/devices",
                                 "device");
    apiDefinition = ApiDefinition.create("add_device", HttpMethod.POST, "/devices",
                                         Lists.newArrayList(httpEndpoint));
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });
    httpEndpoint = HttpEndpoint.http("update_device", HttpMethod.PUT, "/devices/$var.param0",
                                 "device");
    apiDefinition = ApiDefinition.create("update_device", HttpMethod.PUT, "/devices/([\\d+]+)",
                                         Lists.newArrayList(httpEndpoint));
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    httpEndpoint = HttpEndpoint.http("error_device", HttpMethod.GET, "/devices/error",
                                 "device");
    apiDefinition = ApiDefinition.create("error_device", HttpMethod.GET, "/devices/failed",
                                         Lists.newArrayList(httpEndpoint));
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });
  }

}
