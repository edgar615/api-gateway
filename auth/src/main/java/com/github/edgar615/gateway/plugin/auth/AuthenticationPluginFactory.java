package com.github.edgar615.gateway.plugin.auth;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * TokenPlugin的工厂类.
 *
 * @author Edgar  Date 2016/10/31
 */
public class AuthenticationPluginFactory implements ApiPluginFactory {
    @Override
    public String name() {
        return AuthenticationPlugin.class.getSimpleName();
    }

    @Override
    public ApiPlugin create() {
        return new AuthenticationPlugin();
    }

    @Override
    public ApiPlugin decode(JsonObject jsonObject) {

        if (jsonObject.getBoolean("authentication", false)) {
            return new AuthenticationPlugin();
        }
        return null;
    }

    @Override
    public JsonObject encode(ApiPlugin plugin) {
        if (plugin == null) {
            return new JsonObject();
        }
        return new JsonObject().put("authentication", true);
    }
}
