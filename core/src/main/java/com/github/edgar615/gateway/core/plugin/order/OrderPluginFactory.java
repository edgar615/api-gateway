package com.github.edgar615.gateway.core.plugin.order;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

public class OrderPluginFactory implements ApiPluginFactory {
    @Override
    public String name() {
        return OrderPlugin.class.getSimpleName();
    }

    @Override
    public ApiPlugin create() {
        return new OrderPlugin(0);
    }

    @Override
    public ApiPlugin decode(JsonObject jsonObject) {
        if (jsonObject.getValue("order") instanceof Integer) {
            return new OrderPlugin(jsonObject.getInteger("order"));
        }
        return null;
    }

    @Override
    public JsonObject encode(ApiPlugin plugin) {
        return new JsonObject().put("order", ((OrderPlugin) plugin).order());
    }
}
