package com.edgar.direvolves.plugin.authentication;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
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
    if (jsonObject.getBoolean("backend_code", false)) {
      return new BackendAuthCodePlugin();
    }
    return null;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    if (plugin == null) {
      return new JsonObject();
    }
    return new JsonObject().put("backend_code", true);
  }
}
