package com.github.edgar615.gateway.verticle;

import com.github.edgar615.gateway.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.gateway.core.apidiscovery.ApiDiscoveryOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/11/23.
 *
 * @author Edgar  Date 2017/11/23
 */
@RunWith(VertxUnitRunner.class)
public class FileApiDiscoveryVerticleTest {

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
  }

  @Test
  public void testImportDirSuccess(TestContext testContext) {
    ApiDiscovery discovery = ApiDiscovery.create(vertx,
                                                 new ApiDiscoveryOptions());
    JsonObject jsonObject = new JsonObject()
            .put("path", "src/test/resources/api");

    AtomicBoolean check1 = new AtomicBoolean();
    vertx.deployVerticle(FileApiDiscoveryVerticle.class,
                         new DeploymentOptions().setConfig            (jsonObject), ar ->{
              if (ar.succeeded()) {
                check1.set(true);
              } else {
                ar.cause().printStackTrace();
                testContext.fail();
              }
            } );
    Awaitility.await().until(() -> check1.get());

    AtomicBoolean check3 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      System.out.println(ar.result());
      testContext.assertEquals(2, ar.result().size());
      check3.set(true);
    });
    Awaitility.await().until(() -> check3.get());
  }

  @Test
  public void testImportFileSuccess(TestContext testContext) {
    ApiDiscovery discovery = ApiDiscovery.create(vertx,
                                                 new ApiDiscoveryOptions());
    JsonObject jsonObject = new JsonObject()
            .put("path", "src/test/resources/api/device_add.json");

    AtomicBoolean check1 = new AtomicBoolean();
    vertx.deployVerticle(FileApiDiscoveryVerticle.class,
                         new DeploymentOptions().setConfig            (jsonObject), ar ->{
              if (ar.succeeded()) {
                check1.set(true);
              } else {
                ar.cause().printStackTrace();
                testContext.fail();
              }
            } );
    Awaitility.await().until(() -> check1.get());

    AtomicBoolean check3 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      System.out.println(ar.result());
      testContext.assertEquals(1, ar.result().size());
      check3.set(true);
    });
    Awaitility.await().until(() -> check3.get());
  }

  @Test
  public void testInvalidJsonShouldNotAddAnyApi(TestContext testContext) {
    String namespace = UUID.randomUUID().toString();
    ApiDiscovery discovery = ApiDiscovery.create(vertx,
                                                 new ApiDiscoveryOptions());

    JsonObject jsonObject = new JsonObject()
            .put("path", "src/test/resources/invalid")
            .put("api.discovery", new JsonObject().put("name", namespace));

    AtomicBoolean check1 = new AtomicBoolean();
    vertx.deployVerticle(FileApiDiscoveryVerticle.class,
                         new DeploymentOptions().setConfig            (jsonObject), ar ->{
              if (ar.succeeded()) {
                check1.set(true);
              } else {
                ar.cause().printStackTrace();
                testContext.fail();
              }
            } );
    Awaitility.await().until(() -> check1.get());

    AtomicBoolean check3 = new AtomicBoolean();
    discovery.getDefinitions(new JsonObject(), ar -> {
      if (ar.failed()) {
        testContext.fail();
        return;
      }
      System.out.println(ar.result());
      testContext.assertEquals(0, ar.result().size());
      check3.set(true);
    });
    Awaitility.await().until(() -> check3.get());
  }

}
