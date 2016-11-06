package com.edgar.direwolves.rpc.http;

import com.edgar.direwolves.core.rpc.Result;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServer;
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

    Future<Result> future = Http.request(httpClient, new HttpRequestOptions(config));
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        context.fail();
      } else {
        Throwable t = ar.cause();
        context.assertTrue(t instanceof UnsupportedOperationException);
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

    Future<Result> future = Http.request(httpClient, new HttpRequestOptions(config));
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        context.fail();
      } else {
        Throwable t = ar.cause();
        context.assertTrue(t instanceof UnsupportedOperationException);
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

    Future<Result> future = Http.request(httpClient, new HttpRequestOptions(config));
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        Result result = ar.result();
        context.assertFalse(result.isArray());
        context.assertEquals("1", result.responseObject().getString("id"));
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

    Future<Result> future = Http.request(httpClient, new HttpRequestOptions(config));
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        Result result = ar.result();
        context.assertTrue(result.isArray());
        context.assertEquals(2, result.responseArray().size());
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

    Future<Result> future = Http.request(httpClient, new HttpRequestOptions(config));
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        Result result = ar.result();
        context.assertFalse(result.isArray());
        context.assertEquals("1", result.responseObject().getString("result"));
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
        .put("body", new JsonObject().put("foo", "bar"))
        .put("params", new JsonObject().put("userId", 2));

    Future<Result> future = Http.request(httpClient, new HttpRequestOptions(config));

    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        Result result = ar.result();
        context.assertFalse(result.isArray());
        context.assertEquals("bar", result.responseObject().getString("foo"));
        context.assertEquals("abc", result.id());
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
        .put("body", new JsonObject().put("foo", "bar"))
        .put("params", new JsonObject().put("userId", 2));

    Future<Result> future = Http.request(httpClient, new HttpRequestOptions(config));
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        Result result = ar.result();
        context.assertFalse(result.isArray());
        context.assertEquals("bar", result.responseObject().getString("foo"));
        context.assertEquals("abc", result.id());
        async.complete();
      } else {
        context.fail();
      }
    });
  }
}
