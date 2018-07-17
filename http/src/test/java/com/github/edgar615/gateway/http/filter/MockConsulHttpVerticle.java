package com.github.edgar615.gateway.http.filter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟consul的http服务.
 *
 * @author Edgar  Date 2016/10/12
 */
public class MockConsulHttpVerticle extends AbstractVerticle {
  private List<JsonObject> services = new ArrayList<>();

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    vertx.createHttpServer()
            .requestHandler(request -> {
              if (request.path().equals("/v1/catalog/services")) {
                JsonObject result = new JsonObject();
                services.forEach(object ->
                                         result.put(object.getString("ServiceName"),
                                                    object.getJsonArray("ServiceTags", object.getJsonArray("ServiceTags"))));
                request.response().end(result.encodePrettily());
              } else if (request.path().startsWith("/v1/catalog/service/")) {
                String service = request.path().substring("/v1/catalog/service/".length());
                JsonArray value = find(service);
                if (value != null) {
                  request.response().end(value.encodePrettily());
                } else {
                  request.response().setStatusCode(404).end();
                }
              } else {
                request.response().setStatusCode(404).end();
              }
            })
            .listen(config().getInteger("consul.port", 5601), ar -> {
              if (ar.succeeded()) {
                startFuture.complete();
              } else {
                startFuture.fail(ar.cause());
              }
            });
  }

  /**
   * new JsonObject()
   * .put("Node", "u221")
   * .put("Address", "10.4.7.221")
   * .put("ServiceID", "u221:device:8080")
   * .put("ServiceName", "device")
   * .put("ServiceTags", new JsonArray())
   * .put("ServicePort", 32769)
   *
   * @param service
   */
  public void addService(JsonObject service) {
    services.add(service);
  }

  public void clear() {
    services.clear();
  }

  private JsonArray find(String service) {
    JsonArray array = new JsonArray();
    services.stream().filter(json -> json.getString("ServiceName").equalsIgnoreCase(service))
            .forEach(array::add);
    if (!array.isEmpty()) {
      return array;
    }
    return null;
  }
}
