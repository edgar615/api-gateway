package com.github.edgar615.gateway.plugin.fallback;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.rpc.Fallbackable;
import com.github.edgar615.gateway.core.rpc.RpcRequest;
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
        return 14000;
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
