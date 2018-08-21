package com.github.edgar615.gateway.core.rpc;

import io.vertx.core.Future;

/**
 * 该RPC调用会直接返回失败.
 * Created by edgar on 16-12-31.
 */
public class FailureRpcHandler implements RpcHandler {

    /**
     * 错误消息
     */
    private final String failureMessage;

    FailureRpcHandler(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    public static FailureRpcHandler create(String failureMessage) {
        return new FailureRpcHandler(failureMessage);
    }

    @Override
    public String type() {
        return "failed";
    }

    @Override
    public Future<RpcResponse> handle(RpcRequest rpcRequest) {
        return Future.failedFuture(failureMessage);
    }
}
