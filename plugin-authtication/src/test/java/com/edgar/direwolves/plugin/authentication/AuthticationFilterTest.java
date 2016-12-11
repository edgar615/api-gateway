package com.edgar.direwolves.plugin.authentication;

import com.edgar.direwolves.core.cache.CacheProvider;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.EventbusUtils;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.util.base.Randoms;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.edgar.util.vertx.task.Task;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
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
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
@RunWith(VertxUnitRunner.class)
public class AuthticationFilterTest {

  private final List<Filter> filters = new ArrayList<>();
  Filter filter;

  private Vertx vertx;

  String jti = UUID.randomUUID().toString();
  private String userKey = UUID.randomUUID().toString();
  private String cacheAddress = UUID.randomUUID().toString();
  private String namespace = UUID.randomUUID().toString();
  private int userId = Integer.parseInt(Randoms.randomNumber(5));
  CacheProvider cacheProvider = new MockCacheProvider();

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    ProxyHelper.registerService(CacheProvider.class, vertx, cacheProvider, cacheAddress);

    filter = Filter.create(AuthenticationFilter.class.getSimpleName(), vertx,
        new JsonObject()
            .put("jwt.expires", 60 * 30)
            .put("jwt.user.key", userKey)
            .put("jwt.user.unique", false)
            .put("service.cache.address", cacheAddress)
            .put("project.namespace", namespace));

    filters.clear();
    filters.add(filter);

  }

  @Test
  public void testErrorJwt(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");
    headers.put("h3", "v3.2");
    ApiContext apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.createHttp("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    AuthenticationPlugin plugin = (AuthenticationPlugin) ApiPlugin.create(AuthenticationPlugin
        .class
        .getSimpleName());
    plugin.add("jwt").add("basic");
    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
        .andThen(context -> testContext.fail())
        .onFailure(throwable -> {
          throwable.printStackTrace();
          testContext.assertTrue(throwable instanceof SystemException);
          SystemException ex = (SystemException) throwable;
          testContext.assertEquals(DefaultErrorCode.INVALID_TOKEN, ex.getErrorCode());
          async.complete();
        });
  }

  @Test
  public void testSuccessJwt(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");

    JsonObject claims = new JsonObject()
        .put("userId", 1)
        .put("jti", jti);
    headers.put("Authorization", "Bearer " + createToken(claims));
    ApiContext apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.createHttp("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    AuthenticationPlugin plugin = (AuthenticationPlugin) ApiPlugin.create(AuthenticationPlugin
        .class
        .getSimpleName());
    plugin.add("jwt").add("basic");
    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
        .andThen(context -> {
          JsonObject principal = context.principal();
          testContext.assertEquals("edgar", principal.getString("username"));
          async.complete();
        })
        .onFailure(throwable -> testContext.fail());
  }

  @Test
  public void testSuccessBasic(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");

    String basic = Base64.getEncoder().encodeToString("edgar:123".getBytes());
    headers.put("Authorization", "Basic " + basic);
    ApiContext apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.createHttp("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    AuthenticationPlugin plugin = (AuthenticationPlugin) ApiPlugin.create(AuthenticationPlugin
        .class
        .getSimpleName());
    plugin.add("jwt").add("basic");
    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
        .andThen(context -> {
          JsonObject principal = context.principal();
          testContext.assertEquals("edgar", principal.getString("username"));
          testContext.assertEquals("super", principal.getString("role"));
          async.complete();
        })
        .onFailure(throwable -> testContext.fail());
  }

  @Test
  public void testInvalidBasic(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");

    String basic = Base64.getEncoder().encodeToString("edgar".getBytes());
    headers.put("Authorization", "Basic " + basic);
    ApiContext apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.createHttp("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    AuthenticationPlugin plugin = (AuthenticationPlugin) ApiPlugin.create(AuthenticationPlugin
        .class
        .getSimpleName());
    plugin.add("jwt").add("basic");
    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
        .andThen(context -> testContext.fail())
        .onFailure(throwable -> {
          testContext.assertTrue(throwable instanceof SystemException);
          SystemException ex = (SystemException) throwable;
          testContext.assertEquals(DefaultErrorCode.INVALID_TOKEN, ex.getErrorCode());
          async.complete();
        });
  }

  @Test
  public void testErrorPwdBasic(TestContext testContext) {
    Multimap<String, String> params = ArrayListMultimap.create();
    params.put("q3", "v3");
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("h3", "v3");

    String basic = Base64.getEncoder().encodeToString("edgar:1".getBytes());
    headers.put("Authorization", "Basic " + basic);
    ApiContext apiContext =
        ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    com.edgar.direwolves.core.definition.HttpEndpoint httpEndpoint =
        Endpoint.createHttp("add_device", HttpMethod.GET, "devices/", "device");
    ApiDefinition definition = ApiDefinition.create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);

    AuthenticationPlugin plugin = (AuthenticationPlugin) ApiPlugin.create(AuthenticationPlugin
        .class
        .getSimpleName());
    plugin.add("jwt").add("basic");
    apiContext.apiDefinition().addPlugin(plugin);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
        .andThen(context -> testContext.fail())
        .onFailure(throwable -> {
          testContext.assertTrue(throwable instanceof SystemException);
          SystemException ex = (SystemException) throwable;
          testContext.assertEquals(DefaultErrorCode.NO_AUTHORITY, ex.getErrorCode());
          async.complete();
        });
  }

  public String createToken(JsonObject claims) {
    JsonObject config = new JsonObject()
        .put("path", "keystore.jceks")
        .put("type", "jceks")//JKS, JCEKS, PKCS12, BKS，UBER
        .put("password", "secret")
        .put("algorithm", "HS512")
        .put("expiresInSeconds", 1800);
    JsonObject jwtConfig = new JsonObject().put("keyStore", config);
    JWTAuth provider = JWTAuth.create(vertx, jwtConfig);
    return provider.generateToken(claims, new JWTOptions(config));
  }
}
