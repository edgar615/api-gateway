package com.github.edgar615.gateway.plugin.transformer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.rpc.http.SimpleHttpRequest;
import com.github.edgar615.gateway.core.utils.Filters;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
@RunWith(VertxUnitRunner.class)
public class HttpRequestTransformerFilterTest {

    private final List<Filter> filters = new ArrayList<>();

    HttpRequestTransformerFilter filter;

    private ApiContext apiContext;

    private Vertx vertx;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();

        filter = new HttpRequestTransformerFilter(new JsonObject());

        filters.clear();
        filters.add(filter);

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
        filter = new HttpRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);
        SimpleHttpRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                                    "add_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.GET)
                .setPath("/");
        apiContext.addRequest(httpRpcRequest);

        httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                  "update_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.POST)
                .setPath("/")
                .setBody(new JsonObject());
        apiContext.addRequest(httpRpcRequest);


        RequestTransformer transformer = RequestTransformer.create("add_device");

        transformer.addHeader("h2", "h2").addHeader("h1", "h1.3");

        RequestTransformerPlugin plugin = new RequestTransformerPluginImpl()
                .addTransformer(transformer);
        apiContext.apiDefinition().addPlugin(plugin);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        AtomicBoolean check1 = new AtomicBoolean();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.requests());
                    testContext.assertEquals(2, context.requests().size());
                    SimpleHttpRequest request = (SimpleHttpRequest) context.requests().get(0);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.params().size());
                    testContext.assertEquals(4, request.headers().size());
                    testContext.assertTrue(request.headers().containsKey("h1"));
                    testContext.assertTrue(request.headers().containsKey("h2"));
                    testContext.assertEquals("h1.1", request.headers().get("h1").iterator().next());
                    testContext.assertEquals(3, request.headers().get("h1").size());
                    testContext.assertEquals(1, request.headers().get("h2").size());
                    testContext.assertNull(request.body());

                    request = (SimpleHttpRequest) context.requests().get(1);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.params().size());
                    testContext.assertTrue(request.headers().containsKey("h1"));
                    testContext.assertEquals(2, request.headers().get("h1").size());
                    testContext.assertNotNull(request.body());
                    check1.set(true);
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });
        Awaitility.await().until(() -> check1.get());

        JsonObject newConfig = new JsonObject()
                .put("header.add", new JsonArray().add("h1:h1.3").add("h1:h1.4"));
        filter.updateConfig(new JsonObject().put("request.transformer", newConfig));
        AtomicBoolean check2 = new AtomicBoolean();
        task = Task.create();
        task.complete(apiContext);
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.requests());
                    testContext.assertEquals(2, context.requests().size());
                    SimpleHttpRequest request = (SimpleHttpRequest) context.requests().get(0);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.params().size());
                    testContext.assertEquals(4, request.headers().size());
                    testContext.assertTrue(request.headers().containsKey("h1"));
                    testContext.assertTrue(request.headers().containsKey("h2"));
                    testContext.assertEquals("h1.3", request.headers().get("h1").iterator().next());
                    testContext.assertEquals(3, request.headers().get("h1").size());
                    testContext.assertEquals(1, request.headers().get("h2").size());
                    testContext.assertNull(request.body());

                    request = (SimpleHttpRequest) context.requests().get(1);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.params().size());
                    testContext.assertTrue(request.headers().containsKey("h1"));
                    testContext.assertEquals(2, request.headers().get("h1").size());
                    testContext.assertNotNull(request.body());
                    check2.set(true);
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });
        Awaitility.await().until(() -> check2.get());
    }

    @Test
    public void testHeaderReplace(TestContext testContext) {

        JsonObject config = new JsonObject()
                .put("header.replace", new JsonArray().add("h1:nh1").add("h2:nh2"));
        filter = new HttpRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);
        SimpleHttpRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                                    "add_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.GET)
                .setPath("/")
                .addHeader("h1", "h1")
                .addHeader("h4", "h4.1")
                .addHeader("h4", "h4.2");
        apiContext.addRequest(httpRpcRequest);

        httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                  "update_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.POST)
                .setPath("/")
                .setBody(new JsonObject());
        apiContext.addRequest(httpRpcRequest);


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
                    SimpleHttpRequest request = (SimpleHttpRequest) context.requests().get(0);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.params().size());
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
                    testContext.assertNull(request.body());

                    request = (SimpleHttpRequest) context.requests().get(1);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.params().size());
                    testContext.assertFalse(request.headers().containsKey("h1"));
                    testContext.assertNotNull(request.body());
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
        filter = new HttpRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);
        SimpleHttpRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                                    "add_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.GET)
                .setPath("/")
                .addHeader("h1", "h1")
                .addHeader("h4", "h4.1")
                .addHeader("h4", "h4.2")
                .addHeader("h5", "h5");
        apiContext.addRequest(httpRpcRequest);

        httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                  "update_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.POST)
                .setPath("/")
                .setBody(new JsonObject());
        apiContext.addRequest(httpRpcRequest);


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
                    SimpleHttpRequest request = (SimpleHttpRequest) context.requests().get(0);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.params().size());
                    testContext.assertEquals(1, request.headers().size());
                    testContext.assertFalse(request.headers().containsKey("h1"));
                    testContext.assertFalse(request.headers().containsKey("h2"));
                    testContext.assertFalse(request.headers().containsKey("h3"));
                    testContext.assertFalse(request.headers().containsKey("h4"));
                    testContext.assertTrue(request.headers().containsKey("h5"));
                    testContext.assertEquals(1, request.headers().get("h5").size());
                    testContext.assertNull(request.body());

                    request = (SimpleHttpRequest) context.requests().get(1);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.params().size());
                    testContext.assertFalse(request.headers().containsKey("h1"));
                    testContext.assertNotNull(request.body());
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
        filter = new HttpRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);
        SimpleHttpRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                                    "add_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.GET)
                .setPath("/")
                .addHeader("h1", "h1");
        apiContext.addRequest(httpRpcRequest);

        httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                  "update_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.POST)
                .setPath("/")
                .setBody(new JsonObject());
        apiContext.addRequest(httpRpcRequest);


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
                    SimpleHttpRequest request = (SimpleHttpRequest) context.requests().get(0);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.params().size());
                    testContext.assertEquals(2, request.headers().size());
                    testContext.assertTrue(request.headers().containsKey("h1"));
                    testContext.assertTrue(request.headers().containsKey("h2"));
                    testContext.assertEquals("ah1", request.headers().get("h1").iterator().next());
                    testContext.assertEquals("ah2", request.headers().get("h2").iterator().next());
                    testContext.assertNull(request.body());

                    request = (SimpleHttpRequest) context.requests().get(1);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.params().size());
                    testContext.assertTrue(request.headers().containsKey("h1"));
                    testContext.assertNotNull(request.body());
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });
    }

    @Test
    public void testQueryAdd(TestContext testContext) {
        JsonObject config = new JsonObject()
                .put("query.add", new JsonArray().add("q1:q1.1").add("q1:q1.2"));
        filter = new HttpRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);
        SimpleHttpRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                                    "add_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.GET)
                .setPath("/");
        apiContext.addRequest(httpRpcRequest);

        httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                  "update_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.POST)
                .setPath("/")
                .setBody(new JsonObject());
        apiContext.addRequest(httpRpcRequest);


        RequestTransformer transformer = RequestTransformer.create("add_device");

        transformer.addParam("q2", "q2").addParam("q1", "q1.3");

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
                    SimpleHttpRequest request = (SimpleHttpRequest) context.requests().get(0);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertEquals(4, request.params().size());
                    testContext.assertTrue(request.params().containsKey("q1"));
                    testContext.assertTrue(request.params().containsKey("q2"));
                    testContext.assertEquals(3, request.params().get("q1").size());
                    testContext.assertEquals(1, request.params().get("q2").size());
                    testContext.assertNull(request.body());

                    request = (SimpleHttpRequest) context.requests().get(1);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertTrue(request.params().containsKey("q1"));
                    testContext.assertEquals(2, request.params().get("q1").size());
                    testContext.assertNotNull(request.body());
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });
    }

    @Test
    public void testQueryReplace(TestContext testContext) {

        JsonObject config = new JsonObject()
                .put("query.replace", new JsonArray().add("q1:nq1").add("q2:nq2"));
        filter = new HttpRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);
        SimpleHttpRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                                    "add_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.GET)
                .setPath("/")
                .addParam("q1", "q1")
                .addParam("q4", "q4.1")
                .addParam("q4", "q4.2");
        apiContext.addRequest(httpRpcRequest);

        httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                  "update_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.POST)
                .setPath("/")
                .setBody(new JsonObject());
        apiContext.addRequest(httpRpcRequest);


        RequestTransformer transformer = RequestTransformer.create("add_device");

        transformer.replaceParam("q3", "nq3")
                .replaceParam("q4", "nq4")
                .replaceParam("nq4", "nq4.1");

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
                    SimpleHttpRequest request = (SimpleHttpRequest) context.requests().get(0);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertEquals(3, request.params().size());
                    testContext.assertFalse(request.params().containsKey("q1"));
                    testContext.assertFalse(request.params().containsKey("q2"));
                    testContext.assertFalse(request.params().containsKey("q3"));
                    testContext.assertFalse(request.params().containsKey("q4"));
                    testContext.assertTrue(request.params().containsKey("nq1"));
                    testContext.assertFalse(request.params().containsKey("nq4"));
                    testContext.assertTrue(request.params().containsKey("nq4.1"));
                    testContext.assertEquals(1, request.params().get("nq1").size());
                    testContext.assertEquals(2, request.params().get("nq4.1").size());
                    testContext.assertNull(request.body());

                    request = (SimpleHttpRequest) context.requests().get(1);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertFalse(request.params().containsKey("q1"));
                    testContext.assertNotNull(request.body());
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });
    }

    @Test
    public void testQueryRemove(TestContext testContext) {

        JsonObject config = new JsonObject()
                .put("query.remove", new JsonArray().add("q1").add("q2"));
        filter = new HttpRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);
        SimpleHttpRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                                    "add_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.GET)
                .setPath("/")
                .addParam("q1", "q1")
                .addParam("q4", "q4.1")
                .addParam("q4", "q4.2")
                .addParam("q5", "q5");
        apiContext.addRequest(httpRpcRequest);

        httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                  "update_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.POST)
                .setPath("/")
                .setBody(new JsonObject());
        apiContext.addRequest(httpRpcRequest);


        RequestTransformer transformer = RequestTransformer.create("add_device");

        transformer.removeParam("q3")
                .removeParam("q4");

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
                    SimpleHttpRequest request = (SimpleHttpRequest) context.requests().get(0);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertEquals(1, request.params().size());
                    testContext.assertFalse(request.params().containsKey("q1"));
                    testContext.assertFalse(request.params().containsKey("q2"));
                    testContext.assertFalse(request.params().containsKey("q3"));
                    testContext.assertFalse(request.params().containsKey("q4"));
                    testContext.assertTrue(request.params().containsKey("q5"));
                    testContext.assertEquals(1, request.params().get("q5").size());
                    testContext.assertNull(request.body());

                    request = (SimpleHttpRequest) context.requests().get(1);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertFalse(request.params().containsKey("q1"));
                    testContext.assertNotNull(request.body());
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });
    }

    @Test
    public void testQueryOrder(TestContext testContext) {
        //先删掉某个请求头，replace不起作用，add会是一个新元素
        JsonObject config = new JsonObject()
                .put("query.remove", new JsonArray().add("q1"))
                .put("query.replace", new JsonArray().add("q1:rq1"))
                .put("query.add", new JsonArray().add("q1:aq1"));
        filter = new HttpRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);
        SimpleHttpRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                                    "add_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.GET)
                .setPath("/")
                .addParam("q1", "q1");
        apiContext.addRequest(httpRpcRequest);

        httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                  "update_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.POST)
                .setPath("/")
                .setBody(new JsonObject());
        apiContext.addRequest(httpRpcRequest);


        RequestTransformer transformer = RequestTransformer.create("add_device");

        transformer.removeParam("q2")
                .replaceParam("q2", "rq2")
                .addParam("q2", "aq2");

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
                    SimpleHttpRequest request = (SimpleHttpRequest) context.requests().get(0);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertEquals(2, request.params().size());
                    testContext.assertTrue(request.params().containsKey("q1"));
                    testContext.assertTrue(request.params().containsKey("q2"));
                    testContext.assertEquals("aq1", request.params().get("q1").iterator().next());
                    testContext.assertEquals("aq2", request.params().get("q2").iterator().next());
                    testContext.assertNull(request.body());

                    request = (SimpleHttpRequest) context.requests().get(1);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertTrue(request.params().containsKey("q1"));
                    testContext.assertNotNull(request.body());
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
        filter = new HttpRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);
        SimpleHttpRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                                    "add_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.GET)
                .setPath("/")
                .setBody(new JsonObject());
        apiContext.addRequest(httpRpcRequest);

        httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                  "update_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.POST)
                .setPath("/")
                .setBody(new JsonObject());
        apiContext.addRequest(httpRpcRequest);


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
                    SimpleHttpRequest request = (SimpleHttpRequest) context.requests().get(0);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertEquals(0, request.params().size());
                    testContext.assertTrue(request.body().containsKey("b1"));
                    testContext.assertTrue(request.body().containsKey("b2"));
                    testContext.assertEquals("b1.3", request.body().getString("b1"));

                    request = (SimpleHttpRequest) context.requests().get(1);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertEquals(0, request.params().size());
                    testContext.assertTrue(request.body().containsKey("b1"));
                    testContext.assertFalse(request.body().containsKey("b2"));
                    testContext.assertEquals("b1.2", request.body().getString("b1"));
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
        filter = new HttpRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);
        SimpleHttpRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                                    "add_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.GET)
                .setPath("/")
                .setBody(new JsonObject()
                                 .put("b1", "b1")
                                 .put("b4", new JsonArray().add("b4.1").add("b4.2")));
        apiContext.addRequest(httpRpcRequest);

        httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                  "update_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.POST)
                .setPath("/")
                .setBody(new JsonObject());
        apiContext.addRequest(httpRpcRequest);


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
                    SimpleHttpRequest request = (SimpleHttpRequest) context.requests().get(0);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertEquals(0, request.params().size());
                    testContext.assertFalse(request.body().containsKey("b1"));
                    testContext.assertFalse(request.body().containsKey("b2"));
                    testContext.assertFalse(request.body().containsKey("b3"));
                    testContext.assertFalse(request.body().containsKey("b4"));
                    testContext.assertTrue(request.body().containsKey("nb1"));
                    testContext.assertFalse(request.body().containsKey("nb4"));
                    testContext.assertTrue(request.body().containsKey("nb4.1"));
                    testContext.assertEquals("b1", request.body().getString("nb1"));
                    testContext.assertEquals(2, request.body().getJsonArray("nb4.1").size());

                    request = (SimpleHttpRequest) context.requests().get(1);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertEquals(0, request.params().size());
                    testContext.assertTrue(request.body().isEmpty());
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
        filter = new HttpRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);
        SimpleHttpRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                                    "add_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.GET)
                .setPath("/")
                .setBody(new JsonObject()
                                 .put("b1", "b1")
                                 .put("b4", new JsonArray().add("b4.1").add("b4.2"))
                                 .put("b5", "b5"));
        apiContext.addRequest(httpRpcRequest);

        httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                  "update_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.POST)
                .setPath("/")
                .setBody(new JsonObject());
        apiContext.addRequest(httpRpcRequest);


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
                    SimpleHttpRequest request = (SimpleHttpRequest) context.requests().get(0);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertEquals(0, request.params().size());
                    testContext.assertFalse(request.body().containsKey("b1"));
                    testContext.assertFalse(request.body().containsKey("b2"));
                    testContext.assertFalse(request.body().containsKey("b3"));
                    testContext.assertFalse(request.body().containsKey("b4"));
                    testContext.assertTrue(request.body().containsKey("b5"));
                    testContext.assertEquals("b5", request.body().getString("b5"));

                    request = (SimpleHttpRequest) context.requests().get(1);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertTrue(request.body().isEmpty());
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
        filter = new HttpRequestTransformerFilter(
                new JsonObject().put("request.transformer", config));
        filters.clear();
        filters.add(filter);
        SimpleHttpRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                                    "add_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.GET)
                .setPath("/")
                .setBody(new JsonObject()
                                 .put("b1", "b1"));
        apiContext.addRequest(httpRpcRequest);

        httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                  "update_device");
        httpRpcRequest.setHost("localhost")
                .setPort(8080)
                .setHttpMethod(HttpMethod.POST)
                .setPath("/")
                .setBody(new JsonObject());
        apiContext.addRequest(httpRpcRequest);


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
                    SimpleHttpRequest request = (SimpleHttpRequest) context.requests().get(0);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertEquals(0, request.params().size());
                    testContext.assertTrue(request.body().containsKey("b1"));
                    testContext.assertTrue(request.body().containsKey("b2"));
                    testContext.assertEquals("ab1", request.body().getString("b1"));
                    testContext.assertEquals("ab2", request.body().getString("b2"));

                    request = (SimpleHttpRequest) context.requests().get(1);
                    testContext.assertEquals("localhost", request.host());
                    testContext.assertEquals(8080, request.port());
                    testContext.assertEquals(0, request.headers().size());
                    testContext.assertEquals(0, request.params().size());
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
