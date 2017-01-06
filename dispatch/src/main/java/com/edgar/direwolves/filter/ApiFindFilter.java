package com.edgar.direwolves.filter;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiProvider;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * Created by edgar on 17-1-4.
 */
public class ApiFindFilter implements Filter {

  private final Vertx vertx;

  private final ApiProvider apiProvider;

  public ApiFindFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    String address = config.getString("api.provider.address", "direwolves.api");
    this.apiProvider = ProxyHelper.createProxy(ApiProvider.class, vertx, address);
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return true;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    apiProvider.match(apiContext.method().name(), apiContext.path(), ar -> {
      if (ar.succeeded()) {
        try {
          ApiDefinition apiDefinition = ApiDefinition.fromJson(ar.result());
          apiContext.setApiDefinition(apiDefinition);
          completeFuture.complete(apiContext);
        } catch (Exception e) {
          completeFuture.fail(e);
        }
      } else {
        completeFuture.fail(ar.cause());
      }
    });
  }

}
