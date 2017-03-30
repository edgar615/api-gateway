package com.edgar.direwolves.plugin.arg;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/3/30.
 *
 * @author Edgar  Date 2017/3/30
 */
public class StrictArgPluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return StrictArgPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new StrictArgPlugin(true);
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (jsonObject.containsKey("strict_arg")) {
      return new StrictArgPlugin(jsonObject.getBoolean("strict_arg"));
    }
    return null;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    StrictArgPlugin strictArgPlugin = (StrictArgPlugin) plugin;
    return new JsonObject().put("strict_arg", strictArgPlugin.strict());
  }
}
