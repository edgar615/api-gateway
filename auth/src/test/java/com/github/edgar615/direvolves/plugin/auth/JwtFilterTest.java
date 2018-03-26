package com.github.edgar615.direvolves.plugin.auth;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import com.github.edgar615.direwolves.core.definition.ApiDefinition;
import com.github.edgar615.direwolves.core.definition.ApiPlugin;
import com.github.edgar615.direwolves.core.definition.SimpleHttpEndpoint;
import com.github.edgar615.direwolves.core.dispatch.ApiContext;
import com.github.edgar615.direwolves.core.dispatch.Filter;
import com.github.edgar615.direwolves.core.utils.Filters;
import com.github.edgar615.util.base.Randoms;
import com.github.edgar615.util.exception.DefaultErrorCode;
import com.github.edgar615.util.exception.SystemException;
import com.github.edgar615.util.vertx.task.Task;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
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

/**
 * Created by Edgar on 2016/10/31.
 *
 * @author Edgar  Date 2016/10/31
 */
@RunWith(VertxUnitRunner.class)
public class JwtFilterTest {

  private final List<Filter> filters = new ArrayList<>();

  String jti = UUID.randomUUID().toString();

  JWTAuth provider;

  private Vertx vertx;

  private String namespace = UUID.randomUUID().toString();

  private int userId = Integer.parseInt(Randoms.randomNumber(5));

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    filters.clear();
    KeyStoreOptions keyStoreOptions = new KeyStoreOptions()
            .setPath("keystore.jceks")
            .setType("jceks")
            .setPassword("INIHPMOZPO");

    provider = JWTAuth.create(vertx, new JWTAuthOptions().setKeyStore(keyStoreOptions));
  }

  @Test
  public void noHeaderShouldThrowInvalidToken(TestContext testContext) {

    ApiContext apiContext = createApiContext();
    Filter filter = Filter.create(JwtFilter.class.getSimpleName(),
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
              testContext.assertEquals(DefaultErrorCode.INVALID_REQ, ex.getErrorCode());
              async.complete();
            });
  }

  @Test
  public void lackBearerShouldThrowInvalidToken(TestContext testContext) {
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "invalidtoken"),
                                             ArrayListMultimap.create());

    Filter filter = Filter.create(JwtFilter.class.getSimpleName(),
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
  public void jwtExpiredShouldThrowExpiredToken(TestContext testContext) {
    JsonObject claims = new JsonObject()
            .put("userId", userId);
    String token =
            provider.generateToken(claims, new JWTOptions().setExpiresInSeconds(-1000l * 30));
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "Bearer " + token),
                                             ArrayListMultimap.create());

    Filter filter = Filter.create(JwtFilter.class.getSimpleName(),
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
  public void ignoreExpShouldSuccess(TestContext testContext) {
    JsonObject claims = new JsonObject()
            .put("userId", userId)
            .put("jti", jti);
    String token =
            provider.generateToken(claims, new JWTOptions().setExpiresInSeconds(-1000l * 30));
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "Bearer " + token),
                                             ArrayListMultimap.create());

    JsonObject jwtConfig = new JsonObject()
            .put("ignoreExpiration", true);
    Filter filter = Filter.create(JwtFilter.class.getSimpleName(),
                                  vertx, new JsonObject()
                                          .put("jwt.auth", jwtConfig));
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              JsonObject principal = context.principal();
              System.out.println(principal);
              testContext.assertEquals(userId, principal.getInteger("userId"));
              testContext.assertEquals(jti, principal.getString("jti"));
              async.complete();
            })
            .onFailure(throwable -> {
              testContext.fail();
            });
  }

  @Test
  public void leewayShouldSuccess(TestContext testContext) {
    JsonObject claims = new JsonObject()
            .put("userId", userId)
            .put("jti", jti);
    String token =
            provider.generateToken(claims, new JWTOptions().setExpiresInSeconds(-30l));
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "Bearer " + token),
                                             ArrayListMultimap.create());

    JsonObject jwtConfig = new JsonObject()
            .put("leeway", 50);
    Filter filter = Filter.create(JwtFilter.class.getSimpleName(),
                                  vertx, new JsonObject()
                                          .put("jwt.auth", jwtConfig));
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              JsonObject principal = context.principal();
              System.out.println(principal);
              testContext.assertEquals(userId, principal.getInteger("userId"));
              testContext.assertEquals(jti, principal.getString("jti"));
              async.complete();
            })
            .onFailure(throwable -> {
              testContext.fail();
            });
  }

  @Test
  public void leewayThrowExpiredToken(TestContext testContext) {
    JsonObject claims = new JsonObject()
            .put("userId", userId);
    String token =
            provider.generateToken(claims, new JWTOptions().setExpiresInSeconds(-30l));
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "Bearer " + token),
                                             ArrayListMultimap.create());

    JsonObject jwtConfig  = new JsonObject()
            .put("leeway", 10);
    Filter filter = Filter.create(JwtFilter.class.getSimpleName(),
                                  vertx, new JsonObject()
                                          .put("jwt.auth",jwtConfig));
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
  public void testAuthSuccess(TestContext testContext) {

    JsonObject claims = new JsonObject()
            .put("userId", userId)
            .put("jti", jti);
//                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);

    String token =
            provider.generateToken(claims, new JWTOptions());
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "Bearer " + token),
                                             ArrayListMultimap.create());


    Filter filter = Filter.create(JwtFilter.class.getSimpleName(),
                                  vertx, new JsonObject()
                                          .put("jwt",
                                               new JsonObject().put("expiresInSeconds", 60 * 30))
                                          .put("user", new JsonObject())
                                          .put("namespace", namespace));
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              JsonObject principal = context.principal();
              System.out.println(principal);
              testContext.assertEquals(userId, principal.getInteger("userId"));
              testContext.assertEquals(jti, principal.getString("jti"));
              testContext.assertFalse(principal.containsKey("username"));
              async.complete();
            })
            .onFailure(throwable -> {
              testContext.fail();
            });
  }

  @Test
  public void missUserIdShouldThrownInvalidToken(TestContext testContext) {

    JsonObject claims = new JsonObject()
            .put(UUID.randomUUID().toString(), userId)
            .put("jti", jti);
//                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);

    String token =
            provider.generateToken(claims, new JWTOptions());
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "Bearer " + token),
                                             ArrayListMultimap.create());


    Filter filter = Filter.create(JwtFilter.class.getSimpleName(),
                                  vertx, new JsonObject());
    filters.add(filter);

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
  public void invalidIssShouldThrownInvalidToken(TestContext testContext) {

    JsonObject claims = new JsonObject()
            .put("userId", userId)
            .put("jti", jti);
//                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);

    String token =
            provider.generateToken(claims,
                                   new JWTOptions().setIssuer(UUID.randomUUID().toString()));
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "Bearer " + token),
                                             ArrayListMultimap.create());


    JsonObject jwtConfig = new JsonObject()
            .put("issuer", "edgar615");
    Filter filter = Filter.create(JwtFilter.class.getSimpleName(),
                                  vertx, new JsonObject()
                                          .put("jwt.auth", jwtConfig));
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.assertTrue(throwable instanceof SystemException);
              SystemException ex = (SystemException) throwable;
              testContext.assertEquals(DefaultErrorCode.INVALID_TOKEN, ex.getErrorCode());
              async.complete();
            });
  }

  @Test
  public void ValidIssShouldSuccess(TestContext testContext) {

    JsonObject claims = new JsonObject()
            .put("userId", userId)
            .put("jti", jti);
//                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);

    String iss = UUID.randomUUID().toString();
    String token =
            provider.generateToken(claims, new JWTOptions().setIssuer(iss));
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "Bearer " + token),
                                             ArrayListMultimap.create());


    JsonObject jwtConfig = new JsonObject()
            .put("issuer", iss);
    Filter filter = Filter.create(JwtFilter.class.getSimpleName(),
                                  vertx, new JsonObject()
                                          .put("jwt.auth", jwtConfig));
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              JsonObject principal = context.principal();
              System.out.println(principal);
              testContext.assertEquals(userId, principal.getInteger("userId"));
              testContext.assertEquals(jti, principal.getString("jti"));
              async.complete();
            })
            .onFailure(throwable -> {
              testContext.fail();
            });
  }

  @Test
  public void invalidAudShouldThrownInvalidToken(TestContext testContext) {

    JsonObject claims = new JsonObject()
            .put("userId", userId)
            .put("jti", jti);
//                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);

    String token =
            provider.generateToken(claims,
                                   new JWTOptions().addAudience(UUID.randomUUID().toString()));
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "Bearer " + token),
                                             ArrayListMultimap.create());


    JsonObject jwtConfig = new JsonObject()
            .put("audiences", new JsonArray().add("app"));
    Filter filter = Filter.create(JwtFilter.class.getSimpleName(),
                                  vertx, new JsonObject()
                                          .put("jwt.auth", jwtConfig));
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              testContext.fail();
            })
            .onFailure(throwable -> {
              throwable.printStackTrace();
              testContext.assertTrue(throwable instanceof SystemException);
              SystemException ex = (SystemException) throwable;
              testContext.assertEquals(DefaultErrorCode.INVALID_TOKEN, ex.getErrorCode());
              async.complete();
            });
  }

  @Test
  public void ValidAudShouldSuccess(TestContext testContext) {

    JsonObject claims = new JsonObject()
            .put("userId", userId)
            .put("jti", jti);
//                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);

    String aud = UUID.randomUUID().toString();
    String token =
            provider.generateToken(claims, new JWTOptions().addAudience(aud));
    ApiContext apiContext = createApiContext(ImmutableMultimap.of("Authorization",
                                                                  "Bearer " + token),
                                             ArrayListMultimap.create());


    JsonObject jwtConfig = new JsonObject()
            .put("audiences", new JsonArray().add(aud));
    Filter filter = Filter.create(JwtFilter.class.getSimpleName(),
                                  vertx, new JsonObject()
                                          .put("jwt.auth", jwtConfig));
    filters.add(filter);

    Task<ApiContext> task = Task.create();
    task.complete(apiContext);
    Async async = testContext.async();
    Filters.doFilter(task, filters)
            .andThen(context -> {
              JsonObject principal = context.principal();
              System.out.println(principal);
              testContext.assertEquals(userId, principal.getInteger("userId"));
              testContext.assertEquals(jti, principal.getString("jti"));
              async.complete();
            })
            .onFailure(throwable -> {
              testContext.fail();
            });
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
    JwtPlugin plugin = (JwtPlugin) ApiPlugin.create(JwtPlugin
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
    JwtPlugin plugin = (JwtPlugin) ApiPlugin.create(JwtPlugin
                                                                                  .class
                                                                                  .getSimpleName());
    apiContext.apiDefinition().addPlugin(plugin);
    return apiContext;
  }
}
