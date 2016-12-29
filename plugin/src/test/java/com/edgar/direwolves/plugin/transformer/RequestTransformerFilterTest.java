package com.edgar.direwolves.plugin.transformer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.rpc.http.HttpRpcRequest;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.util.vertx.task.Task;
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
import java.util.UUID;

/**
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
@RunWith(VertxUnitRunner.class)
public class RequestTransformerFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  RequestTransformerFilter filter;

  private ApiContext apiContext;

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    filter = new RequestTransformerFilter();

    filters.clear();
    filters.add(filter);

    createApiContext();
  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close(testContext.asyncAssertSuccess());
  }

  @Test
  public void testSingleRequestTransformer(TestContext testContext) {

    RequestTransformer transformer = createRequestTransformer();
    RequestTransformerPlugin plugin = (RequestTransformerPlugin) ApiPlugin
            .create(RequestTransformerPlugin.class.getSimpleName());
    plugin.addTransformer(transformer);
    HttpRpcRequest httpRpcRequest = HttpRpcRequest.create(UUID.randomUUID().toString(),
                                                          "add_device");
    httpRpcRequest.setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .addParam("q3", "q3")
            .addHeader("h3", "h3");
    apiContext.addRequest(httpRpcRequest);

    apiContext.apiDefinition().addPlugin(plugin);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.getHost());
              testContext.assertEquals(8080, request.getPort());
              testContext.assertEquals(2, request.getParams().keySet().size());
              testContext.assertEquals(2, request.getHeaders().keySet().size());
              testContext.assertFalse(request.getParams().containsKey("q3"));
              testContext.assertFalse(request.getHeaders().containsKey("h3"));
              testContext.assertNull(request.getBody());
              System.out.println(request);
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });
  }

  @Test
  public void testTwoRequestTransformer(TestContext testContext) {

    RequestTransformer transformer = createRequestTransformer();

    RequestTransformerPlugin plugin = (RequestTransformerPlugin) ApiPlugin
            .create(RequestTransformerPlugin.class.getSimpleName());
    plugin.addTransformer(transformer);
    HttpRpcRequest httpRpcRequest = HttpRpcRequest.create(UUID.randomUUID().toString(),
                                                          "add_device");
    httpRpcRequest.setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .addParam("q3", "v3")
            .addHeader("h3", "v3");
    apiContext.addRequest(httpRpcRequest);

    httpRpcRequest = HttpRpcRequest.create(UUID.randomUUID().toString(),
                                           "update_device");
    httpRpcRequest.setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .addParam("q3", "v3")
            .addHeader("h3", "v3");
    apiContext.addRequest(httpRpcRequest);


    apiContext.apiDefinition().addPlugin(plugin);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.assertEquals(2, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.getHost());
              testContext.assertEquals(8080, request.getPort());
              testContext.assertEquals(2, request.getParams().size());
              testContext.assertEquals(2, request.getHeaders().size());
              testContext.assertFalse(request.getParams().containsKey("q3"));
              testContext.assertFalse(request.getHeaders().containsKey("h3"));
              testContext.assertNull(request.getBody());

              request = (HttpRpcRequest) context.requests().get(1);
              testContext.assertEquals("localhost", request.getHost());
              testContext.assertEquals(8080, request.getPort());
              testContext.assertEquals(1, request.getParams().size());
              testContext.assertEquals(1, request.getHeaders().size());
              testContext.assertTrue(request.getParams().containsKey("q3"));
              testContext.assertTrue(request.getHeaders().containsKey("h3"));
              testContext.assertNull(request.getBody());
              async.complete();
            }).onFailure(t -> testContext.fail());
  }

  @Test
  public void testSingleRequestTransformerHasBody(TestContext testContext) {

    RequestTransformer transformer = createRequestTransformer();

    RequestTransformerPlugin plugin = (RequestTransformerPlugin) ApiPlugin
            .create(RequestTransformerPlugin.class.getSimpleName());
    plugin.addTransformer(transformer);

    HttpRpcRequest httpRpcRequest = HttpRpcRequest.create(UUID.randomUUID().toString(),
                                                          "add_device");
    httpRpcRequest.setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .addParam("q3", "v3")
            .addHeader("h3", "v3")
            .setBody(new JsonObject().put("b3", "b3"));
    apiContext.addRequest(httpRpcRequest);

    apiContext.apiDefinition().addPlugin(plugin);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.assertEquals(1, context.requests().size());
              HttpRpcRequest request = (HttpRpcRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.getHost());
              testContext.assertEquals(8080, request.getPort());
              testContext.assertEquals(2, request.getParams().size());
              testContext.assertEquals(2, request.getHeaders().size());
              testContext.assertFalse(request.getParams().containsKey("q3"));
              testContext.assertFalse(request.getHeaders().containsKey("h3"));
              testContext.assertEquals(2, request.getBody().size());
              testContext.assertFalse(request.getBody().containsKey("b3"));
              System.out.println(request);
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });
  }

  private void createApiContext() {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, new JsonObject());
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
            Endpoint.createHttp("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists
            .newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
  }

  private RequestTransformer createRequestTransformer() {
    RequestTransformer transformer = RequestTransformer.create("add_device");
    transformer.removeHeader("h3");
    transformer.removeHeader("h4");
    transformer.removeParam("q3");
    transformer.removeParam("q4");
    transformer.removeBody("b3");
    transformer.removeBody("b4");
    transformer.addHeader("h2", "h2");
    transformer.addHeader("h1", "h1");
    transformer.addParam("q1", "q1");
    transformer.addParam("q2", "q2");
    transformer.addBody("b1", "b1");
    transformer.addBody("b2", "b2");
    return transformer;
  }

}
