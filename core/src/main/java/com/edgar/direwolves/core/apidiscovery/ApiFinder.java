package com.edgar.direwolves.core.apidiscovery;

import com.edgar.direwolves.core.definition.ApiDefinition;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import java.util.List;

/**
 * 用于提高ApiDiscovery的本地缓存.
 *
 * @author Edgar  Date 2017/7/13
 */
public interface ApiFinder {

  void getDefinitions(String method, String path,
                      Handler<AsyncResult<List<ApiDefinition>>> resultHandler);

  int size();

  void reload(String name, Handler<AsyncResult<List<ApiDefinition>>> resultHandler);

  static ApiFinder create(Vertx vertx, ApiDiscoveryOptions options) {
    return new ApiFinderImpl(vertx, options);
  }
}
