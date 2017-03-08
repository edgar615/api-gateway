package com.edgar.direwolves.core.rpc.eventbus;

import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.definition.PublishEndpoint;
import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcHandlerFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-31.
 */
public class PublishHandlerFactory implements RpcHandlerFactory {
  @Override
  public String type() {
    return PublishEndpoint.TYPE;
  }

  @Override
  public RpcHandler create(Vertx vertx, JsonObject config) {
    return new PublishRpcHandler(vertx, config);
  }
}
