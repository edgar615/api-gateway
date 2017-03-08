package com.edgar.direwolves;

import com.google.common.collect.Lists;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import io.vertx.core.http.HttpMethod;

/**
 * Created by Edgar on 2017/1/9.
 *
 * @author Edgar  Date 2017/1/9
 */
public class ApiUtils {
  public static void registerApi() {Endpoint
          httpEndpoint = Endpoint.http("add_device", HttpMethod.POST, "/devices",
                                       "device");
    ApiDefinition apiDefinition = ApiDefinition.create("add_device", HttpMethod.POST, "/devices",
                                                       Lists.newArrayList(httpEndpoint));
    ApiDefinitionRegistry.create().add(apiDefinition);

    httpEndpoint = Endpoint.http("list_device", HttpMethod.GET, "/devices",
                                 "device");
    apiDefinition = ApiDefinition.create("list_device", HttpMethod.GET, "/devices",
                                         Lists.newArrayList(httpEndpoint));
    ApiDefinitionRegistry.create().add(apiDefinition);

    httpEndpoint = Endpoint.http("get_device", HttpMethod.GET, "/devices/$var.param0",
                                 "device");
    apiDefinition = ApiDefinition.create("get_device", HttpMethod.GET, "/devices/([\\d+]+)",
                                         Lists.newArrayList(httpEndpoint));
    ApiDefinitionRegistry.create().add(apiDefinition);

    httpEndpoint = Endpoint.http("add_device", HttpMethod.POST, "/devices",
                                 "device");
    apiDefinition = ApiDefinition.create("add_device", HttpMethod.POST, "/devices",
                                         Lists.newArrayList(httpEndpoint));
    ApiDefinitionRegistry.create().add(apiDefinition);
    httpEndpoint = Endpoint.http("update_device", HttpMethod.PUT, "/devices/$var.param0",
                                 "device");
    apiDefinition = ApiDefinition.create("update_device", HttpMethod.PUT, "/devices/([\\d+]+)",
                                         Lists.newArrayList(httpEndpoint));
    ApiDefinitionRegistry.create().add(apiDefinition);

    httpEndpoint = Endpoint.http("error_device", HttpMethod.GET, "/devices/error",
                                 "device");
    apiDefinition = ApiDefinition.create("error_device", HttpMethod.GET, "/devices/failed",
                                         Lists.newArrayList(httpEndpoint));
    ApiDefinitionRegistry.create().add(apiDefinition);
  }

}
