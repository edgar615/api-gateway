package com.github.edgar615.gateway.core.plugin.scope;

import com.github.edgar615.gateway.core.definition.ApiPlugin;
import com.github.edgar615.gateway.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * 权限校验的工厂类.
 * Created by edgar on 16-12-25.
 */
public class ScopePluginFactory implements ApiPluginFactory {
    @Override
    public String name() {
        return ScopePlugin.class.getSimpleName();
    }

    @Override
    public ApiPlugin create() {
        return new ScopePluginImpl();
    }

    @Override
    public ApiPlugin decode(JsonObject jsonObject) {
        if (jsonObject.containsKey("scope")) {
            String scope = jsonObject.getString("scope", "default");
            return new ScopePluginImpl(scope);
        }
        return null;
    }

    @Override
    public JsonObject encode(ApiPlugin plugin) {
        if (plugin == null) {
            return new JsonObject();
        }
        ScopePlugin scopePlugin = (ScopePlugin) plugin;
        return new JsonObject().put("scope", scopePlugin.scope());
    }
}
