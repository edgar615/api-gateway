package com.edgar.direwolves.plugin.authentication;

import com.edgar.direwolves.core.dispatch.ApiContext;
import com.edgar.direwolves.core.utils.EventbusUtils;
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
  private String userGetAddress = UUID.randomUUID().toString();
  private String userKey = UUID.randomUUID().toString();

  @Before
  public void setUp(TestContext testContext) {
    vertx = Vertx.vertx();

    JsonObject config = new JsonObject().put("keyStore", new JsonObject()
            .put("path", "keystore.jceks")
            .put("type", "jceks")
            .put("password", "secret")
    );

    provider = JWTAuth.create(vertx, config);

//    ProxyHelper.registerService(UserService.class, vertx, new MockUserService(vertx, userGetAddress), UserService.SERVICE_ADDRESS);

    vertx.eventBus().<Integer>consumer(userGetAddress, msg -> {
      int userId = msg.body();
      if (userId < 10) {
        msg.reply(new JsonObject()
            .put(userKey, 1)
            .put("username", "edgar")
            .put("tel", "123456")
            .put("jti", jti));
      } else {
        EventbusUtils.fail(msg, SystemException.create(DefaultErrorCode.INVALID_TOKEN));
      }
    });

  }

  @Test
  public void testNoJwtHeader(TestContext testContext) {
    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", ArrayListMultimap
        .create(), null, null);

    JwtStrategy filter = new JwtStrategy();
    filter.config(vertx, new JsonObject());

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
  public void testJwtHeaderNoBearer(TestContext testContext) {
    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("Authorization", "invalidtoken");
    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", headers, null, null);

    JwtStrategy filter = new JwtStrategy();
    filter.config(vertx, new JsonObject());

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
  public void testJwt(TestContext testContext) {

    JsonObject claims = new JsonObject()
        .put(userKey, 1)
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

    JwtStrategy filter = new JwtStrategy();
    filter.config(vertx, new JsonObject()
        .put("jwt.expires", 60 * 30)
        .put("jwt.user.get.address", userGetAddress)
        .put("jwt.user.key", userKey));

    Future<JsonObject> future = Future.future();
    filter.doAuthentication(apiContext, future);

    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        JsonObject principal = ar.result();
        testContext.assertTrue(principal.containsKey("exp"));
        testContext.assertTrue(principal.containsKey("iat"));
        testContext.assertFalse(principal.containsKey("iss"));
        testContext.assertFalse(principal.containsKey("sub"));
        testContext.assertFalse(principal.containsKey("aud"));
        async.complete();
      } else {
        testContext.fail();
      }
    });
  }

  @Test
  public void testExpiredJwt(TestContext testContext) {

    JsonObject claims = new JsonObject()
        .put(userKey, 1)
        .put("companyCode", 1)
        .put("admin", false)
        .put("username", "Edgar")
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

    JwtStrategy filter = new JwtStrategy();
    filter.config(vertx, new JsonObject()
        .put("jwt.expires", 60 * 30));

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
  public void testJwtClaim(TestContext testContext) {

    JsonObject claims = new JsonObject()
        .put("jti", jti)
        .put(userKey, 1);
//                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);

//    JwtTokenGenerator jwtTokenGenerator = new JwtTokenGenerator();
//    jwtTokenGenerator.config(vertx, new JsonObject()
//            .put("jwt.audience", "csst")
//            .put("jwt.subject", "iotp")
//            .put("jwt.issuer", "edgar"));
    String token = createToken2(claims);
    System.out.println(token);

    String[] tokens = token.split("\\.");
    String claim = tokens[1];
    String claimJson = new String(Base64.getDecoder().decode(claim));
    System.out.println(claimJson);

    Multimap<String, String> headers = ArrayListMultimap.create();
    headers.put("Authorization", "Bearer " + token);
    ApiContext apiContext = ApiContext.create(HttpMethod.GET, "/devices", headers, null, null);

    JwtStrategy filter = new JwtStrategy();
    filter.config(vertx, new JsonObject()
        .put("jwt.expires", 60 * 30).put("jwt.user.get.address", userGetAddress).put("jwt.user.key", userKey));

    Future<JsonObject> future = Future.future();
    filter.doAuthentication(apiContext, future);

    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        JsonObject principal = ar.result();
        testContext.assertTrue(principal.containsKey("exp"));
        testContext.assertTrue(principal.containsKey("iat"));
        testContext.assertEquals("edgar", principal.getString("iss"));
        testContext.assertEquals("iotp", principal.getString("sub"));
        testContext.assertEquals("csst", principal.getString("aud"));
        async.complete();
      } else {
        testContext.fail();
      }
    });
  }

  @Test
  public void testUnkownUser(TestContext testContext) {

    JsonObject claims = new JsonObject()
        .put("jti", jti)
        .put(userKey, 10);
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

    JwtStrategy filter = new JwtStrategy();
    filter.config(vertx, new JsonObject()
        .put("jwt.expires", 60 * 30)
        .put("jwt.user.get.address", userGetAddress)
        .put("jwt.user.key", userKey));

    Future<JsonObject> future = Future.future();
    filter.doAuthentication(apiContext, future);

    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        testContext.fail();
      } else {
        async.complete();
      }
    });
  }

  @Test
  public void testUnkownJti(TestContext testContext) {

    JsonObject claims = new JsonObject()
        .put("jti", UUID.randomUUID().toString())
        .put(userKey, 10);
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

    JwtStrategy filter = new JwtStrategy();
    filter.config(vertx, new JsonObject()
        .put("jwt.expires", 60 * 30)
        .put("jwt.user.get.address", "user.get"));

    Future<JsonObject> future = Future.future();
    filter.doAuthentication(apiContext, future);

    Async async = testContext.async();
    future.setHandler(ar -> {
      if (ar.succeeded()) {
        testContext.fail();
      } else {
        SystemException ex = (SystemException) ar.cause();
        testContext.assertEquals(DefaultErrorCode.INVALID_TOKEN, ex.getErrorCode());
        async.complete();
      }
    });
  }

  public String createToken2(JsonObject claims) {
    JsonObject config = new JsonObject()
        .put("path", "keystore.jceks")
        .put("type", "jceks")//JKS, JCEKS, PKCS12, BKS，UBER
        .put("password", "secret")
        .put("algorithm", "HS512")
        .put("expiresInSeconds", 1800)
        .put("audience", "csst")
        .put("subject", "iotp")
        .put("issuer", "edgar");

    JsonObject jwtConfig = new JsonObject().put("keyStore", config);
    JWTAuth provider = JWTAuth.create(vertx, jwtConfig);
    return provider.generateToken(claims, new JWTOptions(config));
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
