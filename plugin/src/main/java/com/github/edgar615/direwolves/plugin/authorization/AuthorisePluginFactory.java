package com.github.edgar615.direwolves.plugin.authorization;

import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * 权限校验的工厂类.
 * Created by edgar on 16-12-25.
 */
public class AuthorisePluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return AuthorisePlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new AuthorisePluginImpl();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (jsonObject.containsKey("scope")) {
      String scope = jsonObject.getString("scope", "default");
      return new AuthorisePluginImpl(scope);
    }
    return null;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    if (plugin == null) {
      return new JsonObject();
    }
    AuthorisePlugin authorisePlugin = (AuthorisePlugin) plugin;
    return new JsonObject().put("scope", authorisePlugin.scope());
  }
}
