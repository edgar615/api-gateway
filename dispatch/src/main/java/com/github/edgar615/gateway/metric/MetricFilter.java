package com.github.edgar615.gateway.metric;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.metric.ApiMetric;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * 用于记录度量值的filter.
 * 只记录系统中定义了的API，其他请求不考虑
 *
 * @author Edgar  Date 2017/11/10
 */
public class MetricFilter implements Filter {
    private final Vertx vertx;

    public MetricFilter(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
    }

    @Override
    public String type() {
        return PRE;
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE + 1100;
    }

    @Override
    public boolean shouldFilter(ApiContext apiContext) {
        return apiContext.apiDefinition() != null;
    }

    @Override
    public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
        try {
            ApiMetric.apiRequest(apiContext.apiDefinition().name());
        } catch (Exception e) {
            //ignore
        }
        completeFuture.complete(apiContext);
    }
}
