package com.edgar.direwolves.filter;

import com.google.common.collect.Lists;

import com.codahale.metrics.MetricRegistry;
import com.edgar.direwolves.ApiUtils;
import com.edgar.direwolves.core.definition.ApiDiscovery;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.direwolves.metric.ApiMetrics;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

/**
 * Created by Edgar on 2017/1/9.
 *
 * @author Edgar  Date 2017/1/9
 */
@RunWith(VertxUnitRunner.class)
public class ApiFindFilterTest {

  ApiDiscovery apiDiscovery;

  private Vertx vertx;

  private String namespace = UUID.randomUUID().toString();

  private JsonObject config = new JsonObject().put("namespace", namespace);

  @Before
  public void setUp(TestContext testContext) {
    vertx = Vertx.vertx();
    apiDiscovery = ApiDiscovery.create(vertx, namespace);
    ApiUtils.registerApi(apiDiscovery);
  }

  @Test
  public void testFoundApi(TestContext testContext) {
    ApiMetrics apiMetrics = ApiMetrics.create(new MetricRegistry(), "test", 100);
    ApiContext apiContext =
            ApiContext.create(HttpMethod.POST, "/devices", null, null, null);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filter filter = Filter.create(ApiFindFilter.class.getSimpleName(), vertx, config);
    Filters.doFilter(task, Lists.newArrayList(filter))
            .andThen(context -> {
              testContext.assertNotNull(context.apiDefinition());
              testContext.assertEquals("add_device", context.apiDefinition().name());
              async.complete();
            }).onFailure(throwable -> {
      throwable.printStackTrace();
      testContext.fail();
    });
  }

  @Test
  public void testNotFoundApi(TestContext testContext) {
    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/users", null, null, null);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filter filter = Filter.create(ApiFindFilter.class.getSimpleName(), vertx, config);
    Filters.doFilter(task, Lists.newArrayList(filter))
            .andThen(context -> {
              testContext.fail();
            }).onFailure(throwable -> {
      throwable.printStackTrace();
      testContext.assertTrue(throwable instanceof SystemException);
      SystemException se = (SystemException) throwable;
      testContext.assertEquals(DefaultErrorCode.RESOURCE_NOT_FOUND, se.getErrorCode());
      async.complete();
    });
  }
}
