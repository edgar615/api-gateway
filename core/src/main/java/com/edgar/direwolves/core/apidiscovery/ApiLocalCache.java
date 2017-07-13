package com.edgar.direwolves.core.apidiscovery;

import com.edgar.direwolves.core.definition.ApiDefinition;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import java.util.List;

/**
 * Created by Edgar on 2017/7/13.
 *
 * @author Edgar  Date 2017/7/13
 */
public interface ApiLocalCache {

  void getDefinitions(String method, String path,
                      Handler<AsyncResult<List<ApiDefinition>>> resultHandler);

  int size();

  static ApiLocalCache create(Vertx vertx, ApiDiscoveryOptions options) {
    return new ApiLocalCacheImpl(vertx, options);
  }
}
