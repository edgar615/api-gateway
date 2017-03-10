package com.edgar.direvolves.plugin.authentication;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * BackdoorVertifyPlugin的工厂类.
 *
 * @author Edgar  Date 2016/10/31
 */
public class BackdoorVertifyPluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return BackdoorVertifyPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new BackdoorAuthPlugin();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (jsonObject.getBoolean("backdoor_vertify", false)) {
      return new BackdoorVertifyPlugin();
    }
    return null;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    if (plugin == null) {
      return new JsonObject();
    }
    return new JsonObject().put("backdoor_vertify", true);
  }
}
