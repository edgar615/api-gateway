package com.github.edgar615.direwolves.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.dispatch.Result;
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
    Multimap<String, String> respHeader = ArrayListMultimap.create();
    respHeader.put("h1", "$query.q1");
    respHeader.put("h2", "$var.foo");
    respHeader.put("h3", "$body.type");
    respHeader.put("h4", "$user.userId");
    respHeader.put("h5", "$var.bar");
    respHeader.put("foo", "bar");
    respHeader.put("h6", "$body.obj");
    respHeader.put("h7", "$body.arr");
    apiContext.setResult(Result.createJsonObject(200, new JsonObject(), respHeader));

    apiContext.addVariable("foo", "var_bar");
    apiContext.setPrincipal(new JsonObject().put("userId", 1));

    Filter filter =
            Filter.create(ResponseReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);


    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
             Result result = context.result();
              System.out.println(result.header());
              testContext.assertEquals(7, result.header().keySet().size());
              testContext.assertEquals("q1.1", result.header().get("h1").iterator().next());
              testContext.assertEquals("var_bar", result.header().get("h2").iterator().next());
              testContext.assertEquals("1", result.header().get("h3").iterator().next());
              testContext.assertEquals("1", result.header().get("h4").iterator().next());
              testContext.assertTrue(result.header().get("h5").isEmpty());
              testContext.assertEquals(1, result.header().get("h6").size());
              testContext.assertEquals("1", result.header().get("h7").iterator().next());
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
    Multimap<String, String> respHeader = ArrayListMultimap.create();

    JsonObject respBody =  new JsonObject()
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
            .put("foo", "bar");
    apiContext.setResult(Result.createJsonObject(200, respBody, respHeader));

    apiContext.addVariable("foo", "var_bar");
    apiContext.setPrincipal(new JsonObject().put("userId", 1));

    Filter filter =
            Filter.create(ResponseReplaceFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              Result result = context.result();
              System.out.println(result.responseObject());
              testContext.assertEquals(7, result.responseObject().size());
              testContext.assertEquals(2, result.responseObject().getJsonArray("b1").size());
              testContext.assertEquals(2, result.responseObject().getJsonArray("b2").size());
              testContext.assertEquals("var_bar", result.responseObject().getString("b3"));
              testContext.assertEquals(1, result.responseObject().getInteger("b4"));
              testContext.assertFalse(result.responseObject().containsKey("b5"));
              testContext.assertEquals(3, result.responseObject().getJsonObject("b6").size());
              testContext.assertEquals(1, result.responseObject().getJsonObject("b6").getInteger("userId"));
              testContext.assertEquals(2, result.responseObject().getJsonObject("b6").getJsonArray("q1")
                      .size());
              testContext.assertEquals(2, result.responseObject().getJsonArray("b7").size());
              testContext.assertEquals(1, result.responseObject().getJsonArray("b7").iterator().next());

              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });

  }


}
