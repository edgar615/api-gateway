package com.edgar.direwolves.cmd;

import com.edgar.direwolves.core.cmd.ApiCmd;
import com.edgar.direwolves.core.definition.ApiDefinition;
import com.edgar.direwolves.core.definition.ApiDiscovery;
import com.edgar.direwolves.verticle.ApiDefinitionRegistry;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/1/19.
 *
 * @author Edgar  Date 2017/1/19
 */
@RunWith(VertxUnitRunner.class)
public class ImportApiCmdTest {

  Vertx vertx;

  ApiDiscovery discovery;

  String namespace;
  ApiCmd cmd;
  @Before
  public void setUp() {
    namespace = UUID.randomUUID().toString();
    vertx = Vertx.vertx();
    discovery = ApiDiscovery.create(vertx, namespace);
    cmd = new ImportApiCmdFactory().create(vertx, new JsonObject());
  }

  @Test
  public void testImportDirSuccess(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
        .put("path", "src/test/resources/api");
    AtomicBoolean check1 = new AtomicBoolean();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                System.out.println(ar.result());
                check1.set(true);
              } else {
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

    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("path", "src/test/resources/api/device_add.json");
    AtomicBoolean check1 = new AtomicBoolean();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                System.out.println(ar.result());
                check1.set(true);
              } else {
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
            .put("namespace", namespace)
            .put("path", "src/test/resources/invalid");
    AtomicBoolean check1 = new AtomicBoolean();
    cmd.handle(jsonObject)
            .setHandler(ar -> {
              if (ar.succeeded()) {
                System.out.println(ar.result());
                check1.set(true);
              } else {
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
