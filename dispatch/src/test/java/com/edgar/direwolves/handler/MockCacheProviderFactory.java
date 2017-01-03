package com.edgar.direwolves.handler;

import com.edgar.direwolves.core.cache.CacheFactory;
import com.edgar.direwolves.core.cache.CacheProvider;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Created by edgar on 16-12-9.
 */
public class MockCacheProviderFactory implements CacheFactory {

  @Override
  public CacheProvider create(Vertx vertx, JsonObject config) {
    return new MockCacheProvider();
  }
}
