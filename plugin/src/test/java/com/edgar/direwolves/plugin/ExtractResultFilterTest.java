package com.edgar.direwolves.plugin;

import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.plugin.request.RequestReplaceFilter;
import com.edgar.util.vertx.task.Task;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.rpc.Result;
import com.edgar.direwolves.core.utils.JsonUtils;
import com.edgar.direwolves.plugin.response.ExtractResultFilter;
import io.vertx.core.Future;
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
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
@RunWith(VertxUnitRunner.class)
public class ExtractResultFilterTest extends FilterTest {

  private final List<Filter> filters = new ArrayList<>();
  ExtractResultFilter filter;
  private ApiContext apiContext;

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    filter = new ExtractResultFilter();
    filter.config(vertx, new JsonObject());

    filters.clear();
    filters.add(filter);
  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close(testContext.asyncAssertSuccess());
  }


  @Test
  public void testSingleValue(TestContext testContext) {

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", null, null, new JsonObject());
    Result result = Result.createJsonObject("1", 200, new JsonObject().put("foo", "bar"), 0);

    apiContext.addResult(result.toJson());

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> {
          JsonObject jsonObject = context.response();
          testContext.assertEquals("bar", jsonObject.getJsonObject("body").getString("foo"));
          async.complete();
        }).onFailure(t -> testContext.fail());
  }

  @Test
  public void testZipValue(TestContext testContext) {

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", null, null, new JsonObject());
    Result result = Result.createJsonObject("1", 200, new JsonObject().put("foo", "bar"), 0);
    apiContext.addResult(result.toJson());
    result = Result.createJsonObject("2", 200, new JsonObject().put("bar", "foo"), 0);
    apiContext.addResult(result.toJson());

    apiContext.requests().add(new JsonObject().put("id", "1").put("name", "a"));
    apiContext.requests().add(new JsonObject().put("id", "2").put("name", "b"));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> {
          JsonObject jsonObject = context.response();
          testContext.assertEquals(2, jsonObject.getJsonObject("body").size());
          testContext.assertEquals("bar", jsonObject.getJsonObject("body").getJsonObject("a")
              .getString("foo"));
          testContext.assertEquals("foo", jsonObject.getJsonObject("body").getJsonObject("b")
              .getString("bar"));
          async.complete();
        }).onFailure(t -> testContext.fail());

  }

  @Test
  public void testOneFailedValue(TestContext testContext) {

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", null, null, new JsonObject());
    Result result = Result.createJsonObject("1", 200, new JsonObject().put("foo", "bar"), 0);
    apiContext.addResult(result.toJson());
    result = Result.createJsonObject("2", 400, new JsonObject().put("bar", "foo"), 0);
    apiContext.addResult(result.toJson());

    apiContext.requests().add(new JsonObject().put("id", "1").put("name", "a"));
    apiContext.requests().add(new JsonObject().put("id", "2").put("name", "b"));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> {
          JsonObject jsonObject = context.response();
          testContext.assertEquals(1, jsonObject.getJsonObject("body").size());
          testContext.assertEquals("foo", jsonObject.getJsonObject("body").getString("bar"));
          async.complete();
        }).onFailure(t -> testContext.fail());
  }

  @Test
  public void testTwoFailedValue(TestContext testContext) {

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", null, null, new JsonObject());
    Result result = Result.createJsonObject("1", 403, new JsonObject().put("foo", "bar"), 0);
    apiContext.addResult(result.toJson());
    result = Result.createJsonObject("2", 400, new JsonObject().put("bar", "foo"), 0);
    apiContext.addResult(result.toJson());

    apiContext.requests().add(new JsonObject().put("id", "1").put("name", "a"));
    apiContext.requests().add(new JsonObject().put("id", "2").put("name", "b"));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> {
          JsonObject jsonObject = context.response();
          testContext.assertEquals(1, jsonObject.getJsonObject("body").size());
          testContext.assertEquals("bar", jsonObject.getJsonObject("body").getString("foo"));
          async.complete();
        }).onFailure(t -> testContext.fail());
  }
}