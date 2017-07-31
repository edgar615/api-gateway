package com.edgar.direwolves.loadbalance;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.servicediscovery.Record;

/**
 * Created by Edgar on 2017/7/28.
 *
 * @author Edgar  Date 2017/7/28
 */
public interface LoadBalance {
  void choose(Handler<AsyncResult<Record>> resultHandler);
}
