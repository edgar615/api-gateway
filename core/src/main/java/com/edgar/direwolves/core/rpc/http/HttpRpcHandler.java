package com.edgar.direwolves.core.rpc.http;

import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.RpcResponse;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/12/30.
 *
 * @author Edgar  Date 2016/12/30
 */
public class HttpRpcHandler implements RpcHandler {

  private final HttpClient httpClient;

  HttpRpcHandler(Vertx vertx, JsonObject config) {
    this.httpClient = vertx.createHttpClient();
  }

  @Override
  public Future<RpcResponse> handle(RpcRequest rpcRequest) {
    return null;
  }
}
