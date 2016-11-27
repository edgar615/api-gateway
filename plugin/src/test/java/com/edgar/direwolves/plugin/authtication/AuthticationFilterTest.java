package com.edgar.direwolves.plugin.authtication;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.Endpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.EventbusUtils;
import com.edgar.direwolves.plugin.FilterTest;
import com.edgar.direwolves.plugin.authentication.AuthenticationFilter;
import com.edgar.direwolves.plugin.authentication.AuthenticationPlugin;
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
public class AuthticationFilterTest extends FilterTest {

  private final List<Filter> filters = new ArrayList<>();
  AuthenticationFilter filter;

  private Vertx vertx;

  private String jti = UUID.randomUUID().toString();

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

    filter = new AuthenticationFilter();
    filter.config(vertx, new JsonObject().put("jwt.user.get.address", "user.get"));

    filters.clear();
    filters.add(filter);

    vertx.eventBus().<Integer>consumer("user.get", msg -> {
      int userId = msg.body();
      if (userId < 10) {
        msg.reply(new JsonObject()
            .put("userId", 1)
            .put("username", "edgar")
            .put("tel", "123456")
            .put("jti", jti));
      } else {
        EventbusUtils.fail(msg, SystemException.create(DefaultErrorCode.INVALID_TOKEN));
      }
    });
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
    doFilter(task, filters)
        .andThen(context -> testContext.fail())
        .onFailure(throwable -> {
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
    doFilter(task, filters)
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
    doFilter(task, filters)
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
    doFilter(task, filters)
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
    doFilter(task, filters)
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
