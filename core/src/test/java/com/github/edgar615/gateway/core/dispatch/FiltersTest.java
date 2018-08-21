package com.github.edgar615.gateway.core.dispatch;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;

import com.github.edgar615.gateway.core.utils.Filters;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Created by edgar on 17-1-4.
 */
@RunWith(VertxUnitRunner.class)
public class FiltersTest {

    @Test
    public void testSort() {
        Filter filter1 = new MockFilter1();
        Filter filter2 = new MockFilter2();
        Filter filter3 = new MockFilter3();
        Filter filter4 = new MockFilter4();
        List<Filter> filterList = Lists.newArrayList(filter1, filter2, filter3, filter4);
        Filters.sort(filterList);
        Assert.assertSame(filter2, filterList.get(0));
        Assert.assertSame(filter3, filterList.get(1));
        Assert.assertSame(filter4, filterList.get(2));
        Assert.assertSame(filter1, filterList.get(3));
    }

    @Test
    public void testDoFilterFailed(TestContext testContext) {
        Filter filter1 = new MockFilter1();
        Filter filter2 = new MockFilter2();
        Filter filter3 = new MockFilter3();
        Filter filter4 = new MockFilter4();
        List<Filter> filters = Lists.newArrayList(filter1, filter2, filter3, filter4);
        Filters.sort(filters);

        ApiContext apiContext = ApiContext
                .create(HttpMethod.GET, "/devices", ArrayListMultimap.create(),
                        ArrayListMultimap.create(), null);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);

        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    testContext.fail();
                }).onFailure(e -> {
            testContext.assertTrue(e instanceof NullPointerException);
            async.complete();
        });
    }

    @Test
    public void testDoFilterSucceed(TestContext testContext) {
        Filter filter1 = new MockFilter1();
        Filter filter2 = new MockFilter2();
        Filter filter3 = new MockFilter3();
        Filter filter4 = new MockFilter4();
        List<Filter> filters = Lists.newArrayList(filter1, filter2, filter3, filter4);
        Filters.sort(filters);

        ApiContext apiContext = ApiContext
                .create(HttpMethod.GET, "/devices", ArrayListMultimap.create(),
                        ArrayListMultimap.create(), null);
        apiContext.addVariable("mock1", true);
        apiContext.addVariable("mock2", true);
        apiContext.addVariable("mock3", true);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);

        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    async.complete();
                }).onFailure(e -> {
            testContext.fail();
        });
    }
}
