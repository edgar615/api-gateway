package com.github.edgar615.direwolves.cmd;

import com.github.edgar615.util.exception.DefaultErrorCode;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Edgar on 2017/1/19.
 *
 * @author Edgar  Date 2017/1/19
 */
@RunWith(VertxUnitRunner.class)
public class ImportApiCmdTest extends BaseApiCmdTest {

  @Test
  public void testMissNameShouldThrowValidationException(TestContext testContext) {
    AtomicBoolean check = new AtomicBoolean();
    vertx.eventBus().<JsonObject>send("api.import", new JsonObject(), ar -> {
      if (ar.succeeded()) {
        testContext.fail();
      } else {
        ar.cause().printStackTrace();
        testContext.assertTrue(ar.cause() instanceof ReplyException);
        testContext.assertEquals(DefaultErrorCode.INVALID_ARGS.getNumber(),
                                 ReplyException.class.cast(ar.cause()).failureCode());
        check.set(true);
      }
    });
    Awaitility.await().until(() -> check.get());

  }

  @Test
  public void testImportDirSuccess(TestContext testContext) {
    JsonObject jsonObject = new JsonObject()
            .put("namespace", namespace)
            .put("path", "src/test/resources/api");
    AtomicBoolean check1 = new AtomicBoolean();
    vertx.eventBus().<JsonObject>send("api.import", jsonObject, ar -> {
      if (ar.succeeded()) {
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
    vertx.eventBus().<JsonObject>send("api.import", jsonObject, ar -> {
      if (ar.succeeded()) {
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
    vertx.eventBus().<JsonObject>send("api.import", jsonObject, ar -> {
      if (ar.succeeded()) {
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
