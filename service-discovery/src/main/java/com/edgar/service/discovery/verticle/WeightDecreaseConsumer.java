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
class WeightDecreaseConsumer extends AbstractInstanceConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MoreServiceDiscovery.class);

  private final Vertx vertx;

  private final int weightDecrease;

  WeightDecreaseConsumer(Vertx vertx, ServiceDiscovery discovery, int weightDecrease) {
    super(discovery);
    this.vertx = vertx;
    final String address = UUID.randomUUID().toString();
    this.weightDecrease = weightDecrease;
    vertx.eventBus().<JsonObject>localConsumer(address, msg -> {
      JsonObject jsonObject = msg.body();
      handle(jsonObject, ar -> {
        if (ar.failed()) {
          msg.reply(new JsonObject().put("error", ar.cause().getMessage()) );
          return;
        }
        msg.reply(new JsonObject().put("result", 1) );
      });
    });
    vertx.eventBus().<JsonObject>consumer("service.discovery.weight.decrease", msg -> {
      JsonObject jsonObject = msg.body();
      vertx.eventBus().<JsonObject>send(address, jsonObject, ar -> {
        if (ar.failed()) {
          msg.reply(new JsonObject().put("error", ar.cause().getMessage()) );
          return;
        }
        msg.reply(ar.result().body() );
      });
    });
  }

  private void handle(JsonObject jsonObject, Handler<AsyncResult<JsonObject>> resultHandler) {
    String id = jsonObject.getString("id");
    vertx.executeBlocking(f -> decWeight(id, ar -> {
      if (ar.succeeded()) {
        LOGGER.info("[{}] [decWeight]", id);
        f.complete();
      } else {
        LOGGER.error("[{}] [decWeight]", id, ar.cause());
        f.fail(ar.cause());
      }
    }), true, ar -> {
      if (ar.failed()) {
        resultHandler.handle(Future.failedFuture(ar.cause()));
        return;
      }
      resultHandler.handle(Future.succeededFuture(new JsonObject().put("result", 1)));
    });
  }

  private void decWeight(String id, Handler<AsyncResult<Void>> handler) {
    discovery.getRecord(r -> r.getRegistration().equalsIgnoreCase(id), ar -> {
      if (ar.failed()) {
        handler.handle(Future.failedFuture(ar.cause()));
        return;
      }
      Record record = ar.result();
      int weight = record.getMetadata().getInteger("weight", 60);
      record.getMetadata().put("weight", weight - weightDecrease);
      update(record, handler);
    });
  }
}
