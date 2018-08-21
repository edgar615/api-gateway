package com.github.edgar615.gateway.plugin.auth;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * TokenPlugin的工厂类.
 *
 * @author Edgar  Date 2016/10/31
 */
public class TokenPluginFactory implements ApiPluginFactory {
    @Override
    public String name() {
        return TokenPlugin.class.getSimpleName();
    }

    @Override
    public ApiPlugin create() {
        return new TokenPlugin();
    }

    @Override
    public ApiPlugin decode(JsonObject jsonObject) {

        if (jsonObject.getBoolean("token", false)) {
            return new TokenPlugin();
        }
        return null;
    }

    @Override
    public JsonObject encode(ApiPlugin plugin) {
        if (plugin == null) {
            return new JsonObject();
        }
        return new JsonObject().put("token", true);
    }
}
