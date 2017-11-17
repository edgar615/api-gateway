package com.github.edgar615.direvolves.plugin.authentication;

import com.github.edgar615.util.vertx.cache.CacheOptions;
import com.github.edgar615.util.vertx.cache.GuavaCache;
import com.google.common.collect.ArrayListMultimap;
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
import com.github.edgar615.util.base.Randoms;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
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

  String jti = UUID.randomUUID().toString();

  JWTAuth provider;

  private Vertx vertx;

  private String userKey = "userId";

  private String namespace = UUID.randomUUID().toString();

  private int userId = Integer.parseInt(Randoms.randomNumber(5));

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    filter = Filter.create(JwtCleanFilter.class.getSimpleName(), vertx,
                           new JsonObject()
                                   .put("namespace", namespace)
                                   .put("user", new JsonObject().put("userClaimKey", userKey)));

    filters.clear();
    filters.add(filter);
    JsonObject config = new JsonObject().put("keyStore", new JsonObject()
            .put("path", "keystore.jceks")
            .put("type", "jceks")
            .put("password", "secret")
    );

    provider = JWTAuth.create(vertx, config);
    GuavaCache<String, JsonObject> cache = new GuavaCache<>(vertx, "userCache", new CacheOptions());
    CacheManager.instance().addCache(cache);
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
    apiContext.setPrincipal(body);

    CacheManager.instance().getCache("userCache").put(namespace + ":user:" + 1, body, ar -> {
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
              CacheManager.instance().getCache("userCache")
                      .get(namespace + ":user:" + userId, ar -> {
                        if (ar.succeeded()) {
                          testContext.assertNull(ar.result());
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
