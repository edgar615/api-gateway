package com.edgar.direwolves.discovery;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.Status;
import io.vertx.servicediscovery.impl.AsyncMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by edgar on 17-5-9.
 */
class ServiceProviderImpl implements ServiceProvider {
  private final Vertx vertx;

  private final AsyncMap<String, ServiceInstance> instances;

  private final Map<String, ProviderStrategy> strategyMap = new ConcurrentHashMap<>();

  private final JsonObject config;

  ServiceProviderImpl(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.instances = new AsyncMap<>(vertx, "service.cache");
    String address = config.getString("service.discovery.announce", "vertx.discovery.announce");
    vertx.eventBus().<JsonObject>consumer(address, msg -> {
      JsonObject jsonObject = msg.body();
      Record record = new Record(jsonObject);
      if (record.getStatus() == Status.UP) {

      } else {
      }
    });
    this.config = config;
  }

  @Override
  public void getInstances(Handler<AsyncResult<List<ServiceInstance>>> handler) {
    getInstances(i -> true, handler);
  }

  @Override
  public void getInstances(Function<ServiceInstance, Boolean> filter,
                           Handler<AsyncResult<List<ServiceInstance>>> handler) {
    instances.getAll(ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture(ar.result().values().stream()
                                                      .filter(i -> filter.apply(i))
                                                      .collect(Collectors.toList())));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void getInstance(Handler<AsyncResult<ServiceInstance>> handler) {
    getInstance(r -> true, handler);
  }

  @Override
  public void getInstance(Function<ServiceInstance, Boolean> filter,
                          Handler<AsyncResult<ServiceInstance>> handler) {
    instances.getAll(ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture(ar.result().values().stream()
                                                      .filter(i -> filter.apply(i))
                                                      .findAny().get()));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void getInstance(String name,
                          Handler<AsyncResult<ServiceInstance>> handler) {
    getInstances(i -> i.name().equals(name), ar -> {
      if (ar.failed()) {
        handler.handle(Future.failedFuture(ar.cause()));
      } else {
        try {
          ServiceInstance instance = getOrCreateProvider(name)
                  .get(ar.result());
          handler.handle(Future.succeededFuture(instance));
        } catch (Exception e) {
          handler.handle(Future.failedFuture(e));
        }
      }
    });
  }

  private ProviderStrategy getOrCreateProvider(String name) {
    return strategyMap.computeIfAbsent(name, k -> createProvider(name));
  }

  private ProviderStrategy createProvider(String name) {
    JsonObject jsonObject = config.getJsonObject(name, new JsonObject());
    String strategy = jsonObject.getString("strategy", "round_robin");
    if ("random".equalsIgnoreCase(strategy)) {
      return ProviderStrategy.random();
    }
    if ("round_robin".equalsIgnoreCase(strategy)) {
      return ProviderStrategy.roundRobin();
    }
    if ("sticky".equalsIgnoreCase(strategy)) {
      return ProviderStrategy.sticky(ProviderStrategy.roundRobin());
    }
    if ("weight_round_robin".equalsIgnoreCase(strategy)) {
      return ProviderStrategy.weightRoundRobin();
    }
    throw new UnsupportedOperationException("Strategy " + strategy);
  }

}
