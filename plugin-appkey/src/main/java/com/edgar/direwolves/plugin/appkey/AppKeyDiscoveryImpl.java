package com.edgar.direwolves.plugin.appkey;

import com.edgar.direwolves.core.utils.Log;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Edgar on 2017/7/20.
 *
 * @author Edgar  Date 2017/7/20
 */
class AppKeyDiscoveryImpl implements AppKeyDsicovery {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppKeyDsicovery.class);

  private final AppKeyBackend backend;

  private final String name;

  private final Vertx vertx;

  private final Set<AppKeyImporter> importers = new CopyOnWriteArraySet<>();

  public AppKeyDiscoveryImpl(Vertx vertx, String name) {
    this.vertx = vertx;
    this.name = name;
    this.backend = new DefaultAppKeyBackend(vertx, name);
    Log.create(LOGGER)
            .setEvent("appkey.discovery.start")
            .addData("namespace", this.name)
            .info();
  }

  @Override
  public void publish(AppKey appKey, Handler<AsyncResult<AppKey>> resultHandler) {
    Log.create(LOGGER)
            .setEvent("appkey.publish")
            .addData("namespace", this.name)
            .addData("appKey", appKey)
            .info();
    backend.store(appKey, ar -> {
//      if (ar.succeeded()) {
//        vertx.eventBus().publish(publishedAddress, definition.toJson());
//      }
      resultHandler.handle(ar);
    });
  }

  @Override
  public void unpublish(String appKey, Handler<AsyncResult<Void>> resultHandler) {
    Log.create(LOGGER)
            .setEvent("appkey.unpublish")
            .addData("namespace", this.name)
            .addData("appKey", appKey)
            .info();
    backend.remove(name, ar -> {
      if (ar.failed()) {
        resultHandler.handle(Future.failedFuture(ar.cause()));
        return;
      }
//      ApiDefinition definition = ar.result();
//      if (definition != null) {
//        vertx.eventBus().publish(unpublishedAddress, definition.toJson());
//      }
      resultHandler.handle(Future.succeededFuture());
    });
  }

  @Override
  public AppKeyDsicovery registerImporter(AppKeyImporter importer, JsonObject config,
                                          Handler<AsyncResult<Void>> completionHandler) {
    JsonObject conf;
    if (config == null) {
      conf = new JsonObject();
    } else {
      conf = config;
    }

    Future<Void> completed = Future.future();
    completed.setHandler(
            ar -> {
              if (ar.failed()) {
                Log.create(LOGGER)
                        .setEvent("appkey.importer.started")
                        .addData("namespace", this.name)
                        .addData("importer", importer)
                        .setMessage("Cannot start the appkey importer")
                        .setThrowable(ar.cause())
                        .error();
                if (completionHandler != null) {
                  completionHandler.handle(Future.failedFuture(ar.cause()));
                }
              } else {
                importers.add(importer);
                Log.create(LOGGER)
                        .setEvent("appkey.importer.started")
                        .addData("namespace", this.name)
                        .addData("importer", importer)
                        .setMessage("AppKey importer started")
                        .info();
                if (completionHandler != null) {
                  completionHandler.handle(Future.succeededFuture(null));
                }
              }
            }
    );

    importer.start(vertx, this, conf, completed);
    return this;
  }

  @Override
  public void close() {
    Log.create(LOGGER)
            .setEvent("appkey.discovery.close")
            .addData("namespace", this.name)
            .info();
  }
}
