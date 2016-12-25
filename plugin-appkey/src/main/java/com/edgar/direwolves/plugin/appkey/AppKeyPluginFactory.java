package com.edgar.direwolves.plugin.appkey;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-10-31.
 */
public class AppKeyPluginFactory implements ApiPluginFactory<AppKeyPlugin> {
  @Override
  public String name() {
    return AppKeyPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new AppKeyPluginImpl();
  }

  @Override
  public AppKeyPlugin decode(JsonObject jsonObject) {
    if (jsonObject.getBoolean("appkey", false)) {
      return new AppKeyPluginImpl();
    }
    return null;
  }

  @Override
  public JsonObject encode(AppKeyPlugin plugin) {
    if (plugin == null) {
      return new JsonObject();
    }
    return new JsonObject().put("appkey", true);
  }
}
