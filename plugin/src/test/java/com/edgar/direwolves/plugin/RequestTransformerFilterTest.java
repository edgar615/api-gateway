package com.edgar.direwolves.plugin;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.utils.JsonUtils;
import com.edgar.direwolves.plugin.transformer.RequestTransformerFilter;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;
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
public class RequestTransformerFilterTest {

  Vertx vertx;

  @Before
  public void testSetUp(TestContext testContext) {
    vertx = Vertx.vertx();

    vertx.eventBus().<String>consumer("service.discovery.select", msg -> {
      String service = msg.body();
      if ("device".equals(service)) {
        Record record =HttpEndpoint.createRecord("device", "localhost", 8080, "/");
        msg.reply(record.toJson());
      } else {
        msg.fail(404, "no " + service + " instance found");
      }
    });
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
        testContext.assertEquals(1, apiContext1.request().size());
        JsonObject request = apiContext1.request().getJsonObject(0);
        testContext.assertEquals("localhost", request.getString("host"));
        testContext.assertEquals(8080, request.getInteger("port"));
        testContext.assertEquals(4, request.getJsonObject("params").size());
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
        testContext.assertEquals(2, apiContext1.request().size());
        JsonObject request = apiContext1.request().getJsonObject(0);
        testContext.assertEquals("localhost", request.getString("host"));
        testContext.assertEquals(8080, request.getInteger("port"));
        testContext.assertEquals(4, request.getJsonObject("params").size());
        testContext.assertEquals(4, request.getJsonObject("headers").size());
        testContext.assertFalse(request.getJsonObject("params").containsKey("q3"));
        testContext.assertFalse(request.getJsonObject("headers").containsKey("h3"));

        request = apiContext1.request().getJsonObject(1);
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
            ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_user_add.json"));
    apiContext.setApiDefinition(definition);

    RequestTransformerFilter filter = new RequestTransformerFilter();
    filter.config(vertx, new JsonObject());

      Future<ApiContext>  future = Future.future();
      filter.doFilter(apiContext, future);
      Async async = testContext.async();
      future.setHandler(ar -> {
        if (ar.succeeded()) {
          ApiContext apiContext1 = ar.result();
          testContext.assertEquals(2, apiContext1.request().size());
          testContext.fail();
        } else {
          testContext.assertTrue(ar.cause() instanceof SystemException);
          SystemException ex = (SystemException) ar.cause();
          testContext.assertEquals(DefaultErrorCode.UNKOWN_REMOTE, ex.getErrorCode());
          async.complete();
        }
      });
  }

}
