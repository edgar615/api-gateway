package com.github.edgar615.direwolves.core.utils;

import com.github.edgar615.direwolves.core.cache.CacheFactory;
import com.github.edgar615.direwolves.core.cache.CacheManager;
import com.github.edgar615.util.vertx.cache.Cache;
import com.github.edgar615.util.vertx.cache.CacheOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2017/11/6.
 *
 * @author Edgar  Date 2017/11/6
 */
public class CacheUtils {
  private CacheUtils() {
    throw new AssertionError("Not instantiable: " + CacheUtils.class);
  }

  /**
   * 创建一个cache的工具类
   *
   * @param vertx
   * @param name   cache的名称
   * @param config 配置
   * @return
   */
  public static Cache<String, JsonObject> createCache(Vertx vertx,
                                                      String name,
                                                      JsonObject config) {
    //cache
    CacheOptions cacheOptions = new CacheOptions();
    String cacheType = "local";
    if (config.getValue("cache") instanceof JsonObject) {
      JsonObject cacheJson = config.getJsonObject("cache");
      cacheType = cacheJson.getString("type", "local");
      cacheOptions.setExpireAfterWrite(cacheJson.getLong("expireAfterWrite", 1800l));
      cacheOptions.setMaximumSize(cacheJson.getLong("maximumSize", 5000l));
    } else {
      cacheOptions.setExpireAfterWrite(1800l);
      cacheOptions.setMaximumSize(5000l);
    }
    CacheFactory factory = CacheFactory.get(cacheType);
    Cache<String, JsonObject> cache = factory.create(vertx, name, cacheOptions);
    CacheManager.instance().addCache(cache);
    return cache;
  }

}
