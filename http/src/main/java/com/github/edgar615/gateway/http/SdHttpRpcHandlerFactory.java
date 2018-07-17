package com.github.edgar615.gateway.http;

import com.github.edgar615.gateway.core.rpc.RpcHandler;
import com.github.edgar615.gateway.core.rpc.RpcHandlerFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-31.
 */
public class SdHttpRpcHandlerFactory implements RpcHandlerFactory {
  @Override
  public String type() {
    return SdHttpEndpoint.TYPE;
  }

  @Override
  public RpcHandler create(Vertx vertx, JsonObject config) {
    return new SdHttpRpcHandler(vertx, config);
  }
}
