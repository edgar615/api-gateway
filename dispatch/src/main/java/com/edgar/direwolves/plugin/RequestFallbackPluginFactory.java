package com.edgar.direwolves.plugin;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/7/6.
 *
 * @author Edgar  Date 2017/7/6
 */
public class RequestFallbackPluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return RequestFallbackPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new RequestFallbackPlugin();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (!jsonObject.containsKey("request.fallback")) {
      return null;
    }
    return new RequestFallbackPlugin().setFallback(jsonObject.getJsonObject("request.fallback"));
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    if (plugin == null) {
      return new JsonObject();
    }
    RequestFallbackPlugin fallbackPlugin = (RequestFallbackPlugin) plugin;
    return new JsonObject()
            .put("request.fallback", fallbackPlugin.fallback());
  }
}
