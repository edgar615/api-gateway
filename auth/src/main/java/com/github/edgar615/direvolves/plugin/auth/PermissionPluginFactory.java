package com.github.edgar615.direvolves.plugin.auth;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * 权限校验的工厂类.
 * Created by edgar on 16-12-25.
 */
public class PermissionPluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return PermissionPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new PermissionPluginImpl();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (jsonObject.containsKey("permission")) {
      String scope = jsonObject.getString("permission", "default");
      return new PermissionPluginImpl(scope);
    }
    return null;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    if (plugin == null) {
      return new JsonObject();
    }
    PermissionPlugin permissionPlugin = (PermissionPlugin) plugin;
    return new JsonObject().put("permission", permissionPlugin.permission());
  }
}
