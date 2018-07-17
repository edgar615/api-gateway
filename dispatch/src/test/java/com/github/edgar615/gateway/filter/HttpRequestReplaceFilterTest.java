package com.github.edgar615.gateway.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.rpc.http.HttpRpcRequest;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
@RunWith(VertxUnitRunner.class)
public class HttpRequestReplaceFilterTest {

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
  public void testReplaceParamsFromHeader(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();

    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h1", "h1.1");
    headers.put("h1", "h1.2");
    headers.put("h2", "h2");

    JsonObject jsonObject = new JsonObject();

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                             "add_device")
            .setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .addParam("q1", "$header.h1")
            .addParam("q2", "$header.h2")
            .addParam("q3", "$header.h3")
            .addParam("foo", "bar");
    apiContext.addRequest(httpRpcRequest);

    Filter filter =
            Filter.create(HttpRequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.requests());
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              System.out.println(request.params());
              testContext.assertEquals(4, request.params().size());
              testContext.assertEquals(2, request.params().get("q1").size());
              testContext.assertEquals("bar", request.params().get("foo").iterator().next());
              testContext.assertEquals("h1.1", request.params().get("q1").iterator().next());
              testContext.assertEquals("h2", request.params().get("q2").iterator().next());
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });

  }

  @Test
  public void testReplaceParamsFromQuery(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q1", "q1.1");
    params.put("q1", "q1.2");
    params.put("q2", "q2");

    Multimap<String, String> headers = ArrayListMultimap.create();

    JsonObject jsonObject = new JsonObject();

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                             "add_device")
            .setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .addParam("foo", "bar")
            .addParam("q1", "$query.q1")
            .addParam("q2", "$query.q2")
            .addParam("q3", "$query.q3");
    apiContext.addRequest(httpRpcRequest);

    Filter filter =
            Filter.create(HttpRequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.requests());
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              System.out.println(request.params());
              testContext.assertEquals(4, request.params().size());
              testContext.assertEquals(2, request.params().get("q1").size());
              testContext.assertEquals("bar", request.params().get("foo").iterator().next());
              testContext.assertEquals("q1.1", request.params().get("q1").iterator().next());
              testContext.assertEquals("q2", request.params().get("q2").iterator().next());
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });

  }

  @Test
  public void testReplaceParamsFromBody(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();

    Multimap<String, String> headers = ArrayListMultimap.create();

    JsonObject jsonObject = new JsonObject()
            .put("b1", new JsonArray().add("b1.1").add("b1.2"))
            .put("b2", "b2")
            .put("obj", new JsonObject().put("foo", "bar"));

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                             "add_device")
            .setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .addParam("foo", "bar")
            .addParam("q1", "$body.b1")
            .addParam("q2", "$body.b2")
            .addParam("q3", "$body.b3")
            .addParam("q4", "$body.obj");
    apiContext.addRequest(httpRpcRequest);

    Filter filter =
            Filter.create(HttpRequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.requests());
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              System.out.println(request.params());
              testContext.assertEquals(5, request.params().size());
              testContext.assertEquals(2, request.params().get("q1").size());
              testContext.assertEquals("bar", request.params().get("foo").iterator().next());
              testContext.assertEquals("b1.1", request.params().get("q1").iterator().next());
              testContext.assertEquals("b2", request.params().get("q2").iterator().next());
              testContext.assertEquals("{\"foo\":\"bar\"}", request.params().get("q4").iterator
                      ().next());
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });

  }


  @Test
  public void testReplaceParamsFromUser(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();

    Multimap<String, String> headers = ArrayListMultimap.create();

    JsonObject jsonObject = new JsonObject();

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);
    apiContext.setPrincipal(new JsonObject()
                                    .put("u1", new JsonArray().add("u1.1").add("u1.2"))
                                    .put("u2", "u2")
                                    .put("obj", new JsonObject().put("foo", "bar")));

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                             "add_device")
            .setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .addParam("foo", "bar")
            .addParam("q1", "$user.u1")
            .addParam("q2", "$user.u2")
            .addParam("q3", "$user.u3")
            .addParam("q4", "$user.obj");
    apiContext.addRequest(httpRpcRequest);

    Filter filter =
            Filter.create(HttpRequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.requests());
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              System.out.println(request.params());
              testContext.assertEquals(5, request.params().size());
              testContext.assertEquals(2, request.params().get("q1").size());
              testContext.assertEquals("bar", request.params().get("foo").iterator().next());
              testContext.assertEquals("u1.1", request.params().get("q1").iterator().next());
              testContext.assertEquals("u2", request.params().get("q2").iterator().next());
              testContext.assertEquals("{\"foo\":\"bar\"}", request.params().get("q4").iterator
                      ().next());
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });

  }

  @Test
  public void testReplaceParamsFromVar(TestContext testContext) {
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

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                             "add_device")
            .setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .addParam("foo", "bar")
            .addParam("q1", "$var.v1")
            .addParam("q2", "$var.v2")
            .addParam("q3", "$var.v3")
            .addParam("q4", "$var.v4")
            .addParam("q5", "$var.v5")
            .addParam("q6", "$var.v6");

    apiContext.addRequest(httpRpcRequest);

    Filter filter =
            Filter.create(HttpRequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.requests());
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              System.out.println(request.params());
              testContext.assertEquals(9, request.params().size());
              testContext.assertEquals(2, request.params().get("q1").size());
              testContext.assertEquals("bar", request.params().get("foo").iterator().next());
              testContext.assertEquals("v1.1", request.params().get("q1").iterator().next());
              testContext.assertEquals("v2", request.params().get("q2").iterator().next());
              testContext.assertEquals("{\"foo\":\"bar\"}", request.params().get("q4").iterator
                      ().next());
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });

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

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                             "add_device")
            .setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .addHeader("h1", "$header.h1")
            .addHeader("h2", "$header.h2")
            .addHeader("h3", "$header.h3")
            .addHeader("foo", "bar");
    apiContext.addRequest(httpRpcRequest);

    Filter filter =
            Filter.create(HttpRequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.requests());
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              testContext.assertEquals(4, request.headers().size());
              testContext.assertEquals(2, request.headers().get("h1").size());
              testContext.assertEquals("bar", request.headers().get("foo").iterator().next());
              testContext.assertEquals("h1.1", request.headers().get("h1").iterator().next());
              testContext.assertEquals("h2", request.headers().get("h2").iterator().next());
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

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                             "add_device")
            .setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .addHeader("foo", "bar")
            .addHeader("h1", "$query.q1")
            .addHeader("h2", "$query.q2")
            .addHeader("h3", "$query.q3");
    apiContext.addRequest(httpRpcRequest);

    Filter filter =
            Filter.create(HttpRequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.requests());
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              testContext.assertEquals(4, request.headers().size());
              testContext.assertEquals(2, request.headers().get("h1").size());
              testContext.assertEquals("bar", request.headers().get("foo").iterator().next());
              testContext.assertEquals("q1.1", request.headers().get("h1").iterator().next());
              testContext.assertEquals("q2", request.headers().get("h2").iterator().next());
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

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                             "add_device")
            .setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .addHeader("foo", "bar")
            .addHeader("h1", "$body.b1")
            .addHeader("h2", "$body.b2")
            .addHeader("h3", "$body.b3")
            .addHeader("h4", "$body.obj");
    apiContext.addRequest(httpRpcRequest);

    Filter filter =
            Filter.create(HttpRequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.requests());
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              testContext.assertEquals(5, request.headers().size());
              testContext.assertEquals(2, request.headers().get("h1").size());
              testContext.assertEquals("bar", request.headers().get("foo").iterator().next());
              testContext.assertEquals("b1.1", request.headers().get("h1").iterator().next());
              testContext.assertEquals("b2", request.headers().get("h2").iterator().next());
              testContext.assertEquals("{\"foo\":\"bar\"}", request.headers().get("h4").iterator
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

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                             "add_device")
            .setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .addHeader("foo", "bar")
            .addHeader("h1", "$user.u1")
            .addHeader("h2", "$user.u2")
            .addHeader("h3", "$user.u3")
            .addHeader("h4", "$user.obj");
    apiContext.addRequest(httpRpcRequest);

    Filter filter =
            Filter.create(HttpRequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.requests());
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              testContext.assertEquals(5, request.headers().size());
              testContext.assertEquals(2, request.headers().get("h1").size());
              testContext.assertEquals("bar", request.headers().get("foo").iterator().next());
              testContext.assertEquals("u1.1", request.headers().get("h1").iterator().next());
              testContext.assertEquals("u2", request.headers().get("h2").iterator().next());
              testContext.assertEquals("{\"foo\":\"bar\"}", request.headers().get("h4").iterator
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

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                             "add_device")
            .setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .addHeader("foo", "bar")
            .addHeader("h1", "$var.v1")
            .addHeader("h2", "$var.v2")
            .addHeader("h3", "$var.v3")
            .addHeader("h4", "$var.v4")
            .addHeader("h5", "$var.v5")
            .addHeader("h6", "$var.v6");

    apiContext.addRequest(httpRpcRequest);

    Filter filter =
            Filter.create(HttpRequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.requests());
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              testContext.assertEquals(9, request.headers().size());
              testContext.assertEquals(2, request.headers().get("h1").size());
              testContext.assertEquals("bar", request.headers().get("foo").iterator().next());
              testContext.assertEquals("v1.1", request.headers().get("h1").iterator().next());
              testContext.assertEquals("v2", request.headers().get("h2").iterator().next());
              testContext.assertEquals("{\"foo\":\"bar\"}", request.headers().get("h4").iterator
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

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                             "add_device")
            .setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .setBody(new JsonObject()
                             .put("b1", "$header.h1")
                             .put("b2", "$header.h2")
                             .put("b3", "$header.h3")
                             .put("foo", "bar"));
    apiContext.addRequest(httpRpcRequest);

    Filter filter =
            Filter.create(HttpRequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.requests());
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              testContext.assertEquals(3, request.body().size());
              testContext.assertEquals(2, request.body().getJsonArray("b1").size());
              testContext.assertEquals("bar", request.body().getString("foo"));
              testContext.assertEquals("h1.1", request.body().getJsonArray("b1").iterator().next());
              testContext.assertEquals("h2", request.body().getString("b2"));
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

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                             "add_device")
            .setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .setBody(new JsonObject()
                             .put("b1", "$query.q1")
                             .put("b2", "$query.q2")
                             .put("b3", "$query.q3")
                             .put("foo", "bar"));
    apiContext.addRequest(httpRpcRequest);

    Filter filter =
            Filter.create(HttpRequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.requests());
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              testContext.assertEquals(3, request.body().size());
              testContext.assertEquals(2, request.body().getJsonArray("b1").size());
              testContext.assertEquals("bar", request.body().getString("foo"));
              testContext.assertEquals("q1.1", request.body().getJsonArray("b1").iterator().next());
              testContext.assertEquals("q2", request.body().getString("b2"));
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

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                             "add_device")
            .setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .setBody(new JsonObject()
                             .put("b1", "$body.b1")
                             .put("b2", "$body.b2")
                             .put("b3", "$body.b3")
                             .put("foo", "bar"));
    apiContext.addRequest(httpRpcRequest);

    Filter filter =
            Filter.create(HttpRequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.requests());
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              testContext.assertEquals(3, request.body().size());
              testContext.assertEquals(2, request.body().getJsonArray("b1").size());
              testContext.assertEquals("bar", request.body().getString("foo"));
              testContext.assertEquals("b1.1", request.body().getJsonArray("b1").iterator().next());
              testContext.assertEquals("b2", request.body().getString("b2"));
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

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                             "add_device")
            .setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .setBody(new JsonObject()
                             .put("b1", "$user.u1")
                             .put("b2", "$user.u2")
                             .put("b3", "$user.u3")
                             .put("foo", "bar"));
    apiContext.addRequest(httpRpcRequest);

    Filter filter =
            Filter.create(HttpRequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.requests());
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              testContext.assertEquals(3, request.body().size());
              testContext.assertEquals(2, request.body().getJsonArray("b1").size());
              testContext.assertEquals("bar", request.body().getString("foo"));
              testContext.assertEquals("u1.1", request.body().getJsonArray("b1").iterator().next());
              testContext.assertEquals("u2", request.body().getString("b2"));
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

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                             "add_device")
            .setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .setBody(new JsonObject()
                             .put("foo", "bar")
                             .put("b1", "$var.v1")
                             .put("b2", "$var.v2")
                             .put("b3", "$var.v3")
                             .put("b4", "$var.v4")
                             .put("b5", "$var.v5")
                             .put("b6", "$var.v6"));


    apiContext.addRequest(httpRpcRequest);

    Filter filter =
            Filter.create(HttpRequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.requests());
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              testContext.assertEquals(7, request.body().size());
              testContext.assertEquals(2, request.body().getJsonArray("b1").size());
              testContext.assertEquals("bar", request.body().getString("foo"));
              testContext.assertEquals("v1.1", request.body().getJsonArray("b1").iterator().next());
              testContext.assertEquals("v2", request.body().getString("b2"));
              testContext.assertEquals(1, request.body().getJsonObject("b4").size());
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });

  }

  @Test
  public void testReplacePath(TestContext testContext) {
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("foo", "bar");

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q1", "q1.1");
    params.put("q1", "q1.2");

    JsonObject jsonObject = new JsonObject()
            .put("type", 1)
            .put("obj", new JsonObject()
                    .put("userId", 1)
                    .put("username", "edgar")
                    .put("q1", "$query.q1"))
            .put("arr", new JsonArray().add(1).add("2"));

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                             "add_device")
            .setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/test/$var.foo/$body.type/$user.userId/$header.foo/$query.q1");
    apiContext.addRequest(httpRpcRequest);

    apiContext.addVariable("foo", "var_bar");
    apiContext.setPrincipal(new JsonObject().put("userId", 1));

    Filter filter =
            Filter.create(HttpRequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.requests());
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              testContext.assertEquals("/test/var_bar/1/1/bar/q1.1", request.path());

              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });

  }


}
