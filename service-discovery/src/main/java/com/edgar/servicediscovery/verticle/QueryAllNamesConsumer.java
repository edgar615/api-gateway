package com.edgar.servicediscovery.verticle;

import com.edgar.servicediscovery.MoreServiceDiscovery;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2017/6/9.
 *
 * @author Edgar  Date 2017/6/9
 */
class QueryAllNamesConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MoreServiceDiscovery.class);

  private final Vertx vertx;

  QueryAllNamesConsumer(Vertx vertx, ServiceDiscovery discovery) {
    this.vertx = vertx;
    vertx.eventBus().<JsonObject>consumer("service.discovery.queryForNames", msg -> {
      queryForNames(discovery, ar -> {
        if (ar.failed()) {
          msg.reply(new JsonObject().put("error", ar.cause().getMessage()));
          return;
        }
        msg.reply(ar.result());
      });
    });
  }

  private void queryForNames(ServiceDiscovery discovery,
                             Handler<AsyncResult<JsonObject>> handler) {
    JsonObject result = new JsonObject();
    queryAllInstances(discovery, ar -> {
      if (ar.failed()) {
        handler.handle(Future.failedFuture(ar.cause()));
        return;
      }
      List<Record> records = ar.result();
      Set<String> names = records.stream()
              .map(r -> r.getName())
              .distinct()
              .collect(Collectors.toSet());
      for (String name : names) {
        long count = records.stream()
                .filter(r -> r.getName().equals(name))
                .count();
        result.put(name, new JsonObject().put("instances", count));
      }
      handler.handle(Future.succeededFuture(result));
    });
  }

  private void queryAllInstances(ServiceDiscovery discovery,
                                 Handler<AsyncResult<List<Record>>> handler) {
    discovery.getRecords(r -> true, ar -> {
      if (ar.failed()) {
        handler.handle(Future.failedFuture(ar.cause()));
        return;
      }
      handler.handle(Future.succeededFuture(ar.result()));
    });
  }

}
