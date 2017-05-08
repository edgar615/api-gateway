package com.edgar.direwolves.core.rpc.eventbus;

import com.edgar.direwolves.core.definition.EventbusEndpoint;
import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcHandlerFactory;
import com.edgar.direwolves.core.rpc.RpcMetric;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-31.
 */
public class EventbusHandlerFactory implements RpcHandlerFactory {
  @Override
  public String type() {
    return EventbusEndpoint.TYPE;
  }

  @Override
  public RpcHandler create(Vertx vertx, JsonObject config, RpcMetric metric) {
    return new EventbusRpcHandler(vertx, config);
  }
}
