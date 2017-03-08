package com.edgar.direwolves.core.rpc.http;

import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.RpcResponse;
import com.edgar.direwolves.core.rpc.eventbus.PointToPointHandlerFactory;
import com.edgar.direwolves.core.rpc.eventbus.PointToPointRpcRequest;
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
    rpcHandler = new PointToPointHandlerFactory().create(vertx, new JsonObject());
//    rpcHandler = new HttpRpcHandler(vertx, new JsonObject());
  }

  @After
  public void after(TestContext context) {
//    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void pointToPointShouldAlwaysReturn200(TestContext context) {
    RpcRequest rpcRequest = PointToPointRpcRequest.create("abc", "device", "device.get", new
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
}
