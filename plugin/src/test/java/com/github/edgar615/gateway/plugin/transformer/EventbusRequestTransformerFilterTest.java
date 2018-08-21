package com.github.edgar615.gateway.plugin.transformer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.definition.EventbusEndpoint;
import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.rpc.eventbus.EventbusRpcRequest;
import com.github.edgar615.gateway.core.utils.Filters;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
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
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
@RunWith(VertxUnitRunner.class)
public class EventbusRequestTransformerFilterTest {

    private final List<Filter> filters = new ArrayList<>();

    EventbusRequestTransformerFilter filter;

    private ApiContext apiContext;

    private Vertx vertx;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        createApiContext();
    }

    @After
    public void tearDown(TestContext testContext) {
        vertx.close(testContext.asyncAssertSuccess());
    }

    @Test
    public void testHeaderAdd(TestContext testContext) {
        JsonObject config = new JsonObject()
                .put("header.add", new JsonArray().add("h1:h1.1").add("h1:h1.2"));
        filter = new EventbusRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);
        Multimap<String, String> ebHeaders = ArrayListMultimap.create();

        apiContext.addRequest(EventbusRpcRequest
                                      .create("a", "add_device", "send_log", EventbusEndpoint
                                                      .REQ_RESP,
                                              ebHeaders, new JsonObject()));


        apiContext.addRequest(EventbusRpcRequest
                                      .create("b", "update_device", "send_log", EventbusEndpoint
                                                      .REQ_RESP,
                                              ArrayListMultimap.create(), new JsonObject()));


        RequestTransformer transformer = RequestTransformer.create("add_device");

        transformer.addHeader("h2", "h2").addHeader("h1", "h1.3");

        RequestTransformerPlugin plugin = new RequestTransformerPluginImpl()
                .addTransformer(transformer);
        apiContext.apiDefinition().addPlugin(plugin);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.requests());
                    testContext.assertEquals(2, context.requests().size());
                    EventbusRpcRequest request = (EventbusRpcRequest) context.requests().get(0);
                    testContext.assertEquals(4, request.headers().size());
                    testContext.assertTrue(request.headers().containsKey("h1"));
                    testContext.assertTrue(request.headers().containsKey("h2"));
                    testContext.assertEquals(3, request.headers().get("h1").size());
                    testContext.assertEquals(1, request.headers().get("h2").size());

                    request = (EventbusRpcRequest) context.requests().get(1);
                    testContext.assertTrue(request.headers().containsKey("h1"));
                    testContext.assertEquals(2, request.headers().get("h1").size());
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });
    }

    @Test
    public void testHeaderReplace(TestContext testContext) {

        JsonObject config = new JsonObject()
                .put("header.replace", new JsonArray().add("h1:nh1").add("h2:nh2"));
        filter = new EventbusRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);
        Multimap<String, String> ebHeaders = ArrayListMultimap.create();
        ebHeaders.put("h1", "h1");
        ebHeaders.put("h4", "h4.1");
        ebHeaders.put("h4", "h4.2");

        apiContext.addRequest(EventbusRpcRequest
                                      .create("a", "add_device", "send_log", EventbusEndpoint
                                                      .REQ_RESP,
                                              ebHeaders, new JsonObject()));


        apiContext.addRequest(EventbusRpcRequest
                                      .create("b", "update_device", "send_log", EventbusEndpoint
                                                      .REQ_RESP,
                                              ArrayListMultimap.create(), new JsonObject()));


        RequestTransformer transformer = RequestTransformer.create("add_device");

        transformer.replaceHeader("h3", "nh3")
                .replaceHeader("h4", "nh4")
                .replaceHeader("nh4", "nh4.1");

        RequestTransformerPlugin plugin = new RequestTransformerPluginImpl()
                .addTransformer(transformer);
        apiContext.apiDefinition().addPlugin(plugin);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.requests());
                    testContext.assertEquals(2, context.requests().size());
                    EventbusRpcRequest request = (EventbusRpcRequest) context.requests().get(0);
                    testContext.assertEquals(3, request.headers().size());
                    testContext.assertFalse(request.headers().containsKey("h1"));
                    testContext.assertFalse(request.headers().containsKey("h2"));
                    testContext.assertFalse(request.headers().containsKey("h3"));
                    testContext.assertFalse(request.headers().containsKey("h4"));
                    testContext.assertTrue(request.headers().containsKey("nh1"));
                    testContext.assertFalse(request.headers().containsKey("nh4"));
                    testContext.assertTrue(request.headers().containsKey("nh4.1"));
                    testContext.assertEquals(1, request.headers().get("nh1").size());
                    testContext.assertEquals(2, request.headers().get("nh4.1").size());

                    request = (EventbusRpcRequest) context.requests().get(1);
                    testContext.assertFalse(request.headers().containsKey("h1"));
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });
    }

    @Test
    public void testHeaderRemove(TestContext testContext) {

        JsonObject config = new JsonObject()
                .put("header.remove", new JsonArray().add("h1").add("h2"));
        filter = new EventbusRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);
        Multimap<String, String> ebHeaders = ArrayListMultimap.create();
        ebHeaders.put("h1", "h1");
        ebHeaders.put("h4", "h4.1");
        ebHeaders.put("h4", "h4.2");
        ebHeaders.put("h5", "h5");
        apiContext.addRequest(EventbusRpcRequest
                                      .create("a", "add_device", "send_log", EventbusEndpoint
                                                      .REQ_RESP,
                                              ebHeaders, new JsonObject()));


        apiContext.addRequest(EventbusRpcRequest
                                      .create("b", "update_device", "send_log", EventbusEndpoint
                                                      .REQ_RESP,
                                              ArrayListMultimap.create(), new JsonObject()));

        RequestTransformer transformer = RequestTransformer.create("add_device");

        transformer.removeHeader("h3")
                .removeHeader("h4");

        RequestTransformerPlugin plugin = new RequestTransformerPluginImpl()
                .addTransformer(transformer);
        apiContext.apiDefinition().addPlugin(plugin);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.requests());
                    testContext.assertEquals(2, context.requests().size());
                    EventbusRpcRequest request = (EventbusRpcRequest) context.requests().get(0);
                    testContext.assertEquals(1, request.headers().size());
                    testContext.assertFalse(request.headers().containsKey("h1"));
                    testContext.assertFalse(request.headers().containsKey("h2"));
                    testContext.assertFalse(request.headers().containsKey("h3"));
                    testContext.assertFalse(request.headers().containsKey("h4"));
                    testContext.assertTrue(request.headers().containsKey("h5"));
                    testContext.assertEquals(1, request.headers().get("h5").size());

                    request = (EventbusRpcRequest) context.requests().get(1);
                    testContext.assertFalse(request.headers().containsKey("h1"));
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });
    }

    @Test
    public void testHeaderOrder(TestContext testContext) {
        //先删掉某个请求头，replace不起作用，add会是一个新元素
        JsonObject config = new JsonObject()
                .put("header.remove", new JsonArray().add("h1"))
                .put("header.replace", new JsonArray().add("h1:rh1"))
                .put("header.add", new JsonArray().add("h1:ah1"));
        filter = new EventbusRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);

        Multimap<String, String> ebHeaders = ArrayListMultimap.create();
        ebHeaders.put("h1", "h1");
        apiContext.addRequest(EventbusRpcRequest
                                      .create("a", "add_device", "send_log", EventbusEndpoint
                                                      .REQ_RESP,
                                              ebHeaders, new JsonObject()));


        apiContext.addRequest(EventbusRpcRequest
                                      .create("b", "update_device", "send_log", EventbusEndpoint
                                                      .REQ_RESP,
                                              ArrayListMultimap.create(), new JsonObject()));


        RequestTransformer transformer = RequestTransformer.create("add_device");

        transformer.removeHeader("h2")
                .replaceHeader("h2", "rh2")
                .addHeader("h2", "ah2");

        RequestTransformerPlugin plugin = new RequestTransformerPluginImpl()
                .addTransformer(transformer);
        apiContext.apiDefinition().addPlugin(plugin);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.requests());
                    testContext.assertEquals(2, context.requests().size());
                    EventbusRpcRequest request = (EventbusRpcRequest) context.requests().get(0);
                    testContext.assertEquals(2, request.headers().size());
                    testContext.assertTrue(request.headers().containsKey("h1"));
                    testContext.assertTrue(request.headers().containsKey("h2"));
                    testContext.assertEquals("ah1", request.headers().get("h1").iterator().next());
                    testContext.assertEquals("ah2", request.headers().get("h2").iterator().next());

                    request = (EventbusRpcRequest) context.requests().get(1);
                    testContext.assertTrue(request.headers().containsKey("h1"));
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });
    }

    @Test
    public void testBodyAdd(TestContext testContext) {
        JsonObject config = new JsonObject()
                .put("body.add", new JsonArray().add("b1:b1.1").add("b1:b1.2"));
        filter = new EventbusRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);
        Multimap<String, String> ebHeaders = ArrayListMultimap.create();
        apiContext.addRequest(EventbusRpcRequest
                                      .create("a", "add_device", "send_log", EventbusEndpoint
                                                      .REQ_RESP,
                                              ebHeaders, new JsonObject()));


        apiContext.addRequest(EventbusRpcRequest
                                      .create("b", "update_device", "send_log", EventbusEndpoint
                                                      .REQ_RESP,
                                              ArrayListMultimap.create(), new JsonObject()));

        RequestTransformer transformer = RequestTransformer.create("add_device");

        transformer.addBody("b2", "b2").addBody("b1", "b1.3");

        RequestTransformerPlugin plugin = new RequestTransformerPluginImpl()
                .addTransformer(transformer);
        apiContext.apiDefinition().addPlugin(plugin);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.requests());
                    testContext.assertEquals(2, context.requests().size());
                    EventbusRpcRequest request = (EventbusRpcRequest) context.requests().get(0);
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertTrue(request.message().containsKey("b1"));
                    testContext.assertTrue(request.message().containsKey("b2"));
                    testContext.assertEquals("b1.3", request.message().getString("b1"));

                    request = (EventbusRpcRequest) context.requests().get(1);
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertTrue(request.message().containsKey("b1"));
                    testContext.assertFalse(request.message().containsKey("b2"));
                    testContext.assertEquals("b1.2", request.message().getString("b1"));
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });
    }

    @Test
    public void testBodyReplace(TestContext testContext) {

        JsonObject config = new JsonObject()
                .put("body.replace", new JsonArray().add("b1:nb1").add("b2:nb2"));
        filter = new EventbusRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);
        Multimap<String, String> ebHeaders = ArrayListMultimap.create();
        apiContext.addRequest(EventbusRpcRequest
                                      .create("a", "add_device", "send_log", EventbusEndpoint
                                                      .REQ_RESP,
                                              ebHeaders, new JsonObject()
                                                      .put("b1", "b1")
                                                      .put("b4",
                                                           new JsonArray().add("b4.1")
                                                                   .add("b4.2"))));


        apiContext.addRequest(EventbusRpcRequest
                                      .create("b", "update_device", "send_log", EventbusEndpoint
                                                      .REQ_RESP,
                                              ArrayListMultimap.create(), new JsonObject()));

        RequestTransformer transformer = RequestTransformer.create("add_device");

        transformer.replaceBody("b3", "nb3")
                .replaceBody("b4", "nb4")
                .replaceBody("nb4", "nb4.1");

        RequestTransformerPlugin plugin = new RequestTransformerPluginImpl()
                .addTransformer(transformer);
        apiContext.apiDefinition().addPlugin(plugin);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.requests());
                    testContext.assertEquals(2, context.requests().size());
                    EventbusRpcRequest request = (EventbusRpcRequest) context.requests().get(0);
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertFalse(request.message().containsKey("b1"));
                    testContext.assertFalse(request.message().containsKey("b2"));
                    testContext.assertFalse(request.message().containsKey("b3"));
                    testContext.assertFalse(request.message().containsKey("b4"));
                    testContext.assertTrue(request.message().containsKey("nb1"));
                    testContext.assertFalse(request.message().containsKey("nb4"));
                    testContext.assertTrue(request.message().containsKey("nb4.1"));
                    testContext.assertEquals("b1", request.message().getString("nb1"));
                    testContext.assertEquals(2, request.message().getJsonArray("nb4.1").size());

                    request = (EventbusRpcRequest) context.requests().get(1);
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertTrue(request.message().isEmpty());
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });
    }

    @Test
    public void testBodyRemove(TestContext testContext) {

        JsonObject config = new JsonObject()
                .put("body.remove", new JsonArray().add("b1").add("b2"));
        filter = new EventbusRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);

        Multimap<String, String> ebHeaders = ArrayListMultimap.create();
        apiContext.addRequest(EventbusRpcRequest
                                      .create("a", "add_device", "send_log", EventbusEndpoint
                                                      .REQ_RESP,
                                              ebHeaders, new JsonObject()
                                                      .put("b1", "b1")
                                                      .put("b4",
                                                           new JsonArray().add("b4.1").add("b4.2"))
                                                      .put("b5", "b5")));


        apiContext.addRequest(EventbusRpcRequest
                                      .create("b", "update_device", "send_log", EventbusEndpoint
                                                      .REQ_RESP,
                                              ArrayListMultimap.create(), new JsonObject()));


        RequestTransformer transformer = RequestTransformer.create("add_device");

        transformer.removeBody("b3")
                .removeBody("b4");

        RequestTransformerPlugin plugin = new RequestTransformerPluginImpl()
                .addTransformer(transformer);
        apiContext.apiDefinition().addPlugin(plugin);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.requests());
                    testContext.assertEquals(2, context.requests().size());
                    EventbusRpcRequest request = (EventbusRpcRequest) context.requests().get(0);
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertFalse(request.message().containsKey("b1"));
                    testContext.assertFalse(request.message().containsKey("b2"));
                    testContext.assertFalse(request.message().containsKey("b3"));
                    testContext.assertFalse(request.message().containsKey("b4"));
                    testContext.assertTrue(request.message().containsKey("b5"));
                    testContext.assertEquals("b5", request.message().getString("b5"));

                    request = (EventbusRpcRequest) context.requests().get(1);
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertTrue(request.message().isEmpty());
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });
    }

    @Test
    public void testBodyOrder(TestContext testContext) {
        //先删掉某个请求头，replace不起作用，add会是一个新元素
        JsonObject config = new JsonObject()
                .put("body.remove", new JsonArray().add("b1"))
                .put("body.replace", new JsonArray().add("b1:rb1"))
                .put("body.add", new JsonArray().add("b1:ab1"));
        filter = new EventbusRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);

        Multimap<String, String> ebHeaders = ArrayListMultimap.create();
        apiContext.addRequest(EventbusRpcRequest
                                      .create("a", "add_device", "send_log", EventbusEndpoint
                                                      .REQ_RESP,
                                              ebHeaders, new JsonObject()
                                                      .put("b1", "b1")));


        apiContext.addRequest(EventbusRpcRequest
                                      .create("b", "update_device", "send_log", EventbusEndpoint
                                                      .REQ_RESP,
                                              ArrayListMultimap.create(), new JsonObject()));

        RequestTransformer transformer = RequestTransformer.create("add_device");

        transformer.removeBody("b2")
                .replaceBody("b2", "rb2")
                .addBody("b2", "ab2");

        RequestTransformerPlugin plugin = new RequestTransformerPluginImpl()
                .addTransformer(transformer);
        apiContext.apiDefinition().addPlugin(plugin);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.requests());
                    testContext.assertEquals(2, context.requests().size());
                    EventbusRpcRequest request = (EventbusRpcRequest) context.requests().get(0);
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertTrue(request.message().containsKey("b1"));
                    testContext.assertTrue(request.message().containsKey("b2"));
                    testContext.assertEquals("ab1", request.message().getString("b1"));
                    testContext.assertEquals("ab2", request.message().getString("b2"));

                    request = (EventbusRpcRequest) context.requests().get(1);
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertEquals(1, request.message().size());
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
        apiContext.setApiDefinition(definition);
    }

}
