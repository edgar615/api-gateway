package com.edgar.direwolves.core.rpc;

import io.vertx.core.Future;

/**
 * Created by Edgar on 2016/12/30.
 *
 * @author Edgar  Date 2016/12/30
 */
public interface RpcHandler {

  Future<RpcResponse> handle(RpcRequest rpcRequest);
}
