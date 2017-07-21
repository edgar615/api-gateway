package com.edgar.direwolves.plugin.appkey.discovery;

import com.edgar.direwolves.core.apidiscovery.ApiDefinitionBackend;
import com.edgar.direwolves.plugin.appkey.discovery.AppKey;
import com.edgar.direwolves.plugin.appkey.discovery.AppKeyBackend;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.sharedata.SyncMap;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Edgar on 2017/6/20.
 *
 * @author Edgar  Date 2017/6/20
 */
class DefaultAppKeyBackend implements AppKeyBackend {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiDefinitionBackend.class);

  private final Vertx vertx;

  private final SyncMap<String, String> registry;

  private final String name;

  public DefaultAppKeyBackend(Vertx vertx, String name) {
    this.vertx = vertx;
    this.registry = new SyncMap<>(vertx, name);
    this.name = name;
  }

  @Override
  public void store(AppKey appKey, Handler<AsyncResult<AppKey>> resultHandler) {
    Objects.requireNonNull(appKey, "appKey is null");
    registry.put(appKey.getAppkey(), appKey.getJsonObject().encode(), ar -> {
      if (ar.succeeded()) {
        resultHandler.handle(Future.succeededFuture(appKey));
      } else {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void remove(String appKey, Handler<AsyncResult<AppKey>> resultHandler) {
    Objects.requireNonNull(appKey, "appKey required");
    registry.remove(appKey, ar -> {
      if (ar.succeeded()) {
        if (ar.result() == null) {
          resultHandler.handle(Future.failedFuture("AppKey: '" + appKey + "' not found"));
        } else {
          resultHandler
                  .handle(Future.succeededFuture(new AppKey(appKey, new JsonObject(ar.result()))));
        }
      } else {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void getAppKeys(Handler<AsyncResult<List<AppKey>>> resultHandler) {
    registry.getAll(ar -> {
      if (ar.succeeded()) {
        List<AppKey> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : ar.result().entrySet()) {
          list.add(new AppKey(entry.getKey(), new JsonObject(entry.getValue())));
        }
        resultHandler.handle(Future.succeededFuture(list));
      } else {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void getAppKey(String appKey, Handler<AsyncResult<AppKey>> resultHandler) {
    registry.get(appKey, ar -> {
      if (ar.succeeded()) {
        if (ar.result() != null) {
          resultHandler.handle(Future.succeededFuture(
                  new AppKey(appKey, new JsonObject(ar.result()))));
        } else {
          SystemException ex = SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND)
                  .set("appKey", appKey);
          resultHandler.handle(Future.failedFuture(ex));
        }
      } else {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }
}
