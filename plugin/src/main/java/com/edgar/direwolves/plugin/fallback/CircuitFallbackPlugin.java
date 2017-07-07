package com.edgar.direwolves.plugin.fallback;

import com.edgar.direwolves.core.definition.ApiPlugin;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/7/6.
 *
 * @author Edgar  Date 2017/7/6
 */
public class CircuitFallbackPlugin implements ApiPlugin {

  private final JsonObject fallback = new JsonObject();

  @Override
  public String name() {
    return CircuitFallbackPlugin.class.getSimpleName();
  }

  public CircuitFallbackPlugin setFallback(JsonObject fallback) {
    this.fallback.mergeIn(fallback);
    return this;
  }

  public JsonObject fallback() {
    return fallback;
  }
}
