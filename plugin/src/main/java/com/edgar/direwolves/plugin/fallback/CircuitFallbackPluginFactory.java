package com.edgar.direwolves.plugin.fallback;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/7/6.
 *
 * @author Edgar  Date 2017/7/6
 */
public class CircuitFallbackPluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return CircuitFallbackPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new CircuitFallbackPlugin();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (!jsonObject.containsKey("circuit.fallback")) {
      return null;
    }
    return new CircuitFallbackPlugin().setFallback(jsonObject.getJsonObject("circuit.fallback"));
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    if (plugin == null) {
      return new JsonObject();
    }
    CircuitFallbackPlugin fallbackPlugin = (CircuitFallbackPlugin) plugin;
    return new JsonObject()
            .put("circuit.fallback", fallbackPlugin.fallback());
  }
}
