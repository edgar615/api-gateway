package com.github.edgar615.gateway.core.definition;

import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/1/6.
 *
 * @author Edgar  Date 2017/1/6
 */
public class MockPluginFactory implements ApiPluginFactory {
    @Override
    public String name() {
        return MockPlugin.class.getSimpleName();
    }

    @Override
    public ApiPlugin create() {
        return new MockPlugin();
    }

    @Override
    public ApiPlugin decode(JsonObject jsonObject) {
        if (jsonObject.getBoolean("mock", false)) {
            return new MockPlugin();
        }
        return null;
    }

    @Override
    public JsonObject encode(ApiPlugin plugin) {
        if (plugin == null) {
            return new JsonObject();
        }
        return new JsonObject().put("mock", true);
    }
}
