package com.edgar.direwolves.definition;

import io.vertx.core.json.JsonObject;

import java.util.function.Function;

/**
 * 从JsonObject转换为RateLimit对象
 *
 * @author Edgar  Date 2016/9/30
 */
class RateLimitDecoder implements Function<JsonObject, RateLimit> {

  private static final RateLimitDecoder INSTANCE = new RateLimitDecoder();

  private RateLimitDecoder() {
  }

  static Function<JsonObject, RateLimit> instance() {
    return INSTANCE;
  }

  @Override
  public RateLimit apply(JsonObject jsonObject) {
    String type = jsonObject.getString("type");
    String limitBy = jsonObject.getString("limit_by");
    int limit = jsonObject.getInteger("limit");
    return RateLimit.create(limitBy, type, limit);
  }

}
