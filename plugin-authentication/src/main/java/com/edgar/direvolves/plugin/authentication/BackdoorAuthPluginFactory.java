package com.edgar.direvolves.plugin.authentication;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * BackdoorAuthPlugin的工厂类.
 *
 * @author Edgar  Date 2016/10/31
 */
public class BackdoorAuthPluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return BackdoorAuthPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new BackdoorAuthPlugin();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (jsonObject.getBoolean("backdoor_auth", false)) {
      return new BackdoorAuthPlugin();
    }
    return null;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    if (plugin == null) {
      return new JsonObject();
    }
    return new JsonObject().put("backdoor_auth", true);
  }
}
