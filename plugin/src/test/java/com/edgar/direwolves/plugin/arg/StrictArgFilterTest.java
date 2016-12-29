package com.edgar.direwolves.plugin.arg;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.util.validation.Rule;
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
import java.util.UUID;

/**
 * Created by edgar on 16-10-28.
 */
@RunWith(VertxUnitRunner.class)
public class StrictArgFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  Filter filter;

  private ApiContext apiContext;

  @Before
  public void setUp() {
    filters.clear();

  }

  @Test
  public void unDefinedUrlArgPluginShouldThrowValidationException(TestContext testContext) {
    createApiContext();
    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject());
    filters.add(filter);
    apiContext.apiDefinition().addPlugin(ApiPlugin.create(StrictArgPlugin.class.getSimpleName()));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();
            }).onFailure(t -> {
      testContext.assertTrue(t instanceof ValidationException);
      ValidationException e = (ValidationException) t;
      testContext.assertEquals("prohibited", e.getErrorDetail().get("appKey").iterator().next());
      testContext.assertEquals("prohibited", e.getErrorDetail().get("sign").iterator().next());
      async.complete();
    });
  }

  @Test
  public void unDefinedUrlArgPluginButExcludeArgShouldSuccess(TestContext
                                                                      testContext) {
    createApiContext();
    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject()
            .put("strict_arg.query.excludes", new JsonArray().add("appKey").add("sign")));
    filters.add(filter);
    apiContext.apiDefinition().addPlugin(ApiPlugin.create(StrictArgPlugin.class.getSimpleName()));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              async.complete();
            }).onFailure(t -> {
      testContext.fail();
    });
  }

  @Test
  public void unDefinedUrlArgPluginButNotExcludeArgShouldSuccess(TestContext
                                                                         testContext) {
    createApiContext();
    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject()
            .put("strict_arg.query.excludes", new JsonArray().add("appKey")));
    filters.add(filter);
    apiContext.apiDefinition().addPlugin(ApiPlugin.create(StrictArgPlugin.class.getSimpleName()));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();
            }).onFailure(t -> {
      testContext.assertTrue(t instanceof ValidationException);
      ValidationException e = (ValidationException) t;
      testContext.assertFalse(e.getErrorDetail().containsKey("appKey"));
      testContext.assertEquals("prohibited", e.getErrorDetail().get("sign").iterator().next());
      async.complete();
    });
  }

  @Test
  public void definedUrlArgPluginShouldButExcludeArgShouldSuccess(TestContext testContext) {
    createApiContext();
    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject()
            .put("strict_arg.query.excludes", new JsonArray().add("appKey")));
    filters.add(filter);

    apiContext.apiDefinition().addPlugin(ApiPlugin.create(StrictArgPlugin.class.getSimpleName()));

    UrlArgPlugin plugin = (UrlArgPlugin) ApiPlugin.create(UrlArgPlugin.class.getSimpleName());
    Parameter parameter = Parameter.create("limit", 10)
            .addRule(Rule.integer())
            .addRule(Rule.max(100))
            .addRule(Rule.min(1));
    plugin.add(parameter);
    parameter = Parameter.create("q3", 0);
    plugin.add(parameter);
    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();
            }).onFailure(t -> {
      testContext.assertTrue(t instanceof ValidationException);
      ValidationException e = (ValidationException) t;
      testContext.assertEquals("prohibited", e.getErrorDetail().get("sign").iterator().next());
      testContext.assertFalse(e.getErrorDetail().containsKey("appKey"));
      testContext.assertFalse(e.getErrorDetail().containsKey("limit"));
      testContext.assertFalse(e.getErrorDetail().containsKey("q3"));
      async.complete();
    });
  }

  @Test
  public void definedUrlArgPluginShouldThrowValidationException(TestContext testContext) {
    createApiContext();
    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject());
    filters.add(filter);

    apiContext.apiDefinition().addPlugin(ApiPlugin.create(StrictArgPlugin.class.getSimpleName()));

    UrlArgPlugin plugin = (UrlArgPlugin) ApiPlugin.create(UrlArgPlugin.class.getSimpleName());
    Parameter parameter = Parameter.create("limit", 10)
            .addRule(Rule.integer())
            .addRule(Rule.max(100))
            .addRule(Rule.min(1));
    plugin.add(parameter);
    parameter = Parameter.create("sign", 0);
    plugin.add(parameter);
    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();
            }).onFailure(t -> {
      testContext.assertTrue(t instanceof ValidationException);
      ValidationException e = (ValidationException) t;
      testContext.assertEquals("prohibited", e.getErrorDetail().get("appKey").iterator().next());
      testContext.assertFalse(e.getErrorDetail().containsKey("limit"));
      testContext.assertFalse(e.getErrorDetail().containsKey("sign"));
      async.complete();
    });
  }

  @Test
  public void unDefinedBodyArgPluginShouldThrowValidationException(TestContext testContext) {
    createApiContext2();
    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject());
    filters.add(filter);
    apiContext.apiDefinition().addPlugin(ApiPlugin.create(StrictArgPlugin.class.getSimpleName()));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();
            }).onFailure(t -> {
      testContext.assertTrue(t instanceof ValidationException);
      ValidationException e = (ValidationException) t;
      testContext.assertEquals("prohibited", e.getErrorDetail().get("type").iterator().next());
      testContext.assertEquals("prohibited", e.getErrorDetail().get("name").iterator().next());
      async.complete();
    });
  }

  @Test
  public void unDefinedBodyArgPluginButExcludeArgShouldSuccess(TestContext
                                                                      testContext) {
    createApiContext2();
    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject()
            .put("strict_arg.body.excludes", new JsonArray().add("type").add("name")));
    filters.add(filter);
    apiContext.apiDefinition().addPlugin(ApiPlugin.create(StrictArgPlugin.class.getSimpleName()));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              async.complete();
            }).onFailure(t -> {
      testContext.fail();
    });
  }

  @Test
  public void unDefinedBodyArgPluginButNotExcludeArgShouldSuccess(TestContext
                                                                         testContext) {
    createApiContext2();
    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject()
            .put("strict_arg.body.excludes", new JsonArray().add("type")));
    filters.add(filter);
    apiContext.apiDefinition().addPlugin(ApiPlugin.create(StrictArgPlugin.class.getSimpleName()));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();
            }).onFailure(t -> {
      testContext.assertTrue(t instanceof ValidationException);
      ValidationException e = (ValidationException) t;
      testContext.assertFalse(e.getErrorDetail().containsKey("type"));
      testContext.assertEquals("prohibited", e.getErrorDetail().get("name").iterator().next());
      async.complete();
    });
  }



  private void createApiContext() {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("appKey", UUID.randomUUID().toString());
    params.put("sign", UUID.randomUUID().toString());
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");

    apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    HttpEndpoint httpEndpoint =
            Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");

    ApiDefinition definition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
  }

  private void createApiContext2() {
    Multimap<String, String> params = ArrayListMultimap.create();
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");

    JsonObject body = new JsonObject()
            .put("type", 1)
            .put("name", "edgar");

    apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, body);
    HttpEndpoint httpEndpoint =
            Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");

    ApiDefinition definition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
  }
}
