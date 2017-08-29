package com.edgar.direwolves.http.loadbalance;

import io.vertx.servicediscovery.Record;

import java.util.List;

/**
 * 最少连接数.
 * Created by edgar on 17-5-6.
 */
class LastConnectionStrategy implements ChooseStrategy {

  @Override
  public Record get(List<Record> records) {
    if (records == null || records.isEmpty()) {
      return null;
    }

    LoadBalanceStats stats = LoadBalanceStats.instance();
    int activeRequests = stats.get(records.get(0).getRegistration()).activeRequests();
    Record r = records.get(0);
    for (Record record : records) {
      ServiceStats serviceStats = stats.get(record.getRegistration());
      if (serviceStats.activeRequests() < activeRequests) {
        activeRequests = serviceStats.activeRequests();
        r = record;
      }

    }
    return r;

  }

}
