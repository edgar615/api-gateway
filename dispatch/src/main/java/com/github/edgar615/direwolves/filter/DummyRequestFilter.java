package com.github.edgar615.direwolves.filter;

import com.github.edgar615.direwolves.core.definition.DummyEndpoint;
import com.github.edgar615.direwolves.core.definition.Endpoint;
import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.rpc.RpcRequest;
import com.github.edgar615.direwolves.core.rpc.dummy.DummyRequest;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * Dummy类型的endpoint需要经过这个Filter转换为RpcRequest.
 *
 * @author Edgar  Date 2016/11/18
 */
public class DummyRequestFilter implements Filter {

  DummyRequestFilter() {
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 13000;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition().endpoints().stream()
            .anyMatch(e -> e instanceof DummyEndpoint);
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    apiContext.apiDefinition().endpoints().stream()
            .filter(e -> e instanceof DummyEndpoint)
            .map(e -> toRpc(apiContext, e))
            .forEach(req -> apiContext.addRequest(req));
    completeFuture.complete(apiContext);
  }


  private RpcRequest toRpc(ApiContext apiContext, Endpoint endpoint) {
    if (endpoint instanceof DummyEndpoint) {
      DummyEndpoint dummyEndpoint = (DummyEndpoint) endpoint;
      String id = apiContext.nextRpcId();
      String name = dummyEndpoint.name();
      JsonObject result = dummyEndpoint.result();
      if (result == null) {
        result = new JsonObject();
      }
      return DummyRequest.create(id, name, result);

    }
    return null;
  }
}
