package com.edgar.direwolves.plugin.authorization;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.Filters;
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

/**
 * Created by edgar on 16-12-25.
 */
@RunWith(VertxUnitRunner.class)
public class AuthoriseFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  private Filter filter;

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    filter = Filter.create(AuthoriseFilter.class.getSimpleName(), vertx, new JsonObject());
    filters.clear();
    filters.add(filter);

  }

  @Test
  public void missAppShouldPass(TestContext testContext) {
    ApiContext apiContext = createContext();
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> async.complete())
            .onFailure(t -> {
              testContext.fail();
            });
  }

  @Test
  public void invalidAppShouldThrowNoAuthority(TestContext testContext) {
    ApiContext apiContext = createContext();
    apiContext.addVariable("app.permissions", "user.write, device.wirte");
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> testContext.fail())
            .onFailure(t -> {
              testContext.assertTrue(t instanceof SystemException);
              SystemException ex = (SystemException) t;
              testContext.assertEquals(DefaultErrorCode.NO_AUTHORITY, ex.getErrorCode());
              async.complete();
            });
  }

  @Test
  public void validAppShouldPass(TestContext testContext) {
    ApiContext apiContext = createContext();
    apiContext.addVariable("app.permissions", "user.read, device.wirte");
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> async.complete())
            .onFailure(t -> {
              testContext.fail();
            });
  }

  @Test
  public void missUserShouldPass(TestContext testContext) {
    ApiContext apiContext = createContext();
    apiContext.addVariable("app.permissions", "user.read, device.wirte");
    apiContext.setPrincipal(new JsonObject());
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> async.complete())
            .onFailure(t -> {
              testContext.fail();
            });
  }

  @Test
  public void invalidUserShouldThrowNoAuthority(TestContext testContext) {
    ApiContext apiContext = createContext();
    apiContext.addVariable("app.permissions", "user.read, device.wirte");
    apiContext.setPrincipal(new JsonObject().put("permissions", "user.write, device.read"));
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> testContext.fail())
            .onFailure(t -> {
              testContext.assertTrue(t instanceof SystemException);
              SystemException ex = (SystemException) t;
              testContext.assertEquals(DefaultErrorCode.NO_AUTHORITY, ex.getErrorCode());
              async.complete();
            });
  }

  @Test
  public void validUserShouldPass(TestContext testContext) {
    ApiContext apiContext = createContext();
    apiContext.addVariable("app.permissions", "user.read, device.wirte");
    apiContext.setPrincipal(new JsonObject().put("permissions", "user.read, device.read"));
    apiContext.setPrincipal(new JsonObject());
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> async.complete())
            .onFailure(t -> {
              testContext.fail();
            });
  }

  private ApiContext createContext() {
    Multimap<String, String> params = ArrayListMultimap.create();

    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", null, params, null);

    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
            Endpoint.createHttp("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition
            .create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    AuthorisePluginImpl plugin =
            (AuthorisePluginImpl) ApiPlugin.create(AuthorisePlugin.class.getSimpleName());
    plugin.setScope("user.read");
    definition.addPlugin(plugin);
    return apiContext;
  }
}
