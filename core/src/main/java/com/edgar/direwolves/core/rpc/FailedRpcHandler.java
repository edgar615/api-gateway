package com.edgar.direwolves.core.rpc;

import io.vertx.core.Future;

/**
 * Created by edgar on 16-12-31.
 */
public class FailedRpcHandler implements RpcHandler {

  private final String failureMessage;

  public FailedRpcHandler(String failureMessage) {
    this.failureMessage = failureMessage;
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
