package com.edgar.direwolves.filter;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import com.edgar.direwolves.record.SelectStrategy;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.spi.ServiceImporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Edgar on 2016/10/12.
 *
 * @author Edgar  Date 2016/10/12
 */
class RecordSelectImpl implements RecordSelect {

  private final String CONSUL_PREFIX = "consul://";

  private final String ZOOKEEPER_PREFIX = "zookeeper://";

  private final String consulImportClass = "io.vertx.servicediscovery.consul.ConsulServiceImporter";

  private final String zookeeperImportClass =
          "com.edgar.direwolves.servicediscovery.zookeeper.ZookeeperServiceImporter";


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
        selectStrategy = SelectStrategy.create("round_robin");
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
    if (Strings.isNullOrEmpty(serviceDiscovery)) {
      throw SystemException.create(DefaultErrorCode.INVALID_ARGS)
              .set("details", "service.discovery cannot be null");
    }
    if (serviceDiscovery.startsWith(CONSUL_PREFIX)) {
      registerConsul(serviceDiscovery, config);
    } else if (serviceDiscovery.startsWith(ZOOKEEPER_PREFIX)) {
      registerZookeeper(serviceDiscovery, config);
    } else {
      throw SystemException.create(DefaultErrorCode.INVALID_ARGS)
              .set("details", "unspport service.discovery:" + serviceDiscovery);
    }

    JsonObject strategyConfig =
            config.getJsonObject("service.discovery.select-strategy", new JsonObject());
    strategyConfig.forEach(entry ->
                                   strategyMap.put(entry.getKey(),
                                                   SelectStrategy
                                                           .create(entry.getValue().toString())
                                   ));

  }

  private void registerZookeeper(String serviceDiscovery, JsonObject config) {
    String address = serviceDiscovery.substring(ZOOKEEPER_PREFIX.length());
    JsonObject zkConfig = new JsonObject()
            .put("zookeeper.connect", address);
    if (config.containsKey("zookeeper.retry.times")) {
      zkConfig.put("zookeeper.retry.times", config.getValue("zookeeper.retry.times"));
    }
    if (config.containsKey("zookeeper.retry.sleep")) {
      zkConfig.put("zookeeper.retry.sleep", config.getValue("zookeeper.retry.sleep"));
    }
    if (config.containsKey("zookeeper.path")) {
      zkConfig.put("zookeeper.path", config.getValue("zookeeper.path"));
    }
    try {
      ServiceImporter serviceImporter =
              (ServiceImporter) Class.forName(zookeeperImportClass).newInstance();
      discovery
              .registerServiceImporter(serviceImporter, zkConfig);
    } catch (Exception e) {
      throw SystemException.wrap(DefaultErrorCode.UNKOWN, e);
    }
  }

  private void registerConsul(String serviceDiscovery, JsonObject config) {
    String address = serviceDiscovery.substring(CONSUL_PREFIX.length());
    Iterable<String> iterable = Splitter.on(":").split(address);
    String host = Iterables.get(iterable, 0);
    int port = Integer.parseInt(Iterables.get(iterable, 1));
    Integer scanPeriod = config.getInteger("service.discovery.scan-period", 2000);
    try {
      ServiceImporter serviceImporter =
              (ServiceImporter) Class.forName(consulImportClass).newInstance();
      discovery
              .registerServiceImporter(serviceImporter, new JsonObject()
                      .put("host", host)
                      .put("port", port)
                      .put("scan-period", scanPeriod));
    } catch (Exception e) {
      throw SystemException.wrap(DefaultErrorCode.UNKOWN, e);
    }
  }

  private void getRecord(String service, Future<Record> competeFuture) {
    SelectStrategy
            selectStrategy = strategyMap.get(service);

    discovery.getRecords(r -> service.equals(r.getName()), ar -> {
      if (ar.succeeded()) {
        List<Record> records = ar.result();
        competeFuture.complete(selectStrategy.select(records));
      } else {
        competeFuture.fail(ar.cause());
      }
    });
  }

}
