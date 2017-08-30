package com.edgar.direwolves.core.cache;

import com.google.common.collect.Lists;

import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
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
  Cache create(Vertx vertx, String cacheName, JsonObject config);

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
