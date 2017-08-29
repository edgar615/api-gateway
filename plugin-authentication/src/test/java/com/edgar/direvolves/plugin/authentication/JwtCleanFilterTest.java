package com.edgar.direvolves.plugin.authentication;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.cache.RedisProvider;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.definition.SimpleHttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.dispatch.Result;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.util.base.Randoms;
import com.edgar.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
@RunWith(VertxUnitRunner.class)
public class JwtCleanFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  Filter filter;

  RedisProvider redisProvider = new MockRedisProvider();

  private Vertx vertx;

  private String userKey = UUID.randomUUID().toString();

  private String namespace = UUID.randomUUID().toString();

  private String cacheAddress = namespace + "." + RedisProvider.class.getName();
  private int userId = Integer.parseInt(Randoms.randomNumber(5));
  String jti = UUID.randomUUID().toString();

  JWTAuth provider;
  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    ProxyHelper.registerService(RedisProvider.class, vertx, redisProvider, cacheAddress);

    filter = Filter.create(JwtBuildFilter.class.getSimpleName(), vertx,
                           new JsonObject()
                                   .put("namespace", namespace)
                                   .put("jwt.userClaimKey", userKey));

    filters.clear();
    filters.add(filter);
    JsonObject config = new JsonObject().put("keyStore", new JsonObject()
            .put("path", "keystore.jceks")
            .put("type", "jceks")
            .put("password", "secret")
    );

    provider = JWTAuth.create(vertx, config);
  }

  @Test
  public void testCleanToken(TestContext testContext) {

    JsonObject claims = new JsonObject()
        .put(userKey, userId)
        .put("jti", jti);
//                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);

    String token =
        provider.generateToken(claims, new JWTOptions().setAlgorithm("HS512"));

    ApiContext apiContext = createContext(token);

    JsonObject body = new JsonObject()
            .put("username", "edgar")
            .put("tel", "123456")
            .put(userKey, 1);
    apiContext.setResult(Result.createJsonObject(200, body, null));

    redisProvider.set(namespace + ":user:" + 1, body, ar -> {
      if (ar.succeeded()) {
        System.out.println(ar.result());
      } else {
        testContext.fail();
      }
    });

    try {
      TimeUnit.SECONDS.sleep(2);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    JwtCleanPlugin plugin = (JwtCleanPlugin) ApiPlugin.create(JwtCleanPlugin
                                                                      .class
                                                                      .getSimpleName());
    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              Result result = context.result();
              redisProvider.get(namespace + ":user:" + userId, ar -> {
                if (ar.succeeded()) {
                  System.out.println(ar.result());
                  testContext.fail();
                } else {
                  async.complete();
                }
              });
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.fail();
            });

  }

  private ApiContext createContext(String token) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");
    headers.put("Authorization",
        "Bearer " + token);
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
