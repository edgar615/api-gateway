package com.github.edgar615.direwolves.mock;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/11/1.
 *
 * @author Edgar  Date 2016/11/1
 */
public class DeviceHttpVerticle extends AbstractVerticle {
  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(DeviceHttpVerticle.class.getName());
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
//    DeviceStore store = new DeviceStore();
//    initData(store);

    vertx.createHttpServer().requestHandler(req -> {
      req.response().putHeader("Content-Type", "application/json");
      String url = req.path();
      if (url.equals("/devices") && req.method() == HttpMethod.GET) {
        String start = req.getParam("start");
        String limit = req.getParam("limit");
        JsonArray devices = new JsonArray();
        devices.add(new JsonObject().put("id", start))
                .add(new JsonObject().put("id", limit));
        req.response()
                .end(devices.encode());
      }
      if (url.startsWith("/devices/") && req.method() == HttpMethod.GET) {
        int id = Integer.parseInt(url.substring("/devices/".length()));
        req.response()
                .end(new JsonObject().put("id", id).encode());
      }
      if (url.equals("/devices") && req.method() == HttpMethod.DELETE) {
        req.response()
                .end(new JsonObject()
                             .put("result", "1")
                             .encode());
      }
      if (url.equals("/devices") && req.method() == HttpMethod.POST) {
        req.bodyHandler(body -> req.response().end(body));
      }
      if (url.equals("/devices") && req.method() == HttpMethod.PUT) {
        req.bodyHandler(body -> req.response().end(body));
      }
    }).listen(config().getInteger("http.port", 8081), ar -> {
      if (ar.succeeded()) {
        startFuture.complete();
      } else {
        startFuture.fail(ar.cause());
      }
    });
  }

  private void initData(DeviceStore store) {
    for (int i = 0; i < 100; i++) {
      store.add(new Device(i, "device-" + i));
    }
  }

}
