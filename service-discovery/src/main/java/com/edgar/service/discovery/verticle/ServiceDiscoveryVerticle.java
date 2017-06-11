package com.edgar.service.discovery.verticle;

import com.edgar.service.discovery.MoreServiceDiscovery;
import com.edgar.service.discovery.MoreServiceDiscoveryOptions;
import io.vertx.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将对服务节点的权重、状态的更新操作放在同一个应用里按顺序执行，避免在分布式环境下对节点的并发修改.
 *
 * @author Edgar  Date 2017/6/9
 */
public class ServiceDiscoveryVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscoveryVerticle.class);

  @Override
  public void start() throws Exception {
    MoreServiceDiscoveryOptions options = new MoreServiceDiscoveryOptions(config());
    MoreServiceDiscovery serviceDiscovery = MoreServiceDiscovery.create(vertx, options);
    new WeightIncreaseConsumer(vertx, serviceDiscovery.discovery(),
                               options.getWeightIncrease());
    new WeightIncreaseConsumer(vertx, serviceDiscovery.discovery(),
                               options.getWeightIncrease());
    new StateOpenConsumer(vertx, serviceDiscovery.discovery());
    new StateCloseConsumer(vertx, serviceDiscovery.discovery());
    new StateHalfOpenConsumer(vertx, serviceDiscovery.discovery());
  }
}
