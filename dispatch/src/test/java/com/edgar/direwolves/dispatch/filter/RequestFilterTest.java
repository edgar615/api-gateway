package com.edgar.direwolves.dispatch.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.utils.JsonUtils;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.dispatch.Utils;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
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
public class RequestFilterTest {

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
            Utils.apiContext(HttpMethod.GET, "/devices", headers, params, null);
    ApiDefinition definition =
            ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add.json"));
    apiContext.setApiDefinition(definition);

    Record httpRecord = HttpEndpoint.createRecord("device", "localhost", 8080, "/");
    apiContext.addService(httpRecord);

    RequestFilter filter = new RequestFilter();
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
        testContext.assertEquals(1, request.getJsonObject("params").size());
        testContext.assertEquals(1, request.getJsonObject("headers").size());
        testContext.assertEquals(1, request.getJsonObject("params").getJsonArray("q3").size());
        testContext.assertEquals(2, request.getJsonObject("headers").getJsonArray("h3").size());
        System.out.println(request.encodePrettily());
        async.complete();
      } else {
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
            Utils.apiContext(HttpMethod.GET, "/devices", headers, params, new JsonObject());
    ApiDefinition definition = ApiDefinition
            .fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add_2endpoint.json"));
    apiContext.setApiDefinition(definition);

    Record httpRecord = HttpEndpoint.createRecord("device", "localhost", 8080, "/");
    apiContext.addService(httpRecord);

    RequestFilter filter = new RequestFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        ApiContext apiContext1 = ar.result();
        testContext.assertEquals(2, apiContext1.request().size());
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
            Utils.apiContext(HttpMethod.GET, "/devices", headers, params, new JsonObject());
    ApiDefinition definition =
            ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add.json"));
    apiContext.setApiDefinition(definition);

    Record httpRecord = HttpEndpoint.createRecord("user", "localhost", 8080, "/");
    apiContext.addService(httpRecord);

    RequestFilter filter = new RequestFilter();
    filter.config(vertx, new JsonObject());

    Async async = testContext.async();
    Future<ApiContext> future = null;
    try {
      future = Future.future();
      filter.doFilter(apiContext, future);
      testContext.fail();
    } catch (Exception e) {
      testContext.assertTrue(e instanceof SystemException);
      SystemException ex = (SystemException) e;
      testContext.assertEquals(DefaultErrorCode.UNKOWN_REMOTE, ex.getErrorCode());
      async.complete();
    }
  }

}
