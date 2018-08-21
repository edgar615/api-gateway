package com.github.edgar615.gateway.plugin.auth;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * UserLoaderPlugin的工厂类.
 *
 * @author Edgar  Date 2016/10/31
 */
public class UserLoaderPluginFactory implements ApiPluginFactory {
    @Override
    public String name() {
        return UserLoaderPlugin.class.getSimpleName();
    }

    @Override
    public ApiPlugin create() {
        return new UserLoaderPlugin();
    }

    @Override
    public ApiPlugin decode(JsonObject jsonObject) {

        if (jsonObject.getBoolean("user.loader", false)) {
            return new AuthenticationPlugin();
        }
        return null;
    }

    @Override
    public JsonObject encode(ApiPlugin plugin) {
        if (plugin == null) {
            return new JsonObject();
        }
        return new JsonObject().put("user.loader", true);
    }
}
