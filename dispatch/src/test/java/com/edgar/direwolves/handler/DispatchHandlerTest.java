package com.edgar.direwolves.handler;

import com.google.common.collect.Lists;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.dispatch.verticle.ApiDispatchVerticle;
import com.edgar.direwolves.filter.servicediscovery.MockConsulHttpVerticle;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.direwolves.verticle.ApiDefinitionVerticle;
import com.edgar.util.base.Randoms;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2017/1/3.
 *
 * @author Edgar  Date 2017/1/3
 */
@RunWith(VertxUnitRunner.class)
public class DispatchHandlerTest {

  static Vertx vertx;

  static MockConsulHttpVerticle mockConsulHttpVerticle;

  @BeforeClass
  public static void setUp(TestContext testContext) {
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

    httpEndpoint = Endpoint.createHttp("get_device", HttpMethod.GET, "/devices/$param.param0",
                                       "device");
    apiDefinition = ApiDefinition.create("get_device", HttpMethod.GET, "/devices/([\\d+]+)",
                                         Lists.newArrayList(httpEndpoint));
    ApiDefinitionRegistry.create().add(apiDefinition);

    httpEndpoint = Endpoint.createHttp("add_device", HttpMethod.POST, "/devices",
                                       "device");
    apiDefinition = ApiDefinition.create("add_device", HttpMethod.POST, "/devices",
                                         Lists.newArrayList(httpEndpoint));
    ApiDefinitionRegistry.create().add(apiDefinition);
    httpEndpoint = Endpoint.createHttp("update_device", HttpMethod.PUT, "/devices/$param.param0",
                                       "device");
    apiDefinition = ApiDefinition.create("update_device", HttpMethod.PUT, "/devices/([\\d+]+)",
                                         Lists.newArrayList(httpEndpoint));
    ApiDefinitionRegistry.create().add(apiDefinition);

    httpEndpoint = Endpoint.createHttp("error_device", HttpMethod.GET, "/devices/error",
                                       "device");
    apiDefinition = ApiDefinition.create("error_device", HttpMethod.GET, "/devices/failed",
                                         Lists.newArrayList(httpEndpoint));
    ApiDefinitionRegistry.create().add(apiDefinition);

    vertx.deployVerticle(DeviceHttpVerticle.class.getName(),
                         new DeploymentOptions().setConfig(new JsonObject().put("http.port",
                                                                                9001)).setWorker
                                 (true),
                         testContext.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext testContext) {
//    vertx.close();
  }

  @Test
  public void testGetError(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient()
            .get(8080, "localhost", "/devices/failed?timestamp=" + Instant.now().getEpochSecond(),
                 resp -> {
                   resp.bodyHandler(body -> {
                     System.out.println(body.toString());
                     testContext.assertTrue(resp.statusCode() == 400);
                     String reqId = resp.getHeader("x-request-id");
                     testContext.assertNotNull(reqId);
                     async.complete();
                   });
                 }).end();
  }

  @Test
  public void testGetArray(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient()
            .get(8080, "localhost", "/devices?timestamp=" + Instant.now().getEpochSecond(),
                 resp -> {
                   resp.bodyHandler(body -> {
                     System.out.println(body.toString());
                     testContext.assertTrue(resp.statusCode() < 300);
                     JsonArray jsonArray = new JsonArray(body.toString());
                     testContext.assertEquals(2, jsonArray.size());
                     String reqId = resp.getHeader("x-request-id");
                     testContext.assertNotNull(reqId);
                     async.complete();
                   });
                 }).end();
  }

  @Test
  public void testGetObject(TestContext testContext) {
    Async async = testContext.async();
    int userId = Integer.parseInt(Randoms.randomNumber(5));
    vertx.createHttpClient()
            .get(8080, "localhost",
                  "/devices/" + userId + "?timestamp="
                  + Instant.now().getEpochSecond(),
                  resp -> {
                    resp.bodyHandler(body -> {
                      System.out.println(body.toString());
                      testContext.assertTrue(resp.statusCode() < 300);
                      JsonObject jsonObject = new JsonObject(body.toString());
                      testContext.assertEquals(userId + "", jsonObject.getString("id"));
                      String reqId = resp.getHeader("x-request-id");
                      testContext.assertNotNull(reqId);
                      async.complete();
                    });
                  }).end();
  }

  @Test
  public void testPostObject(TestContext testContext) {
    Async async = testContext.async();
    vertx.createHttpClient()
            .post(8080, "localhost",
                  "/devices?timestamp="
                  + Instant.now().getEpochSecond(),
                  resp -> {
                    resp.bodyHandler(body -> {
                      System.out.println(body.toString());
                      testContext.assertTrue(resp.statusCode() < 300);
                      JsonObject jsonObject = new JsonObject(body.toString());
                      testContext.assertEquals("bar", jsonObject.getJsonObject("body").getString("foo"));
                      String reqId = resp.getHeader("x-request-id");
                      testContext.assertNotNull(reqId);
                      async.complete();
                    });
                  }).setChunked(true)
            .write(new JsonObject().put("foo", "bar").encode()).end();
  }

  @Test
  public void testPutObject(TestContext testContext) {
    Async async = testContext.async();
    int userId = Integer.parseInt(Randoms.randomNumber(5));
    vertx.createHttpClient()
            .put(8080, "localhost",
                 "/devices/" + userId + "?timestamp="
                 + Instant.now().getEpochSecond(),
                 resp -> {
                   resp.bodyHandler(body -> {
                     System.out.println(body.toString());
                     testContext.assertTrue(resp.statusCode() < 300);
                     JsonObject jsonObject = new JsonObject(body.toString());
                     testContext.assertEquals("bar", jsonObject.getJsonObject("body").getString("foo"));
                     String reqId = resp.getHeader("x-request-id");
                     testContext.assertNotNull(reqId);
                     async.complete();
                   });
                 }).setChunked(true)
            .end(new JsonObject().put("foo", "bar").encode());
  }

  private static void add2Servers() {
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
