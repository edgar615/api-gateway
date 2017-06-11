package com.edgar.service.discovery.verticle;

import com.edgar.service.discovery.MoreServiceDiscovery;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Created by Edgar on 2017/6/9.
 *
 * @author Edgar  Date 2017/6/9
 */
class StateCloseConsumer extends AbstractInstanceConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MoreServiceDiscovery.class);

  private final Vertx vertx;

  StateCloseConsumer(Vertx vertx, ServiceDiscovery discovery) {
    super(discovery);
    this.vertx = vertx;
    final String address = UUID.randomUUID().toString();
    vertx.eventBus().<JsonObject>localConsumer(address, msg -> {
      JsonObject jsonObject = msg.body();
      handle(jsonObject);
    });

    vertx.eventBus().<JsonObject>consumer("service.discovery.close", msg -> {
      JsonObject jsonObject = msg.body();
      vertx.eventBus().send(address, jsonObject);
    });
  }

  private void handle(JsonObject jsonObject) {
    String id = jsonObject.getString("id");
    vertx.executeBlocking(f -> changeState(id, "CLOSE", ar -> {
      if (ar.succeeded()) {
        LOGGER.info("[{}] [CLOSE]", id);
        f.complete();
      } else {
        LOGGER.error("[{}] [CLOSE]", id, ar.cause());
        f.fail(ar.cause());
      }
    }), true, ar -> {
    });
  }

}
