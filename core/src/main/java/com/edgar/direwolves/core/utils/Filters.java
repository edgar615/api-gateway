package com.edgar.direwolves.core.utils;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Filter的工具类.
 *
 * @author Edgar  Date 2016/9/20
 */
public class Filters {
  private static final Logger LOGGER = LoggerFactory.getLogger(Filters.class);

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
    for (Filter filter : filters) {
      task = task.flatMap(filter.getClass().getSimpleName(), apiContext -> {
        if (filter.shouldFilter(apiContext)) {
          apiContext.addVariable("filterStarted", System.currentTimeMillis());
          Future<ApiContext> completeFuture = Future.future();
          filter.doFilter(apiContext.copy(), completeFuture);
          return completeFuture;
        } else {
          return Future.succeededFuture(apiContext);
        }
      }).andThen(apiContext -> {
        if (filter.shouldFilter(apiContext)) {
          long filterStarted = System.currentTimeMillis();
          try {
            filterStarted = (long) apiContext.variables()
                    .getOrDefault("filterStarted", System.currentTimeMillis());
          } catch (Exception e) {
            //ignore
          }

          Log.create(LOGGER)
                  .setTraceId(apiContext.id())
                  .setEvent(filter.getClass().getSimpleName())
                  .setMessage("[{}ms]")
                  .addArg(System.currentTimeMillis() - filterStarted)
                  .info();
        }
      });
    }
    return task;
  }
}