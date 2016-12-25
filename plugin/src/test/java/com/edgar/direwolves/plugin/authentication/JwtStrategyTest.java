package com.edgar.direwolves.plugin.authentication;

import com.edgar.direwolves.core.cache.CacheProvider;
import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.plugin.MockCacheProvider;
import com.edgar.util.base.Randoms;
import com.edgar.util.exception.DefaultErrorCode;
import com.edgar.util.exception.SystemException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Base64;
import java.util.UUID;

/**
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
@RunWith(VertxUnitRunner.class)
public class JwtStrategyTest {

  Vertx vertx;

  JWTAuth provider;

  String jti = UUID.randomUUID().toString();

  CacheProvider cacheProvider = new MockCacheProvider();

  private String userKey = UUID.randomUUID().toString();

  private String cacheAddress = UUID.randomUUID().toString();

  private String namespace = UUID.randomUUID().toString();

  private int userId = Integer.parseInt(Randoms.randomNumber(5));

  @Before
  public void setUp(TestContext testContext) {
    vertx = Vertx.vertx();

    ProxyHelper.registerService(CacheProvider.class, vertx, cacheProvider, cacheAddress);

    JsonObject config = new JsonObject().put("keyStore", new JsonObject()
            .put("path", "keystore.jceks")
            .put("type", "jceks")
            .put("password", "secret")
    );

    provider = JWTAuth.create(vertx, config);

  }

  @Test
  public void noHeaderShouldThrowInvalidToken(TestContext testContext) {
    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", ArrayListMultimap
            .create(), null, null);

    JwtStrategy filter = new JwtStrategy(vertx, new JsonObject());

    Future<JsonObject> future = Future.future();
    filter.doAuthentication(apiContext, future);
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        testContext.fail();
      } else {
        Throwable throwable = ar.cause();
        testContext.assertTrue(throwable instanceof SystemException);
        SystemException ex = (SystemException) throwable;
        testContext.assertEquals(DefaultErrorCode.INVALID_TOKEN, ex.getErrorCode());

      }
    });
  }

  @Test
  public void lackBearerShouldThrowInvalidToken(TestContext testContext) {
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("Authorization", "invalidtoken");
    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", headers, null, null);

    JwtStrategy filter = new JwtStrategy(vertx, new JsonObject());

    Future<JsonObject> future = Future.future();
    filter.doAuthentication(apiContext, future);
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        testContext.fail();
      } else {
        Throwable throwable = ar.cause();
        testContext.assertTrue(throwable instanceof SystemException);
        SystemException ex = (SystemException) throwable;
        testContext.assertEquals(DefaultErrorCode.INVALID_TOKEN, ex.getErrorCode());
      }
    });
  }

  @Test
  public void jwtExpiredShouldThrowExpiredToken(TestContext testContext) {
//    cacheProvider.set(namespace + ":user:" + userId, new JsonObject()
//            .put("userId", userId)
//            .put("username", "password")
//            .put("jti", jti), ar -> {
//
//    });
    JsonObject claims = new JsonObject()
            .put(userKey, userId)
            .put("exp", System.currentTimeMillis() / 1000 - 1000 * 30);

    String token =
            provider.generateToken(claims, new JWTOptions().setAlgorithm("HS512"));
    System.out.println(token);

    String[] tokens = token.split("\\.");
    String claim = tokens[1];
    String claimJson = new String(Base64.getDecoder().decode(claim));
    System.out.println(claimJson);

    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("Authorization", "Bearer " + token);
    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", headers, null, null);

    JwtStrategy filter = new JwtStrategy(vertx, new JsonObject()
            .put("jwt.expires", 60 * 30)
            .put("jwt.userClaimKey", userKey)
            .put("service.cache.address", cacheAddress)
            .put("project.namespace", namespace));

    Future<JsonObject> future = Future.future();
    filter.doAuthentication(apiContext, future);

    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        testContext.fail();
      } else {
        Throwable throwable = ar.cause();
        Assert.assertTrue(throwable instanceof SystemException);
        SystemException ex = (SystemException) throwable;
        Assert.assertEquals(1005, ex.getErrorCode().getNumber());
        async.complete();
      }
    });
  }

  @Test
  public void unequalJtiShouldThrowExpiredTokenWhenRestrictedUniqueUser(TestContext testContext) {
    cacheProvider.set(namespace + ":user:" + userId, new JsonObject()
            .put("userId", userId)
            .put("username", "password")
            .put("jti", jti), ar -> {

    });

    JsonObject claims = new JsonObject()
            .put(userKey, userId)
            .put("jti", UUID.randomUUID().toString());
//                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);

    String token = createToken(claims);
    System.out.println(token);

    String[] tokens = token.split("\\.");
    String claim = tokens[1];
    String claimJson = new String(Base64.getDecoder().decode(claim));
    System.out.println(claimJson);

    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("Authorization", "Bearer " + token);
    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", headers, null, null);

    JwtStrategy filter = new JwtStrategy(vertx, new JsonObject()
            .put("jwt.expires", 60 * 30)
            .put("jwt.userClaimKey", userKey)
            .put("jwt.user.unique", true)
            .put("service.cache.address", cacheAddress)
            .put("project.namespace", namespace));

    Future<JsonObject> future = Future.future();
    filter.doAuthentication(apiContext, future);

    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        testContext.fail();
      } else {
        Throwable throwable = ar.cause();
        Assert.assertTrue(throwable instanceof SystemException);
        SystemException ex = (SystemException) throwable;
        Assert.assertEquals(DefaultErrorCode.EXPIRE_TOKEN, ex.getErrorCode());
        async.complete();
      }
    });
  }

  @Test
  public void unequalJtiShouldSuccessWhenUnrestrictedUniqueUser(TestContext testContext) {
    cacheProvider.set(namespace + ":user:" + userId, new JsonObject()
            .put("userId", userId)
            .put("username", "password")
            .put("jti", jti), ar -> {

    });


    JsonObject claims = new JsonObject()
            .put(userKey, userId)
            .put("jti", UUID.randomUUID().toString());
//                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);

    String token = createToken(claims);
    System.out.println(token);

    String[] tokens = token.split("\\.");
    String claim = tokens[1];
    String claimJson = new String(Base64.getDecoder().decode(claim));
    System.out.println(claimJson);

    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("Authorization", "Bearer " + token);
    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", headers, null, null);

    JwtStrategy filter = new JwtStrategy(vertx, new JsonObject()
            .put("jwt.expires", 60 * 30)
            .put("jwt.userClaimKey", userKey)
            .put("jwt.user.unique", false)
            .put("service.cache.address", cacheAddress)
            .put("project.namespace", namespace));

    Future<JsonObject> future = Future.future();
    filter.doAuthentication(apiContext, future);

    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        JsonObject principal = ar.result();
        testContext.assertTrue(principal.containsKey("username"));
        testContext.assertFalse(principal.containsKey("exp"));
        testContext.assertFalse(principal.containsKey("iat"));
        testContext.assertFalse(principal.containsKey("iss"));
        testContext.assertFalse(principal.containsKey("sub"));
        testContext.assertFalse(principal.containsKey("aud"));
        async.complete();
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
  }

  @Test
  public void equalJtiShouldSuccessWhenRestrictedUniqueUser(TestContext testContext) {
    cacheProvider.set(namespace + ":user:" + userId, new JsonObject()
            .put("userId", userId)
            .put("username", "password")
            .put("jti", jti), ar -> {

    });

    JsonObject claims = new JsonObject()
            .put(userKey, userId)
            .put("jti", UUID.randomUUID().toString());
//                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);

    String token = createToken(claims);
    System.out.println(token);

    String[] tokens = token.split("\\.");
    String claim = tokens[1];
    String claimJson = new String(Base64.getDecoder().decode(claim));
    System.out.println(claimJson);

    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("Authorization", "Bearer " + token);
    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", headers, null, null);

    JwtStrategy filter = new JwtStrategy(vertx, new JsonObject()
            .put("jwt.expires", 60 * 30)
            .put("jwt.userClaimKey", userKey)
            .put("jwt.user.unique", false)
            .put("service.cache.address", cacheAddress)
            .put("project.namespace", namespace));

    Future<JsonObject> future = Future.future();
    filter.doAuthentication(apiContext, future);

    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        JsonObject principal = ar.result();
        testContext.assertTrue(principal.containsKey("username"));
        testContext.assertFalse(principal.containsKey("exp"));
        testContext.assertFalse(principal.containsKey("iat"));
        testContext.assertFalse(principal.containsKey("iss"));
        testContext.assertFalse(principal.containsKey("sub"));
        testContext.assertFalse(principal.containsKey("aud"));
        async.complete();
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
  }

  @Test
  public void unSavedJtiShouldThrownInvalidToken(TestContext testContext) {

    cacheProvider
            .set(namespace + ":user:" + Integer.parseInt(Randoms.randomNumber(4)), new JsonObject()
                    .put("userId", userId)
                    .put("username", "password")
                    .put("jti", jti), ar -> {

            });


    JsonObject claims = new JsonObject()
            .put(userKey, userId)
            .put("jti", jti);
//                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);

    String token = createToken(claims);
    System.out.println(token);

    String[] tokens = token.split("\\.");
    String claim = tokens[1];
    String claimJson = new String(Base64.getDecoder().decode(claim));
    System.out.println(claimJson);

    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("Authorization", "Bearer " + token);
    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", headers, null, null);

    JwtStrategy filter = new JwtStrategy(vertx, new JsonObject()
            .put("jwt.expires", 60 * 30)
            .put("jwt.userClaimKey", userKey)
            .put("jwt.user.unique", true)
            .put("service.cache.address", cacheAddress)
            .put("project.namespace", namespace));

    Future<JsonObject> future = Future.future();
    filter.doAuthentication(apiContext, future);

    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        testContext.fail();
      } else {
        Throwable throwable = ar.cause();
        Assert.assertTrue(throwable instanceof SystemException);
        SystemException ex = (SystemException) throwable;
        Assert.assertEquals(DefaultErrorCode.INVALID_TOKEN, ex.getErrorCode());
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
