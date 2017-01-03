package com.edgar.direwolves.handler;

import com.google.common.collect.Lists;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.dispatch.verticle.ApiDispatchVerticle;
import com.edgar.direwolves.filter.servicediscovery.MockConsulHttpVerticle;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.direwolves.verticle.ApiDefinitionVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2017/1/3.
 *
 * @author Edgar  Date 2017/1/3
 */
@RunWith(VertxUnitRunner.class)
public class DispatchHandlerTest {

  Vertx vertx;

  MockConsulHttpVerticle mockConsulHttpVerticle;

  @Before
  public void setUp(TestContext testContext) {
    vertx = Vertx.vertx();

    JsonObject config = new JsonObject()
            .put("service.discovery", "consul://localhost:8500")
            .put("consul.port", 8500);
    vertx.deployVerticle(ApiDispatchVerticle.class.getName(),
                         new DeploymentOptions().setConfig(config),
                         testContext.asyncAssertSuccess());

    mockConsulHttpVerticle = new MockConsulHttpVerticle();
    vertx.deployVerticle(mockConsulHttpVerticle,
                         new DeploymentOptions().setConfig(config),
                         testContext.asyncAssertSuccess());
    add2Servers();

    vertx.deployVerticle(ApiDefinitionVerticle.class.getName(), testContext.asyncAssertSuccess());

    Endpoint httpEndpoint = Endpoint.createHttp("add_device", HttpMethod.POST, "/devices",
                                                "device");
    ApiDefinition apiDefinition = ApiDefinition.create("add_device", HttpMethod.POST, "/devices",
                                                       Lists.newArrayList(httpEndpoint));
    ApiDefinitionRegistry.create().add(apiDefinition);

    httpEndpoint = Endpoint.createHttp("list_device", HttpMethod.GET, "/devices",
                                       "device");
    apiDefinition = ApiDefinition.create("list_device", HttpMethod.GET, "/devices",
                                         Lists.newArrayList(httpEndpoint));
    ApiDefinitionRegistry.create().add(apiDefinition);

  }

  @Test
  public void testGet(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient()
            .get(8080, "localhost", "/devices?timestamp=" + Instant.now().getEpochSecond(),
                 resp -> {
                   resp.bodyHandler(body -> {
                     System.out.println(body.toString());
                     testContext.assertTrue(resp.statusCode() < 300);
                     async.complete();
                   });
                 }).end();
  }

  private void add2Servers() {
    mockConsulHttpVerticle.addService(new JsonObject()
                                              .put("Node", "u221")
                                              .put("Address", "localhost")
                                              .put("ServiceID", "u221:device:9001")
                                              .put("ServiceName", "device")
                                              .put("ServiceTags", new JsonArray())
                                              .put("ServicePort", 9001));
    mockConsulHttpVerticle.addService((new JsonObject()
            .put("Node", "u222")
            .put("Address", "localhost")
            .put("ServiceID", "u222:device:9002")
            .put("ServiceName", "user")
            .put("ServiceTags", new JsonArray())
            .put("ServicePort", 9002)));
    try {
      TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
