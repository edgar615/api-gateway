package com.github.edgar615.gateway.plugin.auth;

import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * UserLoaderFilter的工厂类.
 * Created by edgar on 16-12-11.
 */
public class UserLoaderFilterFactory implements FilterFactory {
    @Override
    public String name() {
        return UserLoaderFilter.class.getSimpleName();
    }

    @Override
    public Filter create(Vertx vertx, JsonObject config) {
        return new UserLoaderFilter(vertx, config);
    }
}