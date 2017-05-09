package com.edgar.direwolves.discovery;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Created by Edgar on 2017/5/8.
 *
 * @author Edgar  Date 2017/5/8
 */
class RecordProviderImpl implements RecordProvider {

  private final Map<String, RecordSelector> providers = new ConcurrentHashMap<>();

  private final ServiceDiscovery discovery;

  private final JsonObject config;

  RecordProviderImpl(ServiceDiscovery discovery, JsonObject config) {
    this.discovery = discovery;
    this.config = config;
  }

  @Override
  public Future<List<Record>> getRecords(String name) {
    RecordSelector provider = getOrCreateProvider(name);
    Future<List<Record>> future = Future.future();
    provider.getRecords().setHandler(future.completer());
    return future;
  }

  @Override
  public Future<List<Record>> getRecords(String name, Function<Record, Boolean> filter) {
    RecordSelector provider = getOrCreateProvider(name);
    Future<List<Record>> future = Future.future();
    provider.getRecords(filter).setHandler(future.completer());
    return future;
  }

  @Override
  public Future<Record> getRecord(String name) {
    RecordSelector provider = getOrCreateProvider(name);
    Future<Record> future = Future.future();
    provider.getRecord().setHandler(future.completer());
    return future;
  }

  @Override
  public Future<Record> getRecord(String name, Function<Record, Boolean> filter) {
    RecordSelector provider = getOrCreateProvider(name);
    Future<Record> future = Future.future();
    provider.getRecord(filter).setHandler(future.completer());
    return future;
  }

  private RecordSelector getOrCreateProvider(String name) {
    return providers.computeIfAbsent(name, k -> createProvider(name));
  }

  private RecordSelector createProvider(String name) {
    JsonObject jsonObject = config.getJsonObject(name, new JsonObject());
    String strategy = jsonObject.getString("strategy", "round_robin");
    if ("random".equalsIgnoreCase(strategy)) {
      return new RecordSelectorImpl(name, discovery, SelectStrategy.random());
    }
    if ("round_robin".equalsIgnoreCase(strategy)) {
      return new RecordSelectorImpl(name, discovery, SelectStrategy.roundRobin());
    }
    if ("sticky".equalsIgnoreCase(strategy)) {
      return new RecordSelectorImpl(name, discovery,
                                    SelectStrategy.sticky(SelectStrategy.roundRobin()));
    }
    if ("weight_round_robin".equalsIgnoreCase(strategy)) {
      return new RecordSelectorImpl(name, discovery,
                                    SelectStrategy.weightRoundRobin());
    }
    throw new UnsupportedOperationException("Strategy " + strategy);
  }
}
