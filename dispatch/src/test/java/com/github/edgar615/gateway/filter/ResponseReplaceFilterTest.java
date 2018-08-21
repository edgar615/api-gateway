package com.github.edgar615.gateway.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.dispatch.Result;
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
public class ResponseReplaceFilterTest {

    private final List<Filter> filters = new ArrayList<>();

    private Vertx vertx;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();

        filters.clear();
    }

    @After
    public void tearDown(TestContext testContext) {
        vertx.close(testContext.asyncAssertSuccess());
    }

    @Test
    public void testReplaceHeadersFromHeader(TestContext testContext) {
        Multimap<String, String> params = ArrayListMultimap.create();

        Multimap<String, String> headers = ArrayListMultimap.create();
        headers.put("h1", "h1.1");
        headers.put("h1", "h1.2");
        headers.put("h2", "h2");

        JsonObject jsonObject = new JsonObject();

        ApiContext apiContext =
                ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

        Result result = Result.createJsonArray(200, new JsonArray(), ArrayListMultimap.create());
        result
                .addHeader("h1", "$header.h1")
                .addHeader("h2", "$header.h2")
                .addHeader("h3", "$header.h3")
                .addHeader("foo", "bar");
        apiContext.setResult(result);

        Filter filter =
                Filter.create(ResponseReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
        filters.add(filter);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.result());
                    testContext.assertEquals(4, context.result().headers().size());
                    testContext.assertEquals(2, context.result().headers().get("h1").size());
                    testContext
                            .assertEquals("bar",
                                          context.result().headers().get("foo").iterator().next());
                    testContext
                            .assertEquals("h1.1",
                                          context.result().headers().get("h1").iterator().next());
                    testContext
                            .assertEquals("h2",
                                          context.result().headers().get("h2").iterator().next());
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });

    }

    @Test
    public void testReplaceHeadersFromQuery(TestContext testContext) {
        Multimap<String, String> params = ArrayListMultimap.create();
        params.put("q1", "q1.1");
        params.put("q1", "q1.2");
        params.put("q2", "q2");

        Multimap<String, String> headers = ArrayListMultimap.create();

        JsonObject jsonObject = new JsonObject();

        ApiContext apiContext =
                ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

        Result result = Result.createJsonArray(200, new JsonArray(), ArrayListMultimap.create());
        result
                .addHeader("foo", "bar")
                .addHeader("h1", "$query.q1")
                .addHeader("h2", "$query.q2")
                .addHeader("h3", "$query.q3");
        apiContext.setResult(result);

        Filter filter =
                Filter.create(ResponseReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
        filters.add(filter);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.result());
                    testContext.assertEquals(4, context.result().headers().size());
                    testContext.assertEquals(2, context.result().headers().get("h1").size());
                    testContext
                            .assertEquals("bar",
                                          context.result().headers().get("foo").iterator().next());
                    testContext.assertEquals("q1.1", context.result().headers().get("h1").iterator()
                            .next());
                    testContext
                            .assertEquals("q2", context.result().headers().get("h2").iterator().next
                                    ());
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });

    }

    @Test
    public void testReplaceHeadersFromBody(TestContext testContext) {
        Multimap<String, String> params = ArrayListMultimap.create();

        Multimap<String, String> headers = ArrayListMultimap.create();

        JsonObject jsonObject = new JsonObject()
                .put("b1", new JsonArray().add("b1.1").add("b1.2"))
                .put("b2", "b2")
                .put("obj", new JsonObject().put("foo", "bar"));

        ApiContext apiContext =
                ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);
        Result result = Result.createJsonArray(200, new JsonArray(), ArrayListMultimap.create());
        result
                .addHeader("foo", "bar")
                .addHeader("h1", "$body.b1")
                .addHeader("h2", "$body.b2")
                .addHeader("h3", "$body.b3")
                .addHeader("h4", "$body.obj");
        apiContext.setResult(result);

        Filter filter =
                Filter.create(ResponseReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
        filters.add(filter);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.result());
                    testContext.assertEquals(5, context.result().headers().size());
                    testContext.assertEquals(2, context.result().headers().get("h1").size());
                    testContext
                            .assertEquals("bar",
                                          context.result().headers().get("foo").iterator().next());
                    testContext
                            .assertEquals("b1.1",
                                          context.result().headers().get("h1").iterator().next());
                    testContext
                            .assertEquals("b2",
                                          context.result().headers().get("h2").iterator().next());
                    testContext.assertEquals("{\"foo\":\"bar\"}",
                                             context.result().headers().get("h4").iterator
                                                     ().next());
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });

    }


    @Test
    public void testReplaceHeadersFromUser(TestContext testContext) {
        Multimap<String, String> params = ArrayListMultimap.create();

        Multimap<String, String> headers = ArrayListMultimap.create();

        JsonObject jsonObject = new JsonObject();

        ApiContext apiContext =
                ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);
        apiContext.setPrincipal(new JsonObject()
                                        .put("u1", new JsonArray().add("u1.1").add("u1.2"))
                                        .put("u2", "u2")
                                        .put("obj", new JsonObject().put("foo", "bar")));

        Result result = Result.createJsonArray(200, new JsonArray(), ArrayListMultimap.create());
        result
                .addHeader("foo", "bar")
                .addHeader("h1", "$user.u1")
                .addHeader("h2", "$user.u2")
                .addHeader("h3", "$user.u3")
                .addHeader("h4", "$user.obj");
        apiContext.setResult(result);


        Filter filter =
                Filter.create(ResponseReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
        filters.add(filter);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.result());
                    testContext.assertEquals(5, context.result().headers().size());
                    testContext.assertEquals(2, context.result().headers().get("h1").size());
                    testContext
                            .assertEquals("bar",
                                          context.result().headers().get("foo").iterator().next());
                    testContext
                            .assertEquals("u1.1",
                                          context.result().headers().get("h1").iterator().next());
                    testContext
                            .assertEquals("u2",
                                          context.result().headers().get("h2").iterator().next());
                    testContext.assertEquals("{\"foo\":\"bar\"}",
                                             context.result().headers().get("h4").iterator
                                                     ().next());
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });

    }


    @Test
    public void testReplaceHeadersFromVar(TestContext testContext) {
        Multimap<String, String> params = ArrayListMultimap.create();

        Multimap<String, String> headers = ArrayListMultimap.create();

        JsonObject jsonObject = new JsonObject();

        ApiContext apiContext =
                ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);
        apiContext.addVariable("v1", new JsonArray().add("v1.1").add("v1.2"))
                .addVariable("v2", "v2")
                .addVariable("v3", 3)
                .addVariable("v4", new JsonObject().put("foo", "bar"))
                .addVariable("v5", Lists.newArrayList("v5.1", 5))
                .addVariable("v6", ImmutableMap.of("v6.k", "v6.v"));

        Result result = Result.createJsonArray(200, new JsonArray(), ArrayListMultimap.create());
        result
                .addHeader("foo", "bar")
                .addHeader("h1", "$var.v1")
                .addHeader("h2", "$var.v2")
                .addHeader("h3", "$var.v3")
                .addHeader("h4", "$var.v4")
                .addHeader("h5", "$var.v5")
                .addHeader("h6", "$var.v6");
        apiContext.setResult(result);

        Filter filter =
                Filter.create(ResponseReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
        filters.add(filter);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.result());
                    testContext.assertEquals(9, context.result().headers().size());
                    testContext.assertEquals(2, context.result().headers().get("h1").size());
                    testContext
                            .assertEquals("bar",
                                          context.result().headers().get("foo").iterator().next());
                    testContext
                            .assertEquals("v1.1",
                                          context.result().headers().get("h1").iterator().next());
                    testContext
                            .assertEquals("v2",
                                          context.result().headers().get("h2").iterator().next());
                    testContext.assertEquals("{\"foo\":\"bar\"}",
                                             context.result().headers().get("h4").iterator
                                                     ().next());
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });

    }


    @Test
    public void testReplaceBodyFromHeader(TestContext testContext) {
        Multimap<String, String> params = ArrayListMultimap.create();

        Multimap<String, String> headers = ArrayListMultimap.create();
        headers.put("h1", "h1.1");
        headers.put("h1", "h1.2");
        headers.put("h2", "h2");

        JsonObject jsonObject = new JsonObject();

        ApiContext apiContext =
                ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);
        Result result = Result.createJsonObject(200, new JsonObject()
                .put("b1", "$header.h1")
                .put("b2", "$header.h2")
                .put("b3", "$header.h3")
                .put("foo", "bar"), ArrayListMultimap.create());
        apiContext.setResult(result);

        Filter filter =
                Filter.create(ResponseReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
        filters.add(filter);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.result());
                    testContext.assertEquals(3, context.result().responseObject().size());
                    testContext
                            .assertEquals(2, context.result().responseObject().getJsonArray("b1")
                                    .size());
                    testContext.assertEquals("bar",
                                             context.result().responseObject().getString("foo"));
                    testContext
                            .assertEquals("h1.1",
                                          context.result().responseObject().getJsonArray("b1")
                                                  .iterator()
                                                  .next());
                    testContext
                            .assertEquals("h2", context.result().responseObject().getString("b2"));
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });

    }

    @Test
    public void testReplaceBodyFromQuery(TestContext testContext) {
        Multimap<String, String> params = ArrayListMultimap.create();
        params.put("q1", "q1.1");
        params.put("q1", "q1.2");
        params.put("q2", "q2");

        Multimap<String, String> headers = ArrayListMultimap.create();

        JsonObject jsonObject = new JsonObject();

        ApiContext apiContext =
                ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

        Result result = Result.createJsonObject(200, new JsonObject()
                .put("b1", "$query.q1")
                .put("b2", "$query.q2")
                .put("b3", "$query.q3")
                .put("foo", "bar"), ArrayListMultimap.create());
        apiContext.setResult(result);
        Filter filter =
                Filter.create(ResponseReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
        filters.add(filter);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.result());
                    testContext.assertEquals(3, context.result().responseObject().size());
                    testContext
                            .assertEquals(2, context.result().responseObject().getJsonArray("b1")
                                    .size());
                    testContext.assertEquals("bar",
                                             context.result().responseObject().getString("foo"));
                    testContext
                            .assertEquals("q1.1",
                                          context.result().responseObject().getJsonArray("b1")
                                                  .iterator()
                                                  .next());
                    testContext
                            .assertEquals("q2", context.result().responseObject().getString("b2"));
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });

    }

    @Test
    public void testReplaceBodyFromBody(TestContext testContext) {
        Multimap<String, String> params = ArrayListMultimap.create();

        Multimap<String, String> headers = ArrayListMultimap.create();

        JsonObject jsonObject = new JsonObject()
                .put("b1", new JsonArray().add("b1.1").add("b1.2"))
                .put("b2", "b2")
                .put("obj", new JsonObject().put("foo", "bar"));

        ApiContext apiContext =
                ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

        Result result = Result.createJsonObject(200, new JsonObject()
                .put("b1", "$body.b1")
                .put("b2", "$body.b2")
                .put("b3", "$body.b3")
                .put("foo", "bar"), ArrayListMultimap.create());
        apiContext.setResult(result);
        Filter filter =
                Filter.create(ResponseReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
        filters.add(filter);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.result());
                    testContext.assertEquals(3, context.result().responseObject().size());
                    testContext
                            .assertEquals(2, context.result().responseObject().getJsonArray("b1")
                                    .size());
                    testContext.assertEquals("bar",
                                             context.result().responseObject().getString("foo"));
                    testContext
                            .assertEquals("b1.1",
                                          context.result().responseObject().getJsonArray("b1")
                                                  .iterator()
                                                  .next());
                    testContext
                            .assertEquals("b2", context.result().responseObject().getString("b2"));
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });

    }


    @Test
    public void testReplaceBodyFromUser(TestContext testContext) {
        Multimap<String, String> params = ArrayListMultimap.create();

        Multimap<String, String> headers = ArrayListMultimap.create();

        JsonObject jsonObject = new JsonObject();

        ApiContext apiContext =
                ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);
        apiContext.setPrincipal(new JsonObject()
                                        .put("u1", new JsonArray().add("u1.1").add("u1.2"))
                                        .put("u2", "u2")
                                        .put("obj", new JsonObject().put("foo", "bar")));

        Result result = Result.createJsonObject(200, new JsonObject()
                .put("b1", "$user.u1")
                .put("b2", "$user.u2")
                .put("b3", "$user.u3")
                .put("foo", "bar"), ArrayListMultimap.create());
        apiContext.setResult(result);
        Filter filter =
                Filter.create(ResponseReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
        filters.add(filter);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.result());
                    testContext.assertEquals(3, context.result().responseObject().size());
                    testContext
                            .assertEquals(2, context.result().responseObject().getJsonArray("b1")
                                    .size());
                    testContext.assertEquals("bar",
                                             context.result().responseObject().getString("foo"));
                    testContext
                            .assertEquals("u1.1",
                                          context.result().responseObject().getJsonArray("b1")
                                                  .iterator()
                                                  .next());
                    testContext
                            .assertEquals("u2", context.result().responseObject().getString("b2"));
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });

    }

    @Test
    public void testReplaceBodyFromVar(TestContext testContext) {
        Multimap<String, String> params = ArrayListMultimap.create();

        Multimap<String, String> headers = ArrayListMultimap.create();

        JsonObject jsonObject = new JsonObject();

        ApiContext apiContext =
                ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);
        apiContext.addVariable("v1", new JsonArray().add("v1.1").add("v1.2"))
                .addVariable("v2", "v2")
                .addVariable("v3", 3)
                .addVariable("v4", new JsonObject().put("foo", "bar"))
                .addVariable("v5", Lists.newArrayList("v5.1", 5))
                .addVariable("v6", ImmutableMap.of("v6.k", "v6.v"));

        Result result = Result.createJsonObject(200, new JsonObject()
                .put("foo", "bar")
                .put("b1", "$var.v1")
                .put("b2", "$var.v2")
                .put("b3", "$var.v3")
                .put("b4", "$var.v4")
                .put("b5", "$var.v5")
                .put("b6", "$var.v6"), ArrayListMultimap.create());
        apiContext.setResult(result);

        Filter filter =
                Filter.create(ResponseReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
        filters.add(filter);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);
        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    System.out.println(context.requests());
                    testContext.assertEquals(7, context.result().responseObject().size());
                    testContext
                            .assertEquals(2, context.result().responseObject().getJsonArray("b1")
                                    .size());
                    testContext.assertEquals("bar",
                                             context.result().responseObject().getString("foo"));
                    testContext
                            .assertEquals("v1.1",
                                          context.result().responseObject().getJsonArray("b1")
                                                  .iterator()
                                                  .next());
                    testContext
                            .assertEquals("v2", context.result().responseObject().getString("b2"));
                    testContext
                            .assertEquals(1, context.result().responseObject().getJsonObject("b4")
                                    .size());
                    async.complete();
                }).onFailure(t -> {
            t.printStackTrace();
            testContext.fail();
        });

    }


}
