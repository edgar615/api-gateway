package com.github.edgar615.direwolves.test.apifinder;

import com.github.edgar615.util.exception.DefaultErrorCode;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/2/7.
 *
 * @author Edgar  Date 2017/2/7
 */
@RunWith(VertxUnitRunner.class)
public class GrayTest {

  @Test
  public void testFloorMatch(TestContext testContext) {
    AtomicBoolean check = new AtomicBoolean();
    Vertx.vertx().createHttpClient().get(9000, "localhost", "/gray")
            .handler(resp -> {
              testContext.assertEquals(200, resp.statusCode());
              testContext.assertTrue(resp.headers().contains("x-request-id"));
              resp.bodyHandler(body -> {
                System.out.println(body.toString());
                testContext.assertEquals("20171024",
                                         body.toJsonObject().getString("version"));
                check.set(true);
              });
            })
            .setChunked(true)
            .end();
    Awaitility.await().until(() -> check.get());
  }

  @Test
  public void testMatch20171024(TestContext testContext) {
    AtomicBoolean check = new AtomicBoolean();
    Vertx.vertx().createHttpClient().get(9000, "localhost", "/gray")
            .handler(resp -> {
              testContext.assertEquals(200, resp.statusCode());
              testContext.assertTrue(resp.headers().contains("x-request-id"));
              resp.bodyHandler(body -> {
                System.out.println(body.toString());
                testContext.assertEquals("20171024",
                                         body.toJsonObject().getString("version"));
                check.set(true);
              });
            })
            .putHeader("x-api-version", "20171024")
            .setChunked(true)
            .end();
    Awaitility.await().until(() -> check.get());
  }

  @Test
  public void testMatch20171124(TestContext testContext) {
    AtomicBoolean check = new AtomicBoolean();
    Vertx.vertx().createHttpClient().get(9000, "localhost", "/gray")
            .handler(resp -> {
              check.set(true);
              testContext.assertEquals(200, resp.statusCode());
              testContext.assertTrue(resp.headers().contains("x-request-id"));
              resp.bodyHandler(body -> {
                System.out.println(body.toString());
                testContext.assertEquals("20171124",
                                         body.toJsonObject().getString("version"));
              });
            })
            .putHeader("x-api-version", "20171124")
            .setChunked(true)
            .end();
    Awaitility.await().until(() -> check.get());
  }

  @Test
  public void testCeilMatch(TestContext testContext) {
    AtomicBoolean check = new AtomicBoolean();
    Vertx.vertx().createHttpClient().get(9000, "localhost", "/gray2")
            .handler(resp -> {
              testContext.assertEquals(200, resp.statusCode());
              testContext.assertTrue(resp.headers().contains("x-request-id"));
              resp.bodyHandler(body -> {
                System.out.println(body.toString());
                testContext.assertEquals("20171124",
                                         body.toJsonObject().getString("version"));
                check.set(true);
              });
            })
            .setChunked(true)
            .end();
    Awaitility.await().until(() -> check.get());
  }

  @Test
  public void testConflict(TestContext testContext) {
    AtomicBoolean check = new AtomicBoolean();
    Vertx.vertx().createHttpClient().get(9000, "localhost", "/gray3")
            .handler(resp -> {
              testContext.assertEquals(500, resp.statusCode());
              testContext.assertTrue(resp.headers().contains("x-request-id"));
              resp.bodyHandler(body -> {
                System.out.println(body.toString());
                testContext.assertEquals(DefaultErrorCode.CONFLICT.getNumber(),
                                         body.toJsonObject().getInteger("code"));
                check.set(true);
              });
            })
            .putHeader("x-api-version", "20171024")
            .setChunked(true)
            .end();
    Awaitility.await().until(() -> check.get());
  }

}
