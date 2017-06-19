package com.edgar.service.discovery.verticle;

import com.google.common.base.Strings;

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
class QueryByIdConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MoreServiceDiscovery.class);

  private final Vertx vertx;

  QueryByIdConsumer(Vertx vertx, ServiceDiscovery discovery) {
    this.vertx = vertx;
    vertx.eventBus().<JsonObject>consumer("service.discovery.queryById", msg -> {
      JsonObject jsonObject = msg.body();
      String id = jsonObject.getString("id");
      if (Strings.isNullOrEmpty(id)) {
        msg.fail(-1, "Arg `id` is undefined");
        return;
      }
      discovery.getRecord(r -> id.equals(r.getRegistration()), ar -> {
        if (ar.failed()) {
          msg.fail(404, ar.cause().getMessage());
          return;
        }
        if (ar.result() == null) {
          msg.fail(404, String.format("service:%s not found", id));
          return;
        }
        msg.reply(ar.result().toJson());
      });

    });
  }

}
