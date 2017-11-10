package com.github.edgar615.direwolves.plugin.transformer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.rpc.http.SimpleHttpRequest;
import com.github.edgar615.direwolves.core.utils.Filters;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
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

    filter = new RequestTransformerFilter(new JsonObject());

    filters.clear();
    filters.add(filter);

    createApiContext();
  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close(testContext.asyncAssertSuccess());
  }

  @Test
  public void testGlobalTransformer(TestContext testContext) {

    JsonObject config = new JsonObject()
            .put("header.add", new JsonArray().add("gh2:gh2").add( "h1:h1"))
            .put("header.remove", new JsonArray().add("h3").add( "h4"))
            .put("header.replace", new JsonArray().add("h5:rh5").add( "h6:rh6"))
            .put("query.add", new JsonArray().add("q2:q2").add( "q1:q1"))
            .put("query.remove", new JsonArray().add("q3").add( "q4"))
            .put("query.replace", new JsonArray().add("q5:rq5").add( "q6:rq6"))
            .put("body.add", new JsonArray().add("b2:b2").add( "b1:b1"))
            .put("body.remove", new JsonArray().add("b3").add( "b4"))
            .put("body.replace", new JsonArray().add("b5:rb5").add( "b6:rb6"));
    filter = new RequestTransformerFilter(new JsonObject().put("request.transformer", config));
    filters.clear();
    filters.add(filter);
    SimpleHttpRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                             "add_device");
    httpRpcRequest.setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.GET)
            .setPath("/")
            .addParam("q3", "q3")
            .addParam("q5", "q5")
            .addHeader("h3", "h3")
            .addHeader("h6", "h6");
    apiContext.addRequest(httpRpcRequest);

    httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                           "update_device");
    httpRpcRequest.setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .addParam("q3", "q3")
            .addParam("q6", "q6")
            .addHeader("h3", "h3")
            .addHeader("h5", "h5");
    apiContext.addRequest(httpRpcRequest);
    RequestTransformer transformer = createRequestTransformer();

    RequestTransformerPlugin plugin = (RequestTransformerPlugin) ApiPlugin
            .create(RequestTransformerPlugin.class.getSimpleName());
    plugin.addTransformer(transformer);
    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.assertEquals(2, context.requests().size());
              SimpleHttpRequest request = (SimpleHttpRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              testContext.assertEquals(3, request.params().size());
              testContext.assertEquals(4, request.headers().size());
              testContext.assertFalse(request.params().containsKey("q3"));
              testContext.assertFalse(request.headers().containsKey("h3"));
              testContext.assertFalse(request.params().containsKey("q5"));
              testContext.assertFalse(request.headers().containsKey("h6"));
              testContext.assertTrue(request.params().containsKey("rq5"));
              testContext.assertTrue(request.headers().containsKey("rh6"));
              testContext.assertNull(request.body());

              request = (SimpleHttpRequest) context.requests().get(1);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              testContext.assertEquals(3, request.params().size());
              testContext.assertEquals(3, request.headers().size());
              testContext.assertFalse(request.params().containsKey("q3"));
              testContext.assertFalse(request.headers().containsKey("h3"));
              testContext.assertFalse(request.params().containsKey("q6"));
              testContext.assertFalse(request.headers().containsKey("h5"));
              testContext.assertTrue(request.params().containsKey("rq6"));
              testContext.assertTrue(request.headers().containsKey("rh5"));
              testContext.assertNotNull(request.body());
              async.complete();
            }).onFailure(t ->{
      t.printStackTrace();
      testContext.fail();
    });
  }


  @Test
  public void testSingleRequestTransformer(TestContext testContext) {

    RequestTransformer transformer = createRequestTransformer();
    RequestTransformerPlugin plugin = (RequestTransformerPlugin) ApiPlugin
            .create(RequestTransformerPlugin.class.getSimpleName());
    plugin.addTransformer(transformer);
    SimpleHttpRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                          "add_device");
    httpRpcRequest.setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .addParam("q3", "q3")
            .addParam("q5", "q5")
            .addHeader("h3", "h3")
            .addHeader("h6", "h6");
    apiContext.addRequest(httpRpcRequest);

    apiContext.apiDefinition().addPlugin(plugin);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.assertEquals(1, context.requests().size());
              SimpleHttpRequest request = (SimpleHttpRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              testContext.assertEquals(3, request.params().keySet().size());
              testContext.assertEquals(3, request.headers().keySet().size());
              testContext.assertFalse(request.params().containsKey("q3"));
              testContext.assertFalse(request.headers().containsKey("h3"));
              testContext.assertFalse(request.params().containsKey("q5"));
              testContext.assertFalse(request.headers().containsKey("h6"));
              testContext.assertTrue(request.params().containsKey("rq5"));
              testContext.assertTrue(request.headers().containsKey("rh6"));

              testContext.assertNotNull(request.body());
              testContext.assertEquals(2, request.body().size());
              testContext.assertEquals("b1", request.body().getString("b1"));
              testContext.assertEquals("b2", request.body().getString("b2"));
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
    SimpleHttpRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                          "add_device");
    httpRpcRequest.setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.GET)
            .setPath("/")
            .addParam("q3", "q3")
            .addParam("q5", "q5")
            .addHeader("h3", "h3")
            .addHeader("h6", "h6");
    apiContext.addRequest(httpRpcRequest);

    httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                           "update_device");
    httpRpcRequest.setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .addParam("q3", "q3")
            .addParam("q6", "q6")
            .addHeader("h3", "h3")
            .addHeader("h5", "h5");
    apiContext.addRequest(httpRpcRequest);


    apiContext.apiDefinition().addPlugin(plugin);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.assertEquals(2, context.requests().size());
              SimpleHttpRequest request = (SimpleHttpRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              testContext.assertEquals(3, request.params().size());
              testContext.assertEquals(3, request.headers().size());
              testContext.assertFalse(request.params().containsKey("q3"));
              testContext.assertFalse(request.headers().containsKey("h3"));
              testContext.assertFalse(request.params().containsKey("q5"));
              testContext.assertFalse(request.headers().containsKey("h6"));
              testContext.assertTrue(request.params().containsKey("rq5"));
              testContext.assertTrue(request.headers().containsKey("rh6"));
              testContext.assertNull(request.body());

              request = (SimpleHttpRequest) context.requests().get(1);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              testContext.assertEquals(2, request.params().size());
              testContext.assertEquals(2, request.headers().size());
              testContext.assertTrue(request.params().containsKey("q3"));
              testContext.assertTrue(request.headers().containsKey("h3"));
              testContext.assertTrue(request.params().containsKey("q6"));
              testContext.assertTrue(request.headers().containsKey("h5"));
              testContext.assertFalse(request.params().containsKey("rq6"));
              testContext.assertFalse(request.headers().containsKey("rh5"));
              testContext.assertNull(request.body());
              async.complete();
            }).onFailure(t -> testContext.fail());
  }

  @Test
  public void testSingleRequestTransformerHasBody(TestContext testContext) {

    RequestTransformer transformer = createRequestTransformer();

    RequestTransformerPlugin plugin = (RequestTransformerPlugin) ApiPlugin
            .create(RequestTransformerPlugin.class.getSimpleName());
    plugin.addTransformer(transformer);

    SimpleHttpRequest httpRpcRequest = SimpleHttpRequest.create(UUID.randomUUID().toString(),
                                                          "add_device");
    httpRpcRequest.setHost("localhost")
            .setPort(8080)
            .setHttpMethod(HttpMethod.POST)
            .setPath("/")
            .addParam("q3", "q3")
            .addParam("q5", "q5")
            .addHeader("h3", "h3")
            .addHeader("h6", "h6")
            .setBody(new JsonObject().put("b3", "b3").put("b5", "b5"));
    apiContext.addRequest(httpRpcRequest);

    apiContext.apiDefinition().addPlugin(plugin);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.assertEquals(1, context.requests().size());
              SimpleHttpRequest request = (SimpleHttpRequest) context.requests().get(0);
              testContext.assertEquals("localhost", request.host());
              testContext.assertEquals(8080, request.port());
              testContext.assertEquals(3, request.params().size());
              testContext.assertEquals(3, request.headers().size());
              testContext.assertFalse(request.params().containsKey("q3"));
              testContext.assertFalse(request.headers().containsKey("h3"));
              testContext.assertFalse(request.params().containsKey("q5"));
              testContext.assertFalse(request.headers().containsKey("h6"));
              testContext.assertTrue(request.params().containsKey("rq5"));
              testContext.assertTrue(request.headers().containsKey("rh6"));
              testContext.assertEquals(3, request.body().size());
              testContext.assertFalse(request.body().containsKey("b3"));
              testContext.assertFalse(request.body().containsKey("b5"));
              testContext.assertTrue(request.body().containsKey("rb5"));
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
    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/",
                                    80, "localhost");
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

    transformer.replaceHeader("h5", "rh5");
    transformer.replaceHeader("h6", "rh6");
    transformer.replaceParam("q5", "rq5");
    transformer.replaceParam("q6", "rq6");
    transformer.replaceBody("b5", "rb5");
    transformer.replaceBody("b6", "rb6");

    transformer.addHeader("h2", "h2");
    transformer.addHeader("h1", "h1");
    transformer.addParam("q1", "q1");
    transformer.addParam("q2", "q2");
    transformer.addBody("b1", "b1");
    transformer.addBody("b2", "b2");
    return transformer;
  }

}
