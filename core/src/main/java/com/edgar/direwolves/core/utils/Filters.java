package com.edgar.direwolves.core.utils;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Future;

import java.util.List;

/**
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
public class Filters {

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