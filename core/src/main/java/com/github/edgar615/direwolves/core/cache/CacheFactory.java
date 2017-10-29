package com.github.edgar615.direwolves.core.cache;

import com.github.edgar615.util.vertx.cache.Cache;
import com.github.edgar615.util.vertx.cache.CacheOptions;
import com.google.common.collect.Lists;

import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Created by edgar on 16-12-7.
 */
public interface CacheFactory {
  List<CacheFactory> factories
          = Lists.newArrayList(ServiceLoader.load(CacheFactory.class));

  String type();

  /**
   * @return 创建一个缓存
   */
  Cache<String, JsonObject> create(Vertx vertx, String cacheName, CacheOptions options);

  static CacheFactory get(String type) {
    Optional<CacheFactory> factory = factories.stream()
            .filter(f -> f.type().equalsIgnoreCase(type))
            .findAny();
    if (factory.isPresent()) {
      return factory.get();
    }
    throw SystemException.create(DefaultErrorCode.INVALID_ARGS)
            .set("details", "cacheType:" + type);
  }
}
