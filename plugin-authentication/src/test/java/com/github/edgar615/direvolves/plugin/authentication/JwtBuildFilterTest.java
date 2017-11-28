package com.github.edgar615.direvolves.plugin.authentication;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.cache.CacheManager;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
@RunWith(VertxUnitRunner.class)
public class JwtBuildFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  Filter filter;

  private Vertx vertx;

  private String namespace = UUID.randomUUID().toString();

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    filters.clear();
  }

  @Test
  public void badRequestShouldNotContainToken(TestContext testContext) {
    filter = Filter.create(JwtBuildFilter.class.getSimpleName(), vertx,
            new JsonObject());
    filters.add(filter);
    ApiContext apiContext = createContext();

    JsonObject body = new JsonObject()
            .put("username", "edgar")
            .put("tel", "123456")
            .put("userId", 10);
    apiContext.setResult(Result.createJsonObject(400, body, null));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              Result result = context.result();
              System.out.println(result.responseObject());
              testContext.assertFalse(result.responseObject().containsKey("token"));
              async.complete();
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.fail();
            });
  }

  @Test
  public void missUserKeyShouldNotContainToken(TestContext testContext) {
    filter = Filter.create(JwtBuildFilter.class.getSimpleName(), vertx,
            new JsonObject());
    filters.add(filter);
    ApiContext apiContext = createContext();

    JsonObject body = new JsonObject()
            .put("username", "edgar")
            .put("tel", "123456")
            .put(UUID.randomUUID().toString(), 10);
    apiContext.setResult(Result.createJsonObject(200, body, null));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              Result result = context.result();
              System.out.println(result.responseObject());
              testContext.assertFalse(result.responseObject().containsKey("token"));
              async.complete();
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.fail();
            });
  }

  @Test
  public void testDefaultTokenNoExp(TestContext testContext) {
    JsonObject config = new JsonObject()
            .put("algorithm", "HS512");
    filter = Filter.create(JwtBuildFilter.class.getSimpleName(), vertx,
            new JsonObject().put("jwt.builder", config));
    filters.add(filter);
    ApiContext apiContext = createContext();

    JsonObject body = new JsonObject()
            .put("username", "edgar")
            .put("tel", "123456")
            .put("userId", 1);
    apiContext.setResult(Result.createJsonObject(200, body, null));

    JwtBuildPlugin plugin = (JwtBuildPlugin) ApiPlugin.create(JwtBuildPlugin
                                                                      .class
                                                                      .getSimpleName());
    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              Result result = context.result();
              System.out.println(result.responseObject());
              testContext.assertTrue(result.responseObject().containsKey("userId"));
              testContext.assertTrue(result.responseObject().containsKey("username"));
              testContext.assertTrue(result.responseObject().containsKey("tel"));
              testContext.assertTrue(result.responseObject().containsKey("token"));
              String token = result.responseObject().getString("token");
              String token2 = Iterables.get(Splitter.on(".").split(token), 1);
              JsonObject chaim = new JsonObject(new String(Base64.getDecoder().decode(token2)));
              System.out.println(chaim);
              testContext.assertFalse(chaim.containsKey("exp"));
              testContext.assertTrue(chaim.containsKey("jti"));
              async.complete();
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.fail();
            });
  }

  @Test
  public void testTokenHasExp(TestContext testContext) {
    JsonObject config = new JsonObject()
            .put("algorithm", "HS512")
            .put("expiresInSeconds", 1000);
    filter = Filter.create(JwtBuildFilter.class.getSimpleName(), vertx,
            new JsonObject().put("jwt.builder", config));
    filters.add(filter);
    ApiContext apiContext = createContext();

    JsonObject body = new JsonObject()
            .put("username", "edgar")
            .put("tel", "123456")
            .put("userId", 1);
    apiContext.setResult(Result.createJsonObject(200, body, null));

    JwtBuildPlugin plugin = (JwtBuildPlugin) ApiPlugin.create(JwtBuildPlugin
            .class
            .getSimpleName());
    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              Result result = context.result();
              System.out.println(result.responseObject());
              testContext.assertTrue(result.responseObject().containsKey("userId"));
              testContext.assertTrue(result.responseObject().containsKey("username"));
              testContext.assertTrue(result.responseObject().containsKey("tel"));
              testContext.assertTrue(result.responseObject().containsKey("token"));
              String token = result.responseObject().getString("token");
              String token2 = Iterables.get(Splitter.on(".").split(token), 1);
              JsonObject chaim = new JsonObject(new String(Base64.getDecoder().decode(token2)));
              System.out.println(chaim);
              testContext.assertTrue(chaim.containsKey("exp"));
              testContext.assertTrue(chaim.containsKey("jti"));
              async.complete();
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.fail();
            });
  }

  @Test
  public void testOnlyContainsToken(TestContext testContext) {
    JsonObject config = new JsonObject()
            .put("algorithm", "HS512")
            .put("emptyingField", true)
            .put("expiresInSeconds", 1000);
    filter = Filter.create(JwtBuildFilter.class.getSimpleName(), vertx,
            new JsonObject().put("jwt.builder", config));
    filters.add(filter);
    ApiContext apiContext = createContext();

    JsonObject body = new JsonObject()
            .put("username", "edgar")
            .put("tel", "123456")
            .put("userId", 1);
    apiContext.setResult(Result.createJsonObject(200, body, null));

    JwtBuildPlugin plugin = (JwtBuildPlugin) ApiPlugin.create(JwtBuildPlugin
            .class
            .getSimpleName());
    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              Result result = context.result();
              System.out.println(result.responseObject());
              testContext.assertEquals(1, result.responseObject().size());
              testContext.assertTrue(result.responseObject().containsKey("token"));
              String token = result.responseObject().getString("token");
              String token2 = Iterables.get(Splitter.on(".").split(token), 1);
              JsonObject chaim = new JsonObject(new String(Base64.getDecoder().decode(token2)));
              System.out.println(chaim);
              testContext.assertTrue(chaim.containsKey("exp"));
              testContext.assertTrue(chaim.containsKey("jti"));
              testContext.assertFalse(chaim.containsKey("tel"));
              async.complete();
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.fail();
            });
  }

  @Test
  public void testContainsTel(TestContext testContext) {
    JsonObject config = new JsonObject()
            .put("algorithm", "HS512")
            .put("emptyingField", true)
            .put("claimKey", new JsonArray().add("tel").add("username"))
            .put("expiresInSeconds", 1000);
    filter = Filter.create(JwtBuildFilter.class.getSimpleName(), vertx,
            new JsonObject().put("jwt.builder", config));
    filters.add(filter);
    ApiContext apiContext = createContext();

    JsonObject body = new JsonObject()
            .put("username", "edgar")
            .put("tel", "123456")
            .put("userId", 1);
    apiContext.setResult(Result.createJsonObject(200, body, null));

    JwtBuildPlugin plugin = (JwtBuildPlugin) ApiPlugin.create(JwtBuildPlugin
            .class
            .getSimpleName());
    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              Result result = context.result();
              System.out.println(result.responseObject());
              testContext.assertEquals(1, result.responseObject().size());
              testContext.assertTrue(result.responseObject().containsKey("token"));
              String token = result.responseObject().getString("token");
              String token2 = Iterables.get(Splitter.on(".").split(token), 1);
              JsonObject chaim = new JsonObject(new String(Base64.getDecoder().decode(token2)));
              System.out.println(chaim);
              testContext.assertTrue(chaim.containsKey("exp"));
              testContext.assertTrue(chaim.containsKey("jti"));
              testContext.assertTrue(chaim.containsKey("tel"));
              testContext.assertTrue(chaim.containsKey("username"));
              async.complete();
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.fail();
            });
  }

  private ApiContext createContext() {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");
    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/", 80, "localhost");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists
            .newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    JwtBuildPlugin plugin = (JwtBuildPlugin) ApiPlugin.create(JwtBuildPlugin
                                                                      .class
                                                                      .getSimpleName());
    apiContext.apiDefinition().addPlugin(plugin);
    return apiContext;
  }

}
