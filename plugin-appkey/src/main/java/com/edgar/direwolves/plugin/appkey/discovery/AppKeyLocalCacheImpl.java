package com.edgar.direwolves.plugin.appkey.discovery;

import com.google.common.base.Charsets;
import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Lock;

import java.util.concurrent.TimeUnit;

/**
 * Created by edgar on 17-7-25.
 */
class AppKeyLocalCacheImpl implements AppKeyLocalCache {

  private final Cache<String, JsonObject> cache;

  private final AppKeyDiscovery discovery;

  private final Vertx vertx;

  private final int bucket = 64;

  public AppKeyLocalCacheImpl(Vertx vertx, AppKeyDiscovery discovery) {
    this.vertx = vertx;
    this.cache = CacheBuilder.newBuilder()
            .expireAfterWrite(30L, TimeUnit.MINUTES)
            .maximumSize(5000L)
            .ticker(Ticker.systemTicker())
            .build();
    this.discovery = discovery;
  }

  @Override
  public void getAppKey(String appKey, Handler<AsyncResult<AppKey>> resultHandler) {
    JsonObject jsonObject = cache.getIfPresent(appKey);
    if (jsonObject != null) {
      resultHandler.handle(Future.succeededFuture(new AppKey(appKey, jsonObject)));
      return;
    }
    //因为sharedData内部保存lock的map无法删除lock，如果key比较多，对内存的占用会一直得不到释放。因此这里使用hash来限制lock的数量.
    HashCode hashCode = Hashing.md5().newHasher().putString(appKey, Charsets.UTF_8).hash();
    int hash = Hashing.consistentHash(hashCode, bucket);
    vertx.sharedData().getLock("lock.appkey." + hash, lockAsyncResult -> {
      if (lockAsyncResult.succeeded()) {
        Lock lock = lockAsyncResult.result();
        discovery.getAppKey(appKey, ar -> {
          if (ar.result() != null) {
            cache.put(ar.result().getAppkey(), ar.result().getJsonObject());
            lock.release();
          }
          if (ar.failed()) {
            resultHandler.handle(Future.failedFuture(ar.cause()));
            return;
          }
          if (ar.result() != null) {
            cache.put(ar.result().getAppkey(), ar.result().getJsonObject());
            lock.release();
            resultHandler.handle(Future.succeededFuture(
                    new AppKey(ar.result().getAppkey(), ar.result().getJsonObject())));
            return;
          }
          if (ar.result() == null) {
            resultHandler.handle(Future.failedFuture(
                    SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND)
                            .set("details", "Undefined AppKey:" + appKey)));
            return;
          }
        });
      }
    });


  }

  @Override
  public int size() {
    return 0;
  }
}
