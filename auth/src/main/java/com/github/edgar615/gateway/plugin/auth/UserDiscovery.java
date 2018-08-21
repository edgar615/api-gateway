package com.github.edgar615.gateway.plugin.auth;

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
import io.vertx.core.json.JsonObject;

import java.util.UUID;

/**
 * Created by Edgar on 2018/2/6.
 *
 * @author Edgar  Date 2018/2/6
 */
class UserDiscovery {
    private static final long CACHE_EXPIRE = 1800L;

    private static final String NON_EXISTENT = UUID.randomUUID().toString();

    private static final String KEY_PREFIX = "user:";

    private final UserLoader userLoader;

    private final Cache<String, JsonObject> cache;

    UserDiscovery(Vertx vertx, JsonObject config) {
        int port = config.getInteger("port", Consts.DEFAULT_PORT);
        if (config.getValue("api") instanceof String) {
            String path = config.getString("api", "/principal");
            this.userLoader = new UserLoader(vertx, port, path);
        } else {
            userLoader = null;
        }

        boolean cacheEnable = config.getBoolean("cacheEnable", false);
        if (cacheEnable) {
            long expireAfterWrite = config.getLong("expireAfterWrite", CACHE_EXPIRE);
            CacheOptions options = new CacheOptions()
                    .setExpireAfterWrite(expireAfterWrite);
            this.cache = CacheUtils.createCache(vertx, "user", options);
        } else {
            this.cache = null;
        }
    }

    void find(String key, Handler<AsyncResult<JsonObject>> resultHandler) {
        if (cache == null && userLoader == null) {
            nonExistentAppKey(key, resultHandler);
            return;
        }
        if (cache == null && userLoader != null) {
            userLoader.load(key, resultHandler);
            return;
        }
        if (cache != null && userLoader == null) {
            cache.get(cacheKey(key), ar -> {
                if (ar.failed() || ar.result() == null || ar.result().isEmpty()) {
                    nonExistentAppKey(key, resultHandler);
                    return;
                }
                resultHandler.handle(Future.succeededFuture(ar.result()));
            });
            return;
        }
        if (cache != null && userLoader != null) {
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

    private String appKey(String cacheKey) {
        return cacheKey.substring(KEY_PREFIX.length());
    }

    private void nonExistentAppKey(String key, Handler<AsyncResult<JsonObject>> resultHandler) {
        SystemException e = SystemException.create(DefaultErrorCode.INVALID_REQ)
                .set("details", "Non-existent User:" + key);
        resultHandler.handle(Future.failedFuture(e));
    }

    private String cacheKey(String key) {
        return KEY_PREFIX + key;
    }

    private class CacheSecondaryLoader implements CacheLoader<String, JsonObject> {

        @Override
        public void load(String key, Handler<AsyncResult<JsonObject>> handler) {
            JsonObject nonExistent = new JsonObject().put(NON_EXISTENT, NON_EXISTENT);
            userLoader.load(appKey(key), ar -> {
                if (ar.failed() || ar.result() == null || ar.result().isEmpty()) {
                    handler.handle(Future.succeededFuture(nonExistent));
                    return;
                }
                handler.handle(Future.succeededFuture(ar.result()));
            });
        }
    }
}
