package com.edgar.direwolves.plugin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.utils.JsonUtils;
import com.edgar.direwolves.plugin.authentication.AuthenticationFilter;
import com.edgar.direwolves.plugin.authentication.AuthenticationPlugin;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import io.vertx.core.Future;
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

import java.util.Base64;

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
@RunWith(VertxUnitRunner.class)
public class AuthticationFilterTest {

  Vertx vertx;

  AuthenticationFilter filter;

  @Before
  public void setUp(TestContext testContext) {
    vertx = Vertx.vertx();
    filter = new AuthenticationFilter();
    filter.config(vertx, new JsonObject());
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
    ApiDefinition definition =
            ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add.json"));
    apiContext.setApiDefinition(definition);

    AuthenticationPlugin plugin = (AuthenticationPlugin) ApiPlugin.create(AuthenticationPlugin
                                                                                  .class
                                                                                  .getSimpleName());
    plugin.add("jwt").add("basic");
    apiContext.apiDefinition().addPlugin(plugin);

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);

    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        testContext.fail();
      } else {
        Throwable throwable = ar.cause();
        testContext.assertTrue(throwable instanceof SystemException);
        SystemException ex = (SystemException) throwable;
        testContext.assertEquals(DefaultErrorCode.INVALID_TOKEN, ex.getErrorCode());
        async.complete();
      }
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
            .put("companyCode", 1)
            .put("admin", false)
            .put("username", "Edgar");
    headers.put("Authorization", "Bearer " + createToken(claims));
    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", headers, params, null);
    ApiDefinition definition =
            ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add.json"));
    apiContext.setApiDefinition(definition);

    AuthenticationPlugin plugin = (AuthenticationPlugin) ApiPlugin.create(AuthenticationPlugin
                                                                                  .class
                                                                                  .getSimpleName());
    plugin.add("jwt").add("basic");
    apiContext.apiDefinition().addPlugin(plugin);

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);

    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        ApiContext apiContext1 = ar.result();
        JsonObject principal = apiContext1.principal();
        testContext.assertEquals("Edgar", principal.getString("username"));
        async.complete();
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });

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
    ApiDefinition definition =
            ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add.json"));
    apiContext.setApiDefinition(definition);

    AuthenticationPlugin plugin = (AuthenticationPlugin) ApiPlugin.create(AuthenticationPlugin
                                                                                  .class
                                                                                  .getSimpleName());
    plugin.add("jwt").add("basic");
    apiContext.apiDefinition().addPlugin(plugin);

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);

    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        ApiContext apiContext1 = ar.result();
        JsonObject principal = apiContext1.principal();
        testContext.assertEquals("edgar", principal.getString("username"));
        testContext.assertEquals("super", principal.getString("role"));
        async.complete();
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });

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
    ApiDefinition definition =
            ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add.json"));
    apiContext.setApiDefinition(definition);

    AuthenticationPlugin plugin = (AuthenticationPlugin) ApiPlugin.create(AuthenticationPlugin
                                                                                  .class
                                                                                  .getSimpleName());
    plugin.add("jwt").add("basic");
    apiContext.apiDefinition().addPlugin(plugin);

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);

    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        testContext.fail();
      } else {
        Throwable throwable = ar.cause();
        testContext.assertTrue(throwable instanceof SystemException);
        SystemException ex = (SystemException) throwable;
        testContext.assertEquals(DefaultErrorCode.INVALID_TOKEN, ex.getErrorCode());
        async.complete();
      }
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
    ApiDefinition definition =
            ApiDefinition.fromJson(JsonUtils.getJsonFromFile("src/test/resources/device_add.json"));
    apiContext.setApiDefinition(definition);

    AuthenticationPlugin plugin = (AuthenticationPlugin) ApiPlugin.create(AuthenticationPlugin
                                                                                  .class
                                                                                  .getSimpleName());
    plugin.add("jwt").add("basic");
    apiContext.apiDefinition().addPlugin(plugin);

    Future<ApiContext> future = Future.future();
    filter.doFilter(apiContext, future);

    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        testContext.fail();
      } else {
        Throwable throwable = ar.cause();
        testContext.assertTrue(throwable instanceof SystemException);
        SystemException ex = (SystemException) throwable;
        testContext.assertEquals(DefaultErrorCode.NO_AUTHORITY, ex.getErrorCode());
        async.complete();
      }
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
