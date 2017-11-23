package com.github.edgar615.servicediscovery.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.consul.ConsulServiceImporter;
import io.vertx.servicediscovery.spi.ServiceImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 从Consul中读取服务节点.
 *
 * @author Edgar  Date 2017/6/9
 */
public class ConsulServiceDiscoveryVerticle extends AbstractVerticle {
  private static final Logger LOGGER =
          LoggerFactory.getLogger(ConsulServiceDiscoveryVerticle.class);

  @Override
  public void start() throws Exception {
    ServiceDiscoveryOptions options;
    if (config().getValue("service.discovery") instanceof JsonObject) {
      options = new ServiceDiscoveryOptions(config().getJsonObject("service.discovery"));
    } else {
      options = new ServiceDiscoveryOptions();
    }
    ServiceDiscovery discovery = ServiceDiscovery.create(vertx, options);


    LOGGER.info("deploy ConsulServiceDiscoveryVerticle succeeded");

    if (config().getValue("consul") instanceof JsonObject) {
      registerConsul(discovery, config().getJsonObject("consul"));
    }
  }

  private void registerConsul(ServiceDiscovery discovery, JsonObject config) {
    try {
      ServiceImporter serviceImporter = new ConsulServiceImporter();
      discovery
              .registerServiceImporter(serviceImporter, config,
                                       Future.<Void>future().completer());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
