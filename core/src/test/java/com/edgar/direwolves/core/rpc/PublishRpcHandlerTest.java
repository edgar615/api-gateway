package com.edgar.direwolves.core.rpc;

import com.edgar.direwolves.core.rpc.eventbus.PublishHandlerFactory;
import com.edgar.direwolves.core.rpc.eventbus.PublishRpcRequest;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

/**
 * Created by Edgar on 2016/4/8.
 *
 * @author Edgar  Date 2016/4/8
 */
@RunWith(VertxUnitRunner.class)
public class PublishRpcHandlerTest {

  static Vertx vertx;

  RpcHandler rpcHandler;

  @BeforeClass
  public static void startServer(TestContext context) {
    vertx = Vertx.vertx();
//    vertx.deployVerticle(DeviceHttpVerticle.class.getName(), context.asyncAssertSuccess());
  }

  @Before
  public void before(TestContext context) {
    rpcHandler = new PublishHandlerFactory().create(vertx, new JsonObject());
//    rpcHandler = new HttpRpcHandler(vertx, new JsonObject());
  }

  @After
  public void after(TestContext context) {
//    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void publishShouldAlwaysReturn200(TestContext context) {
    RpcRequest rpcRequest = PublishRpcRequest.create("abc", "device", "device.get", new
        JsonObject().put("id", 1));

    Future<RpcResponse> future = rpcHandler.handle(rpcRequest);
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        RpcResponse rpcResponse = ar.result();
        context.assertFalse(rpcResponse.isArray());
        context.assertEquals(1, rpcResponse.responseObject().getInteger("result"));
        async.complete();
      } else {
        context.fail();
      }
    });
  }

  @Test
  public void testPublish(TestContext context) {

    String id = UUID.randomUUID().toString();
    Async async = context.async();
    vertx.eventBus().<JsonObject>consumer("device.get", ar -> {
      String eventId = ar.headers().get("id");
      context.assertEquals(id, eventId);
      System.out.println(eventId);
      async.complete();
    });

    RpcRequest rpcRequest = PublishRpcRequest.create(id, "device", "device.get", new
        JsonObject().put("id", 1));

    Future<RpcResponse> future = rpcHandler.handle(rpcRequest);
    Async async2 = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        RpcResponse rpcResponse = ar.result();
        context.assertFalse(rpcResponse.isArray());
        context.assertEquals(1, rpcResponse.responseObject().getInteger("result"));
        async2.complete();
      } else {
        context.fail();
      }
    });
  }
}
