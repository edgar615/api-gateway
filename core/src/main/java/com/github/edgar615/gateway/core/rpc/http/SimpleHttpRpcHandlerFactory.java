package com.github.edgar615.gateway.core.rpc.http;

import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.gateway.core.rpc.RpcHandler;
import com.github.edgar615.gateway.core.rpc.RpcHandlerFactory;
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
    public RpcHandler create(Vertx vertx, JsonObject config) {
        return new SimpleHttpHandler(vertx, config);
    }
}
