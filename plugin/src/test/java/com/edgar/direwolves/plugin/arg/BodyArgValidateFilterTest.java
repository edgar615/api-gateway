package com.edgar.direwolves.plugin.arg;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.plugin.FilterTest;
import com.edgar.util.validation.Rule;
import com.edgar.util.validation.ValidationException;
import com.edgar.util.vertx.task.Task;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
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
public class BodyArgValidateFilterTest extends FilterTest {

  private final List<Filter> filters = new ArrayList<>();
  BodyArgValidateFilter filter;
  private ApiContext apiContext;

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    filter = new BodyArgValidateFilter();
    filter.config(vertx, new JsonObject());

    filters.clear();
    filters.add(filter);
  }

  @Test
  public void testSuccess(TestContext testContext) {
    BodyArgPlugin plugin = (BodyArgPlugin) ApiPlugin.create(BodyArgPlugin.class.getSimpleName());
    Parameter parameter = Parameter.create("encryptKey", null)
        .addRule(Rule.required())
        .addRule(Rule.regex("[0-9A-F]{16}"));
    plugin.add(parameter);
    parameter = Parameter.create("type", 1)
        .addRule(Rule.required())
        .addRule(Rule.optional(Lists.newArrayList(1, 2, 3)));
    plugin.add(parameter);


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
    HttpEndpoint httpEndpoint =
        Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    apiContext.apiDefinition().addPlugin(plugin);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> {
          testContext.assertTrue(context.body().containsKey("type"));
          testContext.assertEquals("1", context.body().getString("type"));
          testContext.assertEquals(1, context.actions().size());
          System.out.println(context.actions());
          async.complete();
        }).onFailure(t -> testContext.fail());
  }

  @Test
  public void testException(TestContext testContext) {
    BodyArgPlugin plugin = (BodyArgPlugin) ApiPlugin.create(BodyArgPlugin.class.getSimpleName());
    Parameter parameter = Parameter.create("encryptKey", null)
        .addRule(Rule.required())
        .addRule(Rule.regex("[0-9A-F]{16}"));
    plugin.add(parameter);
    parameter = Parameter.create("type", 1)
        .addRule(Rule.required())
        .addRule(Rule.optional(Lists.newArrayList(1, 2, 3)));
    plugin.add(parameter);


    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");

    apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
    HttpEndpoint httpEndpoint =
        Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    apiContext.apiDefinition().addPlugin(plugin);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> {
          testContext.fail();
        }).onFailure(t -> {
      Assert.assertTrue(t instanceof ValidationException);
      async.complete();
    });
  }
}
