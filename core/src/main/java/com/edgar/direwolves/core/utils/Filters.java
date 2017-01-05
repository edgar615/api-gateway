package com.edgar.direwolves.core.utils;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Future;

import java.util.Collections;
import java.util.List;

/**
 * Filter的工具类.
 *
 * @author Edgar  Date 2016/9/20
 */
public class Filters {

  /**
   * 将filter按照order()生序排列.
   *
   * @param filters filter的列表
   */
  public static void sort(List<Filter> filters) {
    Collections.sort(filters, (Filter o1, Filter o2) -> {
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
    for (Filter filter : filters) {
      task = task.flatMap(filter.getClass().getSimpleName(), apiContext -> {
        if (filter.shouldFilter(apiContext)) {
          Future<ApiContext> completeFuture = Future.future();
          filter.doFilter(apiContext.copy(), completeFuture);
          return completeFuture;
        } else {
          return Future.succeededFuture(apiContext);
        }
      }).andThen(apiContext -> apiContext.addAction(filter.getClass().getSimpleName(), apiContext));
    }
    return task;
  }
}