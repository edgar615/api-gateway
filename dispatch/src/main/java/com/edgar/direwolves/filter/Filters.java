package com.edgar.direwolves.filter;

import com.edgar.direwolves.dispatch.ApiContext;
import com.edgar.util.vertx.task.Task;
import com.google.common.collect.Lists;
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

    private List<Filter> filters = new ArrayList<>();

    public void init(Vertx vertx) {
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
            task = task.flatMap(filter.getClass().getSimpleName(), (ApiContext context) -> {
                Future<ApiContext> completeFuture = Future.future();
                filter.doFilter(context, completeFuture);
                return completeFuture;
            });
        }
        return task;
    }
}
