//package com.edgar.direwolves.dispatch.filter;
//
//import com.google.common.collect.ArrayListMultimap;
//import com.google.common.collect.Multimap;
//
//import com.edgar.direwolves.core.utils.JsonUtils;
//import com.edgar.direwolves.core.spi.ApiDefinition;
//import com.edgar.direwolves.core.spi.ApiContext;
//import com.edgar.direwolves.dispatch.Utils;
//import io.vertx.core.Future;
//import io.vertx.core.Vertx;
//import io.vertx.core.http.HttpMethod;
//import io.vertx.core.json.JsonObject;
//import io.vertx.ext.unit.TestContext;
//import io.vertx.ext.unit.junit.VertxUnitRunner;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
///**
// * Created by Edgar on 2016/9/20.
// *
// * @author Edgar  Date 2016/9/20
// */
//@RunWith(VertxUnitRunner.class)
//public class RequestTranformerFilterTest {
//
//  Vertx vertx;
//
//  @Before
//  public void testSetUp(TestContext testContext) {
//    vertx = Vertx.vertx();
//  }
//
//  @After
//  public void tearDown(TestContext testContext) {
////    vertx.close(testContext.asyncAssertSuccess());
//  }
//
//
//  @Test
//  public void testRequestTransformer(TestContext testContext) {
//
//    Multimap<String, String> params = ArrayListMultimap.create();
//    params.put("q3", "v3");
//    Multimap<String, String> headers = ArrayListMultimap.create();
//    headers.put("h3", "v3");
//
//    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
//    ApiDefinition definition =
//            ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add.json"));
//    apiContext.setApiDefinition(definition);
//    JsonObject request = new JsonObject()
//            .put("name", "add_device")
//            .put("host", "localhost")
//            .put("port", 8080)
//            .put("params", Utils.mutlimapToJson(params))
//            .put("headers", Utils.mutlimapToJson(headers))
//            .put("method", "POST")
//            .put("path", "/devices");
//    apiContext.addRequest(request);
//
//    RequestTransformerFilter filter = new RequestTransformerFilter();
//    filter.config(vertx, new JsonObject());
//
//    Future<ApiContext> future = Future.future();
//    filter.doFilter(apiContext, future);
//    future.setHandler(ar -> {
//      if (ar.succeeded()) {
//        ApiContext apiContext1 = ar.result();
//        JsonObject jsonObject = apiContext1.request().getJsonObject(0);
//        testContext.assertEquals(4, jsonObject.getJsonObject("params").size());
//        testContext.assertEquals(4, jsonObject.getJsonObject("headers").size());
//        testContext.assertNull(jsonObject.getJsonObject("body"));
//      } else {
//        testContext.fail();
//      }
//    });
//  }
//
//  @Test
//  public void testRequestTransformerBody(TestContext testContext) {
//
//    Multimap<String, String> params = ArrayListMultimap.create();
//    params.put("q3", "v3");
//    Multimap<String, String> headers = ArrayListMultimap.create();
//    headers.put("h3", "v3");
//
//    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
//    ApiDefinition definition =
//            ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add.json"));
//    apiContext.setApiDefinition(definition);
//    JsonObject request = new JsonObject()
//            .put("name", "add_device")
//            .put("host", "localhost")
//            .put("port", 8080)
//            .put("params", Utils.mutlimapToJson(params))
//            .put("headers", Utils.mutlimapToJson(headers))
//            .put("method", "POST")
//            .put("path", "/devices")
//            .put("body", new JsonObject());
//    apiContext.addRequest(request);
//
//    RequestTransformerFilter filter = new RequestTransformerFilter();
//    filter.config(vertx, new JsonObject());
//
//    Future<ApiContext> future = Future.future();
//    filter.doFilter(apiContext, future);
//    future.setHandler(ar -> {
//      if (ar.succeeded()) {
//        ApiContext apiContext1 = ar.result();
//        JsonObject jsonObject = apiContext1.request().getJsonObject(0);
//        testContext.assertEquals(4, jsonObject.getJsonObject("params").size());
//        testContext.assertEquals(4, jsonObject.getJsonObject("headers").size());
//        testContext.assertEquals(4, jsonObject.getJsonObject("body").size());
//      } else {
//        testContext.fail();
//      }
//    });
//  }
//
//}
