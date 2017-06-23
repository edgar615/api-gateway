package com.edgar.direvolves.plugin.authentication;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * backendVertifyPlugin的工厂类.
 *
 * @author Edgar  Date 2016/10/31
 */
public class BackendVertifyPluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return BackendVertifyPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new BackendVertifyPlugin();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (jsonObject.getBoolean("backend.vertify", false)) {
      return new BackendVertifyPlugin();
    }
    return null;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    if (plugin == null) {
      return new JsonObject();
    }
    return new JsonObject().put("backend.vertify", true);
  }
}
