package com.edgar.direwolves.plugin.appkey;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Edgar on 2016/8/25.
 *
 * @author Edgar  Date 2016/8/25
 */
public class AppKeyImporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppKeyImporter.class);

  private final Vertx vertx;

  private final String path;

  private final int scanPeriod;

  private final int port;

  private final HttpClient httpClient;

  private final AppKeyPublisher publisher;

  private final Map<String, JsonObject> imports = new ConcurrentHashMap<>();


  public AppKeyImporter(Vertx vertx, AppKeyPublisher publisher, JsonObject configuration) {
    this.vertx = vertx;
    this.path = configuration.getString("url", "/appkey/import");
    this.port = configuration.getInteger("http.port", 9000);
    this.scanPeriod = configuration.getInteger("scan-period", 60000);
    this.httpClient = vertx.createHttpClient();
    this.publisher = publisher;
    start();
  }

  private synchronized void retrieveAppKey(JsonArray array, Future<JsonArray> completed) {
    //注册
    List<Future> futures = new ArrayList<>();
    for (int i = 0; i < array.size(); i++) {
      JsonObject app = array.getJsonObject(i).copy();
      Future<Void> future = Future.future();
      future.setHandler(ar -> {
        if (ar.succeeded()) {
          LOGGER.debug("AppKey imported succeed, appKey->{}", app.encode());
        } else {
          LOGGER.debug("AppKey imported failed, appKey->{}", app.encode());
        }
      });
      publisher.publish(app, future.completer());
      futures.add(future);
    }

    CompositeFuture.any(futures)
            .setHandler(ar -> {
              if (ar.failed()) {
                LOGGER.warn("AppKey imported failed", ar.cause());
                completed.fail(ar.cause());
              } else {
                List<String> retrievedIds = new ArrayList<>();
                for (int i = 0; i < array.size(); i++) {
                  JsonObject app = array.getJsonObject(i);
                  String appKey = app.getString("appKey");
                  retrievedIds.add(appKey);
                }
                synchronized (AppKeyImporter.class) {
                  for (String key : imports.keySet()) {
                    if (!retrievedIds.contains(key)) {
                      publisher.unpublish(key, ar2 -> {
                        if (ar2.succeeded()) {
                          LOGGER.debug("AppKey unpublish succeed", ar2.result());
                        } else {
                          LOGGER.warn("AppKey unpublish failed", ar2.cause());
                        }
                      });
                    }
                  }
                  imports.clear();
                  for (int i = 0; i < array.size(); i++) {
                    JsonObject app = array.getJsonObject(i);
                    String appKey = app.getString("appKey");
                    imports.put(appKey, app.copy());
                  }
                }
                completed.complete();
              }
            });
  }

  public void start() {
    vertx.setPeriodic(scanPeriod, l -> {
      //服务查找
      httpClient.get(port, "127.0.0.1", path, response -> {
        response.bodyHandler(body -> {
          JsonArray array = body.toJsonArray().copy();
          Future<JsonArray> future = Future.future();
          retrieveAppKey(array, future);
          future.setHandler(ar -> {
            if (ar.succeeded()) {
              LOGGER.debug("AppKey import succeed");
            } else {
              LOGGER.warn("AppKey import failed", ar.cause());
            }
          });
        });
      }).putHeader("content-type", "application/json").end();

    });
  }
}
