package com.edgar.direwolves.core.rpc;

import com.edgar.direwolves.core.definition.EventbusEndpoint;
import com.edgar.direwolves.core.rpc.eventbus.EventbusHandlerFactory;
import com.edgar.direwolves.core.rpc.eventbus.EventbusRpcRequest;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
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
public class ReqRespRpcHandlerTest {

  static Vertx vertx;

  RpcHandler rpcHandler;

  @BeforeClass
  public static void startServer(TestContext context) {
    vertx = Vertx.vertx();
  }

  @Before
  public void before(TestContext context) {
    rpcHandler = new EventbusHandlerFactory().create(vertx, new JsonObject());
  }

  @After
  public void after(TestContext context) {
//    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void reqShouldAlwaysReturnThrowResourceNotFound(TestContext context) {
    String address = UUID.randomUUID().toString();
    RpcRequest rpcRequest = EventbusRpcRequest.create("abc", "device", address,
                                                      EventbusEndpoint.REQ_RESP,
                                                      null,
                                                      new JsonObject().put("id", 1));

    Future<RpcResponse> future = rpcHandler.handle(rpcRequest);
    Async async = context.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        context.fail();
      } else {
        context.assertTrue(ar.cause() instanceof SystemException);
        SystemException ex = (SystemException) ar.cause();
        context.assertEquals(DefaultErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        async.complete();
      }
    });
  }

  @Test
  public void testSendNoAction(TestContext context) {

    String address = UUID.randomUUID().toString();
    String id = UUID.randomUUID().toString();
    Async async = context.async();
    vertx.eventBus().<JsonObject>consumer(address, msg -> {
      String eventId = msg.headers().get("x-request-id");
      context.assertEquals(id, eventId);
      System.out.println(msg.headers());

      context.assertFalse(msg.headers().contains("action"));
      msg.reply(new JsonObject().put("result", 1));
      async.complete();
    });

    RpcRequest rpcRequest = EventbusRpcRequest.create(id, "device", address,
                                                      EventbusEndpoint.REQ_RESP,
        null,
        new
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

  @Test
  public void testSendWithAction(TestContext context) {

    String address = UUID.randomUUID().toString();
    String id = UUID.randomUUID().toString();
    Async async = context.async();
    vertx.eventBus().<JsonObject>consumer(address, msg -> {
      String eventId = msg.headers().get("x-request-id");
      context.assertEquals(id, eventId);
      System.out.println(msg.headers());

      context.assertEquals("abcdefg",msg.headers().get("action"));
          msg.reply(new JsonObject().put("result", 1));
      async.complete();
    });

    RpcRequest rpcRequest = EventbusRpcRequest.create(id, "device", address,
                                                     EventbusEndpoint.REQ_RESP,
        new JsonObject().put("action", "abcdefg"),
        new
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
