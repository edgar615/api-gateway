package com.github.edgar615.gateway.filter;

import com.google.common.collect.Lists;

import com.github.edgar615.gateway.core.definition.ApiDefinition;
import com.github.edgar615.gateway.core.definition.Endpoint;
import com.github.edgar615.gateway.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.gateway.core.dispatch.ApiContext;
import com.github.edgar615.gateway.core.dispatch.Filter;
import com.github.edgar615.gateway.core.utils.Filters;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
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

import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2017/1/9.
 *
 * @author Edgar  Date 2017/1/9
 */
@RunWith(VertxUnitRunner.class)
public class ApiCheckFilterTest {

  private Vertx vertx;

  @Before
  public void setUp(TestContext testContext) {
    vertx = Vertx.vertx();
  }

  @Test
  public void testFoundApi(TestContext testContext) {
    ApiContext apiContext =
            ApiContext.create(HttpMethod.POST, "/devices", null, null, null);
    Endpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/", 80, "localhost");

    ApiDefinition definition = ApiDefinition
            .create("add_device", HttpMethod.GET, "devices", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);


    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filter filter = Filter.create(ApiCheckFilter.class.getSimpleName(), vertx, new JsonObject());
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
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
    Filter filter = Filter.create(ApiCheckFilter.class.getSimpleName(), vertx, new JsonObject());
    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
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
