package com.edgar.direwolves.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.utils.JsonUtils;
import com.edgar.direwolves.definition.ApiDefinition;
import com.edgar.direwolves.dispatch.ApiContext;
import com.edgar.direwolves.verticle.MockConsulHttpVerticle;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
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

  MockConsulHttpVerticle mockConsulHttpVerticle;

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

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
    ApiDefinition definition =
            ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add.json"));
    apiContext.setApiDefinition(definition);

    Record httpRecord = HttpEndpoint.createRecord("device", "localhost", 8080, "/");
    apiContext.addRecord(httpRecord);

    RequestFilter filter = new RequestFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        ApiContext apiContext1 = ar.result();
        testContext.assertEquals(1, apiContext1.request().size());
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
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
    ApiDefinition definition = ApiDefinition
            .fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add_2endpoint.json"));
    apiContext.setApiDefinition(definition);

    Record httpRecord = HttpEndpoint.createRecord("device", "localhost", 8080, "/");
    apiContext.addRecord(httpRecord);

    RequestFilter filter = new RequestFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        ApiContext apiContext1 = ar.result();
        testContext.assertEquals(2, apiContext1.request().size());
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
            ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add.json"));
    apiContext.setApiDefinition(definition);

    Record httpRecord = HttpEndpoint.createRecord("user", "localhost", 8080, "/");
    apiContext.addRecord(httpRecord);

    RequestFilter filter = new RequestFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = null;
    try {
      future = Future.future();
      filter.doFilter(apiContext, future);
      testContext.fail();
    } catch (Exception e) {
      testContext.assertTrue(e instanceof SystemException);
      SystemException ex = (SystemException) e;
      testContext.assertEquals(DefaultErrorCode.UNKOWN_REMOTE, ex.getErrorCode());
    }
  }

}
