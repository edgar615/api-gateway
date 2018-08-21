package com.github.edgar615.gateway.core.dispatch;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-11.
 */
public interface FilterFactory {

    /**
     * @return filter名称
     */
    String name();

    Filter create(Vertx vertx, JsonObject config);
}
