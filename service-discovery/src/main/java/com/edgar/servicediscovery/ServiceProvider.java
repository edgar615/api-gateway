package com.edgar.servicediscovery;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.servicediscovery.Record;

import java.util.List;
import java.util.function.Function;

/**
 * Created by Edgar on 2017/6/8.
 *
 * @author Edgar  Date 2017/6/8
 */
public interface ServiceProvider {
  void getInstances(Function<Record, Boolean> filter,
                    Handler<AsyncResult<List<Record>>> handler);

  void getAllInstances(Handler<AsyncResult<List<Record>>> handler);

  void getInstance(Function<Record, Boolean> filter,Handler<AsyncResult<Record>> handler);
}
