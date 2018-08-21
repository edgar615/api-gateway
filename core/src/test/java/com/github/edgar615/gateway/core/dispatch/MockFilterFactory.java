package com.github.edgar615.gateway.core.dispatch;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/1/5.
 *
 * @author Edgar  Date 2017/1/5
 */
public class MockFilterFactory implements FilterFactory {
    @Override
    public String name() {
        return MockFilter.class.getSimpleName();
    }

    @Override
    public Filter create(Vertx vertx, JsonObject config) {
        return new MockFilter();
    }
}
