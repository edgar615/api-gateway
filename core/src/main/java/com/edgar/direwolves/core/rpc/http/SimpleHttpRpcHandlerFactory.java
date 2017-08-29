package com.edgar.direwolves.core.rpc.http;

import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.definition.SimpleHttpEndpoint;
import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcHandlerFactory;
import com.edgar.direwolves.core.rpc.RpcMetric;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-31.
 */
public class SimpleHttpRpcHandlerFactory implements RpcHandlerFactory {
  @Override
  public String type() {
    return SimpleHttpEndpoint.TYPE;
  }

  @Override
  public RpcHandler create(Vertx vertx, JsonObject config, RpcMetric metric) {
    return new SimpleHttpHandler(vertx, config);
  }
}
