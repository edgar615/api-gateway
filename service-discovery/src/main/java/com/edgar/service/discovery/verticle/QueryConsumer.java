package com.edgar.service.discovery.verticle;

import com.google.common.base.Strings;

import com.edgar.service.discovery.MoreServiceDiscovery;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Edgar on 2017/6/9.
 *
 * @author Edgar  Date 2017/6/9
 */
class QueryConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MoreServiceDiscovery.class);

  private final Vertx vertx;

  QueryConsumer(Vertx vertx, ServiceDiscovery discovery) {
    this.vertx = vertx;
    vertx.eventBus().<JsonObject>consumer("service.discovery.query", msg -> {
      JsonObject jsonObject = msg.body();
      discovery.getRecords(jsonObject, ar -> {
        if (ar.failed()) {
          msg.fail(404, ar.cause().getMessage());
          return;
        }
        if (ar.result() == null) {
          msg.fail(404, String.format("service:%s not found", jsonObject.encode()));
          return;
        }
        JsonArray array = new JsonArray();
        for (Record record : ar.result()) {
          array.add(record.toJson());
        }
        msg.reply(array);
      });

    });
  }

}
