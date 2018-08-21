package com.github.edgar615.gateway.core.rpc;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-31.
 */
public interface RpcHandlerFactory {

    String type();

    RpcHandler create(Vertx vertx, JsonObject config);
}
