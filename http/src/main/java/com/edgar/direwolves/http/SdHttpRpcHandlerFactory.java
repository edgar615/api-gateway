package com.edgar.direwolves.http;

import com.edgar.direwolves.core.definition.SimpleHttpEndpoint;
import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcHandlerFactory;
import com.edgar.direwolves.core.rpc.RpcMetric;
import com.edgar.direwolves.core.rpc.http.SimpleHttpHandler;
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
