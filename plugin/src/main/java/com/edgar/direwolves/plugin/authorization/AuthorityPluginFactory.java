package com.edgar.direwolves.plugin.authorization;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-25.
 */
public class AuthorityPluginFactory implements ApiPluginFactory<ApiPlugin> {
  @Override
  public String name() {
    return AuthorityPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new AuthorityPluginImpl();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    String scope = jsonObject.getString("scope", "default");
    return new AuthorityPluginImpl(scope);
  }

  @Override
  public JsonObject encode(ApiPlugin obj) {
    return new JsonObject().put("scope", true);
  }
}
