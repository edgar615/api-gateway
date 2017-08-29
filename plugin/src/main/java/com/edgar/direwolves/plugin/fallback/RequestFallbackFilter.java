package com.edgar.direwolves.plugin.fallback;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.Fallbackable;
import com.edgar.direwolves.core.rpc.RpcRequest;
import com.edgar.direwolves.core.rpc.http.HttpRpcRequest;
import io.vertx.core.Future;

/**
 * Created by Edgar on 2017/8/7.
 *
 * @author Edgar  Date 2017/8/7
 */
public class RequestFallbackFilter implements Filter {
  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return 10100;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return apiContext.apiDefinition().plugin(FallbackPlugin.class.getSimpleName()) != null;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    FallbackPlugin plugin = (FallbackPlugin) apiContext.apiDefinition()
            .plugin(FallbackPlugin.class.getSimpleName());
    for (RpcRequest request : apiContext.requests()) {
      if (plugin.fallback().containsKey(request.name())
              && request instanceof Fallbackable) {
        Fallbackable fallbackable = (Fallbackable) request;
        fallbackable.setFallback(plugin.fallback().get(request.name()).copy());
      }
    }
    completeFuture.complete(apiContext);
  }
}
