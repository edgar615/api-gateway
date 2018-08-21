package com.github.edgar615.gateway.core.rpc;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.definition.EventbusEndpoint;
import com.github.edgar615.gateway.core.rpc.eventbus.EventbusHandlerFactory;
import com.github.edgar615.gateway.core.rpc.eventbus.EventbusRpcRequest;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
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
    public void undefinedReqShouldAlwaysThrowUnavaliable(TestContext context) {
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
                ar.cause().printStackTrace();
                context.assertTrue(ar.cause() instanceof SystemException);
                SystemException ex = (SystemException) ar.cause();
                context.assertEquals(DefaultErrorCode.SERVICE_UNAVAILABLE, ex.getErrorCode());
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
            JsonObject jsonObject = new JsonObject().put("result", 1);
            msg.reply(jsonObject);
            async.complete();
        });

        RpcRequest rpcRequest = EventbusRpcRequest.create(id, "device", address,
                                                          EventbusEndpoint.REQ_RESP,
                                                          null,
                                                          new JsonObject().put("id", 1));

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

            context.assertEquals("abcdefg", msg.headers().get("action"));
            JsonObject jsonObject = new JsonObject().put("result", 1);
            msg.reply(jsonObject);
            async.complete();
        });

        Multimap<String, String> headers = ArrayListMultimap.create();
        headers.put("action", "abcdefg");

        RpcRequest rpcRequest = EventbusRpcRequest.create(id, "device", address,
                                                          EventbusEndpoint.REQ_RESP,
                                                          headers,
                                                          new JsonObject().put("id", 1));

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
    public void testReplyError(TestContext context) {

        String address = UUID.randomUUID().toString();
        String id = UUID.randomUUID().toString();
        Async async = context.async();
        vertx.eventBus().<JsonObject>consumer(address, msg -> {
            String eventId = msg.headers().get("x-request-id");
            context.assertEquals(id, eventId);
            System.out.println(msg.headers());
            context.assertFalse(msg.headers().contains("action"));
            msg.fail(1012, "test");
            async.complete();
        });

        RpcRequest rpcRequest = EventbusRpcRequest.create(id, "device", address,
                                                          EventbusEndpoint.REQ_RESP,
                                                          null,
                                                          new JsonObject().put("id", 1));

        Future<RpcResponse> future = rpcHandler.handle(rpcRequest);
        Async async2 = context.async();
        future.setHandler(ar -> {
            if (ar.succeeded()) {
                context.fail();
            } else {
                ar.cause().printStackTrace();
                async2.complete();
            }
        });
    }

}
