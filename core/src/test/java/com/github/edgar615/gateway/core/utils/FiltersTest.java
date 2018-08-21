package com.github.edgar615.gateway.core.utils;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Edgar on 2018/4/3.
 *
 * @author Edgar  Date 2018/4/3
 */
@RunWith(VertxUnitRunner.class)
public class FiltersTest {
    private Vertx vertx;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
    }

    @Test
    public void testFilter(TestContext testContext) {
        ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/aaaa", null, null, null);
        Task<ApiContext> task = Task.create();
        task.complete(apiContext);

        List<Filter> filters = new ArrayList<>();
        filters.add(new ApiDefinitionFilter());
        Async async = testContext.async();
        Filters.doFilter(task, filters, apiContext2 -> {})
                .andThen(apiContext1 -> {
                    System.out.println(apiContext1);
                    async.complete();
                });
    }
}
