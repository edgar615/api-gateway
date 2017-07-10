package com.edgar.direvolves.plugin.authentication;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.Result;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.validation.ValidationException;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
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
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
@RunWith(VertxUnitRunner.class)
public class BackendAuthCodeFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  Filter filter;

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    filter = Filter.create(BackendAuthCodeFilter.class.getSimpleName(), vertx,
                           new JsonObject().put("backend.permitted", new JsonArray().add("987654321")));

    filters.clear();
    filters.add(filter);

  }

  @Test
  public void missUsernameShouldThrowValidationException(TestContext testContext) {
    ApiContext apiContext = createGetContext();

    JsonObject body = new JsonObject()
            .put("username", "edgar");
    apiContext.setResult(Result.createJsonObject(200, body, null));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();
            })
            .onFailure(throwable -> {
              testContext.assertTrue(throwable instanceof ValidationException);
              async.complete();
            });
  }

  @Test
  public void notAllowedUsernameShouldThrow1004(TestContext testContext) {
    ApiContext apiContext = createContext("123456");

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
             testContext.fail();
            })
            .onFailure(throwable -> {
              testContext.assertTrue(throwable instanceof SystemException);
              SystemException ex = (SystemException) throwable;
              testContext.assertEquals(DefaultErrorCode.PERMISSION_DENIED, ex.getErrorCode());
              async.complete();
            });

  }

  @Test
  public void validUsernameShouldSuccess(TestContext testContext) {
    ApiContext apiContext = createContext("987654321");

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.variables());
              testContext.assertTrue(context.variables().containsKey("backend.code"));
              testContext.assertTrue(context.variables().containsKey("backend.sign"));

              async.complete();
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.fail();
            });

  }

  private ApiContext createGetContext() {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");
    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    HttpEndpoint httpEndpoint =
            HttpEndpoint.http("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists
            .newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    BackendAuthCodePlugin plugin = (BackendAuthCodePlugin) ApiPlugin.create(BackendAuthCodePlugin
                                                                      .class
                                                                      .getSimpleName());
    apiContext.apiDefinition().addPlugin(plugin);
    return apiContext;
  }

  private ApiContext createContext(String tel) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");
    JsonObject body = new JsonObject()
            .put("username", tel);
    ApiContext apiContext =
            ApiContext.create(HttpMethod.POST, "/devices", headers, params, body);
    HttpEndpoint httpEndpoint =
            HttpEndpoint.http("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists
            .newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    BackendAuthCodePlugin plugin = (BackendAuthCodePlugin) ApiPlugin.create(BackendAuthCodePlugin
                                                                                    .class
                                                                                    .getSimpleName());
    apiContext.apiDefinition().addPlugin(plugin);
    return apiContext;
  }

}
