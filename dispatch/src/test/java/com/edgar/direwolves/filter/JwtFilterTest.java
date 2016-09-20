package com.edgar.direwolves.filter;

import com.edgar.direwolves.dispatch.ApiContext;
import com.edgar.direwolves.dispatch.JwtTokenGenerator;
import com.edgar.direwolves.filter.JWTFilter;
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

/**
 * Created by Edgar on 2016/9/20.
 *
 * @author Edgar  Date 2016/9/20
 */
@RunWith(VertxUnitRunner.class)
public class JwtFilterTest {

    Vertx vertx;

    JWTAuth provider;

    @Before
    public void setUp(TestContext testContext) {
        vertx = Vertx.vertx();

        JsonObject config = new JsonObject().put("keyStore", new JsonObject()
                .put("path", "keystore.jceks")
                .put("type", "jceks")
                .put("password", "secret"));

        provider = JWTAuth.create(vertx, config);

    }

    @Test
    public void testNoJwtHeader() {
        ApiContext apiContext = ApiContext.builder()
                .setMethod(HttpMethod.GET)
                .setPath("/devices")
                .setHeaders(ArrayListMultimap.create())
                .build();

        JWTFilter filter = new JWTFilter();
        filter.config(vertx, new JsonObject());

        Future<ApiContext> future = Future.future();
        try {
            filter.doFilter(apiContext, future);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof SystemException);
            SystemException ex = (SystemException) e;
            Assert.assertEquals(1021, ex.getErrorCode().getNumber());
        }
    }

    @Test
    public void testJwtHeaderNoBearer() {
        Multimap<String, String> headers = ArrayListMultimap.create();
        headers.put("Authorization", "invalidtoken");
        ApiContext apiContext = ApiContext.builder()
                .setMethod(HttpMethod.GET)
                .setPath("/devices")
                .setHeaders(headers)
                .build();

        JWTFilter filter = new JWTFilter();
        filter.config(vertx, new JsonObject());

        Future<ApiContext> future = Future.future();
        try {
            filter.doFilter(apiContext, future);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof SystemException);
            SystemException ex = (SystemException) e;
            Assert.assertEquals(1021, ex.getErrorCode().getNumber());
        }
    }

    @Test
    public void testJwt(TestContext testContext) {

        JsonObject claims = new JsonObject()
                .put("userId", 1)
                .put("companyCode", 1)
                .put("admin", false)
                .put("username", "Edgar");
//                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);

        JwtTokenGenerator jwtTokenGenerator = new JwtTokenGenerator();
        jwtTokenGenerator.config(vertx, new JsonObject());
        String token = jwtTokenGenerator.createToken(claims);
        System.out.println(token);

        String[] tokens = token.split("\\.");
        String claim = tokens[1];
        String claimJson = new String(Base64.getDecoder().decode(claim));
        System.out.println(claimJson);

        Multimap<String, String> headers = ArrayListMultimap.create();
        headers.put("Authorization", "Bearer " + token);
        ApiContext apiContext = ApiContext.builder()
                .setMethod(HttpMethod.GET)
                .setPath("/devices")
                .setHeaders(headers)
                .build();

        JWTFilter filter = new JWTFilter();
        filter.config(vertx, new JsonObject()
                .put("jwt.expires", 60 * 30));

        Future<ApiContext> future = Future.future();
        filter.doFilter(apiContext, future);

        Async async = testContext.async();
        future.setHandler(ar -> {
            if (ar.succeeded()) {
                ApiContext apiContext1 = ar.result();
                testContext.assertNotNull(apiContext1);
                JsonObject principal = apiContext1.principal();
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
                .put("userId", 1)
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
        ApiContext apiContext = ApiContext.builder()
                .setMethod(HttpMethod.GET)
                .setPath("/devices")
                .setHeaders(headers)
                .build();

        JWTFilter filter = new JWTFilter();
        filter.config(vertx, new JsonObject()
                .put("jwt.expires", 60 * 30));

        Future<ApiContext> future = Future.future();
        filter.doFilter(apiContext, future);

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
                .put("userId", 1)
                .put("companyCode", 1)
                .put("admin", false)
                .put("username", "Edgar");
//                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);

        JwtTokenGenerator jwtTokenGenerator = new JwtTokenGenerator();
        jwtTokenGenerator.config(vertx, new JsonObject()
                .put("jwt.audience", "csst")
                .put("jwt.subject", "iotp")
                .put("jwt.issuer", "edgar"));
        String token = jwtTokenGenerator.createToken(claims);
        System.out.println(token);

        String[] tokens = token.split("\\.");
        String claim = tokens[1];
        String claimJson = new String(Base64.getDecoder().decode(claim));
        System.out.println(claimJson);

        Multimap<String, String> headers = ArrayListMultimap.create();
        headers.put("Authorization", "Bearer " + token);
        ApiContext apiContext = ApiContext.builder()
                .setMethod(HttpMethod.GET)
                .setPath("/devices")
                .setHeaders(headers)
                .build();

        JWTFilter filter = new JWTFilter();
        filter.config(vertx, new JsonObject()
                .put("jwt.expires", 60 * 30));

        Future<ApiContext> future = Future.future();
        filter.doFilter(apiContext, future);

        Async async = testContext.async();
        future.setHandler(ar -> {
            if (ar.succeeded()) {
                ApiContext apiContext1 = ar.result();
                testContext.assertNotNull(apiContext1);
                JsonObject principal = apiContext1.principal();
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
}
