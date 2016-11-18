package com.edgar.direwolves.plugin;

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
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
@RunWith(VertxUnitRunner.class)
public class ExtractResultFilterTest {

  Vertx vertx;

  @Before
  public void testSetUp(TestContext testContext) {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext testContext) {
//    vertx.close(testContext.asyncAssertSuccess());
  }


  @Test
  public void testSingleValue(TestContext testContext) {

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
    ApiDefinition definition =
            ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add.json"));
    apiContext.setApiDefinition(definition);
    Result result = Result.createJsonObject("1", 200, new JsonObject().put("foo", "bar"), 0);

    apiContext.addResult(result.toJson());

    ExtractResultFilter filter = new ExtractResultFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        ApiContext apiContext1 = ar.result();
        JsonObject jsonObject = apiContext1.response();
        testContext.assertEquals("bar", jsonObject.getJsonObject("body").getString("foo"));
      } else {
        testContext.fail();
      }
    });
  }

  @Test
  public void testZipValue(TestContext testContext) {

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
    ApiDefinition definition =
            ApiDefinition.fromJson(
                    JsonUtils.getJsonFromFile("src/test/resources/device_add_2endpoint.json"));
    apiContext.setApiDefinition(definition);
    Result result = Result.createJsonObject("1", 200, new JsonObject().put("foo", "bar"), 0);
    apiContext.addResult(result.toJson());
    result = Result.createJsonObject("2", 200, new JsonObject().put("bar", "foo"), 0);
    apiContext.addResult(result.toJson());

    apiContext.requests().add(new JsonObject().put("id", "1").put("name", "a"));
    apiContext.requests().add(new JsonObject().put("id", "2").put("name", "b"));

    ExtractResultFilter filter = new ExtractResultFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        ApiContext apiContext1 = ar.result();
        JsonObject jsonObject = apiContext1.response();
        testContext.assertEquals(2, jsonObject.getJsonObject("body").size());
        testContext.assertEquals("bar", jsonObject.getJsonObject("body").getJsonObject("a")
                .getString("foo"));
        testContext.assertEquals("foo", jsonObject.getJsonObject("body").getJsonObject("b")
                .getString("bar"));
      } else {
        testContext.fail();
      }
    });
  }

  @Test
  public void testOneFailedValue(TestContext testContext) {

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
    ApiDefinition definition =
            ApiDefinition.fromJson(
                    JsonUtils.getJsonFromFile("src/test/resources/device_add_2endpoint.json"));
    apiContext.setApiDefinition(definition);
    Result result = Result.createJsonObject("1", 200, new JsonObject().put("foo", "bar"), 0);
    apiContext.addResult(result.toJson());
    result = Result.createJsonObject("2", 400, new JsonObject().put("bar", "foo"), 0);
    apiContext.addResult(result.toJson());

    apiContext.requests().add(new JsonObject().put("id", "1").put("name", "a"));
    apiContext.requests().add(new JsonObject().put("id", "2").put("name", "b"));

    ExtractResultFilter filter = new ExtractResultFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        ApiContext apiContext1 = ar.result();
        JsonObject jsonObject = apiContext1.response();
        testContext.assertEquals(1, jsonObject.getJsonObject("body").size());
        testContext.assertEquals("foo", jsonObject.getJsonObject("body").getString("bar"));
      } else {
        testContext.fail();
      }
    });
  }

  @Test
  public void testTwoFailedValue(TestContext testContext) {

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
    ApiDefinition definition =
            ApiDefinition.fromJson(
                    JsonUtils.getJsonFromFile("src/test/resources/device_add_2endpoint.json"));
    apiContext.setApiDefinition(definition);
    Result result = Result.createJsonObject("1", 403, new JsonObject().put("foo", "bar"), 0);
    apiContext.addResult(result.toJson());
    result = Result.createJsonObject("2", 400, new JsonObject().put("bar", "foo"), 0);
    apiContext.addResult(result.toJson());

    apiContext.requests().add(new JsonObject().put("id", "1").put("name", "a"));
    apiContext.requests().add(new JsonObject().put("id", "2").put("name", "b"));

    ExtractResultFilter filter = new ExtractResultFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        ApiContext apiContext1 = ar.result();
        JsonObject jsonObject = apiContext1.response();
        testContext.assertEquals(1, jsonObject.getJsonObject("body").size());
        testContext.assertEquals("bar", jsonObject.getJsonObject("body").getString("foo"));
      } else {
        testContext.fail();
      }
    });
  }
}