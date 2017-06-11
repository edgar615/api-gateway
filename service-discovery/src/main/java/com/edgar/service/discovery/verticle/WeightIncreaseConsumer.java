package com.edgar.service.discovery.verticle;

import com.edgar.service.discovery.MoreServiceDiscovery;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Created by Edgar on 2017/6/9.
 *
 * @author Edgar  Date 2017/6/9
 */
class WeightIncreaseConsumer extends AbstractInstanceConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MoreServiceDiscovery.class);

  private final Vertx vertx;

  private final int weightIncrease;

  WeightIncreaseConsumer(Vertx vertx, ServiceDiscovery discovery, int weightIncrease) {
    super(discovery);
    this.vertx = vertx;
    final String address = UUID.randomUUID().toString();
    this.weightIncrease = weightIncrease;
    vertx.eventBus().<JsonObject>localConsumer(address, msg -> {
      JsonObject jsonObject = msg.body();
      handle(jsonObject);
    });
    vertx.eventBus().<JsonObject>consumer("service.discovery.weight.increase", msg -> {
      JsonObject jsonObject = msg.body();
      vertx.eventBus().send(address, jsonObject);
    });
  }

  private void handle(JsonObject jsonObject) {
    String id = jsonObject.getString("id");
    vertx.executeBlocking(f -> incWeight(id, ar -> {
      if (ar.succeeded()) {
        LOGGER.info("[{}] [incWeight]", id);
        f.complete();
      } else {
        LOGGER.error("[{}] [incWeight]", id, ar.cause());
        f.fail(ar.cause());
      }
    }), true, ar -> {
    });
  }

  private void incWeight(String id, Handler<AsyncResult<Void>> handler) {
    discovery.getRecord(r -> r.getRegistration().equalsIgnoreCase(id), ar -> {
      if (ar.failed()) {
        handler.handle(Future.failedFuture(ar.cause()));
        return;
      }
      Record record = ar.result();
      int weight = record.getMetadata().getInteger("weight", 60);
      record.getMetadata().put("weight", weight + weightIncrease);
      update(record, handler);
    });
  }
}
