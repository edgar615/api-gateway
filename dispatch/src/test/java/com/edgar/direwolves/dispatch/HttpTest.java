package com.edgar.direwolves.dispatch;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Edgar on 2016/4/8.
 *
 * @author Edgar  Date 2016/4/8
 */
@RunWith(VertxUnitRunner.class)
public class HttpTest {

  Vertx vertx;

  HttpServer server;

  HttpClient httpClient;

  @Before
  public void before(TestContext context) {
    vertx = Vertx.vertx();
    httpClient = vertx.createHttpClient();
    server = vertx.createHttpServer().requestHandler(req -> {
      String url = req.path();
      if (url.equals("/foo") && req.method() == HttpMethod.GET) {
        System.out.println(req.path());
        System.out.println(req.absoluteURI());
        req.response().putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                             .put("foo", "bar")
                             .put("query", req.query())
                             .encode());
      }
      if (url.equals("/foo/array") && req.method() == HttpMethod.GET) {
        req.response().putHeader("Content-Type", "application/json")
                .end(new JsonArray()
                             .add(new JsonObject()
                                          .put("foo", "bar")
                                          .encode()).encode());
      }
      if (url.equals("/foo") && req.method() == HttpMethod.DELETE) {
        req.response().putHeader("Content-Type", "application/json")
                .end(new JsonObject()
                             .put("foo", "bar")
                             .encode());
      }
      if (url.equals("/foo") && req.method() == HttpMethod.POST) {
        req.response().putHeader("Content-Type", "application/json");
        req.bodyHandler(body -> req.response().end(body));
      }
      if (url.equals("/foo") && req.method() == HttpMethod.PUT) {
        req.response().putHeader("Content-Type", "application/json");
        req.bodyHandler(body -> req.response().end(body));
      }
    }).listen(8080, context.asyncAssertSuccess());
  }

  @After
  public void after(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }


  @Test
  public void postMethodMustHasBody(TestContext context) {
    JsonObject config = new JsonObject()
            .put("path", "foo")
            .put("port", 8080)
            .put("host", "localhost")
            .put("name", "user")
            .put("id", "abc")
            .put("method", "POST");

    Future<HttpResult> future = Http.request(httpClient, config);
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        async.complete();
      } else {
        context.fail();
      }
    });
  }

  @Test
  public void putMethodMustHasBody(TestContext context) {
    JsonObject config = new JsonObject()
            .put("path", "foo")
            .put("port", 8080)
            .put("host", "localhost")
            .put("name", "user")
            .put("id", "abc")
            .put("method", "PUT");

    Future<HttpResult> future = Http.request(httpClient, config);
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        async.complete();
      } else {
        context.fail();
      }
    });
  }

  @Test
  public void testGet(TestContext context) {
    JsonObject config = new JsonObject()
            .put("path", "foo?type=2")
            .put("port", 8080)
            .put("host", "localhost")
            .put("name", "user")
            .put("id", "abc")
            .put("method", "GET")
            .put("params", new JsonObject().put("userId", 1));

    Future<HttpResult> future = Http.request(httpClient, config);
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        HttpResult httpResult = ar.result();
        context.assertFalse(httpResult.isArray());
        context.assertEquals("bar", httpResult.responseObject().getString("foo"));
        context.assertEquals("type=2&userId=1", httpResult.responseObject().getString("query"));
        context.assertEquals("abc", httpResult.id());
        async.complete();
      } else {
        context.fail();
      }
    });
  }

  @Test
  public void testGet2Param(TestContext context) {
    JsonObject config = new JsonObject()
            .put("path", "foo?type=2")
            .put("port", 8080)
            .put("host", "localhost")
            .put("name", "user")
            .put("id", "abc")
            .put("method", "GET")
            .put("params", new JsonObject().put("userId", new JsonArray().add(1).add(2)));

    Future<HttpResult> future = Http.request(httpClient, config);
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        HttpResult httpResult = ar.result();
        context.assertFalse(httpResult.isArray());
        context.assertEquals("bar", httpResult.responseObject().getString("foo"));
        context.assertEquals("type=2&userId=1&userId=2", httpResult.responseObject().getString
                ("query"));
        context.assertEquals("abc", httpResult.id());
        async.complete();
      } else {
        context.fail();
      }
    });
  }

  @Test
  public void testGetArray(TestContext context) {
    JsonObject config = new JsonObject()
            .put("path", "/foo/array")
            .put("port", 8080)
            .put("host", "localhost")
            .put("name", "user")
            .put("id", "abc")
            .put("method", "GET");

    Future<HttpResult> future = Http.request(httpClient, config);
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        HttpResult httpResult = ar.result();
        context.assertTrue(httpResult.isArray());
        context.assertEquals(1, httpResult.responseArray().size());
        context.assertEquals("abc", httpResult.id());
        async.complete();
      } else {
        context.fail();
      }
    });
  }

  @Test
  public void testDelete(TestContext context) {
    JsonObject config = new JsonObject()
            .put("path", "foo?type=2")
            .put("port", 8080)
            .put("host", "localhost")
            .put("name", "user")
            .put("id", "abc")
            .put("method", "GET")
            .put("params", new JsonObject().put("userId", 2));

    Future<HttpResult> future = Http.request(httpClient, config);
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        HttpResult httpResult = ar.result();
        context.assertFalse(httpResult.isArray());
        context.assertEquals("bar", httpResult.responseObject().getString("foo"));
        context.assertEquals("abc", httpResult.id());
        async.complete();
      } else {
        context.fail();
      }
    });
  }

  @Test
  public void testPost(TestContext context) {
    JsonObject config = new JsonObject()
            .put("path", "foo?type=2")
            .put("port", 8080)
            .put("host", "localhost")
            .put("name", "user")
            .put("id", "abc")
            .put("method", "POST")
            .put("body",new JsonObject().put("foo", "bar"))
            .put("params", new JsonObject().put("userId", 2));

    Future<HttpResult> future = Http.request(httpClient, config);

    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        HttpResult httpResult = ar.result();
        context.assertFalse(httpResult.isArray());
        context.assertEquals("bar", httpResult.responseObject().getString("foo"));
        context.assertEquals("abc", httpResult.id());
        async.complete();
      } else {
        context.fail();
      }
    });
  }

  @Test
  public void testPut(TestContext context) {
    JsonObject config = new JsonObject()
            .put("path", "foo?type=2")
            .put("port", 8080)
            .put("host", "localhost")
            .put("name", "user")
            .put("id", "abc")
            .put("method", "PUT")
            .put("body",new JsonObject().put("foo", "bar"))
            .put("params", new JsonObject().put("userId", 2));

    Future<HttpResult> future = Http.request(httpClient, config);
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        HttpResult httpResult = ar.result();
        context.assertFalse(httpResult.isArray());
        context.assertEquals("bar", httpResult.responseObject().getString("foo"));
        context.assertEquals("abc", httpResult.id());
        async.complete();
      } else {
        context.fail();
      }
    });
  }
}
