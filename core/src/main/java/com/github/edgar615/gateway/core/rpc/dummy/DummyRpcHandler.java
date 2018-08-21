package com.github.edgar615.gateway.core.rpc.dummy;

import com.github.edgar615.gateway.core.definition.DummyEndpoint;
import com.github.edgar615.gateway.core.rpc.RpcHandler;
import com.github.edgar615.gateway.core.rpc.RpcRequest;
import com.github.edgar615.gateway.core.rpc.RpcResponse;
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
        LOGGER.info("[{}] [DUMMY] [OK] [{}bytes] [{}ms]", request.id(),
                    request.result().encode().getBytes().length, 0);
        future.complete(RpcResponse.createJsonObject(request.id(), 200, request.result(), 0));
        return future;
    }
}
