package com.edgar.direwolves.core.rpc.eventbus;

import com.edgar.direwolves.core.definition.PointToPointEndpoint;
import com.edgar.direwolves.core.definition.PublishEndpoint;
import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.RpcResponse;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/12/30.
 *
 * @author Edgar  Date 2016/12/30
 */
public class PointToPointRpcHandler implements RpcHandler {

  private final Vertx vertx;

  PointToPointRpcHandler(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
  }

  @Override
  public String type() {
    return PointToPointEndpoint.TYPE;
  }

  @Override
  public Future<RpcResponse> handle(RpcRequest rpcRequest) {
    PointToPointRpcRequest request = (PointToPointRpcRequest) rpcRequest;
    Future<RpcResponse> future = Future.future();
    vertx.eventBus().send(request.address(), request.message());
    JsonObject result = new JsonObject()
            .put("result", 1);
    future.complete(RpcResponse.createJsonObject(request.id(), 200, result, 0));
    return future;
  }

}
