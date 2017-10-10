package com.github.edgar615.direwolves.handler;

import com.github.edgar615.direwolves.dispatch.BaseHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2016/11/1.
 *
 * @author Edgar  Date 2016/11/1
 */
public class DeviceHttpVerticle extends AbstractVerticle {

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.route().handler(BaseHandler.create());
    router.get("/devices/timeout").handler(rc -> {
      try {
        TimeUnit.SECONDS.sleep(2);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      JsonArray devices = new JsonArray();
      devices.add(new JsonObject().put("id", 1))
              .add(new JsonObject().put("id", 2));
      rc.response()
              .end(devices.encode());
    });
    router.get("/devices/error").handler(rc -> {
      JsonObject device = new JsonObject().put("code", 999)
              .put("message", "undefined");
      rc.response().setStatusCode(400)
              .end(device.encode());
    });
    router.get("/devices").handler(rc -> {
      JsonArray devices = new JsonArray();
      devices.add(new JsonObject().put("id", 1))
              .add(new JsonObject().put("id", 2));
      rc.response()
              .end(devices.encode());
    });

    router.get("/devices/:id").handler(rc -> {
      String id = rc.request().getParam("id");
      rc.response()
              .end(new JsonObject().put("id", id)
                           .put("query", rc.request().query()).encode());
    });

    router.delete("/devices/:id").handler(rc -> {
      String id = rc.request().getParam("id");
      rc.response()
              .end(new JsonObject().put("id", id).encode());
    });

    router.post("/devices").handler(rc -> {
      rc.response()
              .end(new JsonObject().put("result", 1)
                           .put("body", rc.getBodyAsJson()).encode());
    });

    router.put("/devices/:id").handler(rc -> {
      String id = rc.request().getParam("id");
      rc.response()
              .end(new JsonObject().put("id", id)
                           .put("result", 1)
                           .put("body", rc.getBodyAsJson()).encode());
    });

    router.route().handler(rc -> {
      rc.response().end(new JsonObject().put("code", 404).encode());
    });

    vertx.createHttpServer().requestHandler(router::accept)
            .listen(config().getInteger("http.port", 8080), ar
                    -> {
              if (ar.succeeded()) {
                startFuture.complete();
              } else {
                startFuture.fail(ar.cause());
              }
            });
  }

}
