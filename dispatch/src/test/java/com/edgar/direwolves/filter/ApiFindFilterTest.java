package com.edgar.direwolves.filter;

import com.google.common.collect.Lists;

import com.edgar.direwolves.ApiUtils;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiProvider;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.direwolves.definition.ApiProviderImpl;
import com.edgar.direwolves.dispatch.verticle.ApiDispatchVerticle;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import com.edgar.direwolves.verticle.ApiDefinitionVerticle;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Edgar on 2017/1/9.
 *
 * @author Edgar  Date 2017/1/9
 */
@RunWith(VertxUnitRunner.class)
public class ApiFindFilterTest {

  private Vertx vertx;

  private JsonObject config = new JsonObject();

  private String address = "direwolves.api";

  @Before
  public void setUp(TestContext testContext) {
    vertx = Vertx.vertx();

    config.clear()
            .put("api.provider.address", address);
    ProxyHelper.registerService(ApiProvider.class, vertx, new ApiProviderImpl(), address);

    ApiUtils.registerApi();
  }

  @Test
  public void testFoundApi(TestContext testContext) {
    ApiContext apiContext =
            ApiContext.create(HttpMethod.POST, "/devices", null, null, null);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filter filter = Filter.create(ApiFindFilter.class.getSimpleName(), vertx, config);
    Filters.doFilter(task, Lists.newArrayList(filter))
            .andThen(context -> {
              testContext.assertNotNull(context.apiDefinition());
              testContext.assertEquals("add_device",context.apiDefinition().name());
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
      testContext.assertTrue(throwable instanceof ReplyException);
      async.complete();
    });
  }
}
