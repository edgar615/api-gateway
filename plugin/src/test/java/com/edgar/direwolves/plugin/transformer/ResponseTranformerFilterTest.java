package com.edgar.direwolves.plugin.transformer;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.plugin.FilterTest;
import com.edgar.direwolves.plugin.transformer.ResponseTransformerFilter;
import com.edgar.direwolves.plugin.transformer.ResponseTransformerPlugin;
import com.edgar.util.vertx.task.Task;
import com.google.common.collect.Lists;
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
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
@RunWith(VertxUnitRunner.class)
public class ResponseTranformerFilterTest extends FilterTest {

  private final List<Filter> filters = new ArrayList<>();
  ResponseTransformerFilter filter;
  private ApiContext apiContext;

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    filter = new ResponseTransformerFilter();
    filter.config(vertx, new JsonObject());

    filters.clear();
    filters.add(filter);
  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close(testContext.asyncAssertSuccess());
  }


  @Test
  public void testResponseTransformer(TestContext testContext) {
    ResponseTransformerPlugin plugin = (ResponseTransformerPlugin) ApiPlugin.create(ResponseTransformerPlugin.class.getSimpleName());
    plugin.removeHeader("h3");
    plugin.removeHeader("h4");
    plugin.removeBody("p3");
    plugin.removeBody("p4");
    plugin.replaceHeader("h5", "v2");
    plugin.replaceHeader("h6", "v1");
    plugin.replaceBody("p5", "v2");
    plugin.replaceBody("p6", "v1");
    plugin.addHeader("h2", "v1");
    plugin.addHeader("h1", "v2");
    plugin.addBody("q1", "v2");
    plugin.addBody("q2", "v1");
    apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", null, null, new JsonObject());

    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.createHttp("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    definition.addPlugin(plugin);
    apiContext.setApiDefinition(definition);
    JsonObject response = new JsonObject()
        .put("name", "add_device")
        .put("isArray", false)
        .put("body", new JsonObject().put("foo", "bar"));
    apiContext.setResponse(response);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> {
          JsonObject jsonObject = context.response();
          testContext.assertEquals(4, jsonObject.getJsonObject("headers").size());
          testContext.assertEquals(5, jsonObject.getJsonObject("body").size());
          async.complete();
        }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });
  }
}