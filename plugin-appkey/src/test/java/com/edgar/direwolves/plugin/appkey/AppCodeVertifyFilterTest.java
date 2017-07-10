package com.edgar.direwolves.plugin.appkey;

import com.google.common.collect.Lists;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.util.base.Randoms;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Edgar on 2017/5/27.
 *
 * @author Edgar  Date 2017/5/27
 */
@RunWith(VertxUnitRunner.class)
public class AppCodeVertifyFilterTest {
  private final List<Filter> filters = new ArrayList<>();

  private int appCode = Integer.parseInt(Randoms.randomNumber(3));

  private String codeKey = UUID.randomUUID().toString();

  private Vertx vertx;

  private Filter filter;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    filter = Filter.create(AppCodeVertifyFilter.class.getSimpleName(), vertx, new JsonObject()
            .put("app.codeKey", codeKey));
    filters.clear();
    filters.add(filter);
  }

  @Test
  public void invalidUserShouldThrowInvalidReq(TestContext testContext) {
    ApiContext apiContext = createContext();

    apiContext.setPrincipal(new JsonObject()
                                    .put(codeKey, Integer.parseInt(Randoms.randomNumber(4))));

    Async async = testContext.async();
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Filters.doFilter(task, filters )
            .andThen(context -> testContext.fail())
            .onFailure(t -> {
              testContext.assertTrue(t instanceof SystemException);
              SystemException ex = (SystemException) t;
              testContext.assertEquals(ex.getErrorCode(), DefaultErrorCode.INVALID_REQ);
              async.complete();
            });
  }

  @Test
  public void validUserShouldSuccess(TestContext testContext) {
    ApiContext apiContext = createContext();

    apiContext.setPrincipal(new JsonObject()
                                    .put(codeKey, appCode));

    Async async = testContext.async();
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Filters.doFilter(task, filters )
            .andThen(context ->  async.complete())
            .onFailure(t -> {
              testContext.fail();

            });
  }

  public ApiContext createContext() {
    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, null, null);
    apiContext.addVariable("app.code", appCode);
    HttpEndpoint httpEndpoint =
            HttpEndpoint.http("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition
            .create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    definition.addPlugin(ApiPlugin.create(AppCodeVertifyPlugin.class.getSimpleName()));
    return apiContext;
  }
}
