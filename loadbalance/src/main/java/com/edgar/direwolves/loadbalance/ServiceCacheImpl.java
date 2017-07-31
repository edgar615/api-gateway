package com.edgar.direwolves.loadbalance;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2017/7/31.
 *
 * @author Edgar  Date 2017/7/31
 */
class ServiceCacheImpl implements ServiceCache {
  private final List<Record> records = new CopyOnWriteArrayList<>();

  ServiceCacheImpl(Vertx vertx, ServiceDiscovery discovery) {
    String announce = discovery.options().getAnnounceAddress();
    discovery.getRecords(new JsonObject(), ar -> {
      records.addAll(ar.result());
    });
    vertx.eventBus().<JsonObject>consumer(announce, msg -> {
      Record record = new Record(msg.body());
      String name = record.getName();
      discovery.getRecords(new JsonObject().put("name", name), ar -> {
        records.removeIf(r -> r.getName().endsWith(name));
        records.addAll(ar.result());
      });
    });
  }

  @Override
  public void getRecords(Function<Record, Boolean> filter,
                         Handler<AsyncResult<List<Record>>> resultHandler) {
    List<Record> result = records.stream()
            .filter(filter::apply)
            .collect(Collectors.toList());
    resultHandler.handle(Future.succeededFuture(result));
  }
}
