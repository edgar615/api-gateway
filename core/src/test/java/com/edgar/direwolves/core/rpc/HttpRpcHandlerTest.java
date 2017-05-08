package com.edgar.direwolves.core.rpc;

import com.edgar.direwolves.core.rpc.http.HttpRpcHandlerFactory;
import com.edgar.direwolves.core.rpc.http.HttpRpcRequest;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.UUID;

/**
 * Created by Edgar on 2016/4/8.
 *
 * @author Edgar  Date 2016/4/8
 */
@RunWith(VertxUnitRunner.class)
public class HttpRpcHandlerTest {

  static Vertx vertx;

  RpcHandler rpcHandler;

  @BeforeClass
  public static void startServer(TestContext context) {
    vertx = Vertx.vertx();
    vertx.deployVerticle(DeviceHttpVerticle.class.getName(), context.asyncAssertSuccess());
  }

  @Before
  public void before(TestContext context) {
    rpcHandler = new HttpRpcHandlerFactory().create(vertx, new JsonObject(), null);
//    rpcHandler = new HttpRpcHandler(vertx, new JsonObject());
  }

  @After
  public void after(TestContext context) {
//    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void undefinedMethodShouldThrowInvalidArg(TestContext context) {
    HttpRpcRequest rpcRequest = HttpRpcRequest.create("abc", "device")
            .setPath("devices")
            .setPort(8080)
            .setHost("localhost")
            .setHttpMethod(HttpMethod.OPTIONS);

    Future<RpcResponse> future = rpcHandler.handle(rpcRequest);
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        context.fail();
      } else {
        Throwable t = ar.cause();
        context.assertTrue(t instanceof SystemException);
        SystemException ex = (SystemException) t;
        context.assertEquals(DefaultErrorCode.INVALID_ARGS, ex.getErrorCode());
        async.complete();
      }
    });
  }

  @Test
  public void postMissBodyShouldThrowMissArg(TestContext context) {
    HttpRpcRequest rpcRequest = HttpRpcRequest.create("abc", "device")
            .setPath("devices")
            .setPort(8080)
            .setHost("localhost")
            .setHttpMethod(HttpMethod.POST);

    Future<RpcResponse> future = rpcHandler.handle(rpcRequest);
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        context.fail();
      } else {
        Throwable t = ar.cause();
        context.assertTrue(t instanceof SystemException);
        SystemException ex = (SystemException) t;
        context.assertEquals(DefaultErrorCode.MISSING_ARGS, ex.getErrorCode());
        async.complete();
      }
    });
  }

  @Test
  public void putMissBodyShouldThrowMissArg(TestContext context) {
    HttpRpcRequest rpcRequest = HttpRpcRequest.create("abc", "device")
            .setPath("devices")
            .setPort(8080)
            .setHost("localhost")
            .setHttpMethod(HttpMethod.PUT);

    Future<RpcResponse> future = rpcHandler.handle(rpcRequest);
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        context.fail();
      } else {
        Throwable t = ar.cause();
        context.assertTrue(t instanceof SystemException);
        SystemException ex = (SystemException) t;
        context.assertEquals(DefaultErrorCode.MISSING_ARGS, ex.getErrorCode());
        async.complete();
      }
    });
  }

  @Test
  public void testGet(TestContext context) {
    HttpRpcRequest rpcRequest = HttpRpcRequest.create("abc", "device")
            .setPath("devices/1?type=2")
            .setPort(8080)
            .setHost("localhost")
            .setHttpMethod(HttpMethod.GET)
            .addHeader("x-req-id", UUID.randomUUID().toString())
            .addParam("userId", "2")
            .addParam("userId", "1");

    Assert.assertEquals("http", rpcRequest.type());
    Future<RpcResponse> future = rpcHandler.handle(rpcRequest);
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        RpcResponse rpcResponse = ar.result();
        context.assertFalse(rpcResponse.isArray());
        context.assertEquals("1", rpcResponse.responseObject().getString("id"));
        context.assertEquals("type=2&userId=2",
                             rpcResponse.responseObject().getString("query"));
        async.complete();
      } else {
        context.fail();
      }
    });
  }

  @Test
  public void testGetArray(TestContext context) {
    HttpRpcRequest rpcRequest = HttpRpcRequest.create("abc", "device")
            .setPath("/devices")
            .setPort(8080)
            .setHost("localhost")
            .setHttpMethod(HttpMethod.GET);

    Future<RpcResponse> future = rpcHandler.handle(rpcRequest);
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        RpcResponse rpcResponse = ar.result();
        context.assertTrue(rpcResponse.isArray());
        context.assertEquals(2, rpcResponse.responseArray().size());
        async.complete();
      } else {
        context.fail();
      }
    });
  }

  @Test
  public void testDelete(TestContext context) {
    HttpRpcRequest rpcRequest = HttpRpcRequest.create("abc", "device")
            .setPath("devices")
            .setPort(8080)
            .setHost("localhost")
            .setHttpMethod(HttpMethod.DELETE)
            .addParam("userId", "2");

    Future<RpcResponse> future = rpcHandler.handle(rpcRequest);
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        RpcResponse rpcResponse = ar.result();
        context.assertFalse(rpcResponse.isArray());
        context.assertEquals("1", rpcResponse.responseObject().getString("result"));
        async.complete();
      } else {
        ar.cause().printStackTrace();
        context.fail();
      }
    });
  }

  @Test
  public void testPost(TestContext context) {
    HttpRpcRequest rpcRequest = HttpRpcRequest.create("abc", "device")
            .setPath("devices?type=2")
            .setPort(8080)
            .setHost("localhost")
            .setHttpMethod(HttpMethod.POST)
            .setBody(new JsonObject().put("foo", "bar"))
            .addParam("userId", "2");

    Future<RpcResponse> future = rpcHandler.handle(rpcRequest);

    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        RpcResponse rpcResponse = ar.result();
        context.assertFalse(rpcResponse.isArray());
        context.assertEquals("bar", rpcResponse.responseObject().getString("foo"));
        context.assertEquals("abc", rpcResponse.id());
        async.complete();
      } else {
        context.fail();
      }
    });
  }

  @Test
  public void testPut(TestContext context) {
    HttpRpcRequest rpcRequest = HttpRpcRequest.create("abc", "device")
            .setPath("devices?type=2")
            .setPort(8080)
            .setHost("localhost")
            .setHttpMethod(HttpMethod.PUT)
            .setBody(new JsonObject().put("foo", "bar"))
            .addParam("userId", "2");

    Future<RpcResponse> future = rpcHandler.handle(rpcRequest);
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        RpcResponse rpcResponse = ar.result();
        context.assertFalse(rpcResponse.isArray());
        context.assertEquals("bar", rpcResponse.responseObject().getString("foo"));
        context.assertEquals("abc", rpcResponse.id());
        async.complete();
      } else {
        context.fail();
      }
    });
  }
}
