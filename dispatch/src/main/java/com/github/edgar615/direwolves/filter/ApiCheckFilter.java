package com.github.edgar615.direwolves.filter;

import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * 该filter检查上下文中有没有对应的API，如果没有直接返回对应的异常.
 * <p>
 * 该filter可以接受下列的配置参数
 * <pre>
 * </pre>
 * <p>
 * <b>该filter应该在所有的filter之前执行</b>如果未找到对应的API定义，直接返回对应的异常。
 * 该filter的order=-2147482648
 * Created by edgar on 17-1-4.
 */
public class ApiCheckFilter implements Filter {

  private final Vertx vertx;

  public ApiCheckFilter(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
  }

  @Override
  public String type() {
    return PRE;
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE + 1099;
  }

  @Override
  public boolean shouldFilter(ApiContext apiContext) {
    return true;
  }

  @Override
  public void doFilter(ApiContext apiContext, Future<ApiContext> completeFuture) {
    if (apiContext.apiDefinition() == null) {
      SystemException systemException =
              SystemException.create(DefaultErrorCode.RESOURCE_NOT_FOUND);
      ;
      systemException.set("details", String.format("Undefined Api %s:%s",
                                                   apiContext.method().name(),
                                                   apiContext.path()));
      failed(completeFuture, apiContext.id(), "api.tripped", systemException);
    } else {
      completeFuture.complete(apiContext);
    }
  }

}
