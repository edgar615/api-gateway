package com.github.edgar615.gateway.verticle;

import com.github.edgar615.gateway.core.apidiscovery.ApiDiscovery;
import com.github.edgar615.gateway.core.apidiscovery.ApiDiscoveryOptions;
import com.github.edgar615.gateway.core.apidiscovery.ApiImporter;
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
 * Created by Edgar on 2017/1/19.
 *
 * @author Edgar  Date 2017/1/19
 */
@RunWith(VertxUnitRunner.class)
public class FileApiImporterTest {

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

    ApiImporter apiImporter = new FileApiImporter();
    AtomicBoolean check1 = new AtomicBoolean();
    discovery.registerImporter(apiImporter, jsonObject, ar -> {
      if (ar.succeeded()) {
        check1.set(true);
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });

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
    ApiImporter apiImporter = new FileApiImporter();
    discovery.registerImporter(apiImporter, jsonObject, ar -> {
      if (ar.succeeded()) {
        check1.set(true);
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
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
    JsonObject jsonObject = new JsonObject()
            .put("path", "src/test/resources/invalid");
    ApiDiscovery discovery = ApiDiscovery.create(vertx,
                                                 new ApiDiscoveryOptions());

    ApiImporter apiImporter = new FileApiImporter();
    AtomicBoolean check1 = new AtomicBoolean();
    discovery.registerImporter(apiImporter, jsonObject, ar -> {
      if (ar.succeeded()) {
        check1.set(true);
      } else {
        ar.cause().printStackTrace();
        testContext.fail();
      }
    });
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
