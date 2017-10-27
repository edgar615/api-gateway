package com.github.edgar615.direwolves.plugin.appkey.discovery;

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
@Deprecated
public class HttpAppKeyImporter implements AppKeyImporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpAppKeyImporter.class);

  private final Map<String, JsonObject> imports = new ConcurrentHashMap<>();

  private Vertx vertx;

  private String path;

  private int scanPeriod;

  private int port;

  private String host;

  private HttpClient httpClient;

  private AppKeyPublisher publisher;

  private synchronized void retrieveAppKey(JsonArray array, Future<JsonArray> completed) {
    //注册
    List<Future> futures = new ArrayList<>();
    for (int i = 0; i < array.size(); i++) {
      JsonObject app = array.getJsonObject(i).copy();
      Future<AppKey> future = Future.future();
      AppKey appKey = new AppKey(app.getString("appKey", "unkown"), app);
      publisher.publish(appKey, future.completer());
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
                synchronized (HttpAppKeyImporter.class) {
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

  private void startHttpImporter() {
    vertx.setPeriodic(scanPeriod, l -> {
      //服务查找
      httpClient.get(port, host, path, response -> {
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

  public void start(Vertx vertx, AppKeyPublisher publisher, JsonObject config,
                    Future<Void> future) {
    this.vertx = vertx;
    this.path = config.getString("url", "/appkey/import");
    this.host = config.getString("host", "127.0.0.1");
    this.port = config.getInteger("port", 9000);
    this.scanPeriod = config.getInteger("scan-period", 60000);
    this.httpClient = vertx.createHttpClient();
    this.publisher = publisher;
    startHttpImporter();
  }

}
