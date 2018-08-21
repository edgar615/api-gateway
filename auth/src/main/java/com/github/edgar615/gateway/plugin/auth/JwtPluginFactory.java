package com.github.edgar615.gateway.plugin.auth;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * AuthenticationPlugin的工厂类.
 *
 * @author Edgar  Date 2016/10/31
 */
public class JwtPluginFactory implements ApiPluginFactory {
    @Override
    public String name() {
        return JwtPlugin.class.getSimpleName();
    }

    @Override
    public ApiPlugin create() {
        return new JwtPluginImpl();
    }

    @Override
    public ApiPlugin decode(JsonObject jsonObject) {

        if (jsonObject.getBoolean("authentication", false)) {
            return new JwtPluginImpl();
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
