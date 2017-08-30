package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.definition.SimpleHttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.http.SimpleHttpRequest;
import io.vertx.core.Future;

/**
 * simple-http类型的endpoint需要经过这个Filter转换为RpcRequest.
 * <p>
 *
 * @author Edgar  Date 2016/11/18
 */
public class SimpleHttpRequestFilter implements Filter {

  SimpleHttpRequestFilter() {
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 10000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition().endpoints().stream()
            .anyMatch(e -> e instanceof SimpleHttpEndpoint);
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    apiContext.apiDefinition().endpoints().stream()
            .filter(e -> e instanceof SimpleHttpEndpoint)
            .map(e -> (SimpleHttpEndpoint) e)
            .map(e -> toRpc(apiContext, e))
            .forEach(r -> apiContext.addRequest(r));
    completeFuture.complete(apiContext);
  }


  private RpcRequest toRpc(ApiContext apiContext, SimpleHttpEndpoint endpoint) {
    SimpleHttpRequest httpRpcRequest =
            SimpleHttpRequest.create(apiContext.nextRpcId(), endpoint.name());
    httpRpcRequest.setPath(endpoint.path());
    httpRpcRequest.setHttpMethod(endpoint.method());
    httpRpcRequest.addParams(apiContext.params());
//    httpRpcRequest.addHeaders(apiContext.headers());
    httpRpcRequest.addHeader("x-request-id", httpRpcRequest.id());
    httpRpcRequest.setBody(apiContext.body());
    httpRpcRequest.setHost(endpoint.host());
    httpRpcRequest.setPort(endpoint.port());
    return httpRpcRequest;
  }

}
