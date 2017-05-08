package com.edgar.direwolves.dispatch.verticle;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.spi.ServiceImporter;

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

  private final ServiceDiscovery discovery;

  public RecordSelectImpl(Vertx vertx, JsonObject config) {
    discovery = ServiceDiscovery.create(vertx);
    String serviceDiscovery = config.getString("service.discovery");
    if (Strings.isNullOrEmpty(serviceDiscovery)) {
      throw SystemException.create(DefaultErrorCode.INVALID_ARGS)
              .set("details", "Config : service.discovery cannot be null");
    }
    if (serviceDiscovery.startsWith(CONSUL_PREFIX)) {
      registerConsul(serviceDiscovery, config);
    } else if (serviceDiscovery.startsWith(ZOOKEEPER_PREFIX)) {
      registerZookeeper(serviceDiscovery, config);
    } else {
      throw SystemException.create(DefaultErrorCode.INVALID_ARGS)
              .set("details", "Config : service.discovery:" + serviceDiscovery + " unsupported");
    }

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
      throw SystemException.wrap(DefaultErrorCode.UNKOWN, e)
              .set("details", e.getMessage());
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

}
