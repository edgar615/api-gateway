package com.edgar.servicediscovery.verticle;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.spi.ServiceImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将对服务节点的权重、状态的更新操作放在同一个应用里按顺序执行，避免在分布式环境下对节点的并发修改.
 *
 * @author Edgar  Date 2017/6/9
 */
public class ServiceDiscoveryVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscoveryVerticle.class);
  private final String CONSUL_PREFIX = "consul://";

  private final String ZOOKEEPER_PREFIX = "zookeeper://";

  private final String consulImportClass =
          "io.vertx.servicediscovery.consul.ConsulServiceImporter";

  private final String zookeeperImportClass =
          "com.edgar.servicediscovery.zookeeper.ZookeeperServiceImporter";

  @Override
  public void start() throws Exception {
    ServiceDiscovery discovery = ServiceDiscovery.create(vertx);

    LOGGER.info("deploy ServiceDiscoveryVerticle succeeded");

    String importer = config().getString("importer");
    if (Strings.isNullOrEmpty(importer)) {
      return;
    }
    if (importer.startsWith(CONSUL_PREFIX)) {
      LOGGER.info("import service from consul");
      registerConsul(discovery, importer, config());
    } else if (importer.startsWith(ZOOKEEPER_PREFIX)) {
      LOGGER.info("import service from zookeeper");
      registerZookeeper(discovery, importer, config());
    } else {
      throw new IllegalArgumentException(
              String.format("Field `importer` not supported: %s", importer));
    }

  }

  private void registerZookeeper(ServiceDiscovery discovery, String importer, JsonObject config) {
    String address = importer.substring(ZOOKEEPER_PREFIX.length());
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
              .registerServiceImporter(serviceImporter, zkConfig,
                                       Future.<Void>future().completer());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void registerConsul(ServiceDiscovery discovery, String importer, JsonObject config) {
    String address = importer.substring(CONSUL_PREFIX.length());
    Iterable<String> iterable = Splitter.on(":").split(address);
    String host = Iterables.get(iterable, 0);
    int port = Integer.parseInt(Iterables.get(iterable, 1));
    Integer scanPeriod = config.getInteger("consul.scan-period", 2000);
    try {
      ServiceImporter serviceImporter =
              (ServiceImporter) Class.forName(consulImportClass).newInstance();
      discovery
              .registerServiceImporter(serviceImporter, new JsonObject()
                                               .put("host", host)
                                               .put("port", port)
                                               .put("scan-period", scanPeriod),
                                       Future.<Void>future().completer());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
