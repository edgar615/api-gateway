package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.utils.JsonUtils;
import com.edgar.direwolves.definition.ApiDefinition;
import com.edgar.direwolves.dispatch.ApiContext;
import com.edgar.direwolves.service.ServiceDiscoveryVerticle;
import com.edgar.direwolves.verticle.MockConsulHttpVerticle;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
@RunWith(VertxUnitRunner.class)
public class RequestTransfomerFilterTest {

  Vertx vertx;

  MockConsulHttpVerticle mockConsulHttpVerticle;

  @Before
  public void testSetUp(TestContext testContext) {
    vertx = Vertx.vertx();
    mockConsulHttpVerticle = new MockConsulHttpVerticle();
    vertx.deployVerticle(mockConsulHttpVerticle, testContext.asyncAssertSuccess());
    JsonObject config = new JsonObject()
        .put("service.discovery", "consul://localhost:5601");
    JsonObject strategy = new JsonObject();
    config.put("service.discovery.select-strategy", strategy);
    vertx.deployVerticle(ServiceDiscoveryVerticle.class.getName(),
        new DeploymentOptions().setConfig(config),
        testContext.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close();
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
//    vertx.close(testContext.asyncAssertSuccess());
  }

  private void add2Servers() {
    mockConsulHttpVerticle.addService(new JsonObject()
        .put("Node", "u221")
        .put("Address", "10.4.7.221")
        .put("ServiceID", "u221:device:8080")
        .put("ServiceName", "device")
        .put("ServiceTags", new JsonArray())
        .put("ServicePort", 32769));
    mockConsulHttpVerticle.addService((new JsonObject()
        .put("Node", "u222")
        .put("Address", "10.4.7.222")
        .put("ServiceID", "u222:device:8080")
        .put("ServiceName", "device")
        .put("ServiceTags", new JsonArray())
        .put("ServicePort", 32770)));
  }

  @Test
  public void testRequestTransformer(TestContext testContext) {

    add2Servers();
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");

    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
    ApiDefinition definition = ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add.json"));
    apiContext.setApiDefinition(definition);

    RequestTransfomerFilter filter = new RequestTransfomerFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        ApiContext apiContext1 = ar.result();
        JsonObject jsonObject = apiContext1.request().getJsonObject(0);
        testContext.assertEquals(4, jsonObject.getJsonObject("params").size());
        testContext.assertEquals(4, jsonObject.getJsonObject("headers").size());
        testContext.assertEquals(4, jsonObject.getJsonObject("body").size());
      } else {
        testContext.fail();
      }
    });
  }

  @Test
  public void testRequestTransformer2(TestContext testContext) {
    add2Servers();
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");

    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
    ApiDefinition definition = ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add_2endpoint.json"));
    apiContext.setApiDefinition(definition);

    RequestTransfomerFilter filter = new RequestTransfomerFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        ApiContext apiContext1 = ar.result();
        JsonObject jsonObject = apiContext1.request().getJsonObject(1);
        testContext.assertEquals(4, jsonObject.getJsonObject("params").size());
        testContext.assertEquals(4, jsonObject.getJsonObject("headers").size());
        testContext.assertEquals(4, jsonObject.getJsonObject("body").size());
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
  }

}
