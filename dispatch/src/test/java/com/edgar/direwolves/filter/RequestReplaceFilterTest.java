package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.http.HttpRpcRequest;
import com.edgar.util.vertx.task.Task;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
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
import java.util.UUID;

/**
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
@RunWith(VertxUnitRunner.class)
public class RequestReplaceFilterTest extends FilterTest {

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
  public void testReplaceParams(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("foo", "bar");
    params.put("q1", "$header.h1");
    params.put("q2", "$var.foo");
    params.put("q3", "$body.type");
    params.put("q4", "$user.userId");
    params.put("q5", "$var.bar");

    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h1", "h1");

    JsonObject jsonObject = new JsonObject()
        .put("type", 1);

    ApiContext apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

    HttpRpcRequest httpRpcRequest = HttpRpcRequest.create(UUID.randomUUID().toString(),
        "add_device")
        .setHost("localhost")
        .setPort(8080)
        .setHttpMethod(HttpMethod.POST)
        .setPath("/")
        .addParam("q1", "$header.h1")
        .addParam("q2", "$var.foo")
        .addParam("q3", "$body.type")
        .addParam("q4", "$user.userId")
        .addParam("q5", "$var.bar")
        .addParam("q6", "bar");
    apiContext.addRequest(httpRpcRequest);

    apiContext.addVariable("foo", "var_bar");
    apiContext.setPrincipal(new JsonObject().put("userId", 1));

    Filter filter = Filter.create(RequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> {
          testContext.assertEquals(1, context.requests().size());
          HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
          testContext.assertEquals("localhost", request.getHost());
          testContext.assertEquals(8080, request.getPort());
          testContext.assertEquals(5, request.getParams().keys().size());
          testContext.assertEquals("var_bar", request.getParams().get("q2").iterator().next());
          testContext.assertEquals("1", request.getParams().get("q3").iterator().next());
          testContext.assertEquals("1", request.getParams().get("q4").iterator().next());
          testContext.assertTrue(request.getParams().get("q5").isEmpty());

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
    headers.put("h1", "$query.q1");
    headers.put("h2", "$var.foo");
    headers.put("h3", "$body.type");
    headers.put("h4", "$user.userId");
    headers.put("h5", "$var.bar");

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q1", "q1");

    JsonObject jsonObject = new JsonObject()
        .put("type", 1);

    ApiContext apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

    HttpRpcRequest httpRpcRequest = HttpRpcRequest.create(UUID.randomUUID().toString(),
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
        .addHeader("h6", "bar");
    apiContext.addRequest(httpRpcRequest);

    apiContext.addVariable("foo", "var_bar");
    apiContext.setPrincipal(new JsonObject().put("userId", 1));

    Filter filter = Filter.create(RequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> {
          testContext.assertEquals(1, context.requests().size());
          HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
          testContext.assertEquals("localhost", request.getHost());
          testContext.assertEquals(8080, request.getPort());
          testContext.assertEquals(5, request.getHeaders().keys().size());
          testContext.assertEquals("q1", request.getHeaders().get("h1").iterator().next());
          testContext.assertEquals("var_bar", request.getHeaders().get("h2").iterator().next());
          testContext.assertEquals("1", request.getHeaders().get("h3").iterator().next());
          testContext.assertEquals("1", request.getHeaders().get("h4").iterator().next());
          testContext.assertTrue(request.getHeaders().get("h5").isEmpty());

          async.complete();
        }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });

  }

  @Test
  public void testReplaceBody(TestContext testContext) {
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h1", "h1");

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q1", "q1");

    JsonObject jsonObject = new JsonObject()
        .put("type", 1);

    ApiContext apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);

    HttpRpcRequest httpRpcRequest = HttpRpcRequest.create(UUID.randomUUID().toString(),
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
            .put("b6", "bar"));
    apiContext.addRequest(httpRpcRequest);

    apiContext.addVariable("foo", "var_bar");
    apiContext.setPrincipal(new JsonObject().put("userId", 1));

    Filter filter = Filter.create(RequestReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> {
          testContext.assertEquals(1, context.requests().size());
          HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
          testContext.assertEquals("localhost", request.getHost());
          testContext.assertEquals(8080, request.getPort());
          testContext.assertEquals(5, request.getBody().size());
          testContext.assertEquals("h1", request.getBody().getString("b1"));
          testContext.assertEquals("q1", request.getBody().getString("b2"));
          testContext.assertEquals("var_bar", request.getBody().getString("b3"));
          testContext.assertEquals(1, request.getBody().getInteger("b4"));
          testContext.assertFalse(request.getBody().containsKey("b5"));

          async.complete();
        }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });

  }



}
