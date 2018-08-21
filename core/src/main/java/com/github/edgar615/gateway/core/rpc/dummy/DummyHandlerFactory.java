package com.github.edgar615.gateway.core.rpc.dummy;

import com.github.edgar615.gateway.core.definition.DummyEndpoint;
import com.github.edgar615.gateway.core.rpc.RpcHandler;
import com.github.edgar615.gateway.core.rpc.RpcHandlerFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-31.
 */
public class DummyHandlerFactory implements RpcHandlerFactory {
    @Override
    public String type() {
        return DummyEndpoint.TYPE;
    }

    @Override
    public RpcHandler create(Vertx vertx, JsonObject config) {
        return new DummyRpcHandler(vertx, config);
    }
}
