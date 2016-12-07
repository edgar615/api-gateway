package com.edgar.direwolves.core.cache;

import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.google.common.collect.Lists;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * Created by edgar on 16-12-7.
 */
public interface CacheFactory {

  List<CacheFactory> factories = Lists.newArrayList(ServiceLoader.load(CacheFactory.class));

  static CacheProvider create(String type, Vertx vertx, JsonObject config) {
    List<CacheFactory> list = factories.stream()
        .filter(f -> type.equalsIgnoreCase(f.type()))
        .collect(Collectors.toList());
    if (list.isEmpty()) {
      throw SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND)
          .set("details", type + " CacheFactory not found");
    }
    return list.get(0).create(vertx, config);
  }

  /**
   * @return 缓存的类型.
   */
  String type();

  /**
   * @return 创建一个缓存
   */
  CacheProvider create(Vertx vertx, JsonObject config);
}
