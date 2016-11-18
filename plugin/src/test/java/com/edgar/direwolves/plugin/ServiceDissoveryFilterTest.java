package com.edgar.direwolves.plugin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.utils.EventbusUtils;
import com.edgar.direwolves.core.utils.JsonUtils;
import com.edgar.direwolves.plugin.servicediscovery.ServiceDissoveryFilter;
import com.edgar.direwolves.plugin.transformer.RequestTransformerFilter;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.ReplyException;
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
 * Created by Edgar on 2016/11/18.
 *
 * @author Edgar  Date 2016/11/18
 */
@RunWith(VertxUnitRunner.class)
public class ServiceDissoveryFilterTest {
  Vertx vertx;

  @Before
  public void testSetUp(TestContext testContext) {
    vertx = Vertx.vertx();

    vertx.eventBus().<String>consumer("service.discovery.select", msg -> {
      String service = msg.body();
      if ("device".equals(service)) {
        Record record = HttpEndpoint.createRecord("device", "localhost", 8080, "/");
        msg.reply(record.toJson());
      } else {
        EventbusUtils.fail(msg, SystemException.create(DefaultErrorCode.UNKOWN_REMOTE));
      }
    });
  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close(testContext.asyncAssertSuccess());
  }

  @Test
  public void testEndpointToRequest(TestContext testContext) {

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", null, null, null);
    ApiDefinition definition =
            ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add.json"));
    apiContext.setApiDefinition(definition);

    ServiceDissoveryFilter filter = new ServiceDissoveryFilter();
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
        testContext.assertEquals(0, request.getJsonObject("params").size());
        testContext.assertEquals(0, request.getJsonObject("headers").size());
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

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", null, null, new JsonObject());
    ApiDefinition definition = ApiDefinition
            .fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add_2endpoint.json"));
    apiContext.setApiDefinition(definition);

    ServiceDissoveryFilter filter = new ServiceDissoveryFilter();
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
        testContext.assertEquals(0, request.getJsonObject("params").size());
        testContext.assertEquals(0, request.getJsonObject("headers").size());

        request = apiContext1.requests().getJsonObject(1);
        testContext.assertEquals("localhost", request.getString("host"));
        testContext.assertEquals(8080, request.getInteger("port"));
        testContext.assertEquals(0, request.getJsonObject("params").size());
        testContext.assertEquals(0, request.getJsonObject("headers").size());
        async.complete();
      } else {
        testContext.fail();
      }
    });
  }

  @Test
  public void testNoService(TestContext testContext) {

    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", null, null, new JsonObject());
    ApiDefinition definition =
            ApiDefinition
                    .fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_user_add.json"));
    apiContext.setApiDefinition(definition);

    ServiceDissoveryFilter filter = new ServiceDissoveryFilter();
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

}
