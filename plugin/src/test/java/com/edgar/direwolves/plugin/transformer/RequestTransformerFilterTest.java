package com.edgar.direwolves.plugin.transformer;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.plugin.FilterTest;
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
public class RequestTransformerFilterTest extends FilterTest {

  private final List<Filter> filters = new ArrayList<>();
  RequestTransformerFilter filter;
  private ApiContext apiContext;

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    filter = new RequestTransformerFilter();
    filter.config(vertx, new JsonObject());

    filters.clear();
    filters.add(filter);
  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close(testContext.asyncAssertSuccess());
  }

  @Test
  public void testEndpointToRequest(TestContext testContext) {

    RequestTransformer transformer = createRequestTransformer();

    RequestTransformerPlugin plugin = (RequestTransformerPlugin) ApiPlugin.create(RequestTransformerPlugin.class.getSimpleName());
    plugin.addTransformer(transformer);
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.createHttp("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    apiContext.addRequest(new JsonObject()
        .put("name", "add_device")
        .put("host", "localhost")
        .put("port", 8080)
        .put("method", "post")
        .put("params", new JsonObject().put("q3", "v3"))
        .put("headers", new JsonObject().put("h3", "v3")));

    apiContext.apiDefinition().addPlugin(plugin);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> {
          testContext.assertEquals(1, context.requests().size());
          JsonObject request = context.requests().getJsonObject(0);
          testContext.assertEquals("localhost", request.getString("host"));
          testContext.assertEquals(8080, request.getInteger("port"));
          testContext.assertEquals(4, request.getJsonObject("params").size());
          testContext.assertEquals(4, request.getJsonObject("headers").size());
          testContext.assertFalse(request.getJsonObject("params").containsKey("q3"));
          testContext.assertFalse(request.getJsonObject("headers").containsKey("h3"));
          testContext.assertNull(request.getJsonObject("body"));
          System.out.println(request.encodePrettily());
          async.complete();
        }).onFailure(t -> testContext.fail());
  }

  @Test
  public void testEndpointToRequest2(TestContext testContext) {

    RequestTransformer transformer = createRequestTransformer();

    RequestTransformerPlugin plugin = (RequestTransformerPlugin) ApiPlugin.create(RequestTransformerPlugin.class.getSimpleName());
    plugin.addTransformer(transformer);
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.createHttp("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    apiContext.addRequest(new JsonObject()
        .put("name", "add_device")
        .put("host", "localhost")
        .put("port", 8080)
        .put("method", "post")
        .put("params", new JsonObject().put("q3", "v3"))
        .put("headers", new JsonObject().put("h3", "v3")));
    apiContext.addRequest(new JsonObject()
        .put("name", "update_device")
        .put("host", "localhost")
        .put("port", 8080)
        .put("method", "post")
        .put("params", new JsonObject().put("q3", "v3"))
        .put("headers", new JsonObject().put("h3", "v3")));

    apiContext.apiDefinition().addPlugin(plugin);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> {
          testContext.assertEquals(2, context.requests().size());
          JsonObject request = context.requests().getJsonObject(0);
          testContext.assertEquals("localhost", request.getString("host"));
          testContext.assertEquals(8080, request.getInteger("port"));
          testContext.assertEquals(4, request.getJsonObject("params").size());
          testContext.assertEquals(4, request.getJsonObject("headers").size());
          testContext.assertFalse(request.getJsonObject("params").containsKey("q3"));
          testContext.assertFalse(request.getJsonObject("headers").containsKey("h3"));
          testContext.assertNull(request.getJsonObject("body"));

          request = context.requests().getJsonObject(1);
          testContext.assertEquals("localhost", request.getString("host"));
          testContext.assertEquals(8080, request.getInteger("port"));
          testContext.assertEquals(1, request.getJsonObject("params").size());
          testContext.assertEquals(1, request.getJsonObject("headers").size());
          testContext.assertTrue(request.getJsonObject("params").containsKey("q3"));
          testContext.assertTrue(request.getJsonObject("headers").containsKey("h3"));
          testContext.assertNull(request.getJsonObject("body"));
          async.complete();
        }).onFailure(t -> testContext.fail());
  }

  @Test
  public void testEndpointToRequestBody(TestContext testContext) {

    RequestTransformer transformer = createRequestTransformer();

    RequestTransformerPlugin plugin = (RequestTransformerPlugin) ApiPlugin.create(RequestTransformerPlugin.class.getSimpleName());
    plugin.addTransformer(transformer);
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.createHttp("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    apiContext.addRequest(new JsonObject()
        .put("name", "add_device")
        .put("host", "localhost")
        .put("port", 8080)
        .put("method", "post")
        .put("body", new JsonObject())
        .put("params", new JsonObject().put("q3", "v3"))
        .put("headers", new JsonObject().put("h3", "v3")));

    apiContext.apiDefinition().addPlugin(plugin);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    doFilter(task, filters)
        .andThen(context -> {
          testContext.assertEquals(1, context.requests().size());
          JsonObject request = context.requests().getJsonObject(0);
          testContext.assertEquals("localhost", request.getString("host"));
          testContext.assertEquals(8080, request.getInteger("port"));
          testContext.assertEquals(4, request.getJsonObject("params").size());
          testContext.assertEquals(4, request.getJsonObject("headers").size());
          testContext.assertFalse(request.getJsonObject("params").containsKey("q3"));
          testContext.assertFalse(request.getJsonObject("headers").containsKey("h3"));
          testContext.assertEquals(4, request.getJsonObject("body").size());
          System.out.println(request.encodePrettily());
          async.complete();
        }).onFailure(t -> testContext.fail());
  }

  private RequestTransformer createRequestTransformer() {
    RequestTransformer transformer = RequestTransformer.create("add_device");
    transformer.removeHeader("h3");
    transformer.removeHeader("h4");
    transformer.removeParam("q3");
    transformer.removeParam("q4");
    transformer.removeBody("p3");
    transformer.removeBody("p4");
    transformer.replaceHeader("h5", "v2");
    transformer.replaceHeader("h6", "v1");
    transformer.replaceParam("q5", "v2");
    transformer.replaceParam("q6", "v1");
    transformer.replaceBody("p5", "v2");
    transformer.replaceBody("p6", "v1");
    transformer.addHeader("h2", "v1");
    transformer.addHeader("h1", "v2");
    transformer.addParam("q1", "v2");
    transformer.addParam("q2", "v1");
    transformer.addBody("q1", "v2");
    transformer.addBody("q2", "v1");
    return transformer;
  }

}
