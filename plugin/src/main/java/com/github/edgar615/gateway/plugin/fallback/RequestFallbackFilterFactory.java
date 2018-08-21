package com.github.edgar615.gateway.plugin.fallback;

import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/8/7.
 *
 * @author Edgar  Date 2017/8/7
 */
public class RequestFallbackFilterFactory implements FilterFactory {
    @Override
    public String name() {
        return RequestFallbackFilter.class.getSimpleName();
    }

    @Override
    public Filter create(Vertx vertx, JsonObject config) {
        return new RequestFallbackFilter();
    }
}
