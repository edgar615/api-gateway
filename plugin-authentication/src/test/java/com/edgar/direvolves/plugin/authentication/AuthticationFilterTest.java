package com.edgar.direvolves.plugin.authentication;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.edgar.direwolves.core.cache.RedisProvider;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiPlugin;
import com.edgar.direwolves.core.definition.HttpEndpoint;
import com.edgar.direwolves.core.definition.SimpleHttpEndpoint;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.dispatch.Filter;
import com.edgar.direwolves.core.utils.Filters;
import com.edgar.util.base.Randoms;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
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

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
@RunWith(VertxUnitRunner.class)
public class AuthticationFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  String jti = UUID.randomUUID().toString();

  RedisProvider redisProvider = new MockRedisProvider();

  JWTAuth provider;

  private Vertx vertx;

  private String userKey = UUID.randomUUID().toString();

  private String namespace = UUID.randomUUID().toString();

  private String cacheAddress = namespace + "." + RedisProvider.class.getName();

  private int userId = Integer.parseInt(Randoms.randomNumber(5));

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    ProxyHelper.registerService(RedisProvider.class, vertx, redisProvider, cacheAddress);
    filters.clear();
    JsonObject config = new JsonObject().put("keyStore", new JsonObject()
            .put("path", "keystore.jceks")
            .put("type", "jceks")
            .put("password", "secret")
    );

    provider = JWTAuth.create(vertx, config);
  }

  @Test
  public void noHeaderShouldThrowInvalidToken(TestContext testContext) {

    ApiContext apiContext = createApiContext();
    Filter filter = Filter.create(AuthenticationFilter.class.getSimpleName(),
                                  vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, Lists.newArrayList(filter))
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
  public void lackBearerShouldThrowInvalidToken(TestContext testContext) {
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "invalidtoken"),
                                             ArrayListMultimap.create());

    Filter filter = Filter.create(AuthenticationFilter.class.getSimpleName(),
                                  vertx, new JsonObject());
    filters.add(filter);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, Lists.newArrayList(filter))
            .andThen(context -> testContext.fail())
            .onFailure(throwable -> {
              testContext.assertTrue(throwable instanceof SystemException);
              SystemException ex = (SystemException) throwable;
              testContext.assertEquals(DefaultErrorCode.INVALID_TOKEN, ex.getErrorCode());
              async.complete();
            });
  }

  @Test
  public void jwtExpiredShouldThrowExpiredToken(TestContext testContext) {
    JsonObject claims = new JsonObject()
            .put(userKey, userId)
            .put("exp", System.currentTimeMillis() / 1000 - 1000 * 30);
    String token =
            provider.generateToken(claims, new JWTOptions().setAlgorithm("HS512"));
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "Bearer " + token),
                                             ArrayListMultimap.create());

    Filter filter = Filter.create(AuthenticationFilter.class.getSimpleName(),
                                  vertx, new JsonObject());
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> testContext.fail())
            .onFailure(throwable -> {
              testContext.assertTrue(throwable instanceof SystemException);
              SystemException ex = (SystemException) throwable;
              testContext.assertEquals(DefaultErrorCode.EXPIRE_TOKEN, ex.getErrorCode());
              async.complete();
            });
  }

  @Test
  public void unequalJtiShouldThrowExpiredTokenWhenRestrictedUniqueUser(TestContext testContext) {
    redisProvider.set(namespace + ":user:" + userId, new JsonObject()
            .put("userId", userId)
            .put("username", "password")
            .put("jti", jti), ar -> {

    });

    JsonObject claims = new JsonObject()
            .put(userKey, userId)
            .put("jti", UUID.randomUUID().toString());
//                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);

    String token =
            provider.generateToken(claims, new JWTOptions().setAlgorithm("HS512"));
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "Bearer " + token),
                                             ArrayListMultimap.create());

    Filter filter = Filter.create(AuthenticationFilter.class.getSimpleName(),
                                  vertx, new JsonObject()
                                          .put("token.expires", 60 * 30)
                                          .put("jwt.userClaimKey", userKey)
                                          .put("jwt.user.unique", true)
                                          .put("namespace", namespace));
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> testContext.fail())
            .onFailure(throwable -> {
              testContext.assertTrue(throwable instanceof SystemException);
              SystemException ex = (SystemException) throwable;
              testContext.assertEquals(DefaultErrorCode.EXPIRE_TOKEN, ex.getErrorCode());
              async.complete();
            });
  }

  @Test
  public void unequalJtiShouldSuccessWhenUnrestrictedUniqueUser(TestContext testContext) {
    redisProvider.set(namespace + ":user:" + userId, new JsonObject()
            .put(userKey, userId)
            .put("username", "edgar")
            .put("jti", jti), ar -> {

    });

    JsonObject claims = new JsonObject()
            .put(userKey, userId)
            .put("jti", UUID.randomUUID().toString());
//                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);

    String token =
            provider.generateToken(claims, new JWTOptions().setAlgorithm("HS512"));
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "Bearer " + token),
                                             ArrayListMultimap.create());

    Filter filter = Filter.create(AuthenticationFilter.class.getSimpleName(),
                                  vertx, new JsonObject()
                                          .put("token.expires", 60 * 30)
                                          .put("jwt.userClaimKey", userKey)
                                          .put("namespace", namespace));
    filters.add(filter);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              JsonObject principal = context.principal();
              testContext.assertEquals(userId, principal.getInteger(userKey));
              testContext.assertEquals(userId + "", principal.getValue("userId"));
              testContext.assertEquals("edgar", principal.getString("username"));
              async.complete();
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.fail();
            });
  }

  @Test
  public void equalJtiShouldSuccessWhenRestrictedUniqueUser(TestContext testContext) {
    redisProvider.set(namespace + ":user:" + userId, new JsonObject()
            .put(userKey, userId)
            .put("username", "edgar")
            .put("jti", jti), ar -> {

    });

    JsonObject claims = new JsonObject()
            .put(userKey, userId)
            .put("jti", jti);
//                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);

    String token =
            provider.generateToken(claims, new JWTOptions().setAlgorithm("HS512"));
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "Bearer " + token),
                                             ArrayListMultimap.create());

    Filter filter = Filter.create(AuthenticationFilter.class.getSimpleName(),
                                  vertx, new JsonObject()
                                          .put("token.expires", 60 * 30)
                                          .put("jwt.userClaimKey", userKey)
                                          .put("namespace", namespace)
                                          .put("jwt.user.unique", true));
    filters.add(filter);
    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              JsonObject principal = context.principal();
              testContext.assertEquals("edgar", principal.getString("username"));
              testContext.assertEquals(userId, principal.getInteger(userKey));
              testContext.assertEquals(userId + "", principal.getValue("userId"));
              async.complete();
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.fail();
            });
  }

  @Test
  public void unSavedJtiShouldThrownInvalidToken(TestContext testContext) {

    redisProvider
            .set(namespace + ":user:" + Integer.parseInt(Randoms.randomNumber(4)), new JsonObject()
                    .put("userId", userId)
                    .put("username", "password")
                    .put("jti", jti), ar -> {

            });


    JsonObject claims = new JsonObject()
            .put(userKey, userId)
            .put("jti", UUID.randomUUID().toString());
//                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);

    String token =
            provider.generateToken(claims, new JWTOptions().setAlgorithm("HS512"));
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "Bearer " + token),
                                             ArrayListMultimap.create());


    Filter filter = Filter.create(AuthenticationFilter.class.getSimpleName(),
                                  vertx, new JsonObject()
                                          .put("token.expires", 60 * 30)
                                          .put("jwt.userClaimKey", userKey)
                                          .put("namespace", namespace)
                                          .put("jwt.user.unique", true));
    filters.add(filter);

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
  public void missUserKeyShouldThrownInvalidToken(TestContext testContext) {

    redisProvider
            .set(namespace + ":user:" + Integer.parseInt(Randoms.randomNumber(4)), new JsonObject()
//                    .put("userId", userId)
                    .put("username", "password")
                    .put("jti", jti), ar -> {

            });


    JsonObject claims = new JsonObject()
            .put(userKey, userId)
            .put("jti", jti);
//                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);

    String token =
            provider.generateToken(claims, new JWTOptions().setAlgorithm("HS512"));
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "Bearer " + token),
                                             ArrayListMultimap.create());


    Filter filter = Filter.create(AuthenticationFilter.class.getSimpleName(),
                                  vertx, new JsonObject()
                                          .put("token.expires", 60 * 30)
                                          .put("jwt.userClaimKey", userKey)
                                          .put("namespace", namespace));
    filters.add(filter);

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

  private ApiContext createApiContext(Multimap<String, String> header,
                                      Multimap<String, String> params) {
    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", header,
                              params, null);
   SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/", 80, "localhost");
    ApiDefinition definition = ApiDefinition
            .create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    AuthenticationPlugin plugin = (AuthenticationPlugin) ApiPlugin.create(AuthenticationPlugin
                                                                                  .class
                                                                                  .getSimpleName());
    apiContext.apiDefinition().addPlugin(plugin);
    return apiContext;
  }

  private ApiContext createApiContext() {
    ApiContext apiContext =
            ApiContext.create(HttpMethod.GET, "/devices", ArrayListMultimap.create(),
                              ArrayListMultimap.create(), null);
    SimpleHttpEndpoint httpEndpoint =
            SimpleHttpEndpoint.http("add_device", HttpMethod.GET, "devices/", 80, "localhost");
    ApiDefinition definition = ApiDefinition
            .create("add_device", HttpMethod.GET, "devices/", Lists.newArrayList(httpEndpoint));
    apiContext.setApiDefinition(definition);
    AuthenticationPlugin plugin = (AuthenticationPlugin) ApiPlugin.create(AuthenticationPlugin
                                                                                  .class
                                                                                  .getSimpleName());
    apiContext.apiDefinition().addPlugin(plugin);
    return apiContext;
  }
}
