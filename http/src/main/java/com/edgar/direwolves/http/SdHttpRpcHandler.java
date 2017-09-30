package com.edgar.direwolves.http;

import com.edgar.direwolves.core.rpc.RpcHandler;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.RpcResponse;
import com.edgar.direwolves.core.rpc.http.SimpleHttpHandler;
import com.edgar.direwolves.core.rpc.http.SimpleHttpRequest;
import com.edgar.direwolves.http.loadbalance.LoadBalanceStats;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
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
public class SdHttpRpcHandler implements RpcHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(SdHttpRpcHandler.class);

  private final SimpleHttpHandler httpHandler;

  public SdHttpRpcHandler(Vertx vertx, JsonObject config) {
    this.httpHandler = new SimpleHttpHandler(vertx, config);
  }

  @Override
  public String type() {
    return SdHttpEndpoint.TYPE;
  }

  @Override
  public Future<RpcResponse> handle(RpcRequest rpcRequest) {
    SdHttpRequest sdRequest = (SdHttpRequest) rpcRequest;
    if (sdRequest.record() == null) {
      return Future.failedFuture(SystemException.create(DefaultErrorCode.SERVICE_UNAVAILABLE));
    }
    SimpleHttpRequest httpRequest
            = SimpleHttpRequest.create(rpcRequest.id(), rpcRequest.name());
    httpRequest.setHttpMethod(sdRequest.method());
    httpRequest.setTimeout(sdRequest.timeout());
    httpRequest.setPath(sdRequest.path());
    httpRequest.setHost(sdRequest.host());
    httpRequest.setPort(sdRequest.port());
    httpRequest.setBody(sdRequest.body());
    httpRequest.addParams(sdRequest.params());
    httpRequest.addHeaders(sdRequest.headers());

    String serviceId = sdRequest.record().getRegistration();
    LoadBalanceStats.instance().get(serviceId).incActiveRequests();
    Future<RpcResponse> future = Future.future();
    httpHandler.handle(httpRequest).setHandler(ar -> {
      LoadBalanceStats.instance().get(serviceId).decActiveRequests();
      //TODO 更新权重
      LoadBalanceStats.instance().get(serviceId).decEffectiveWeight(5);
      if (ar.succeeded()) {
        future.complete(ar.result());
      } else {
        future.fail(ar.cause());
      }
    });
    return future;
  }

}
