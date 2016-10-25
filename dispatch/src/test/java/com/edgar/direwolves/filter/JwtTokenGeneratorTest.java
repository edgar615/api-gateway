//package com.edgar.direwolves.filter;
//
//import com.edgar.direwolves.dispatch.JwtTokenGenerator;
//import io.vertx.core.Vertx;
//import io.vertx.core.json.JsonObject;
//import io.vertx.ext.unit.TestContext;
//import io.vertx.ext.unit.junit.VertxUnitRunner;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.util.Base64;
//
///**
// * Created by Edgar on 2016/9/20.
// *
// * @author Edgar  Date 2016/9/20
// */
//@RunWith(VertxUnitRunner.class)
//public class JwtTokenGeneratorTest {
//
//    Vertx vertx;
//
//    @Before
//    public void setUp(TestContext testContext) {
//        vertx = Vertx.vertx();
//
//    }
//
//    @Test
//    public void testJwt(TestContext testContext) {
//
//        JsonObject claims = new JsonObject()
//                .put("userId", 1)
//                .put("companyCode", 1)
//                .put("admin", false)
//                .put("username", "Edgar");
////                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);
//
//        JwtTokenGenerator jwtTokenGenerator = new JwtTokenGenerator();
//        jwtTokenGenerator.config(vertx, new JsonObject());
//        String token = jwtTokenGenerator.createToken(claims);
//        System.out.println(token);
//
//        String[] tokens = token.split("\\.");
//        String claim = tokens[1];
//        String claimJson = new String(Base64.getDecoder().decode(claim));
//        System.out.println(claimJson);
//        JsonObject principal = new JsonObject(claimJson);
//        Assert.assertTrue(principal.containsKey("exp"));
//        Assert.assertTrue(principal.containsKey("iat"));
//        Assert.assertFalse(principal.containsKey("iss"));
//        Assert.assertFalse(principal.containsKey("sub"));
//        Assert.assertFalse(principal.containsKey("aud"));
//    }
//
//    @Test
//    public void testJwtClaim(TestContext testContext) {
//
//        JsonObject claims = new JsonObject()
//                .put("userId", 1)
//                .put("companyCode", 1)
//                .put("admin", false)
//                .put("username", "Edgar");
////                .put("exp", System.currentTimeMillis() / 1000 + 1000 * 30);
//
//        JwtTokenGenerator jwtTokenGenerator = new JwtTokenGenerator();
//        jwtTokenGenerator.config(vertx, new JsonObject()
//                .put("jwt.audience", "csst")
//                .put("jwt.subject", "iotp")
//                .put("jwt.issuer", "edgar"));
//        String token = jwtTokenGenerator.createToken(claims);
//        System.out.println(token);
//
//        String[] tokens = token.split("\\.");
//        String claim = tokens[1];
//        String claimJson = new String(Base64.getDecoder().decode(claim));
//        System.out.println(claimJson);
//
//        JsonObject principal = new JsonObject(claimJson);
//        Assert.assertTrue(principal.containsKey("exp"));
//        Assert.assertTrue(principal.containsKey("iat"));
//        Assert.assertEquals("edgar", principal.getString("iss"));
//        Assert.assertEquals("iotp", principal.getString("sub"));
//        Assert.assertEquals("csst", principal.getString("aud"));
//    }
//}
