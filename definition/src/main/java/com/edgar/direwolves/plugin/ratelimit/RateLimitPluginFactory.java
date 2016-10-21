package com.edgar.direwolves.plugin.ratelimit;

import com.google.common.base.Preconditions;

import com.edgar.direwolves.plugin.ApiPlugin;
import com.edgar.direwolves.plugin.ApiPluginFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/10/21.
 *
 * @author Edgar  Date 2016/10/21
 */
public class RateLimitPluginFactory implements ApiPluginFactory<RateLimitPlugin> {
  @Override
  public RateLimitPlugin decode(JsonObject jsonObject) {
    Preconditions.checkArgument(jsonObject.containsKey("name"), "name cannot be null");
    Preconditions.checkArgument("rate_limit".equalsIgnoreCase(jsonObject.getString("name")),
                                "name must be rate_limit");
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
    JsonObject jsonObject = new JsonObject()
            .put("name", "rate_limit");
    JsonArray rateLimtArray = new JsonArray();
    jsonObject.put("rate_limit", rateLimtArray);
    for (RateLimit rateLimit : rateLimitPlugin.rateLimits()) {
      rateLimtArray.add(new JsonObject()
                                .put("limit", rateLimit.limit())
                                .put("limit_by", rateLimit.limitBy())
                                .put("type", rateLimit.type()));
    }
    return jsonObject;
  }

  @Override
  public String name() {
    return "RATE_LIMIT";
  }

  @Override
  public ApiPlugin create() {
    return new RateLimitPluginImpl();
  }
}
