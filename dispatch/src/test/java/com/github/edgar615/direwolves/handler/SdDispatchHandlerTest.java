//package com.github.edgar615.direwolves.handler;
//
//import static org.awaitility.Awaitility.await;
//
//import ApiUtils;
//import ApiDiscovery;
//import ApiDiscoveryOptions;
//import MockConsulHttpVerticle;
//import ApiDispatchVerticle;
//import com.github.edgar615.util.base.Randoms;
//import io.vertx.core.DeploymentOptions;
//import io.vertx.core.Vertx;
//import io.vertx.core.json.JsonArray;
//import io.vertx.core.json.JsonObject;
//import io.vertx.ext.unit.Async;
//import io.vertx.ext.unit.TestContext;
//import io.vertx.ext.unit.junit.VertxUnitRunner;
//import io.vertx.servicediscovery.ServiceDiscovery;
//import io.vertx.servicediscovery.consul.ConsulServiceImporter;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.time.Instant;
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicBoolean;
//
///**
// * Created by Edgar on 2017/1/3.
// *
// * @author Edgar  Date 2017/1/3
// */
//@RunWith(VertxUnitRunner.class)
//public class SdDispatchHandlerTest {
//
//  ApiDiscovery apiDiscovery;
//   ServiceDiscovery serviceDiscovery;
//  Vertx vertx;
//
//  int port = Integer.parseInt(Randoms.randomNumber(4));
//
//  int consulPort = Integer.parseInt(Randoms.randomNumber(4));
//
//  int devicePort = Integer.parseInt(Randoms.randomNumber(4));
//
//  MockConsulHttpVerticle mockConsulHttpVerticle;
//
//  AtomicBoolean started = new AtomicBoolean();
//
//  private String namespace = UUID.randomUUID().toString();
//
//  private JsonObject config = new JsonObject()
//          .put("namespace", namespace)
//          .put("consul.port", consulPort)
//          .put("port", port);
//
//  private ConsulServiceImporter importer;
//  @Before
//  public void setUp(TestContext testContext) {
//    vertx = Vertx.vertx();
//
//    apiDiscovery = ApiDiscovery.create(vertx, new ApiDiscoveryOptions().setName(namespace));
//    ApiUtils.registerApi(apiDiscovery, devicePort);
//
//    serviceDiscovery = ServiceDiscovery.create(vertx);
//
//    importer = new ConsulServiceImporter();
//    serviceDiscovery.registerServiceImporter(importer, new JsonObject()
//            .put("host", "localhost")
//            .put("port", consulPort));
//
//    System.out.println(config);
//    vertx.deployVerticle(ApiDispatchVerticle.class.getName(),
//                         new DeploymentOptions().setConfig(config),
//                         ar -> {
//                           if (ar.failed()) {
//                             ar.cause().printStackTrace();
//                           }
//                           started.set(true);
//                         });
//
//    mockConsulHttpVerticle = new MockConsulHttpVerticle();
//    vertx.deployVerticle(mockConsulHttpVerticle,
//                         new DeploymentOptions().setConfig(config),
//                         testContext.asyncAssertSuccess());
//    add2Servers();
//
//    vertx.deployVerticle(DeviceHttpVerticle.class.getName(),
//                         new DeploymentOptions().setConfig(new JsonObject().put("port",
//                                                                                devicePort)).setWorker
//                                 (true),
//                         testContext.asyncAssertSuccess());
//    await().until(() -> started.get());
//  }
//
//  private void add2Servers() {
//    mockConsulHttpVerticle.addService(new JsonObject()
//                                              .put("Node", "u221")
//                                              .put("Address", "localhost")
//                                              .put("ServiceID", "u221:device:9001")
//                                              .put("ServiceName", "device")
//                                              .put("ServiceTags", new JsonArray())
//                                              .put("ServicePort", 9001));
//    mockConsulHttpVerticle.addService((new JsonObject()
//            .put("Node", "u222")
//            .put("Address", "localhost")
//            .put("ServiceID", "u222:device:9002")
//            .put("ServiceName", "user")
//            .put("ServiceTags", new JsonArray())
//            .put("ServicePort", 9002)));
//    try {
//      TimeUnit.SECONDS.sleep(3);
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }
//  }
//
//  @After
//  public void tearDown(TestContext testContext) {
////    AtomicBoolean complete = new AtomicBoolean();
////    importer.close(ar -> {
////      complete.set(true);
////    });
////    Awaitility.await().until(() -> complete.set(true));
//
//    vertx.close(ar -> {
//      started.set(false);
//    });
//    await().until(() -> !started.get());
//  }
//
//  @Test
//  public void testGetError(TestContext testContext) {
//    Async async = testContext.async();
//    vertx.createHttpClient()
//            .get(port, "localhost", "/devices/failed?timestamp=" + Instant.now().getEpochSecond(),
//                 resp -> {
//                   resp.bodyHandler(body -> {
//                     System.out.println(body.toString());
//                     System.out.println(resp.statusCode());
//                     testContext.assertTrue(resp.statusCode() == 400);
//                     String reqId = resp.getHeader("x-request-id");
//                     testContext.assertNotNull(reqId);
//                     async.complete();
//                   });
//                 }).end();
//  }
//
//  @Test
//  public void testGetArray(TestContext testContext) {
//    Async async = testContext.async();
//    vertx.createHttpClient()
//            .get(port, "localhost", "/devices?timestamp=" + Instant.now().getEpochSecond(),
//                 resp -> {
//                   System.out.println(resp.statusCode());
//                   resp.bodyHandler(body -> {
//                     System.out.println(body.toString());
//                     testContext.assertTrue(resp.statusCode() < 300);
//                     JsonArray jsonArray = new JsonArray(body.toString());
//                     testContext.assertEquals(2, jsonArray.size());
//                     String reqId = resp.getHeader("x-request-id");
//                     testContext.assertNotNull(reqId);
//                     async.complete();
//                   });
//                 }).end();
//  }
//
//  @Test
//  public void testGetObject(TestContext testContext) {
//    Async async = testContext.async();
//    int userId = Integer.parseInt(Randoms.randomNumber(5));
//    vertx.createHttpClient()
//            .get(port, "localhost",
//                 "/devices/" + userId + "?timestamp="
//                 + Instant.now().getEpochSecond(),
//                 resp -> {
//                   resp.bodyHandler(body -> {
//                     System.out.println(body.toString());
//                     testContext.assertTrue(resp.statusCode() < 300);
//                     JsonObject jsonObject = new JsonObject(body.toString());
//                     testContext.assertEquals(userId + "", jsonObject.getString("id"));
//                     String reqId = resp.getHeader("x-request-id");
//                     testContext.assertNotNull(reqId);
//                     async.complete();
//                   });
//                 }).end();
//  }
//
//  @Test
//  public void testPostObject(TestContext testContext) {
//    Async async = testContext.async();
//    vertx.createHttpClient()
//            .post(port, "localhost",
//                  "/devices?timestamp="
//                  + Instant.now().getEpochSecond(),
//                  resp -> {
//                    resp.bodyHandler(body -> {
//                      System.out.println(body.toString());
//                      testContext.assertTrue(resp.statusCode() < 300);
//                      JsonObject jsonObject = new JsonObject(body.toString());
//                      testContext.assertEquals("bar",
//                                               jsonObject.getJsonObject("body").getString("foo"));
//                      String reqId = resp.getHeader("x-request-id");
//                      testContext.assertNotNull(reqId);
//                      async.complete();
//                    });
//                  }).setChunked(true)
//            .write(new JsonObject().put("foo", "bar").encode()).end();
//  }
//
//  @Test
//  public void testPutObject(TestContext testContext) {
//    Async async = testContext.async();
//    int userId = Integer.parseInt(Randoms.randomNumber(5));
//    vertx.createHttpClient()
//            .put(port, "localhost",
//                 "/devices/" + userId + "?timestamp="
//                 + Instant.now().getEpochSecond(),
//                 resp -> {
//                   resp.bodyHandler(body -> {
//                     System.out.println(body.toString());
//                     testContext.assertTrue(resp.statusCode() < 300);
//                     JsonObject jsonObject = new JsonObject(body.toString());
//                     testContext.assertEquals("bar",
//                                              jsonObject.getJsonObject("body").getString("foo"));
//                     String reqId = resp.getHeader("x-request-id");
//                     testContext.assertNotNull(reqId);
//                     async.complete();
//                   });
//                 }).setChunked(true)
//            .end(new JsonObject().put("foo", "bar").encode());
//  }
//}
