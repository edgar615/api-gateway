package com.github.edgar615.gateway.http.loadbalance;

import com.google.common.base.Preconditions;

import io.vertx.core.json.JsonObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Edgar on 2017/11/6.
 *
 * @author Edgar  Date 2017/11/6
 */
public class LoadBalanceOptions {

  /**
   * 服务的负载均衡策略
   */
  private final Map<String, ChooseStrategy> strategies = new ConcurrentHashMap<>();

  public LoadBalanceOptions() {
  }

  public LoadBalanceOptions(JsonObject json) {
    this();
    LoadBalanceOptionsConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    LoadBalanceOptionsConverter.toJson(this, jsonObject);
    return jsonObject;
  }

  public LoadBalanceOptions addStrategy(String name, ChooseStrategy strategy) {
    Preconditions.checkNotNull(name);
    Preconditions.checkNotNull(strategy);
    strategies.remove(name);
    strategies.put(name, strategy);
    return this;
  }

  public Map<String, ChooseStrategy> getStrategies() {
    return strategies;
  }
}
