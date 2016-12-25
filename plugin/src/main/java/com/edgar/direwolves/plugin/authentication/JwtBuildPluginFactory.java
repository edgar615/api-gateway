package com.edgar.direwolves.plugin.authentication;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
public class JwtBuildPluginFactory implements ApiPluginFactory<JwtBuildPlugin> {
  @Override
  public String name() {
    return JwtBuildPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new JwtBuildPluginImpl();
  }

  @Override
  public JwtBuildPlugin decode(JsonObject jsonObject) {
    if (jsonObject.getBoolean("jwt_build", false)) {
      return new JwtBuildPluginImpl();
    }
    return null;
  }

  @Override
  public JsonObject encode(JwtBuildPlugin plugin) {
    return new JsonObject().put("jwt_build",true);
  }
}
