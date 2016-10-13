package com.edgar.direwolves.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.servicediscovery.Record;

/**
 * Created by Edgar on 2016/10/13.
 *
 * @author Edgar  Date 2016/10/13
 */
public class ServiceDiscoveryVerticle extends AbstractVerticle {

  public static final String ADDRESS = "service.discovery.select";

  @Override
  public void start() throws Exception {
    RecordSelect recordSelect = RecordSelect.create();
    recordSelect.config(vertx, config());
    vertx.eventBus().<String>consumer(ADDRESS, msg -> {
      String service = msg.body();
      Future<Record> future = recordSelect.select(service);
      future.setHandler(ar -> {
        if (ar.succeeded()) {
          Record record = ar.result();
          if (record == null) {
            msg.fail(404, "no " + service + " instance found");
          } else {
            msg.reply(ar.result().toJson());
          }
        } else {
          msg.fail(-1, ar.cause().getMessage());
        }
      });
    });
  }
}
