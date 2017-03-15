package com.edgar.direwolves.core.rpc.dummy;

import com.edgar.direwolves.core.definition.DummyEndpoint;
import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.RpcResponse;
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
    LOGGER.info("------> [{}] [{}] [{}] [{}] [{}] [{}]",
        request.id(),
        type().toUpperCase(),
        request.result() == null ? "no body" : request.result().encode()
    );

    LOGGER.info("<------ [{}] [{}] [{}ms] [{} bytes]",
        request.id(),
        request.type().toUpperCase(),
        0,
        request.result().encode().getBytes().length
    );

    future.complete(RpcResponse.createJsonObject(request.id(), 200, request.result(), 0));
    return future;
  }
}
