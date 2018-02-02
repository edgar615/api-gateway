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
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edgar on 16-10-28.
 */
@RunWith(VertxUnitRunner.class)
public class BodyArgFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  Filter filter;

  private ApiContext apiContext;

  @Before
  public void setUp() {
    filter = Filter.create(BodyArgFilter.class.getSimpleName(), Vertx.vertx(), new JsonObject());

    filters.clear();
    filters.add(filter);

    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");

    JsonObject jsonObject = new JsonObject()
            .put("encryptKey", "AAAAAAAAAAAAAAAA")
            .put("barcode", "AAAAAAAAAAAAAAAA");

    apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, jsonObject);
    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/",
                                    80, "localhost");
    ApiDefinition definition = ApiDefinition
            .create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
  }

  @Test
  public void testMissParameterAllocateDefaultValue(TestContext testContext) {
    BodyArgPlugin plugin = (BodyArgPlugin) ApiPlugin.create(BodyArgPlugin.class.getSimpleName());
    Parameter parameter = Parameter.create("type", 1)
            .addRule(Rule.required())
            .addRule(Rule.optional(Lists.newArrayList(1, 2, 3)));
    plugin.add(parameter);
    apiContext.apiDefinition().addPlugin(plugin);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.assertTrue(context.body().containsKey("type"));
              testContext.assertEquals(1, context.body().getValue("type"));
              async.complete();
            }).onFailure(t -> testContext.fail());
  }

  @Test
  public void testExistParameterNotAllocateDefaultValue(TestContext testContext) {
    BodyArgPlugin plugin = (BodyArgPlugin) ApiPlugin.create(BodyArgPlugin.class.getSimpleName());
    Parameter parameter = Parameter.create("encryptKey", null)
            .addRule(Rule.required())
            .addRule(Rule.regex("[0-9A-F]{16}"));
    plugin.add(parameter);
    parameter = Parameter.create("barcode", 1)
            .addRule(Rule.required());
    plugin.add(parameter);
    apiContext.apiDefinition().addPlugin(plugin);
//    apiContext.addParam("type", "3");
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.assertEquals("AAAAAAAAAAAAAAAA", context.body().getString("barcode"));
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });
  }

  @Test
  public void testInvalidParameterShouldThrowValidationException(TestContext testContext) {
    BodyArgPlugin plugin = (BodyArgPlugin) ApiPlugin.create(BodyArgPlugin.class.getSimpleName());
    Parameter parameter = Parameter.create("username", null)
            .addRule(Rule.required())
            .addRule(Rule.regex("[0-9A-F]{16}"));
    plugin.add(parameter);
    parameter = Parameter.create("type", 1)
            .addRule(Rule.required())
            .addRule(Rule.optional(Lists.newArrayList(1, 2, 3)));
    plugin.add(parameter);

    apiContext.apiDefinition().addPlugin(plugin);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();
            }).onFailure(t -> {
      Assert.assertTrue(t instanceof ValidationException);
      async.complete();
    });
  }
}
