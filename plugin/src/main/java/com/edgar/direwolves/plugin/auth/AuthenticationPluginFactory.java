package com.edgar.direwolves.plugin.auth;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
public class AuthenticationPluginFactory implements ApiPluginFactory<AuthenticationPlugin> {
  @Override
  public String name() {
    return AuthenticationPlugin.NAME;
  }

  @Override
  public ApiPlugin create() {
    return new AuthenticationPluginImpl();
  }

  @Override
  public AuthenticationPlugin decode(JsonObject jsonObject) {
    AuthenticationPlugin authenticationPlugin = new AuthenticationPluginImpl();
    JsonArray jsonArray = jsonObject.getJsonArray("authentication", new JsonArray());
    for (int i = 0; i < jsonArray.size(); i++) {
      authenticationPlugin.add(jsonArray.getString(i));
    }
    return authenticationPlugin;
  }

  @Override
  public JsonObject encode(AuthenticationPlugin plugin) {
    return new JsonObject().put("authentication", new JsonArray(plugin.authentications()));
  }
}
