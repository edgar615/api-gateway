package com.edgar.direwolves.plugin.fallback;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import com.edgar.direwolves.core.rpc.RpcResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

/**
 * Created by Edgar on 2017/7/6.
 *
 * @author Edgar  Date 2017/7/6
 */
public class FallbackPluginFactory implements ApiPluginFactory {
  @Override
  public String name() {
    return FallbackPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new FallbackPlugin();
  }

  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (jsonObject.getValue("request.fallback") instanceof JsonObject) {
      JsonObject config = jsonObject.getJsonObject("request.fallback", new JsonObject());
      FallbackPlugin plugin = new FallbackPlugin();
      for (String key : config.fieldNames()) {
        JsonObject fallbackConfig = config.getJsonObject(key, new JsonObject());
        RpcResponse response = createResponse(fallbackConfig);
        plugin.addFallBack(key, response);
      }
      return plugin;
    }
    return null;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    if (plugin == null) {
      return new JsonObject();
    }
    FallbackPlugin fallbackPlugin = (FallbackPlugin) plugin;
    JsonObject jsonObject = new JsonObject();
    for (String key : fallbackPlugin.fallback().keySet()) {
      RpcResponse response = fallbackPlugin.fallback().get(key);
      JsonObject fallback = new JsonObject()
              .put("statusCode", response.statusCode());
      if (response.isArray()) {
        fallback.put("result", response.responseArray());
      } else {
        fallback.put("result", response.responseObject());
      }
      jsonObject.put(key, fallback);
    }
    return new JsonObject()
            .put("request.fallback", jsonObject);
  }

  private RpcResponse createResponse(JsonObject fallbackConfig) {
    int statusCode = fallbackConfig.getInteger("statusCode", 200);
    RpcResponse response = null;
    if (fallbackConfig.getValue("result") instanceof JsonObject) {
      response = RpcResponse.create(UUID.randomUUID().toString(),
                                    statusCode,
                                    fallbackConfig.getJsonObject("result").encode(), 0);
    } else if (fallbackConfig.getValue("result") instanceof JsonArray) {
      response = RpcResponse.create(UUID.randomUUID().toString(),
                                    statusCode,
                                    fallbackConfig.getJsonArray("result").encode(), 0);
    }
    return response;
  }
}
