package com.edgar.direwolves.rpc.http;

import com.edgar.direwolves.core.rpc.Result;
import com.edgar.util.exception.DefaultErrorCode;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

/**
 * Created by Edgar on 2016/11/2.
 *
 * @author Edgar  Date 2016/11/2
 */
@RunWith(VertxUnitRunner.class)
public class HttpRpcVerticleTest {

  Vertx vertx;

  String address = "direwolves.rpc.http.req";

  @Before
  public void setUp(TestContext testContext) {
    vertx = Vertx.vertx();
    vertx.deployVerticle(DeviceHttpVerticle.class.getName(), testContext.asyncAssertSuccess());
    vertx.deployVerticle(HttpRpcVerticle.class.getName(), testContext.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext testContext) {
//    vertx.close(testContext.asyncAssertSuccess());
    vertx.close();
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

    Async async = context.async();
    vertx.eventBus().<JsonObject>send(address, config, ar -> {
      if (ar.succeeded()) {
        JsonObject jsonObject = ar.result().body();
        Result result = createBodyResult(jsonObject);
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

    Async async = context.async();
    vertx.eventBus().<JsonObject>send(address, config, ar -> {
      if (ar.succeeded()) {
        JsonObject jsonObject = ar.result().body();
        Result result = createBodyResult(jsonObject);
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
  public void testDelete(TestContext context) {
    JsonObject config = new JsonObject()
            .put("path", "devices?type=2")
            .put("port", 8080)
            .put("host", "localhost")
            .put("name", "user")
            .put("id", "abc")
            .put("method", "DELETE")
            .put("params", new JsonObject().put("userId", 2));

    Async async = context.async();
    vertx.eventBus().<JsonObject>send(address, config, ar -> {
      if (ar.succeeded()) {
        Result result = createBodyResult(ar.result().body());
        context.assertFalse(result.isArray());
        context.assertEquals("1", result.responseObject().getString("result"));
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
    Async async = context.async();
    vertx.eventBus().<JsonObject>send(address, config, ar -> {
      if (ar.succeeded()) {
        Result result = createArrayResult(ar.result().body());
        context.assertTrue(result.isArray());
        context.assertEquals(2, result.responseArray().size());
        async.complete();
      } else {
        context.fail();
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

    Async async = context.async();
    vertx.eventBus().<JsonObject>send(address, config, ar -> {
      if (ar.succeeded()) {
        Result result = createBodyResult(ar.result().body());
        context.assertFalse(result.isArray());
        context.assertEquals("1", result.responseObject().getString("id"));
        async.complete();
      } else {
        context.fail();
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

    Async async = context.async();
    vertx.eventBus().<JsonObject>send(address, config, ar -> {
      if (ar.succeeded()) {
        context.fail();
      } else {
        Throwable t = ar.cause();
        ReplyException e = (ReplyException) t;
        context.assertEquals(DefaultErrorCode.MISSING_ARGS.getNumber(), e.failureCode());
        async.complete();
      }
    });
  }

  @Test
  public void postMethodMustHasBody(TestContext context) {
    JsonObject config = new JsonObject()
            .put("path", "devices")
            .put("port", 8080)
            .put("host", "localhost")
            .put("name", "user")
            .put("id", "abc")
            .put("method", "post");

    Async async = context.async();
    vertx.eventBus().<JsonObject>send(address, config, ar -> {
      if (ar.succeeded()) {
        context.fail();
      } else {
        Throwable t = ar.cause();
        ReplyException e = (ReplyException) t;
        context.assertEquals(DefaultErrorCode.MISSING_ARGS.getNumber(), e.failureCode());
        async.complete();
      }
    });
  }

  public Result createArrayResult(JsonObject jsonObject) {
    String id = jsonObject.getString("id", UUID.randomUUID().toString());
    long elapsedTime = jsonObject.getLong("elapsedTime", 0l);
    int statusCode = jsonObject.getInteger("statusCode", 200);
    JsonArray body = jsonObject.getJsonArray("responseArray");
    return Result.createJsonArray(id, statusCode, body, elapsedTime);
  }

  public Result createBodyResult(JsonObject jsonObject) {
    String id = jsonObject.getString("id", UUID.randomUUID().toString());
    long elapsedTime = jsonObject.getLong("elapsedTime", 0l);
    int statusCode = jsonObject.getInteger("statusCode", 200);
    JsonObject body = jsonObject.getJsonObject("responseBody");
    return Result.createJsonObject(id, statusCode, body, elapsedTime);
  }
}
