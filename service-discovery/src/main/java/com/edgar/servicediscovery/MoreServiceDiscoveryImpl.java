package com.edgar.servicediscovery;

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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Edgar on 2017/6/8.
 *
 * @author Edgar  Date 2017/6/8
 */
@Deprecated
class MoreServiceDiscoveryImpl implements MoreServiceDiscovery {

  private static final Logger LOGGER = LoggerFactory.getLogger(MoreServiceDiscovery.class);

  private final ServiceDiscovery discovery;

  /**
   * 服务发现策略
   */
  private final JsonObject strategyConfig;

  private final Vertx vertx;

  private final Map<String, ServiceProvider> providerMap = new ConcurrentHashMap<>();

  MoreServiceDiscoveryImpl(Vertx vertx) {
    this(vertx, new MoreServiceDiscoveryOptions());
  }

  MoreServiceDiscoveryImpl(Vertx vertx, MoreServiceDiscoveryOptions options) {
    this.vertx = vertx;
    this.discovery = ServiceDiscovery.create(vertx, options.getServiceDiscoveryOptions());
    this.strategyConfig = options.getStrategy();
  }

  @Override
  public ServiceDiscovery discovery() {
    return discovery;
  }

  @Override
  public void queryAllInstances(String name, Handler<AsyncResult<List<Record>>> handler) {
    getOrCreateProvider(name).getInstances(r -> true, handler);
  }

  @Override
  public void queryAllInstances(Handler<AsyncResult<List<Record>>> handler) {
    discovery.getRecords(r -> true, ar -> {
      if (ar.failed()) {
        handler.handle(Future.failedFuture(ar.cause()));
        return;
      }
      handler.handle(Future.succeededFuture(ar.result()));
    });
  }

  @Override
  public void queryForInstance(String name, Handler<AsyncResult<Record>> handler) {
    getOrCreateProvider(name).getInstance(r -> true, handler);
  }

  @Override
  public void queryForInstance(String name, String id,
                               Handler<AsyncResult<Record>> handler) {
    getOrCreateProvider(name).getInstances(r -> r.getRegistration().equals(id), ar -> {
      if (ar.failed()) {
        handler.handle(Future.failedFuture(ar.cause()));
        return;
      }
      if (ar.result().isEmpty()) {
        handler.handle(Future.failedFuture(new NullPointerException()));
        return;
      }
      handler.handle(Future.succeededFuture(ar.result().get(0)));
    });
  }

  @Override
  public void queryForNames(Handler<AsyncResult<JsonObject>> handler) {
    JsonObject result = new JsonObject();
    queryAllInstances(ar -> {
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

  private ServiceProvider getOrCreateProvider(String name) {
    return providerMap.computeIfAbsent(name, k -> createProvider(name));
  }

  private ServiceProvider createProvider(String serviceName) {
    String strategyName = strategyConfig.getString(serviceName, "round_robin");
    ProviderStrategy strategy = ProviderStrategy.roundRobin();
    if ("random".equalsIgnoreCase(strategyName)) {
      strategy = ProviderStrategy.random();
    }
    if ("round_robin".equalsIgnoreCase(strategyName)) {
      strategy = ProviderStrategy.roundRobin();
    }
    if ("sticky".equalsIgnoreCase(strategyName)) {
      strategy = ProviderStrategy.sticky(ProviderStrategy.roundRobin());
    }
    if ("weight_round_robin".equalsIgnoreCase(strategyName)) {
      strategy = ProviderStrategy.weightRoundRobin();
    }
    return new ServiceProviderImpl(this.discovery, serviceName, strategy);
  }
}
