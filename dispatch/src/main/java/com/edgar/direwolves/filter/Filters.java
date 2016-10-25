package com.edgar.direwolves.filter;

import com.google.common.collect.Lists;

import com.edgar.direwolves.dispatch.ApiContext;
import com.edgar.direwolves.dispatch.filter.Filter;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
public class Filters {

  private static final Filters INSTANCE = new Filters();

  private final List<Filter> filters = new ArrayList<>();

  private Filters() {
  }

  public static Filters instance() {
    return INSTANCE;
  }

  public void init(Vertx vertx) {
    filters.clear();
    List<Filter> filterList = Lists.newArrayList(ServiceLoader.load(Filter.class));
    filterList.forEach(filter -> {
      filter.config(vertx, new JsonObject());
      filters.add(filter);
    });
  }

  public Task<ApiContext> doFilter(ApiContext apiContext) {
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    for (Filter filter : filters) {
      task = task.flatMap(filter.getClass().getSimpleName(), context -> {
        Future<ApiContext> completeFuture = Future.future();
        filter.doFilter(context.copy(), completeFuture);
        return completeFuture;
      });
    }
    return task;
  }
}
