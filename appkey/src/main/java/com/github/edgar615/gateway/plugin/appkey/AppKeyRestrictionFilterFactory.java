package com.github.edgar615.gateway.plugin.appkey;

import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-10-28.
 */
public class AppKeyRestrictionFilterFactory implements FilterFactory {

    @Override
    public String name() {
        return AppKeyRestrictionFilter.class.getSimpleName();
    }

    @Override
    public Filter create(Vertx vertx, JsonObject config) {
        return new AppKeyRestrictionFilter(config);
    }
}
