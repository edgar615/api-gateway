package com.edgar.direwolves.core.rpc.http;

import com.edgar.direwolves.core.rpc.RpcResponse;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
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
  public void postMissBodyShouldThrowMissArg(TestContext context) {
    HttpRpcRequest rpcRequest = HttpRpcRequest.create("abc", "device")
            .setPath("devices")
            .setPort(8080)
            .setHost("localhost")
            .setHttpMethod(HttpMethod.POST);

    Future<RpcResponse> future = Http.request(httpClient, rpcRequest);
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

    Future<RpcResponse> future = Http.request(httpClient, rpcRequest);
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
            .addParam("userId", "2")
            .addParam("userId", "1");
    Future<RpcResponse> future = Http.request(httpClient, rpcRequest);
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

    Future<RpcResponse> future = Http.request(httpClient, rpcRequest);
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

    Future<RpcResponse> future = Http.request(httpClient, rpcRequest);
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

    Future<RpcResponse> future = Http.request(httpClient, rpcRequest);

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

    Future<RpcResponse> future = Http.request(httpClient, rpcRequest);
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
