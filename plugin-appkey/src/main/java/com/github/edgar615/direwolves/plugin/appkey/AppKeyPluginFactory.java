package com.github.edgar615.direwolves.plugin.appkey;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-10-31.
 */
public class AppKeyPluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return AppKeyPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new AppKeyPluginImpl();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (jsonObject.getBoolean("appKey", false)) {
      return new AppKeyPluginImpl();
    }
    return null;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    if (plugin == null) {
      return new JsonObject();
    }
    return new JsonObject().put("appKey", true);
  }
}
