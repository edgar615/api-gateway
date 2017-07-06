package com.edgar.direwolves.dispatch;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.FilterFactory;
import com.edgar.direwolves.core.dispatch.Result;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * API请求的处理类.
 * 该类是整个网关和核心逻辑，所有的请求都会被这个处理类处理 .
 * 实际上DispatchHandler作为一个中心大脑来指导并驱动整个请求的处理，请求的处理逻辑被分散在各个filter中.
 * Filter分为两种：PRE和POST
 * PRE类型的filter会在RPC调用前处理，比如API路由的匹配，服务发现，参数校验等Filter都属于PRE类型.
 * POST类型的filter会在RPC调用之后处理，比如结果提取、日志等filter都属于POST类型.
 * <p>
 * 一次请求的调用过程如下描述
 * <p>
 * <pre>
 *                -> PRE Filter -> PRE Filter -> PRE Filter
 * 调用方                                                                            RPC -> 微服务
 *               <- POST Filter <- POST Filter <- POST Filter
 * </pre>
 * Created by edgar on 16-9-12.
 */
public class DispatchHandler implements Handler<RoutingContext> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DispatchHandler.class);

  /**
   * 过滤器集合
   */
  private final List<Filter> filters;

  private DispatchHandler(Vertx vertx, JsonObject config) {

    List<Filter> filterList = Lists.newArrayList(ServiceLoader.load(FilterFactory.class))
            .stream().map(f -> f.create(vertx, config))
            .collect(Collectors.toList());
    Filters.sort(filterList);
    this.filters = ImmutableList.copyOf(filterList);
    this.filters.forEach(filter -> {
      LOGGER.info("---| [load filter] [{}] [{}] [{}]",
                  filter.getClass().getSimpleName(),
                  filter.type(), filter.order());
    });
  }

  /**
   * 创建DispatchHandler
   *
   * @param vertx  Vertx对象
   * @param config 配置JSON
   * @return DispatchHandler
   */
  public static DispatchHandler create(Vertx vertx, JsonObject config) {
    return new DispatchHandler(vertx, config);
  }

  @Override
  public void handle(RoutingContext rc) {

    //创建上下文
    Task<ApiContext> task = Task.create();
    task.complete(ApiContextUtils.apiContext(rc));
    task = doFilter(task, f -> Filter.PRE.equalsIgnoreCase(f.type()));
    task = doFilter(task, f -> Filter.POST.equalsIgnoreCase(f.type()));
    task = task.andThen("Response", apiContext -> response(rc, apiContext));
//    task = doFilter(task, f -> Filter.AFTER_RESP.equalsIgnoreCase(f.type()));
    task.onFailure(throwable -> FailureHandler.doHandle(rc, throwable));
  }

  public Task<ApiContext> doFilter(Task<ApiContext> task, Predicate<Filter> filterPredicate) {
    List<Filter> postFilters = filters.stream()
            .filter(filterPredicate)
            .collect(Collectors.toList());
    return Filters.doFilter(task, postFilters);
  }

  private void response(RoutingContext rc, ApiContext apiContext) {
    rc.response().putHeader("x-request-id", apiContext.id());
    Result result = apiContext.result();
    int statusCode = result.statusCode();
    boolean isArray = result.isArray();
    if (isArray) {
      rc.response()
              .setStatusCode(statusCode)
              .setChunked(true)
              .end(result.responseArray().encode());
    } else {
      rc.response()
              .setStatusCode(statusCode)
              .setChunked(true)
              .end(result.responseObject().encode());
    }
  }

}
