package com.edgar.direwolves.plugin.client;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-10-31.
 */
public class AppKeyCheckerPluginFactory implements ApiPluginFactory<AppKeyCheckerPlugin> {
  @Override
  public String name() {
    return AppKeyCheckerPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new AppKeyCheckerPluginImpl();
  }

  @Override
  public AppKeyCheckerPlugin decode(JsonObject jsonObject) {
    if (jsonObject.getBoolean("app_key_checker", false)) {
      return new AppKeyCheckerPluginImpl();
    }
    return null;
  }

  @Override
  public JsonObject encode(AppKeyCheckerPlugin obj) {
    return new JsonObject().put("app_key_checker", true);
  }
}
