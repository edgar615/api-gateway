package com.edgar.direwolves.rpc.http;

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
    vertx.deployVerticle(DeviceHttpVerticle.class.getName(), context.asyncAssertSuccess());
  }

  @After
  public void after(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }


  @Test
  public void postMethodMustHasBody(TestContext context) {
    JsonObject config = new JsonObject()
            .put("path", "devices")
            .put("port", 8080)
            .put("host", "localhost")
            .put("name", "user")
            .put("id", "abc")
            .put("method", "POST");

    Future<HttpResult> future = Http.request(httpClient, new HttpRequestOptions(config));
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        context.fail();
      } else {
        Throwable t = ar.cause();
        context.assertTrue(t instanceof  UnsupportedOperationException);
        async.complete();
      }
    });
  }

  @Test
  public void putMethodMustHasBody(TestContext context) {
    JsonObject config = new JsonObject()
            .put("path", "devices")
            .put("port", 8080)
            .put("host", "localhost")
            .put("name", "user")
            .put("id", "abc")
            .put("method", "PUT");

    Future<HttpResult> future = Http.request(httpClient, new HttpRequestOptions(config));
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        context.fail();
      } else {
        Throwable t = ar.cause();
        context.assertTrue(t instanceof  UnsupportedOperationException);
        async.complete();
      }
    });
  }

  @Test
  public void testGet(TestContext context) {
    JsonObject config = new JsonObject()
            .put("path", "devices/1?type=2")
            .put("port", 8080)
            .put("host", "localhost")
            .put("name", "user")
            .put("id", "abc")
            .put("method", "GET")
            .put("params", new JsonObject().put("userId", 1));

    Future<HttpResult> future = Http.request(httpClient, new HttpRequestOptions(config));
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        HttpResult httpResult = ar.result();
        context.assertFalse(httpResult.isArray());
        context.assertEquals("1", httpResult.responseObject().getString("id"));
        async.complete();
      } else {
        context.fail();
      }
    });
  }

  @Test
  public void testGetArray(TestContext context) {
    JsonObject config = new JsonObject()
            .put("path", "/devices")
            .put("port", 8080)
            .put("host", "localhost")
            .put("name", "user")
            .put("id", "abc")
            .put("method", "GET");

    Future<HttpResult> future = Http.request(httpClient, new HttpRequestOptions(config));
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        HttpResult httpResult = ar.result();
        context.assertTrue(httpResult.isArray());
        context.assertEquals(2, httpResult.responseArray().size());
        async.complete();
      } else {
        context.fail();
      }
    });
  }

  @Test
  public void testDelete(TestContext context) {
    JsonObject config = new JsonObject()
            .put("path", "devices?type=2")
            .put("port", 8080)
            .put("host", "localhost")
            .put("name", "user")
            .put("id", "abc")
            .put("method", "DELETE")
            .put("params", new JsonObject().put("userId", 2));

    Future<HttpResult> future = Http.request(httpClient, new HttpRequestOptions(config));
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        HttpResult httpResult = ar.result();
        context.assertFalse(httpResult.isArray());
        context.assertEquals("1", httpResult.responseObject().getString("result"));
        async.complete();
      } else {
        context.fail();
      }
    });
  }

  @Test
  public void testPost(TestContext context) {
    JsonObject config = new JsonObject()
            .put("path", "devices?type=2")
            .put("port", 8080)
            .put("host", "localhost")
            .put("name", "user")
            .put("id", "abc")
            .put("method", "POST")
            .put("body",new JsonObject().put("foo", "bar"))
            .put("params", new JsonObject().put("userId", 2));

    Future<HttpResult> future = Http.request(httpClient, new HttpRequestOptions(config));

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
            .put("path", "devices?type=2")
            .put("port", 8080)
            .put("host", "localhost")
            .put("name", "user")
            .put("id", "abc")
            .put("method", "PUT")
            .put("body",new JsonObject().put("foo", "bar"))
            .put("params", new JsonObject().put("userId", 2));

    Future<HttpResult> future = Http.request(httpClient, new HttpRequestOptions(config));
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
