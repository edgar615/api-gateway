package com.edgar.direwolves.plugin.authentication;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
public class JwtCreatePluginFactory implements ApiPluginFactory<JwtCreatePlugin> {
  @Override
  public String name() {
    return JwtCreatePlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new JwtCreatePluginImpl();
  }

  @Override
  public JwtCreatePlugin decode(JsonObject jsonObject) {
    if (jsonObject.getBoolean("jwt_create", false)) {
      return new JwtCreatePluginImpl();
    }
    return null;
  }

  @Override
  public JsonObject encode(JwtCreatePlugin plugin) {
    return new JsonObject().put("jwt_create",true);
  }
}
