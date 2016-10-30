package com.edgar.direwolves.servicediscovery;

import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.consul.ConsulServiceImporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Edgar on 2016/10/12.
 *
 * @author Edgar  Date 2016/10/12
 */
class RecordSelectImpl implements RecordSelect {

  private static final String CONSUL_PREFIX = "consul://";


  private final Map<String, SelectStrategy> strategyMap = new HashMap<>();

  ServiceDiscovery discovery;

  private volatile boolean configed = false;

  @Override
  public Future<Record> select(final String service) {
    Future<Record> competeFuture = Future.future();
    if (discovery == null) {
      competeFuture.fail("ServiceDiscovery has not been started");
      return competeFuture;
    }
    synchronized (this) {
      SelectStrategy selectStrategy = strategyMap.get(service);
      if (selectStrategy == null) {
        selectStrategy = SelectStrategy.roundRobin();
        strategyMap.put(service, selectStrategy);
      }
    }
    getRecord(service, competeFuture);
    return competeFuture;
  }

  @Override
  public void config(Vertx vertx, JsonObject config) {
    if (configed) {
      throw new UnsupportedOperationException("RecordSelect has been config");
    }
    discovery = ServiceDiscovery.create(vertx);
    String serviceDiscovery = config.getString("service.discovery");
    Integer scanPeriod = config.getInteger("service.discovery.scan-period", 2000);
    if (Strings.isNullOrEmpty(serviceDiscovery)) {
      throw SystemException.create(DefaultErrorCode.INVALID_ARGS)
              .set("details", "service.discovery cannot be null");
    }
    if (serviceDiscovery.startsWith(CONSUL_PREFIX)) {
      String address = serviceDiscovery.substring(CONSUL_PREFIX.length());
      Iterable<String> iterable = Splitter.on(":").split(address);
      String host = Iterables.get(iterable, 0);
      int port = Integer.parseInt(Iterables.get(iterable, 1));
      discovery
              .registerServiceImporter(new ConsulServiceImporter(), new JsonObject()
                      .put("host", host)
                      .put("port", port)
                      .put("scan-period", scanPeriod));
    } else {
      throw SystemException.create(DefaultErrorCode.INVALID_ARGS)
              .set("details", "unspport service.discovery:" + serviceDiscovery);
    }

    JsonObject strategyConfig =
            config.getJsonObject("service.discovery.select-strategy", new JsonObject());
    strategyConfig.forEach(entry ->
                                   strategyMap.put(entry.getKey(),
                                                   selectStrategy(entry.getValue().toString())));

  }

  private void getRecord(String service, Future<Record> competeFuture) {
    SelectStrategy
            selectStrategy = strategyMap.get(service);

    discovery.getRecords(r -> service.equals(r.getMetadata().getString("ServiceName")), ar -> {
      if (ar.succeeded()) {
        List<Record> records = ar.result();
        competeFuture.complete(selectStrategy.select(records));
      } else {
        competeFuture.fail(ar.cause());
      }
    });
  }

  private SelectStrategy selectStrategy(String type) {
    SelectStrategy selectStrategy = null;
    switch (type) {
      case "random":
        selectStrategy = SelectStrategy.random();
        break;
      case "round_robin":
        selectStrategy = SelectStrategy.roundRobin();
        break;
      default:
        selectStrategy = SelectStrategy.roundRobin();
    }
    return selectStrategy;
  }
}
