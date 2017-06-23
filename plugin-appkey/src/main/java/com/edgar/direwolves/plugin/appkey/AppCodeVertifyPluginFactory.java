package com.edgar.direwolves.plugin.appkey;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-10-31.
 */
public class AppCodeVertifyPluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return AppCodeVertifyPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new AppCodeVertifyPlugin();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (jsonObject.getBoolean("app.code.vertify", false)) {
      return new AppCodeVertifyPlugin();
    }
    return null;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    if (plugin == null) {
      return new JsonObject();
    }
    return new JsonObject().put("app.code.vertify", true);
  }
}
