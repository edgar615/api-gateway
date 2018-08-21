package com.github.edgar615.gateway.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.definition.Endpoint;
import com.github.edgar615.gateway.core.definition.EventbusEndpoint;
import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.rpc.eventbus.EventbusRpcRequest;
import com.github.edgar615.gateway.core.utils.Filters;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Edgar on 2016/11/18.
 *
 * @author Edgar  Date 2016/11/18
 */
@RunWith(VertxUnitRunner.class)
public class EventbusRequestFilterTest {
    private final List<Filter> filters = new ArrayList<>();

    private Vertx vertx;

    private Filter filter;

    private ApiContext apiContext;

    @Before
    public void testSetUp(TestContext testContext) {
        vertx = Vertx.vertx();

        JsonObject config = new JsonObject();

        filter = Filter.create(EventBusRequestFilter.class.getSimpleName(), vertx, config);

        filters.clear();
        filters.add(filter);

    }

    @After
    public void tearDown(TestContext testContext) {
//    vertx.close(testContext.asyncAssertSuccess());
    }

    @Test
    public void testEventbusEndpoint(TestContext testContext) {
        Multimap<String, String> params = ArrayListMultimap.create();
        params.put("q3", "v3");
        Multimap<String, String> headers = ArrayListMultimap.create();
        headers.put("h3", "v3");
        headers.put("h3", "v3.2");

        Multimap<String, String> ebHeaders = ArrayListMultimap.create();
        ebHeaders.put("action", "get");

        apiContext =
                ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
        Endpoint httpEndpoint =
                SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "devices/", 80, "localhost");
        EventbusEndpoint reqResp =
                EventbusEndpoint.reqResp("send_log", "send_log", null, ebHeaders);
        EventbusEndpoint point =
                EventbusEndpoint.pointToPoint("point", "send_log", null, null);
        EventbusEndpoint pub =
                EventbusEndpoint.publish("pub", "send_log", null, null);
        ApiDefinition definition = ApiDefinition
                .create("get_device", HttpMethod.GET, "devices/",
                        Lists.newArrayList(httpEndpoint, reqResp, point, pub));
        apiContext.setApiDefinition(definition);


        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    testContext.assertEquals(3, context.requests().size());
                    EventbusRpcRequest request = (EventbusRpcRequest) context.requests().get(0);
                    System.out.println(request);
                    testContext.assertEquals("send_log", request.address());
                    testContext.assertEquals(1, request.headers().size());
                    testContext.assertTrue(request.headers().containsKey("action"));
                    testContext.assertTrue(request.message().isEmpty());
                    async.complete();
                }).onFailure(t -> testContext.fail());
    }


    @Test
    public void testEventbusEndpointHasMessage(TestContext testContext) {
        Multimap<String, String> params = ArrayListMultimap.create();
        params.put("q3", "v3");
        Multimap<String, String> headers = ArrayListMultimap.create();
        headers.put("h3", "v3");
        headers.put("h3", "v3.2");

        Multimap<String, String> ebHeaders = ArrayListMultimap.create();
        headers.put("action", "get");

        apiContext =
                ApiContext.create(HttpMethod.POST, "/devices", headers, params,
                                  new JsonObject().put("foo", "bar"));
        Endpoint httpEndpoint =
                SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "devices/", 80, "localhost");
        EventbusEndpoint reqResp =
                EventbusEndpoint.reqResp("send_log", "send_log", null, ebHeaders);
        EventbusEndpoint point =
                EventbusEndpoint.pointToPoint("point", "send_log", null, null);
        EventbusEndpoint pub =
                EventbusEndpoint.publish("pub", "send_log", null, null);
        ApiDefinition definition = ApiDefinition
                .create("get_device", HttpMethod.GET, "devices/",
                        Lists.newArrayList(httpEndpoint, reqResp, point, pub));
        apiContext.setApiDefinition(definition);


        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    testContext.assertEquals(3, context.requests().size());
                    EventbusRpcRequest request = (EventbusRpcRequest) context.requests().get(2);
                    testContext.assertEquals("send_log", request.address());
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertTrue(request.message().containsKey("foo"));
                    async.complete();
                }).onFailure(t -> testContext.fail());
    }

}
