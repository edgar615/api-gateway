package com.github.edgar615.direwolves.filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.definition.Endpoint;
import com.github.edgar615.direwolves.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.Filters;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Edgar on 2016/11/18.
 *
 * @author Edgar  Date 2016/11/18
 */
@RunWith(VertxUnitRunner.class)
public class PathParamFilterTest {
  private final List<Filter> filters = new ArrayList<>();

  private Vertx vertx;

  private Filter filter;

  private ApiContext apiContext;

  @Before
  public void testSetUp(TestContext testContext) {
    vertx = Vertx.vertx();

    JsonObject config = new JsonObject();

    filter = Filter.create(PathParamFilter.class.getSimpleName(), vertx, config);

    filters.clear();
    filters.add(filter);

  }

  @After
  public void tearDown(TestContext testContext) {
//    vertx.close(testContext.asyncAssertSuccess());
  }

  @Test
  public void testVariable(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");

    Multimap<String, String> ebHeaders = ArrayListMultimap.create();
    ebHeaders.put("action", "get");

    apiContext =
        ApiContext.create(HttpMethod.GET, "/devices/123", headers, params, null);
    Endpoint httpEndpoint =
            SimpleHttpEndpoint.http("get_device", HttpMethod.GET, "devices/", 80, "localhost");
    ApiDefinition definition = ApiDefinition
        .create("get_device", HttpMethod.GET, "/devices/([\\d+]+)", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);


    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
        .andThen(context -> {
          testContext.assertEquals("123", context.variables().get("param0"));
          async.complete();
        }).onFailure(t -> testContext.fail()
    );
  }

}
