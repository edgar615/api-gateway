package com.edgar.direwolves.discovery;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.Status;

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

  private final Map<String, ServiceInstance> instances = new ConcurrentHashMap<>();

  private final Map<String, ProviderStrategy> strategyMap = new ConcurrentHashMap<>();

  private final JsonObject config;

  ServiceProviderImpl(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    String address = config.getString("service.discovery.announce", "vertx.discovery.announce");
    vertx.eventBus().<JsonObject>consumer(address, msg -> {
      JsonObject jsonObject = msg.body();
      Record record = new Record(jsonObject);
      if (record.getStatus() == Status.UP) {
        instances.putIfAbsent(record.getRegistration(), new ServiceInstance(record));
      } else {
        instances.remove(record.getRegistration());
      }
    });
    this.config = config;
  }

  @Override
  public List<ServiceInstance> getInstances() {
    return getInstances(i -> true);
  }

  @Override
  public List<ServiceInstance> getInstances(Function<ServiceInstance, Boolean> filter) {
    return instances.values()
            .stream()
            .filter(i -> filter.apply(i))
            .collect(Collectors.toList());
  }

  @Override
  public ServiceInstance getInstance() {
    return getInstance(r -> true);
  }

  @Override
  public ServiceInstance getInstance(Function<ServiceInstance, Boolean> filter) {
    return instances.values()
            .stream()
            .filter(i -> filter.apply(i))
            .findAny()
            .get();
  }

  @Override
  public ServiceInstance getInstance(String name) {
    return getOrCreateProvider(name)
            .get(getInstances(i -> i.name().equals(name) && i.weight() > 0));
  }

  public void success(String id, long duration) {
    if (duration > 3000) {
      failed(id);
    } else {
      instances.computeIfPresent(id, (s, instance) -> instance.incWeight(1));
    }
  }

  public void failed(String id) {
    instances.computeIfPresent(id, (s, instance) -> instance.decWeight(10));
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
