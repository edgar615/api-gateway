package com.edgar.direwolves.plugin.authorization;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-25.
 */
public class AuthorisePluginFactory implements ApiPluginFactory<AuthorisePlugin> {
  @Override
  public String name() {
    return AuthorisePlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new AuthorisePluginImpl();
  }

  @Override
  public AuthorisePlugin decode(JsonObject jsonObject) {
    String scope = jsonObject.getString("scope", "default");
    return new AuthorisePluginImpl(scope);
  }

  @Override
  public JsonObject encode(AuthorisePlugin plugin) {
    if (plugin == null) {
      return new JsonObject();
    }
    return new JsonObject().put("scope", plugin.scope());
  }
}
