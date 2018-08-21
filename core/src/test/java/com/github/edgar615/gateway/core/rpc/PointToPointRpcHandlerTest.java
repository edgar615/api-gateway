package com.github.edgar615.gateway.core.rpc;

import com.github.edgar615.gateway.core.definition.EventbusEndpoint;
import com.github.edgar615.gateway.core.rpc.eventbus.EventbusHandlerFactory;
import com.github.edgar615.gateway.core.rpc.eventbus.EventbusRpcRequest;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2016/4/8.
 *
 * @author Edgar  Date 2016/4/8
 */
@RunWith(VertxUnitRunner.class)
public class PointToPointRpcHandlerTest {

    static Vertx vertx;

    RpcHandler rpcHandler;

    @BeforeClass
    public static void startServer(TestContext context) {
        vertx = Vertx.vertx();
//    vertx.deployVerticle(DeviceHttpVerticle.class.getName(), context.asyncAssertSuccess());
    }

    @Before
    public void before(TestContext context) {
        rpcHandler = new EventbusHandlerFactory().create(vertx, new JsonObject());
//    rpcHandler = new HttpRpcHandler(vertx, new JsonObject());
    }

    @After
    public void after(TestContext context) {
//    vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void pointToPointShouldAlwaysReturn200(TestContext context) {
        String address = UUID.randomUUID().toString();
        RpcRequest rpcRequest = EventbusRpcRequest.create("abc", "device", address,
                                                          EventbusEndpoint.POINT_POINT, null,
                                                          new JsonObject().put("id", 1));

        Future<RpcResponse> future = rpcHandler.handle(rpcRequest);
        AtomicBoolean complete = new AtomicBoolean();
        future.setHandler(ar -> {
            if (ar.succeeded()) {
                RpcResponse rpcResponse = ar.result();
                context.assertFalse(rpcResponse.isArray());
                context.assertEquals(1, rpcResponse.responseObject().getInteger("result"));
                complete.set(true);
            } else {
                context.fail();
            }
        });

        Awaitility.await().until(() -> complete.get());
    }

    @Test
    public void testSend(TestContext context) {

        String address = UUID.randomUUID().toString();
        String id = UUID.randomUUID().toString();
        AtomicBoolean complete1 = new AtomicBoolean();
        vertx.eventBus().<JsonObject>consumer(address, ar -> {
            String eventId = ar.headers().get("x-request-id");
            context.assertEquals(id, eventId);
            System.out.println(eventId);
            complete1.set(true);
        });

        RpcRequest rpcRequest = EventbusRpcRequest
                .create(id, "device", address, EventbusEndpoint.POINT_POINT, null,
                        new JsonObject().put("id", 1));

        Future<RpcResponse> future = rpcHandler.handle(rpcRequest);
        AtomicBoolean complete2 = new AtomicBoolean();
        future.setHandler(ar -> {
            if (ar.succeeded()) {
                RpcResponse rpcResponse = ar.result();
                context.assertFalse(rpcResponse.isArray());
                context.assertEquals(1, rpcResponse.responseObject().getInteger("result"));
                complete2.set(true);
            } else {
                context.fail();
            }
        });
        Awaitility.await().until(() -> complete2.get());
        Awaitility.await().until(() -> complete1.get());
    }
}
