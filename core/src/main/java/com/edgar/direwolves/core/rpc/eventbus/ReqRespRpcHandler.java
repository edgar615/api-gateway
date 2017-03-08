package com.edgar.direwolves.core.rpc.eventbus;

import com.google.common.base.Strings;

import com.edgar.direwolves.core.definition.PublishEndpoint;
import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.RpcResponse;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/12/30.
 *
 * @author Edgar  Date 2016/12/30
 */
public class ReqRespRpcHandler implements RpcHandler {

  private final Vertx vertx;

  ReqRespRpcHandler(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
  }

  @Override
  public String type() {
    return PublishEndpoint.TYPE;
  }

  @Override
  public Future<RpcResponse> handle(RpcRequest rpcRequest) {
    ReqRespRpcRequest request = (ReqRespRpcRequest) rpcRequest;
    Future<RpcResponse> future = Future.future();
    DeliveryOptions deliveryOptions = new DeliveryOptions();
    if (!Strings.isNullOrEmpty(request.action())) {
      deliveryOptions.addHeader("action", request.action());
    }
    vertx.eventBus().<JsonObject>send(request.address(), request.message(), deliveryOptions, ar -> {
      if (ar.succeeded()) {
        if (!ar.result().body().containsKey("result")) {
          JsonObject result = ar.result().body();
          future.complete(RpcResponse.createJsonObject(request.id(), 200, result, 0));
        } else {
          Object result = ar.result().body().getValue("result");
          if (result instanceof JsonArray) {
            future.complete(RpcResponse.createJsonArray(request.id(), 200, (JsonArray) result, 0));
          } else if (result instanceof JsonObject) {
            future.complete(
                    RpcResponse.createJsonObject(request.id(), 200, (JsonObject) result, 0));
          } else {
            future.complete(RpcResponse.createJsonObject(request.id(), 200, ar.result().body(), 0));
          }

        }
      } else {
        future.fail(ar.cause());
      }
    });

    return future;
  }

}
