package com.edgar.direwolves.plugin.arg;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

public class StrictArgPluginFactory implements ApiPluginFactory<StrictArgPlugin> {

  @Override
  public String name() {
    return StrictArgPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new StrictArgPluginImpl();
  }

  @Override
  public StrictArgPlugin decode(JsonObject jsonObject) {
    boolean strictArg = jsonObject.getBoolean("strict_arg", false);
    if (strictArg) {
      return new StrictArgPluginImpl();
    }
    return null;

  }

  @Override
  public JsonObject encode(StrictArgPlugin plugin) {
    if (plugin == null) {
      return new JsonObject();
    }
    return new JsonObject()
            .put("strict_arg", true);
  }

}
