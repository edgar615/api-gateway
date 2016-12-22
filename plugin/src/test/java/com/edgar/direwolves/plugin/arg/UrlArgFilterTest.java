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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
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
 * Created by edgar on 16-10-28.
 */
@RunWith(VertxUnitRunner.class)
public class UrlArgFilterTest extends FilterTest {

  private final List<Filter> filters = new ArrayList<>();
  UrlArgFilter filter;
  private Vertx vertx;
  private ApiContext apiContext;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");

    apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    HttpEndpoint httpEndpoint =
        Endpoint.createHttp("get_device", HttpMethod.GET, "devices/", "device");

    ApiDefinition definition = ApiDefinition.create("get_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    filter = new UrlArgFilter();

    filters.clear();
    filters.add(filter);
  }

  @Test
  public void testSuccess(TestContext testContext) {
    UrlArgPlugin plugin = (UrlArgPlugin) ApiPlugin.create(UrlArgPlugin.class.getSimpleName());
    Parameter parameter = Parameter.create("limit", 10)
        .addRule(Rule.integer())
        .addRule(Rule.max(100))
        .addRule(Rule.min(1));
    plugin.add(parameter);
    parameter = Parameter.create("start", 0)
        .addRule(Rule.integer());
    plugin.add(parameter);
    apiContext.apiDefinition().addPlugin(plugin);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> {
          testContext.assertTrue(context.params().containsKey("limit"));
          testContext.assertEquals("10", Iterables.get(context.params().get("limit"), 0));
          testContext.assertEquals("0", Iterables.get(context.params().get("start"), 0));
          testContext.assertEquals(1, context.actions().size());
          System.out.println(context.actions());
          async.complete();
        }).onFailure(t -> testContext.fail());
  }

  @Test
  public void testException(TestContext testContext) {
    UrlArgPlugin plugin = (UrlArgPlugin) ApiPlugin.create(UrlArgPlugin.class.getSimpleName());
    Parameter parameter = Parameter.create("appKey", null)
        .addRule(Rule.required());
    plugin.add(parameter);
    apiContext.apiDefinition().addPlugin(plugin);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> async.complete())
        .onFailure(t -> {
          testContext.assertTrue(t instanceof ValidationException);
          async.complete();
        });
  }
}
