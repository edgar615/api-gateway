package com.edgar.service.discovery;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Edgar on 2017/6/8.
 *
 * @author Edgar  Date 2017/6/8
 */
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

  @Override
  public ServiceDiscovery discovery() {
    return discovery;
  }

  MoreServiceDiscoveryImpl(Vertx vertx, MoreServiceDiscoveryOptions options) {
    this.vertx = vertx;
    this.discovery = ServiceDiscovery.create(vertx, options.getServiceDiscoveryOptions());
    this.strategyConfig = options.getStrategy();
  }

  @Override
  public void queryAllInstances(String name, Handler<AsyncResult<List<Record>>> handler) {
    getOrCreateProvider(name).getInstances(r -> true, handler);
  }

  @Override
  public void queryAllInstances(Handler<AsyncResult<List<Record>>> handler) {
    List<Future> futures = new ArrayList<>();
    providerMap.values().forEach(p -> {
      Future<List<Record>> future = Future.future();
      futures.add(future);
      p.getAllInstances(ar -> {
        if (ar.failed()) {
          future.complete(new ArrayList<>());
        } else {
          future.complete(ar.result());
        }
      });
    });
    CompositeFuture.all(futures)
            .setHandler(ar -> {
              if (ar.failed()) {
                handler.handle(Future.failedFuture(ar.cause()));
                return;
              }
              List<Record> instances = new ArrayList<>();
              for (int i = 0; i < ar.result().size(); i++) {
                instances.addAll(ar.result().resultAt(i));
              }
              handler.handle(Future.succeededFuture(instances));
            });
  }

  @Override
  public void queryForInstance(String name, Handler<AsyncResult<Record>> handler) {
    getOrCreateProvider(name).getInstance(handler);
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
