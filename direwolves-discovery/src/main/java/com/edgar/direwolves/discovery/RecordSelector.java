package com.edgar.direwolves.discovery;

import io.vertx.core.Future;
import io.vertx.servicediscovery.Record;

import java.util.List;
import java.util.function.Function;

/**
 * 单个服务的服务发现.
 *
 * @author Edgar  Date 2017/5/8
 */
public interface RecordSelector {
  Future<Record> getRecord();

  Future<List<Record>> getRecords();

  Future<List<Record>> getRecords(Function<Record, Boolean> filter);

  Future<Record> getRecord(Function<Record, Boolean> filter);
}
