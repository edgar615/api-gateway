package com.github.edgar615.direwolves.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.rpc.http.HttpRpcRequest;
import com.github.edgar615.direwolves.core.rpc.http.SimpleHttpRequest;
import com.github.edgar615.direwolves.core.utils.Filters;
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


  //分割线
  @Test
  public void testReplaceParams(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("foo", "bar");

    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h1", "h1.1");
    headers.put("h1", "h1.2");

    JsonObject jsonObject = new JsonObject()
            .put("type", 1)
            .put("obj", new JsonObject()
                    .put("userId", 1)
                    .put("username", "edgar")
                    .put("h1", "$header.h1"))
            .put("arr", new JsonArray().add(1).add("2").add("$header.h1"));

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

    HttpRpcRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                             "add_device")
            .setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .addParam("foo", "bar")
            .addParam("q1", "$header.h1")
            .addParam("q2", "$var.foo")
            .addParam("q3", "$body.type")
            .addParam("q4", "$user.userId")
            .addParam("q5", "$var.bar")
            .addParam("q6", "$body.obj")
            .addParam("q7", "$body.arr");
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
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              System.out.println(request.params().get("q6"));
              System.out.println(request.params().get("q7"));
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              System.out.println(request.params());
              testContext.assertEquals(7, request.params().keySet().size());
              testContext.assertEquals("h1.1", request.params().get("q1").iterator().next());
              testContext.assertEquals("var_bar", request.params().get("q2").iterator().next());
              testContext.assertEquals("1", request.params().get("q3").iterator().next());
              testContext.assertEquals("1", request.params().get("q4").iterator().next());
              testContext.assertTrue(request.params().get("q5").isEmpty());
              testContext.assertEquals(1, request.params().get("q6").size());
              testContext.assertEquals("1", request.params().get("q7").iterator().next());
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });

  }

  @Test
  public void testReplaceHeader(TestContext testContext) {
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
            .setPath("/")
            .addHeader("h1", "$query.q1")
            .addHeader("h2", "$var.foo")
            .addHeader("h3", "$body.type")
            .addHeader("h4", "$user.userId")
            .addHeader("h5", "$var.bar")
            .addHeader("foo", "bar")
            .addHeader("h6", "$body.obj")
            .addHeader("h7", "$body.arr");
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
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              System.out.println(request.headers());
              testContext.assertEquals(7, request.headers().keySet().size());
              testContext.assertEquals("q1.1", request.headers().get("h1").iterator().next());
              testContext.assertEquals("var_bar", request.headers().get("h2").iterator().next());
              testContext.assertEquals("1", request.headers().get("h3").iterator().next());
              testContext.assertEquals("1", request.headers().get("h4").iterator().next());
              testContext.assertTrue(request.headers().get("h5").isEmpty());
              testContext.assertEquals(1, request.headers().get("h6").size());
              testContext.assertEquals("1", request.headers().get("h7").iterator().next());
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });

  }

  @Test
  public void testReplaceBody(TestContext testContext) {
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h1", "h1.1");
    headers.put("h1", "h1.2");
    headers.put("h2", "h2");

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q1", "q1.1");
    params.put("q1", "q1.2");
    params.put("q2", "q2");

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
            .setPath("/")
            .setBody(new JsonObject()
                             .put("b1", "$header.h1")
                             .put("b2", "$query.q1")
                             .put("b3", "$var.foo")
                             .put("b4", "$user.userId")
                             .put("b5", "$var.bar")
                             .put("b6", new JsonObject()
                                     .put("userId", 1)
                                     .put("username", "edgar")
                                     .put("q1", "$query.q1"))
                             .put("b7", new JsonArray().add("$user.userId").add("2"))
                             .put("foo", "bar"));
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
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              System.out.println(request.body());
              testContext.assertEquals(7, request.body().size());
              testContext.assertEquals(2, request.body().getJsonArray("b1").size());
              testContext.assertEquals(2, request.body().getJsonArray("b2").size());
              testContext.assertEquals("var_bar", request.body().getString("b3"));
              testContext.assertEquals(1, request.body().getInteger("b4"));
              testContext.assertFalse(request.body().containsKey("b5"));
              testContext.assertEquals(3, request.body().getJsonObject("b6").size());
              testContext.assertEquals(1, request.body().getJsonObject("b6").getInteger("userId"));
              testContext.assertEquals(2, request.body().getJsonObject("b6").getJsonArray("q1")
                      .size());
              testContext.assertEquals(2, request.body().getJsonArray("b7").size());
              testContext.assertEquals(1, request.body().getJsonArray("b7").iterator().next());

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
