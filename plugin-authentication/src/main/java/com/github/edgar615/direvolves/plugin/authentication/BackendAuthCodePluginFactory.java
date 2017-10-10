package com.github.edgar615.direvolves.plugin.authentication;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * backendAuthPlugin的工厂类.
 *
 * @author Edgar  Date 2016/10/31
 */
public class BackendAuthCodePluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return BackendAuthCodePlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new BackendAuthCodePlugin();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (jsonObject.getBoolean("backend.code", false)) {
      return new BackendAuthCodePlugin();
    }
    return null;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    if (plugin == null) {
      return new JsonObject();
    }
    return new JsonObject().put("backend.code", true);
  }
}
