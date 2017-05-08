package com.edgar.direwolves.discovery;

import io.vertx.core.Future;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

import java.util.List;
import java.util.function.Function;

/**
 * Created by Edgar on 2017/5/8.
 *
 * @author Edgar  Date 2017/5/8
 */
class RecordSelectorImpl implements RecordSelector {

  private final String service;

  private final ServiceDiscovery discovery;

  private final SelectStrategy strategy;

  RecordSelectorImpl(String service, ServiceDiscovery discovery, SelectStrategy strategy) {
    this.service = service;
    this.discovery = discovery;
    this.strategy = strategy;
  }

  @Override
  public Future<Record> getRecord() {
    return getRecord(r -> true);
  }

  @Override
  public Future<List<Record>> getRecords() {
    return getRecords(r -> true);
  }

  @Override
  public Future<List<Record>> getRecords(Function<Record, Boolean> filter) {
    Future<List<Record>> competeFuture = Future.future();
    discovery.getRecords(r -> service.equals(r.getName())
                              && filter.apply(r), ar -> {
      if (ar.succeeded()) {
        List<Record> records = ar.result();
        competeFuture.complete(records);
      } else {
        competeFuture.fail(ar.cause());
      }
    });
    return competeFuture;
  }

  @Override
  public Future<Record> getRecord(Function<Record, Boolean> filter) {
    Future<Record> future = Future.future();
    getRecords(filter)
            .setHandler(ar -> {
              if (ar.failed()) {
                future.fail(ar.cause());
              } else {
                try {
                  future.complete(strategy.get(ar.result()));
                } catch (Exception e) {
                  future.fail(e);
                }
              }
            });
    return future;
  }
}
