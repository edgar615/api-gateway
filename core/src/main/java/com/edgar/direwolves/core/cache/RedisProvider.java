package com.edgar.direwolves.core.cache;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.ServiceHelper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-7.
 */
@ProxyGen
public interface RedisProvider {
  CacheFactory factory = ServiceHelper.loadFactory(CacheFactory.class);

  /**
   * 从缓存中获取值.
   *
   * @param key     缓存的键值，不能为 {@code null}.
   * @param handler 回调函数.
   */
  void get(String key, Handler<AsyncResult<JsonObject>> handler);

  /**
   * 将键值对放入缓存.
   *
   * @param key     缓存的键值，不能为 {@code null}.
   * @param value   缓存的键值，不能为 {@code null}.
   * @param handler 回调函数.
   */
  void set(String key, JsonObject value, Handler<AsyncResult<Void>> handler);

  /**
   * 将键值对放入缓存.
   *
   * @param key     缓存的键值，不能为 {@code null}.
   * @param value   缓存的键值，不能为 {@code null}.
   * @param expires 缓存的失效
   * @param handler 回调函数.
   */
  void setex(String key, JsonObject value, int expires, Handler<AsyncResult<Void>> handler);

  /**
   * 根据键删除缓存值.
   *
   * @param key     缓存的键值，不能为 {@code null}.
   * @param handler 回调函数.
   */
  void delete(String key, Handler<AsyncResult<Void>> handler);

  static RedisProvider create(Vertx vertx, JsonObject config) {
    return factory.create(vertx, config);
  }

}
