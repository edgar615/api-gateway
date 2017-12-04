package com.github.edgar615.direwolves.plugin.transformer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.dispatch.Result;
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
public class ResponseTranformerFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  ResponseTransformerFilter filter;

  private ApiContext apiContext;

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    createApiContext();
    filters.clear();
  }

  @After
  public void tearDown(TestContext testContext) {
    vertx.close(testContext.asyncAssertSuccess());
  }

  @Test
  public void testHeaderAdd(TestContext testContext) {
    JsonObject config = new JsonObject()
            .put("header.add", new JsonArray().add("h1:h1.1").add("h1:h1.2"));
    filter = new ResponseTransformerFilter(new JsonObject().put("response.transformer", config));
    filters.clear();
    filters.add(filter);

    ResponseTransformerPlugin plugin = new ResponseTransformerPluginImpl();
    plugin.addHeader("h2", "h2");
    plugin.addHeader("h1", "h1.3");

    apiContext.apiDefinition().addPlugin(plugin);

    apiContext.setResult(Result.createJsonObject(
            200, new JsonObject().put("foo", "bar"),
            ArrayListMultimap.create()));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.result());
              Result result = context.result();
              testContext.assertEquals(4, result.headers().size());
              testContext.assertTrue(result.headers().containsKey("h1"));
              testContext.assertTrue(result.headers().containsKey("h2"));
              testContext.assertEquals(3, result.headers().get("h1").size());
              testContext.assertEquals(1, result.headers().get("h2").size());
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });
  }

  @Test
  public void testHeaderReplace(TestContext testContext) {
    JsonObject config = new JsonObject()
            .put("header.replace", new JsonArray().add("h1:nh1").add("h2:nh2"));
    filter = new ResponseTransformerFilter(new JsonObject().put("response.transformer", config));
    filters.clear();
    filters.add(filter);

    ResponseTransformerPlugin plugin = new ResponseTransformerPluginImpl();
    plugin.replaceHeader("h4", "nh4");
    plugin.replaceHeader("nh4", "nh4.1");

    apiContext.apiDefinition().addPlugin(plugin);

    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h1", "h1");
    headers.put("h4", "h4.1");
    headers.put("h4", "h4.2");

    apiContext.setResult(Result.createJsonObject(
            200, new JsonObject().put("foo", "bar"),
            headers));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.result());
              Result result = context.result();
              testContext.assertEquals(3, result.headers().size());
              testContext.assertFalse(result.headers().containsKey("h1"));
              testContext.assertFalse(result.headers().containsKey("h2"));
              testContext.assertFalse(result.headers().containsKey("h3"));
              testContext.assertFalse(result.headers().containsKey("h4"));
              testContext.assertTrue(result.headers().containsKey("nh1"));
              testContext.assertFalse(result.headers().containsKey("nh4"));
              testContext.assertTrue(result.headers().containsKey("nh4.1"));
              testContext.assertEquals(1, result.headers().get("nh1").size());
              testContext.assertEquals(2, result.headers().get("nh4.1").size());
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });
  }

  @Test
  public void testHeaderRemove(TestContext testContext) {

    JsonObject config = new JsonObject()
            .put("header.remove", new JsonArray().add("h1").add("h2"));
    filter = new ResponseTransformerFilter(new JsonObject().put("response.transformer", config));
    filters.clear();
    filters.add(filter);

    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h1", "h1");
    headers.put("h4", "h4.1");
    headers.put("h4", "h4.2");
    headers.put("h5", "h5");

    ResponseTransformerPlugin plugin = new ResponseTransformerPluginImpl();
    plugin.removeHeader("h3");
    plugin.removeHeader("h4");
    apiContext.apiDefinition().addPlugin(plugin);
    apiContext.setResult(Result.createJsonObject(
            200, new JsonObject().put("foo", "bar"),
            headers));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.result());
              Result result = context.result();
              testContext.assertFalse(result.headers().containsKey("h1"));
              testContext.assertFalse(result.headers().containsKey("h2"));
              testContext.assertFalse(result.headers().containsKey("h3"));
              testContext.assertFalse(result.headers().containsKey("h4"));
              testContext.assertTrue(result.headers().containsKey("h5"));
              testContext.assertEquals(1, result.headers().get("h5").size());
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });
  }

  @Test
  public void testHeaderOrder(TestContext testContext) {

    JsonObject config = new JsonObject()
            .put("header.remove", new JsonArray().add("h1"))
            .put("header.replace", new JsonArray().add("h1:rh1"))
            .put("header.add", new JsonArray().add("h1:ah1"));
    filter = new ResponseTransformerFilter(new JsonObject().put("response.transformer", config));
    filters.clear();
    filters.add(filter);

    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h1", "h1");

    ResponseTransformerPlugin plugin = new ResponseTransformerPluginImpl();
    plugin.removeHeader("h2");
    plugin.replaceHeader("h2", "rh2");
    plugin.addHeader("h2", "ah2");
    apiContext.apiDefinition().addPlugin(plugin);
    apiContext.setResult(Result.createJsonObject(
            200, new JsonObject().put("foo", "bar"),
            headers));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.result());
              Result result = context.result();
              testContext.assertTrue(result.headers().containsKey("h1"));
              testContext.assertTrue(result.headers().containsKey("h2"));
              testContext.assertEquals("ah1", result.headers().get("h1").iterator().next());
              testContext.assertEquals("ah2", result.headers().get("h2").iterator().next());
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });
  }

  @Test
  public void testBodyAdd(TestContext testContext) {
    JsonObject config = new JsonObject()
            .put("body.add", new JsonArray().add("b1:b1.1").add("b1:b1.2"));
    filter = new ResponseTransformerFilter(new JsonObject().put("response.transformer", config));
    filters.clear();
    filters.add(filter);
    apiContext.setResult(Result.createJsonObject(
            200, new JsonObject(),
            ArrayListMultimap.create()));

    ResponseTransformerPlugin plugin = new ResponseTransformerPluginImpl();

    plugin.addBody("b2", "b2").addBody("b1", "b1.3");

    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.result());
              Result result = context.result();
              testContext.assertTrue(result.responseObject().containsKey("b1"));
              testContext.assertTrue(result.responseObject().containsKey("b2"));
              testContext.assertEquals("b1.3", result.responseObject().getString("b1"));
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });
  }

  @Test
  public void testBodyReplace(TestContext testContext) {

    JsonObject config = new JsonObject()
            .put("body.replace", new JsonArray().add("b1:nb1").add("b2:nb2"));

    filter = new ResponseTransformerFilter(new JsonObject().put("response.transformer", config));
    filters.clear();
    filters.add(filter);
    apiContext.setResult(Result.createJsonObject(
            200, new JsonObject()
                    .put("b1", "b1")
                    .put("b4", new JsonArray().add("b4.1").add("b4.2")),
            ArrayListMultimap.create()));

    ResponseTransformerPlugin plugin = new ResponseTransformerPluginImpl();

    plugin.replaceBody("b3", "nb3")
            .replaceBody("b4", "nb4")
            .replaceBody("nb4", "nb4.1");

    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.result());
              Result result = context.result();
              testContext.assertFalse(result.responseObject().containsKey("b1"));
              testContext.assertFalse(result.responseObject().containsKey("b2"));
              testContext.assertFalse(result.responseObject().containsKey("b3"));
              testContext.assertFalse(result.responseObject().containsKey("b4"));
              testContext.assertTrue(result.responseObject().containsKey("nb1"));
              testContext.assertFalse(result.responseObject().containsKey("nb4"));
              testContext.assertTrue(result.responseObject().containsKey("nb4.1"));
              testContext.assertEquals("b1", result.responseObject().getString("nb1"));
              testContext.assertEquals(2, result.responseObject().getJsonArray("nb4.1").size());
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });


  }

  @Test
  public void testBodyRemove(TestContext testContext) {

    JsonObject config = new JsonObject()
            .put("body.remove", new JsonArray().add("b1").add("b2"));
    filter = new ResponseTransformerFilter(new JsonObject().put("response.transformer", config));
    filters.clear();
    filters.add(filter);
    apiContext.setResult(Result.createJsonObject(
            200, new JsonObject()
                    .put("b1", "b1")
                    .put("b4", new JsonArray().add("b4.1").add("b4.2"))
                    .put("b5", "b5"),
            ArrayListMultimap.create()));

    ResponseTransformerPlugin plugin = new ResponseTransformerPluginImpl();

    plugin.removeBody("b3")
            .removeBody("b4");

    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.result());
              Result result = context.result();
              testContext.assertFalse(result.responseObject().containsKey("b1"));
              testContext.assertFalse(result.responseObject().containsKey("b2"));
              testContext.assertFalse(result.responseObject().containsKey("b3"));
              testContext.assertFalse(result.responseObject().containsKey("b4"));
              testContext.assertTrue(result.responseObject().containsKey("b5"));
              testContext.assertEquals("b5", result.responseObject().getString("b5"));
              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });

  }

  @Test
  public void testBodyOrder(TestContext testContext) {
    //先删掉某个请求头，replace不起作用，add会是一个新元素
    JsonObject config = new JsonObject()
            .put("body.remove", new JsonArray().add("b1"))
            .put("body.replace", new JsonArray().add("b1:rb1"))
            .put("body.add", new JsonArray().add("b1:ab1"));
    filter = new ResponseTransformerFilter(new JsonObject().put("response.transformer", config));
    filters.clear();
    filters.add(filter);
    apiContext.setResult(Result.createJsonObject(
            200, new JsonObject()
                    .put("b1", "b1"),
            ArrayListMultimap.create()));

    ResponseTransformerPlugin plugin = new ResponseTransformerPluginImpl();

    plugin.removeBody("b2")
            .replaceBody("b2", "rb2")
            .addBody("b2", "ab2");

    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              System.out.println(context.result());
              Result result = context.result();
              testContext.assertTrue(result.responseObject().containsKey("b1"));
              testContext.assertTrue(result.responseObject().containsKey("b2"));
              testContext.assertEquals("ab1", result.responseObject().getString("b1"));
              testContext.assertEquals("ab2", result.responseObject().getString("b2"));

              async.complete();
            }).onFailure(t -> {
      t.printStackTrace();
      testContext.fail();
    });
  }

  @Test
  public void testJsonArrayIncludeBody(TestContext testContext) {
    ResponseTransformerPlugin plugin = (ResponseTransformerPlugin) ApiPlugin
            .create(ResponseTransformerPlugin.class.getSimpleName());
    plugin.addBody("b1", "b1");
    plugin.addBody("b2", "b2");
    apiContext.apiDefinition().addPlugin(plugin);
    apiContext.setResult(Result.createJsonArray(
            200, new JsonArray().add(1),
            ImmutableMultimap.of("h3", "h3", "h6", "h6")));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              Result result = context.result();
              testContext.assertEquals(1, result.responseArray().size());
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

}