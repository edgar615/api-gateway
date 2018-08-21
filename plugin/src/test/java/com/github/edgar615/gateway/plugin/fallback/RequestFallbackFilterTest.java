package com.github.edgar615.gateway.plugin.fallback;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.rpc.RpcResponse;
import com.github.edgar615.gateway.core.rpc.http.HttpRpcRequest;
import com.github.edgar615.gateway.core.rpc.http.SimpleHttpRequest;
import com.github.edgar615.gateway.core.utils.Filters;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Edgar on 2017/8/7.
 *
 * @author Edgar  Date 2017/8/7
 */
@RunWith(VertxUnitRunner.class)
public class RequestFallbackFilterTest {
    private final List<Filter> filters = new ArrayList<>();

    Filter filter;

    private Vertx vertx;

    private ApiContext apiContext;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        createApiContext();
        filter = new RequestFallbackFilter();

        filters.clear();
        filters.add(filter);
    }

    @Test
    public void testFilter(TestContext testContext) {
        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    testContext.assertEquals(2, context.requests().size());
                    HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
                    testContext.assertNotNull(request.fallback());

                    request = (HttpRpcRequest) context.requests().get(1);
                    testContext.assertNull(request.fallback());
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });
    }

    private void createApiContext() {
        Multimap<String, String> params = ArrayListMultimap.create();
        params.put("q3", "v3");
        Multimap<String, String> headers = ArrayListMultimap.create();
        headers.put("h3", "v3");
        apiContext =
                ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
        SimpleHttpEndpoint httpEndpoint =
                SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/",
                                        80, "localhost");
        ApiDefinition definition =
                ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists
                        .newArrayList(httpEndpoint));

        FallbackPlugin plugin = new FallbackPlugin()
                .addFallBack("add_device", RpcResponse.create(UUID.randomUUID().toString(),
                                                              200,
                                                              new JsonObject().put("foo", "bar")
                                                                      .encode(),
                                                              0));
        definition.addPlugin(plugin);

        apiContext.setApiDefinition(definition);

        SimpleHttpRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                                    "add_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.GET)
                .setPath("/")
                .addParam("q3", "q3")
                .addParam("q5", "q5")
                .addHeader("h3", "h3")
                .addHeader("h6", "h6");
        apiContext.addRequest(httpRpcRequest);

        httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                  "update_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.POST)
                .setPath("/")
                .addParam("q3", "q3")
                .addParam("q6", "q6")
                .addHeader("h3", "h3")
                .addHeader("h5", "h5");
        apiContext.addRequest(httpRpcRequest);
    }
}
