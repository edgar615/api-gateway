package com.edgar.direwolves.plugin.ratelimit;

import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 流量控制的工厂类.
 * json配置
 * <pre>
 *    "rate.limiter" : [
 * {
 * "burst" : 1000,
 * "name" : "token"
 * },
 * {
 * "burst" : 100,
 * "name" : "device"
 * }
 * ]
 * </pre>
 *
 * @author Edgar  Date 2016/10/21
 */
public class RateLimitPluginFactory implements ApiPluginFactory {
  @Override
  public ApiPlugin decode(JsonObject jsonObject) {
    if (!jsonObject.containsKey("rate.limiter")) {
      return null;
    }
    JsonArray jsonArray = jsonObject.getJsonArray("rate.limiter", new JsonArray());
    RateLimiterPlugin rateLimiterPlugin = new RateLimiterPluginImpl();
    for (int i = 0; i < jsonArray.size(); i++) {
      JsonObject rateLimiterJson = jsonArray.getJsonObject(i);
      String type = rateLimiterJson.getString("name");
      long burst = rateLimiterJson.getLong("burst", 0l);
      rateLimiterPlugin.addRateLimiter(new RateLimiter(type, burst));
    }
    return rateLimiterPlugin;
  }

  @Override
  public JsonObject encode(ApiPlugin plugin) {
    RateLimiterPlugin rateLimiterPlugin = (RateLimiterPlugin) plugin;
    JsonArray rateLimiterArray = new JsonArray();
    for (RateLimiter rateLimiter : rateLimiterPlugin.rateLimiters()) {
      rateLimiterArray.add(new JsonObject()
                                .put("name", rateLimiter.name())
                                .put("burst", rateLimiter.burst()));
    }
    return new JsonObject().put("rate.limiter", rateLimiterArray);
  }

  @Override
  public String name() {
    return RateLimiterPlugin.class.getSimpleName();
  }

  @Override
  public ApiPlugin create() {
    return new RateLimiterPluginImpl();
  }
}
