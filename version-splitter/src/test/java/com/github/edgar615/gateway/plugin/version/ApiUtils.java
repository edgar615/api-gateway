package com.github.edgar615.gateway.plugin.version;

import com.google.common.collect.Lists;

import com.github.edgar615.gateway.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.definition.Endpoint;
import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.util.net.IPUtils;
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
        ApiDefinition apiDefinition =
                ApiDefinition.create("add_device", HttpMethod.POST, "/devices",
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
        VersionSplitterPlugin grayPlugin =
                new VersionSplitterPlugin().floor(new RequestVersionTraffic());
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
        grayPlugin = new VersionSplitterPlugin().floor(new RequestVersionTraffic());
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
        grayPlugin = new VersionSplitterPlugin().ceil(new RequestVersionTraffic());
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


        httpEndpoint = SimpleHttpEndpoint.http("ip-hash", HttpMethod.GET, "/ip-splitter",
                                               devicePort, "localhost");
        apiDefinition = ApiDefinition.create("ip-hash1", HttpMethod.GET, "/ip-splitter",
                                             Lists.newArrayList(httpEndpoint));
        versionPlugin = new VersionPlugin();
        versionPlugin.setVersion("v0.0.1");
        apiDefinition.addPlugin(versionPlugin);
        apiDiscovery.publish(apiDefinition, ar -> {
            seq.incrementAndGet();
        });

        httpEndpoint = SimpleHttpEndpoint.http("ip-hash", HttpMethod.GET, "/ip-splitter",
                                               devicePort, "localhost");
        apiDefinition = ApiDefinition.create("ip-hash2", HttpMethod.GET, "/ip-splitter",
                                             Lists.newArrayList(httpEndpoint));
        versionPlugin = new VersionPlugin();
        versionPlugin.setVersion("v0.0.2");
        apiDefinition.addPlugin(versionPlugin);
        apiDiscovery.publish(apiDefinition, ar -> {
            seq.incrementAndGet();
        });

        httpEndpoint = SimpleHttpEndpoint.http("ip-hash", HttpMethod.GET, "/ip-splitter",
                                               devicePort, "localhost");
        apiDefinition = ApiDefinition.create("ip-hash3", HttpMethod.GET, "/ip-splitter",
                                             Lists.newArrayList(httpEndpoint));
        versionPlugin = new VersionPlugin();
        versionPlugin.setVersion("v0.0.3");
        apiDefinition.addPlugin(versionPlugin);
        apiDiscovery.publish(apiDefinition, ar -> {
            seq.incrementAndGet();
        });

        httpEndpoint = SimpleHttpEndpoint.http("ip-hash", HttpMethod.GET, "/ip-splitter",
                                               devicePort, "localhost");
        apiDefinition = ApiDefinition.create("ip-hash4", HttpMethod.GET, "/ip-splitter",
                                             Lists.newArrayList(httpEndpoint));
        IpRangePolicy ipRangePolicy = new IpRangePolicy(IPUtils.ipToLong("192.168.1.2"), IPUtils
                .ipToLong("192.168.1.88"), "v0.0.1");
        IpHashPolicy ipHashPolicy = new IpHashPolicy(60, 80, "v0.0.2");
        IpAppointPolicy ipAppointPolicy = new IpAppointPolicy("v0.0.1").addIp("192.169.*");
        ClientIpTraffic clientIpTraffic = new ClientIpTraffic(Lists.newArrayList(ipRangePolicy,
                                                                                 ipHashPolicy,
                                                                                 ipAppointPolicy));
        grayPlugin = new VersionSplitterPlugin().ceil(clientIpTraffic);
        apiDefinition.addPlugin(grayPlugin);
        apiDiscovery.publish(apiDefinition, ar -> {
            seq.incrementAndGet();
        });


        Awaitility.await().until(() -> seq.get() == 15);
    }

}