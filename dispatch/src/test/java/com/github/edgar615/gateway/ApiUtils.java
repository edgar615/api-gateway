package com.github.edgar615.gateway;

import com.google.common.collect.Lists;

import com.github.edgar615.gateway.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.definition.Endpoint;
import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.gateway.core.plugin.order.OrderPlugin;
import com.github.edgar615.gateway.core.plugin.predicate.AfterPredicate;
import com.github.edgar615.gateway.core.plugin.predicate.BeforePredicate;
import com.github.edgar615.gateway.core.plugin.predicate.BetweenPredicate;
import com.github.edgar615.gateway.core.plugin.predicate.PredicatePlugin;
import io.vertx.core.http.HttpMethod;
import org.awaitility.Awaitility;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

        httpEndpoint = SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "/devices/$var.param0",
                                               devicePort, "localhost");
        apiDefinition =
                ApiDefinition.createRegex("get_device", HttpMethod.GET, "/devices/([\\d+]+)",
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
        httpEndpoint =
                SimpleHttpEndpoint.http("update_device", HttpMethod.PUT, "/devices/$var.param0",
                                        devicePort, "localhost");
        apiDefinition =
                ApiDefinition.createRegex("update_device", HttpMethod.PUT, "/devices/([\\d+]+)",
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

        //测试order
        httpEndpoint = SimpleHttpEndpoint.http(HttpMethod.GET, "/user",
                                               devicePort, "localhost");
        apiDefinition = ApiDefinition.create("order.api.1.0.0", HttpMethod.GET, "/order",
                                             Lists.newArrayList(httpEndpoint));
        apiDefinition.addPlugin(new OrderPlugin(100));
        apiDiscovery.publish(apiDefinition, ar -> {
            seq.incrementAndGet();
        });

        httpEndpoint = SimpleHttpEndpoint.http(HttpMethod.GET, "/user",
                                               devicePort, "localhost");
        apiDefinition = ApiDefinition.create("order.api.1.0.1", HttpMethod.GET, "/order",
                                             Lists.newArrayList(httpEndpoint));
        apiDefinition.addPlugin(new OrderPlugin(20));
        apiDiscovery.publish(apiDefinition, ar -> {
            seq.incrementAndGet();
        });

        httpEndpoint = SimpleHttpEndpoint.http(HttpMethod.GET, "/user",
                                               devicePort, "localhost");
        apiDefinition = ApiDefinition.create("order.api.2.0.0", HttpMethod.GET, "/order2",
                                             Lists.newArrayList(httpEndpoint));
        apiDefinition.addPlugin(new OrderPlugin(100));
        apiDiscovery.publish(apiDefinition, ar -> {
            seq.incrementAndGet();
        });

        httpEndpoint = SimpleHttpEndpoint.http(HttpMethod.GET, "/user",
                                               devicePort, "localhost");
        apiDefinition = ApiDefinition.create("order.api.2.0.1", HttpMethod.GET, "/order2",
                                             Lists.newArrayList(httpEndpoint));
        apiDefinition.addPlugin(new OrderPlugin(100));
        apiDiscovery.publish(apiDefinition, ar -> {
            seq.incrementAndGet();
        });

        //谓词
        //before
        httpEndpoint = SimpleHttpEndpoint.http(HttpMethod.GET, "/user",
                                               devicePort, "localhost");
        apiDefinition = ApiDefinition.create("before.api.1.0.0", HttpMethod.GET, "/before",
                                             Lists.newArrayList(httpEndpoint));
        apiDefinition.addPlugin(new OrderPlugin(100));
        BeforePredicate beforePredicate = new BeforePredicate(
                ZonedDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        apiDefinition.addPlugin(new PredicatePlugin().add(beforePredicate));
        apiDiscovery.publish(apiDefinition, ar -> {
            seq.incrementAndGet();
        });
        httpEndpoint = SimpleHttpEndpoint.http(HttpMethod.GET, "/user",
                                               devicePort, "localhost");
        apiDefinition = ApiDefinition.create("before.api.1.0.2", HttpMethod.GET, "/before",
                                             Lists.newArrayList(httpEndpoint));
        apiDefinition.addPlugin(new OrderPlugin(20));
        beforePredicate = new BeforePredicate(
                ZonedDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        apiDefinition.addPlugin(new PredicatePlugin().add(beforePredicate));
        apiDiscovery.publish(apiDefinition, ar -> {
            seq.incrementAndGet();
        });

        //after
        httpEndpoint = SimpleHttpEndpoint.http(HttpMethod.GET, "/user",
                                               devicePort, "localhost");
        apiDefinition = ApiDefinition.create("after.api.1.0.0", HttpMethod.GET, "/after",
                                             Lists.newArrayList(httpEndpoint));
        apiDefinition.addPlugin(new OrderPlugin(100));
        AfterPredicate afterPredicate = new AfterPredicate(
                ZonedDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        apiDefinition.addPlugin(new PredicatePlugin().add(afterPredicate));
        apiDiscovery.publish(apiDefinition, ar -> {
            seq.incrementAndGet();
        });
        httpEndpoint = SimpleHttpEndpoint.http(HttpMethod.GET, "/user",
                                               devicePort, "localhost");
        apiDefinition = ApiDefinition.create("after.api.1.0.2", HttpMethod.GET, "/after",
                                             Lists.newArrayList(httpEndpoint));
        apiDefinition.addPlugin(new OrderPlugin(20));
        afterPredicate = new AfterPredicate(
                ZonedDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        apiDefinition.addPlugin(new PredicatePlugin().add(afterPredicate));
        apiDiscovery.publish(apiDefinition, ar -> {
            seq.incrementAndGet();
        });

        //between
        httpEndpoint = SimpleHttpEndpoint.http(HttpMethod.GET, "/user",
                                               devicePort, "localhost");
        apiDefinition = ApiDefinition.create("between.api.1.0.0", HttpMethod.GET, "/between",
                                             Lists.newArrayList(httpEndpoint));
        apiDefinition.addPlugin(new OrderPlugin(100));
        BetweenPredicate betweenPredicate = new BetweenPredicate(
                ZonedDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                ZonedDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        apiDefinition.addPlugin(new PredicatePlugin().add(betweenPredicate));
        apiDiscovery.publish(apiDefinition, ar -> {
            seq.incrementAndGet();
        });
        httpEndpoint = SimpleHttpEndpoint.http(HttpMethod.GET, "/user",
                                               devicePort, "localhost");
        apiDefinition = ApiDefinition.create("between.api.1.0.2", HttpMethod.GET, "/between",
                                             Lists.newArrayList(httpEndpoint));
        apiDefinition.addPlugin(new OrderPlugin(20));
        betweenPredicate = new BetweenPredicate(
                ZonedDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                ZonedDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        apiDefinition.addPlugin(new PredicatePlugin().add(betweenPredicate));
        apiDiscovery.publish(apiDefinition, ar -> {
            seq.incrementAndGet();
        });

        Awaitility.await().until(() -> seq.get() == 20);
    }

}
