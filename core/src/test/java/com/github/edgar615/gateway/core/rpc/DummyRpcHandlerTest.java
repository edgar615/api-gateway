package com.github.edgar615.gateway.core.rpc;

import com.github.edgar615.gateway.core.rpc.dummy.DummyHandlerFactory;
import com.github.edgar615.gateway.core.rpc.dummy.DummyRequest;
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

/**
 * Created by Edgar on 2016/4/8.
 *
 * @author Edgar  Date 2016/4/8
 */
@RunWith(VertxUnitRunner.class)
public class DummyRpcHandlerTest {

    static Vertx vertx;

    RpcHandler rpcHandler;

    @BeforeClass
    public static void startServer(TestContext context) {
        vertx = Vertx.vertx();
    }

    @Before
    public void before(TestContext context) {
        rpcHandler = new DummyHandlerFactory().create(vertx, new JsonObject());
    }

    @After
    public void after(TestContext context) {
//    vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void dummyShouldAlwaysReturn200(TestContext context) {
        RpcRequest rpcRequest = DummyRequest.create("abc", "device",
                                                    new JsonObject().put("result", 1));

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
}
