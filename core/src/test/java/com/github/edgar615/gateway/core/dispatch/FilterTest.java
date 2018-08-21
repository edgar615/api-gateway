package com.github.edgar615.gateway.core.dispatch;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
@RunWith(VertxUnitRunner.class)
public class FilterTest {

    Vertx vertx;

    @Before
    public void setUp(TestContext testContext) {
        vertx = Vertx.vertx();
    }


    @Test
    public void testCreateDefinedFilter(TestContext testContext) {

        Filter filter = Filter.create(MockFilter.class.getSimpleName(), vertx, new JsonObject());
        Assert.assertTrue(filter instanceof MockFilter);
    }

    @Test
    public void testUndefinedFilterShouldThrowNoSuchElementException(TestContext testContext) {

        try {
            Filter.create(UUID.randomUUID().toString(), vertx, new JsonObject());
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof NoSuchElementException);
        }
    }

    @Test
    public void testException(TestContext testContext) {

        ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, null, null);

        MockFilter filter = new MockFilter();

        Future<ApiContext> future = Future.future();
        try {
            filter.doFilter(apiContext, future);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof NullPointerException);
        }
    }

    @Test
    public void testSuccess(TestContext testContext) {

        ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, null, null);
        apiContext.variables().put("test", true);

        MockFilter filter = new MockFilter();

        Future<ApiContext> future = Future.future();
        filter.doFilter(apiContext, future);

        Async async = testContext.async();
        future.setHandler(ar -> {
            if (ar.succeeded()) {
                async.complete();
            } else {
                testContext.fail();
            }
        });
    }

    @Test
    public void testFailed(TestContext testContext) {

        ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, null, null);
        apiContext.variables().put("test", false);

        MockFilter filter = new MockFilter();

        Future<ApiContext> future = Future.future();
        filter.doFilter(apiContext, future);

        Async async = testContext.async();
        future.setHandler(ar -> {
            if (ar.succeeded()) {
                testContext.fail();
            } else {
                async.complete();
            }
        });
    }


}
