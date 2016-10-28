package com.edgar.direwolves.dispatch.filter;

import com.edgar.direwolves.core.utils.JsonUtils;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.dispatch.Utils;
import com.edgar.direwolves.service.ServiceDiscoveryVerticle;
import com.edgar.direwolves.verticle.MockConsulHttpVerticle;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
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

import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
@RunWith(VertxUnitRunner.class)
public class ServiceDescoveryFilterTest {

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
    vertx.close(testContext.asyncAssertSuccess());
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

  private void add2UserServers() {
    mockConsulHttpVerticle.addService(new JsonObject()
                                              .put("Node", "u221")
                                              .put("Address", "10.4.7.221")
                                              .put("ServiceID", "u221:device:8080")
                                              .put("ServiceName", "user")
                                              .put("ServiceTags", new JsonArray())
                                              .put("ServicePort", 32769));
    mockConsulHttpVerticle.addService((new JsonObject()
            .put("Node", "u222")
            .put("Address", "10.4.7.222")
            .put("ServiceID", "u222:device:8080")
            .put("ServiceName", "user")
            .put("ServiceTags", new JsonArray())
            .put("ServicePort", 32770)));
  }

  @Test
  public void testService(TestContext testContext) {

    add2Servers();
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ApiContext apiContext = Utils.apiContext(HttpMethod.GET, "/devices", null, null, null);
    ApiDefinition definition = ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add_2endpoint.json"));
    apiContext.setApiDefinition(definition);

    ServiceDiscoveryFilter filter = new ServiceDiscoveryFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        ApiContext apiContext1 = ar.result();
        testContext.assertEquals(1, apiContext1.services().size());
        System.out.println(apiContext1.services().get(0).toJson());
        async.complete();
      } else {
        testContext.fail();
        async.complete();
      }
    });
  }

  @Test
  public void testFailed(TestContext testContext) {

    add2UserServers();
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ApiContext apiContext = Utils.apiContext(HttpMethod.GET, "/devices", null, null, null);
    ApiDefinition definition = ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add_2endpoint.json"));
    apiContext.setApiDefinition(definition);

    ServiceDiscoveryFilter filter = new ServiceDiscoveryFilter();
    filter.config(vertx, new JsonObject());

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        testContext.fail();
      } else {
        Throwable throwable = ar.cause();
        testContext.assertTrue(throwable instanceof SystemException);
        SystemException ex = (SystemException) throwable;
        testContext.assertEquals(DefaultErrorCode.UNKOWN_REMOTE, ex.getErrorCode());
      }
    });
  }

}
