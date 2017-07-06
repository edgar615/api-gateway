package com.edgar.direwolves.plugin;

import com.edgar.direwolves.core.definition.ApiPlugin;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/7/6.
 *
 * @author Edgar  Date 2017/7/6
 */
public class RequestFallbackPlugin implements ApiPlugin {

  private final JsonObject fallback = new JsonObject();

  @Override
  public String name() {
    return RequestFallbackPlugin.class.getSimpleName();
  }

  public RequestFallbackPlugin setFallback(JsonObject fallback) {
    this.fallback.mergeIn(fallback);
    return this;
  }

  public JsonObject fallback() {
    return fallback;
  }
}
