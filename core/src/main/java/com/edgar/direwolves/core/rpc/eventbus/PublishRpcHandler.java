package com.edgar.direwolves.core.rpc.eventbus;

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
public class PublishRpcHandler implements RpcHandler {

  private final Vertx vertx;

  PublishRpcHandler(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
  }

  @Override
  public String type() {
    return PublishEndpoint.TYPE;
  }

  @Override
  public Future<RpcResponse> handle(RpcRequest rpcRequest) {
    PublishRpcRequest request = (PublishRpcRequest) rpcRequest;
    Future<RpcResponse> future = Future.future();
    vertx.eventBus().publish(request.address(), request.message());
    JsonObject result = new JsonObject()
            .put("result", 1);
    future.complete(RpcResponse.createJsonObject(request.id(), 200, result, 0));
    return future;
  }

}
