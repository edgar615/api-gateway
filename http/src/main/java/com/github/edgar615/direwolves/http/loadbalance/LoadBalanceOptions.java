package com.github.edgar615.direwolves.http.loadbalance;

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
  private Map<String, ChooseStrategy> strategyMap = new ConcurrentHashMap<>();

  public LoadBalanceOptions(JsonObject json) {
    if (json.getValue("strategy") instanceof JsonObject) {
      addStrategy(json.getJsonObject("strategy"));
    }
  }

  public JsonObject toJson() {
    JsonObject strategy = new JsonObject();
    strategyMap.forEach((k, v) -> {
      String strategyName = null;
      if (v instanceof RandomStrategy) {
        strategyName = "random";
      }
      if (v instanceof RoundRobinStrategy) {
        strategyName = "round_robin";
      }
      if (v instanceof WeightRoundbinStrategy) {
        strategyName = "weight_round_robin";
      }
      if (v instanceof StickyStrategy) {
        strategyName = "sticky";
      }
      if (v instanceof LastConnectionStrategy) {
        strategyName = "last_conn";
      }
      if (strategyName != null) {
        strategy.put(k, strategyName);
      }
    });
    JsonObject jsonObject = new JsonObject();
    jsonObject.put("strategy", strategy);
    return jsonObject;
  }

  public LoadBalanceOptions addStrategy(String name, ChooseStrategy strategy) {
    Preconditions.checkNotNull(name);
    Preconditions.checkNotNull(strategy);
    strategyMap.remove(name);
    strategyMap.put(name, strategy);
    return this;
  }

  public ChooseStrategy getStrategy(String name) {
    return strategyMap.getOrDefault(name, ChooseStrategy.roundRobin());
  }

  private void addStrategy(JsonObject strategyConfig) {
    for (String name : strategyConfig.fieldNames()) {
      if (strategyConfig.getValue(name) instanceof String) {
        String strategyName = strategyConfig.getString(name);
        ChooseStrategy strategy = ChooseStrategy.roundRobin();
        if ("random".equalsIgnoreCase(strategyName)) {
          strategy = ChooseStrategy.random();
        }
        if ("round_robin".equalsIgnoreCase(strategyName)) {
          strategy = ChooseStrategy.roundRobin();
        }
        if ("sticky".equalsIgnoreCase(strategyName)) {
          strategy = ChooseStrategy.sticky(ChooseStrategy.roundRobin());
        }
        if ("weight_round_robin".equalsIgnoreCase(strategyName)) {
          strategy = ChooseStrategy.weightRoundRobin();
        }
        if ("last_conn".equalsIgnoreCase(strategyName)) {
          strategy = ChooseStrategy.lastConnection();
        }
        strategyMap.put(name, strategy);
      }
    }
  }
}
