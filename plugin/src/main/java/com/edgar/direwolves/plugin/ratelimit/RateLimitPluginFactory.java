package com.edgar.direwolves.plugin.ratelimit;

import com.edgar.direwolves.core.spi.ApiPlugin;
import com.edgar.direwolves.core.spi.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 流量控制的工厂类.
 * json配置
 * <pre>
 *    "rate_limit" : [
 * {
 * "limit" : 1000,
 * "limit_by" : "token",
 * "type" : "hour"
 * },
 * {
 * "limit" : 100,
 * "limit_by" : "app_key",
 * "type" : "second"
 * }
 * ]
 * </pre>
 *
 * @author Edgar  Date 2016/10/21
 */
public class RateLimitPluginFactory implements ApiPluginFactory<RateLimitPlugin> {
  @Override
  public RateLimitPlugin decode(JsonObject jsonObject) {
    if (!jsonObject.containsKey("rate_limit")) {
      return null;
    }
    JsonArray jsonArray = jsonObject.getJsonArray("rate_limit", new JsonArray());
    RateLimitPlugin rateLimitPlugin = new RateLimitPluginImpl();
    for (int i = 0; i < jsonArray.size(); i++) {
      JsonObject rateLimitJson = jsonArray.getJsonObject(i);
      String type = rateLimitJson.getString("type");
      String limitBy = rateLimitJson.getString("limit_by");
      int limit = rateLimitJson.getInteger("limit");
      rateLimitPlugin.addRateLimit(RateLimit.create(limitBy, type, limit));
    }
    return rateLimitPlugin;
  }

  @Override
  public JsonObject encode(RateLimitPlugin rateLimitPlugin) {
    JsonArray rateLimtArray = new JsonArray();
    for (RateLimit rateLimit : rateLimitPlugin.rateLimits()) {
      rateLimtArray.add(new JsonObject()
          .put("limit", rateLimit.limit())
          .put("limit_by", rateLimit.limitBy())
          .put("type", rateLimit.type()));
    }
    return new JsonObject().put("rate_limit", rateLimtArray);
  }

  @Override
  public String name() {
    return RateLimitPlugin.NAME;
  }

  @Override
  public ApiPlugin create() {
    return new RateLimitPluginImpl();
  }
}
