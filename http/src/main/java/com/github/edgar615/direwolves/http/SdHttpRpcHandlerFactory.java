package com.github.edgar615.direwolves.http;

import com.github.edgar615.direwolves.core.rpc.RpcHandler;
import com.github.edgar615.direwolves.core.rpc.RpcHandlerFactory;
import com.github.edgar615.direwolves.core.rpc.RpcMetric;
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
  public RpcHandler create(Vertx vertx, JsonObject config, RpcMetric metric) {
    return new SdHttpRpcHandler(vertx, config);
  }
}
