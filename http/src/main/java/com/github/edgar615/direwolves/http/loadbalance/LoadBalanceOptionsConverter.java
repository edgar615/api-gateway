package com.github.edgar615.direwolves.http.loadbalance;

import io.vertx.core.json.JsonObject;

class LoadBalanceOptionsConverter {

  static void fromJson(JsonObject json, LoadBalanceOptions obj) {
    if (json.getValue("strategy") instanceof JsonObject) {
      addStrategy(json.getJsonObject("strategy"), obj);
    }
  }

  static void toJson(LoadBalanceOptions obj, JsonObject json) {
   putStrategy(obj, json);
  }

  private static void putStrategy(LoadBalanceOptions obj, JsonObject json) {
    JsonObject strategy = new JsonObject();
    obj.getStrategies().forEach((k, v) -> {
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
    json.put("strategy", strategy);
  }

  private static void addStrategy(JsonObject strategyConfig, LoadBalanceOptions obj) {
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
        obj.addStrategy(name, strategy);
      }
    }
  }
}