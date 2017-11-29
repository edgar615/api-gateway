package com.github.edgar615.direvolves.plugin.authentication;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.Filters;
import com.github.edgar615.util.base.Randoms;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
@RunWith(VertxUnitRunner.class)
public class UserLoadFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  Filter filter;

  int port = Integer.parseInt(Randoms.randomNumber(4));

  String id = UUID.randomUUID().toString();

  private Vertx vertx;

  private String namespace = UUID.randomUUID().toString();

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    filters.clear();
    AtomicBoolean completed = new AtomicBoolean();

    vertx.createHttpServer().requestHandler(req -> {
      String userId = req.getParam("userId");
      if (id.equalsIgnoreCase(userId)) {
        JsonObject jsonObject = new JsonObject()
                .put("userId", userId)
                .put("username", "edgar615");
        req.response().end(jsonObject.encode());
      } else {
        req.response().setStatusCode(404)
                .end();
      }

    }).listen(port, ar -> {
      if (ar.succeeded()) {
        completed.set(true);
      } else {
        ar.cause().printStackTrace();
      }
    });

    Awaitility.await().until(() -> completed.get());
  }

  @Test
  public void testNoLoader(TestContext testContext) {
    filter = Filter.create(UserLoaderFilter.class.getSimpleName(), vertx,
                           new JsonObject());
    filters.add(filter);
    ApiContext apiContext = createContext();

    JsonObject body = new JsonObject()
            .put("userId", 10);
    apiContext.setPrincipal(body);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              JsonObject user = context.principal();
              System.out.println(user);
              testContext.assertFalse(user.containsKey("username"));
              async.complete();
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.fail();
            });
  }

  @Test
  public void testNoUserId(TestContext testContext) {
    JsonObject userConfig = new JsonObject()
            .put("url", "/" + UUID.randomUUID().toString());
    filter = Filter.create(UserLoaderFilter.class.getSimpleName(), vertx,
                           new JsonObject().put("user", userConfig)
                                   .put("port", port).put("namespace", namespace));
    filters.add(filter);
    ApiContext apiContext = createContext();

    JsonObject body = new JsonObject();
    apiContext.setPrincipal(body);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              JsonObject user = context.principal();
              System.out.println(user);
              testContext.assertFalse(user.containsKey("username"));
              async.complete();
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.fail();
            });
  }

  @Test
  public void testNotExist(TestContext testContext) {
    JsonObject userConfig = new JsonObject()
            .put("url", "/" + UUID.randomUUID().toString());
    filter = Filter.create(UserLoaderFilter.class.getSimpleName(), vertx,
                           new JsonObject().put("user", userConfig)
                                   .put("port", port).put("namespace", namespace));
    filters.add(filter);
    ApiContext apiContext = createContext();

    JsonObject body = new JsonObject()
            .put("userId", UUID.randomUUID().toString());
    apiContext.setPrincipal(body);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              JsonObject user = context.principal();
              System.out.println(user);
              testContext.assertFalse(user.containsKey("username"));
              async.complete();
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.fail();
            });
  }

  @Test
  public void testLoadSuccess(TestContext testContext) {
    JsonObject userConfig = new JsonObject()
            .put("url", "/" + UUID.randomUUID().toString());
    filter = Filter.create(UserLoaderFilter.class.getSimpleName(), vertx,
                           new JsonObject().put("user", userConfig)
                                   .put("port", port).put("namespace", namespace));
    filters.add(filter);
    ApiContext apiContext = createContext();

    JsonObject body = new JsonObject()
            .put("userId", id);
    apiContext.setPrincipal(body);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              JsonObject user = context.principal();
              System.out.println(user);
              testContext.assertTrue(user.containsKey("username"));
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
