package com.github.edgar615.direwolves.core.rpc.dummy;

import com.github.edgar615.direwolves.core.definition.DummyEndpoint;
import com.github.edgar615.direwolves.core.rpc.RpcHandler;
import com.github.edgar615.direwolves.core.rpc.RpcRequest;
import com.github.edgar615.direwolves.core.rpc.RpcResponse;
import com.github.edgar615.util.log.Log;
import com.github.edgar615.util.log.LogType;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Edgar on 2016/12/30.
 *
 * @author Edgar  Date 2016/12/30
 */
public class DummyRpcHandler implements RpcHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(DummyRpcHandler.class);

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
    Log.create(LOGGER)
            .setTraceId(request.id())
            .setLogType(LogType.LOG)
            .setEvent(type().toUpperCase())
            .setMessage("[0ms] [{}bytes]")
            .addArg(request.result().encode().getBytes().length)
            .info();
    future.complete(RpcResponse.createJsonObject(request.id(), 200, request.result(), 0));
    return future;
  }
}
