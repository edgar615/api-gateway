package com.github.edgar615.gateway.plugin.auth;

import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * TokenFilter的工厂类.
 * Created by edgar on 16-12-11.
 */
public class TokenFilterFactory implements FilterFactory {
    @Override
    public String name() {
        return TokenFilter.class.getSimpleName();
    }

    @Override
    public Filter create(Vertx vertx, JsonObject config) {
        return new TokenFilter(vertx, config);
    }
}
