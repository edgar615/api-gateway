package com.github.edgar615.direwolves.plugin.appkey.discovery;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Edgar on 2017/7/21.
 *
 * @author Edgar  Date 2017/7/21
 */
@Deprecated
public class JsonAppKeyImpoter implements AppKeyImporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonAppKeyImpoter.class);

  @Override
  public void start(Vertx vertx, AppKeyPublisher publisher, JsonObject config,
                    Future<Void> completed) {
    JsonArray appKeys = config.getJsonArray("data", new JsonArray());
    List<Future> futures = new ArrayList<>();
    for (int i = 0; i < appKeys.size(); i++) {
      JsonObject jsonObject = appKeys.getJsonObject(i);
      String appKey = jsonObject.getString("appKey");
      if (appKey != null) {
        Future<AppKey> future = Future.future();
        publisher.publish(new AppKey(appKey, jsonObject), future.completer());
        futures.add(future);
      }
    }
    CompositeFuture.any(futures)
            .setHandler(ar -> {
              if (ar.failed()) {
                LOGGER.warn("AppKey imported failed", ar.cause());
                completed.fail(ar.cause());
              } else {
                LOGGER.warn("AppKey imported succeed", ar.cause());
                completed.complete();
              }
            });
  }
}
