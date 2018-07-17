package com.github.edgar615.gateway.plugin.appkey;

import com.github.edgar615.gateway.core.utils.CacheUtils;
import com.github.edgar615.gateway.core.utils.Consts;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.vertx.cache.Cache;
import com.github.edgar615.util.vertx.cache.CacheLoader;
import com.github.edgar615.util.vertx.cache.CacheOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Edgar on 2018/2/6.
 *
 * @author Edgar  Date 2018/2/6
 */
class AppKeyFinder {
  private static final long CACHE_EXPIRE = 1800L;

  private static final String NON_EXISTENT = UUID.randomUUID().toString();

  private static final String KEY_PREFIX = "appKey:";

  private final Map<String, JsonObject> localAppKeys = new HashMap<>();

  private final AppKeyLoader appKeyLoader;

  private final Cache<String, JsonObject> cache;

  AppKeyFinder(Vertx vertx, JsonObject config) {
    JsonArray originData = config.getJsonArray("data", new JsonArray());
    for (int i = 0; i < originData.size(); i++) {
      JsonObject jsonObject = originData.getJsonObject(i);
      String appKey = jsonObject.getString("appKey");
      String appSecret = jsonObject.getString("appSecret");
      if (appKey != null && appSecret != null) {
        localAppKeys.put(appKey, jsonObject);
      }
    }
    int port = config.getInteger("port", Consts.DEFAULT_PORT);
    if (config.getValue("api") instanceof String) {
      String path = config.getString("api", "/");
      this.appKeyLoader = new AppKeyLoader(vertx, port, path);
    } else {
      appKeyLoader = null;
    }

    boolean cacheEnable = config.getBoolean("cacheEnable", false);
    if (cacheEnable) {
      long expireAfterWrite = config.getLong("expireAfterWrite", CACHE_EXPIRE);
      CacheOptions options = new CacheOptions()
              .setExpireAfterWrite(expireAfterWrite);
      this.cache = CacheUtils.createCache(vertx, "appKey", options);
    } else {
      this.cache = null;
    }
  }

  void find(String key, Handler<AsyncResult<JsonObject>> resultHandler) {
    if (localAppKeys.containsKey(key)) {
      resultHandler.handle(Future.succeededFuture(localAppKeys.get(key)));
      return;
    }
    if (cache == null && appKeyLoader == null) {
      nonExistentAppKey(key, resultHandler);
      return;
    }
    if (cache == null && appKeyLoader != null) {
      appKeyLoader.load(key, resultHandler);
      return;
    }
    if (cache != null && appKeyLoader == null) {
      cache.get(cacheKey(key), ar -> {
        if (ar.failed() || ar.result() == null || ar.result().isEmpty()) {
          nonExistentAppKey(key, resultHandler);
          return;
        }
        resultHandler.handle(Future.succeededFuture(ar.result()));
      });
      return;
    }
    if (cache != null && appKeyLoader != null) {
      cache.get(cacheKey(key), new CacheSecondaryLoader(), ar -> {
        if (ar.failed() || ar.result() == null || ar.result().isEmpty()) {
          nonExistentAppKey(key, resultHandler);
          return;
        }
        if (ar.result().containsKey(NON_EXISTENT)) {
          nonExistentAppKey(key, resultHandler);
          return;
        }
        resultHandler.handle(Future.succeededFuture(ar.result()));
      });
    }
  }

  private void nonExistentAppKey(String key, Handler<AsyncResult<JsonObject>> resultHandler) {SystemException

          e = SystemException.create(DefaultErrorCode.INVALID_REQ)
          .set("details", "Undefined AppKey:" + key);
    resultHandler.handle(Future.failedFuture(e));
  }

  private String cacheKey(String key) {
    return KEY_PREFIX + key;
  }

  private String appKey(String cacheKey) {
    return cacheKey.substring(KEY_PREFIX.length());
  }

  private class CacheSecondaryLoader implements CacheLoader<String, JsonObject> {

    @Override
    public void load(String key, Handler<AsyncResult<JsonObject>> handler) {
      JsonObject nonExistent = new JsonObject().put(NON_EXISTENT, NON_EXISTENT);
      appKeyLoader.load(appKey(key), ar -> {
          if (ar.failed() || ar.result() == null || ar.result().isEmpty()) {
            handler.handle(Future.succeededFuture(nonExistent));
            return;
          }
        handler.handle(Future.succeededFuture(ar.result()));
      });
    }
  }
}
