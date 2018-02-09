package com.github.edgar615.direwolves.plugin.version;

import com.google.common.collect.Lists;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.apidiscovery.ApiDiscovery;
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

    httpEndpoint = SimpleHttpEndpoint.http("add_user", HttpMethod.POST, "/users",
                                           devicePort, "localhost");
    apiDefinition = ApiDefinition.create("add_user", HttpMethod.POST, "/users",
                                         Lists.newArrayList(httpEndpoint));
    VersionMatchPlugin grayPlugin = new VersionMatchPlugin();
    apiDefinition.addPlugin(grayPlugin);
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    httpEndpoint = SimpleHttpEndpoint.http("add_user", HttpMethod.POST, "/users",
                                           devicePort, "localhost");
    apiDefinition = ApiDefinition.create("add_user2", HttpMethod.POST, "/users",
                                         Lists.newArrayList(httpEndpoint));
    VersionPlugin versionPlugin = new VersionPlugin();
    versionPlugin.setVersion("20171101");
    apiDefinition.addPlugin(versionPlugin);
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });


    httpEndpoint = SimpleHttpEndpoint.http("add_menu", HttpMethod.POST, "/menus",
                                           devicePort, "localhost");
    apiDefinition = ApiDefinition.create("add_menu", HttpMethod.POST, "/menus",
                                         Lists.newArrayList(httpEndpoint));
     grayPlugin = new VersionMatchPlugin();
    apiDefinition.addPlugin(grayPlugin);
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    httpEndpoint = SimpleHttpEndpoint.http("add_menu", HttpMethod.POST, "/menus",
                                           devicePort, "localhost");
    apiDefinition = ApiDefinition.create("add_menu2", HttpMethod.POST, "/menus",
                                         Lists.newArrayList(httpEndpoint));
     versionPlugin = new VersionPlugin();
    versionPlugin.setVersion("20171101");
    apiDefinition.addPlugin(versionPlugin);
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    httpEndpoint = SimpleHttpEndpoint.http("add_menu", HttpMethod.POST, "/menus",
                                           devicePort, "localhost");
    apiDefinition = ApiDefinition.create("add_menu3", HttpMethod.POST, "/menus",
                                         Lists.newArrayList(httpEndpoint));
    versionPlugin = new VersionPlugin();
    versionPlugin.setVersion("20171108");
    apiDefinition.addPlugin(versionPlugin);
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    httpEndpoint = SimpleHttpEndpoint.http("list_menu", HttpMethod.GET, "/menus",
                                           devicePort, "localhost");
    apiDefinition = ApiDefinition.create("list_menu", HttpMethod.GET, "/menus",
                                         Lists.newArrayList(httpEndpoint));
    grayPlugin = new VersionMatchPlugin();
    grayPlugin.ceil();
    apiDefinition.addPlugin(grayPlugin);
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    httpEndpoint = SimpleHttpEndpoint.http("list_menu", HttpMethod.GET, "/menus",
                                           devicePort, "localhost");
    apiDefinition = ApiDefinition.create("list_menu2", HttpMethod.GET, "/menus",
                                         Lists.newArrayList(httpEndpoint));
    versionPlugin = new VersionPlugin();
    versionPlugin.setVersion("20171101");
    apiDefinition.addPlugin(versionPlugin);
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    httpEndpoint = SimpleHttpEndpoint.http("list_menu", HttpMethod.GET, "/menus",
                                           devicePort, "localhost");
    apiDefinition = ApiDefinition.create("list_menu3", HttpMethod.GET, "/menus",
                                         Lists.newArrayList(httpEndpoint));
    versionPlugin = new VersionPlugin();
    versionPlugin.setVersion("20171108");
    apiDefinition.addPlugin(versionPlugin);
    apiDiscovery.publish(apiDefinition, ar -> {
      seq.incrementAndGet();
    });

    Awaitility.await().until(() -> seq.get() == 11);
  }

}