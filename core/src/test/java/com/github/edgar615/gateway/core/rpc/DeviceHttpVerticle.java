package com.github.edgar615.gateway.core.rpc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by Edgar on 2016/11/1.
 *
 * @author Edgar  Date 2016/11/1
 */
public class DeviceHttpVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        vertx.createHttpServer().requestHandler(req -> {
            String url = req.path();
            if (url.equals("/devices") && req.method() == HttpMethod.GET) {
                JsonArray devices = new JsonArray();
                devices.add(new JsonObject().put("id", 1))
                        .add(new JsonObject().put("id", 2));
                req.response().putHeader("Content-Type", "application/json")
                        .end(devices.encode());
            }
            if (url.startsWith("/devices/") && req.method() == HttpMethod.GET) {
                String id = url.substring("/devices/".length());
                req.response().putHeader("Content-Type", "application/json")
                        .end(new JsonObject().put("id", id)
                                     .put("query", req.query()).encode());
            }
            if (url.equals("/devices") && req.method() == HttpMethod.DELETE) {
                req.response().putHeader("Content-Type", "application/json")
                        .end(new JsonObject()
                                     .put("result", "1")
                                     .encode());
            }
            if (url.equals("/devices") && req.method() == HttpMethod.POST) {
                req.response().putHeader("Content-Type", "application/json");
                req.bodyHandler(body -> req.response().end(body));
            }
            if (url.equals("/devices") && req.method() == HttpMethod.PUT) {
                req.response().putHeader("Content-Type", "application/json");
                req.bodyHandler(body -> req.response().end(body));
            }
        }).listen(config().getInteger("port", 8080), ar -> {
            if (ar.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(ar.cause());
            }
        });
    }

}
