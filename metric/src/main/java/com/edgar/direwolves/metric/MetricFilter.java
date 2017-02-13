package com.edgar.direwolves.metric;

import com.edgar.direwolves.core.cache.RedisProvider;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * Created by Edgar on 2017/2/9.
 *
 * @author Edgar  Date 2017/2/9
 */
public class MetricFilter implements Filter {

  private final Vertx vertx;

  private final RedisProvider redisProvider;

  MetricFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    String address = config.getString("service.cache.address", "direwolves.cache.provider");
    this.redisProvider = ProxyHelper.createProxy(RedisProvider.class, vertx, address);
  }

  @Override
  public String type() {
    return POST;
  }

  @Override
  public int order() {
    return Integer.MAX_VALUE;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return true;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {

  }
}
