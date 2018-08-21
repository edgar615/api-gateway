package com.github.edgar615.gateway.redis;

import com.github.edgar615.util.vertx.cache.Cache;
import com.github.edgar615.util.vertx.cache.CacheEvictor;
import com.github.edgar615.util.vertx.cache.CacheLoader;
import com.github.edgar615.util.vertx.cache.CacheOptions;
import com.github.edgar615.util.vertx.cache.CacheWriter;
import com.github.edgar615.util.vertx.redis.RedisClientHelper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;

/**
 * Created by Administrator on 2017/10/29.
 */
public class RedisCache implements Cache<String, JsonObject> {
    private final RedisClient redisClient;

    /**
     * 过期时间
     */
    private long expires;

    private String name;

    RedisCache(RedisClient redisClient, String name, CacheOptions options) {
        this.redisClient = redisClient;
        this.name = name;
        this.expires = options.getExpireAfterWrite();
    }

    public static RedisCache create(Vertx vertx, String cacheName,
                                    CacheOptions options) {
        RedisClient redisClient = RedisClientHelper.getShared(vertx);
        return new RedisCache(redisClient, cacheName, options);
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public void get(String key, Handler<AsyncResult<JsonObject>> handler) {
        redisClient.hgetall(key, ar -> {
            if (ar.failed()) {
                handler.handle(Future.failedFuture(ar.cause()));
                return;
            }
            if (ar.result() == null
                || ar.result().isEmpty()) {
                handler.handle(Future.succeededFuture(null));
                return;
            }
            handler.handle(Future.succeededFuture(ar.result()));
        });
    }

    @Override
    public void get(String key, CacheLoader<String, JsonObject> cacheLoader,
                    Handler<AsyncResult<JsonObject>> handler) {
        redisClient.hgetall(key, ar -> {
            if (ar.failed()) {
                handler.handle(Future.failedFuture(ar.cause()));
                return;
            }
            if (ar.result() != null
                && !ar.result().isEmpty()) {
                handler.handle(Future.succeededFuture(ar.result()));
                return;
            }
            load(key, cacheLoader, handler);
        });
    }

    @Override
    public void put(String key, JsonObject value, Handler<AsyncResult<Void>> handler) {
        Future<String> future = Future.future();
        redisClient.hmset(key, value, future.completer());

        future.compose(s -> {
            Future<Long> exFuture = Future.future();
            redisClient.expire(key, expires, exFuture.completer());
            return exFuture;
        }).setHandler(ar -> {
            if (ar.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void evict(String key, Handler<AsyncResult<JsonObject>> handler) {
        redisClient.del(key, ar -> {
            if (ar.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void put(String key, JsonObject value, CacheWriter<String, JsonObject> cacheWriter,
                    Handler<AsyncResult<Void>> resultHandler) {
        cacheWriter.write(key, value, ar -> {
            if (ar.succeeded()) {
                //如果缓存更新失败，会有数据不一致问题
                put(key, value, resultHandler);
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void evict(String key, CacheEvictor<String> cacheEvictor,
                      Handler<AsyncResult<JsonObject>> handler) {
        cacheEvictor.delete(key, ar -> {
            if (ar.succeeded()) {
                //如果缓存删除失败，会有数据不一致问题
                evict(key, handler);
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    private void load(String key, CacheLoader<String, JsonObject> cacheLoader,
                      Handler<AsyncResult<JsonObject>> handler) {
        Future<JsonObject> loaderFuture = Future.future();
        cacheLoader.load(key, loaderFuture.completer());
        loaderFuture.compose(s -> {
            Future<JsonObject> exFuture = Future.future();
            if (s != null && !s.isEmpty()) {
                exFuture.complete(s);
            } else {
                exFuture.complete(null);
            }
            return exFuture;
        }).compose(s -> {
            Future<JsonObject> exFuture = Future.future();
            if (s != null) {
                put(key, s, ar -> {
                    if (ar.succeeded()) {
                        exFuture.complete(s);
                    } else {
                        exFuture.fail(ar.cause());
                    }
                });
            } else {
                exFuture.complete(null);
            }
            return exFuture;
        }).setHandler(ar -> {
            if (ar.succeeded()) {
                //放入缓存
                handler.handle(Future.succeededFuture(ar.result()));
            } else {
                handler.handle(Future.succeededFuture(null));
            }
        });
    }
}
