package com.github.edgar615.gateway.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.utils.Filters;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.validation.ValidationException;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.util.List;

/**
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
@RunWith(VertxUnitRunner.class)
public class TImeoutFilterTest {

    Vertx vertx;

    @Before
    public void setUp(TestContext testContext) {
        vertx = Vertx.vertx();
    }


    @Test
    public void missTimestampShouldThrowValidationException(TestContext testContext) {

        ApiContext apiContext = ApiContext
                .create(HttpMethod.GET, "/devices", ArrayListMultimap.create(),
                        ArrayListMultimap.create(), null);

        Filter filter = Filter.create(TimeoutFilter.class.getSimpleName(), vertx, new JsonObject());

        List<Filter> filters = Lists.newArrayList(filter);

        Task<ApiContext> task = Task.create();
        task.complete(apiContext);

        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    testContext.fail();
                    ;
                }).onFailure(e -> {
            testContext.assertTrue(e instanceof ValidationException);
            async.complete();
        });

    }

    @Test
    public void laterTimestampShouldThrowExpire(TestContext testContext) {

        Multimap<String, String> params = ArrayListMultimap.create();
        params.put("timestamp", Instant.now().getEpochSecond() + (5 * 60 + 5) + "");

        ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, params, null);

        Filter filter = Filter.create(TimeoutFilter.class.getSimpleName(), vertx, new JsonObject());
        List<Filter> filters = Lists.newArrayList(filter);
        Task<ApiContext> task = Task.create();
        task.complete(apiContext);

        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    testContext.fail();
                    ;
                }).onFailure(e -> {
            testContext.assertTrue(e instanceof SystemException);
            SystemException ex = (SystemException) e;
            testContext
                    .assertEquals(DefaultErrorCode.EXPIRE.getNumber(),
                                  ex.getErrorCode().getNumber());

            async.complete();
        });

    }

    @Test
    public void beforeTimestampShouldThrowExpire(TestContext testContext) {

        Multimap<String, String> params = ArrayListMultimap.create();
        params.put("timestamp", Instant.now().getEpochSecond() - (5 * 60 + 5) + "");

        ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, params, null);

        Filter filter = Filter.create(TimeoutFilter.class.getSimpleName(), vertx, new JsonObject());

        List<Filter> filters = Lists.newArrayList(filter);
        Task<ApiContext> task = Task.create();
        task.complete(apiContext);

        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    testContext.fail();
                    ;
                }).onFailure(e -> {
            testContext.assertTrue(e instanceof SystemException);
            SystemException ex = (SystemException) e;
            testContext
                    .assertEquals(DefaultErrorCode.EXPIRE.getNumber(),
                                  ex.getErrorCode().getNumber());

            async.complete();
        });

    }

    @Test
    public void testTimeoutOK(TestContext testContext) {

        Multimap<String, String> params = ArrayListMultimap.create();
        params.put("timestamp", Instant.now().getEpochSecond() + "");

        ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, params, null);

        Filter filter = Filter.create(TimeoutFilter.class.getSimpleName(), vertx, new JsonObject());

        List<Filter> filters = Lists.newArrayList(filter);
        Task<ApiContext> task = Task.create();
        task.complete(apiContext);

        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    testContext.assertTrue(context.params().containsKey("timestamp"));
                    async.complete();
                    ;
                }).onFailure(e -> {
            testContext.fail();
        });

    }

    @Test
    public void testConfigExpire(TestContext testContext) {

        Multimap<String, String> params = ArrayListMultimap.create();
        params.put("timestamp", Instant.now().getEpochSecond() + (9 * 60) + "");

        ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, params, null);

        Filter filter = Filter.create(TimeoutFilter.class.getSimpleName(), vertx,
                                      new JsonObject()
                                              .put("timeout", new JsonObject().put("expires", 600))
        );

        List<Filter> filters = Lists.newArrayList(filter);
        Task<ApiContext> task = Task.create();
        task.complete(apiContext);

        Async async = testContext.async();
        Filters.doFilter(task, filters)
                .andThen(context -> {
                    testContext.assertTrue(context.params().containsKey("timestamp"));
                    async.complete();
                    ;
                }).onFailure(e -> {
            testContext.fail();
        });

    }
}
