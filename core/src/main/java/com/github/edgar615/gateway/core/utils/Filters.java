package com.github.edgar615.gateway.core.utils;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.util.log.Log;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Future;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Filter的工具类.
 *
 * @author Edgar  Date 2016/9/20
 */
public class Filters {

  private Filters() {
    throw new AssertionError("Not instantiable: " + Filters.class);
  }

  /**
   * 将filter按照order()生序排列.
   *
   * @param filters filter的列表
   */
  public static void sort(List<Filter> filters) {
    Collections.sort(filters, (Filter o1, Filter o2) -> {
      if ("PRE".equalsIgnoreCase(o1.type())
          && "POST".equalsIgnoreCase(o2.type())) {
        return -1;
      }
      if ("POST".equalsIgnoreCase(o1.type())
          && "PRE".equalsIgnoreCase(o2.type())) {
        return 1;
      }
      if (o1.order() < o2.order()) {
        return -1;
      }
      if (o1.order() > o2.order()) {
        return 1;
      }
      return 0;
    });
  }

  /**
   * 按顺序执行filter.
   *
   * @param task    ApiContext的异步任务
   * @param filters filter的列表
   * @return Task ApiContext的异步任务
   */
  public static Task<ApiContext> doFilter(Task<ApiContext> task, List<Filter> filters) {
    return doFilter(task, filters, null);
  }

  /**
   * 按顺序执行filter.
   *
   * @param task     ApiContext的异步任务
   * @param filters  filter的列表
   * @param consumer 在每个filter处理完之后的额外操作
   * @return Task ApiContext的异步任务
   */
  public static Task<ApiContext> doFilter(Task<ApiContext> task, List<Filter> filters,
                                          Consumer<ApiContext> consumer) {
    Map<String, Boolean> invoked = new ConcurrentHashMap<>();
    //andThen中使用shouldFilter判断是否输出invoked日志会出现判断错误，所有使用一个bool来判断，需要在flatMap中动态修改
    for (Filter filter : filters) {
      String filterName = filter.getClass().getSimpleName();
      task = task.flatMap(filterName, apiContext -> {
        if (filter.shouldFilter(apiContext)) {
          invoked.put(filterName, true);
          Filter.LOGGER.debug("[{}] [filterStart] [{}]", apiContext.id(),
                              filter.getClass().getSimpleName());
          apiContext.addVariable("filterStarted", System.currentTimeMillis());
          Future<ApiContext> completeFuture = Future.future();
          filter.doFilter(apiContext.copy(), completeFuture);
          return completeFuture;
        } else {
          return Future.succeededFuture(apiContext);
        }
      }).andThen(apiContext -> {
        if (invoked.containsKey(filterName)) {
          long filterStarted = System.currentTimeMillis();
          try {
            filterStarted = (long) apiContext.variables()
                    .getOrDefault("filterStarted", System.currentTimeMillis());
            if (consumer != null) {
              consumer.accept(apiContext);
            }
          } catch (Exception e) {
            //ignore
          }
          Filter.LOGGER.debug("[{}] [filterEnd] [{}] [{}ms]", apiContext.id(),
                              filter.getClass().getSimpleName(),
                              System.currentTimeMillis() - filterStarted);
        }
      });
    }
    return task;
  }
}