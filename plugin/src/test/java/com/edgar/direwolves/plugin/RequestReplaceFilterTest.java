package com.edgar.direwolves.plugin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.utils.JsonUtils;
import com.edgar.direwolves.plugin.transformer.RequestTransformerFilter;
import com.edgar.util.exception.DefaultErrorCode;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
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
public class RequestReplaceFilterTest {

  Vertx vertx;

  @Before
  public void testSetUp(TestContext testContext) {
    vertx = Vertx.vertx();

  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close(testContext.asyncAssertSuccess());
  }

  @Test
  public void testEndpointToRequest(TestContext testContext) {

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    ApiDefinition definition =
            ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add.json"));
    apiContext.setApiDefinition(definition);

    RequestTransformerFilter filter = new RequestTransformerFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        ApiContext apiContext1 = ar.result();
        testContext.assertEquals(1, apiContext1.requests().size());
        JsonObject request = apiContext1.requests().getJsonObject(0);
        testContext.assertEquals("localhost", request.getString("host"));
        testContext.assertEquals(8080, request.getInteger("port"));
        testContext.assertEquals(5, request.getJsonObject("params").size());
        testContext.assertEquals(4, request.getJsonObject("headers").size());
        testContext.assertFalse(request.getJsonObject("params").containsKey("q3"));
        testContext.assertFalse(request.getJsonObject("headers").containsKey("h3"));
        testContext.assertNull(request.getJsonObject("body"));
        System.out.println(request.encodePrettily());
        async.complete();
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
  }

  @Test
  public void testEndpointToRequest2(TestContext testContext) {

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
    ApiDefinition definition = ApiDefinition
            .fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add_2endpoint.json"));
    apiContext.setApiDefinition(definition);

    RequestTransformerFilter filter = new RequestTransformerFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        ApiContext apiContext1 = ar.result();
        testContext.assertEquals(2, apiContext1.requests().size());
        JsonObject request = apiContext1.requests().getJsonObject(0);
        testContext.assertEquals("localhost", request.getString("host"));
        testContext.assertEquals(8080, request.getInteger("port"));
        testContext.assertEquals(4, request.getJsonObject("params").size());
        testContext.assertEquals(4, request.getJsonObject("headers").size());
        testContext.assertFalse(request.getJsonObject("params").containsKey("q3"));
        testContext.assertFalse(request.getJsonObject("headers").containsKey("h3"));

        request = apiContext1.requests().getJsonObject(1);
        testContext.assertEquals("localhost", request.getString("host"));
        testContext.assertEquals(8080, request.getInteger("port"));
        testContext.assertEquals(1, request.getJsonObject("params").size());
        testContext.assertEquals(1, request.getJsonObject("headers").size());
        testContext.assertTrue(request.getJsonObject("params").containsKey("q3"));
        testContext.assertTrue(request.getJsonObject("headers").containsKey("h3"));
        async.complete();
      } else {
        testContext.fail();
      }
    });
  }

  @Test
  public void testNoService(TestContext testContext) {

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
    ApiDefinition definition =
            ApiDefinition
                    .fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_user_add.json"));
    apiContext.setApiDefinition(definition);

    RequestTransformerFilter filter = new RequestTransformerFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        ApiContext apiContext1 = ar.result();
        testContext.assertEquals(2, apiContext1.requests().size());
        testContext.fail();
      } else {
        testContext.assertTrue(ar.cause() instanceof ReplyException);
        ReplyException ex = (ReplyException) ar.cause();
        testContext.assertEquals(DefaultErrorCode.UNKOWN_REMOTE.getNumber(), ex.failureCode());
        async.complete();
      }
    });
  }


  @Test
  public void testReplace(TestContext testContext) {

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    params.put("foo", "query_bar");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params,
                              new JsonObject().put("type", 2));
    ApiDefinition definition =
            ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add.json"));
    apiContext.setApiDefinition(definition);
    apiContext.setPrincipal(new JsonObject().put("userId", "1"));
    apiContext.addVariable("foo", "var_bar");

    RequestTransformerFilter filter = new RequestTransformerFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        ApiContext apiContext1 = ar.result();
        testContext.assertEquals(1, apiContext1.requests().size());
        JsonObject request = apiContext1.requests().getJsonObject(0);
        testContext.assertEquals("localhost", request.getString("host"));
        testContext.assertEquals(8080, request.getInteger("port"));
        testContext.assertEquals(9, request.getJsonObject("params").size());
        testContext.assertEquals(2, request.getJsonObject("params").getJsonArray("q7").size());
        testContext.assertEquals("var_bar", request.getJsonObject("params").getString("q8"));
        testContext.assertEquals(2, request.getJsonObject("params").getInteger("q9"));
        testContext.assertEquals("1", request.getJsonObject("params").getString("q10"));

        testContext.assertEquals(8, request.getJsonObject("headers").size());
        testContext.assertEquals("1", request.getJsonObject("headers").getString("h7"));
        testContext.assertEquals("var_bar", request.getJsonObject("headers").getString("h9"));
        testContext.assertEquals("query_bar", request.getJsonObject("headers").getString("h8"));
        testContext.assertEquals(2, request.getJsonObject("headers").getInteger("h10"));

        testContext.assertNotNull(request.getJsonObject("body"));
        testContext.assertEquals(9, request.getJsonObject("body").size());
        testContext.assertEquals(2, request.getJsonObject("body").getJsonArray("p7").size());
        testContext.assertEquals("1", request.getJsonObject("body").getString("p10"));
        testContext.assertEquals("var_bar", request.getJsonObject("body").getString("p8"));
        testContext.assertEquals("query_bar", request.getJsonObject("body").getString("p9"));
        System.out.println(request.encodePrettily());
        async.complete();
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
  }

}
