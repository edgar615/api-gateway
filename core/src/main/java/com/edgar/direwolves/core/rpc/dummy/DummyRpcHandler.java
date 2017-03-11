package com.edgar.direwolves.core.rpc.dummy;

import com.edgar.direwolves.core.definition.DummyEndpoint;
import com.edgar.direwolves.core.definition.EventbusEndpoint;
import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.RpcResponse;
import com.edgar.direwolves.core.rpc.eventbus.EventbusRpcRequest;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.google.common.collect.Multimap;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/12/30.
 *
 * @author Edgar  Date 2016/12/30
 */
public class DummyRpcHandler implements RpcHandler {

  private final Vertx vertx;

  DummyRpcHandler(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
  }

  @Override
  public String type() {
    return DummyEndpoint.TYPE;
  }

  @Override
  public Future<RpcResponse> handle(RpcRequest rpcRequest) {
    DummyRequest request = (DummyRequest) rpcRequest;
    Future<RpcResponse> future = Future.future();
    future.complete(RpcResponse.createJsonObject(request.id(), 200, request.result(), 0));
    return future;
  }
}
