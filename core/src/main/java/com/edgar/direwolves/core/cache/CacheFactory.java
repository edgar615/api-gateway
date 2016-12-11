package com.edgar.direwolves.core.cache;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-7.
 */
public interface CacheFactory {

  /**
   * @return 创建一个缓存
   */
  CacheProvider create(Vertx vertx, JsonObject config);
}
