package com.github.edgar615.direwolves.plugin.arg;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.Filters;
import com.github.edgar615.util.validation.Rule;
import com.github.edgar615.util.validation.ValidationException;
import com.github.edgar615.util.vertx.task.Task;
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
  public void disabledShouldSuccess(TestContext testContext) {
    createApiContext();
    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject());
    filters.add(filter);

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
  public void unDefinedUrlArgPluginShouldFailed(TestContext testContext) {
    createApiContext();
    JsonObject jsonObject = new JsonObject()
            .put("enable", true);
    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(),
                           new JsonObject().put("strict.arg", jsonObject));
    filters.add(filter);

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

    JsonObject jsonObject = new JsonObject()
            .put("enable", true)
            .put("query.excludes", new JsonArray().add("appKey").add("sign"));

    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject()
            .put("strict.arg", jsonObject));
    filters.add(filter);

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
  public void unDefinedUrlArgPluginButNotExcludeArgShouldFailed(TestContext
                                                                        testContext) {
    createApiContext();
    JsonObject jsonObject = new JsonObject()
            .put("enable", true)
            .put("query.excludes", new JsonArray().add("appKey"));
    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject()
            .put("strict.arg", jsonObject));
    filters.add(filter);

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
  public void definedUrlArgPluginButExcludeArgShouldSuccess(TestContext testContext) {
    createApiContext();
    JsonObject jsonObject = new JsonObject()
            .put("enable", true)
            .put("query.excludes", new JsonArray().add("appKey"));
    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject()
            .put("strict.arg", jsonObject));
    filters.add(filter);


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
  public void definedUrlArgPluginShouldFailed(TestContext testContext) {
    createApiContext();
    JsonObject jsonObject = new JsonObject()
            .put("enable", true);
    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject()
            .put("strict.arg", jsonObject));
    filters.add(filter);

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
  public void definedUrlArgPluginExcludeArgShouldSuccess(TestContext testContext) {
    createApiContext();
    JsonObject jsonObject = new JsonObject()
            .put("enable", true)
            .put("query.excludes", new JsonArray().add("appKey"));
    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject()
            .put("strict.arg", jsonObject));
    filters.add(filter);

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
              async.complete();
            }).onFailure(t -> {
      testContext.fail();
    });
  }

  @Test
  public void unDefinedBodyArgPluginShouldFailed(TestContext testContext) {
    createApiContext2();
    JsonObject jsonObject = new JsonObject()
            .put("enable", true);
    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject()
            .put("strict.arg", jsonObject));
    filters.add(filter);

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
    JsonObject jsonObject = new JsonObject()
            .put("enable", true)
            .put("body.excludes", new JsonArray().add("type").add("name"));
    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject()
            .put("strict.arg", jsonObject));

    filters.add(filter);

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
  public void unDefinedBodyArgPluginButNotExcludeArgShouldFailed(TestContext
                                                                         testContext) {
    createApiContext2();
    JsonObject jsonObject = new JsonObject()
            .put("enable", true)
            .put("body.excludes", new JsonArray().add("type"));
    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject()
            .put("strict.arg", jsonObject));
    filters.add(filter);

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


  @Test
  public void definedBodyArgPluginButNotExcludeArgShouldFailed(TestContext testContext) {
    createApiContext2();
    JsonObject jsonObject = new JsonObject()
            .put("enable", true)
            .put("body.excludes", new JsonArray().add("name"));
    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject()
            .put("strict.arg", jsonObject));
    filters.add(filter);

    BodyArgPlugin plugin = (BodyArgPlugin) ApiPlugin.create(BodyArgPlugin.class.getSimpleName());
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
      testContext.assertEquals("prohibited", e.getErrorDetail().get("type").iterator().next());
      testContext.assertFalse(e.getErrorDetail().containsKey("name"));
      testContext.assertFalse(e.getErrorDetail().containsKey("limit"));
      testContext.assertFalse(e.getErrorDetail().containsKey("q3"));
      async.complete();
    });
  }

  @Test
  public void definedBodyArgPluginButExcludeArgShouldSuccess(TestContext testContext) {
    createApiContext2();
    JsonObject jsonObject = new JsonObject()
            .put("enable", true)
            .put("body.excludes", new JsonArray().add("name"));
    filter = Filter.create(StrictArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject()
            .put("strict.arg", jsonObject));
    filters.add(filter);

    BodyArgPlugin plugin = (BodyArgPlugin) ApiPlugin.create(BodyArgPlugin.class.getSimpleName());
    Parameter parameter = Parameter.create("limit", 10)
            .addRule(Rule.integer())
            .addRule(Rule.max(100))
            .addRule(Rule.min(1));
    plugin.add(parameter);
    parameter = Parameter.create("type", 0);
    plugin.add(parameter);
    apiContext.apiDefinition().addPlugin(plugin);

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


  private void createApiContext() {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("appKey", UUID.randomUUID().toString());
    params.put("sign", UUID.randomUUID().toString());
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");

    apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/",
                                    80, "localhost");

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
    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/",
                                    80, "localhost");

    ApiDefinition definition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
  }
}
