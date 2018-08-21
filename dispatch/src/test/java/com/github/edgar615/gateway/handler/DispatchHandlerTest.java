package com.github.edgar615.gateway.handler;

import static org.awaitility.Awaitility.await;

import com.github.edgar615.gateway.ApiUtils;
import com.github.edgar615.gateway.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.gateway.core.apidiscovery.ApiDiscoveryOptions;
import com.github.edgar615.gateway.verticle.ApiDispatchVerticle;
import com.github.edgar615.util.base.Randoms;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/1/3.
 *
 * @author Edgar  Date 2017/1/3
 */
@RunWith(VertxUnitRunner.class)
public class DispatchHandlerTest {

    ApiDiscovery apiDiscovery;

    Vertx vertx;

    int port = Integer.parseInt(Randoms.randomNumber(4));

    int devicePort = Integer.parseInt(Randoms.randomNumber(4));

    AtomicBoolean started = new AtomicBoolean();

    private String namespace = UUID.randomUUID().toString();

    private JsonObject config = new JsonObject()
            .put("namespace", namespace)
            .put("port", port)
            .put("api.discovery", new JsonObject()
                    .put("name", namespace));

    @Before
    public void setUp(TestContext testContext) {
        vertx = Vertx.vertx();

        apiDiscovery = ApiDiscovery.create(vertx, new ApiDiscoveryOptions().setName(namespace));
        ApiUtils.registerApi(apiDiscovery, devicePort);


        System.out.println(config);
        vertx.deployVerticle(ApiDispatchVerticle.class.getName(),
                             new DeploymentOptions().setConfig(config),
                             ar -> {
                                 if (ar.failed()) {
                                     ar.cause().printStackTrace();
                                 }
                                 started.set(true);
                             });

        vertx.deployVerticle(DeviceHttpVerticle.class.getName(),
                             new DeploymentOptions().setConfig(new JsonObject().put("port",
                                                                                    devicePort))
                                     .setWorker
                                             (true),
                             testContext.asyncAssertSuccess());
        await().until(() -> started.get());
    }

    @After
    public void tearDown(TestContext testContext) {
//    AtomicBoolean complete = new AtomicBoolean();
//    importer.close(ar -> {
//      complete.set(true);
//    });
//    Awaitility.await().until(() -> complete.set(true));

        vertx.close(ar -> {
            started.set(false);
        });
        await().until(() -> !started.get());
    }

    @Test
    public void testGetError(TestContext testContext) {
        Async async = testContext.async();
        vertx.createHttpClient()
                .get(port, "localhost",
                     "/devices/failed?timestamp=" + Instant.now().getEpochSecond(),
                     resp -> {
                         resp.bodyHandler(body -> {
                             System.out.println(body.toString());
                             System.out.println(resp.statusCode());
                             testContext.assertTrue(resp.statusCode() == 400);
                             String reqId = resp.getHeader("x-request-id");
                             testContext.assertNotNull(reqId);
                             async.complete();
                         });
                     }).end();
    }

    @Test
    public void testGetArray(TestContext testContext) {
        Async async = testContext.async();
        vertx.createHttpClient()
                .get(port, "localhost", "/v2/devices?timestamp=" + Instant.now().getEpochSecond(),
                     resp -> {
                         System.out.println(resp.statusCode());
                         resp.bodyHandler(body -> {
                             System.out.println(body.toString());
                             testContext.assertTrue(resp.statusCode() < 300);
                             JsonArray jsonArray = new JsonArray(body.toString());
                             testContext.assertEquals(2, jsonArray.size());
                             String reqId = resp.getHeader("x-request-id");
                             testContext.assertNotNull(reqId);
                             async.complete();
                         });
                     }).end();
    }

    @Test
    public void testGetObject(TestContext testContext) {
        Async async = testContext.async();
        int userId = Integer.parseInt(Randoms.randomNumber(5));
        vertx.createHttpClient()
                .get(port, "localhost",
                     "/devices/" + userId + "?timestamp="
                     + Instant.now().getEpochSecond(),
                     resp -> {
                         resp.bodyHandler(body -> {
                             System.out.println(body.toString());
                             testContext.assertTrue(resp.statusCode() < 300);
                             JsonObject jsonObject = new JsonObject(body.toString());
                             testContext.assertEquals(userId + "", jsonObject.getString("id"));
                             String reqId = resp.getHeader("x-request-id");
                             testContext.assertNotNull(reqId);
                             async.complete();
                         });
                     }).end();
    }

    @Test
    public void testPostObject(TestContext testContext) {
        Async async = testContext.async();
        vertx.createHttpClient()
                .post(port, "localhost",
                      "/devices?timestamp="
                      + Instant.now().getEpochSecond(),
                      resp -> {
                          resp.bodyHandler(body -> {
                              System.out.println(body.toString());
                              testContext.assertTrue(resp.statusCode() < 300);
                              JsonObject jsonObject = new JsonObject(body.toString());
                              testContext.assertEquals("bar",
                                                       jsonObject.getJsonObject("body")
                                                               .getString("foo"));
                              String reqId = resp.getHeader("x-request-id");
                              testContext.assertNotNull(reqId);
                              async.complete();
                          });
                      }).setChunked(true)
                .write(new JsonObject().put("foo", "bar").encode()).end();
    }

    @Test
    public void testPutObject(TestContext testContext) {
        Async async = testContext.async();
        int userId = Integer.parseInt(Randoms.randomNumber(5));
        vertx.createHttpClient()
                .put(port, "localhost",
                     "/devices/" + userId + "?timestamp="
                     + Instant.now().getEpochSecond(),
                     resp -> {
                         resp.bodyHandler(body -> {
                             System.out.println(body.toString());
                             testContext.assertTrue(resp.statusCode() < 300);
                             JsonObject jsonObject = new JsonObject(body.toString());
                             testContext.assertEquals("bar",
                                                      jsonObject.getJsonObject("body")
                                                              .getString("foo"));
                             String reqId = resp.getHeader("x-request-id");
                             testContext.assertNotNull(reqId);
                             async.complete();
                         });
                     }).setChunked(true)
                .end(new JsonObject().put("foo", "bar").encode());
    }
}
