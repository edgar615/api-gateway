package com.edgar.service.discovery;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;

/**
 * Created by Edgar on 2017/6/8.
 *
 * @author Edgar  Date 2017/6/8
 */
public class MoreServiceDiscoveryOptions {

  private static final Long DEFAULT_TIMEOUT_THRESHOLD = 3000l;

  private static final int DEFAULT_WEIGHT_INCREASE = 1;

  private static final int DEFAULT_WEIGHT_DECREASE = 5;

  private final ServiceDiscoveryOptions serviceDiscoveryOptions;

  /**
   * 服务发现策略
   */
  private final JsonObject strategy = new JsonObject();

  /**
   * 服务端超过多少毫秒的响应认为是超时
   */
  private long timeoutThreshold = DEFAULT_TIMEOUT_THRESHOLD;

  /**
   * 成功之后增加的权重
   */
  private int weightIncrease = DEFAULT_WEIGHT_INCREASE;

  /**
   * 失败或超时之后减少的权重
   */
  private int weightDecrease = DEFAULT_WEIGHT_DECREASE;

  public MoreServiceDiscoveryOptions() {
    this.serviceDiscoveryOptions = new ServiceDiscoveryOptions();
  }

  public MoreServiceDiscoveryOptions(JsonObject json) {
    this.serviceDiscoveryOptions = new ServiceDiscoveryOptions(json);
    if (json.getValue("weightIncrease") instanceof Number) {
      this.weightIncrease = json.getInteger("weightIncrease");
    }
    if (json.getValue("weightDecrease") instanceof Number) {
      this.weightDecrease = json.getInteger("weightDecrease");
    }
    if (json.getValue("strategy") instanceof JsonObject) {
      this.strategy.mergeIn(json.getJsonObject("strategy"));
    }
  }

  public MoreServiceDiscoveryOptions(ServiceDiscoveryOptions serviceDiscoveryOptions) {
    this.serviceDiscoveryOptions = serviceDiscoveryOptions;
  }

  public long getTimeoutThreshold() {
    return timeoutThreshold;
  }

  public MoreServiceDiscoveryOptions setTimeoutThreshold(long timeoutThreshold) {
    this.timeoutThreshold = timeoutThreshold;
    return this;
  }

  public int getWeightIncrease() {
    return weightIncrease;
  }

  public MoreServiceDiscoveryOptions setWeightIncrease(int weightIncrease) {
    this.weightIncrease = weightIncrease;
    return this;
  }

  public int getWeightDecrease() {
    return weightDecrease;
  }

  public MoreServiceDiscoveryOptions setWeightDecrease(int weightDecrease) {
    this.weightDecrease = weightDecrease;
    return this;
  }

  public MoreServiceDiscoveryOptions addStrategy(String serviceName, String strategyName) {
    strategy.put(serviceName, strategyName);
    return this;
  }

  public JsonObject getStrategy() {
    return strategy;
  }

  public ServiceDiscoveryOptions getServiceDiscoveryOptions() {
    return serviceDiscoveryOptions;
  }
}
