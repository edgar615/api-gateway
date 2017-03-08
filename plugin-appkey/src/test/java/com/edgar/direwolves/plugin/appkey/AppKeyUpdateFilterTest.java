package com.edgar.direwolves.plugin.appkey;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.cache.RedisProvider;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.Result;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.util.base.Randoms;
import com.edgar.util.vertx.task.Task;
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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
@RunWith(VertxUnitRunner.class)
public class AppKeyUpdateFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  Filter filter;

  RedisProvider redisProvider = new MockRedisProvider();

  private Vertx vertx;

  private String userKey = UUID.randomUUID().toString();

  private String namespace = UUID.randomUUID().toString();

  private String cacheAddress = namespace + ":" + RedisProvider.class.getName();

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    ProxyHelper.registerService(RedisProvider.class, vertx, redisProvider, cacheAddress);

    filter = Filter.create(AppKeyUpdateFilter.class.getSimpleName(), vertx,
                           new JsonObject()
                                   .put("project.namespace", namespace)
                                   .put("jwt.userClaimKey", userKey));

    filters.clear();
    filters.add(filter);

  }

  @Test
  public void badRequestShouldNotUpdateAppKey(TestContext testContext) {
    ApiContext apiContext = createContext();

    String appKey = Randoms.randomAlphabet(20);
    JsonObject body = new JsonObject()
            .put("appKey", appKey)
            .put("appSecret", "123456")
            .put("appCode", 0);
    apiContext.setResult(Result.createJsonObject(400, body, null));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              redisProvider.get(namespace + ":appKey:" + appKey, ar -> {
                if (ar.succeeded()) {
                  System.out.println(ar.result());
                  testContext.fail();
                } else {
                  testContext.assertTrue(ar.cause() instanceof NoSuchElementException);
                  async.complete();
                }
              });
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.fail();
            });
  }

  @Test
  public void missUserKeyShouldNotUpdateAppKey(TestContext testContext) {
    ApiContext apiContext = createContext();

    String appKey = Randoms.randomAlphabet(20);
    JsonObject body = new JsonObject()
            .put("appSecret", "123456")
            .put("appCode", 0);
    apiContext.setResult(Result.createJsonObject(200, body, null));

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              redisProvider.get(namespace + ":appKey:" + appKey, ar -> {
                if (ar.succeeded()) {
                  System.out.println(ar.result());
                  testContext.fail();
                } else {
                  testContext.assertTrue(ar.cause() instanceof NoSuchElementException);
                  async.complete();
                }
              });
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.fail();
            });
  }

  @Test
  public void validAppKeyShouldUpdateCache(TestContext testContext) {
    ApiContext apiContext = createContext();

    String appKey = Randoms.randomAlphabet(20);
    JsonObject body = new JsonObject()
            .put("appKey", appKey)
            .put("appSecret", "123456")
            .put("appCode", 5656);
    apiContext.setResult(Result.createJsonObject(200, body, null));

    AppKeyUpdatePlugin plugin = (AppKeyUpdatePlugin)
            ApiPlugin.create(AppKeyUpdatePlugin.class
                                     .getSimpleName());
    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              Result result = context.result();
              testContext.assertTrue(result.responseObject().containsKey("appKey"));
              redisProvider.get(namespace + ":appKey:" + appKey, ar -> {
                if (ar.succeeded()) {
                  testContext.assertEquals(body.getString("appKey"), appKey);
                  testContext.assertEquals(body.getString("appSecret"), ar.result().getString("appSecret"));
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
            Endpoint.http("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists
            .newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    return apiContext;
  }

}
