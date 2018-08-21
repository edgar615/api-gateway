package com.github.edgar615.gateway.plugin.transformer;

import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.dispatch.FilterFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * 将endpoint转换为json对象.
 * params和header的json均为{"k1", ["v1"]}，{"k1", ["v1", "v2]}格式的json对象.
 * <p>
 * Created by edgar on 16-9-20.
 */
public class EventbusRequestTransformerFilterFactory implements FilterFactory {


    @Override
    public String name() {
        return EventbusRequestTransformerFilter.class.getSimpleName();
    }

    @Override
    public Filter create(Vertx vertx, JsonObject config) {
        return new EventbusRequestTransformerFilter(config);
    }
}