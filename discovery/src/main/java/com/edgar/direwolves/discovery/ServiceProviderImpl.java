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

  private final JsonObject strategyConfig;

  private final long timeoutThreshold;

  private final int weightIncrease;

  private final int weightDecrease;

  ServiceProviderImpl(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    String address = config.getString("service.discovery.announce", "vertx.discovery.announce");
    this.timeoutThreshold = config.getLong("service.discovery.weight.timeout", 2000l);
    this.weightIncrease = config.getInteger("service.discovery.weight.increase", 1);
    this.weightDecrease = config.getInteger("service.discovery.weight.decrease", 10);
    this.strategyConfig =
            config.getJsonObject("service.discovery.strategy", new JsonObject());
    vertx.eventBus().<JsonObject>consumer(address, msg -> {
      JsonObject jsonObject = msg.body();
      Record record = new Record(jsonObject);
      if (record.getStatus() == Status.UP) {
        ServiceInstance instance = new ServiceInstance(record);
        instances.putIfAbsent(instance.id(), instance);
      } else {
        ServiceInstance instance = new ServiceInstance(record);
        instances.remove(instance.id());
      }
    });
    vertx.eventBus().<JsonObject>consumer(address + ".weight", msg -> {
      JsonObject jsonObject = msg.body();
      String id = jsonObject.getString("id");
      long duration = jsonObject.getLong("duration", 0l);
      boolean result = jsonObject.getBoolean("result", true);
      if (result) {
        complete(id, duration);
      } else {
        fail(id);
      }
    });

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
  public ServiceInstance getInstance(String name) {
    return getOrCreateProvider(name)
            .get(getInstances(i -> i.name().equals(name) && i.weight() > 0));
  }

  @Override
  public void complete(String id, long duration) {
    if (duration > timeoutThreshold) {
      fail(id);
    } else {
      instances.computeIfPresent(id, (s, instance) -> instance.incWeight(weightIncrease));
    }
  }

  @Override
  public void fail(String id) {
    instances.computeIfPresent(id, (s, instance) -> instance.decWeight(weightDecrease));
  }

  private ProviderStrategy getOrCreateProvider(String name) {
    return strategyMap.computeIfAbsent(name, k -> createProvider(name));
  }

  private ProviderStrategy createProvider(String name) {
    JsonObject jsonObject = strategyConfig.getJsonObject(name, new JsonObject());
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
