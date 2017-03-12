package com.edgar.direvolves.plugin.authentication;

import com.edgar.direwolves.core.cache.RedisProvider;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.Result;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.util.vertx.task.Task;
import com.google.common.base.Splitter;
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
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
@RunWith(VertxUnitRunner.class)
public class JwtBuildFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  Filter filter;

  RedisProvider redisProvider = new MockRedisProvider();

  private Vertx vertx;

  private String userKey = UUID.randomUUID().toString();

  private String namespace = UUID.randomUUID().toString();

  private String cacheAddress = namespace + "." + RedisProvider.class.getName();

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    ProxyHelper.registerService(RedisProvider.class, vertx, redisProvider, cacheAddress);

    filter = Filter.create(JwtBuildFilter.class.getSimpleName(), vertx,
                           new JsonObject()
                                   .put("project.namespace", namespace)
                                   .put("jwt.userClaimKey", userKey));

    filters.clear();
    filters.add(filter);

  }

  @Test
  public void badRequestShouldNotContainToken(TestContext testContext) {
    ApiContext apiContext = createContext();

    JsonObject body = new JsonObject()
            .put("username", "edgar")
            .put("tel", "123456")
            .put(userKey, 10);
    apiContext.setResult(Result.createJsonObject(400, body, null));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              Result result = context.result();
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
    ApiContext apiContext = createContext();

    JsonObject body = new JsonObject()
            .put("username", "edgar")
            .put("tel", "123456")
            .put("userId", 10);
    apiContext.setResult(Result.createJsonObject(200, body, null));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              Result result = context.result();
              testContext.assertFalse(result.responseObject().containsKey("token"));
              async.complete();
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.fail();
            });
  }

  @Test
  public void validUserShouldContainToken(TestContext testContext) {
    ApiContext apiContext = createContext();

    JsonObject body = new JsonObject()
            .put("username", "edgar")
            .put("tel", "123456")
            .put(userKey, 1);
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
              testContext.assertTrue(result.responseObject().containsKey("token"));
              String token = result.responseObject().getString("token");
              String token2 = Iterables.get(Splitter.on(".").split(token), 1);
              JsonObject chaim = new JsonObject(new String(Base64.getDecoder().decode(token2)));
              System.out.println(chaim);
              System.out.println(new Date(chaim.getLong("exp") * 1000));
              testContext.assertTrue(chaim.containsKey("jti"));

              redisProvider.get(namespace + ":user:" + 1, ar -> {
                if (ar.succeeded()) {
                  System.out.println(ar.result());
                  async.complete();
                } else {
                  testContext.fail();
                }
              });
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
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
            HttpEndpoint.http("add_device", HttpMethod.GET, "devices/", "device");
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
