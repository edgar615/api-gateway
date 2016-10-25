//package com.edgar.direwolves.filter;
//
//import com.edgar.direwolves.dispatch.ApiContext;
//import io.vertx.core.Future;
//import io.vertx.core.Vertx;
//import io.vertx.core.http.HttpClient;
//import io.vertx.core.http.HttpMethod;
//import io.vertx.core.http.HttpServer;
//import io.vertx.core.json.JsonArray;
//import io.vertx.core.json.JsonObject;
//import io.vertx.ext.unit.TestContext;
//import io.vertx.ext.unit.junit.VertxUnitRunner;
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
///**
// * Created by Edgar on 2016/10/18.
// *
// * @author Edgar  Date 2016/10/18
// */
//@RunWith(VertxUnitRunner.class)
//public class HttpRpcFilterTest {
//
//  Vertx vertx;
//
//  HttpServer server;
//
//  HttpClient httpClient;
//
//  @Before
//  public void before(TestContext context) {
//    vertx = Vertx.vertx();
//    httpClient = vertx.createHttpClient();
//    server = vertx.createHttpServer().requestHandler(req -> {
//      String url = req.path();
//      if (url.equals("/foo") && req.method() == HttpMethod.GET) {
//        System.out.println(req.path());
//        System.out.println(req.absoluteURI());
//        req.response().putHeader("Content-Type", "application/json")
//                .end(new JsonObject()
//                             .put("foo", "bar")
//                             .put("query", req.query())
//                             .encode());
//      }
//      if (url.equals("/foo/array") && req.method() == HttpMethod.GET) {
//        req.response().putHeader("Content-Type", "application/json")
//                .end(new JsonArray()
//                             .add(new JsonObject()
//                                          .put("foo", "bar")
//                                          .encode()).encode());
//      }
//      if (url.equals("/foo") && req.method() == HttpMethod.DELETE) {
//        req.response().putHeader("Content-Type", "application/json")
//                .end(new JsonObject()
//                             .put("foo", "bar")
//                             .encode());
//      }
//      if (url.equals("/foo") && req.method() == HttpMethod.POST) {
//        req.response().putHeader("Content-Type", "application/json");
//        req.bodyHandler(body -> req.response().end(body));
//      }
//      if (url.equals("/foo") && req.method() == HttpMethod.PUT) {
//        req.response().putHeader("Content-Type", "application/json");
//        req.bodyHandler(body -> req.response().end(body));
//      }
//    }).listen(8080, context.asyncAssertSuccess());
//  }
//
//  @After
//  public void after(TestContext context) {
//    vertx.close(context.asyncAssertSuccess());
//  }
//
//  @Test
//  public void testRpc(TestContext testContext) {
//    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, null, null);
//    JsonObject config = new JsonObject()
//            .put("path", "foo?type=2")
//            .put("port", 8080)
//            .put("host", "localhost")
//            .put("name", "user")
//            .put("id", "abc")
//            .put("method", "GET")
//            .put("params", new JsonObject().put("userId", 1));
//    apiContext.addResponse(config);
//
//    HttpRpcFilter filter = new HttpRpcFilter();
//    filter.config(vertx, new JsonObject());
//
//    Future<ApiContext> future = Future.future();
//    filter.doFilter(apiContext, future);
//    future.setHandler(ar -> {
//      if (ar.succeeded()) {
//        ApiContext apiContext1 = ar.response();
//        testContext.assertEquals(1, apiContext1.response().size());
//        JsonObject response = apiContext1.response().getJsonObject(0);
//        System.out.println(apiContext.response());
//        Assert.assertEquals(response.getString("id"), config.getString("id"));
//      } else {
//        testContext.fail();
//      }
//    });;
//  }
//
//}
