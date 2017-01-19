package com.edgar.direwolves.plugin.appkey;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
public class AppKeyUpdatePluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return AppKeyUpdatePlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new AppKeyUpdatePluginImpl();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (jsonObject.getBoolean("appkey_update", false)) {
      return new AppKeyUpdatePluginImpl();
    }
    return null;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    return new JsonObject().put("appkey_update", true);
  }
}
