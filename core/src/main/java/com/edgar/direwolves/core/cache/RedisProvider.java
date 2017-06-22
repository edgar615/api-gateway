package com.edgar.direwolves.core.cache;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.ServiceHelper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

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

  /**
   * 载入lua脚本.
   * @param script 脚本
   * @param handler 回调函数
   */
  void scriptLoad(String script, Handler<AsyncResult<String>> handler);

  /**
   * 运行lua脚本.
   * @param sha1 脚本的sha值
   * @param keys 键值列表
   * @param args 参数列表
   * @param handler 回调函数
   */
  void evalsha(String sha1, List<String> keys, List<String> args,
               Handler<AsyncResult<JsonArray>> handler);

  /**
   * 运行lua脚本.
   * @param script 脚本
   * @param keys 键值列表
   * @param args 参数列表
   * @param handler 回调函数
   */
  void eval(String script, List<String> keys, List<String> args,
            Handler<AsyncResult<JsonArray>> handler);

  /**
   * 请求一个令牌，限流的实现
   * @param rules 限流规则的集合
   * @param handler 回调函数
   */
  void acquireToken(JsonArray rules, Handler<AsyncResult<JsonArray>> handler);

  static RedisProvider create(Vertx vertx, JsonObject config) {
    return factory.create(vertx, config);
  }

}
