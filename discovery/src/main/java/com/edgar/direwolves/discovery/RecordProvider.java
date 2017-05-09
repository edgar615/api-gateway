package com.edgar.direwolves.discovery;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

import java.util.List;
import java.util.function.Function;

/**
 * 服务发现.
 *
 * @author Edgar  Date 2017/5/8
 */
public interface RecordProvider {

  Future<List<Record>> getRecords(String name);

  Future<List<Record>> getRecords(String name, Function<Record, Boolean> filter);

  Future<Record> getRecord(String name);

  Future<Record> getRecord(String name, Function<Record, Boolean> filter);

  static RecordProvider create(ServiceDiscovery discovery, JsonObject config) {
    return new RecordProviderImpl(discovery, config);
  }
}
